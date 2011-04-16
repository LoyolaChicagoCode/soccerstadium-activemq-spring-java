package edu.luc.etl.webservices.jms.stadium;

public interface ICounter {

  void setCounterListener(final CounterListener listener);
  public int getCapacity();
  public void setCapacity(int capacity);
  public int getValue();
  public void increment();
  public void decrement();
}
