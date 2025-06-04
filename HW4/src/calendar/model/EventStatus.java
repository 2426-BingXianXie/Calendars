package calendar.model;

import java.util.Arrays;

import calendar.CalendarException;

public enum EventStatus {
  PUBLIC("public"), PRIVATE("private");

  private final String value;

  EventStatus(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

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
