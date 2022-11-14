package com.example.kbus.service;

import com.example.kbus.event.Event;
import com.example.kbus.event.OrderEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KbusFailedConsumer {

  private final ObjectMapper objectMapper;

  @KafkaListener(
      id = "${kbus.kafka.failed-consumer-id}",
      topics = "${kbus.kafka.failed-topic}",
      groupId = "${kbus.kafka.failed-group}",
      errorHandler = "errorHandler" //빈으로 등록한 에러 핸들러 빈이름
  )
  public void listen(
      @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key,
      @Header("trace_id") String traceId,
      @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partitionId,
      @Header(KafkaHeaders.CONSUMER) Consumer consumer,
      @Header(KafkaHeaders.ACKNOWLEDGMENT)Acknowledgment acknowledgment,
      @Payload String payload
  ) {

    try {
      Event event = objectMapper.readValue(payload, new TypeReference<Event<OrderEvent>>(){});
      log.info("""
        [FAILED_TOPIC]
        \n
        =======================================================
        key: {}, traceId: {}, partitionId: {}, consumerId: {}, ThreadId: {}\n
        event: {}
        =======================================================
        """, key, traceId,partitionId, consumer.groupMetadata().memberId(), Thread.currentThread().getId(),
          event);

      //명시적으로 받았음을 확인 및 커밋요청 한다.
      acknowledgment.acknowledge();

    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
