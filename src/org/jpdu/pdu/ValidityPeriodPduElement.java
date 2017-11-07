package org.jpdu.pdu;


public class ValidityPeriodPduElement extends PduElement {

  private final ValidityPeriodFormatPduElement.Value format;

  public ValidityPeriodPduElement(byte[] buffer, int offset, ValidityPeriodFormatPduElement.Value vpf) {
    super(Type.TP_VP, buffer, offset, 0);
    this.format = vpf;
  }

  public int getOctetLength() {
    switch(format) {
      case notPresent: return 0;
      case relative: return 1;
      default: return 7;
    }
  }

}
