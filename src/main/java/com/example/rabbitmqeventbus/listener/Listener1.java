package com.example.rabbitmqeventbus.listener;

import com.example.rabbitmqeventbus.event.SimpleMessage;
import lombok.extern.slf4j.Slf4j;

@EventListener
@Slf4j
public class Listener1 {

    @EventSubscribe(eventType = SimpleMessage.class)
    public void handleSimpleMessage(SimpleMessage simpleMessage) {
        log.info("Handling {}", simpleMessage);
    }
}
