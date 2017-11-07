package org.jpdu.pdu;


public interface Pdu {

  public static final int TYPE_HDR_LENGTH = 1;
  public static final int TP_MR_LENGTH = 1;
  public static final int TP_PID_LENGTH = 1;
  public static final int TP_DCS_LENGTH = 1;


  public static final int PROTOCOL_IDENTIFIER_HDR_LENGTH = 1;
  
  

  byte[] getBytes();

  Address getMscAddress();

  PduElement getELement(PduElement.Type type) throws PduElementTypeNotSupported;


}
