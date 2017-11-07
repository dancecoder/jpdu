package org.jpdu.handlers;

import org.jpdu.pdu.PDUImpl;


public interface MessageAcceptor extends MessageHandler {

  public enum Result {
    Accepted,
    NotReady,
    Rejected,
    Failed
  }

  Result acceptMessage(PDUImpl message);

}
