package org.jpdu.atutils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class ATResponse {
  
  List<ATResultCode> results = new ArrayList<>();

  public ATResponse(InputStream stream) throws IOException {
    ATResultCode rc;
    do {
      rc = new ATResultCode(stream);
      if (rc.getResultCode() == null) {
        return;
      }
      results.add(rc);
    } while (!rc.isTerminating());
  }

  public ATResponse(String data) throws IOException {    
    this(new ByteArrayInputStream(data.getBytes()));
  }

  public List<ATResultCode> getResults() {
    return this.results;
  }

  public boolean haveResult(String code) {
    for (ATResultCode rc : results) {
      if (code.equals(rc.getResultCode())) {
        return true;
      }
    }
    return false;
  }

  public boolean isOk() {
    return haveResult("OK");
  }

  public ATResultCode getFirstOf(String code) {
    for (ATResultCode rc : results) {
      if (code.equals(rc.getResultCode())) {
        return rc;
      }
    }
    return null;
  }

  public Iterable<ATResultCode> getAlltOf(String code) {

    final Iterator<ATResultCode> it = results.listIterator();

    return () -> new Iterator<ATResultCode>() {

      ATResultCode next = null;

      @Override
      public boolean hasNext() {
        if (next == null) {
          while (it.hasNext()) {
            ATResultCode rc = it.next();
            if (code.equals(rc.getResultCode())) {
              next = rc;
              break;
            }
          }
        }
        return next != null;
      }
      
      @Override
      public ATResultCode next() {
        if (hasNext()) {
          ATResultCode rc = next;
          next = null;
          return rc;
        } else {
          throw new NoSuchElementException();
        }
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException("remove");
      }

    }; // Iterable
  }

  public ATError getError() {
    for (ATResultCode rc : results) {
      String code = rc.getResultCode();
      if ("ERROR".equals(code) || "+CMS ERROR".equals(code) || "+CME ERROR".equals(code)) {
        return (ATError)rc.getResultValue();
      }
    }
    return null;
  }
}
