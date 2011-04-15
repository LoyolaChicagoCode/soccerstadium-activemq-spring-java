package edu.luc.etl.webservices.jms.stadium;

import static edu.luc.etl.webservices.jms.stadium.Constants.FULL;
import static edu.luc.etl.webservices.jms.stadium.Constants.DECREMENT;
import static edu.luc.etl.webservices.jms.stadium.Constants.EVENT;
import static edu.luc.etl.webservices.jms.stadium.Constants.INCREMENT;
import static edu.luc.etl.webservices.jms.stadium.Constants.LOCATION;
import static edu.luc.etl.webservices.jms.stadium.Constants.NOTFULL;

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

public class Door implements MessageListener {

  private final Log log = LogFactory.getLog(Door.class);

  private boolean blocked = false;

  private String location;

  private String id;

  private JmsTemplate template;

  private JmsTemplate producerTemplate;

  private Destination destination;

  private Connection connection;

  private Session session;

  private MessageConsumer consumer;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public JmsTemplate getTemplate() {
    return template;
  }

  public void setTemplate(JmsTemplate template) {
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

  public void setDestination(Destination destination) {
    this.destination = destination;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public void enter() {
    if (!isBlocked()) {
      log.info("someone entered through " + getLocation());
      sendIncrement();
    } else {
      log.info("someone was blocked from entering through " + getLocation());
    }
  }

  public void leave() {
    log.info("someone left through " + getLocation());
    sendDecrement();
  }

  public boolean isBlocked() {
    return blocked;
  }

  public void setBlocked(final boolean blocked) {
    if (blocked == this.blocked) {
      log.debug("door already " + (blocked ? "" : "un") + "blocked");
      return;
    }
    log.info((blocked ? "" : "un") + "blocking door at " + getLocation());
    this.blocked = blocked;
  }

  protected void sendIncrement() {
    producerTemplate.send(destination, new MessageCreator() {
      public Message createMessage(Session session) throws JMSException {
        final Message message = session.createMessage();
        message.setStringProperty(EVENT, INCREMENT);
        message.setStringProperty(LOCATION, getLocation());
        return message;
      }
    });
  }

  protected void sendDecrement() {
    producerTemplate.send(destination, new MessageCreator() {
      public Message createMessage(Session session) throws JMSException {
        final Message message = session.createMessage();
        message.setStringProperty(EVENT, DECREMENT);
        message.setStringProperty(LOCATION, getLocation());
        return message;
      }
    });
  }

  public void start() throws JMSException {
    String selector = "event = 'full' OR event = 'notfull'";

    ConnectionFactory connectionFactory = template.getConnectionFactory();
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

  public void onMessage(Message message) {
    try {
      final String event = message.getStringProperty(EVENT);
      if (FULL.equals(event)) {
        setBlocked(true);
      } else if (NOTFULL.equals(event)) {
        setBlocked(false);
      } else {
        log.warn("unknown event: " + event);
      }
      message.acknowledge();
    } catch (JMSException e) {
      e.printStackTrace();
    }
  }

}
