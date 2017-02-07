package org.dancecoder.smailer.gateway.modem;

public class ParserFactory {
  public static Parser createParser(String command) {
    if (null != command) switch (command) {
      case "+CMGL":
        return new MessageListParser();
      case "+CME ERROR":
        return new CmeErrorParser();
      case "+CMS ERROR":
        return new CmsErrorParser();
    }
    return null;
  }
}
