package org.jpdu.atutils;

import java.io.IOException;
import java.io.InputStream;

public class EolParser implements Parser {

  private enum State {
    begin,
    line,
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
              continue; // ignore leading \r and spaces
            default:
              buffer.append((char)c);
              state = State.line;
              break;
          }
          break;
        case line:
          switch(c) {
            case 10:
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
    return buffer.toString();
  }

}
