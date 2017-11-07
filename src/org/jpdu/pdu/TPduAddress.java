package org.jpdu.pdu;

import org.jpdu.Convert;

public class TPduAddress extends Address {

  private static final int ADDR_TYPE_HDR = 1;
  private String number;

  private TPduAddress(byte[] bytes, int from, int length) {
    super(bytes, from, length);
  }

  public static Address Create(byte[] octets, int from) {
    return new TPduAddress(octets, from, calculateOctetLength(octets[from]));
  }
  
  private static int calculateOctetLength(byte lengthHdr) {
    int nibbleLength = lengthHdr;
    return nibbleLength % 2 == 0 ? nibbleLength / 2 : nibbleLength / 2 + 1;
  }

  @Override
  public int getLength() {
    int octetLength = calculateOctetLength(this.octets[this.from]);
    return ADDRESS_LENGTH_HDR + ADDR_TYPE_HDR + octetLength;
  }

  public String getNumber() {
    if (number == null) {
      if (getType() == Type.alphanumeric) {
        number = Convert.alfanumericToString(octets, from+2, length);
      } else {
        number = Convert.semioctetToString(octets, from+2, length);
      }
    }
    return number;
  }

  @Override
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
    sb.append(this.getNumber());
    sb.append("'");
    sb.append(" }");
    return sb.toString();
  }
}
