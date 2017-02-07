package org.jpdu.pdu;

import org.jpdu.Convert;

public class TPDUAddress extends Address {

  private static final int ADDR_TYPE_HDR = 1;

  private TPDUAddress(byte[] octets, int from, int length) {
    super(octets, from, length);
  }

  public static Address Create(byte[] octets, int from) {
    return new TPDUAddress(octets, from, calculateOctetLength(octets[from]));
  }
  
  private static int calculateOctetLength(byte lengthHdr) {
    int nibbleLength = lengthHdr;
    return nibbleLength % 2 == 0 ? nibbleLength / 2 : nibbleLength / 2 + 1;
  }

  public int getLength() {
    int octetLength = calculateOctetLength(this.octets[this.from]);
    return ADDRESS_LENGTH_HDR + ADDR_TYPE_HDR + octetLength;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("{ ");
    sb.append("type: '");
    sb.append(this.getType().toString());
    sb.append("', ");
    sb.append("numberingPlan: '");
    sb.append(this.getNumberingPlan().toString());
    sb.append("', ");
    sb.append("number: '");
    if (getType() == Type.alphanumeric) {
      sb.append(Convert.alfanumericToString(octets, from+2, length));
    } else {
      sb.append(Convert.semioctetToString(octets, from+2, length));
    }
    sb.append("'");
    sb.append(" }");
    return sb.toString();
  }
}
