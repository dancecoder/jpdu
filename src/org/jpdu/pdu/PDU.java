package org.jpdu.pdu;

public class PDU {

  // SC - Servise Center
  // MS - Mobail Station
  // SME - Short Message Entity
  // See also 3GPP TS 23.040 [2.1.1]

  public static final int PDU_TYPE_HDR = 1;
  public static final int PROTOCOL_IDENTIFIER_HDR = 1;
  public static final int DATA_CODING_SCHEME_HDR = 1;
  public static final int USER_DATA_LENGTH_HDR = 1;

  byte[] bytes;
  Address mscAddress;
  PduType type;
  Address origAddress;
  DataCodingScheme codingScheme;
  int mscAddrLength;

  public PDU(byte[] pdu) {
    bytes = pdu;
    mscAddress = RPAddress.Create(bytes, 0);
    mscAddrLength = Address.ADDRESS_LENGTH_HDR + mscAddress.getLength();
  }

  public Address getMscAddress() {
    return mscAddress;
  }

  public PduType getType() {
    if (type == null) {
      type = new PduType(bytes[mscAddrLength]);
    }
    return type;
  }

  /**
   * Address of the originating SME.
   * TP‑OA (see 3GPP TS 23.040 [9.2.3.7])
   * @return address
   * @throws org.jpdu.pdu.WrongMessageTypeException
   */
  public Address getOriginatingAddress() throws WrongMessageTypeException {
    if (this.getType().getMessageTypeIndicator() == PduType.SMS_DELIVER) {
      if (origAddress == null) {
        origAddress = TPduAddress.Create(bytes, mscAddrLength + PDU_TYPE_HDR);
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
   * @throws org.jpdu.pdu.WrongMessageTypeException
   */
  public int getProtocolIdentifier() throws WrongMessageTypeException {
    if (this.getType().getMessageTypeIndicator() == PduType.SMS_DELIVER) {
      int from = mscAddrLength + 
                 PDU_TYPE_HDR +                 
                 this.getOriginatingAddress().getLength();
      return bytes[from] & 0xFF;
    }
    throw new UnsupportedOperationException();
  }

  /**
   * Parameter identifying the coding scheme within the TP‑User‑Data.
   * TP‑DCS (see3GPP TS 23.038 [4])
   * @return Data Coding Scheme object
   * @throws org.jpdu.pdu.WrongMessageTypeException
   */
  public DataCodingScheme getDataCodingScheme() throws WrongMessageTypeException {
    if (this.getType().getMessageTypeIndicator() == PduType.SMS_DELIVER) {
      if (codingScheme == null) {
        int from = mscAddrLength +
                   PDU_TYPE_HDR +                   
                   this.getOriginatingAddress().getLength() +
                   PROTOCOL_IDENTIFIER_HDR;
        byte b = bytes[from];
        codingScheme = new DataCodingScheme(b);
      }
      return codingScheme;
    }
    throw new UnsupportedOperationException();
  }

  /**
   * Parameter identifying time when the SC received the message.
   * TP‑SCTS (see3GPP TS 23.040 [9.2.3.11])
   * @return message time
   * @throws org.jpdu.pdu.WrongMessageTypeException
   */
  public TimeStamp getServiceCentreTimeStamp() throws WrongMessageTypeException {
    if (this.getType().getMessageTypeIndicator() == PduType.SMS_DELIVER) {
      int from = mscAddrLength +
                 PDU_TYPE_HDR +                 
                 this.getOriginatingAddress().getLength() +
                 PROTOCOL_IDENTIFIER_HDR +
                 DATA_CODING_SCHEME_HDR;
      return new TimeStamp(bytes, from);
    }
    throw new WrongMessageTypeException();
  }

  /**
   * Parameter indicating the length of the TP‑User‑Data field to follow.
   * NOTE: may indicate septet or octet count (depends on TP‑DCS)
   * TP‑UDL (see3GPP TS 23.040 [9.2.3.16])
   * @return user data length
   * @throws org.jpdu.pdu.WrongMessageTypeException
   */
  public int getUserDataLength() throws WrongMessageTypeException {
    if (this.getType().getMessageTypeIndicator() == PduType.SMS_DELIVER) {
      int from = mscAddrLength +
                 PDU_TYPE_HDR +                 
                 this.getOriginatingAddress().getLength() +
                 PROTOCOL_IDENTIFIER_HDR +
                 DATA_CODING_SCHEME_HDR +
                 TimeStamp.TIME_STAMP_LENGTH;
      return bytes[from] & 0xFF;
    }
    throw new UnsupportedOperationException();
  }

  /**
   * PDU user data part
   * TP‑UD (see 3GPP TS 23.040 [9.2.3.24])
   * @return user data object
   * @throws org.jpdu.pdu.WrongMessageTypeException
   */
  public UserData getUserData() throws WrongMessageTypeException {
    if (this.getType().getMessageTypeIndicator() == PduType.SMS_DELIVER) {
      int from = mscAddrLength +
                 PDU_TYPE_HDR +                 
                 this.getOriginatingAddress().getLength() +
                 PROTOCOL_IDENTIFIER_HDR +
                 DATA_CODING_SCHEME_HDR +
                 TimeStamp.TIME_STAMP_LENGTH +
                 USER_DATA_LENGTH_HDR;
      return new UserData(
        bytes,
        from,
        getUserDataLength(),
        this.getType().getUserDataHeaderExists(),
        getDataCodingScheme()
      );
    }
    throw new WrongMessageTypeException();
  }

}
