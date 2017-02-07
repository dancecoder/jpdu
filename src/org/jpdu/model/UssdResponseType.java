package org.jpdu.model;

public enum UssdResponseType {
  noActionRequired,
  userActionRequired,
  terminatedByNetwork,
  otherLocalClientResponded,
  operationNotSupported,
  networkTimeOut,
  unknown
}
