package calendar.controller.commands;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

import calendar.CalendarException;
import calendar.controller.AbstractCommand;
import calendar.model.Event;
import calendar.model.ICalendar;
import calendar.view.ICalendarView;

public class Print extends AbstractCommand {

  public Print(Scanner sc, ICalendarView view) {
    super(sc, view);
  }

  @Override
  public void go(ICalendar calendar) throws CalendarException {
    handlePrint(calendar);
  }

  private void handlePrint(ICalendar model) throws CalendarException {
    if (!sc.hasNext()) throw new IllegalArgumentException("Missing input after 'print'.");
    // check that input entered after 'print' is 'events'
    if (!sc.next().equalsIgnoreCase("events")) {
      throw new IllegalArgumentException("Expected 'events' after 'print'.");
    } else {
      // input is valid, check for next keyword after events
      if (!sc.hasNext()) throw new IllegalArgumentException("Missing input after 'events'.");
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
        if (!sc.hasNext()) throw new IllegalArgumentException("Missing input after 'from'.");
        // attempt to parse start date input
        LocalDateTime fromDate = parseDateTime(sc);
        if (!sc.hasNext()) throw new IllegalArgumentException(
                "Missing input after <dateStringTTimeString>.");
        if (!sc.next().equalsIgnoreCase("to")) throw new IllegalArgumentException(
                "Expected 'to' after <dateStringTTimeString>.");
        // attempt to parse end date input
        LocalDateTime toDate = parseDateTime(sc);
        List<Event> events = model.getEventsListInDateRange(fromDate, toDate);
        view.showCalendarEventsInDateRange(fromDate, toDate, events);
      }
    }
  }
}
