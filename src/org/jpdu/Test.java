package org.jpdu;



import java.util.List;
import org.jpdu.gateway.Gateway;
import org.jpdu.gateway.ModemGateway;
import org.jpdu.atutils.ATMessageListItem;
import org.jpdu.handlers.DirectoryInboundHandler;
import org.jpdu.handlers.DirectoryOutboundHandler;
import org.jpdu.handlers.ModemMessageHandler;
import org.jpdu.model.UnsupportedEncodingException;
import org.jpdu.model.UssdResponse;
import org.jpdu.pdu.PDUImpl;
import org.jpdu.pdu.PduType;
import org.jpdu.pdu.WrongMessageTypeException;


public class Test {

  public static void main(String[] args) throws WrongMessageTypeException {

    //Gateway gateway = new ModemGateway("COM5", "0000");
    //Thread t = new Thread(gateway, "modemgateway");
    //t.start();

    ModemMessageHandler modem = new ModemMessageHandler("modem");
    modem.setDebug(true);
    modem.setPortName("COM5");
    Thread t0 = new Thread(modem, modem.getName());
    t0.start();
    
    DirectoryOutboundHandler outbound = new DirectoryOutboundHandler("outbound");
    outbound.setStoragePath("d:\\temp\\jpdu_outbound");
    outbound.addMessageAcceptor(modem);
    Thread t1 = new Thread(outbound, outbound.getName());
    t1.start();
    
    DirectoryInboundHandler inbound = new DirectoryInboundHandler("inbound");
    inbound.setStoragePath("d:\\temp\\jpdu_inbound");
    Thread t2 = new Thread(inbound, inbound.getName());
    t2.start();

    modem.addMessageAcceptor(inbound);



      //try {
        //UssdResponse rsp = gateway.sendUssd("*105#"); // Баланс
        //UssdResponse rsp = gateway.sendUssd("*201#"); // узнать свой номер
        //System.out.println(rsp.getValue());
      //} catch(UnsupportedEncodingException e) {
        //System.out.print(e);
      //}

      //List<MessageListItem> msgList = gateway.getSmsList();
      List<ATMessageListItem> msgList = null;
      if (msgList != null) {
        for (ATMessageListItem item : msgList) {
          PDUImpl pdu = item.getPdu();
          System.out.print("MscAddress: ");
          System.out.println(pdu.getMscAddress().toString());
          System.out.print("Pdu type: ");
          System.out.println(pdu.getType());
          if (pdu.getType().getMessageTypeIndicator() == PduType.SMS_DELIVER) {
            System.out.print("OriginatingAddress: ");
            System.out.println(pdu.getOriginatingAddress());
            System.out.print("ProtocolIdentifier: ");
            System.out.println(pdu.getProtocolIdentifier());
            System.out.print("DataCodingScheme: ");
            System.out.println(pdu.getDataCodingScheme());
            System.out.print("ServiceCentreTimeStamp: ");
            System.out.println(pdu.getServiceCentreTimeStamp());
            System.out.print("UserDataLength: ");
            System.out.println(pdu.getUserDataLength());
            System.out.print("UserData: ");
            System.out.println(pdu.getUserData());
          }
          System.out.println();
        }
      }
    }
  
}
