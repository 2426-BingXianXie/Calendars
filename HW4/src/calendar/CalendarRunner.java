package calendar;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.IOException;

import calendar.controller.CalendarController;
import calendar.view.CalendarView;
import calendar.model.ICalendar;
import calendar.controller.ICalendarController;
import calendar.view.ICalendarView;
import calendar.model.VirtualCalendar;

/**
 * The driver of the calendar application.
 * This class is responsible for initializing the model, view, and controller,
 * and starting the application based on the command line arguments.
 */
public class CalendarRunner {
  /**
   * The main method of the calendar application.
   * It initializes the model, view, and controller based on the command line arguments.
   *
   * @param args command line arguments to determine the mode of operation
   */
  public static void main(String[] args) {
    ICalendar model = new VirtualCalendar();
    ICalendarView view = new CalendarView(System.out);
    ICalendarController controller = null;

    try {
      // check that first input is '--mode'
      if (args.length >= 2 && args[0].equalsIgnoreCase("--mode")) {
        String mode = args[1].toLowerCase();
        if ("interactive".equals(mode)) {
          if (args.length != 2) { // check that the input is '--mode interactive'
            System.err.println("Error: expected '--mode interactive'");
            System.exit(1);
          }
          Readable rd = new InputStreamReader(System.in);
          controller = new CalendarController(model, view, rd);
          controller.go();
        } else if ("headless".equals(mode)) { // check that input is '--mode headless <filename>'
          if (args.length != 3) {
            System.err.println("Error: expected '--mode headless <filename>'");
            System.exit(1);
          }
          String commandFile = args[2];
          try (BufferedReader reader = new BufferedReader(new FileReader(commandFile))) {
            controller = new CalendarController(model, view, reader); // pass file reader
            controller.go();
          } catch (IOException e) { // check for valid command file
            view.writeMessage("Error: Could not read command file '" + commandFile + "'. ");
            System.exit(1);
          }
        } else { // invalid mode input
          view.writeMessage("Error: Unknown mode '" + mode + "'. " +
                  "Valid modes are 'interactive' or 'headless'.");
          System.exit(1);
        }
      } else { // invalid first input
        view.writeMessage("Error: expected '--mode <interactive | headless <filename>>'");
        System.exit(1);
      }
    } catch (CalendarException e) { // catch calendarExceptions from controller/model
      view.writeMessage("Application Error");
      System.exit(1);
    }
  }
}