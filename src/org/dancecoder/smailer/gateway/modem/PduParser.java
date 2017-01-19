/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dancecoder.smailer.gateway.modem;

import java.util.PrimitiveIterator;
import org.dancecoder.smailer.TextConverter;
import org.dancecoder.smailer.pdu.PDU;

/**
 *
 * @author Odoom
 */
public class PduParser implements Parser {
	
	@Override
	public Object parse(PrimitiveIterator.OfInt iterator) {
		StringBuilder buffer = new StringBuilder();
		char c;
		while(iterator.hasNext()) {
			c = (char)iterator.nextInt();
			if (buffer.length() == 0 && c == ' ') continue;
			if (c == 13) continue;
			if (c == 10) {				
				break;
			} else {
				buffer.append(c);
			}
		}
		String pdu = buffer.toString();
		byte[] bytes = TextConverter.unhex(pdu);
		return new PDU(bytes);
	}
	
}
