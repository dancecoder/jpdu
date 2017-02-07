package org.dancecoder.smailer;

import java.util.List;
import org.dancecoder.smailer.gateway.Gateway;
import org.dancecoder.smailer.gateway.ModemGateway;
import org.dancecoder.smailer.model.MessageListItem;
import org.dancecoder.smailer.model.UnsupportedEncodingException;
import org.dancecoder.smailer.model.UssdResponse;
import org.dancecoder.smailer.pdu.PDU;
import org.dancecoder.smailer.pdu.PduType;
import org.dancecoder.smailer.pdu.WrongMessageTypeException;


public class SMailer {

  public static void main(String[] args) throws WrongMessageTypeException {

    try(Gateway gateway = new ModemGateway("COM5")) {
      gateway.initialize();
      //try {
      //  UssdResponse rsp = gateway.sendUssd("*105#");
      //  System.out.println(rsp.getValue());
      //} catch(UnsupportedEncodingException e) {
      //  System.out.print(e);
      //}

      List<MessageListItem> msgList = gateway.getSmsList();
      if (msgList != null) {
        for (MessageListItem item : msgList) {
          PDU pdu = item.getPdu();
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
}
