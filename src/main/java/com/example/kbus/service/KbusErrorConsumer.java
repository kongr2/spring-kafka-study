package com.example.kbus.service;

import com.example.kbus.exception.NotRetryableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KbusErrorConsumer {

  @KafkaListener(
      id = "${kbus.kafka.error-consumer-id}",
      topics = "${kbus.kafka.error-topic}",
      groupId = "${kbus.kafka.error-group}",
      errorHandler = "errorHandler"
  )
  @SendTo("${kbus.kafka.failed-topic}")
  public void listen(
      @Header(KafkaHeaders.DELIVERY_ATTEMPT) int delivery,
      @Header(KafkaHeaders.CONSUMER) Consumer consumer,
      @Payload String payload
  ) {
    log.info("deliver attempt: {}", delivery);
    log.info("ErrorPayload: {}", payload);

    if(delivery >= 3) {
      consumer.pause(consumer.assignment());
    } else {
      throw new NotRetryableException("some error occurred");
      //throw new RetryableException("some error occurred");
    }
  }

}
