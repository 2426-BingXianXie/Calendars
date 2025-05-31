package calendar;

public enum Days {
  MONDAY('M'),
  TUESDAY('T'),
  WEDNESDAY('W'),
  THURSDAY('R'),
  FRIDAY('F'),
  SATURDAY('S'),
  SUNDAY('U');

  private final char symbol;

  Days(char symbol) {
    this.symbol = symbol;
  }

  /**
   * Returns the symbol representing the day of the week.
   * @return the symbol for the day
   */
  public char getSymbol() {
    return symbol;
  }

  /**
   * Returns the day of the week corresponding to the given symbol.
   * @param c the character representing the day
   * @return the corresponding Days enum value
   * @throws IllegalArgumentException if the character does not match any day
   */
  public static Days fromSymbol(char c) {
    for (Days day : values()) {
      if (day.symbol == c) return day;
    }
    throw new IllegalArgumentException("Invalid symbol: " + c);
  }
}
