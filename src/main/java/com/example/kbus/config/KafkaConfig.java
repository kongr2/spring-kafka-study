package com.example.kbus.config;

import com.example.kbus.exception.NotRetryableException;
import com.example.kbus.exception.RetryableException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.protocol.types.Field.Str;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties.AckMode;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.KafkaListenerErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Slf4j
@Configuration
public class KafkaConfig {

  @Bean
  public NewTopic topic(
      @Value("${kbus.kafka.topic}") String topic,
      @Value("${kbus.kafka.partitions}") int partitions,
      @Value("${kbus.kafka.replicas}") int replicas
  ) {
    return TopicBuilder.name(topic)
        .partitions(partitions)
        .replicas(replicas)
        .build();
  }

  @Bean
  public NewTopic errorTopic(
      @Value("${kbus.kafka.error-topic}") String topic,
      @Value("${kbus.kafka.partitions}") int partitions,
      @Value("${kbus.kafka.replicas}") int replicas
  ) {
    return TopicBuilder.name(topic)
        .partitions(partitions)
        .replicas(replicas)
        .build();
  }
  @Bean
  public NewTopic failedTopic(
      @Value("${kbus.kafka.failed-topic}") String topic,
      @Value("${kbus.kafka.partitions}") int partitions,
      @Value("${kbus.kafka.replicas}") int replicas
  ) {
    return TopicBuilder.name(topic)
        .partitions(partitions)
        .replicas(replicas)
        .build();
  }

  @Bean
  public NewTopic retryableTopic(
      @Value("${kbus.kafka.retryable-topic}") String topic,
      @Value("${kbus.kafka.partitions}") int partitions,
      @Value("${kbus.kafka.replicas}") int replicas
  ) {
    return TopicBuilder.name(topic)
        .partitions(partitions)
        .replicas(replicas)
        .build();
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
      ConsumerFactory<String, String> consumerFactory, DefaultErrorHandler errorHandler
  ){
    ConcurrentKafkaListenerContainerFactory factory = new ConcurrentKafkaListenerContainerFactory();
    factory.setConsumerFactory(consumerFactory);
    factory.setCommonErrorHandler(errorHandler);
    factory.getContainerProperties().setDeliveryAttemptHeader(true);

    factory.getContainerProperties().setDeliveryAttemptHeader(true);

    factory.getContainerProperties().setAckMode(AckMode.MANUAL_IMMEDIATE);
    return factory;
  }

  @Bean
  public KafkaListenerErrorHandler errorHandler() {
    return (message, exception) -> {
      log.info("inside error handler: {}", message);
      //return "[FAILED] {}" + message;
      throw new RuntimeException("inside error handler error");
    };
  }


  @Bean
  public DefaultErrorHandler defaultErrorHandler() {
    DefaultErrorHandler defaultErrorHandler = new DefaultErrorHandler((consumerRecord, e) -> {
      log.info("inside default handler");
    }, new FixedBackOff(1000L, 2)); // 1초 간격으로 2번만 시도 하도록 한다.

    defaultErrorHandler.setAckAfterHandle(false);

    defaultErrorHandler.addNotRetryableExceptions(NotRetryableException.class);
    defaultErrorHandler.addRetryableExceptions(RetryableException.class);
    return defaultErrorHandler;
  }

}
