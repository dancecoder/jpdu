package org.jpdu.pdu;



public class BooleanPduElement extends PduElement {

  public BooleanPduElement(Type type, byte[] buffer, int offset, int bitOffset) {
    super(type, buffer, offset, bitOffset);
  }

  public boolean getValue() {
    return ((buffer[offset] >> bitOffset) & 0b00000001) == 1;
  }

}
