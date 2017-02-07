package org.jpdu.gateway;

import java.io.Closeable;
import java.util.List;
import org.jpdu.model.MessageListItem;
import org.jpdu.model.UnsupportedEncodingException;
import org.jpdu.model.UssdResponse;

public interface Gateway extends Closeable {

  public void initialize();

  @Override
  public void close();

  public UssdResponse sendUssd(String data) throws UnsupportedEncodingException;

  public void sendSms();

  public void getSms();

  public List<MessageListItem> getSmsList();

}
