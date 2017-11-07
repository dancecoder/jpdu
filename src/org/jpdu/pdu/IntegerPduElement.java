package org.jpdu.pdu;


public class IntegerPduElement extends PduElement {

  public IntegerPduElement(Type type, byte[] buffer, int offset) {
    super(type, buffer, offset, 0);
  }

  public int getValue() {
    return buffer[offset]; // TODO: check negative
  }

}
