package calendar.controller;

import java.util.Scanner;

import calendar.CalendarException;
import calendar.controller.commands.Create;
import calendar.controller.commands.Edit;
import calendar.controller.commands.Print;
import calendar.controller.commands.Show;
import calendar.view.ICalendarView;
import calendar.model.ICalendar;

public class CalendarController implements ICalendarController {
  private final Readable in;
  private final ICalendarView view;
  private final ICalendar model;

  public CalendarController(ICalendar model, ICalendarView view, Readable in) {
    this.model = model;
    this.in = in;
    this.view = view;
  }

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

  private void processInput(String userInstruction, Scanner sc, ICalendar calendar)
          throws CalendarException {
    CalendarCommand cmd = null;
    try {
      switch (userInstruction) {
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
