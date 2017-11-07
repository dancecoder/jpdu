package org.jpdu.pdu;


public class OctetPduElement extends PduElement {

  public OctetPduElement(Type type, byte[] buffer, int offset) {
    super(type, buffer, offset, 0);
  }

  public byte getValue() {
    return buffer[offset];
  }

}
