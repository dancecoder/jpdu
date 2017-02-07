/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jpdu.gateway.modem;

import java.util.PrimitiveIterator;
import org.jpdu.model.MessageListItem;
import org.jpdu.pdu.PDU;

/**
 *
 * @author Odoom
 */
public class MessageListParser implements Parser {

  enum State {
    index,
    stat,
    alpha,
    length,
    pdu
  }

  @Override
  public Object parse(PrimitiveIterator.OfInt iterator) {
    StringBuilder buffer = new StringBuilder();
    int index = -1, stat = -1, alpha = -1, length = -1;
    State state = State.index;
    char c = 0;
    loop: while(iterator.hasNext()) {
      if (state != State.pdu) {
        c = (char)iterator.nextInt();
        if (buffer.length() == 0 && c == ' ') continue;
        if (c == 13) continue;
      }
      switch(state) {
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
            state = State.pdu;
          } else {
            buffer.append(c);
          }
          break;
        case pdu:
          PduParser pdup = new PduParser();
          PDU pdu = (PDU)pdup.parse(iterator);
          buffer.setLength(0);
          return new MessageListItem(index, stat, length, pdu);
          //break loop;
      }
    }
    return null;
  }
}
