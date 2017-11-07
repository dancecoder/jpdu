package org.jpdu.atutils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/*
  Complete capabilities list (+GCAP)

  See ITU-T V.250 point 6.1.9
  +GCAP: +CGSM,+DS,+ES
*/
public class GCAPParser implements Parser  {

  private enum State {
    begin,
    item,
    finished
  }

  @Override
  public Object parse(InputStream is) throws IOException {
    State state = State.begin;
    StringBuilder buffer = new StringBuilder();
    Set<String> set = new HashSet<>();
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
              state = State.item;
              break;
          }
          break;
        case item:
          switch(c) {
            case ',':
              set.add(buffer.toString());
              buffer.setLength(0);
              break;
            case 10:
              set.add(buffer.toString());
              buffer.setLength(0);
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
    return set;
  }

}
