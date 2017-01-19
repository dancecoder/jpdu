package org.dancecoder.smailer.pdu;

public class UserData {
	
	private byte[] bytes;
	private int index;
		
	public UserData(byte[] bytes, int from) {
		this.bytes = bytes;
		this.index = from;
	}
}
