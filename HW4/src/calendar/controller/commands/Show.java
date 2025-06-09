package calendar.controller.commands;

import java.time.LocalDateTime;
import java.util.Scanner;

import calendar.CalendarException;
import calendar.model.ICalendar;
import calendar.model.ICalendarSystem;
import calendar.view.ICalendarView;

/**
 * Represents the "show" command for the calendar application.
 * This command checks and displays the user's availability status at a specific date and time.
 */
public class Show extends AbstractCommand {

  /**
   * Constructs a {@code Show} command.
   *
   * @param sc   The {@link Scanner} from which command arguments are read.
   * @param view The {@link ICalendarView} used for displaying messages to the user.
   */
  public Show(Scanner sc, ICalendarView view) {
    super(sc, view);
  }

  /**
   * Executes the "show" command to display the user's availability status.
   *
   * @param system The {@link ICalendarSystem} model on which the command will operate.
   * @throws CalendarException if an error occurs during command execution.
   */
  @Override
  public void execute(ICalendarSystem system) throws CalendarException {
    ICalendar calendar = system.getCurrentCalendar();
    handleShow(calendar);
  }

  /**
   * Handles the process of showing the user's availability status.
   * It parses the date-time from the input and queries the model
   * to determine if the user is busy or available at that time.
   *
   * @param model The {@link ICalendar} model to query for availability.
   * @throws CalendarException if there are missing or invalid inputs.
   */
  private void handleShow(ICalendar model) throws CalendarException {
    if (!sc.hasNext()) {
      throw new CalendarException("Missing input after 'show'.");
    }
    // check that input after 'show' is 'status'
    if (!sc.next().equalsIgnoreCase("status")) {
      throw new CalendarException("Expected 'status' after 'show'.");
    }
    if (!sc.hasNext()) {
      throw new CalendarException("Missing input after 'status'.");
    }

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