package com.example.kbus.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NonBlockingConsumer {

  /**
   * 해당 토픽을 리스닝 할 경우 3초 * 2 단위로 3번 시도 후 실패시  dead letter topic 으로 전송 한다.
   * 이때 시도별 토픽은 자동으로 생성 되며, dlt 또한 자동으로 생성 하고 있다.
   * 토픽 생성 규칙은 원래 토픽 이름 + backoff value 값으로 정해진다.
   * dlt 토픽이름은 원래 토픽 이름 + dlt-xx 로 생성된다.
   *
   * original topic & group name : kbus-retryable-topic -> kbus-retryable-group
   *
   * 해당 시나리오는 정해진 시도 횟수 만큼 실패시 dlt 로 전송 한다고 가정 한것이다.
   * 만약 시도중 성공 한다면 dlt 전송은 하지 않을것이다.
   *
   * kbus-retrayable-topic-retry-3000 -> kbus-retryable-group-retry-3000
   * kbus-retrayable-topic-retry-6000 -> kbus-retryable-group-retry-6000
   * kbus-retrayable-topic-retry-12000 -> kbus-retryable-group-retry-12000
   * kbus-retryable-topic-dlt
   */
  @RetryableTopic(
      attempts = "4",
      backoff = @Backoff(value = 3000L, multiplier = 2)
  )
  @KafkaListener(
      id = "${kbus.kafka.retryable-consumer-id}",
      topics = "${kbus.kafka.retryable-topic}",
      groupId = "${kbus.kafka.retryable-group}"
  )
  public void listen(
      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
      @Header(KafkaHeaders.DELIVERY_ATTEMPT) int delivery,
      @Header(KafkaHeaders.GROUP_ID) String groupId,
      @Payload String payload
  ){
    log.info("""
        \n
        =======================================================
        topic: {}, ThreadId: {}, delivery: {}, groupId: {}\n
        payload: {}
        =======================================================
        """, topic, Thread.currentThread().getId(),delivery,groupId, payload);
    throw new RuntimeException("some error occured");
  }

  @DltHandler
  public void dltHandler(@Payload String payload) {
    //dlt 이벤트에 대한 핸들러
    log.error("Received some event in dlt topic: {}", payload);
  }
}
