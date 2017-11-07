package org.jpdu.handlers;

import java.util.ArrayList;
import java.util.Collection;
import org.jpdu.handlers.MessageAcceptor.Result;
import org.jpdu.pdu.PDUImpl;


public abstract class AbstractMessageEmitter extends AbstractMessageHandler implements MessageEmitter {

  private final Collection<MessageAcceptor> acceptors = new ArrayList<>();

  public AbstractMessageEmitter(String name) {
    super(name);
  }

  @Override
  public void addMessageAcceptor(MessageAcceptor acceptor) {
    synchronized(acceptors) {
      acceptors.add(acceptor);
    }
  }

  public boolean emitMessage(PDUImpl message) {
    synchronized(acceptors) {
      for(MessageAcceptor acceptor : acceptors) {
        Result result = acceptor.acceptMessage(message);
        switch(result) {
          case Accepted: return true;
          case Failed: 
            System.out.println("Message acceptor " + acceptor.getName() + " failed");
            break;
          default: break;
        }
      }
      return false;
    }
  }

}
