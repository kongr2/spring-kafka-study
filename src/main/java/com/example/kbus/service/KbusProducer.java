package com.example.kbus.service;

import com.example.kbus.event.Event;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class KbusProducer {

  private final KafkaTemplate<String,String> kafkaTemplate;
  private final ObjectMapper objectMapper;


  // 단순 문자열 발행 하기
  public void sendMessage(String topic, String message){
    ListenableFuture<SendResult<String, String>> publish =  kafkaTemplate.send(topic, message);
    publish.addCallback(sCallback->{
      log.info("[PUB_SUCCESS] topic: {}, offset: {}, partition: {}"
          , sCallback.getRecordMetadata().topic()
          , sCallback.getRecordMetadata().offset()
          , sCallback.getRecordMetadata().partition());
    }, fCallback->{
      log.error("[PUB_FAIL] :{}", fCallback.getMessage());
    });
  }

  //제네릭 객체 이벤트를 받아서 처리
  public <T> void sendMessage(String topic, String key, Event<T> event) {
    try {
      String payload = objectMapper.writeValueAsString(event);
      ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic, key, payload);

      //발행 메시지 추적을 위해 커스텀 헤더 값을 추가하고 컨슈머에서 해당 값들을 필터링 해보도록 한다.
      producerRecord.headers().add("trace_id", UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));

      ListenableFuture<SendResult<String, String>> publish =  kafkaTemplate.send(producerRecord);

      publish.addCallback(successCallback->{
        //성공 했을경우
        log.info("[PUB-SUCCESS] topic: {}, offset: {}, partition: {}"
            , successCallback.getRecordMetadata().topic()
            , successCallback.getRecordMetadata().offset()
            , successCallback.getRecordMetadata().partition());
      }, failCallback->{
        log.error("[PUB-FAIL] :{}", failCallback.getMessage());
      });

    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
