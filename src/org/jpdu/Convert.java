package org.jpdu;

import java.nio.charset.Charset;

public class Convert {

  public static String bytesToHexString(byte[] bytes) {
    return bytesToHexString(bytes, 0, bytes.length);
  }

  public static String bytesToHexString(byte[] bytes, int first, int length) {
    char[] output = new char[length * 2];
    int b;
    int ii = 0;
    for (int i = first, max = first + length; i < max; i++) {
      b = bytes[i] & 0xFF;      
      output[ii] = TextConverter.HEX_CHAR_TABLE[b >> 4 & 15];
      ii++;
      output[ii] = TextConverter.HEX_CHAR_TABLE[b & 15];
      ii++;
    }
    return new String(output);
  }
          
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

  public static byte[] string2packedGsm7bit(String text) {
    char[] chars = TextConverter.stringToBytes(text, TextConverter.GSM_7BIT_DEFAULT);
    chars = TextConverter.pack(chars);
    byte[] octets = new byte[chars.length];
    for (int i = 0; i < chars.length; i++) {
      octets[i] = (byte)chars[i];
    }
    return octets;
  }
  
  // todo: create a special Charset
  //public static int toDefaultAlphabet(String s, byte[] buffer, int offset) {
  //  Charset.forName(s)
  //}

}
