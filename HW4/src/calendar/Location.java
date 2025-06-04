package calendar;

import java.util.Arrays;

public enum Location {
  PHYSICAL("physical"), ONLINE("online");

  private final String value;

  Location(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static Location fromStr(String s) {
    for (Location location : values()) {
      if (location.value.equalsIgnoreCase(s)) {
        return location;
      }
    }
    throw new IllegalArgumentException("Invalid location: " + s +
            ", valid values are: " + Arrays.toString(values()));
  }

}
