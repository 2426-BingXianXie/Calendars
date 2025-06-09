package calendar.controller;

import java.util.Scanner;

import calendar.CalendarException;
import calendar.controller.commands.Copy;
import calendar.controller.commands.CreateCalendar;
import calendar.controller.commands.CreateEvent;
import calendar.controller.commands.EditCalendar;
import calendar.controller.commands.EditEvent;
import calendar.controller.commands.Print;
import calendar.controller.commands.Show;
import calendar.controller.commands.Use;
import calendar.model.ICalendarSystem;
import calendar.view.ICalendarView;


/**
 * Controller class that manages user interactions with the calendar application.
 * Handles input processing, command execution, and view updates for the calendar system.
 * This controller supports both interactive and file-based command processing.
 */
public class CalendarController implements ICalendarController {
  private final Readable in;
  private final ICalendarView view;
  private final ICalendarSystem model;

  /**
   * Constructs a new CalendarController with the specified model, view, and input source.
   *
   * @param model The calendar system model that manages the calendar data
   *              across multiple calendars
   * @param view  The view component that handles user interface display.
   * @param in    The input source for reading user commands.
   */
  public CalendarController(ICalendarSystem model, ICalendarView view, Readable in) {
    this.model = model;
    this.in = in;
    this.view = view;
  }

  /**
   * Starts the controller's main loop, processing user commands from the input source.
   * The loop continues until the user enters 'quit' or 'q', or the input stream ends.
   * Displays a menu at the start and a farewell message at the end.
   *
   * @throws CalendarException if an unrecoverable error occurs during command processing.
   */
  public void execute() throws CalendarException {
    Scanner sc = new Scanner(in);
    boolean quit = false;
    view.showMenu(); //prompt for the instruction name
    //print the welcome message
    while (!quit) { //continue until the user quits
      view.writeMessage("Enter command: ");
      if (!sc.hasNextLine()) { // end of input
        break;
      }
      String line = sc.nextLine().trim();
      if (line.isEmpty()) { // if the user just pressed enter
        continue;
      }
      Scanner lineScanner = new Scanner(line);
      String userInstruction = lineScanner.next(); //take an instruction name
      if (userInstruction.equals("quit") || userInstruction.equals("q")) {
        quit = true;
      } else if (userInstruction.equals("menu")) {
        view.showMenu();
      } else {
        processInput(userInstruction, lineScanner, model);
      }
    }
    //after the user has quit, print farewell message
    view.farewellMessage();
  }

  /**
   * Processes user input and executes the corresponding calendar command.
   * Supports create, edit, print, and show operations on the calendar.
   *
   * @param userInstruction The command string entered by the user.
   * @param sc              Scanner containing the remainder of the command input.
   * @param system          The calendar system to perform operations on.
   * @throws CalendarException If an error occurs while processing the command.
   */
  private void processInput(String userInstruction, Scanner sc, ICalendarSystem system)
          throws CalendarException {
    CalendarCommand cmd = null;
    try {
      switch (userInstruction.toLowerCase()) {
        case "create":
          if (!sc.hasNext()) throw new CalendarException("Missing 'event' or 'calendar' after 'create'.");
          String createType = sc.next();
          if (createType.equalsIgnoreCase("event")) {
            cmd = new CreateEvent(sc, view); // create event
          } else if (createType.equalsIgnoreCase("calendar")) {
            cmd = new CreateCalendar(sc, view); // create calendar
          } else {
            throw new CalendarException("Unknown command: 'create " + createType + "'");
          }
          break;
        case "edit":
          if (!sc.hasNext()) throw new CalendarException(
                  "Missing 'event'/'events'/'series' or 'calendar' after 'edit'.");
          String editType = sc.next();
          if (editType.equalsIgnoreCase("calendar")) {
            cmd = new EditCalendar(sc, view); // edit a calendar
            // edit an event, EditEvent logic will handle next keyword
          } else {
            //re-add editType keyword to scanner for EditEvent to parse
            // check if scanner has a next line, then append it.
            String restOfLine = editType + (sc.hasNextLine() ? " " + sc.nextLine() : "");
            cmd = new EditEvent(new Scanner(restOfLine), view);
          }
          break;
        case "print":
          cmd = new Print(sc, view);
          break;
        case "show":
          cmd = new Show(sc, view);
          break;
        case "use":
          cmd = new Use(sc, view);
        case "copy":
          cmd = new Copy(sc, view);
          break;
        default:
          throw new CalendarException("Unknown instruction: " + userInstruction);
      }
      cmd.execute(system);
    } catch (CalendarException e) {
      view.writeMessage("Error processing command: " + e.getMessage() + System.lineSeparator());
    }
  }
}