package calendar.controller.commands;

import java.util.Scanner;

import calendar.CalendarException;
import calendar.model.CalendarProperty;
import calendar.model.ICalendar;
import calendar.model.ICalendarSystem;
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
    handleEdit(system);
  }

  /**
   * Handles the overall process of editing a calendar based on user input.
   * @param system the {@Link ICalendarSystem} model containing the calendar to be edited.
   * @throws CalendarException if the input keyword after "edit" is invalid.
   */
  private void handleEdit(ICalendarSystem system) throws CalendarException {
    if (!sc.hasNext() || !sc.next().equalsIgnoreCase("--name")) {
      throw new CalendarException("Expected '--name' after 'Calendar'.");
    }
    String calName = checkName(sc, "--property");
    // check that there is a valid calendar name provided
    if (calName.isEmpty()) {
      throw new CalendarException("Missing calendar name.");
    }
    CalendarProperty property = checkValidProperty(sc);
    if (!sc.hasNext()) {
      throw new CalendarException("Missing new property value.");
    }
    String newProperty = sc.next();
    system.editCalendar(calName, property, newProperty);
    view.writeMessage("Edited calendar '" + calName + "' property '" + property + "' to '"
            + newProperty + "'." + System.lineSeparator());
  }

  /**
   * Validates and parses the {@link CalendarProperty} to be edited from the scanner.
   *
   * @param sc The {@link Scanner} to read the property string.
   * @return The parsed {@link CalendarProperty} enum.
   * @throws CalendarException if the property string is missing or invalid.
   */
  private CalendarProperty checkValidProperty(Scanner sc) throws CalendarException {
    // check that user inputted a calendar property
    if (!sc.hasNext()) {
      throw new CalendarException("Missing event property.");
    }
    // attempt to store property, will result in error if invalid property
    return CalendarProperty.fromStr(sc.next());
  }
}