package org.jpdu.atutils;

import java.io.IOException;
import java.io.InputStream;
import org.jpdu.pdu.PDUImpl;

/*
  Incoming message unsolicited result code parser

  See 3gpp 27.005 point 3.4.1
  +CMT: [<alpha>],<length><CR><LF><pdu>
*/
public class CMTParser implements Parser {

  private enum State {
    begin,
    alpha,
    length,
    pdu,
    finished
  }

  @Override
  public Object parse(InputStream is) throws IOException {
    State state = State.begin;
    StringBuilder buffer = new StringBuilder();
    int alpha = -1; // ??? how to use this?
    int length;
    PDUImpl pdu = null;
    byte c;
    do {
      c = (byte)is.read();
      if (c == 13) continue;
      switch(state) {
        case begin:
          switch(c) {
            case 10:
            case ' ':
              continue; // ignore leading \n and spaces
            case ',': // no alpha
              state = State.length;
              break;
            default:
              buffer.append((char)c);
              state = State.alpha;
              break;
          }
          break;
        case alpha:
          switch(c) {
            case ',':
              String a = buffer.toString();
              if (a.length() > 0) {
                alpha = Integer.parseInt(a);
              }
              buffer.setLength(0);
              state = State.length;
              break;
            default:
              buffer.append((char)c);
              break;
          }
          break;
        case length:
          switch(c) {
            case 10:
              String l = buffer.toString();              
              length = Integer.parseInt(l);
              buffer.setLength(0);
              PduParser pdup = new PduParser(length);
              pdu = (PDUImpl)pdup.parse(is);
              state = State.finished;
              break;
            default:
              buffer.append((char)c);
              break;
          }
          break;        
        default: throw new RuntimeException("Impossible situation");
      }
    } while(c > -1 && state != State.finished);
    return pdu;
  }

}
