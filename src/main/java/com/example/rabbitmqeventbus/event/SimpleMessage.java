package com.example.rabbitmqeventbus.event;

import lombok.Value;

import java.io.Serializable;
import java.time.Instant;

@Value
public class SimpleMessage implements Serializable {

    String msg;
    Instant received = Instant.now();

}
