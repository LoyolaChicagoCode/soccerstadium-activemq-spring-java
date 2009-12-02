package edu.luc.etl.webservices.jms.stadium;

import static edu.luc.etl.webservices.jms.stadium.Constants.COUNT;
import static edu.luc.etl.webservices.jms.stadium.Constants.EVENT;
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

public class Display implements MessageListener {

	private final Log log = LogFactory.getLog(Display.class);

	private String id = "display";

	private JmsTemplate template;

	private Destination destination;

	private Connection connection;

	private Session session;

	private MessageConsumer consumer;

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

	public void start() throws JMSException {
		String selector = "event = 'count'";

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
			if (COUNT.equals(event)) {
				final int value = message.getIntProperty(VALUE);
				log.info("count = " + value);
			} else {
				log.warn("unknown event: " + event);
			}
			message.acknowledge();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

}
