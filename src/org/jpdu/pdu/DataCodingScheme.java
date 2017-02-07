package org.jpdu.pdu;

/**
 * Indicates the data coding scheme of the TP‑UD field
 * INPORTANT: and may indicate a message class 
 * see 3GPP TS 23.038 [4]
 */
public class DataCodingScheme {

  byte field;

  public DataCodingScheme(byte pduField) {
    field = pduField;
  }

  public MessageGroup getMessageGroup() {
    if (field == 0) {
      return MessageGroup.general;
    }
    int b = (field & 0xff) >>> 6;
    if (b == 0 | b == 1) {
      return MessageGroup.general;
    } else if (b == 2) {
      return MessageGroup.unknown;
    } else if (b == 3) {
      b = (field & 0xff) >> 4 & 3;
      if (b == 3) {
        return MessageGroup.data;
      } else {
        return MessageGroup.indication;
      }      
    }
    // TODO: warning log here
    return MessageGroup.unknown;
  }

  public boolean doSaveMessage() {
    if (field == 0) {
      return true;
    }
    MessageGroup group = getMessageGroup();
    switch(group) {
      case general: return (field & 192) != 64;        
      case indication: return (field & 48) != 0;        
      case data: return true;
      default:
        // TODO: warning log here
        return true;        
    }
  }

  public UserDataEncoding getUserDataEncoding() {
    if (field == 0) {
      return UserDataEncoding.GSM7bit;
    }
    MessageGroup group = getMessageGroup();    
    switch(group) {
      case general:
        int b = (field & 12) >> 2;
        switch (b) {
          case 0: return UserDataEncoding.GSM7bit;
          case 1: return UserDataEncoding.data8bit;
          case 2: return UserDataEncoding.UCS2;
          default: return UserDataEncoding.unknown;
        }        
      case indication: return (field & 32) == 32 ? UserDataEncoding.UCS2 : UserDataEncoding.GSM7bit;
      case data: return (field & 4) == 4 ? UserDataEncoding.data8bit : UserDataEncoding.GSM7bit;
      default: return UserDataEncoding.unknown;        
    }
  }

  public MessageClass getMessageClass() {
    if (field == 0) {
      return MessageClass.noclass;
    }
    MessageGroup group = getMessageGroup();
    switch(group) {
      case data:
      case general:
        int b = field & 3;
        switch(b) {
          case 0: return MessageClass.class0;
          case 1: return MessageClass.class1;
          case 2: return MessageClass.class2;
          case 3: return MessageClass.class3;
          default: return MessageClass.noclass;
        }
      default: return MessageClass.noclass;
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("{ bits: '");
    for (int i = 128; i > 0; i = i >> 1) {
       sb.append( (field & i) > 0 ? "1" : "0");
       if (i == 16) {
        sb.append(" ");
      }
    }
    sb.append("', messageGroup: '");
    sb.append(this.getMessageGroup());
    sb.append("', doSaveMessage: ");
    sb.append(this.doSaveMessage());
    sb.append(", userDataEncoding: '");
    sb.append(this.getUserDataEncoding());
    sb.append("', messageClass: '");
    sb.append(this.getMessageClass());
    sb.append("' }");
    return sb.toString();
  }
}
