package com.example.kbus.event;

import java.sql.Timestamp;
import java.util.UUID;

public record Event<T>(
  UUID eventId,
  String eventType,
  Timestamp createdAt,
  T payload
){

}
