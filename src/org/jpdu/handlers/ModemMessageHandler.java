package org.jpdu.handlers;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortTimeoutException;
import org.jpdu.Convert;

import org.jpdu.atutils.ATResponse;
import org.jpdu.atutils.ATResultCode;
import org.jpdu.atutils.ATMessageListItem;
import org.jpdu.pdu.PDUImpl;
import org.jpdu.pdu.WrongMessageTypeException;



public class ModemMessageHandler extends AbstractMessageEmitter implements MessageAcceptor, SerialPortEventListener {

  private static final int READ_TIMEOUT_MSEK = 1000;

  private String portName = null;

  private boolean debug = false;
  private String pinCode = "0000";
  private String modemInitString = "ATZ";
  private int baundrate = SerialPort.BAUDRATE_9600;
  private int dataBits = SerialPort.DATABITS_8;
  private int stopBits = SerialPort.STOPBITS_1;
  private int parity = SerialPort.PARITY_NONE;

  private boolean ready = false;
  private final Object serialDataMonitor = new Object();
  private PDUImpl outgoingPdu = null;


  public ModemMessageHandler(String name) {
    super(name);
  }

  public boolean isDebug() {
    return this.debug;
  }

  public void setDebug(boolean value) {
    this.debug = value;
  }

  public String getPortName() {
    return this.portName;
  }

  public void setPortName(String value) {
    this.portName = value;
  }

  public int getBaundrate() {
    return this.baundrate;
  }

  public void setBaundrate(int value) {
    this.baundrate = value;
  }

  public int getDataBits() {
    return this.dataBits;
  }

  public void setDataBits(int value) {
    this.dataBits = value;
  }

  public int getStopBits() {
    return this.stopBits;
  }

  public void setStopBits(int value) {
    this.stopBits = value;
  }

  public int getParity() {
    return this.parity;
  }

  public void setParity(int value) {
    this.parity = value;
  }

  public String getPinCode() {
    return pinCode;
  }

  public void setPinCode(String value) {
    this.pinCode = value;
  }

  public String getModemInitString() {
    return modemInitString;
  }

  public void setModemInitString(String value) {
    this.modemInitString = value;
  }

  @Override
  public void run() {
    if (portName == null) {
      System.out.print("Port name is not defined, exiting");
      return;
    }
    SerialPort port = new SerialPort(portName);
    try {
      port.openPort();
      port.setParams(getBaundrate(), getDataBits(), getStopBits(), getParity());
      port.addEventListener(this);
      ready = initializeModem(port);
      if (ready) {
        checkStoredMessages(port);
        while(port.isOpened() && !Thread.currentThread().isInterrupted()) {
          synchronized(serialDataMonitor) {
            serialDataMonitor.wait(5000);
            if (port.getInputBufferBytesCount() > 0) {
              try {
                ATResultCode rc = readUnsolicitedResultCode(port);
                handleResultCode(rc, port);
              } catch(IOException e) {
                // Ignore for now
              }
              continue;
            }
            if (outgoingPdu != null) {
              sendMessage(port, outgoingPdu);
              outgoingPdu = null;
            }
          }
        }
      }
      port.closePort();
    } catch(SerialPortException e) {
      System.out.print(e);
    } catch(InterruptedException e) {
      System.out.println("ModemMessageHandler thread interrupted, exiting");
    }
  }

  @Override
  public void serialEvent(SerialPortEvent spe) {
    synchronized(serialDataMonitor) {
      if (spe.getEventValue() > 0) {
        serialDataMonitor.notify();
      }
    }
  }

  @Override
  public Result acceptMessage(PDUImpl message) {
    synchronized(serialDataMonitor) {
      if (ready) {
        outgoingPdu = message;        
        serialDataMonitor.notify();
        System.out.println("Some message accepted");
        return Result.Accepted;
      }
      return Result.NotReady;
    }
  }

  private void handleResultCode(ATResultCode rc, SerialPort port) throws SerialPortException, InterruptedException {
    String code = rc.getResultCode();
    switch(code) {
      case "RING":
      case "+CRING":
        try {
          ATResultCode cliprc = readUnsolicitedResultCode(port);
          if ("+CLIP".equals(cliprc.getResultCode())) {
            if (debug) System.out.println("h> caller ID: " + cliprc.getResultValue().toString());
          }
        } catch (IOException e) {
          if (debug) System.out.println("h> cannot get caller Id");
        }
        if (debug) System.out.println("h> Rejecting incomming call");
        sendATCommand(port, "AT+CHUP");
        break;
      case "+CMT":
        PDUImpl pdu = (PDUImpl)rc.getResultValue();
        if (pdu != null) {
          try {
            System.out.print("h> incoming message: ");
            System.out.println(pdu.getUserData().getUserDataText());
            boolean accepted = emitMessage(pdu);
            sendATCommand(port, "AT+CNMA=" + (accepted ? "1" : "2"));
          } catch (WrongMessageTypeException e) {
            System.out.println("h> " + e.toString());
          }
        } else {
          System.out.println("h> incoming message missed");
        }
        break;
      default:
        if (debug) System.out.println("h> ignore " + code);
        break;
    }
    // TODO: HUAWEI specific:
    // TODO: we have handle it well and think how to handle events from other modems
    // if ^CURC=1 (default) modem will send the next notificartions periodically
    // ^DSFLOWRPT,
    // ^RSSI,
    // ^MODE,
    // ^SIMST,
    // ^SRVST,
    // ^MODE,
    // ^EARST,
    // ^SMMEMFULL - Message Memory Full    
  }

  private ATResponse sendATCommand(SerialPort port, String command) throws SerialPortException, InterruptedException {    
    port.writeString(command);
    port.writeByte((byte)13); // TODO: check S3 (ATS3?)
    if (debug) System.out.print("-< " + command + "\r\n");
    try {
      ATResponse resp = readATResponse(port);
      return resp;
    } catch (IOException e) {
      if (debug) System.out.println("Command execution failed");
      return null;
    }
  }
  
  private ATResponse sendMessage(SerialPort port, PDUImpl pdu) throws SerialPortException, InterruptedException {
    synchronized(serialDataMonitor) {
      byte[] octets = pdu.getBytes();
      int length = octets.length - 1; // minus SMS-center zero address
      port.writeString("AT+CMGS=" + Integer.toString(length));
      port.writeByte((byte)13);      
      try {
        String prompt = port.readString(4, READ_TIMEOUT_MSEK);
        if ("\r\n> ".equals(prompt)) {
          if (debug) System.out.println(Convert.bytesToHexString(octets));
          port.writeString(Convert.bytesToHexString(octets));
          port.writeByte((byte)26);
          serialDataMonitor.wait(READ_TIMEOUT_MSEK * 5);
          ATResponse result = readATResponse(port);
          return result;
        }
      } catch(IOException e) {
        return null;
      } catch (SerialPortTimeoutException e) {
        return null;
      }
      return null;
    }
  }

  private ATResponse readATResponse(SerialPort port) throws IOException, SerialPortException {
    synchronized(serialDataMonitor) {
      if (debug) System.out.print("->");
      try {
        return new ATResponse(new SerialInputStream(port, READ_TIMEOUT_MSEK, debug));
      } catch(IOException e) {
        Throwable t = e.getCause();
        if (t instanceof SerialPortException) {
          throw (SerialPortException)t;
        } else {
          throw e;
        }
      }
    }
  }

  private ATResultCode readUnsolicitedResultCode(SerialPort port) throws IOException, SerialPortException {
    synchronized(serialDataMonitor) {
      if (debug) System.out.print("u>");
      try {
        return new ATResultCode(new SerialInputStream(port, READ_TIMEOUT_MSEK, debug));
      } catch(IOException e) {
        Throwable t = e.getCause();
        if (t instanceof SerialPortException) {
          throw (SerialPortException)t;
        } else {
          throw e;
        }
      }
    }
  }

  private boolean initializeModem(SerialPort port) throws SerialPortException, InterruptedException {
    ATResponse result = sendATCommand(port, "AT");
    if (!result.isOk()) {
      System.out.println("Device on port " + port.getPortName() + " not alive or not a Hayes modem, cannot continue");
      return false;
    }

    result = sendATCommand(port, "ATE0");
    if (!result.isOk()) {
      System.out.println("er " + result.getError());
      return false;
    }

    result = sendATCommand(port, this.getModemInitString());
    if (!result.isOk()) {
      System.out.println("er " + result.getError());
      return false;
    }

    result = sendATCommand(port, "AT+GCAP");
    if (!result.isOk()) {
      System.out.println("er " + result.getError());
      return false;
    } else {
      ATResultCode rc = result.getFirstOf("+GCAP");
      Object value = rc.getResultValue();
      if (value == null || !((Set<String>)value).contains("+CGSM")) {
        System.out.println("This AT-device do not support GSM, cannot continue");
        return false;
      }
    }

    // TODO: check failed PIN input attemps limit and go fail if there is one only
    result = sendATCommand(port, "AT+CPIN?");
    if (!result.isOk()) {
      System.out.println("er " + result.getError());
      return false;
    }
    ATResultCode cpin = result.getFirstOf("+CPIN");
    String pinStatus = (String)cpin.getResultValue();
    if ("SIM PIN".equals(pinStatus)) {
      result = sendATCommand(port, "AT+CPIN=" + this.getPinCode());
      if (!result.isOk()) {
        System.out.println("er " + result.getError());
        return false;
      }
    } else if (!"READY".equals(pinStatus)) {
      System.out.println("PIN status is: '"+ pinStatus + "' cannot continue");
      return false;
    }

    if (debug) {
      sendATCommand(port, "ATI");
      sendATCommand(port, "AT+CREG?"); // TODO: implement +CREG codes to have a control on network state
    }

    sendATCommand(port, "AT+CSSN=1,1"); // Supplementary service notifications
    if (!result.isOk()) {
      System.out.println("er " + result.getError());
      return false;
    }
    
    sendATCommand(port, "AT+CLIP=1"); // Caller id indication enabled
    if (!result.isOk()) {
      System.out.println("er " + result.getError());
      return false;
    }

    sendATCommand(port, "AT+CRC=1"); // Caller id indication enabled
    if (!result.isOk()) {
      System.out.println("er " + result.getError());
      return false;
    }

    sendATCommand(port, "AT+CMGF=0"); // PDUImpl mode
    if (!result.isOk()) {
      System.out.println("er " + result.getError());
      return false;
    }

    // <mode> 1 - Discard indication and reject new received message unsolicited result codes when TA-TE link is reserved
    // <mt>   2 - SMS-DELIVERs (except class 2 messages and messages in the message waiting indication group (store message)) are routed directly to the TE.
    // <bm>   2 - New CBMs are routed directly to the TE
    // <ds>   1 - SMS-STATUS-REPORTs are routed to the TE
    // <bfr>  0 - TA buffer of unsolicited result codes defined within this command is flushed to the TE when <mode> 1...3
    // see 3gpp 27.005 3.4.1
    // TODO: check modes available with AT+CNMI=?
    // NOTE: Some combinations may not work, for example mode 2 (buffering) is incompatioble with mt 2
    result = sendATCommand(port, "AT+CNMI=1,2,2,1,0");
    if (!result.isOk()) {
      System.out.println("er " + result.getError());
      return false;
    }
    return true;
  }

  private void checkStoredMessages(SerialPort port) throws SerialPortException, InterruptedException {
    synchronized(serialDataMonitor) {
      ATResponse resp = this.sendATCommand(port, "AT+CMGl=4");
      if (resp.isOk()) {
        for(ATResultCode rc : resp.getAlltOf("+CMGL")) {
          ATMessageListItem item = (ATMessageListItem)rc.getResultValue();
          if ( item.getStatus() == ATMessageListItem.Status.receivedRead ||
               item.getStatus() == ATMessageListItem.Status.receivedUnread ) {
            PDUImpl pdu = item.getPdu();
            if (emitMessage(pdu)) {
              ATResponse dresp = this.sendATCommand(port, "AT+CMGD=" + item.getIndex());
              if (!dresp.isOk()) {
                System.out.println("Unable delete message index: " + item.getIndex());
                // TODO: think how to avoid message read duplication this case
              }
            }
          }
        }
      } else {
        System.out.print(resp.getError());
      }
    }
  }


  private class SerialInputStream extends InputStream {

    private final SerialPort port;
    private final int timeout;
    private final boolean debug;

    SerialInputStream(SerialPort port, int timeout, boolean debug) {
      this.port = port;
      this.timeout = timeout;
      this.debug = debug;
    }

    @Override
    public int read() throws IOException {
      try {
        byte[] b = port.readBytes(1, timeout);       
        if (this.debug) System.out.print((char)b[0]);
        return b[0];
      } catch (SerialPortException e) {
        throw new IOException(e);
      } catch (SerialPortTimeoutException e) {
        throw new EOFException();
      }
    }
    
  }

}
