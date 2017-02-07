package org.jpdu.pdu;

import org.jpdu.Convert;

public class UserData {

  private static final int USER_DATA_HEADER_LENGTH_HDR = 1;

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

  public int getUserDataHeaderLength() {
    if (containsHeader) {
      return USER_DATA_HEADER_LENGTH_HDR + (bytes[index] & 0xFF);
    } else {
      return 0;
    }
  }
  
  public int getUserDataHeadersCount() {
    if (containsHeader) {
      int headersLength = 0;
      int count = 0;
      int pos = 0;
      while (headersLength < bytes[index]) {
        pos = index +
              USER_DATA_HEADER_LENGTH_HDR +
              headersLength +
              UserDataHeader.INFORMATION_ELEMENT_ID_HDR +
              count * USER_DATA_HEADER_LENGTH_HDR;
        headersLength += bytes[pos] + 
                         UserDataHeader.INFORMATION_ELEMENT_ID_HDR +
                         UserDataHeader.INFORMATION_ELEMENT_LENGTH_HDR;
        count++;
      }
      return count;
    } else {
      return 0;
    }
  }

  public UserDataHeader[] getUserDataHeaders() {
    int count = getUserDataHeadersCount();
    if (count == 0) {
      return null;
    } else {
      UserDataHeader[] headers = new UserDataHeader[count];
      int position = index + USER_DATA_HEADER_LENGTH_HDR;
      for (int i = 0; i < count; i++) {        
        UserDataHeader header = new UserDataHeader(bytes, position);
        headers[i] = header;
        position += header.getLength();
      }
      return headers;
    }    
  }

  public String getUserDataText() {
    int dataIndex = index + getUserDataHeaderLength();
    int octetLength = userDataLength - getUserDataHeaderLength();
    switch(codingScheme.getUserDataEncoding()) {
      case GSM7bit:
        // TODO: uncompressed 7 bit padding calculation here
        octetLength -= octetLength / 8;
        return Convert.packetGsm7bitToString(bytes, dataIndex, octetLength);
      case UCS2:
        return Convert.ucs2ToString(bytes, dataIndex, octetLength);
      case data8bit:
        return Convert.bytesToHexString(bytes, dataIndex, octetLength);
      default:
        // TODO: logging unknown data encoding here
        return null;
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("{");
    sb.append(" headers:");
    
    UserDataHeader[] headers = getUserDataHeaders();
    if (headers == null) {
      sb.append(" null,");
    } else {
      sb.append(" [");
      for (int i = 0, max = headers.length; i < max; i++) {
        sb.append(headers[i]);
        if (i < max -1) {
          sb.append(", ");
        }
      }
      sb.append("],");
    }  
    sb.append(" text: '");
    sb.append(getUserDataText());
    sb.append("'");
    sb.append(" }");
    return sb.toString();
  }

}
