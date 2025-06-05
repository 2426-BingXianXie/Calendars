package calendar.controller.commands;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

import calendar.CalendarException;
import calendar.controller.CalendarCommand;
import calendar.model.ICalendar;
import calendar.view.ICalendarView;

public abstract class AbstractCommand implements CalendarCommand {

  protected final Scanner sc;
  protected final ICalendarView view;

  protected AbstractCommand(Scanner sc, ICalendarView view) {
    this.sc = sc;
    this.view = view;
  }

  @Override
  public abstract void go(ICalendar calendar) throws CalendarException;

  protected LocalDate parseDate(Scanner sc) throws CalendarException {
    // check that there is a valid date input
    if (!sc.hasNext()) throw new CalendarException("Missing <dateString> after 'on'.");
    String dateString = sc.next();
    try { // check for valid date format
      return LocalDate.parse(dateString);
    } catch (DateTimeParseException e) {
      throw new CalendarException("Invalid date format for <dateString>. " +
              "Expected YYYY-MM-DD");
    }
  }

  protected LocalDateTime parseDateTime(Scanner sc) throws CalendarException {
    // check that there is a valid date input
    if (!sc.hasNext()) throw new CalendarException("Missing <dateStringTtimeString>");
    String dateString = sc.next();
    try { // check for valid starting date format
      return LocalDateTime.parse(dateString);
    } catch (DateTimeParseException e) {
      throw new CalendarException("Invalid date format for <dateStringTtimeString>. " +
              "Expected YYYY-MM-DDThh:mm");
    }
  }
}
