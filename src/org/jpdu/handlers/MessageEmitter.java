package org.jpdu.handlers;


public interface MessageEmitter extends MessageHandler {

  void addMessageAcceptor(MessageAcceptor acceptor);

}
