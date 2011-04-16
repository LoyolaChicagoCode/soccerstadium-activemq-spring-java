package edu.luc.etl.webservices.jms.stadium;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CounterImpl implements ICounter {

  private final Log log = LogFactory.getLog(CounterImpl.class);

  private int value;

  private int capacity;

  private CounterListener listener;

  public void setCounterListener(final CounterListener listener) {
    this.listener = listener;
  }

  public int getCapacity() {
    return capacity;
  }

  public void setCapacity(int capacity) {
    this.capacity = capacity;
  }

  public int getValue() {
    return value;
  }

  public void increment() {
    if (value >= capacity) {
      log.warn("attempted increment on full");
    } else {
      ++value;
      log.info("count = " + value);
      sendCount();
      if (value == capacity) {
        log.info("capacity has been reached");
        sendFull();
      }
    }
  }

  public void decrement() {
    if (value <= 0) {
      log.warn("attempted decrement on empty");
    } else {
      if (value == capacity) {
        sendNotFull();
      }
      --value;
      log.info("count = " + value);
      sendCount();
    }
  }

  protected void sendCount() {
    listener.sendCount(getValue());
  }

  protected void sendFull() {
    listener.sendFull();
  }

  protected void sendNotFull() {
    listener.sendNotFull();
  }
}
