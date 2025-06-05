package calendar.controller.commands;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

import calendar.CalendarException;
import calendar.model.Event;
import calendar.model.ICalendar;
import calendar.view.ICalendarView;

/**
 * Represents the "print" command for the calendar application.
 * This command handles displaying events, either for a specific date or within a date range.
 */
public class Print extends AbstractCommand {

  /**
   * Constructs a {@code Print} command.
   *
   * @param sc   The {@link Scanner} from which command arguments are read.
   * @param view The {@link ICalendarView} used for displaying messages to the user.
   */
  public Print(Scanner sc, ICalendarView view) {
    super(sc, view);
  }

  /**
   * Executes the "print" command to display events.
   *
   * @param calendar The {@link ICalendar} model on which the command will operate.
   * @throws CalendarException if an error occurs during command execution.
   */
  @Override
  public void go(ICalendar calendar) throws CalendarException {
    handlePrint(calendar);
  }

  /**
   * Handles the overall process of printing events based on user input.
   * It determines whether to print events for a specific date ("on")
   * or within a date range ("from...to").
   *
   * @param model The {@link ICalendar} model to retrieve events from.
   * @throws CalendarException if there are missing or invalid inputs.
   */
  private void handlePrint(ICalendar model) throws CalendarException {
    if (!sc.hasNext()) {
      throw new IllegalArgumentException("Missing input after 'print'.");
    }
    // check that input entered after 'print' is 'events'
    if (!sc.next().equalsIgnoreCase("events")) {
      throw new IllegalArgumentException("Expected 'events' after 'print'.");
    } else {
      // input is valid, check for next keyword after events
      if (!sc.hasNext()) {
        throw new IllegalArgumentException("Missing input after 'events'.");
      }
      String nextKeyword = sc.next();
      // check if next word is 'on', print events only on that day if so
      if (nextKeyword.equalsIgnoreCase("on")) {
        // check for valid input after 'on'
        // attempt to parse date input
        LocalDate date = parseDate(sc);
        List<Event> events = model.getEventsList(date);
        view.showCalendarEvents(events, date);
        // check if next word is 'from'
      } else if (nextKeyword.equalsIgnoreCase("from")) {
        if (!sc.hasNext()) {
          throw new IllegalArgumentException("Missing input after 'from'.");
        }
        // attempt to parse start date input
        LocalDateTime fromDate = parseDateTime(sc);
        if (!sc.hasNext()) {
          throw new IllegalArgumentException(
                  "Missing input after <dateStringTTimeString>.");
        }
        if (!sc.next().equalsIgnoreCase("to")) {
          throw new IllegalArgumentException(
                  "Expected 'to' after <dateStringTTimeString>.");
        }
        // attempt to parse end date input
        LocalDateTime toDate = parseDateTime(sc);
        List<Event> events = model.getEventsListInDateRange(fromDate, toDate);
        view.showCalendarEventsInDateRange(fromDate, toDate, events);
      } else {
        throw new IllegalArgumentException("Expected 'on' or 'from' after 'events'.");
      }
    }
  }
}