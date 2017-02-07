package org.dancecoder.smailer.pdu;

import org.dancecoder.smailer.TextConverter;

public class Convert {

  public static String semioctetToString(byte[] bytes, int first, int length) {
    StringBuilder sb = new StringBuilder(length * 2);
    int b = 0;
    for (int i = first; i < first+length ; i++) {
      sb.append(bytes[i] & 15);
      b = bytes[i] >> 4 & 15;
      if (b == 15) {
        break;
      } else {
        sb.append(b);
      }
    }
    return sb.toString();
  }

  public static int semioctetsToInteger(byte octet) {
    return (octet & 15) * 10 + (octet >> 4 & 15);
  }

  public static String alfanumericToString(byte[] bytes, int first, int length) {
    return packetGsm7bitToString(bytes, first, length);
  }

  public static String packetGsm7bitToString(byte[] bytes, int first, int length) {
    int[] chars = TextConverter.unpack(bytes, first, length);
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < chars.length ; i++) {
      sb.append(TextConverter.GSM_7BIT_DEFAULT[chars[i]]);
    }
    return sb.toString();
  }

  public static String ucs2ToString(byte[] bytes, int first, int length) {
    return TextConverter.bytesToUnicode(bytes, first, length);
  }

}
