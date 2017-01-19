package org.dancecoder.smailer.model;


public class UssdResponse {
	
	private UssdResponseType type;	
	private String value;
	
	public UssdResponseType getType() {
		return type;
	}

	public void setType(UssdResponseType type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
}
