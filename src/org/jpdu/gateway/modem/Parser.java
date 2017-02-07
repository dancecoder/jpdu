package org.jpdu.gateway.modem;

import java.util.PrimitiveIterator;


public interface Parser {
  Object parse(PrimitiveIterator.OfInt iterator);
}
