package org.jpdu.pdu;

import org.jpdu.Convert;

/**
 * Relay protocol layer Address
 * Based on 3GPP TS 24.011
 * see also 3GPP TS 23.040 section 9.1.2.5
*/
public class RPAddress extends Address {

  private String number;

  private RPAddress(byte[] octets, int from, int length) {
    super(octets, from, length);
  }

  public static Address Create(byte[] octets, int from) {
    int octetLength = octets[from];
    return new RPAddress(octets, from, octetLength);
  }

  @Override
  public String getNumber() {
    if (number == null) {
      number = Convert.semioctetToString(octets, from+2, length);
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
    sb.append("',");
    sb.append("number: '");
    sb.append(this.getNumber());
    sb.append("'");
    sb.append(" }");
    return sb.toString();
  }

}
