package org.jpdu.pdu;

public class MessageTypeIndicator extends PduElement {

  public static final int SMS_DELIVER = 0;        // (in the direction SC to MS)
  public static final int SMS_DELIVER_REPORT = 0; // (in the direction MS to SC)
  public static final int SMS_STATUS_REPORT = 2;  // (in the direction SC to MS)
  public static final int SMS_COMMAND = 2;        // (in the direction MS to SC)
  public static final int SMS_SUBMIT = 1;         // (in the direction MS to SC)
  public static final int SMS_SUBMIT_REPORT = 1;  // (in the direction MS to SC)
  public static final int RESERVED = 3;

  public MessageTypeIndicator(byte[] buffer, int offset, int bitOffset) {
    super(Type.TP_MTI, buffer, offset, bitOffset);
  }

  public int getValue() {
    return (buffer[offset] >> bitOffset) & 0b00000011;
  }

}
