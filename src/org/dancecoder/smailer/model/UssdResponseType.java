package org.dancecoder.smailer.model;

public enum UssdResponseType {
  noActionRequired,
  userActionRequired,
  terminatedByNetwork,
  otherLocalClientResponded,
  operationNotSupported,
  networkTimeOut,
  unknown
}
