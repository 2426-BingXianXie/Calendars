package calendar;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Scanner;
import calendar.controller.CalendarController;
import calendar.view.CalendarView;
import calendar.model.ICalendar;
import calendar.controller.ICalendarController;
import calendar.view.ICalendarView;
import calendar.model.VirtualCalendar;
import calendar.CalendarException;

public class CalendarRunner {
  public static void main(String[] args) {
    ICalendar model = new VirtualCalendar();
    ICalendarView view = new CalendarView(System.out);
    ICalendarController controller = null;

    try {
      // If no arguments provided, prompt user for mode
      if (args.length == 0) {
        System.out.println("Please select mode:");
        System.out.println("1. Interactive");
        System.out.println("2. Headless");
        System.out.print("Enter choice 1 or 2 : ");

        Scanner scanner = new Scanner(System.in);
        int choice = scanner.nextInt();
        scanner.nextLine();  // Consume newline

        if (choice == 1) {
          args = new String[]{"--mode", "interactive"};
        } else if (choice == 2) {
          System.out.print("Enter command file path: ");
          String filePath = scanner.nextLine();
          args = new String[]{"--mode", "headless", filePath};
        } else {
          System.err.println("Invalid choice. Exiting.");
          System.exit(1);
        }
      }

      // Process arguments
      if (args.length >= 2 && args[0].equalsIgnoreCase("--mode")) {
        String mode = args[1].toLowerCase();

        if ("interactive".equals(mode)) {
          if (args.length != 2) {
            System.err.println("Usage for interactive: java CalendarRunner --mode interactive");
            System.exit(1);
          }
          controller = new CalendarController(model, view, new InputStreamReader(System.in));
          controller.go();
        } else if ("headless".equals(mode)) {
          if (args.length != 3) {
            System.err.println("Usage for headless: java CalendarRunner --mode headless <filename>");
            System.exit(1);
          }
          try (BufferedReader reader = new BufferedReader(new FileReader(args[2]))) {
            controller = new CalendarController(model, view, reader);
            controller.go();
          }
        } else {
          System.err.println("Unknown mode: " + mode);
          System.err.println("Valid modes: interactive, headless");
          System.exit(1);
        }
      } else {
        System.err.println("Usage: java CalendarRunner [--mode interactive] [--mode headless <filename>]");
        System.err.println("Example for interactive mode: java CalendarRunner --mode interactive");
        System.err.println("Example for headless mode: java CalendarRunner --mode headless commands.txt");
        System.exit(1);
      }
    } catch (CalendarException e) {
      System.err.println("Calendar Error: " + e.getMessage());
      System.exit(1);
    } catch (IOException e) {
      System.err.println("I/O Error: " + e.getMessage());
      System.err.println("Could not read file: " + (args.length > 2 ? args[2] : ""));
      System.exit(1);
    } catch (Exception e) {
      System.err.println("Unexpected Error: " + e.getMessage());
      e.printStackTrace();
      System.exit(1);
    }
  }
}