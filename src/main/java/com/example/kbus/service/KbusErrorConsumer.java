package com.example.kbus.service;

import com.example.kbus.exception.NotRetryableException;
import com.example.kbus.exception.RetryableException;
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
      errorHandler = "errorHandler" //빈으로 등록한 에러 핸들러 빈이름
  )

  public void listen(
      @Header(KafkaHeaders.DELIVERY_ATTEMPT) int delivery,
      @Header(KafkaHeaders.CONSUMER) Consumer consumer,
      @Payload String payload
  ) {
    log.info("del@SendTo(\"${kbus.kafka.failed-topic}\")iver attempt: {}", delivery);
    log.info("ErrorPayload: {}", payload);

    //기본적으로 10번 시도 하지만 여기서 3번 시도 후 중지하도록 로직 추가 함.
    if(delivery >= 3) {
      consumer.pause(consumer.assignment());
    } else {
      throw new NotRetryableException("some error occurred");
//      throw new RetryableException("some error occurred");
    }
  }

}
