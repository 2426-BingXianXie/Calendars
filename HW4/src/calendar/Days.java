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
}
