package edu.luc.etl.webservices.jms.stadium;

import static edu.luc.etl.webservices.jms.stadium.Constants.COUNT;
import static edu.luc.etl.webservices.jms.stadium.Constants.DECREMENT;
import static edu.luc.etl.webservices.jms.stadium.Constants.EVENT;
import static edu.luc.etl.webservices.jms.stadium.Constants.FULL;
import static edu.luc.etl.webservices.jms.stadium.Constants.INCREMENT;
import static edu.luc.etl.webservices.jms.stadium.Constants.NOTFULL;
import static edu.luc.etl.webservices.jms.stadium.Constants.VALUE;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

public class Counter implements MessageListener, CounterListener {

  private final Log log = LogFactory.getLog(Counter.class);

  private final String id = "counter";

  private JmsTemplate template;

  private JmsTemplate producerTemplate;

  private Destination destination;

  private Connection connection;

  private Session session;

  private MessageConsumer consumer;

  private CounterImpl theCounter;

  public void setCounterImpl(final CounterImpl theCounter) {
    this.theCounter = theCounter;
    theCounter.setCounterListener(this);
  }

  public JmsTemplate getTemplate() {
    return template;
  }

  public void setTemplate(final JmsTemplate template) {
    this.template = template;
  }

  public JmsTemplate getProducerTemplate() {
    return producerTemplate;
  }

  public void setProducerTemplate(JmsTemplate producerTemplate) {
    this.producerTemplate = producerTemplate;
  }

  public Destination getDestination() {
    return destination;
  }

  public void setDestination(final Destination destination) {
    this.destination = destination;
  }

  public void sendCount(final int value) {
    log.debug("sendCount: " + value);
    producerTemplate.send(destination, new MessageCreator() {
      public Message createMessage(Session session) throws JMSException {
        final Message message = session.createMessage();
        message.setStringProperty(EVENT, COUNT);
        message.setIntProperty(VALUE, value);
        return message;
      }
    });
  }

  public void sendFull() {
    log.debug("received sendFull event from counter");
    producerTemplate.send(destination, new MessageCreator() {
      public Message createMessage(Session session) throws JMSException {
        final Message message = session.createMessage();
        message.setStringProperty(EVENT, FULL);
        return message;
      }
    });
  }

  public void sendNotFull() {
    log.debug("received sendNotFull event from counter");
    producerTemplate.send(destination, new MessageCreator() {
      public Message createMessage(Session session) throws JMSException {
        final Message message = session.createMessage();
        message.setStringProperty(EVENT, NOTFULL);
        return message;
      }
    });
  }

  public void start() throws JMSException {
    final String selector = "event = 'increment' OR event = 'decrement'";

    final ConnectionFactory connectionFactory = template.getConnectionFactory();
    connection = connectionFactory.createConnection();

    synchronized (connection) {
      if (connection.getClientID() == null) {
        connection.setClientID(id);
      }
    }

    connection.start();

    session = connection.createSession(true, Session.CLIENT_ACKNOWLEDGE);
    consumer = session.createConsumer(destination, selector, false);
    consumer.setMessageListener(this);
  }

  public void stop() throws JMSException {
    if (consumer != null)
      consumer.close();
    if (session != null)
      session.close();
    if (connection != null)
      connection.close();
  }

  public void onMessage(final Message message) {
    log.debug("received message: " + message);
    try {
      final String event = message.getStringProperty(EVENT);
      if (INCREMENT.equals(event)) {
        theCounter.increment();
      } else if (DECREMENT.equals(event)) {
        theCounter.decrement();
      } else {
        log.warn("unknown event: " + event);
      }
      message.acknowledge();
    } catch (final JMSException ex) {
      ex.printStackTrace();
    }
  }

}
