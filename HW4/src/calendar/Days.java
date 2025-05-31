package calendar;

import java.time.DayOfWeek;
import java.util.EnumSet;
import java.util.Set;

public enum Days {
  MONDAY('M', DayOfWeek.MONDAY),
  TUESDAY('T', DayOfWeek.TUESDAY),
  WEDNESDAY('W', DayOfWeek.WEDNESDAY),
  THURSDAY('R', DayOfWeek.THURSDAY),
  FRIDAY('F', DayOfWeek.FRIDAY),
  SATURDAY('S', DayOfWeek.SATURDAY),
  SUNDAY('U', DayOfWeek.SUNDAY);

  private final char symbol;
  private final DayOfWeek dayOfWeek;

  Days(char symbol, DayOfWeek dayOfWeek) {
    this.symbol = symbol;
    this.dayOfWeek = dayOfWeek;
  }

  /**
   * Returns the symbol representing this day of the week.
   *
   * @return the character symbol for the day
   */
  public char getSymbol() {
    return symbol;
  }

  /**
   * Converts this enum to its corresponding DayOfWeek.
   *
   * @return the DayOfWeek representation of this day
   */
  public DayOfWeek toDayOfWeek() {
    return dayOfWeek;
  }

  /**
   * Returns the Days enum corresponding to the given character symbol.
   *
   * @param c the character symbol for the day
   * @return the Days enum for the specified symbol
   * @throws IllegalArgumentException if the character does not match any day
   */
  public static Days fromSymbol(char c) {
    for (Days day : values()) {
      if (day.symbol == c) {
        return day;
      }
    }
    throw new IllegalArgumentException("Invalid weekday symbol: " + c);
  }

  /**
   * Parses a string of weekday characters into a set of DayOfWeek enums.
   *
   * @param weekdayChars a string containing characters representing weekdays
   * @return a set of DayOfWeek enums corresponding to the characters
   */
  public static Set<DayOfWeek> parseWeekdays(String weekdayChars) {
    Set<DayOfWeek> days = EnumSet.noneOf(DayOfWeek.class);
    for (char c : weekdayChars.toCharArray()) {
      days.add(fromSymbol(c).toDayOfWeek());
    }
    return days;
  }
}