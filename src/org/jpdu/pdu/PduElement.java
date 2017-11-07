package org.jpdu.pdu;


public abstract class PduElement {

  public enum Type {
    TP_MTI,  // Message Type Indicator
    TP_MMS,  // More Messages to Send
    TP_RD, 	 // Reject Duplicates
    TP_LP,   // Loop Prevention
    TP_VPF,  // Validity Period Format
    TP_SRI,  // Status Report Indication
    TP_SRR,  // Status Report Request
    TP_SRQ,  // Status Report Qualifier
    TP_UDHI, //	User Data Header Indicator
    TP_RP,   // Reply Path
    TP_FCS,  // Failure Cause
    TP_MR,   // Message Reference
    TP_DA,   // Destination Address
    TP_OA,   // Originating Address
    TP_RA,   // Recipient Address
    TP_SCTS, // Service Centre Time Stamp
    TP_DT,   // Discharge Time
    TP_ST,   // Status
    TP_PI,   // Parameter Indicator    
    TP_PID,  // Protocol Identifier
    TP_DCS,  // Data Coding Scheme
    TP_VP,   // Validity Period
    TP_UDL,  // User Data Length
    TP_UD,   // User Data
    TP_CT,   // Command Type
    TP_MN,   // Message Number    
    TP_CDL,  // Command Data Length
    TP_CD    // Command Data
  }

  private final Type type;

  final int offset;
  final int bitOffset;
  final byte[] buffer;

  public PduElement(Type type, byte[] buffer, int offset, int bitOffset) {
    this.buffer = buffer;
    this.offset = offset;
    this.bitOffset = bitOffset;
    this.type = type;
  }

  public PduElement(Type type, byte[] buffer, int offset) {
    this(type, buffer, offset, 0);
  }

  public Type getType() {
    return this.type;
  }


}
