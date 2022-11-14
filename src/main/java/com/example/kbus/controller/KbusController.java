package com.example.kbus.controller;

import com.example.kbus.event.Event;
import com.example.kbus.event.OrderEvent;
import com.example.kbus.service.KbusProducer;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class KbusController {

  private final KbusProducer kbusProducer;

  @GetMapping("/{topic}/simple")
  public String publish(@PathVariable String topic, @RequestParam String payload){
    kbusProducer.sendMessage(topic, payload);
    return "SUCCESS";
  }

  @GetMapping("/{topic}/event")
  public String publishEvent(@PathVariable String topic, @RequestParam String item){
    OrderEvent order = new OrderEvent(UUID.randomUUID(), item, "1000",10);
    Event event = new Event(UUID.randomUUID(), "INSERT_ORDER", Timestamp.from(Instant.now()), order);
    kbusProducer.sendMessage(topic, order.orderId().toString(), event);
    return "SUCCESS";
  }
}
