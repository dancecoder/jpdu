package org.dancecoder.smailer.pdu;

/**
 * Semi-octet representation of SMS time field
 * see3GPP TS 23.040 [9.2.3.11]
 */
public class TimeStamp {
	
	public static final int TIME_STAMP_LENGTH = 7;
	
	private byte[] bytes;
	private int index;
		
	public TimeStamp(byte[] bytes, int from) {
		this.bytes = bytes;
		this.index = from;
	}
}
