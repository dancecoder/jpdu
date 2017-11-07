package org.jpdu.pdu;


public class SmsSubmitPdu implements Pdu {


  public static final PduElement.Type[] ELEMENTS = new PduElement.Type[] {
    PduElement.Type.TP_MTI,
    PduElement.Type.TP_RD,
    PduElement.Type.TP_VPF,
    PduElement.Type.TP_RP,
    PduElement.Type.TP_UDHI,
    PduElement.Type.TP_SRR,
    PduElement.Type.TP_MR,
    PduElement.Type.TP_MR,
    PduElement.Type.TP_DA,
    PduElement.Type.TP_PID,
    PduElement.Type.TP_DCS,
    PduElement.Type.TP_VP,
    PduElement.Type.TP_UDL,
    PduElement.Type.TP_UD,
  };

  final byte[] octets;

  private final Address mscAddress;
  private final int typeOffset;
  private final PduType type;

  private AddressPduElement destAddress;
  private OctetPduElement protocolIdentifier;
  private OctetPduElement dataCodingScheme;
  private ValidityPeriodPduElement validityPeriod;
  private IntegerPduElement userDataLength;
  
  
  public SmsSubmitPdu(byte[] octets) throws WrongMessageTypeException {
    this.octets = octets;
    mscAddress = RPAddress.Create(octets, 0);
    typeOffset = Address.ADDRESS_LENGTH_HDR + mscAddress.getLength();
    type = new PduType(octets[typeOffset]);
    if (type.getMessageTypeIndicator() != PduType.SMS_DELIVER) {
      throw new WrongMessageTypeException();
    }
  }

  @Override
  public Address getMscAddress() {    
    return mscAddress;
  }

  @Override
  public byte[] getBytes() {
    return this.octets;
  }

  @Override
  public PduElement getELement(PduElement.Type type) throws PduElementTypeNotSupported {
    
    switch(type) {
      
      case TP_MTI: return new MessageTypeIndicator(octets, typeOffset, 0);
      case TP_RD: return new BooleanPduElement(PduElement.Type.TP_RD, octets, typeOffset, 2);
      case TP_VPF: return new ValidityPeriodFormatPduElement(octets, typeOffset, 3);
      case TP_RP: return new BooleanPduElement(PduElement.Type.TP_RP, octets, typeOffset, 5);
      case TP_UDHI: return new BooleanPduElement(PduElement.Type.TP_UDHI, octets, typeOffset, 6);
      case TP_SRR: return new BooleanPduElement(PduElement.Type.TP_UDHI, octets, typeOffset, 7);
      case TP_MR: return new IntegerPduElement(PduElement.Type.TP_MR, octets, typeOffset + TYPE_HDR_LENGTH);
      case TP_DA: return getDestinationAddress();
      case TP_PID: getProtocolIdentifier();
      case TP_DCS: getDataCodingScheme();
      case TP_VP: return getValidityPeriod();
      case TP_UDL: return getUserDataLength();
      
      default: throw new PduElementTypeNotSupported();
    }
  }

  /**
   * Address of the originating SME.
   * TP-DA (see 3GPP TS 23.040 [9.2.3.8])
   * 
   * @return address
   */
  public AddressPduElement getDestinationAddress() {
    if (destAddress == null) {
      int offset = typeOffset + TYPE_HDR_LENGTH + TP_MR_LENGTH;
      destAddress = new AddressPduElement(PduElement.Type.TP_DA, octets, offset);
    }
    return destAddress;
  }

  public OctetPduElement getProtocolIdentifier() {
    if (protocolIdentifier == null) {
      AddressPduElement addr = getDestinationAddress();
      protocolIdentifier = new OctetPduElement(PduElement.Type.TP_PID, octets, typeOffset + TYPE_HDR_LENGTH + TP_MR_LENGTH + addr.getOctetLength());
    }
    return protocolIdentifier;
  }

  public OctetPduElement getDataCodingScheme() {
    if (dataCodingScheme == null) {
      AddressPduElement addr = getDestinationAddress();
      dataCodingScheme = new OctetPduElement(PduElement.Type.TP_DCS, octets, typeOffset + TYPE_HDR_LENGTH + TP_MR_LENGTH + addr.getOctetLength() + TP_PID_LENGTH);
    }
    return dataCodingScheme;
  }

  public ValidityPeriodPduElement getValidityPeriod() {
    if (validityPeriod == null) {
      ValidityPeriodFormatPduElement vpf = new ValidityPeriodFormatPduElement(octets, typeOffset, 3);
      AddressPduElement addr = getDestinationAddress();
      int offset = typeOffset + TYPE_HDR_LENGTH + TP_MR_LENGTH + addr.getOctetLength() + TP_PID_LENGTH + TP_DCS_LENGTH;
      validityPeriod = new ValidityPeriodPduElement(octets, offset, vpf.getValue());
    }
    return validityPeriod;
  }

  public IntegerPduElement getUserDataLength() {
    if (userDataLength == null) {
      ValidityPeriodPduElement vp = getValidityPeriod();
      AddressPduElement addr = getDestinationAddress();
      int offset = typeOffset + TYPE_HDR_LENGTH + TP_MR_LENGTH + addr.getOctetLength() + TP_PID_LENGTH + TP_DCS_LENGTH + vp.getOctetLength();
      userDataLength = new IntegerPduElement(PduElement.Type.TP_UDL, octets, offset);
    }
    return userDataLength;
  }

  
  public SmsSubmitPdu build(String message, String destinationAddress) {
    // TODO: implement constructing of PDU here;
    throw new UnsupportedOperationException("Not implemented yet");
  }

}
