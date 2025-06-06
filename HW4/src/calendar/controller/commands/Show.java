package calendar.controller.commands;

import java.time.LocalDateTime;
import java.util.Scanner;

import calendar.CalendarException;
import calendar.model.ICalendar;
import calendar.view.ICalendarView;

public class Show extends AbstractCommand {

  public Show(Scanner sc, ICalendarView view) {
    super(sc, view);
  }

  @Override
  public void go(ICalendar calendar) throws CalendarException {
    handleShow(calendar);
  }

  private void handleShow(ICalendar model) throws CalendarException {
    if (!sc.hasNext()) throw new CalendarException("Missing input after 'show'.");
    // check that input after 'show' is 'status'
    if (!sc.next().equalsIgnoreCase("status")) {
      throw new CalendarException("Expected 'status' after 'show'.");
    }
    if (!sc.hasNext()) throw new CalendarException("Missing input after 'status'.");
    // check that input after 'status' is 'on'
    if (!sc.next().equalsIgnoreCase("on")) {
      throw new CalendarException("Expected 'on' after 'status'.");
    }
    // attempt to parse date string
    LocalDateTime date = parseDateTime(sc);
    boolean isBusy = model.isBusyAt(date); // check if user already has event on this dateTime
    if (isBusy) {
      view.writeMessage("User is busy, already has an event scheduled on " + date + "."
              + System.lineSeparator());
    } else {
      view.writeMessage("User is available on " + date + "."
              + System.lineSeparator());
    }
  }
}
