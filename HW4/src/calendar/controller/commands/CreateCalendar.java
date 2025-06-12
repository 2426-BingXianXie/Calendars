package calendar.controller.commands;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.Scanner;

import calendar.CalendarException;
import calendar.model.ICalendarSystem;
import calendar.view.ICalendarView;

/**
 * Represents the "create calendar" command for the calendar application.
 * This command handles the creation of a calendar.
 * It parses the event details from the scanner input.
 */
public class CreateCalendar extends AbstractCommand {
  /**
   * Constructs a {@code Create} command.
   *
   * @param sc   The {@link Scanner} from which command arguments are read.
   * @param view The {@link ICalendarView} used for displaying messages to the user.
   */
  public CreateCalendar(Scanner sc, ICalendarView view) {
    super(sc, view);
  }

  /**
   * Executes the "create" command to create a calendar.
   *
   * @param system The {@link ICalendarSystem} model on which the command will operate.
   * @throws CalendarException if an error occurs during command execution.
   */
  @Override
  public void execute(ICalendarSystem system) throws CalendarException {
    handleCreate(system);
  }

  /**
   * Handles the overall process of creating a calendar on user input.
   * It parses the calendar name then delegates to appropriate helper methods.
   *
   * @throws CalendarException if there are missing or invalid inputs during parsing.
   */
  private void handleCreate(ICalendarSystem system) throws CalendarException {
    if (!sc.hasNext() || !sc.next().equalsIgnoreCase("--name")) {
      throw new CalendarException("Expected '--name' after 'calendar");
    }

    String calName = checkName(sc, "--timezone");
    // check that there is a valid calendar name provided
    if (calName.isEmpty()) {
      throw new CalendarException("Missing calendar name.");
    }
    // check that user inputted a timezone after '--timezone'
    if (!sc.hasNext()) {
      throw new CalendarException("Incomplete command, missing timezone.");
    }
    String timezoneString = sc.next();
    try { // attempt to parse timezoneString, throw error if invalid format
      ZoneId timezone = ZoneId.of(timezoneString);
      system.createCalendar(calName, timezone);
      view.writeMessage("Calendar '" + calName + "' in timezone '" + timezone + "' created."
              + System.lineSeparator());
    } catch (DateTimeException e) {
      throw new CalendarException("Invalid timezone format. Must be in 'Area/Location' format.");
    }
  }
}
