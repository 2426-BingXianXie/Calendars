package calendar;

/**
 * Represents an exception specific to calendar operations.
 * This exception is typically thrown when an operation on the calendar
 * model or its components violates business rules or encounters an unexpected state.
 */
public class CalendarException extends Exception {
  /**
   * Constructs a new CalendarException with the specified detail message.
   *
   * @param message the detail message (which is saved for later retrieval by the
   * {@link Throwable#getMessage()} method).
   */
  public CalendarException(String message) {
    super(message);
  }
}