package org.jpdu.charset;

import java.nio.charset.Charset;
import java.util.Iterator;

public class CharsetProvider extends java.nio.charset.spi.CharsetProvider {

  @Override
  public Iterator<Charset> charsets() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public Charset charsetForName(String charsetName) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
  
}
