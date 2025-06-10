package calendar.model;

import java.util.Arrays;

import calendar.CalendarException;

/**
 * Represents the properties that define a calendar.
 */
public enum CalendarProperty {
  NAME("name"),
  TIMEZONE("timezone");

  private final String str;

  /**
   * Constructs a CalendarProperty with the specified string representation.
   *
   * @param str the string representation of the calendar property
   */
  CalendarProperty(String str) {
    this.str = str;
  }

  /**
   * Returns the string representation of the calendar property.
   *
   * @return the string value of the calendar property
   */
  public String getStr() {
    return str;
  }

  /**
   * Converts a string to the corresponding CalendarProperty.
   *
   * @param s the string representation of the property
   * @return the CalendarProperty corresponding to the string
   * @throws CalendarException if the string does not match any valid property
   */
  public static CalendarProperty fromStr(String s) throws CalendarException {
    for (CalendarProperty property : values()) {
      if (property.str.equalsIgnoreCase(s)) {
        return property;
      }
    }
    throw new CalendarException("Invalid property: " + s +
            ", valid values are: " + Arrays.toString(values()));
  }

}


