package org.jpdu.pdu;

import java.util.Iterator;
import org.jpdu.Convert;


public class SmsSubmitBulder {

  private static int messageReference = 0;

  private String recipient;
  private String text;

  public SmsSubmitBulder() {}

  public SmsSubmitBulder setText(String value) {
    this.text = value;
    return this;
  }

  public SmsSubmitBulder setRecipient(String value) {
    // TODO: check number
    recipient = value;
    return this;
  }

  public Iterable<PDUImpl> build() {
    return new Iterable<PDUImpl>() {

      @Override
      public Iterator<PDUImpl> iterator() {
        return new Iterator<PDUImpl>() {
          boolean sent = false;
          @Override
          public boolean hasNext() {
            return !sent;
          }

          @Override
          public PDUImpl next() {
            PDUImpl pdu = createPDU(text, recipient);
            sent = true;
            return pdu;
          }
        };
      }
      
    };
  }

  private  static synchronized PDUImpl createPDU(String text, String destination) {

    byte[] tpUd = Convert.string2packedGsm7bit(text);
    
    // i = 0
    byte tpMti = (byte)0b0_0_0_00_0_01; // TP-Message-Type-Indicator
    byte tpRd  = (byte)0b0_0_0_00_1_00; // TP-Reject-Duplicaes (3gpp 23.040 point 9.2.3.25)
    byte tpVpf = (byte)0b0_0_0_00_0_00; // TP-Validity-Period-Format (3gpp 23.040 point 9.2.3.3)
    byte tpSrr = (byte)0b0_0_1_00_0_00; // TP-Status-Report-Request (3gpp 23.040 point 9.2.3.5)
    byte tpUdhi= (byte)0b0_0_0_00_0_00; // TP-User-Data-Header-Indicator (3gpp 23.040 point 9.2.3.23)
    byte tpRp  = (byte)0b0_0_0_00_0_00; // TP-Reply-Path (3gpp 23.040 point 9.2.3.17)

    // i = 1
    //messageReference++;
    //if (messageReference > 255) {
    //  messageReference = 0;
    //}
    //byte tpMr = (byte)messageReference; // TP-Message-Reference (3gpp 23.040 point 9.2.3.6)
    byte tpMr = 0; // this should be set by modem

    byte[] tpDa = createAddress(destination); // TP-Destination-Address (3gpp 23.040 point 9.2.3.8)
    
    byte tpPid = (byte)0b00_0_00000; // TP-Protocol-Identifier (3gpp 23.040 point 9.2.3.9)
    
    byte tpDcs = (byte)0b0001_00_10; // TP-Data-Coding-Scheme (3gpp 23.038 point 4)
    for (char c : text.toCharArray()) {
      if (!Character.isBmpCodePoint(c)) {
        tpDcs = (byte)0b0001_10_00;
        break;
      }
    }

    tpDcs = 0; // test;

    byte tpVp = 0; // as tpVpf is 0

    byte tpUdl = (byte)text.length(); // TP-User-Data-Length (3gpp 23.040 point 9.2.3.16)

    // join
    byte[] bytes = new byte[3 + tpDa.length + 3 + tpUd.length];
    bytes[0] = 0; // CMS Center zero address
    bytes[1] = (byte)(tpMti | tpRd | tpVpf | tpSrr | tpUdhi | tpRp );
    bytes[2] = tpMr;
    System.arraycopy(tpDa, 0, bytes, 3, tpDa.length);
    bytes[3 + tpDa.length] = tpPid;
    bytes[3 + tpDa.length + 1] = tpDcs;
    //bytes[3 + tpDa.length + 2] = tpVp;
    bytes[3 + tpDa.length + 2] = tpUdl;
    System.arraycopy(tpUd, 0, bytes, 3 + tpDa.length + 3, tpUd.length);

    return new PDUImpl(bytes);
  }

  // Transport layer address (3gpp 23.040 point 9.1.2.5)
  private static byte[] createAddress(String address) {
    int nibbleLength = (byte)address.length();
    int octetLength = nibbleLength % 2 == 0 ? nibbleLength / 2 : nibbleLength / 2 + 1;
    byte[] addr = new byte[2 + octetLength];
    addr[0] = (byte)nibbleLength;
    addr[1] = (byte)0b1_000_0000;
    for(int i = 2; i < octetLength+2; i++) {
      for (int odd = 0; odd < 2; odd++) {
        char c = ((i-2) * 2 + odd) < nibbleLength ? address.charAt((i-2) * 2 + odd) : 'F';
        switch(c) {
          case '0': break;
          case '1': addr[i] |= 1 << (4 * odd); break;
          case '2': addr[i] |= 2 << (4 * odd); break;
          case '3': addr[i] |= 3 << (4 * odd); break;
          case '4': addr[i] |= 4 << (4 * odd); break;
          case '5': addr[i] |= 5 << (4 * odd); break;
          case '6': addr[i] |= 6 << (4 * odd); break;
          case '7': addr[i] |= 7 << (4 * odd); break;
          case '8': addr[i] |= 8 << (4 * odd); break;
          case '9': addr[i] |= 9 << (4 * odd); break;
          case 'F': addr[i] |= 15 << (4 * odd); break;
        }
      }      
    }
    return addr;
  }

}
