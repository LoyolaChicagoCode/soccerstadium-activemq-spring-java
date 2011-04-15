package edu.luc.etl.webservices.jms.stadium;

import static edu.luc.etl.webservices.jms.stadium.Constants.EVENT;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

public class Console {

  private final Log log = LogFactory.getLog(Console.class);

  private JmsTemplate template;

  private Destination destination;

  public JmsTemplate getTemplate() {
    return template;
  }

  public void setTemplate(JmsTemplate template) {
    this.template = template;
  }

  public Destination getDestination() {
    return destination;
  }

  public void setDestination(Destination destination) {
    this.destination = destination;
  }

  public void start() {
    final BufferedReader in = new BufferedReader(new InputStreamReader(
        System.in));
    System.out.print("event> ");
    String input;
    try {
      while ((input = in.readLine()) != null) {
        final String event = input.trim();
        if (event.length() > 0) {
          log.debug("sending event " + event);
          template.send(destination, new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
              final Message message = session.createMessage();
              message.setStringProperty(EVENT, event);
              return message;
            }
          });
          log.debug("sent event " + event);
        }
        System.out.print("event> ");
      }
    } catch (final IOException ex) {
      ex.printStackTrace();
    }
  }
}