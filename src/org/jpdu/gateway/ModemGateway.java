package org.jpdu.gateway;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

import org.jpdu.atutils.ATResponse;
import org.jpdu.TextConverter;
import org.jpdu.TextEncoding;
import org.jpdu.atutils.ATResultCode;
import org.jpdu.atutils.ATMessageListItem;
import org.jpdu.model.UnsupportedEncodingException;
import org.jpdu.model.UssdResponse;
import org.jpdu.model.UssdResponseType;


public class ModemGateway implements SerialPortEventListener, Gateway {

  private static final String[] REQUIRED_COMMANDS = new String[] {
    "+CPIN", "+CUSD", "+CMGF"
  };

  private final SerialPort port;
  private final String pincode;

  private final Object dataMonitor = new  Object();

  private String lastCommand = "";
  private String lastData = "";

  private List<String> supportedCommands;


  private boolean isRinging = false;

  public ModemGateway(String portName, String pin) {
    port = new SerialPort(portName);
    pincode = pin;
  }

  @Override
  public void initialize() {
    try {
      port.openPort();
      port.setParams(
        SerialPort.BAUDRATE_9600,
        //SerialPort.BAUDRATE_14400,
        //SerialPort.BAUDRATE_19200,
        SerialPort.DATABITS_8,
        SerialPort.STOPBITS_1,
        SerialPort.PARITY_NONE
      );
      port.setFlowControlMode(
        SerialPort.FLOWCONTROL_RTSCTS_IN |
        SerialPort.FLOWCONTROL_RTSCTS_OUT
      );
      port.addEventListener( this, SerialPort.MASK_RXCHAR );
    } catch (SerialPortException ex) {
      System.out.println(ex);
    }

    try {
      synchronized(this){
        this.wait(100); // wait some loss data
      }
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    ATResponse result = sendATCommand("ATZ E0"); // TODO: move this init string to config
    if (!result.isOk()) {
      System.out.println(result.getFirstOf("+CME ERROR").getResultValue());
      throw new RuntimeException("unable to initialize modem");
    }

    result = sendATCommand("AT+CPIN?");
    String error = (String)result.getFirstOf("+CME ERROR").getResultValue();
    if (error != null) {
      throw new RuntimeException(error);
    }
    String cpin = (String)result.getFirstOf("+CPIN").getResultValue();    
    if (cpin.equals("SIM PIN")) {
      sendATCommand("AT+CPIN=" + pincode); // TODO: move PIN to config
      try {
        synchronized(this){
          this.wait(1000); // whait SIM initialised
        }
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    } else if (!cpin.equals("READY")) {
      throw new RuntimeException("PIN status is: '"+ cpin + "' cannot continue");
    }

    //collectATCommandList();
  }

  @Override
  public void close() {
    try {
      this.port.purgePort(SerialPort.PURGE_RXCLEAR | SerialPort.PURGE_TXCLEAR);
      this.port.closePort();
    } catch(SerialPortException e) {
      System.out.println(e);
    }
  }

  @Override
  public UssdResponse sendUssd(String data) throws UnsupportedEncodingException {
    //modem.sendATCommand("at+cscs?");
    String s = TextConverter.convert(data, TextEncoding.ira7bitHex);

    ATResponse resp = this.sendATCommand("AT+CUSD=1,\"" + s + "\",15", "\r\n+CUSD: ");
    if (!resp.isOk()) {
      System.out.println(resp.getFirstOf("+CPIN").getResultValue());
      return null;
    }
    String[] answ = resp.getFirstOf("+CUSD").getResultValue().toString().split(",");
    int answLength = answ.length;

    UssdResponse rsp = new UssdResponse();

    if (answLength > 0) {
      switch (answ[0]) {
        case "0": rsp.setType(UssdResponseType.noActionRequired); break;
        case "1":
          rsp.setType(UssdResponseType.userActionRequired);
          this.sendATCommand("AT+CUSD=2");
          break;
        case "2": rsp.setType(UssdResponseType.terminatedByNetwork); break;
        case "3": rsp.setType(UssdResponseType.otherLocalClientResponded); break;
        case "4": rsp.setType(UssdResponseType.operationNotSupported); break;
        case "5": rsp.setType(UssdResponseType.networkTimeOut); break;
        default:  rsp.setType(UssdResponseType.unknown);
      }
    } else {
      rsp.setType(UssdResponseType.unknown);
    }

    if (answLength > 1) {
      String value = answ[1].trim();
      if (value.charAt(0) == '"') {
        value = value.substring(1, value.length() -1);
      }
      if (answLength > 2) {
        String encoding = answ[2];
        switch (encoding) {
          case "0":
          case "1":
          case "15":
            rsp.setValue(TextConverter.parse(value, TextEncoding.ira7bitHex));
            break;
          case "72":
            rsp.setValue(TextConverter.parse(value, TextEncoding.ucs2Hex));
            break;
          default:
            throw new UnsupportedEncodingException();
        }
      }
    }

    return rsp;
  }

  @Override
  public void sendSms() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void getSms() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public List<ATMessageListItem> getSmsList() {
    // TODO: +CMGF check modes available;
    ATResponse resp = this.sendATCommand("AT+CMGF=0;+CMGl=4;");
    if (!resp.isOk()) {
      System.out.println(resp.getFirstOf("+CMS ERROR").getResultValue());
      return null;
    } else {
      List<ATMessageListItem> msgList = new ArrayList<>();
      for (ATResultCode rc : resp.getAlltOf("+CMGL")) {
        msgList.add((ATMessageListItem)rc.getResultValue());
      }
      return msgList;
    }
  }

  @Override
  public void serialEvent(SerialPortEvent event) {
    System.out.print("<" + event.getEventType());
    System.out.print("<" + (event.isRXCHAR() ? "RX" : "??"));
    if(event.isRXCHAR()){
      int octetCount = event.getEventValue();
      if ( octetCount > 0 ) {
        try  {          
          synchronized(dataMonitor){
            lastData = port.readString(octetCount);
            dataMonitor.notify();
          }
          if ("\r\nRING\r\n".equals(lastData)) {
            isRinging = true;
          }
          System.out.println("<" + lastData);
        } catch(SerialPortException e) {
          System.out.println(e);
        }
      } else {        
        synchronized(dataMonitor){
          lastData = "";
          dataMonitor.notify();
        }
        System.out.println("octet count = 0");
      }
    } else {
      System.out.println("type: " + event.getEventType() + " value: " + event.getEventValue());
    }
  }

  public ATResponse sendATCommand(String command) {
    return sendATCommand(command, "\r\nOK\r\n");
  }

  public ATResponse sendATCommand(String command, String whaitFor) {
    if (port.isOpened()) {
      synchronized(dataMonitor){
        lastData = null;
        try {
          System.out.println(">" + command);
          port.writeString(command);
          port.writeByte((byte)10);
          port.writeByte((byte)13);
          String data = null;
          while(data == null || !data.contains(whaitFor) && !data.contains("ERROR")) {
            dataMonitor.wait(1000);
            data += lastData;
          }
          try {
            return new ATResponse(data);
          } catch(IOException e) {
            return null;
          }
        } catch (SerialPortException | InterruptedException e) {        
          System.out.println(e);
        }
      }
    }
    throw new RuntimeException("command called on closed port");
  }

  @Override
  public void run() {
    initialize();
    while (this.port.isOpened()) {
      try {
        if (isRinging) {
          ATResponse resp = sendATCommand("ATH");
          if (resp.isOk()) {
            isRinging = false;
          } else {
            throw new RuntimeException("Cannot halt ring");
          }
        }
        synchronized(dataMonitor){
          dataMonitor.wait(1000);
        }
      } catch (InterruptedException ex) {
        System.out.println(ex);
      }
    }
  }

}
