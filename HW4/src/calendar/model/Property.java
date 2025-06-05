package calendar.model;

import java.util.Arrays;

import calendar.CalendarException;

/**
 * Represents the status of an event in a calendar.
 * The status can be either PUBLIC or PRIVATE.
 */
public enum Property {
  SUBJECT("subject"),
  START("start"),
  END("end"),
  DESCRIPTION("description"),
  LOCATION("location"),
  STATUS("status");

  private final String str;

  /**
   * Constructs a Property with the specified string representation.
   *
   * @param str the string representation of the property
   */
  Property(String str) {
    this.str = str;
  }

  /**
   * Returns the string representation of the property.
   *
   * @return the string value of the property
   */
  public String getStr() {
    return str;
  }

  /**
   * Converts a string to the corresponding Property.
   *
   * @param s the string representation of the property
   * @return the Property corresponding to the string
   * @throws CalendarException if the string does not match any valid property
   */
  public static Property fromStr(String s) throws CalendarException {
    for (Property property : values()) {
      if (property.str.equalsIgnoreCase(s)) {
        return property;
      }
    }
    throw new CalendarException("Invalid property: " + s +
            ", valid values are: " + Arrays.toString(values()));
  }

}


