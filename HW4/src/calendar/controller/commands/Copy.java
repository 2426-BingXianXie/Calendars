package calendar.controller.commands;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Scanner;

import calendar.CalendarException;
import calendar.model.ICalendarSystem;
import calendar.view.ICalendarView;

/**
 * Represents the "copy" command for the calendar application.
 * This command copies an event or series of events from the current calendar in use
 * to another calendar inside the calendar system.
 */
public class Copy extends AbstractCommand {

  /**
   * Constructs a {@code Use} command.
   *
   * @param sc   The {@link Scanner} from which command arguments are read.
   * @param view The {@link ICalendarView} used for displaying messages to the user.
   */
  public Copy(Scanner sc, ICalendarView view) {
    super(sc, view);
  }

  /**
   * Executes the "copy" command to copy an event or series of events from one calendar
   * to another.
   *
   * @param system The {@link ICalendarSystem} model on which the command will operate.
   * @throws CalendarException if an error occurs during command execution.
   */
  @Override
  public void execute(ICalendarSystem system) throws CalendarException {
    // check if the calendarSystem has a calendar in use
    if (system.getCurrentCalendar() == null) {
      throw new CalendarException("No calendar currently in use.");
    }
    handleCopy(system);
  }

  /**
   * Handles the process of copying an event or series of events from one calendar to another.
   *
   * @param system The {@link ICalendarSystem} model to put in use.
   * @throws CalendarException if there are missing or invalid inputs.
   */
  private void handleCopy(ICalendarSystem system) throws CalendarException {
    // check that user inputted 'event' after 'copy'
    if (!sc.hasNext()) {
      throw new CalendarException("No input after 'copy'");
    }
    String eventKeyword = sc.next();
    switch (eventKeyword) {
      case "event":
        handleOnVariants(sc, system, false);
        break;
      case "events":
        if (!sc.hasNext()) {
          throw new CalendarException("No input after 'events'");
        }
        String nextKeyword = sc.next(); // check if next word is 'on' or 'from'
        if (nextKeyword.equalsIgnoreCase("on")) {
          handleOnVariants(sc, system, true);
        } else if (nextKeyword.equalsIgnoreCase("between")) {
          handleBetweenVariants(sc, system);
        } else {
          throw new CalendarException("Expected 'on' or 'between' after events.");
        }
        break;
      default:
        throw new CalendarException("Expected 'event' or 'events' after 'copy");
    }
  }

  private void handleOnVariants(Scanner sc, ICalendarSystem system, boolean copyAll)
          throws CalendarException {
    if (!copyAll) { // user intends to copy a single event
      handleSingleEventCopy(sc, system);
    } else { // user intends to copy all events from given date
      handleMultipleEventsCopy(sc, system);
    }
  }

  private void handleSingleEventCopy(Scanner sc, ICalendarSystem system)
          throws CalendarException {
    String eventName = checkName(sc, "on");
    // check that user inputted an event name
    if (eventName.isEmpty()) {
      throw new CalendarException("Expected event name after 'event'");
    }
    LocalDateTime startDateTime = parseDateTime(sc); // attempt to parse given dateTime string
    if (!sc.hasNext() || !sc.next().equalsIgnoreCase("--target")) {
      throw new CalendarException("Expected '--target' after <dateStringTtimeString>.");
    }
    // retrieve target calendar name
    String targetCalName = checkName(sc, "to");
    if (targetCalName.isEmpty()) {
      throw new CalendarException("No given calendar name.");
    }
    // attempt to parse given target dateTime string
    LocalDateTime targetDateTime = parseDateTime(sc);
    system.copyEvent(eventName, startDateTime, targetCalName, targetDateTime);
    view.writeMessage("Copied event '" + eventName + "' to calendar '" + targetCalName + "' "
            + "on '" + targetDateTime + "'." + System.lineSeparator());
  }

  private void handleMultipleEventsCopy(Scanner sc, ICalendarSystem system)
          throws CalendarException {
    // attempt to parse given start date string
    LocalDate startDate = parseDate(sc);
    if (!sc.hasNext() || !sc.next().equalsIgnoreCase("--target")) {
      throw new CalendarException("Expected '--target' after 'events'.");
    }
    // retrieve target calendar name
    String targetCalName = checkName(sc, "to");
    if (targetCalName.isEmpty()) {
      throw new CalendarException("No given calendar name.");
    }
    // attempt to parse given target date string
    LocalDate targetDate = parseDate(sc);
    system.copyEventsOnDate(startDate, targetCalName, targetDate);
    view.writeMessage("Copied events on '" + startDate + "' to calendar '" + targetCalName
            + "' on '" + targetDate + "'." + System.lineSeparator());
  }

  private void handleBetweenVariants(Scanner sc, ICalendarSystem system)
          throws CalendarException {
    // attempt to parse given start date input
    LocalDate startDate = parseDate(sc);
    if (!sc.hasNext() || !sc.next().equalsIgnoreCase("and")) {
      throw new CalendarException("Expected 'and' after <dateString>.");
    }
    // attempt to parse given end date input
    LocalDate endDate = parseDate(sc);
    if (!sc.hasNext() || !sc.next().equalsIgnoreCase("--target")) {
      throw new CalendarException("Expected '--target' after <dateString>.");
    }
    String targetCalName = checkName(sc, "to");
    // attempt to parse given target date input
    LocalDate targetDate = parseDate(sc);
    system.copyEventsBetweenDates(startDate, endDate, targetCalName, targetDate);
    view.writeMessage("Copied events from '" + startDate + "' to '" + endDate + "'in calendar '"
            + targetCalName + "' on '" + targetDate + "'." + System.lineSeparator());
  }
}