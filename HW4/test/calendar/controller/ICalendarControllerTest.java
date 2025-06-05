package calendar.controller;

import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import calendar.CalendarException;
import calendar.model.Days;
import calendar.model.Event;
import calendar.model.ICalendar;
import calendar.model.VirtualCalendar;
import calendar.view.ICalendarView;

import static org.junit.Assert.*;

public class ICalendarControllerTest {
  ICalendar model;

  static Interaction prints(String... lines) {
    return (input, output) -> {
      for (String line : lines) {
        output.append(line);
      }
    };
  }

  static Interaction inputs(String in) {
    return (input, output) -> {
      input.append(in);
    };
  }

  @Before
  public void setUp() {
    model = new VirtualCalendar();
  }

  void testRun(ICalendar model, Interaction... interactions) throws CalendarException {
    StringBuilder fakeUserInput = new StringBuilder();
    StringBuilder expectedOutput = new StringBuilder();
    StringBuilder actualOutput = new StringBuilder();

    class MockView implements ICalendarView {

      public void writeMessage(String message) {
        actualOutput.append(message);
      }

      public void showMenu() {
        welcomeMessage();
        showOptions();
      }

      private void welcomeMessage() {
        writeMessage("Welcome to the calendar program!" + System.lineSeparator());
      }

      public void farewellMessage() {
        writeMessage("Thank you for using this program!");
      }

      public void showCalendarEvents(List<Event> events, LocalDate date) {
        writeMessage("Printing events on " + date.toString() + "." + System.lineSeparator());
        printEvents(events);
      }

      @Override
      public void showCalendarEventsInDateRange(LocalDateTime start, LocalDateTime end,
                                                List<Event> events) {
        writeMessage("Printing events from " + start + " to " + end + "." + System.lineSeparator());
        printEvents(events);
      }

      private void printEvents(List<Event> events) {
        if (events.isEmpty()) {
          writeMessage("No events found" + System.lineSeparator());
        }
        for (Event event : events) {
          if (event.getLocation() == null) { // check for valid location in event
            writeMessage("Event '" + event.getSubject() + "' on " + event.getStart().toLocalDate()
                    + " from " + event.getStart().toLocalTime() + " to " +
                    event.getEnd().toLocalTime() + System.lineSeparator());
          } else { // print out location as well
            writeMessage(event.getLocation() + " event '" + event.getSubject() + "' on " +
                    event.getStart().toLocalDate() + "' from " + event.getStart().toLocalTime()
                    + " to " + event.getEnd().toLocalTime() + System.lineSeparator());
          }
        }
      }

      private void showOptions() {
        createOptions();
        editOptions();
        printOptions();
        writeMessage("show status on <dateStringTtimeString>" +
                "(Prints busy status if the user has events scheduled on a given day and time, " +
                "otherwise, available)." + System.lineSeparator());
        writeMessage("menu (Print supported instruction list)" + System.lineSeparator());
        writeMessage("q or quit (quit the program) " + System.lineSeparator());
      }

      private void createOptions() {
        writeMessage("Supported user instructions are: " + System.lineSeparator());
        writeMessage("create event <eventSubject> from <dateStringTtimeString> to " +
                "<dateStringTtimeString> (Create a singular event)"
                + System.lineSeparator());
        writeMessage("create event <eventSubject> from <dateStringTtimeString> to " +
                "<dateStringTtimeString> repeats <weekdays> for <N> times " +
                "(Creates an event series that repeats N times on specific weekdays)"
                + System.lineSeparator());
        writeMessage("create event <eventSubject> from <dateStringTtimeString> to " +
                "<dateStringTtimeString> repeats <weekdays> until <dateString> " +
                "(Creates an event series until a specific date (inclusive))"
                + System.lineSeparator());
        writeMessage("create event <eventSubject> on <dateString> " +
                "(Creates a single all day event.)"
                + System.lineSeparator());
        writeMessage("create event <eventSubject> on <dateString> repeats <weekdays> for <N> times" +
                "(Creates a series of all day events that repeats N times on specific weekdays)"
                + System.lineSeparator());
        writeMessage("create event <eventSubject> on <dateString> repeats <weekdays> until " +
                "<dateString>" + "(Creates a series of all day events until a specific date " +
                "(inclusive)."
                + System.lineSeparator());
      }

      private void editOptions() {
        writeMessage("edit event <property> <eventSubject> from <dateStringTtimeString> to " +
                "<dateStringTtimeString> with <NewPropertyValue>" +
                "(Changes the property of the given event)." + System.lineSeparator());
        writeMessage("edit events <property> <eventSubject> from <dateStringTtimeString> " +
                "with <NewPropertyValue>" +
                "(Identify the event that has the given subject and starts at the given date " +
                "and time and edit its property. If this event is part of a series then the " +
                "properties of all events in that series that start at or after the given date " +
                "and time is changed)." + System.lineSeparator());
        writeMessage("edit series <property> <eventSubject> from <dateStringTtimeString> " +
                "with <NewPropertyValue>" +
                "(Identify the event that has the given subject and starts at the given date and " +
                "time and edit its property. If this event is part of a series then the properties " +
                "of all events in that series is changed)." + System.lineSeparator());
      }

      private void printOptions() {
        writeMessage("print events on <dateString>" +
                "(Prints a bulleted list of all events on that day along with their start and " +
                "end time and location (if any))." + System.lineSeparator());
        writeMessage("print events from <dateStringTtimeString> to <dateStringTtimeString>" +
                "(Prints a bulleted list of all events in the given interval including their start " +
                "and end times and location (if any))." + System.lineSeparator());
      }
    }

    for (Interaction interaction : interactions) {
      interaction.apply(fakeUserInput, expectedOutput);
    }
    System.out.println(fakeUserInput); // debug statement
    Readable input = new StringReader(fakeUserInput.toString());
    ICalendarView view = new MockView();
    ICalendarController controller = new CalendarController(model, view, input);
    controller.go();
    // farewell message will always print due to controller logic
    // controller won't detect a next line as it's taking in a stringReader
    // instead of inputStreamReader, so it will quit automatically at the end of
    // the string. This logic is expected for this test controller, but won't happen
    // in the actual controller
    expectedOutput.append(getExpectedFarewellMessage());
    assertEquals(expectedOutput.toString(), actualOutput.toString());
  }

  private static String getExpectedFullMenuOutput() {
    StringBuilder sb = new StringBuilder();
    // Welcome message
    sb.append("Welcome to the calendar program!").append(System.lineSeparator());
    sb.append("Supported user instructions are: ").append(System.lineSeparator());
    sb.append("create event <eventSubject> from <dateStringTtimeString> to <dateStringTtimeString> (Create a singular event)").append(System.lineSeparator());
    sb.append("create event <eventSubject> from <dateStringTtimeString> to <dateStringTtimeString> repeats <weekdays> for <N> times (Creates an event series that repeats N times on specific weekdays)").append(System.lineSeparator());
    sb.append("create event <eventSubject> from <dateStringTtimeString> to <dateStringTtimeString> repeats <weekdays> until <dateString> (Creates an event series until a specific date (inclusive))").append(System.lineSeparator());
    sb.append("create event <eventSubject> on <dateString> (Creates a single all day event.)").append(System.lineSeparator());
    sb.append("create event <eventSubject> on <dateString> repeats <weekdays> for <N> times(Creates a series of all day events that repeats N times on specific weekdays)").append(System.lineSeparator());
    sb.append("create event <eventSubject> on <dateString> repeats <weekdays> until <dateString>(Creates a series of all day events until a specific date (inclusive).").append(System.lineSeparator());

    // Edit options
    sb.append("edit event <property> <eventSubject> from <dateStringTtimeString> to <dateStringTtimeString> with <NewPropertyValue>(Changes the property of the given event).").append(System.lineSeparator());
    sb.append("edit events <property> <eventSubject> from <dateStringTtimeString> with <NewPropertyValue>(Identify the event that has the given subject and starts at the given date and time and edit its property. If this event is part of a series then the properties of all events in that series that start at or after the given date and time is changed).").append(System.lineSeparator());
    sb.append("edit series <property> <eventSubject> from <dateStringTtimeString> with <NewPropertyValue>(Identify the event that has the given subject and starts at the given date and time and edit its property. If this event is part of a series then the properties of all events in that series is changed).").append(System.lineSeparator());

    // Print options
    sb.append("print events on <dateString>(Prints a bulleted list of all events on that day along with their start and end time and location (if any)).").append(System.lineSeparator());
    sb.append("print events from <dateStringTtimeString> to <dateStringTtimeString>(Prints a bulleted list of all events in the given interval including their start and end times and location (if any)).").append(System.lineSeparator());
    sb.append("show status on <dateStringTtimeString>(Prints busy status if the user has events scheduled on a given day and time, otherwise, available).").append(System.lineSeparator());
    sb.append("menu (Print supported instruction list)").append(System.lineSeparator());
    sb.append("q or quit (quit the program) ").append(System.lineSeparator()); // Note the trailing space in your view, if intentional
    return sb.toString();
  }

  private static String getExpectedEnterCommandPrompt() {
    return "Enter command: "; // Your controller adds this
  }

  private static String getExpectedFarewellMessage() {
    return "Thank you for using this program!";
  }

  private static String getErrorMessage(String errorMessage) {
    return "Error processing command: " + errorMessage;
  }

  private static String getSuccessfulAllDayMessage(String subject, LocalDate date) {
    return "Event '" + subject + "' created from 8am to 5pm on " + date + "\n";
  }

  private static String getSuccessfulSeriesMessage(String subject, LocalDate startDate,
                                                   LocalTime startTime, LocalTime endTime,
                                                   Set<Days> daysOfWeek, int repeatCount) {
    return "Event series '" + subject + "' created on " + startDate +
            " from " + startTime + " to " + endTime + " repeating on " + daysOfWeek + " " +
            repeatCount + " times.\n";
  }


  @Test
  public void testNoInput() throws CalendarException {
    // controller should always print the menu of commands and enter prompt upon creation
    testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()));
  }

  @Test
  public void testUnknownCommand() throws CalendarException {
    String unknownCommand = "hello";
    String expectedErrorMessage = "Unknown instruction: "
            + unknownCommand + System.lineSeparator();
    testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // check when inputting unknown command
            inputs(unknownCommand + "\n"),
            // should print out error message
            prints(getErrorMessage(expectedErrorMessage)),
            // prompt user to type in again
            prints(getExpectedEnterCommandPrompt()));
  }

  @Test
  public void testInputNumber() throws CalendarException {
    String unknownCommand = "123";
    String expectedErrorMessage = "Error processing command: Unknown instruction: "
            + unknownCommand + "\n";
    testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // check when inputting a number as a prompt
            inputs(unknownCommand + "\n"),
            // should print out error message
            prints(expectedErrorMessage),
            // prompt user to type in again
            prints(getExpectedEnterCommandPrompt()));
  }

  @Test
  public void testInputQ() throws CalendarException {
    testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("q\n")); // program should end after inputting 'q'
  }

  @Test
  public void testInputQuit() throws CalendarException {
    testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("quit\n")); // program should end after inputting 'quit'
  }

  @Test
  public void testInputMenu() throws CalendarException {
    testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("menu\n"), // program should re-print menu and command prompt
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()));
  }

  @Test
  public void testInputOnlyCreate() throws CalendarException {
    String errorMessage = "Missing 'event' keyword after 'create'.\n";
    testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create\n"),
            prints(getErrorMessage(errorMessage)),
            // program should always prompt user for next command
            prints(getExpectedEnterCommandPrompt()));
  }

  @Test
  public void testInputOnlyCreateAllCapitalLetters() throws CalendarException {
    String errorMessage = "Missing 'event' keyword after 'create'.\n";
    testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("CREATE\n"), // input 'create' in all capital letters
            // controller should register input disregarding capitalization, output should be
            // the same as the above test
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
  }

  @Test
  public void testInvalidInputAfterCreate() throws CalendarException {
    String input = "hello";
    String errorMessage = "Invalid command 'create " + input + "'.\n";
    testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create " + input), // input "create hello"
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
  }

  @Test
  public void testInvalidInputNumberAfterCreate() throws CalendarException {
    String input = "1";
    String errorMessage = "Invalid command 'create " + input + "'.\n";
    testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create " + input), // input "create 1"
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
  }

  @Test
  public void testInputOnlyCreateEvent() throws CalendarException {
    String errorMessage = "Missing event subject.\n";
    testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event"),
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
  }

  @Test
  public void testInputOnlyCreateEventCapitalized() throws CalendarException {
    String errorMessage = "Missing event subject.\n";
    testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("CREATE EVENT"), // input 'create event' all capitalized
            // again, controller disregards capitalization, output should be same as above test
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
  }

  @Test
  public void testInputOnlyCreateEventSubject() throws CalendarException {
    String errorMessage = "Incomplete command, expected 'on' or 'from'.\n";
    testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test"), // input subject without 'on' or 'from' afterward
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
  }

  @Test
  public void testInputOnlySubjectWithMultipleWords() throws CalendarException {
    String errorMessage = "Incomplete command, expected 'on' or 'from'.\n";
    testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test test"), // input subject without 'on' or 'from' afterward
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
  }

  @Test
  public void testInputOnlyOnAfterSubject() throws CalendarException {
    String errorMessage = "Missing <dateString>.\n";
    testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test on"), // input 'on' keyword without date
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
  }

  @Test
  public void testInputOnlyOnAfterSubjectWithMultipleWords() throws CalendarException {
    String errorMessage = "Missing <dateString>.\n";
    testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // input 'on' keyword without date
            // controller should register all words before 'on' as subject name
            inputs("create event test test test on"),
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
  }

  @Test
  public void testOnAllCapitalLetters() throws CalendarException {
    String errorMessage = "Missing <dateString>.\n";
    testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // input 'on' keyword without date in all capital letters
            // controller should disregard capital letters
            inputs("create event test ON"),
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
  }

  @Test
  public void testInvalidDateAfterOn() throws CalendarException {
    String errorMessage = "Invalid date format for <dateString>. Expected YYYY-MM-DD\n";
    testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test on test"), // input invalid date
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
  }

  @Test
  public void testInvalidDateAllZeros() throws CalendarException {
    String errorMessage = "Invalid date format for <dateString>. Expected YYYY-MM-DD\n";
    testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test on 0000-00-00"), // input invalid date
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
  }

  @Test
  public void testInvalidDateWithSlashes() throws CalendarException {
    String errorMessage = "Invalid date format for <dateString>. Expected YYYY-MM-DD\n";
    testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test on 2024/11/12"), // input date with '/' instead of '-'
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
  }

  @Test
  public void testInvalidDateFormat() throws CalendarException {
    String errorMessage = "Invalid date format for <dateString>. Expected YYYY-MM-DD\n";
    testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test on 2024-1-1"), // input YYYY-MM-DD without leading zeros
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
  }

  @Test
  public void testInputDDMMYYYY() throws CalendarException {
    String errorMessage = "Invalid date format for <dateString>. Expected YYYY-MM-DD\n";
    testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test on 01-01-2024"), // input DD-MM-YYYY
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
  }

  @Test
  public void testInputMMDDYYYY() throws CalendarException {
    String errorMessage = "Invalid date format for <dateString>. Expected YYYY-MM-DD\n";
    testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test on 12-31-2024"), // input MM-DD-YYYY
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
  }

  @Test
  public void testInputYYYYDDMM() throws CalendarException {
    String errorMessage = "Invalid date format for <dateString>. Expected YYYY-MM-DD\n";
    testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test on 2025-31-12"), // input YYYY-DD-MM
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
  }

  @Test
  public void testInputDayAsZero() throws CalendarException {
    String errorMessage = "Invalid date format for <dateString>. Expected YYYY-MM-DD\n";
    testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test on 2025-12-00"), // input day as 0
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
  }

  @Test
  public void testInputMonthAsZero() throws CalendarException {
    String errorMessage = "Invalid date format for <dateString>. Expected YYYY-MM-DD\n";
    testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test on 2025-00-31"), // input month as 0
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
  }

  @Test
  public void testInputMonthAndDayAsZero() throws CalendarException {
    String errorMessage = "Invalid date format for <dateString>. Expected YYYY-MM-DD\n";
    testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test on 2025-00-00"), // input day and month as 0
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
  }

  @Test
  public void testInputFeb29OnNonLeapYear() throws CalendarException {
    String errorMessage = "Invalid date format for <dateString>. Expected YYYY-MM-DD\n";
    testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test on 2025-02-29"), // input Feb 29 2025
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
  }

  @Test
  public void testInputInvalidDate() throws CalendarException {
    String errorMessage = "Invalid date format for <dateString>. Expected YYYY-MM-DD\n";
    testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test on 2025-04-31"), // input Apr 31 2025
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
  }

  @Test
  public void testInputValidDate() throws CalendarException {
    testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test on 2025-06-05"), // input Jun 5 2025
            // date is valid, print success message
            prints(getSuccessfulAllDayMessage("test", LocalDate.of(2025, 6, 5))),
            // prompt user for next command
            prints(getExpectedEnterCommandPrompt()));
  }

  @Test
  public void testInputDuplicateDate() throws CalendarException {
    String errorMessage = "Event already exists\n";
    testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test on 2025-06-05\n"), // input Jun 5 2025
            // date is valid, print success message
            prints(getSuccessfulAllDayMessage("test", LocalDate.of(2025, 6, 5))),
            // prompt user for next command
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test on 2025-06-05"),
            // event already exists, should throw error
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
  }

  @Test
  public void testInputMinDate() throws CalendarException {
    testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test on 0000-01-01"), // input Jan 1 0000
            // date is valid, print success message
            prints(getSuccessfulAllDayMessage("test", LocalDate.of(0000, 1, 1))),
            // prompt user for next command
            prints(getExpectedEnterCommandPrompt()));
  }

  @Test
  public void testInputMaxDate() throws CalendarException {
    testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test on 9999-12-31"), // input Dec 31 9999
            // date is valid, print success message
            prints(getSuccessfulAllDayMessage("test", LocalDate.of(9999, 12, 31))),
            // prompt user for next command
            prints(getExpectedEnterCommandPrompt()));
  }


  @Test
  public void testInputFeb29OnLeapYear() throws CalendarException {
    testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test on 2024-02-29"), // input Feb 29 2024
            // date is valid, print success message
            prints(getSuccessfulAllDayMessage("test", LocalDate.of(2024, 2, 29))),
            // prompt user for next command
            prints(getExpectedEnterCommandPrompt()));
  }

  @Test
  public void testInvalidInputAfterDate() throws CalendarException {
    String errorMessage =
            "Unexpected token. Expected 'repeats' or end of command for single all-day event.\n";
    testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test on 2024-02-29 test"), // input 'test' after date
            // expected is 'repeats', throw an error
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
  }

  @Test
  public void testInputOnlyRepeatsAfterDate() throws CalendarException {
    String errorMessage =
            "Missing days to repeat\n";
    testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test on 2024-02-29 repeats"), // input 'repeats' after date
            // expected is 'repeats', throw an error
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
  }

  @Test
  public void testInputInvalidDaysAfterRepeats() throws CalendarException {
    String input = "XYZ";
    String errorMessage = "Invalid weekday symbol: X\n";
    testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // input invalid weekday string
            inputs("create event test on 2024-02-29 repeats " + input),
            // expected is 'repeats', throw an error
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
  }

  @Test
  public void testInputInvalidDaysWithValidDays() throws CalendarException {
    String input = "MFZ"; // monday, friday, invalid
    String errorMessage = "Invalid weekday symbol: Z\n";
    testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // input invalid weekday string
            inputs("create event test on 2024-02-29 repeats " + input),
            // expected is 'repeats', throw an error
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
  }

  @Test
  public void testInputInvalidDaysAsNum() throws CalendarException {
    String input = "123";
    String errorMessage = "Invalid weekday symbol: 1\n";
    testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // input invalid weekday string
            inputs("create event test on 2024-02-29 repeats " + input),
            // expected is 'repeats', throw an error
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
  }

  @Test
  public void testInputValidDays() throws CalendarException {
    String input = "MWF"; // mon, wed, fri
    String errorMessage = "Missing 'for' or 'until'.\n";
    testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // input valid weekday string
            inputs("create event test on 2024-02-29 repeats " + input),
            // expected is 'from' or 'until' after, throw error
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
  }

  @Test
  public void testInputValidDaysLowerCase() throws CalendarException {
    String input = "mwf"; // mon, wed, fri
    String errorMessage = "Missing 'for' or 'until'.\n";
    testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // input valid weekday string
            // should ignore capitalization
            inputs("create event test on 2024-02-29 repeats " + input),
            // expected is 'from' or 'until' after, throw error
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
  }

  @Test
  public void testInvalidInputAfterDays() throws CalendarException {
    String input = "mwf"; // mon, wed, fri
    String errorMessage = "Expected 'for' or 'until' after 'repeats <weekdays>'.\n";
    testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // input valid weekday string
            // should ignore capitalization
            inputs("create event test on 2024-02-29 repeats " + input + " test"),
            // expected is 'from' or 'until' after, throw error
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
  }

  @Test
  public void testInputOnlyForAfterDays() throws CalendarException {
    String input = "mwf"; // mon, wed, fri
    String errorMessage = "Missing <N> after 'for'.\n";
    testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // input valid weekday string
            // should ignore capitalization
            inputs("create event test on 2024-02-29 repeats " + input + " for"),
            // expected is number of repeats after 'for', throw an error
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
  }

  @Test
  public void testInputOnlyForCapitalizedAfterDays() throws CalendarException {
    String input = "mwf"; // mon, wed, fri
    String errorMessage = "Missing <N> after 'for'.\n";
    testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // input valid weekday string
            // should ignore capitalization
            inputs("create event test on 2024-02-29 repeats " + input + " FOR"),
            // expected is number of repeats after 'for', throw an error
            // should ignore capitalization, return same error as test above
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
  }

  @Test
  public void testInputNonNumberAfterFor() throws CalendarException {
    String input = "mwf"; // mon, wed, fri
    String errorMessage = "Missing <N> after 'for'.\n";
    testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // input valid weekday string
            // should ignore capitalization
            inputs("create event test on 2024-02-29 repeats " + input + " for j"),
            // expected is a number for <N>, will throw error if given a non-number
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
  }

  @Test
  public void testInputOnlyNumberAfterFor() throws CalendarException {
    String input = "mwf"; // mon, wed, fri
    String errorMessage = "Missing 'times' after 'for <N>'.\n";
    testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // input valid weekday string
            // should ignore capitalization
            inputs("create event test on 2024-02-29 repeats " + input + " for 5"),
            // expected 'times' after number, throw an error
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
  }

  @Test
  public void testInvalidInputAfterNumber() throws CalendarException {
    String input = "mwf"; // mon, wed, fri
    String errorMessage = "Missing 'times' after 'for <N>'.\n";
    testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // input valid weekday string
            // should ignore capitalization
            inputs("create event test on 2024-02-29 repeats " + input + " for 5 test"),
            // expected 'times' after number, throw an error
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
  }

  @Test
  public void testValidInputAfterNumber() throws CalendarException {
    String days = "mwf"; // mon, wed, fri
    int repeats = 5;
    Set<Days> daysOfWeek = new HashSet<Days>();
    char[] chars = days.toCharArray();
    for (char c : chars) {
      daysOfWeek.add(Days.fromSymbol(c));
    }
    String dateString = "2024-02-29";
    LocalDate date = LocalDate.parse(dateString);
    LocalTime startTime = LocalTime.of(8, 0);
    LocalTime endTime = LocalTime.of(17, 0);
    testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // input valid weekday string
            inputs("create event test on " + dateString + " repeats " + days + " for " + repeats +
                    " times"),
            // given valid input, should successfully create a event series
            prints(getSuccessfulSeriesMessage("test", date, startTime, endTime, daysOfWeek,
                    repeats)),
            prints(getExpectedEnterCommandPrompt()));
  }

  @Test
  public void testDuplicateSeriesAfterTimes() throws CalendarException {
    String subject = "test";
    String errorMessage = "Duplicate event in series: " + subject + "\n";
    String days = "mwf"; // mon, wed, fri
    int repeats = 5;
    Set<Days> daysOfWeek = new HashSet<Days>();
    char[] chars = days.toCharArray();
    for (char c : chars) {
      daysOfWeek.add(Days.fromSymbol(c));
    }
    String dateString = "2024-02-29";
    LocalDate date = LocalDate.parse(dateString);
    LocalTime startTime = LocalTime.of(8, 0);
    LocalTime endTime = LocalTime.of(17, 0);
    testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // input valid weekday string
            inputs("create event " + subject + " on " + dateString + " repeats " + days + " for "
                    + repeats + " times\n"),
            // given valid input, should successfully create a event series
            prints(getSuccessfulSeriesMessage("test", date, startTime, endTime, daysOfWeek,
                    repeats)),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test on " + dateString + " repeats " + days + " for " + repeats +
                    " times"),
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
  }

  @Test
  public void testDuplicateEventAfterEventSeries() throws CalendarException {
    String subject = "test";
    String errorMessage = "Event already exists\n";
    String days = "mwrf"; // mon, wed, thur, fri
    int repeats = 5;
    Set<Days> daysOfWeek = new HashSet<Days>();
    char[] chars = days.toCharArray();
    for (char c : chars) {
      daysOfWeek.add(Days.fromSymbol(c));
    }
    String dateString = "2024-02-29";
    LocalDate date = LocalDate.parse(dateString);
    LocalTime startTime = LocalTime.of(8, 0);
    LocalTime endTime = LocalTime.of(17, 0);
    testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // input valid weekday string
            inputs("create event " + subject + " on " + dateString + " repeats " + days + " for "
                    + repeats + " times\n"),
            // given valid input, should successfully create a event series
            prints(getSuccessfulSeriesMessage("test", date, startTime, endTime, daysOfWeek,
                    repeats)),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test on 2024-02-29"),
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
  }

  @Test
  public void testInputOnlyUntilAfterDays() throws CalendarException {
    String input = "mwf"; // mon, wed, fri
    String errorMessage = "Missing <dateString>.\n";
    testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // input valid weekday string
            // should ignore capitalization
            inputs("create event test on 2024-02-29 repeats " + input + " until"),
            // expected is end date after 'until', throw an error
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
  }

}






