package org.jpdu.atutils;

import java.io.IOException;
import java.util.Properties;

public class ATError {

  private static final Properties messages;

  static {
    messages = new Properties();
    try {
      messages.load(ATError.class.getResourceAsStream("ATErrorCodes.properties"));
    } catch (IOException e) {
      System.out.print(e);
    }
  }

  private final String code;
  private final String value;
  private String description;


  public ATError(String code, String value) {
    this.code = code;
    this.value = value;
  }

  public String getCode() {
    return this.code;
  }

  public String getValue() {
    return this.value;
  }

  public String getDescription() {
    if (description == null) {
      String key;
      if (Character.isAlphabetic(code.charAt(0))) {
        key = code.replace(' ', '_') + '_' + value;
      } else {
        key = code.substring(1).replace(' ', '_') + '_' + value;
      }
      description = messages.getProperty(key, "unknown");
    }    
    return description;
  }

  @Override
  public String toString() {
    if (value != null) {
      return code + ':' +  ' ' + value + ' ' + this.getDescription();
    }
    return code;
  }

}
