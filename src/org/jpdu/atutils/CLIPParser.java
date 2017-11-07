package org.jpdu.atutils;

import java.io.IOException;
import java.io.InputStream;


/*
  This parser return the number only, feel free to extend.

  See 3gpp 27.007 point 7.6
  +CLIP: <number>,<type>[,<subaddr>,<satype>[,[<alpha>][,<CLI validity>]]]
*/
public class CLIPParser implements Parser {

  private enum State {
    begin,
    stringNumber,
    waitEOL,
    finished
  }

  @Override
  public Object parse(InputStream is) throws IOException {
    State state = State.begin;
    StringBuilder buffer = new StringBuilder();
    byte c;
    do {
      c = (byte)is.read();
      if (c == 13) continue;
      switch(state) {
        case begin:
          switch(c) {
            case 10:
            case ' ':
            case '"':
              continue; // ignore leading \n and spaces
            default:
              buffer.append((char)c);
              state = State.stringNumber;
              break;
          }
          break;
        case stringNumber:
          switch(c) {
            case '"':
              state = State.waitEOL;
              break;
            default:
              buffer.append((char)c);
              break;
          }
          break;
        case waitEOL:
          if (c == 10) {
            state = State.finished;
          }
          break;
        default: throw new RuntimeException("Impossible situation");
      }
    } while(c > -1 && state != State.finished);
    return buffer.toString();
  }
  
}
