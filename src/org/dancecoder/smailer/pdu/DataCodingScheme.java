package org.dancecoder.smailer.pdu;

/**
 * Indicates the data coding scheme of the TP‑UD field
 * INPORTANT: and may indicate a message class 
 * see 3GPP TS 23.038 [4]
 */
public class DataCodingScheme {
	
	byte field;
	
	public DataCodingScheme(byte pduField) {
		field = pduField;
	}
}
