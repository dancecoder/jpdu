package org.dancecoder.smailer;

import java.util.List;
import org.dancecoder.smailer.gateway.Gateway;
import org.dancecoder.smailer.gateway.ModemGateway;
import org.dancecoder.smailer.model.MessageListItem;
import org.dancecoder.smailer.model.UnsupportedEncodingException;
import org.dancecoder.smailer.model.UssdResponse;
import org.dancecoder.smailer.pdu.PDU;
import org.dancecoder.smailer.pdu.WrongMessageTypeException;


public class SMailer {

	static Gateway gateway;
	
	public static void main(String[] args) throws WrongMessageTypeException {

		gateway = new ModemGateway("COM10");
		gateway.initialize();
		//try {
		//	UssdResponse rsp = gateway.sendUssd("*105#");
		//	System.out.println(rsp.getValue());
		//}	catch(UnsupportedEncodingException e) {			
		//	System.out.print(e);
		//}		
		
		List<MessageListItem> msgList = gateway.getSmsList();
		for (MessageListItem item : msgList) {
			PDU pdu = item.getPdu();
			//System.out.println(pdu.getMscAddress().toString());
			System.out.println(pdu.getMessageTypeIndicator());
			
		}
		
		gateway.close();

	}

}
