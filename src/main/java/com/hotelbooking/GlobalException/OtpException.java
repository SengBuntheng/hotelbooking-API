package com.hotelbooking.exception;

public class OtpException extends Exception {
  public OtpException(String message) {
    super(message);
  }

  public OtpException(String message, Throwable cause) {
    super(message, cause);
  }
}