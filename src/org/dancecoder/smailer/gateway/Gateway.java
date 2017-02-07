package org.dancecoder.smailer.gateway;

import java.io.Closeable;
import java.util.List;
import org.dancecoder.smailer.model.MessageListItem;
import org.dancecoder.smailer.model.UnsupportedEncodingException;
import org.dancecoder.smailer.model.UssdResponse;

public interface Gateway extends Closeable {

  public void initialize();

  @Override
  public void close();

  public UssdResponse sendUssd(String data) throws UnsupportedEncodingException;

  public void sendSms();

  public void getSms();

  public List<MessageListItem> getSmsList();

}
