package org.jpdu.pdu;

import org.jpdu.Convert;

/**
 * Semi-octet representation of SMS time field
 * see3GPP TS 23.040 [9.2.3.11]
 */
public class TimeStamp {

  public static final int TIME_STAMP_LENGTH = 7;
  
  private static final int TIME_STAMP_YEAR_POS = 0;
  private static final int TIME_STAMP_MONTH_POS = 1;
  private static final int TIME_STAMP_DAY_POS = 2;
  private static final int TIME_STAMP_HOUR_POS = 3;
  private static final int TIME_STAMP_MINUTE_POS = 4;
  private static final int TIME_STAMP_SECOND_POS = 5;
  private static final int TIME_STAMP_TIMEZONE_POS = 6;

  private final byte[] bytes;
  private final int index;

  public TimeStamp(byte[] bytes, int from) {
    this.bytes = bytes;
    this.index = from;
  }

  public int getYear() {
    return Convert.semioctetsToInteger(bytes[TIME_STAMP_YEAR_POS + index]);
  }

  public int getMonth() {
    return Convert.semioctetsToInteger(bytes[TIME_STAMP_MONTH_POS + index]);
  }

  public int getDay() {
    return Convert.semioctetsToInteger(bytes[TIME_STAMP_DAY_POS + index]);
  }

  public int getHour() {
    return Convert.semioctetsToInteger(bytes[TIME_STAMP_HOUR_POS + index]);
  }

  public int getMinute() {
    return Convert.semioctetsToInteger(bytes[TIME_STAMP_MINUTE_POS + index]);
  }

  public int getSecond() {
    return Convert.semioctetsToInteger(bytes[TIME_STAMP_SECOND_POS + index]);
  }

  public int getTimeZone() {
    byte field = bytes[TIME_STAMP_TIMEZONE_POS + index];
    int sign = (field & 8) == 8 ? -1 : 1;
    return sign * Convert.semioctetsToInteger((byte)(field & 0xF7)) / 4;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("{ year: ");
    sb.append(this.getYear());
    sb.append(", month: ");
    sb.append(this.getMonth());
    sb.append(", day: ");
    sb.append(this.getDay());
    sb.append(", hour: ");
    sb.append(this.getHour());
    sb.append(", minute: ");
    sb.append(this.getMinute());
    sb.append(", second: ");
    sb.append(this.getSecond());
    sb.append(", timeZone: ");
    sb.append(this.getTimeZone());    
    sb.append(" }");
    return sb.toString();
  }
}
