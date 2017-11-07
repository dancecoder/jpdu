package org.jpdu.handlers;

public abstract class AbstractMessageHandler implements MessageHandler {

  private final String messageHandlerName;

  public AbstractMessageHandler(String name) {
    messageHandlerName = name;
  }
          
  @Override
  public String getName() {
    return messageHandlerName;
  }
  
}
