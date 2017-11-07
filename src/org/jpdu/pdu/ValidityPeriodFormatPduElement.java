package org.jpdu.pdu;


public class ValidityPeriodFormatPduElement extends PduElement {

  public enum Value {
    notPresent,
    relative,
    enhanced,
    absolute
  }

  public ValidityPeriodFormatPduElement(byte[] buffer, int offset, int bitOffset) {
    super(Type.TP_VPF, buffer, offset, bitOffset);
  }

  public Value getValue() {
    int shifted = (buffer[offset] >> bitOffset) & 0b00000011;
    switch(shifted) {
      case 0: return Value.notPresent;
      case 1: return Value.enhanced;
      case 2: return Value.relative;
      case 3: return Value.absolute;
      default: throw new RuntimeException("Impossible situation");
    }
  }

}
