package org.jpdu.atutils;

import java.io.IOException;
import java.io.InputStream;
import org.jpdu.TextConverter;
import org.jpdu.pdu.PDUImpl;

public class PduParser implements Parser {

  private enum State {
    begin,    
    pdu,
    finished
  }

  private final int length;

  public PduParser(int length) {
    this.length = length;
  }

  @Override
  public Object parse(InputStream is) throws IOException {
    // TODO: use length instead of \n to find end of data
    StringBuilder buffer = new StringBuilder();
    State state = State.begin;
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
            default:
              buffer.append((char)c);
              state = State.pdu;
              break;
          }
          break;
        case pdu:
          switch(c) {
            case 10:
              state = State.finished;
              break;
            default:
              buffer.append((char)c);
              break;
          }
          break;
      }
    } while(c > -1 && state != State.finished);
    String pdu = buffer.toString();
    byte[] bytes = TextConverter.unhex(pdu);
    return new PDUImpl(bytes);
  }

}
