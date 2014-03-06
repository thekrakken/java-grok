package com.nflabs.grok;

/**
 * Signals that an Grok exception of some sort has occurred. This class is the general class of
 * exceptions produced by failed or interrupted Grok operations.
 *
 * @author anthonyc
 *
 */
public class GrokException extends Exception {

  /**
   * Generated
   */
  private static final long serialVersionUID = 1L;

  public GrokException() {
    super();
  }

  /**
   *
   * @param message
   * @param cause
   */
  public GrokException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   *
   * @param message
   */
  public GrokException(String message) {
    super(message);
  }

  /**
   *
   * @param cause
   */
  public GrokException(Throwable cause) {
    super(cause);
  }

}
