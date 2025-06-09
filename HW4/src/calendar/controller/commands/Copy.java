package calendar.controller.commands;

import java.time.LocalDateTime;
import java.util.Scanner;

import calendar.CalendarException;
import calendar.model.ICalendar;
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
    ICalendar calendar = system.getCurrentCalendar();
    handleCopy(calendar);
  }

  /**
   * Handles the process of copying an event or series of events from one calendar to another.
   *
   * @param model The {@link ICalendar} model to put in use.
   * @throws CalendarException if there are missing or invalid inputs.
   */
  private void handleCopy(ICalendar model) throws CalendarException {
  }
}