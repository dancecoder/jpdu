package org.dancecoder.smailer.pdu;

public class UserData {

  private final byte[] bytes;
  private final int index;
  private final int userDataLength;
  private final boolean containsHeader;
  private final DataCodingScheme codingScheme;

  public UserData(byte[] bytes, int from, int userDataLength, boolean containsHeader, DataCodingScheme codingScheme) {
    this.bytes = bytes;
    this.index = from;
    this.userDataLength = userDataLength;
    this.containsHeader = containsHeader;
    this.codingScheme = codingScheme;
  }

  @Override
  public String toString() {
    if (containsHeader) {
      return "TODO: parse header";
    } else {
      switch(codingScheme.getUserDataEncoding()) {
        case GSM7bit: 
          int octetLength = userDataLength - userDataLength / 8;
          return Convert.packetGsm7bitToString(bytes, index, octetLength);
        case UCS2: return Convert.ucs2ToString(bytes, index, userDataLength);
        default: return "TODO: data decoding here";
      }
    }
  }

}
