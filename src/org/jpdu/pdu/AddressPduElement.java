package org.jpdu.pdu;


public class AddressPduElement extends PduElement {

  public AddressPduElement(Type type, byte[] buffer, int offset) {
    super(type, buffer, offset, 0);
  }

  public Address getValue() {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public int getOctetLength() {
    throw new UnsupportedOperationException("Not implemented yet");
  }

}
