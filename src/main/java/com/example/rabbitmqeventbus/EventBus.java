package com.example.rabbitmqeventbus;

import com.example.rabbitmqeventbus.listener.EventListener;
import com.example.rabbitmqeventbus.listener.EventSubscribe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventBus {

    private final AmqpTemplate amqpTemplate;
    private final ApplicationContext applicationContext;
    private final ConcurrentMap<Class<?>, List<String>> eventListenerRoutingKeys = new ConcurrentHashMap<>();

    @PostConstruct
    public void postConstruct() {
        final Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(EventListener.class);
        beansWithAnnotation.forEach((key1, value1) -> {
            final Method[] declaredMethods = ReflectionUtils.getDeclaredMethods(value1.getClass());
            final Map<? extends Class<?>, List<Method>> collect = Arrays.stream(declaredMethods)
                    .filter(m -> m.isAnnotationPresent(EventSubscribe.class))
                    .collect(Collectors.groupingBy(method -> {
                        final EventSubscribe annotation = method.getAnnotation(EventSubscribe.class);
                        return annotation.eventType();
                    }));

            collect.forEach((key, value) -> {
                final String simpleName = value1.getClass().getSimpleName();
                if (eventListenerRoutingKeys.containsKey(key)) {
                    eventListenerRoutingKeys.get(key).add(simpleName);
                } else {
                    final ArrayList<String> list = new ArrayList<>();
                    list.add(simpleName);
                    eventListenerRoutingKeys.put(key, list);
                }
            });

        });
    }

    public <T> void sendEvent(T event) {
        // send to events.system with key: eventbus.Listener1.SimpleMessage
        final Class<?> eventClass = event.getClass();
        if (!eventListenerRoutingKeys.containsKey(eventClass)) {
            log.error("Could not resolve routing key for event: {}. Will skip send this event.", eventClass.getSimpleName());
            return;
        }

        eventListenerRoutingKeys.get(eventClass).forEach(routingKey ->
                amqpTemplate.convertAndSend("events.system", "eventbus." + routingKey + "." + eventClass.getSimpleName(), event)
        );
    }
}
