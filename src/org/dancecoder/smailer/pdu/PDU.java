package org.dancecoder.smailer.pdu;

public class PDU {
	
	// SC - Servise Center
	// MS - Mobail Station
	// SME - Short Message Entity
	// See also 3GPP TS 23.040 [2.1.1]
	
	public static final int TYPE_SMS_DELIVER = 0;        // (in the direction SC to MS)	
	public static final int TYPE_SMS_DELIVER_REPORT = 0; // (in the direction MS to SC)
	public static final int TYPE_SMS_STATUS_REPORT = 2;  // (in the direction SC to MS)
	public static final int TYPE_SMS_COMMAND = 2;        // (in the direction MS to SC)
	public static final int TYPE_SMS_SUBMIT = 1;         // (in the direction MS to SC)	
	public static final int TYPE_SMS_SUBMIT_REPORT = 1;  // (in the direction MS to SC)	
	public static final int TYPE_RESERVED = 3;
	
	byte[] bytes;
	Address mscAddress;
	Address origAddress;
	DataCodingScheme codingScheme;
	int mscAddrLength;
	
	public PDU(byte[] pdu) {
		bytes = pdu;
		mscAddress = RPAddress.Create(bytes, 0);							
		mscAddrLength = mscAddress.getLength();
	}	
	
	public Address getMscAddress() {
		return mscAddress;
	}
	
	/**
	 * Parameter indicating whether or not there are more messages to send
	 * TP-MTI (see 3GPP TS 23.040 [9.2.3.1])
	 * @return Message type
	 */
	public int getMessageTypeIndicator() {
		return bytes[mscAddrLength + 1] & 3;
	}
	
	/**
	 * Parameter indicating whether or not there are more messages to send
	 * TP‑MMS (see 3GPP TS 23.040 [9.2.3.2])
	 * @return More messages are waiting for the MS in this SC
	 * @throws org.dancecoder.smailer.pdu.WrongMessageTypeException
	*/
	public boolean getMoreMessagesToSend() throws WrongMessageTypeException {
		if (this.getMessageTypeIndicator() == TYPE_SMS_DELIVER) {
			return (bytes[mscAddrLength + 1] & 4) == 0;
		}
		throw new WrongMessageTypeException();
	}
	
	/**
	 * Parameter indicating that SMS applications should inhibit forwarding or 
	 * automatic message generation that could cause infinite looping.
	 * TP‑LP (see 3GPP TS 23.040 [9.2.3.28])
	 * NOTE: SC may not support this setting
	 * @return The message has either been forwarded or is a spawned message
	 * @throws org.dancecoder.smailer.pdu.WrongMessageTypeException
	 */
	public boolean getLoopPrevention() throws WrongMessageTypeException {
		if (this.getMessageTypeIndicator() == TYPE_SMS_DELIVER) {
			return (bytes[mscAddrLength + 1] & 8) != 0;
		}
		throw new WrongMessageTypeException();
	}
	
	/**
	 * Parameter indicating if the SME has requested a status report.
	 * TP‑SRI (see 3GPP TS 23.040 [9.2.3.4])
	 * @return A status report shall (true) or shell not (false) be returned to the SME
	 * @throws org.dancecoder.smailer.pdu.WrongMessageTypeException
	 */
	public boolean getStatusReportIndication() throws WrongMessageTypeException {
		if (this.getMessageTypeIndicator() == TYPE_SMS_DELIVER) {
			return (bytes[mscAddrLength + 1] & 32) != 0;
		}
		throw new WrongMessageTypeException();
	}
	
	/**
	 * Parameter indicating that the TP‑UD field contains a Header
	 * TP‑UDHI (see 3GPP TS 23.040 [9.2.3.23])
	 * @return The beginning of the TP‑UD field contains a Header in addition 
	 * to the short message (true) or the TP‑UD field contains only the short 
	 * message (false)
	 */
	public boolean getUserDataHeaderExists() {
		return (bytes[mscAddrLength + 1] & 64) != 0;
	}
	
	/**
	 * Parameter indicating that Reply Path exists.
	 * TP‑RP (see 3GPP TS 23.040 [9.2.3.17])
	 * @return TP‑Reply‑Path parameter is set in this SMS‑SUBMIT/DELIVER
	 * @throws org.dancecoder.smailer.pdu.WrongMessageTypeException
	 */
	public boolean getReplyPathExists() throws WrongMessageTypeException {
		int mti = this.getMessageTypeIndicator();
		if (mti == TYPE_SMS_DELIVER || mti == TYPE_SMS_SUBMIT) {
			return (bytes[mscAddrLength + 1] & 128) != 0;
		}
		throw new WrongMessageTypeException();
	}

	/**
	 * Address of the originating SME.
	 * TP‑OA (see 3GPP TS 23.040 [9.2.3.7])
	 * @return address
	 * @throws org.dancecoder.smailer.pdu.WrongMessageTypeException
	 */
	public Address getOriginatingAddress() throws WrongMessageTypeException {
		if (this.getMessageTypeIndicator() == TYPE_SMS_DELIVER) {
			if (origAddress == null) {
				origAddress = TPDUAddress.Create(bytes, mscAddrLength + 2);
			}
			return origAddress;
		}
		throw new WrongMessageTypeException();
	}
	
	/**
	 * Parameter identifying the above layer protocol, if any.
	 * TP‑PID (see3GPP TS 23.040 [9.2.3.9])
	 * TODO: it seems doesn't useful, possible it may be implemented in future
	 * @return field byte
	 * @throws org.dancecoder.smailer.pdu.WrongMessageTypeException
	 */
	public byte getProtocolIdentifier() throws WrongMessageTypeException {
		if (this.getMessageTypeIndicator() == TYPE_SMS_DELIVER) {
			return bytes[mscAddrLength + 2 + this.getOriginatingAddress().getLength()];			
		}
		throw new UnsupportedOperationException();
	}

	/**
	 * Parameter identifying the coding scheme within the TP‑User‑Data.
	 * TP‑DCS (see3GPP TS 23.038 [4])
	 * @return Data Coding Scheme object
	 * @throws org.dancecoder.smailer.pdu.WrongMessageTypeException
	 */
	public DataCodingScheme getDataCodingScheme() throws WrongMessageTypeException {
		if (this.getMessageTypeIndicator() == TYPE_SMS_DELIVER) {
			if (codingScheme == null) {
				byte b = bytes[mscAddrLength + 2 + this.getOriginatingAddress().getLength() + 1];
				codingScheme = new DataCodingScheme(b);
			}
			return codingScheme;
		}
		throw new UnsupportedOperationException();
	}

	/**
	 * Parameter identifying time when the SC received the message.
	 * TP‑SCTS (see3GPP TS 23.040 [9.2.3.11])
	 * @return messace time
	 * @throws org.dancecoder.smailer.pdu.WrongMessageTypeException
	 */
	public TimeStamp getServiceCentreTimeStamp() throws WrongMessageTypeException {
		if (this.getMessageTypeIndicator() == TYPE_SMS_DELIVER) {
			int from = mscAddrLength + 2 + this.getOriginatingAddress().getLength() + 2;
			return new TimeStamp(bytes, from);
		}
		throw new UnsupportedOperationException();
	}

	/**
	 * Parameter indicating the length of the TP‑User‑Data field to follow.
	 * NOTE: may indicate septet or octet count (depends on TP‑DCS)
	 * TP‑UDL (see3GPP TS 23.040 [9.2.3.16])
	 * @return user data length
	 * @throws org.dancecoder.smailer.pdu.WrongMessageTypeException
	 */	
	public int getUserDataLength() throws WrongMessageTypeException {
		if (this.getMessageTypeIndicator() == TYPE_SMS_DELIVER) {
			int from = mscAddrLength + 2 + this.getOriginatingAddress().getLength() + 2 
							+ TimeStamp.TIME_STAMP_LENGTH;
			return bytes[from];
		}
		throw new UnsupportedOperationException();
	}
	
	/**
	 * PDU user data part
	 * TP‑UD (see 3GPP TS 23.040 [9.2.3.24])
	 * @return user data object
	 * @throws org.dancecoder.smailer.pdu.WrongMessageTypeException
	 */
	public UserData getUserData() throws WrongMessageTypeException {
		if (this.getMessageTypeIndicator() == TYPE_SMS_DELIVER) {
			int from = mscAddrLength + 2 + this.getOriginatingAddress().getLength() + 2 
							+ TimeStamp.TIME_STAMP_LENGTH + 1;
			return new UserData(bytes, from);
		}
		throw new UnsupportedOperationException();
	}
	
}
