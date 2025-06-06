package calendar.controller.commands;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

import calendar.CalendarException;
import calendar.controller.CalendarCommand;
import calendar.model.ICalendar;
import calendar.view.ICalendarView;

/**
 * Provides a base abstract class for calendar commands.
 * This class handles common functionalities such as parsing dates and date-times
 * from a scanner, and provides protected fields for the scanner and view.
 */
public abstract class AbstractCommand implements CalendarCommand {

  protected final Scanner sc;
  protected final ICalendarView view;

  /**
   * Constructs an {@code AbstractCommand} with the given scanner and view.
   *
   * @param sc   The {@link Scanner} from which command arguments are read.
   * @param view The {@link ICalendarView} used for displaying messages to the user.
   */
  protected AbstractCommand(Scanner sc, ICalendarView view) {
    this.sc = sc;
    this.view = view;
  }

  /**
   * Executes the command on the provided calendar model.
   *
   * @param calendar The {@link ICalendar} model on which the command will operate.
   * @throws CalendarException if an error occurs during command execution.
   */
  @Override
  public abstract void execute(ICalendar calendar) throws CalendarException;

  /**
   * Parses a date string from the scanner.
   *
   * @param sc The {@link Scanner} containing the date string.
   * @return A {@link LocalDate} object parsed from the input.
   * @throws CalendarException if the date string is missing or in an invalid format.
   */
  protected LocalDate parseDate(Scanner sc) throws CalendarException {
    // check that there is a valid date input
    if (!sc.hasNext()) throw new CalendarException("Missing <dateString>.");
    String dateString = sc.next();
    try { // check for valid date format
      return LocalDate.parse(dateString);
    } catch (DateTimeParseException e) {
      throw new CalendarException("Invalid date format for <dateString>. " +
              "Expected YYYY-MM-DD");
    }
  }

  /**
   * Parses a date-time string from the scanner.
   *
   * @param sc The {@link Scanner} containing the date-time string.
   * @return A {@link LocalDateTime} object parsed from the input.
   * @throws CalendarException if the date-time string is missing or in an invalid format.
   */
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