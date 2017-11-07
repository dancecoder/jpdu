package org.jpdu.atutils;

import java.io.InputStream;
import java.io.IOException;

public interface Parser {
  Object parse(InputStream is) throws IOException;
}
