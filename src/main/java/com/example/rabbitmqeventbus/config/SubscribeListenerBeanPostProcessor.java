package com.example.rabbitmqeventbus.config;

import com.example.rabbitmqeventbus.listener.EventSubscribe;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscribeListenerBeanPostProcessor implements BeanPostProcessor {

    private final ConfigurableApplicationContext applicationContext;
    private final AmqpAdmin admin;
    private final MessageListenerContainerFactory messageListenerContainerFactory;
    private final Jackson2JsonMessageConverter jackson2JsonMessageConverter = new Jackson2JsonMessageConverter();

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        ReflectionUtils.doWithLocalMethods(bean.getClass(),
                new SubscribeMethodCallback(
                        bean, applicationContext, messageListenerContainerFactory, admin, jackson2JsonMessageConverter));
        return bean;
    }

    @AllArgsConstructor
    static class SubscribeMethodCallback implements ReflectionUtils.MethodCallback {

        private final Object bean;
        private final ConfigurableApplicationContext applicationContext;
        private final MessageListenerContainerFactory messageListenerContainerFactory;
        private final AmqpAdmin admin;
        private final Jackson2JsonMessageConverter jackson2JsonMessageConverter;

        @Override
        public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
            Method[] declaredMethods = ReflectionUtils.getDeclaredMethods(bean.getClass());
            final List<Method> methods = Arrays.stream(declaredMethods)
                    .filter(m -> m.isAnnotationPresent(EventSubscribe.class))
                    .collect(Collectors.toList());

            if (methods.isEmpty()) {
                return;
            }

            final CustomMessageListenerAdapter messageListenerAdapter = new CustomMessageListenerAdapter(bean);
            for (Method m : methods) {
                final String classSimpleName = bean.getClass().getSimpleName();
                log.info("Setting up listener for bean {} and method {}", classSimpleName, m.getName());


                final Queue queue = new Queue(classSimpleName, true);
                AbstractMessageListenerContainer container = messageListenerContainerFactory.createMessageListenerContainer(queue);
                container.setQueues(queue);

                String routingKey = "eventbus." + classSimpleName + ".*"; // Listener1.*, Listener2.*
                Binding binding = BindingBuilder.bind(queue).to(new TopicExchange("events.system")).with(routingKey);
                admin.declareQueue(queue);
                admin.declareBinding(binding);

                messageListenerAdapter.setMessageConverter(jackson2JsonMessageConverter);
                messageListenerAdapter.setDefaultListenerMethod(m.getName());
                container.setMessageListener(messageListenerAdapter);

                final EventSubscribe annotation = m.getAnnotation(EventSubscribe.class);
                final Class<?> eventType = annotation.eventType();

//                final Flux<Object> flux = Flux.create(fluxSink -> {
//                    messageListenerAdapter.setEmitter(fluxSink);
//                    container.setupMessageListener(messageListenerAdapter);
//                    fluxSink.onRequest(v -> container.start());
//                    fluxSink.onDispose(container::stop);
//                }, FluxSink.OverflowStrategy.BUFFER);
//
//                Flux.interval(Duration.ofSeconds(1)).map(aLong -> (Object) aLong).mergeWith(flux);
                applicationContext.getBeanFactory().registerSingleton(classSimpleName + "SimpleMessageListenerContainer", container);
            }
        }
    }

}
