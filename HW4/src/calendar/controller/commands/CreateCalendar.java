package calendar.controller.commands;

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
   * Executes the "create" command to create a calendar..
   *
   * @param system The {@link ICalendarSystem} model on which the command will operate.
   * @throws CalendarException if an error occurs during command execution.
   */
  @Override
  public void execute(ICalendarSystem system) throws CalendarException {
    ICalendar calendar = system.getCurrentCalendar();
    handleCreate(calendar);
  }

  /**
   * Handles the overall process of creating a calendar on user input.
   * It parses the calendar name then delegates to appropriate helper methods.
   *
   * @param model The {@link ICalendar} model to create the event(s) in.
   * @throws CalendarException if there are missing or invalid inputs during parsing.
   */
  private void handleCreate(ICalendar model) throws CalendarException {

  }



}
