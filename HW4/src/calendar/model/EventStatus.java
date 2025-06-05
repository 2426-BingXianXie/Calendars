package calendar.model;

import java.util.Arrays;

import calendar.CalendarException;

/**
 * Represents the status of an event in a calendar.
 * The status can be either PUBLIC or PRIVATE.
 */
public enum EventStatus {
  PUBLIC("public"), PRIVATE("private");

  private final String value;

  /**
   * Constructs an EventStatus with the specified value.
   *
   * @param value the string representation of the event status
   */
  EventStatus(String value) {
    this.value = value;
  }

  /**
   * Returns the string representation of the event status.
   *
   * @return the string value of the event status
   */
  public String getValue() {
    return value;
  }

  /**
   * Converts a string to the corresponding EventStatus.
   *
   * @param s the string representation of the event status
   * @return the EventStatus corresponding to the string
   * @throws CalendarException if the string does not match any valid event status
   */
  public static EventStatus fromStr(String s) throws CalendarException {
    for (EventStatus eventStatus : values()) {
      if (eventStatus.value.equalsIgnoreCase(s)) {
        return eventStatus;
      }
    }
    throw new CalendarException("Invalid event status: " + s +
            ", valid values are: " + Arrays.toString(values()));
  }
}
