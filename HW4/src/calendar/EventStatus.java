package calendar;

import java.util.Arrays;

public enum EventStatus {
  PUBLIC("public"), PRIVATE("private");

  private final String value;

  EventStatus(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static EventStatus fromStr(String s) {
    for (EventStatus eventStatus : values()) {
      if (eventStatus.value.equalsIgnoreCase(s)) {
        return eventStatus;
      }
    }
    throw new IllegalArgumentException("Invalid event status: " + s +
            ", valid values are: " + Arrays.toString(values()));
  }
}
