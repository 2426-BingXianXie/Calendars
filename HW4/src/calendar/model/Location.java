package calendar.model;

import java.util.Arrays;

import calendar.CalendarException;

/**
 * Represents the status of an event in a calendar.
 * The status can be either PUBLIC or PRIVATE.
 */
public enum Location {
  PHYSICAL("physical"), ONLINE("online");

  private final String value;

  /**
   * Constructs a Location with the specified value.
   *
   * @param value the string representation of the location
   */
  Location(String value) {
    this.value = value;
  }

  /**
   * Returns the string representation of the location.
   *
   * @return the string value of the location
   */
  public String getValue() {
    return value;
  }

  /**
   * Converts a string to the corresponding Location.
   *
   * @param s the string representation of the location
   * @return the Location corresponding to the string
   * @throws CalendarException if the string does not match any valid location
   */
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
