package calendar.controller.commands;

import java.time.LocalDateTime;
import java.util.Scanner;

import calendar.CalendarException;
import calendar.model.ICalendar;
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
    ICalendar calendar = system.getCurrentCalendar();
    handleUse(calendar);
  }

  /**
   * Handles the process of activating a calendar in a calendar system to be in use,
   * allowing event commands to be acted on the calendar.
   *
   * @param model The {@link ICalendar} model to put in use.
   * @throws CalendarException if there are missing or invalid inputs.
   */
  private void handleUse(ICalendar model) throws CalendarException {
  }
}