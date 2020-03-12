package com.example.rabbitmqeventbus.config;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageListenerContainerFactory {

    private final ConnectionFactory connectionFactory;

    public AbstractMessageListenerContainer createMessageListenerContainer(String... queueNames) {
        SimpleMessageListenerContainer mlc = new SimpleMessageListenerContainer(connectionFactory);
        mlc.addQueueNames(queueNames);
        return mlc;
    }

    public AbstractMessageListenerContainer createMessageListenerContainer(Queue... queues) {
        SimpleMessageListenerContainer mlc = new SimpleMessageListenerContainer(connectionFactory);
        mlc.addQueues(queues);
        return mlc;
    }
}
