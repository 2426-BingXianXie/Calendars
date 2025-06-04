package calendar.model;

import java.util.Arrays;

import calendar.CalendarException;

public enum Location {
  PHYSICAL("physical"), ONLINE("online");

  private final String value;

  Location(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static Location fromStr(String s) throws CalendarException {
    for (Location location : values()) {
      if (location.value.equalsIgnoreCase(s)) {
        return location;
      }
    }
    throw new CalendarException("Invalid location: " + s +
            ", valid values are: " + Arrays.toString(values()));
  }

}
