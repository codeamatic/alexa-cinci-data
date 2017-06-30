package com.codeamatic.exceptions;

/**
 * This is an exception thrown when a date is out of range of the supported features.
 * Example: future dates, past dates
 */
public class DateRangeException extends Exception {
  public DateRangeException(String message, Exception e) {
    super(message, e);
  }

  public DateRangeException(String message) {
    super(message);
  }

  public DateRangeException(Exception e) {
    super(e);
  }
}
