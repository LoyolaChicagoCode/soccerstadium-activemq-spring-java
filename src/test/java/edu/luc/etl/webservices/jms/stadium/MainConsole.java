package edu.luc.etl.webservices.jms.stadium;

import javax.jms.JMSException;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class MainConsole {

    public static void main(final String... args) throws JMSException {

        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
                "spring-context.xml");

        ((Display) context.getBean("display")).start();
        ((Counter) context.getBean("counter")).start();
        ((Door) context.getBean("door_north")).start();
        ((Door) context.getBean("door_south")).start();
        ((Console) context.getBean("console")).start();
    }
}
