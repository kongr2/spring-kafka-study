package com.example.kbus.event;

import java.util.UUID;

public record OrderEvent(
    UUID orderId,
    String itemName,
    String itemPrice,
    int itemCount
){

}
