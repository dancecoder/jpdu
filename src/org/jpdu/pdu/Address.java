package org.jpdu.pdu;

public abstract class Address {

  public static final int ADDRESS_LENGTH_HDR = 1;

  public enum Type {
    unknown(0),
    international(1),
    national(2),
    networkSpecific(3),
    subscriber(4),
    alphanumeric(5), // coded according to 3GPP TS 23.038 GSM 7-bit default alphabet
    abbreviated(6),
    reservedForExtension(7);

    private final int bits;

    private Type(int bits) {
      this.bits = bits;
    }

    public static Type valueOf(int bits) {
      Type[] tt = Type.values();
      for (int i = 0; i < tt.length; i++) {
        if (tt[i].bits == bits) {
          return tt[i];
        }
      }
      return null;
    }
  }

  public enum NumberingPlan {
    unknown(0),
    telephoneOrIsdn(1),
    data(3), // X.121
    telex(4),
    serviceCentreSpecific(5), // (also use mask number 6) used to indicate a numbering plan
                              // specific to External Short Message Entities attached to the SMSC
    national(8),
    privatePlan(9),
    ermes(10),
    reservedForExtension(11);

    private final int mask;

    private NumberingPlan(int m) {
      this.mask = m;
    }

    public static NumberingPlan valueOf(int mask) {
      NumberingPlan[] tt = NumberingPlan.values();
      for (int i = 0; i < tt.length; i++) {
        if (tt[i].mask == mask) {
          return tt[i];
        }
      }
      if (mask == 6) {
        return NumberingPlan.serviceCentreSpecific;
      }
      return null;
    }
  }

  Type type;
  NumberingPlan plan;

  byte[] octets;
  int from = 0;
  int length = 0;

  Address(byte[] octets, int from, int length) {
    this.octets = octets;
    this.from = from;
    this.length = length;
  }

  public Type getType() {
    if (this.type == null) {
      this.type = Type.valueOf(this.octets[from+ADDRESS_LENGTH_HDR] >> 4 & 7);
    }
    return this.type;
  }

  public NumberingPlan getNumberingPlan() {
    if (this.plan == null) {
      this.plan = NumberingPlan.valueOf(this.octets[from+ADDRESS_LENGTH_HDR] & 15);
    }
    return this.plan;
  }

  public int getLength() {
    return this.length;
  }

  public abstract String getNumber();

}
