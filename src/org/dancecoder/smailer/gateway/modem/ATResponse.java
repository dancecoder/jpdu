package org.dancecoder.smailer.gateway.modem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PrimitiveIterator;

public class ATResponse {
	
	enum State {
		begin,
		newLine,
		command,
		result
	}
	
	private static final String[] CME_GENERAL_ERRORS = new String[] {
		/* 0 */ "phone failure",
		/* 1 */ "no connection to phone",
		/* 2 */ "phone‑adaptor link reserved",
		/* 3 */ "operation not allowed",
		/* 4 */ "operation not supported",
		/* 5 */ "PH‑SIM PIN required",
		/* 6 */ "PH-FSIM PIN required",
		/* 7 */ "PH-FSIM PUK required",
		"error 8: unknown", "error 9: unknown", 
		/* 10 */ "SIM not inserted",
		/* 11 */ "SIM PIN required",
		/* 12 */ "SIM PUK required",
		/* 13 */ "SIM failure",
		/* 14 */ "SIM busy",
		/* 15 */ "SIM wrong",
		/* 16 */ "incorrect password",
		/* 17 */ "SIM PIN2 required",
		/* 18 */ "SIM PUK2 required",
		"error 19: unknown",
		/* 20 */ "memory full",
		/* 21 */ "invalid index",
		/* 22 */ "not found",
		/* 23 */ "memory failure",
		/* 24 */ "text string too long",
		/* 25 */ "invalid characters in text string",
		/* 26 */ "dial string too long",
		/* 27 */ "invalid characters in dial string",
		"error 28: unknown","error 29: unknown",
		/* 30 */ "no network service",
		/* 31 */ "network timeout",
		/* 32 */ "network not allowed - emergency calls only",
		"error 33: unknown","error 34: unknown","error 35: unknown",
		"error 36: unknown","error 37: unknown","error 38: unknown",
		"error 39: unknown",
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
		"error 64: unknown","error 65: unknown","error 66: unknown","error 67: unknown",
		"error 68: unknown","error 69: unknown","error 70: unknown","error 71: unknown",
		"error 72: unknown","error 73: unknown","error 74: unknown","error 75: unknown",
		"error 76: unknown","error 77: unknown","error 78: unknown","error 79: unknown",
		"error 80: unknown","error 81: unknown","error 82: unknown","error 83: unknown",
		"error 84: unknown","error 85: unknown","error 86: unknown","error 87: unknown",
		"error 88: unknown","error 89: unknown","error 90: unknown","error 91: unknown",
		"error 92: unknown","error 93: unknown","error 94: unknown","error 95: unknown",
		"error 96: unknown","error 97: unknown","error 98: unknown","error 99: unknown",
		/* 100 */ "unknown"
	};
	
	private State state = State.begin;
	private boolean isOK = false;
	
	Map<String, Object> answers = new HashMap<>();
	List<String> results = new ArrayList<>();
	
	public ATResponse(String data) {
		
		PrimitiveIterator.OfInt it = data.chars().iterator();
		StringBuilder buffer = new StringBuilder();
		
		String command = null;
		String result;
		
		while(it.hasNext()) {
			int c = it.next();
			if (c == 13) continue;
			switch (state) {
				case begin:
					if (c == 10) state = State.newLine; 
					break;
				case newLine: 					
					if (c == 10) continue;
					if (c == ' ') continue;
					buffer.setLength(0);					
					if (Character.isLetter(c)) {
						state = State.result;						
					} else {
						state = State.command;
					}
					buffer.append((char)c);
					break;
				case command:
					if (c == ':') {
						command = buffer.toString();
						buffer.setLength(0);
						Parser p = ParserFactory.createParser(command);
						if (p != null) {
							Object r = p.parse(it);
							addAnswer(command, r);
							command = null;
							state = State.newLine;
						} else {
							state = State.result;
						}
					} else if (c == 10) {
						if (buffer.length() > 0) {
							addResult(buffer.toString());
						}
						state = State.newLine;
					} else {
						buffer.append((char)c);
					}
					break;
				case result:
					if (buffer.length() == 0 && c == ' ') continue;
					if (c == 10) {
						result = buffer.toString();
						if (command == null) {
							addResult(result);
						} else {
							addAnswer(command, result);
							command = null;
						}
						state = State.newLine;
					} else {
						buffer.append((char)c);
					}
					break;
				default: // TODO: throw and log here;
			}
		}
	}
	
	private void addAnswer(String c, Object r) {
		Object o = answers.get(c);
		if (o != null) {
			if (o instanceof List) {
				((List)o).add(r);
			} else {
				List l = new ArrayList();
        l.add(o);
				l.add(r);
				answers.put(c, l);
			}
		} else {
			answers.put(c, r);
		}
	}
	
	private void addResult(String r) {
		results.add(r);
		if ("OK".equals(r)) {
			isOK = true;
		}
	}
	
	public Map<String, Object> getAnswers() {
		return this.answers;
	}
	
	public List<String> getResults() {		
		return this.results;
	}
	
	public boolean isOk() {
		return this.isOK;
	}

	public String getCMEError() {
		String err = (String)answers.get("CME ERROR");
		if (err != null) {
			int code = Integer.parseInt(err);
			if (code < 100) {
				return CME_GENERAL_ERRORS[code];
			} else {
				return "Error: " + err;
			}
		}
		return null;
	}
}
