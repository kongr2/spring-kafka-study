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

  public void sendMessage(String topic, String message){
    ListenableFuture<SendResult<String, String>> publish =  kafkaTemplate.send(topic, message);
    publish.addCallback(sCallback->{
      log.info("[SUCCESS] topic: {}, offset: {}, partition: {}"
          , sCallback.getRecordMetadata().topic()
          , sCallback.getRecordMetadata().offset()
          , sCallback.getRecordMetadata().partition());
    }, fCallback->{
      log.error("[FAIL] :{}", fCallback.getMessage());
    });
  }

  public <T> void sendMessage(String topic, String key, Event<T> event) {
    try {
      String payload = objectMapper.writeValueAsString(event);
      ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic, key, payload);

      producerRecord.headers().add("trace_id", UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));

      ListenableFuture<SendResult<String, String>> publish =  kafkaTemplate.send(producerRecord);

      publish.addCallback(sCallback->{
        log.info("[SUCCESS] topic: {}, offset: {}, partition: {}"
            , sCallback.getRecordMetadata().topic()
            , sCallback.getRecordMetadata().offset()
            , sCallback.getRecordMetadata().partition());
      }, fCallback->{
        log.error("[FAIL] :{}", fCallback.getMessage());
      });

    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
