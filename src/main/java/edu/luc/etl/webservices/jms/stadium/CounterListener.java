package edu.luc.etl.webservices.jms.stadium;

public interface CounterListener {

  void sendCount(int value);

  void sendFull();

  void sendNotFull();
}
