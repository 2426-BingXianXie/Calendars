package calendar.controller;

import java.util.Scanner;

import calendar.CalendarException;
import calendar.controller.commands.Create;
import calendar.controller.commands.Edit;
import calendar.controller.commands.Print;
import calendar.controller.commands.Show;
import calendar.view.ICalendarView;
import calendar.model.ICalendar;


/**
 * Controller class that manages user interactions with the calendar application.
 * Handles input processing, command execution, and view updates for the calendar system.
 * This controller supports both interactive and file-based command processing.
 */
public class CalendarController implements ICalendarController {
  private final Readable in;
  private final ICalendarView view;
  private final ICalendar model;

  /**
   * Constructs a new CalendarController with the specified model, view, and input source.
   *
   * @param model The calendar model that manages the calendar data.
   * @param view  The view component that handles user interface display.
   * @param in    The input source for reading user commands.
   */
  public CalendarController(ICalendar model, ICalendarView view, Readable in) {
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
  public void go() throws CalendarException {
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
   * @param calendar        The calendar model to perform operations on.
   * @throws CalendarException If an error occurs while processing the command.
   */
  private void processInput(String userInstruction, Scanner sc, ICalendar calendar)
          throws CalendarException {
    CalendarCommand cmd = null;
    try {
      switch (userInstruction.toLowerCase()) {
        case "create":
          cmd = new Create(sc, view);
          break;
        case "edit":
          cmd = new Edit(sc, view);
          break;
        case "print":
          cmd = new Print(sc, view);
          break;
        case "show":
          cmd = new Show(sc, view);
          break;
        default:
          throw new CalendarException("Unknown instruction: " + userInstruction);
      }
      cmd.go(calendar);
    } catch (CalendarException e) {
      view.writeMessage("Error processing command: " + e.getMessage() + System.lineSeparator());
    }
  }
}