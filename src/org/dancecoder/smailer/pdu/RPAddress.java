package org.dancecoder.smailer.pdu;

import java.util.Arrays;

/**
 * Relay protocol layer Address
 * Based on 3GPP TS 24.011
 * see also 3GPP TS 23.040 section 9.1.2.5
*/
public class RPAddress extends Address {
	
	private RPAddress(byte[] octets, int from, int length) {
		super(octets, from, length);
	}

	public static Address Create(byte[] octets, int from) {
		int octetLength = octets[from];
		return new RPAddress(octets, from, octetLength);
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
