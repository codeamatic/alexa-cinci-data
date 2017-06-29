package com.codeamatic.exceptions;

/**
 * This is an exception thrown from the {@code CincyDataSpeechlet} that indicates a requested neighborhood is not supported.
 */
public class NeighborhoodNotSupportedException extends Exception {
  public NeighborhoodNotSupportedException(String message, Exception e) {
    super(message, e);
  }

  public NeighborhoodNotSupportedException(String message) {
    super(message);
  }

  public NeighborhoodNotSupportedException(Exception e) {
    super(e);
  }
}
