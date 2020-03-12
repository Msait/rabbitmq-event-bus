package com.example.rabbitmqeventbus;

import com.example.rabbitmqeventbus.event.SimpleMessage;
import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.AsyncRabbitTemplate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.stream.IntStream;

@SpringBootApplication
@AllArgsConstructor
public class RabbitmqEventBusApplication implements CommandLineRunner {

    private final EventBus eventBus;

    public static void main(String[] args) {
        SpringApplication.run(RabbitmqEventBusApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        IntStream.range(0, 10)
                .forEach(i -> eventBus.sendEvent(new SimpleMessage("Simple Message " + i)));

    }
}
