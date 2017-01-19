package org.dancecoder.smailer.gateway.modem;

import java.util.PrimitiveIterator;

public class CmeErrorParser implements Parser {

		private static final String[] CME_GENERAL_ERRORS = new String[] {
		/* 0 */ "phone failure",
		/* 1 */ "no connection to phone",
		/* 2 */ "phone‑adaptor link reserved",
		/* 3 */ "operation not allowed",
		/* 4 */ "operation not supported",
		/* 5 */ "PH‑SIM PIN required",
		/* 6 */ "PH-FSIM PIN required",
		/* 7 */ "PH-FSIM PUK required",
		null, null,
		/* 10 */ "SIM not inserted",
		/* 11 */ "SIM PIN required",
		/* 12 */ "SIM PUK required",
		/* 13 */ "SIM failure",
		/* 14 */ "SIM busy",
		/* 15 */ "SIM wrong",
		/* 16 */ "incorrect password",
		/* 17 */ "SIM PIN2 required",
		/* 18 */ "SIM PUK2 required",
		null,
		/* 20 */ "memory full",
		/* 21 */ "invalid index",
		/* 22 */ "not found",
		/* 23 */ "memory failure",
		/* 24 */ "text string too long",
		/* 25 */ "invalid characters in text string",
		/* 26 */ "dial string too long",
		/* 27 */ "invalid characters in dial string",
		null, null,
		/* 30 */ "no network service",
		/* 31 */ "network timeout",
		/* 32 */ "network not allowed - emergency calls only",
		null,null,null,null,null,null,null,
		/* 40 */ "network personalization PIN required",
		/* 41 */ "network personalization PUK required",
		/* 42 */ "network subset personalization PIN required",
		/* 43 */ "network subset personalization PUK required",
		/* 44 */ "service provider personalization PIN required",
		/* 45 */ "service provider personalization PUK required",
		/* 46 */ "corporate personalization PIN required",
		/* 47 */ "corporate personalization PUK required",
		/* 48 */ "hidden key required (See NOTE 2)",
		/* 49 */ "EAP method not supported",
		/* 50 */ "Incorrect parameters",
		/* 51 */ "command implemented but currently disabled",
		/* 52 */ "command aborted by user",
		/* 53 */ "not attached to network due to MT functionality restrictions",
		/* 54 */ "modem not allowed - MT restricted to emergency calls only",
		/* 55 */ "operation not allowed because of MT functionality restrictions",
		/* 56 */ "fixed dial number only allowed - called number is not a fixed dial number (refer 3GPP TS 22.101 [147])",
		/* 57 */ "temporarily out of service due to other MT usage",
		/* 58 */ "language/alphabet not supported",
		/* 59 */ "unexpected data value",
		/* 60 */ "system failure",
		/* 61 */ "data missing",
		/* 62 */ "call barred",
		/* 63 */ "message waiting indication subscription failure",
		null,null,null,null,null,null,null,null,null,null,null,null,
		null,null,null,null,null,null,null,null,null,null,null,null,
		null,null,null,null,null,null,null,null,null,null,null,null,
		/* 100 */ "unknown"
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
				int code = Integer.valueOf(buffer.toString());
				String text = null;
				if (code < 101) {
					text = CME_GENERAL_ERRORS[code];
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
