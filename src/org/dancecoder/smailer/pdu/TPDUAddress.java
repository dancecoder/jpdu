/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dancecoder.smailer.pdu;

import java.util.Arrays;

/**
 *
 * @author Odoom
 */
public class TPDUAddress extends Address {
	
	private TPDUAddress(byte[] octets, int from, int length) {
		super(octets, from, length);
	}
	
	public static Address Create(byte[] octets, int from) {
		int nibbleLength = octets[from];
		int octetLength = nibbleLength % 2 == 0 ? nibbleLength / 2 : nibbleLength / 2 + 1;
		return new TPDUAddress(octets, from, octetLength);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{ ");
		sb.append("type: '");
		sb.append(this.getType().toString());
		sb.append("', ");
		sb.append("numberingPlan: '");
		sb.append(this.getNumberingPlan().toString());
		sb.append("',");
		sb.append("number: '");
		int b;
		for (int i = from+2; i < from+length+1 ; i++) {			
			sb.append(this.octets[i] & 15);
			b = this.octets[i] >> 4 & 15;
			if (b < 10) {
				sb.append(this.octets[i] >> 4 & 15);
			}			
		}
		sb.append("'");
		sb.append(" }");
		return sb.toString();
	}
}
