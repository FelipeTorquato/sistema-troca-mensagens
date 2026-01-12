package org.example;

import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.ConnectionFactory;

public class Config {
    public static final String BROKER_URL = "tcp://localhost:61616";

    public static ConnectionFactory getConnectionFactory() {
        return new ActiveMQConnectionFactory(BROKER_URL);
    }
}
