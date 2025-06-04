package calendar;

import java.util.Arrays;

public enum Property {
  SUBJECT("subject"),
  START("start"),
  END("end"),
  DESCRIPTION("description"),
  LOCATION("location"),
  STATUS("status");

  private final String str;

  Property(String str) {
    this.str = str;
  }

  public String getStr() {
    return str;
  }

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


