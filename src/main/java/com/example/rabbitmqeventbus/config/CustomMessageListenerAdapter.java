package com.example.rabbitmqeventbus.config;

import lombok.Setter;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.MessageConverter;
import reactor.core.publisher.FluxSink;

public class CustomMessageListenerAdapter extends MessageListenerAdapter {

    public CustomMessageListenerAdapter() {
    }

    public CustomMessageListenerAdapter(Object delegate) {
        super(delegate);
    }

    public CustomMessageListenerAdapter(Object delegate, MessageConverter messageConverter) {
        super(delegate, messageConverter);
    }

    public CustomMessageListenerAdapter(Object delegate, String defaultListenerMethod) {
        super(delegate, defaultListenerMethod);
    }

    @Setter
    private FluxSink<Object> emitter;

    @Override
    public void onMessage(Message message) {
        if (emitter == null) {
            super.onMessage(message);
        } else {
            emitter.next(message);
        }
    }
}
