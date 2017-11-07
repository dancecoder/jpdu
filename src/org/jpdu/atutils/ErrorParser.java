package org.jpdu.atutils;

import java.io.IOException;
import java.io.InputStream;

public class ErrorParser implements Parser {

  private enum State {
    begin,
    value,
    finished
  }

  private final String code;

  public ErrorParser(String code) {
    this.code = code;
  }

  @Override
  public Object parse(InputStream stream) throws IOException {
    StringBuilder buffer = new StringBuilder();
    char c;
    State state = State.begin;
    do {
      c = (char)stream.read();
      switch(state) {
        case begin:
          if (c != ' ' && c != 10) {
            state = State.value;
            buffer.append((char)c);
          }
          break;
        case value:
          if (c == 10) {
            state = State.finished;
          } else {
            buffer.append((char)c);
          }
          break;
      }
    } while(c > -1 && state != State.finished);
    return new ATError(code, buffer.toString());
  }

}
