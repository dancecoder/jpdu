package org.jpdu.atutils;

import java.io.IOException;
import java.io.InputStream;


public class ATResultCode {

  private enum State {
    begin,
    result,
    finished
  }

  private final StringBuilder buffer = new StringBuilder();

  private State state = State.begin;
  private String result;  
  private Object value;

  public ATResultCode(InputStream is) throws IOException {
    byte c;
    do {
      c = (byte)is.read();
      if (c == 13) continue;
      switch (state) {
        case begin:
          switch(c) {
            case 10:
            case ' ':
              continue;
            default:
              state = State.result;
              buffer.append((char)c);
              break;
          }
          break;
        case result:
          switch (c) {
            case ':':
              result = buffer.toString();
              buffer.setLength(0);
              Parser p = ParserFactory.createParser(result);              
              value = p.parse(is);
              state = State.finished;
              break;
            case 10:
              result = buffer.toString();              
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
  }

  public String getResultCode() {
    return this.result;
  }

  public Object getResultValue() {
    return this.value;
  }

  public boolean isTerminating() {
    return "OK".equals(result) || "+CME ERROR".equals(result) || "+CMS ERROR".equals(result);
  }

}
