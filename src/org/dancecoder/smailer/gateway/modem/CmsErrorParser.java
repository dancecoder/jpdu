package org.dancecoder.smailer.gateway.modem;

import java.util.PrimitiveIterator;

public class CmsErrorParser implements Parser {
	
	private static final String[] MESSAGES = new String[]{
		"Unassigned (unallocated) number",	
		"Operator determined barring",
		"Call barred",
		"Short message transfer rejected",
		"Destination out of service",
		"Unidentified subscriber",
		"Facility rejected",
		"Unknown subscriber",
		"Network out of order",
		"Temporary failure",
		"Congestion",
		"Resources unavailable, unspecified",
		"Requested facility not subscribed",
		"Requested facility not implemented",
		"Invalid short message transfer reference value",
		"Invalid message, unspecified",
		"Invalid mandatory information",
		"Message type non‑existent or not implemented",
		"Message not compatible with short message protocol state",
		"Information element non‑existent or not implemented",
		"Protocol error, unspecified",
		"Interworking, unspecified",
		"Telematic interworking not supported",
		"Short message Type 0 not supported",
		"Cannot replace short message",
		"Unspecified TP-PID error",
		"Data coding scheme (alphabet) not supported",
		"Message class not supported",
		"Unspecified TP-DCS error",
		"Command cannot be actioned",
		"Command unsupported",
		"Unspecified TP-Command error",
		"TPDU not supported",
		"SC busy",
		"No SC subscription",
		"SC system failure",
		"Invalid SME address",
		"Destination SME barred",
		"SM Rejected-Duplicate SM",
		"TP-VPF not supported",
		"TP-VP not supported",
		"(U)SIM SMS storage full",
		"No SMS storage capability in (U)SIM",
		"Error in MS",
		"Memory Capacity Exceeded",
		"(U)SIM Application Toolkit Busy",
		"(U)SIM data download error",
		"ME failure",
		"SMS service of ME reserved",
		"operation not allowed",
		"operation not supported",
		"invalid PDU mode parameter",
		"invalid text mode parameter",
		"(U)SIM not inserted",
		"(U)SIM PIN required",
		"PH-(U)SIM PIN required",
		"(U)SIM failure",
		"(U)SIM busy",
		"(U)SIM wrong",
		"(U)SIM PUK required",
		"(U)SIM PIN2 required",
		"(U)SIM PUK2 required",
		"memory failure",
		"invalid memory index",
		"memory full",
		"SMSC address unknown",
		"no network service",
		"network timeout",
		"no +CNMA acknowledgement expected",
		"unknown error"
	};
	
	private static final String[] INDEX = new String[]{
		// 3GPP TS 24.011 clause E.2 values 
		"1",  "8",  "10", "21", "27", "28", "29", "30", "38", "41",
		"42", "47", "50", "69", "81", "95", "96", "97", "98", "99",
		"111", "127",
		// 3GPP TS 23.040 clause 9.2.3.22 values
		"128", "129", "130", "143",
		"144", "145", "159",
		"160", "161", "175",
		"176",
		"192", "193", "194", "195", "196", "197", "198", "199",
		"208", "209", "210", "211", "212", "213",
		// 3GPP 27.005 TS clause 3.2.5 values
		"300", "301", "302", "303", "304", "305", "310", "311", "312", "313", "314",
		"315", "316", "317", "318", "320", "321", "322", "330", "331", "332", "340",
		"500"
	};
	
	@Override
	public Object parse(PrimitiveIterator.OfInt iterator) {
		StringBuilder buffer = new StringBuilder();
		char c;
		while(iterator.hasNext()) {
			c = (char)iterator.nextInt();
			if (buffer.length() == 0 && c == ' ') continue;
			if (c == 13) continue;
			if (c == 10) {
				String code = buffer.toString();
				String text = null;
				for (int i = 0; i < INDEX.length; i++) {
					if (INDEX[i].equals(code)) {
						text = MESSAGES[i];
						break;
					}
				}
				if (text == null) {
					text = "Unspecified error ("+code+")";
				}
				return text;				
			} else {
				buffer.append((char)c);
			}
		}		
		return null;
	}
	
}
