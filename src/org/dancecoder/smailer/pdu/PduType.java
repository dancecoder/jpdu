package org.dancecoder.smailer.pdu;

public class PduType {

  public static final int SMS_DELIVER = 0;        // (in the direction SC to MS)
  public static final int SMS_DELIVER_REPORT = 0; // (in the direction MS to SC)
  public static final int SMS_STATUS_REPORT = 2;  // (in the direction SC to MS)
  public static final int SMS_COMMAND = 2;        // (in the direction MS to SC)
  public static final int SMS_SUBMIT = 1;         // (in the direction MS to SC)
  public static final int SMS_SUBMIT_REPORT = 1;  // (in the direction MS to SC)
  public static final int RESERVED = 3;

  private final byte field;

  public PduType(byte octet) {
    this.field = octet;
  }

  /**
   * Parameter indicating whether or not there are more messages to send
   * TP-MTI (see 3GPP TS 23.040 [9.2.3.1])
   * @return Message type
   */
  public int getMessageTypeIndicator() {
    return field & 3;
  }

  /**
   * Parameter indicating whether or not there are more messages to send
   * TP‑MMS (see 3GPP TS 23.040 [9.2.3.2])
   * @return More messages are waiting for the MS in this SC
   * @throws org.dancecoder.smailer.pdu.WrongMessageTypeException
  */
  public boolean getMoreMessagesToSend() throws WrongMessageTypeException {
    if (this.getMessageTypeIndicator() == SMS_DELIVER) {
      return (field & 4) == 0;
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
    if (this.getMessageTypeIndicator() == SMS_DELIVER) {
      return (field & 8) != 0;
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
    if (this.getMessageTypeIndicator() == SMS_DELIVER) {
      return (field & 32) != 0;
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
    return (field & 64) != 0;
  }

  /**
   * Parameter indicating that Reply Path exists.
   * TP‑RP (see 3GPP TS 23.040 [9.2.3.17])
   * @return TP‑Reply‑Path parameter is set in this SMS‑SUBMIT/DELIVER
   * @throws org.dancecoder.smailer.pdu.WrongMessageTypeException
   */
  public boolean getReplyPathExists() throws WrongMessageTypeException {
    int mti = this.getMessageTypeIndicator();
    if (mti == SMS_DELIVER || mti == SMS_SUBMIT) {
      return (field & 128) != 0;
    }
    throw new WrongMessageTypeException();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("{ bits: '");
    for (int i = 128; i > 0; i = i >> 1) {
       sb.append( (field & i) > 0 ? "1" : "0");
       if (i == 16) {
        sb.append(" ");
      }
    }
    sb.append("', MessageTypeIndicator: '");
    sb.append(this.getMessageTypeIndicator());
    if (this.getMessageTypeIndicator() == SMS_DELIVER) {
      try {
        sb.append("', MoreMessagesToSend: ");
        sb.append(this.getMoreMessagesToSend());
        sb.append(", LoopPrevention: '");
        sb.append(this.getLoopPrevention());
        sb.append("', StatusReportIndication: '");
        sb.append(this.getStatusReportIndication());
        sb.append("', UserDataHeaderExists: '");
        sb.append(this.getUserDataHeaderExists());
        sb.append("', ReplyPathExists: '");
        sb.append(this.getReplyPathExists());
      } catch (WrongMessageTypeException e) {
        // TODO: logging here
      }
    }    
    sb.append("' }");
    return sb.toString();
  }

}
