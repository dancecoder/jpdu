package org.dancecoder.smailer.gateway.modem;

import java.util.PrimitiveIterator;


public interface Parser {
  Object parse(PrimitiveIterator.OfInt iterator);
}
