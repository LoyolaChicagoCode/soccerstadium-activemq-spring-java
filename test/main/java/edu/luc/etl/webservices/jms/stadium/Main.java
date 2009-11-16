package edu.luc.etl.webservices.jms.stadium;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {

    public static void main(final String... args) throws Exception {

        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
                "spring-context.xml");

        ((Display) context.getBean("display")).start();
        ((Counter) context.getBean("counter")).start();

        final Door[] doors = new Door[] {
    		((Door) context.getBean("door_north")),
    		((Door) context.getBean("door_south"))
        };
        for (final Door d : doors) {
        	d.start();
        }

        final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("action [enter | leave num]> ");
        String input;
        while ((input = in.readLine()) != null) {
        	try {
	        	input = input.trim();
	            final String action = input.substring(0, 5);
	            final int num = Integer.parseInt(input.substring(5).trim());
	            if ("enter".equals(action)) {
	            	doors[num].enter();
	            } else if ("leave".equals(action)) {
	            	doors[num].leave();
	            }
        	} catch (final NumberFormatException ex1) {
        	} catch (final IndexOutOfBoundsException ex2) {
        	}
            System.out.print("action [enter | leave num]> ");
        }

    }
}
