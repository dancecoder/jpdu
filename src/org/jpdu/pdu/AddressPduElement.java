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
  
  static int getOctetLength(String address) {    
    return getOctetLength(address.length());
  }
  
  static int getOctetLength(int nibbleLength) {    
    return nibbleLength % 2 == 0 ? nibbleLength / 2 : nibbleLength / 2 + 1;
  }

  // Transfer layer address (3gpp 23.040 point 9.1.2.5)
  public static int build(byte[] buffer, int offset, String address) {
    int nibbleLength = (byte)address.length();
    int octetLength = getOctetLength(nibbleLength);
    buffer[offset++] = (byte)nibbleLength;
    buffer[offset++] = (byte)0b1_000_0000;
    for(int i = offset; i < octetLength+offset; i++) {
      for (int odd = 0; odd < 2; odd++) {
        char c = ((i-2) * 2 + odd) < nibbleLength ? address.charAt((i-2) * 2 + odd) : 'F';
        switch(c) {
          case '0': break;
          case '1': buffer[i] |= 1 << (4 * odd); break;
          case '2': buffer[i] |= 2 << (4 * odd); break;
          case '3': buffer[i] |= 3 << (4 * odd); break;
          case '4': buffer[i] |= 4 << (4 * odd); break;
          case '5': buffer[i] |= 5 << (4 * odd); break;
          case '6': buffer[i] |= 6 << (4 * odd); break;
          case '7': buffer[i] |= 7 << (4 * odd); break;
          case '8': buffer[i] |= 8 << (4 * odd); break;
          case '9': buffer[i] |= 9 << (4 * odd); break;
          case 'F': buffer[i] |= 15 << (4 * odd); break;
        }
      }      
    }
    return octetLength + 2;
  }
  
}
