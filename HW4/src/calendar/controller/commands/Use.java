package calendar.controller.commands;

import java.util.Scanner;

import calendar.CalendarException;
import calendar.model.ICalendarSystem;
import calendar.view.ICalendarView;

/**
 * Represents the "use" command for the calendar application.
 * This command puts a specific calendar in a calendar system in use for event commands.
 */
public class Use extends AbstractCommand {

  /**
   * Constructs a {@code Use} command.
   *
   * @param sc   The {@link Scanner} from which command arguments are read.
   * @param view The {@link ICalendarView} used for displaying messages to the user.
   */
  public Use(Scanner sc, ICalendarView view) {
    super(sc, view);
  }

  /**
   * Executes the "use" command to set a specific calendar in a calendar system to be used.
   *
   * @param system The {@link ICalendarSystem} model on which the command will operate.
   * @throws CalendarException if an error occurs during command execution.
   */
  @Override
  public void execute(ICalendarSystem system) throws CalendarException {
    if (!sc.hasNext()) {
      throw new CalendarException("Missing input after 'use'. Expected 'calendar'.");
    }
    // check that input entered after 'use' is 'calendar'
    if (!sc.next().equalsIgnoreCase("calendar")) {
      throw new CalendarException("Expected 'calendar' after 'use'.");
    } else {
      checkCalendarName();
    }
    if (!sc.hasNext()) {
      throw new CalendarException("Missing name of calendar");
    }
    String calendarName = sc.next();
    // set given calendar name to active. Will throw an error if calendar is not found in system
    system.useCalendar(calendarName);
    view.writeMessage("Calendar '" + calendarName + "' set to be in use."
            + System.lineSeparator());
  }

  /**
   * Checks that the input after 'calendar' is '--name'.
   *
   * @throws CalendarException if there is no input after 'calendar', or incorrect input.
   */
  private void checkCalendarName() throws CalendarException {
    // input is valid, check for next keyword after 'calendar'
    if (!sc.hasNext()) {
      throw new CalendarException("Missing input after 'calendar'.");
    }
    if (!sc.next().equalsIgnoreCase("--name")) {
      throw new CalendarException("Expected '--name' after 'calendar'");
    }
  }


}