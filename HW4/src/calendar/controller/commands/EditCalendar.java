package calendar.controller.commands;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import calendar.CalendarException;
import calendar.model.Event;
import calendar.model.ICalendar;
import calendar.model.ICalendarSystem;
import calendar.model.Property;
import calendar.view.ICalendarView;

/**
 * Represents the "edit calendar" command for the calendar application.
 * This command handles editing properties of a calendar.
 */
public class EditCalendar extends AbstractCommand {

  /**
   * Constructs an {@code EditEvent} command.
   *
   * @param sc   The {@link Scanner} from which command arguments are read.
   * @param view The {@link ICalendarView} used for displaying messages to the user.
   */
  public EditCalendar(Scanner sc, ICalendarView view) {
    super(sc, view);
  }

  /**
   * Executes the "edit" command to modify event properties.
   *
   * @param system The {@link ICalendarSystem} model on which the command will operate.
   * @throws CalendarException if an error occurs during command execution.
   */
  @Override
  public void execute(ICalendarSystem system) throws CalendarException {
    ICalendar calendar = system.getCurrentCalendar();
    handleEdit(calendar);
  }

  /**
   * Handles the overall process of editing a calendar based on user input.
   *
   * @param model The {@link ICalendar} model to perform the edit operation on.
   * @throws CalendarException if the input keyword after "edit" is invalid.
   */
  private void handleEdit(ICalendar model) throws CalendarException {
  }
}