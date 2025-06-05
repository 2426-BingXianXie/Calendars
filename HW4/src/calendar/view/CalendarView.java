package calendar.view;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import calendar.model.Event;

/**
 * Implements the {@link ICalendarView} interface, providing methods to display calendar information
 * and user interaction messages to an {@link Appendable} output stream.
 */
public class CalendarView implements ICalendarView {
  // The Appendable object to which all messages will be written (e.g., System.out, StringWriter).
  private final Appendable out;

  /**
   * Constructs a CalendarView with the specified Appendable object.
   *
   * @param out The Appendable object used for output.
   */
  public CalendarView(Appendable out) {
    this.out = out;
  }

  /**
   * Writes a given message string to the output stream.
   *
   * @param message The string message to be written.
   * @throws IllegalStateException if an {@link IOException} occurs during the write operation.
   */
  public void writeMessage(String message) throws IllegalStateException {
    try {
      out.append(message);

    } catch (IOException e) {
      // Wraps IOException in an IllegalStateException to conform to the interface.
      throw new IllegalStateException(e.getMessage());
    }
  }

  /**
   * Displays the main menu of the calendar program to the user.
   * This includes a welcome message and a list of supported instructions.
   *
   * @throws IllegalStateException if an I/O error occurs while writing to the output.
   */
  public void showMenu() throws IllegalStateException {
    welcomeMessage();
    showOptions();
  }

  /**
   * Prints a welcome message to the output.
   *
   * @throws IllegalStateException if an I/O error occurs while writing to the output.
   */
  private void welcomeMessage() throws IllegalStateException {
    writeMessage("Welcome to the calendar program!" + System.lineSeparator());
  }

  /**
   * Prints a farewell message to the output when the program is exiting.
   *
   * @throws IllegalStateException if an I/O error occurs while writing to the output.
   */
  public void farewellMessage() throws IllegalStateException {
    writeMessage("Thank you for using this program!");
  }

  /**
   * Displays a list of calendar events for a specific date.
   *
   * @param events The list of {@link Event} objects to display.
   * @param date   The {@link LocalDate} for which the events are being displayed.
   * @throws IllegalStateException if an I/O error occurs while writing to the output.
   */
  public void showCalendarEvents(List<Event> events, LocalDate date) throws IllegalStateException {
    writeMessage("Printing events on " + date.toString() + "." + System.lineSeparator());
    printEvents(events);
  }

  /**
   * Displays a list of calendar events that fall within a specified date and time range.
   *
   * @param start  The {@link LocalDateTime} representing the start of the range (inclusive).
   * @param end    The {@link LocalDateTime} representing the end of the range (inclusive).
   * @param events The list of {@link Event} objects to display.
   * @throws IllegalStateException if an I/O error occurs while writing to the output.
   */
  @Override
  public void showCalendarEventsInDateRange(LocalDateTime start, LocalDateTime end,
                                            List<Event> events) throws IllegalStateException {
    writeMessage("Printing events from " + start + " to " + end + "." + System.lineSeparator());
    printEvents(events);
  }

  /**
   * Helper method to iterate through a list of events and print their details.
   * If the list is empty, it prints a "No events found" message.
   *
   * @param events The list of {@link Event} objects to print.
   * @throws IllegalStateException if an I/O error occurs while writing to the output.
   */
  private void printEvents(List<Event> events) throws IllegalStateException {
    if (events.isEmpty()) {
      writeMessage("No events found" + System.lineSeparator());
    }
    // Iterate through each event in the list.
    for (Event event : events) {
      // Check if the event has a specific location.
      if (event.getLocation() == null) { // check for valid location in event
        // If no location, print event subject, date, and time range.
        writeMessage("Event '" + event.getSubject() + "' on " + event.getStart().toLocalDate()
                + " from " + event.getStart().toLocalTime() + " to " +
                event.getEnd().toLocalTime() + System.lineSeparator());
      } else { // print out location as well
        // If location exists, print location type, subject, date, and time range.
        writeMessage(event.getLocation() + " event '" + event.getSubject() + "' on " +
                event.getStart().toLocalDate() + "' from " + event.getStart().toLocalTime()
                + " to " + event.getEnd().toLocalTime() + System.lineSeparator());
      }
    }
  }

  /**
   * Displays the various command options available to the user, categorized by function.
   *
   * @throws IllegalStateException if an I/O error occurs while writing to the output.
   */
  private void showOptions() throws IllegalStateException {
    createOptions();
    editOptions();
    printOptions();
    // Displays the "show status" command option.
    writeMessage("show status on <dateStringTtimeString>" +
            "(Prints busy status if the user has events scheduled on a given day and time, " +
            "otherwise, available)." + System.lineSeparator());
    // Displays the "menu" command option.
    writeMessage("menu (Print supported instruction list)" + System.lineSeparator());
    // Displays the "quit" command option.
    writeMessage("q or quit (quit the program) " + System.lineSeparator());
  }

  /**
   * Displays the command options related to creating new events.
   *
   * @throws IllegalStateException if an I/O error occurs while writing to the output.
   */
  private void createOptions() throws IllegalStateException {
    writeMessage("Supported user instructions are: " + System.lineSeparator());
    // Option to create a singular event.
    writeMessage("create event <eventSubject> from <dateStringTtimeString> to " +
            "<dateStringTtimeString> (Create a singular event)"
            + System.lineSeparator());
    // Option to create an event series repeating N times.
    writeMessage("create event <eventSubject> from <dateStringTtimeString> to " +
            "<dateStringTtimeString> repeats <weekdays> for <N> times " +
            "(Creates an event series that repeats N times on specific weekdays)"
            + System.lineSeparator());
    // Option to create an event series repeating until a specific date.
    writeMessage("create event <eventSubject> from <dateStringTtimeString> to " +
            "<dateStringTtimeString> repeats <weekdays> until <dateString> " +
            "(Creates an event series until a specific date (inclusive))"
            + System.lineSeparator());
    // Option to create a single all-day event.
    writeMessage("create event <eventSubject> on <dateString> " +
            "(Creates a single all day event.)"
            + System.lineSeparator());
    // Option to create an all-day event series repeating N times.
    writeMessage("create event <eventSubject> on <dateString> repeats <weekdays> for <N> times" +
            "(Creates a series of all day events that repeats N times on specific weekdays)"
            + System.lineSeparator());
    // Option to create an all-day event series repeating until a specific date.
    writeMessage("create event <eventSubject> on <dateString> repeats <weekdays> until " +
            "<dateString>" + "(Creates a series of all day events until a specific date " +
            "(inclusive)."
            + System.lineSeparator());
  }

  /**
   * Displays the command options related to editing existing events or event series.
   *
   * @throws IllegalStateException if an I/O error occurs while writing to the output.
   */
  private void editOptions() throws IllegalStateException {
    // Option to edit a property of a singular event.
    writeMessage("edit event <property> <eventSubject> from <dateStringTtimeString> to " +
            "<dateStringTtimeString> with <NewPropertyValue>" +
            "(Changes the property of the given event)." + System.lineSeparator());
    // Option to edit properties of an event and subsequent events in its series from a given date.
    writeMessage("edit events <property> <eventSubject> from <dateStringTtimeString> " +
            "with <NewPropertyValue>" +
            "(Identify the event that has the given subject and starts at the given date " +
            "and time and edit its property. If this event is part of a series then the " +
            "properties of all events in that series that start at or after the given date " +
            "and time is changed)." + System.lineSeparator());
    // Option to edit properties of all events within an entire series.
    writeMessage("edit series <property> <eventSubject> from <dateStringTtimeString> " +
            "with <NewPropertyValue>" +
            "(Identify the event that has the given subject and starts at the given date and " +
            "time and edit its property. If this event is part of a series then the properties " +
            "of all events in that series is changed)." + System.lineSeparator());
  }

  /**
   * Displays the command options related to printing calendar events.
   *
   * @throws IllegalStateException if an I/O error occurs while writing to the output.
   */
  private void printOptions() throws IllegalStateException {
    // Option to print events on a specific date.
    writeMessage("print events on <dateString>" +
            "(Prints a bulleted list of all events on that day along with their start and " +
            "end time and location (if any))." + System.lineSeparator());
    // Option to print events within a specific date and time interval.
    writeMessage("print events from <dateStringTtimeString> to <dateStringTtimeString>" +
            "(Prints a bulleted list of all events in the given interval including their start " +
            "and end times and location (if any))." + System.lineSeparator());
  }
}