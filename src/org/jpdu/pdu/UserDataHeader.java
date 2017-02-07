package org.jpdu.pdu;

import org.jpdu.Convert;

public class UserDataHeader {

  public static final int INFORMATION_ELEMENT_ID_HDR = 1;
  public static final int INFORMATION_ELEMENT_LENGTH_HDR = 1;

  private final byte[] octets;
  private final int from;

  public UserDataHeader(byte[] octets, int from) {
    this.octets = octets;
    this.from = from;
  }

  public int getLength() {
    return octets[from + INFORMATION_ELEMENT_ID_HDR] + 
           INFORMATION_ELEMENT_ID_HDR +
           INFORMATION_ELEMENT_LENGTH_HDR;
  }

  public byte getId() {
    return octets[from];
  }

  private int getValueLength() {
    return octets[from + INFORMATION_ELEMENT_ID_HDR];
  }

  public byte[] getValue() {
    byte[] value = new byte[getValueLength()];
    final int start = from + INFORMATION_ELEMENT_ID_HDR + INFORMATION_ELEMENT_LENGTH_HDR;
    final int max = start + value.length;
    for (int i = start, j = 0; i < max; i++, j++) {
      value[j] = octets[i];
    }
    return value;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("{");
    sb.append(" id:");
    sb.append(" '");
    sb.append(Integer.toHexString(getId()));
    sb.append("', value: '");
    sb.append(Convert.bytesToHexString(getValue()));
    sb.append("' }");
    return sb.toString();
  }

}
