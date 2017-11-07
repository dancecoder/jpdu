package org.jpdu.atutils;

import java.io.IOException;
import java.io.InputStream;
import org.jpdu.pdu.PDUImpl;

/*
  Stored messages parser

  See 3gpp 27.005 point 4.1
  +CMGL: <index>,<stat>,[<alpha>],<length><CR><LF><pdu>
  [<CR><LF>+CMGL:<index>,<stat>,[<alpha>],<length><CR><LF><pdu>
  [...]]
 */
public class CMGLParser implements Parser {

  enum State {
    begin,
    index,
    stat,
    alpha,
    length,    
    finished
  }

  @Override
  public Object parse(InputStream stream) throws IOException {
    StringBuilder buffer = new StringBuilder();
    int index = -1, stat = -1, alpha = -1;
    int length;
    State state = State.begin;
    PDUImpl pdu = null;
    char c;
    do {
      c = (char)stream.read();
      if (c == 13) continue;
      switch(state) {
        case begin:
          if (c != ' ' && c != 10) {
            buffer.append(c);
            state = State.index;
          }
          break;
        case index:
          if (c == ',') {
            index = Integer.parseInt(buffer.toString());
            buffer.setLength(0);
            state = State.stat;
          } else {
            buffer.append(c);
          }
          break;
        case stat:
          if (c == ',') {
            stat = Integer.parseInt(buffer.toString());
            buffer.setLength(0);
            state = State.alpha;
          } else {
            buffer.append(c);
          }
          break;
        case alpha:
          if (c == ',') {
            String a = buffer.toString();
            if (a.length() > 0) {
              alpha = Integer.parseInt(buffer.toString());
            }
            buffer.setLength(0);
            state = State.length;
          } else {
            buffer.append(c);
          }
          break;
        case length:
          if (c == 10) {
            length = Integer.parseInt(buffer.toString());
            buffer.setLength(0);
            PduParser pdup = new PduParser(length);
            pdu = (PDUImpl)pdup.parse(stream);            
            buffer.setLength(0);
            state = State.finished;
          } else {
            buffer.append(c);
          }
          break;        
      }
    } while(c > -1 && state != State.finished);
    return new ATMessageListItem(index, stat, alpha, pdu);
  }
}
