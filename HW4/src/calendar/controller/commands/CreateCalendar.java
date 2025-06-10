package calendar.controller.commands;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.zone.ZoneRulesException;
import java.util.Scanner;

import calendar.CalendarException;
import calendar.model.ICalendar;
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
    String calName = checkValidCalendarName(sc);
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
    } catch(DateTimeException e) {
      throw new CalendarException("Invalid timezone format. Must be in 'Area/Location' format.");
    }
  }

  private String checkValidCalendarName(Scanner sc) {
    StringBuilder subjectBuilder = new StringBuilder();
    while (sc.hasNext()) {
      String token = sc.next();
      // check if subject only contains 1 word
      if (token.equalsIgnoreCase("--timezone")) {
        break; // keyword found, subject is complete
      }
      // if subject already contains word, add a space
      if (subjectBuilder.length() > 0) {
        subjectBuilder.append(" ");
      }
      // append next word to subject
      subjectBuilder.append(token);
    }
    return subjectBuilder.toString();
  }



}
