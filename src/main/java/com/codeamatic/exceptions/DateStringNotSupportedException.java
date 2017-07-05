package com.codeamatic.exceptions;

/**
 * This is an exception thrown that indicates a requested date string is not supported.
 */
public class DateStringNotSupportedException extends Exception {
  public DateStringNotSupportedException(String message, Exception e) {
    super(message, e);
  }

  public DateStringNotSupportedException(String message) {
    super(message);
  }

  public DateStringNotSupportedException(Exception e) {
    super(e);
  }
}