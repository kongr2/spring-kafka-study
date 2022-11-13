package com.example.kbus.exception;

public class RetryableException extends RuntimeException {

  public RetryableException(String message) {
    super(message);
  }
}
