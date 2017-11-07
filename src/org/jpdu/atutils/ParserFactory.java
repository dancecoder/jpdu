package org.jpdu.atutils;

public class ParserFactory {
  public static Parser createParser(String command) {
    switch (command) {
      case "+CMGL":
        return new CMGLParser(); // stored messages
      case "+CMT":
        return new CMTParser(); // incoming message unsolicited result code 
      case "+CME ERROR":
      case "+CMS ERROR":
        return new ErrorParser(command);
      case "+CLIP":
        return new CLIPParser(); // caller id unsolicited result code 
      case "+GCAP":
        return new GCAPParser(); // complete capabilities list (V.250)
      default:
        return new EolParser();
    }    
  }
}
