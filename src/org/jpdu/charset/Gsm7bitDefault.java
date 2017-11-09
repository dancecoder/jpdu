package org.jpdu.charset;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;


public class Gsm7bitDefault extends Charset {
  

  
  public Gsm7bitDefault(String canonicalName, String[] aliases) {
    super(canonicalName, aliases);
  }

  @Override
  public boolean contains(Charset cs) {
    return (cs instanceof Gsm7bitDefault);    
  }

  @Override
  public CharsetDecoder newDecoder() {
    return new Gsm7bitDefaultDecoder(this, 1 + 1/7, 1 + 1/8);    
  }

  @Override
  public CharsetEncoder newEncoder() {    
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
  
}
