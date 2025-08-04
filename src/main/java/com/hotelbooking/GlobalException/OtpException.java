package com.hotelbooking.GlobalException;

public class OtpException extends RuntimeException {
  public OtpException(String message) {
    super(message);
  }

  public OtpException(String message, Throwable cause) {
    super(message, cause);
  }



  public static class AuthenticationFailedException extends RuntimeException {
    public AuthenticationFailedException(String message) {
      super(message);
    }
  }

  public static class OtpVerificationException extends RuntimeException {
    public OtpVerificationException(String message) {
      super(message);
    }
  }

  public static class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
      super(message);
    }
  }
}