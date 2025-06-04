package calendar.view;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import calendar.model.Event;

public class CalendarView implements ICalendarView {
  private final Appendable out;

  public CalendarView(Appendable out) {
    this.out = out;
  }


  public void showMenu() throws IllegalStateException {
    welcomeMessage();
    showOptions();
  }

  private void welcomeMessage() throws IllegalStateException {
    writeMessage("Welcome to the calendar program!" + System.lineSeparator());
  }

  public void farewellMessage() throws IllegalStateException {
    writeMessage("Thank you for using this program!");
  }

  public void showCalendarEvents(List<Event> events, LocalDate date) {
    writeMessage("Printing events on " + date.toString() + "." + System.lineSeparator());
    printEvents(events);
  }

  @Override
  public void showCalendarEventsInDateRange(LocalDateTime start, LocalDateTime end,
                                            List<Event> events) {
    writeMessage("Printing events from " + start + " to " + end + "." + System.lineSeparator());
    printEvents(events);
  }

  private void printEvents(List<Event> events) {
    if (events.isEmpty()) {
      writeMessage("No events found" + System.lineSeparator());
    }
    for (Event event : events) {
      if (event.getLocation() == null) { // check for valid location in event
        writeMessage("Event '" + event.getSubject() + "' on " + event.getStart().toLocalDate()
                + " from " + event.getStart().toLocalTime() + " to " +
                event.getEnd().toLocalTime() + System.lineSeparator());
      } else { // print out location as well
        writeMessage(event.getLocation() + " event '" + event.getSubject() + "' on " +
                        event.getStart().toLocalDate() + "' from " + event.getStart().toLocalTime()
                + " to " + event.getEnd().toLocalTime() + System.lineSeparator());
      }
    }
  }

  public void writeMessage(String message) throws IllegalStateException {
    try {
      out.append(message);

    } catch (IOException e) {
      throw new IllegalStateException(e.getMessage());
    }
  }

  private void showOptions() {
    createOptions();
    editOptions();
    printOptions();
    writeMessage("show status on <dateStringTtimeString>" +
            "(Prints busy status if the user has events scheduled on a given day and time, " +
            "otherwise, available)." + System.lineSeparator());
    writeMessage("menu (Print supported instruction list)" + System.lineSeparator());
    writeMessage("q or quit (quit the program) " + System.lineSeparator());
  }

  private void createOptions() {
    writeMessage("Supported user instructions are: " + System.lineSeparator());
    writeMessage("create event <eventSubject> from <dateStringTtimeString> to " +
            "<dateStringTtimeString> (Create a singular event)"
            + System.lineSeparator());
    writeMessage("create event <eventSubject> from <dateStringTtimeString> to " +
            "<dateStringTtimeString> repeats <weekdays> for <N> times " +
            "(Creates an event series that repeats N times on specific weekdays)"
            + System.lineSeparator());
    writeMessage("create event <eventSubject> from <dateStringTtimeString> to " +
            "<dateStringTtimeString> repeats <weekdays> until <dateString> " +
            "(Creates an event series until a specific date (inclusive))"
            + System.lineSeparator());
    writeMessage("create event <eventSubject> on <dateString> " +
            "(Creates a single all day event.)"
            + System.lineSeparator());
    writeMessage("create event <eventSubject> on <dateString> repeats <weekdays> for <N> times" +
            "(Creates a series of all day events that repeats N times on specific weekdays)"
            + System.lineSeparator());
    writeMessage("create event <eventSubject> on <dateString> repeats <weekdays> until " +
            "<dateString>" + "(Creates a series of all day events until a specific date " +
            "(inclusive)."
            + System.lineSeparator());
  }

  private void editOptions() {
    writeMessage("edit event <property> <eventSubject> from <dateStringTtimeString> to " +
            "<dateStringTtimeString> with <NewPropertyValue>" +
            "(Changes the property of the given event)." + System.lineSeparator());
    writeMessage("edit events <property> <eventSubject> from <dateStringTtimeString> " +
            "with <NewPropertyValue>" +
            "(Identify the event that has the given subject and starts at the given date " +
            "and time and edit its property. If this event is part of a series then the " +
            "properties of all events in that series that start at or after the given date " +
            "and time is changed)." + System.lineSeparator());
    writeMessage("edit series <property> <eventSubject> from <dateStringTtimeString> " +
            "with <NewPropertyValue>" +
            "(Identify the event that has the given subject and starts at the given date and " +
            "time and edit its property. If this event is part of a series then the properties " +
            "of all events in that series is changed)." + System.lineSeparator());
  }

  private void printOptions() {
    writeMessage("print events on <dateString>" +
            "(Prints a bulleted list of all events on that day along with their start and " +
            "end time and location (if any))." + System.lineSeparator());
    writeMessage("print events from <dateStringTtimeString> to <dateStringTtimeString>" +
            "(Prints a bulleted list of all events in the given interval including their start " +
            "and end times and location (if any))." + System.lineSeparator());
  }

}
