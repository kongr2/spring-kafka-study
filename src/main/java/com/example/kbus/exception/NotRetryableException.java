package com.example.kbus.exception;

public class NotRetryableException extends RuntimeException {

  public NotRetryableException(String message) {
    super(message);
  }
}
