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
import calendar.model.CalendarSystem;
import calendar.model.Days;
import calendar.model.Event;
import calendar.model.ICalendar;
import calendar.model.ICalendarSystem;
import calendar.model.VirtualCalendar;
import calendar.view.ICalendarView;

import static org.junit.Assert.assertEquals;


/**
 * Test class for the ICalendarController implementation.
 * Contains tests for various calendar operations including creating, editing,
 * and managing calendar events.
 */
public class ICalendarControllerTest {
  ICalendarSystem model;

  /**
   * Creates an Interaction that simulates system output.
   *
   * @param lines The lines of text that should be output by the system
   * @return An Interaction object that will append the given lines to the output
   */
  static Interaction prints(String... lines) {
    return (input, output) -> {
      for (String line : lines) {
        output.append(line);
      }
    };
  }

  /**
   * Creates an Interaction that simulates user input.
   *
   * @param in The text to be provided as user input
   * @return An Interaction object that will append the given input to the input buffer
   */
  static Interaction inputs(String in) {
    return (input, output) -> {
      input.append(in);
    };
  }

  @Before
  public void setUp() {
    model = new CalendarSystem();
  }


  /**
   * Executes a test scenario by simulating user interactions with the calendar controller.
   * This method creates a mock view and processes a series of interactions to test the
   * controller's behavior. It compares the actual output against expected output to verify correct
   * functionality.
   *
   * @param model        The calendar model to be used for the test
   * @param interactions A variable number of Interaction objects that simulate user inputs and
   *                     expected outputs
   * @throws CalendarException if an error occurs during the test execution
   */
  String[] testRun(ICalendarSystem model, Interaction... interactions) throws CalendarException {
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
        writeMessage("create event <eventSubject> on <dateString> repeats <weekdays> for <N> times"
                + "(Creates a series of all day events that repeats N times on specific weekdays)"
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
                "(Identify the event that has the given subject and starts at the given date and "
                + "time and edit its property. If this event is part of a series then the " +
                "properties " + "of all events in that series is changed)."
                + System.lineSeparator());
      }

      private void printOptions() {
        writeMessage("print events on <dateString>" +
                "(Prints a bulleted list of all events on that day along with their start and " +
                "end time and location (if any))." + System.lineSeparator());
        writeMessage("print events from <dateStringTtimeString> to <dateStringTtimeString>" +
                "(Prints a bulleted list of all events in the given interval including " +
                "their start " +
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
    controller.execute();
    // farewell message will always print due to controller logic
    // controller won't detect a next line as it's taking in a stringReader
    // instead of inputStreamReader, so it will quit automatically at the end of
    // the string. This logic is expected for this test controller, but won't happen
    // in the actual controller
    expectedOutput.append(getExpectedFarewellMessage());
    String[] strings = {expectedOutput.toString(), actualOutput.toString()};
    return strings;
  }

  private static String getExpectedFullMenuOutput() {
    StringBuilder sb = new StringBuilder();
    // Welcome message
    sb.append("Welcome to the calendar program!").append(System.lineSeparator());
    sb.append("Supported user instructions are: ").append(System.lineSeparator());
    sb.append("create event <eventSubject> from <dateStringTtimeString> to " +
            "<dateStringTtimeString> (Create a singular event)").append(System.lineSeparator());
    sb.append("create event <eventSubject> from <dateStringTtimeString> to " +
            "<dateStringTtimeString> repeats <weekdays> for <N> times (Creates an event series " +
            "that repeats N times on specific weekdays)").append(System.lineSeparator());
    sb.append("create event <eventSubject> from <dateStringTtimeString> to " +
            "<dateStringTtimeString> repeats <weekdays> until <dateString> " +
            "(Creates an event series until a specific date (inclusive))")
            .append(System.lineSeparator());
    sb.append("create event <eventSubject> on <dateString> (Creates a single all day event.)")
            .append(System.lineSeparator());
    sb.append("create event <eventSubject> on <dateString> repeats <weekdays> for <N> times" +
            "(Creates a series of all day events that repeats N times on specific weekdays)")
            .append(System.lineSeparator());
    sb.append("create event <eventSubject> on <dateString> repeats <weekdays> until " +
            "<dateString>(Creates a series of all day events until a specific date (inclusive).")
            .append(System.lineSeparator());

    // Edit options
    sb.append("edit event <property> <eventSubject> from <dateStringTtimeString> to " +
            "<dateStringTtimeString> with <NewPropertyValue>(Changes the property of the" +
            " given event).").append(System.lineSeparator());
    sb.append("edit events <property> <eventSubject> from <dateStringTtimeString> with" +
            " <NewPropertyValue>(Identify the event that has the given subject and starts" +
            " at the given date and time and edit its property. If this event is part of a " +
            "series then the properties of all events in that series that start at or after " +
            "the given date and time is changed).").append(System.lineSeparator());
    sb.append("edit series <property> <eventSubject> from <dateStringTtimeString> with " +
            "<NewPropertyValue>(Identify the event that has the given subject and starts at " +
            "the given date and time and edit its property. If this event is part of a series " +
            "then the properties of all events in that series is changed).")
            .append(System.lineSeparator());

    // Print options
    sb.append("print events on <dateString>(Prints a bulleted list of all events on that day " +
            "along with their start and end time and location (if any)).")
            .append(System.lineSeparator());
    sb.append("print events from <dateStringTtimeString> to <dateStringTtimeString>" +
            "(Prints a bulleted list of all events in the given interval including their " +
            "start and end times and location (if any)).").append(System.lineSeparator());
    sb.append("show status on <dateStringTtimeString>(Prints busy status if the user has " +
            "events scheduled on a given day and time, otherwise, available).")
            .append(System.lineSeparator());
    sb.append("menu (Print supported instruction list)").append(System.lineSeparator());
    sb.append("q or quit (quit the program) ").append(System.lineSeparator());
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

  private static String getSuccessfulEventMessage(
          String subject, LocalDateTime start, LocalDateTime end) {
    return "Event '" + subject + "' created from " + start + " to " + end + ".\n";
  }

  private static String getSuccessfulForSeriesMessage(String subject, LocalDate startDate,
                                                   LocalTime startTime, LocalTime endTime,
                                                   Set<Days> daysOfWeek, int repeatCount) {
    return "Event series '" + subject + "' created on " + startDate +
            " from " + startTime + " to " + endTime + " repeating on " + daysOfWeek + " " +
            repeatCount + " times.\n";
  }

  private static String getSuccessfulUntilSeriesMessage(String subject, LocalDate startDate,
                                                        LocalTime startTime, LocalTime endTime,
                                                        Set<Days> daysOfWeek, LocalDate endDate) {
    return "Event series '" + subject + "' created on " + startDate +
            " from " + startTime + " to " + endTime + " repeating on " + daysOfWeek + " until " +
            endDate + ".\n";
  }

  private static String getPrintOnDateMessage(LocalDate date, String eventListString) {
    return "Printing events on " + date.toString() + "." + System.lineSeparator() + eventListString;
  }

  private static String getNoEventsFoundMessage(LocalDate date) {
    return "Printing events on " + date.toString() + "." + System.lineSeparator()
            + "No events found"
            + System.lineSeparator();
  }

  private static String getBusyStatusMessage(LocalDateTime dateTime, boolean isBusy) {
    if (isBusy) {
      return "User is busy, already has an event scheduled on " + dateTime + "."
              + System.lineSeparator();
    }
    return "User is available on " + dateTime + "."
            + System.lineSeparator();
  }

  private static String getSuccessfulEditMessage(String subject, String property, String newValue) {
    return "Edited event '" + subject + "' " + property + " property to " + newValue
            + System.lineSeparator();
  }

  private static String getSuccessfulSeriesFromDateEditMessage(String subject, String property,
                                                               String newValue,
                                                               LocalDateTime date) {
    return "Edited event series '" + subject + "' " + property + " property to " + newValue +
            " from " + date + System.lineSeparator();
  }

  private static String getSuccessfulSeriesEditMessage(String subject, String property,
                                                       String newValue) {
    return "Edited event series '" + subject + "' " + property + " property to " + newValue
            + System.lineSeparator();
  }


  @Test
  public void testNoInput() throws CalendarException {
    // controller should always print the menu of commands and enter prompt upon creation
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testUnknownCommand() throws CalendarException {
    String unknownCommand = "hello";
    String expectedErrorMessage = "Unknown instruction: "
            + unknownCommand + System.lineSeparator();
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // check when inputting unknown command
            inputs(unknownCommand + "\n"),
            // should print out error message
            prints(getErrorMessage(expectedErrorMessage)),
            // prompt user to type in again
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInputNumber() throws CalendarException {
    String unknownCommand = "123";
    String expectedErrorMessage = "Error processing command: Unknown instruction: "
            + unknownCommand + "\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // check when inputting a number as a prompt
            inputs(unknownCommand + "\n"),
            // should print out error message
            prints(expectedErrorMessage),
            // prompt user to type in again
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInputQ() throws CalendarException {
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("q\n")); // program should end after inputting 'q'
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInputQuit() throws CalendarException {
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("quit\n")); // program should end after inputting 'quit'
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInputMenu() throws CalendarException {
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("menu\n"), // program should re-print menu and command prompt
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInputOnlyCreate() throws CalendarException {
    String errorMessage = "Missing 'event' keyword after 'create'.\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create\n"),
            prints(getErrorMessage(errorMessage)),
            // program should always prompt user for next command
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInputOnlyCreateAllCapitalLetters() throws CalendarException {
    String errorMessage = "Missing 'event' keyword after 'create'.\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("CREATE\n"), // input 'create' in all capital letters
            // controller should register input disregarding capitalization, output should be
            // the same as the above test
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInvalidInputAfterCreate() throws CalendarException {
    String input = "hello";
    String errorMessage = "Invalid command 'create " + input + "'.\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create " + input), // input "create hello"
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInvalidInputNumberAfterCreate() throws CalendarException {
    String input = "1";
    String errorMessage = "Invalid command 'create " + input + "'.\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create " + input), // input "create 1"
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInputOnlyCreateEvent() throws CalendarException {
    String errorMessage = "Missing event subject.\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event"),
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInputOnlyCreateEventCapitalized() throws CalendarException {
    String errorMessage = "Missing event subject.\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("CREATE EVENT"), // input 'create event' all capitalized
            // again, controller disregards capitalization, output should be same as above test
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInputOnlyCreateEventSubject() throws CalendarException {
    String errorMessage = "Incomplete command, expected 'on' or 'from'.\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test"), // input subject without 'on' or 'from' afterward
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInputOnlySubjectWithMultipleWords() throws CalendarException {
    String errorMessage = "Incomplete command, expected 'on' or 'from'.\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test test"), // input subject without 'on' or 'from' afterward
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInputOnlyOnAfterSubject() throws CalendarException {
    String errorMessage = "Missing <dateString>.\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test on"), // input 'on' keyword without date
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInputOnlyOnAfterSubjectWithMultipleWords() throws CalendarException {
    String errorMessage = "Missing <dateString>.\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // input 'on' keyword without date
            // controller should register all words before 'on' as subject name
            inputs("create event test test test on"),
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testOnAllCapitalLetters() throws CalendarException {
    String errorMessage = "Missing <dateString>.\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // input 'on' keyword without date in all capital letters
            // controller should disregard capital letters
            inputs("create event test ON"),
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInvalidDateAfterOn() throws CalendarException {
    String errorMessage = "Invalid date format for <dateString>. Expected YYYY-MM-DD\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test on test"), // input invalid date
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInvalidDateAllZeros() throws CalendarException {
    String errorMessage = "Invalid date format for <dateString>. Expected YYYY-MM-DD\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test on 0000-00-00"), // input invalid date
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInvalidDateWithSlashes() throws CalendarException {
    String errorMessage = "Invalid date format for <dateString>. Expected YYYY-MM-DD\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test on 2024/11/12"), // input date with '/' instead of '-'
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInvalidDateFormat() throws CalendarException {
    String errorMessage = "Invalid date format for <dateString>. Expected YYYY-MM-DD\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test on 2024-1-1"), // input YYYY-MM-DD without leading zeros
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInputDDMMYYYY() throws CalendarException {
    String errorMessage = "Invalid date format for <dateString>. Expected YYYY-MM-DD\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test on 01-01-2024"), // input DD-MM-YYYY
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInputMMDDYYYY() throws CalendarException {
    String errorMessage = "Invalid date format for <dateString>. Expected YYYY-MM-DD\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test on 12-31-2024"), // input MM-DD-YYYY
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInputYYYYDDMM() throws CalendarException {
    String errorMessage = "Invalid date format for <dateString>. Expected YYYY-MM-DD\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test on 2025-31-12"), // input YYYY-DD-MM
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInputDayAsZero() throws CalendarException {
    String errorMessage = "Invalid date format for <dateString>. Expected YYYY-MM-DD\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test on 2025-12-00"), // input day as 0
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInputMonthAsZero() throws CalendarException {
    String errorMessage = "Invalid date format for <dateString>. Expected YYYY-MM-DD\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test on 2025-00-31"), // input month as 0
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInputMonthAndDayAsZero() throws CalendarException {
    String errorMessage = "Invalid date format for <dateString>. Expected YYYY-MM-DD\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test on 2025-00-00"), // input day and month as 0
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInputFeb29OnNonLeapYear() throws CalendarException {
    String errorMessage = "Invalid date format for <dateString>. Expected YYYY-MM-DD\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test on 2025-02-29"), // input Feb 29 2025
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInputInvalidDate() throws CalendarException {
    String errorMessage = "Invalid date format for <dateString>. Expected YYYY-MM-DD\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test on 2025-04-31"), // input Apr 31 2025
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInputDateTime() throws CalendarException {
    String errorMessage = "Invalid date format for <dateString>. Expected YYYY-MM-DD\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test on 2025-04-31T09:00"), // input Apr 31 2025 09:00
            // given LocalDateTime instade of LocalDate, throw an error
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInputValidDate() throws CalendarException {
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test on 2025-06-05"), // input Jun 5 2025
            // date is valid, print success message
            prints(getSuccessfulAllDayMessage("test", LocalDate.of(2025, 6,
                    5))),
            // prompt user for next command
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInputValidDateWithSubjectAsNumber() throws CalendarException {
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event 123 on 2025-06-05"), // input Jun 5 2025
            // date is valid, print success message
            prints(getSuccessfulAllDayMessage("123", LocalDate.of(2025, 6,
                    5))),
            // prompt user for next command
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInputDuplicateDate() throws CalendarException {
    String errorMessage = "Event already exists\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test on 2025-06-05\n"), // input Jun 5 2025
            // date is valid, print success message
            prints(getSuccessfulAllDayMessage("test", LocalDate.of(2025, 6,
                    5))),
            // prompt user for next command
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test on 2025-06-05"),
            // event already exists, should throw error
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInputMinDate() throws CalendarException {
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test on 0000-01-01"), // input Jan 1 0000
            // date is valid, print success message
            prints(getSuccessfulAllDayMessage("test", LocalDate.of(0000, 1,
                    1))),
            // prompt user for next command
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInputMaxDate() throws CalendarException {
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test on 9999-12-31"), // input Dec 31 9999
            // date is valid, print success message
            prints(getSuccessfulAllDayMessage("test", LocalDate.of(9999, 12,
                    31))),
            // prompt user for next command
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }


  @Test
  public void testInputFeb29OnLeapYear() throws CalendarException {
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test on 2024-02-29"), // input Feb 29 2024
            // date is valid, print success message
            prints(getSuccessfulAllDayMessage("test", LocalDate.of(2024, 2,
                    29))),
            // prompt user for next command
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInvalidInputAfterDate() throws CalendarException {
    String errorMessage =
            "Unexpected token. Expected 'repeats' or end of command for single all-day event.\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test on 2024-02-29 test"), // input 'test' after date
            // expected is 'repeats', throw an error
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInputOnlyRepeatsAfterDate() throws CalendarException {
    String errorMessage =
            "Missing days to repeat\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test on 2024-02-29 repeats"), // input 'repeats' after date
            // expected is 'repeats', throw an error
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInputInvalidDaysAfterRepeats() throws CalendarException {
    String input = "XYZ";
    String errorMessage = "Invalid weekday symbol: X\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // input invalid weekday string
            inputs("create event test on 2024-02-29 repeats " + input),
            // expected is 'repeats', throw an error
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInputInvalidDaysWithValidDays() throws CalendarException {
    String input = "MFZ"; // monday, friday, invalid
    String errorMessage = "Invalid weekday symbol: Z\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // input invalid weekday string
            inputs("create event test on 2024-02-29 repeats " + input),
            // expected is 'repeats', throw an error
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInputInvalidDaysAsNum() throws CalendarException {
    String input = "123";
    String errorMessage = "Invalid weekday symbol: 1\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // input invalid weekday string
            inputs("create event test on 2024-02-29 repeats " + input),
            // expected is 'repeats', throw an error
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInputValidDays() throws CalendarException {
    String input = "MWF"; // mon, wed, fri
    String errorMessage = "Missing 'for' or 'until'.\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // input valid weekday string
            inputs("create event test on 2024-02-29 repeats " + input),
            // expected is 'from' or 'until' after, throw error
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInputValidDaysLowerCase() throws CalendarException {
    String input = "mwf"; // mon, wed, fri
    String errorMessage = "Missing 'for' or 'until'.\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // input valid weekday string
            // should ignore capitalization
            inputs("create event test on 2024-02-29 repeats " + input),
            // expected is 'from' or 'until' after, throw error
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInvalidInputAfterDays() throws CalendarException {
    String input = "mwf"; // mon, wed, fri
    String errorMessage = "Expected 'for' or 'until' after 'repeats <weekdays>'.\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // input valid weekday string
            // should ignore capitalization
            inputs("create event test on 2024-02-29 repeats " + input + " test"),
            // expected is 'from' or 'until' after, throw error
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInputOnlyForAfterDays() throws CalendarException {
    String input = "mwf"; // mon, wed, fri
    String errorMessage = "Missing <N> after 'for'.\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // input valid weekday string
            // should ignore capitalization
            inputs("create event test on 2024-02-29 repeats " + input + " for"),
            // expected is number of repeats after 'for', throw an error
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInputOnlyForCapitalizedAfterDays() throws CalendarException {
    String input = "mwf"; // mon, wed, fri
    String errorMessage = "Missing <N> after 'for'.\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // input valid weekday string
            // should ignore capitalization
            inputs("create event test on 2024-02-29 repeats " + input + " FOR"),
            // expected is number of repeats after 'for', throw an error
            // should ignore capitalization, return same error as test above
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInputNonNumberAfterFor() throws CalendarException {
    String input = "mwf"; // mon, wed, fri
    String errorMessage = "Missing <N> after 'for'.\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // input valid weekday string
            // should ignore capitalization
            inputs("create event test on 2024-02-29 repeats " + input + " for j"),
            // expected is a number for <N>, will throw error if given a non-number
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInputOnlyNumberAfterFor() throws CalendarException {
    String input = "mwf"; // mon, wed, fri
    String errorMessage = "Missing 'times' after 'for <N>'.\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // input valid weekday string
            // should ignore capitalization
            inputs("create event test on 2024-02-29 repeats " + input + " for 5"),
            // expected 'times' after number, throw an error
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInvalidInputAfterNumber() throws CalendarException {
    String input = "mwf"; // mon, wed, fri
    String errorMessage = "Missing 'times' after 'for <N>'.\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // input valid weekday string
            // should ignore capitalization
            inputs("create event test on 2024-02-29 repeats " + input + " for 5 test"),
            // expected 'times' after number, throw an error
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
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
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // input valid weekday string
            inputs("create event test on " + dateString + " repeats " + days + " for " + repeats +
                    " times"),
            // given valid input, should successfully create a event series
            prints(getSuccessfulForSeriesMessage("test", date, startTime, endTime, daysOfWeek,
                    repeats)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
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
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // input valid weekday string
            inputs("create event " + subject + " on " + dateString + " repeats " + days + " for "
                    + repeats + " times\n"),
            // given valid input, should successfully create a event series
            prints(getSuccessfulForSeriesMessage("test", date, startTime, endTime,
                    daysOfWeek, repeats)),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test on " + dateString + " repeats " + days + " for " + repeats +
                    " times"),
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
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
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // input valid weekday string
            inputs("create event " + subject + " on " + dateString + " repeats " + days + " for "
                    + repeats + " times\n"),
            // given valid input, should successfully create a event series
            prints(getSuccessfulForSeriesMessage("test", date, startTime, endTime,
                    daysOfWeek, repeats)),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test on 2024-02-29"),
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInputOnlyUntilAfterDays() throws CalendarException {
    String input = "mwf"; // mon, wed, fri
    String errorMessage = "Missing <dateString>.\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // input valid weekday string
            // should ignore capitalization
            inputs("create event test on 2024-02-29 repeats " + input + " until"),
            // expected is end date after 'until', throw an error
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInputOnlyUntilCapitalizedAfterDays() throws CalendarException {
    String input = "mwf"; // mon, wed, fri
    String errorMessage = "Missing <dateString>.\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // input valid weekday string
            // should ignore capitalization
            inputs("create event test on 2024-02-29 repeats " + input + " UNTIL"),
            // expected is end date after 'until', throw an error
            // should ignore capitalization, return same error as test above
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInvalidInputAfterUntil() throws CalendarException {
    String input = "mwf"; // mon, wed, fri
    String errorMessage = "Invalid date format for <dateString>. Expected YYYY-MM-DD\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // input valid weekday string
            // should ignore capitalization
            inputs("create event test on 2024-02-29 repeats " + input + " until test"),
            // given invalid input after 'until', throw error
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInvalidDateAfterUntil() throws CalendarException {
    String input = "mwf"; // mon, wed, fri
    String errorMessage = "Invalid date format for <dateString>. Expected YYYY-MM-DD\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // input valid weekday string
            // should ignore capitalization
            inputs("create event test on 2024-02-29 repeats " + input + " until 2025-02-29"),
            // given invalid date, throw error
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInputDateTimeAfterUntil() throws CalendarException {
    String input = "mwf"; // mon, wed, fri
    String errorMessage = "Invalid date format for <dateString>. Expected YYYY-MM-DD\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // input valid weekday string
            // should ignore capitalization
            inputs("create event test on 2024-02-29 repeats " + input + " until 2024-05-05T08:00"),
            // given LocalDateTime instead of LocalDate after 'until', throw error
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testEndDateBeforeStartDateAfterUntil() throws CalendarException {
    String input = "mwf"; // mon, wed, fri
    String errorMessage = "End date cannot be before start date\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // input valid weekday string
            // should ignore capitalization
            inputs("create event test on 2024-02-29 repeats " + input + " until 2024-02-28"),
            // end date is before start date, throw error
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testValidInputAfterUntil() throws CalendarException {
    String days = "mwf"; // mon, wed, fri
    Set<Days> daysOfWeek = new HashSet<Days>();
    char[] chars = days.toCharArray();
    for (char c : chars) {
      daysOfWeek.add(Days.fromSymbol(c));
    }
    String startDateString = "2024-02-29";
    String endDateString = "2024-05-05";
    LocalDate startDate = LocalDate.parse(startDateString);
    LocalDate endDate = LocalDate.parse(endDateString);
    LocalTime startTime = LocalTime.of(8, 0);
    LocalTime endTime = LocalTime.of(17, 0);
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test on " + startDateString + " repeats " + days + " until " +
                    endDateString),
            // given valid input, should successfully create an event series
            prints(getSuccessfulUntilSeriesMessage("test", startDate, startTime, endTime,
                    daysOfWeek, endDate)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testDuplicateEventAfterEndDate() throws CalendarException {
    String subject = "test";
    String errorMessage = "Event already exists\n";
    String days = "mwrf"; // mon, wed, thur, fri
    int repeats = 5;
    Set<Days> daysOfWeek = new HashSet<Days>();
    char[] chars = days.toCharArray();
    for (char c : chars) {
      daysOfWeek.add(Days.fromSymbol(c));
    }
    String startDateString = "2024-02-29";
    String endDateString = "2024-05-05";
    LocalDate startDate = LocalDate.parse(startDateString);
    LocalDate endDate = LocalDate.parse(endDateString);
    LocalTime startTime = LocalTime.of(8, 0);
    LocalTime endTime = LocalTime.of(17, 0);
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // input valid weekday string
            inputs("create event test on " + startDateString + " repeats " + days + " until " +
                    endDateString + "\n"),
            // given valid input, should successfully create a event series
            prints(getSuccessfulUntilSeriesMessage("test", startDate, startTime, endTime,
                    daysOfWeek, endDate)),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test on 2024-02-29"),
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInputOnlyFromAfterSubject() throws CalendarException {
    String errorMessage = "Missing <dateStringTtimeString>\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test from"), // input 'from' keyword without date
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInvalidInputAfterFrom() throws CalendarException {
    String errorMessage = "Invalid date format for <dateStringTtimeString>. " +
            "Expected YYYY-MM-DDThh:mm\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test from test"), // input invalid date
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInputDateAfterFrom() throws CalendarException {
    String errorMessage = "Invalid date format for <dateStringTtimeString>. " +
            "Expected YYYY-MM-DDThh:mm\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test from 2024-11-12"), // input date instead of datetime
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInputInvalidMinutesAfterFrom() throws CalendarException {
    String errorMessage = "Invalid date format for <dateStringTtimeString>. " +
            "Expected YYYY-MM-DDThh:mm\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test from 2024-11-12T00:61"), // input mm as 61
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInputInvalidHoursAfterFrom() throws CalendarException {
    String errorMessage = "Invalid date format for <dateStringTtimeString>. " +
            "Expected YYYY-MM-DDThh:mm\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test from 2024-11-12T24:00"), // input hh as 24
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInputInvalidDateTimeAfterFrom() throws CalendarException {
    String errorMessage = "Invalid date format for <dateStringTtimeString>. " +
            "Expected YYYY-MM-DDThh:mm\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test from 2024-11-31T00:00"), // input nov 31
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testNoInputAfterDateTime() throws CalendarException {
    String errorMessage = "Missing 'to' keyword.\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test from 2024-11-12T00:00"), // input valid date
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInvalidInputAfterDateTime() throws CalendarException {
    String errorMessage = "Missing 'to' keyword.\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test from 2024-11-12T00:00 test"), // not 'to', throw error
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testNoInputAfterTo() throws CalendarException {
    String errorMessage = "Missing <dateStringTtimeString>\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test from 2024-11-12T00:00 to"), // no input after 'to', error
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInputToCapitalized() throws CalendarException {
    String errorMessage = "Missing <dateStringTtimeString>\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // should ignore capitalization, return same error as above
            inputs("create event test from 2024-11-12T00:00 TO"),
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInvalidInputAfterTo() throws CalendarException {
    String errorMessage = "Invalid date format for <dateStringTtimeString>. " +
            "Expected YYYY-MM-DDThh:mm\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test from 2024-11-12T00:00 to test"),
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInputDateTimeAfterTo() throws CalendarException {
    String errorMessage = "Invalid date format for <dateStringTtimeString>. " +
            "Expected YYYY-MM-DDThh:mm\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test from 2024-11-12T00:00 to 2024-11-13"), // input date, error
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testEndDateBeforeStartDateAfterTo() throws CalendarException {
    String errorMessage = "End date must be after start date\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // input end date that is before start date
            inputs("create event test from 2024-11-12T00:00 to 2024-11-11T00:00"),
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testEndDateTimeBeforeStartDateTimeAfterTo() throws CalendarException {
    String errorMessage = "End date must be after start date\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // input same day, earlier hour for end date
            inputs("create event test from 2024-11-12T01:00 to 2024-11-12T00:00"),
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInvalidEndDateTimeAfterTo() throws CalendarException {
    String errorMessage = "Invalid date format for <dateStringTtimeString>. " +
            "Expected YYYY-MM-DDThh:mm\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test from 2024-11-12T00:00 to 2024-11-31T00:00"), // input nov 31
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testValidEndDateTimeAfterTo() throws CalendarException {
    String startDateTimeString = "2024-11-12T00:00";
    String endDateTimeString = "2024-11-13T00:00";
    LocalDateTime startDateTime = LocalDateTime.parse(startDateTimeString);
    LocalDateTime endDateTime = LocalDateTime.parse(endDateTimeString);
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test from " + startDateTimeString + " to " + endDateTimeString),
            // date is valid, print success message
            prints(getSuccessfulEventMessage("test", startDateTime, endDateTime)),
            // prompt user for next command
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testValidEndDateTimeAfterTo2() throws CalendarException {
    String startDateTimeString = "2024-11-12T00:00";
    String endDateTimeString = "2024-11-12T01:00"; // same day, different time
    LocalDateTime startDateTime = LocalDateTime.parse(startDateTimeString);
    LocalDateTime endDateTime = LocalDateTime.parse(endDateTimeString);
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test from " + startDateTimeString + " to " + endDateTimeString),
            // date is valid, print success message
            prints(getSuccessfulEventMessage("test", startDateTime, endDateTime)),
            // prompt user for next command
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testMinDateToMaxDate() throws CalendarException {
    String startDateTimeString = "2024-11-12T00:00";
    String endDateTimeString = "2024-11-13T00:00";
    LocalDateTime startDateTime = LocalDateTime.parse(startDateTimeString);
    LocalDateTime endDateTime = LocalDateTime.parse(endDateTimeString);
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test from " + startDateTimeString + " to " + endDateTimeString),
            // date is valid, print success message
            prints(getSuccessfulEventMessage("test", startDateTime, endDateTime)),
            // prompt user for next command
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInvalidInputAfterToDate() throws CalendarException {
    String errorMessage = "Expected 'repeats' or end of command for single timed event.\n";
    String startDateTimeString = "2024-11-12T00:00";
    String endDateTimeString = "2024-11-12T01:00"; // same day, different time
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test from " + startDateTimeString + " to " + endDateTimeString
            + " test"), // invalid input 'test' after end date
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInputRepeatsAfterToDate() throws CalendarException {
    String errorMessage = "Missing days to repeat\n";
    String startDateTimeString = "2024-11-12T00:00";
    String endDateTimeString = "2024-11-12T01:00"; // same day, different time
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test from " + startDateTimeString + " to " + endDateTimeString
                    + " repeats"), // no input after 'repeats', throw error
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testEventSeriesLongerThanOneDayAfterRepeats() throws CalendarException {
    String errorMessage = "Each event in a series can only last one day\n";
    String startDateTimeString = "2024-11-12T00:00";
    String endDateTimeString = "2024-11-13T01:00"; // end date goes overnight
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test from " + startDateTimeString + " to " + endDateTimeString
                    + " repeats"), // each event goes overnight, throw error
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInvalidInputAfterRepeats() throws CalendarException {
    String errorMessage = "Invalid weekday symbol: X\n";
    String startDateTimeString = "2024-11-12T00:00";
    String endDateTimeString = "2024-11-12T01:00";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test from " + startDateTimeString + " to " + endDateTimeString
                    + " repeats XYZ"), // input invalid days string
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInvalidDaysWithValidDaysAfterRepeats() throws CalendarException {
    String errorMessage = "Invalid weekday symbol: X\n";
    String startDateTimeString = "2024-11-12T00:00";
    String endDateTimeString = "2024-11-12T01:00";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test from " + startDateTimeString + " to " + endDateTimeString
                    + " repeats MWFX"), // input invalid days string
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInputValidDaysAfterRepeats() throws CalendarException {
    String input = "MWF"; // mon, wed, fri
    String startDateTimeString = "2024-11-12T00:00";
    String endDateTimeString = "2024-11-12T01:00";
    String errorMessage = "Missing 'for' or 'until'.\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // input valid weekday string
            inputs("create event test from " + startDateTimeString + " to " + endDateTimeString
                    + " repeats " + input),
            // expected is 'from' or 'until' after, throw error
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInputForAfterDays() throws CalendarException {
    String input = "MWF"; // mon, wed, fri
    String startDateTimeString = "2024-11-12T00:00";
    String endDateTimeString = "2024-11-12T01:00";
    String errorMessage = "Missing <N> after 'for'.\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // input valid weekday string
            inputs("create event test from " + startDateTimeString + " to " + endDateTimeString
                    + " repeats " + input + " for"),
            // expected <N> after 'for', throw error
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testNonNumberAfterFor() throws CalendarException {
    String input = "MWF"; // mon, wed, fri
    String startDateTimeString = "2024-11-12T00:00";
    String endDateTimeString = "2024-11-12T01:00";
    String errorMessage = "Missing <N> after 'for'.\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // input valid weekday string
            inputs("create event test from " + startDateTimeString + " to " + endDateTimeString
                    + " repeats " + input + " for x"),
            // inputted non-number, throw error
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testValidNumberAfterFor() throws CalendarException {
    String input = "MWF"; // mon, wed, fri
    String startDateTimeString = "2024-11-12T00:00";
    String endDateTimeString = "2024-11-12T01:00";
    String errorMessage = "Missing 'times' after 'for <N>'.\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // input valid weekday string
            inputs("create event test from " + startDateTimeString + " to " + endDateTimeString
                    + " repeats " + input + " for 5"),
            // inputted number but missing 'times', throw error
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInvalidInputAfterNumber2() throws CalendarException {
    String input = "MWF"; // mon, wed, fri
    String startDateTimeString = "2024-11-12T00:00";
    String endDateTimeString = "2024-11-12T01:00";
    String errorMessage = "Missing 'times' after 'for <N>'.\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // input valid weekday string
            inputs("create event test from " + startDateTimeString + " to " + endDateTimeString
                    + " repeats " + input + " for 5 test"),
            // expected 'times' but given 'test', throw error
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testValidInputAfterNumber2() throws CalendarException {
    String days = "mwf"; // mon, wed, fri
    int repeats = 5;
    Set<Days> daysOfWeek = new HashSet<Days>();
    char[] chars = days.toCharArray();
    for (char c : chars) {
      daysOfWeek.add(Days.fromSymbol(c));
    }
    String startDateTimeString = "2024-11-12T00:00";
    String endDateTimeString = "2024-11-12T01:00";
    LocalDateTime startDateTime = LocalDateTime.parse(startDateTimeString);
    LocalDate startDate = startDateTime.toLocalDate();
    LocalTime startTime = LocalTime.of(0, 0);
    LocalTime endTime = LocalTime.of(1, 0);
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // input valid weekday string
            inputs("create event test from " + startDateTimeString + " to " + endDateTimeString
                    + " repeats " + days + " for 5 times"),
            // given valid input, should successfully create an event series
            prints(getSuccessfulForSeriesMessage("test", startDate, startTime, endTime,
                    daysOfWeek, repeats)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testDuplicateEventAfterSeries() throws CalendarException {
    String errorMessage = "Event already exists\n";
    String days = "mwft"; // mon, wed, fri, tue
    int repeats = 5;
    Set<Days> daysOfWeek = new HashSet<Days>();
    char[] chars = days.toCharArray();
    for (char c : chars) {
      daysOfWeek.add(Days.fromSymbol(c));
    }
    String startDateTimeString = "2024-11-12T00:00";
    String endDateTimeString = "2024-11-12T01:00";
    LocalDateTime startDateTime = LocalDateTime.parse(startDateTimeString);
    LocalDate startDate = startDateTime.toLocalDate();
    LocalTime startTime = LocalTime.of(0, 0);
    LocalTime endTime = LocalTime.of(1, 0);
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // input valid weekday string
            inputs("create event test from " + startDateTimeString + " to " + endDateTimeString
                    + " repeats " + days + " for 5 times\n"),
            // given valid input, should successfully create a event series
            prints(getSuccessfulForSeriesMessage("test", startDate, startTime, endTime,
                    daysOfWeek, repeats)),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test from " + startDateTimeString + " to " + endDateTimeString),
            // event already exists, throw an error
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testDuplicateSeriesAfterSeries() throws CalendarException {
    String errorMessage = "Duplicate event in series: test\n";
    String days = "mwft"; // mon, wed, fri, tue
    int repeats = 5;
    Set<Days> daysOfWeek = new HashSet<Days>();
    char[] chars = days.toCharArray();
    for (char c : chars) {
      daysOfWeek.add(Days.fromSymbol(c));
    }
    String startDateTimeString = "2024-11-12T00:00";
    String endDateTimeString = "2024-11-12T01:00";
    LocalDateTime startDateTime = LocalDateTime.parse(startDateTimeString);
    LocalDate startDate = startDateTime.toLocalDate();
    LocalTime startTime = LocalTime.of(0, 0);
    LocalTime endTime = LocalTime.of(1, 0);
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // input valid weekday string
            inputs("create event test from " + startDateTimeString + " to " + endDateTimeString
                    + " repeats " + days + " for 5 times\n"),
            // given valid input, should successfully create a event series
            prints(getSuccessfulForSeriesMessage("test", startDate, startTime, endTime,
                    daysOfWeek, repeats)),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test from " + startDateTimeString + " to " + endDateTimeString
                    + " repeats " + days + " for 5 times"),
            // event series already exists, throw error
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInputUntilAfterDays() throws CalendarException {
    String input = "MWF"; // mon, wed, fri
    String startDateTimeString = "2024-11-12T00:00";
    String endDateTimeString = "2024-11-12T01:00";
    String errorMessage = "Missing <dateString>.\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // input valid weekday string
            inputs("create event test from " + startDateTimeString + " to " + endDateTimeString
                    + " repeats " + input + " until"),
            // expected <N> after 'for', throw error
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInvalidInputAfterUntil2() throws CalendarException {
    String input = "MWF"; // mon, wed, fri
    String startDateTimeString = "2024-11-12T00:00";
    String endDateTimeString = "2024-11-12T01:00";
    String errorMessage = "Invalid date format for <dateString>. Expected YYYY-MM-DD\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // input valid weekday string
            inputs("create event test from " + startDateTimeString + " to " + endDateTimeString
                    + " repeats " + input + " until test"),
            // given 'test' instead of date, throw error
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInvalidDateAfterUntil2() throws CalendarException {
    String input = "MWF"; // mon, wed, fri
    String startDateTimeString = "2024-11-12T00:00";
    String endDateTimeString = "2024-11-12T01:00";
    String errorMessage = "Invalid date format for <dateString>. Expected YYYY-MM-DD\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // input valid weekday string
            inputs("create event test from " + startDateTimeString + " to " + endDateTimeString
                    + " repeats " + input + " until 2025-02-29"),
            // given invalid date, throw error
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testInputDateTimeAfterUntil2() throws CalendarException {
    String input = "MWF"; // mon, wed, fri
    String startDateTimeString = "2024-11-12T00:00";
    String endDateTimeString = "2024-11-12T01:00";
    String errorMessage = "Invalid date format for <dateString>. Expected YYYY-MM-DD\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // input valid weekday string
            inputs("create event test from " + startDateTimeString + " to " + endDateTimeString
                    + " repeats " + input + " until 2024-11-13T00:00"),
            // given invalid date, throw error
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testEventSeriesLongerThanOneDayAfterUntil() throws CalendarException {
    String input = "MWF"; // mon, wed, fri
    String errorMessage = "Each event in a series can only last one day\n";
    String startDateTimeString = "2024-11-12T00:00";
    String endDateTimeString = "2024-11-13T01:00"; // end date goes overnight
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test from " + startDateTimeString + " to " + endDateTimeString
                    + " repeats " + input + " until 2024-11-13"),
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testEndDateBeforeStartDate() throws CalendarException {
    String input = "MWF"; // mon, wed, fri
    String errorMessage = "End date cannot be before start date\n";
    String startDateTimeString = "2024-11-12T00:00";
    String endDateTimeString = "2024-11-12T01:00"; // end date goes overnight
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test from " + startDateTimeString + " to " + endDateTimeString
                    + " repeats " + input + " until 2024-11-11"),
            // end date is before start date
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testValidEndDateAfterUntil() throws CalendarException {
    String input = "MWF"; // mon, wed, fri
    String startDateTimeString = "2024-11-12T00:00";
    String endDateTimeString = "2024-11-12T01:00";
    String endDateString = "2024-11-15";
    LocalDate endDate = LocalDate.parse(endDateString);
    LocalDateTime startDateTime = LocalDateTime.parse(startDateTimeString);
    LocalDateTime endDateTime = LocalDateTime.parse(endDateTimeString);
    LocalDate startDate = startDateTime.toLocalDate();
    LocalTime startTime = startDateTime.toLocalTime();
    LocalTime endTime = endDateTime.toLocalTime();
    Set<Days> daysOfWeek = new HashSet<Days>();
    char[] chars = input.toCharArray();
    for (char c : chars) {
      daysOfWeek.add(Days.fromSymbol(c));
    }
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test from " + startDateTimeString + " to " + endDateTimeString
                    + " repeats " + input + " until " + endDateString),
            // end date is before start date
            prints(getSuccessfulUntilSeriesMessage("test", startDate, startTime,
                    endTime, daysOfWeek, endDate)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testPrintOnDateWithNoEvents() throws CalendarException {
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("print events on 2025-10-10\n"),
            prints(getNoEventsFoundMessage(LocalDate.of(2025, 10, 10))),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testPrintOnDateWithOneEvent() throws CalendarException {
    String subject = "test";
    LocalDate date = LocalDate.of(2025, 11, 11);
    // Expected output string for one event
    String eventListOutput = "Event 'test' on 2025-11-11 from 08:00 to 17:00"
            + System.lineSeparator();

    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test on 2025-11-11\n"),
            prints(getSuccessfulAllDayMessage(subject, date)),
            prints(getExpectedEnterCommandPrompt()),
            inputs("print events on 2025-11-11\n"),
            prints(getPrintOnDateMessage(date, eventListOutput)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testPrintOnInvalidDate() throws CalendarException {
    String errorMessage = "Invalid date format for <dateString>. Expected YYYY-MM-DD\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("print events on 2025/11/11\n"),
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testPrintWithMissingArgs() throws CalendarException {
    String errorMessage = "Missing input after 'events'.\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("print events\n"),
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testPrintMissingEventsKeyword() throws CalendarException {
    String errorMessage = "Expected 'events' after 'print'.\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("print users\n"),
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testPrintEventsInRange() throws CalendarException {
    String event1 = "Event 'Event 1' on 2025-07-04 from 10:00 to 11:00" + System.lineSeparator();
    String event2 = "Event 'Event 2' on 2025-07-05 from 12:00 to 13:00" + System.lineSeparator();

    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event Event 1 from 2025-07-04T10:00 to 2025-07-04T11:00\n"),
            prints(getSuccessfulEventMessage("Event 1", LocalDateTime.of(2025,7,
                            4,10,0),
                    LocalDateTime.of(2025,7,4,11,0))),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event Event 2 from 2025-07-05T12:00 to 2025-07-05T13:00\n"),
            prints(getSuccessfulEventMessage("Event 2", LocalDateTime.of(2025,7,
                            5,12,0),
                    LocalDateTime.of(2025,7,5,13,0))),
            prints(getExpectedEnterCommandPrompt()),
            inputs("print events from 2025-07-04T09:00 to 2025-07-05T14:00\n"),
            prints("Printing events from 2025-07-04T09:00 to 2025-07-05T14:00." +
                    System.lineSeparator() + event1 + event2),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testPrintEventsInRangeInvalidFormat() throws CalendarException {
    String errorMessage = "Invalid date format for <dateStringTtimeString>. Expected " +
            "YYYY-MM-DDThh:mm\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("print events from 2025-07-04 to 2025-07-05\n"), // Using date, not datetime
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testPrintEventsInRangeMissingArgs() throws CalendarException {
    String errorMessage = "Missing input after <dateStringTTimeString>.\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("print events from 2025-07-04T09:00\n"),
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testShowStatusWhenAvailable() throws CalendarException {
    LocalDateTime time = LocalDateTime.of(2025, 8, 8, 10, 0);
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("show status on 2025-08-08T10:00\n"),
            prints(getBusyStatusMessage(time, false)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testShowStatusWhenBusy() throws CalendarException {
    String subject = "test";
    LocalDate date = LocalDate.of(2025, 8, 8);
    LocalDateTime time = LocalDateTime.of(2025, 8, 8, 10, 0);
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test on 2025-08-08\n"),
            prints(getSuccessfulAllDayMessage(subject, date)),
            prints(getExpectedEnterCommandPrompt()),
            inputs("show status on 2025-08-08T10:00\n"),
            prints(getBusyStatusMessage(time, true)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testShowStatusAtEventEndTime() throws CalendarException {
    // Should be available at the exact end time (interval is [start, end))
    String subject = "test";
    LocalDate date = LocalDate.of(2025, 8, 9);
    LocalDateTime time = LocalDateTime.of(2025, 8, 9, 17, 0);
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test on 2025-08-09\n"),
            prints(getSuccessfulAllDayMessage(subject, date)),
            prints(getExpectedEnterCommandPrompt()),
            inputs("show status on 2025-08-09T17:00\n"),
            prints(getBusyStatusMessage(time, false)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testShowMissingStatus() throws CalendarException {
    String errorMessage = "Expected 'status' after 'show'.\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("show availability on 2025-01-01T10:00\n"),
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testShowStatusMissingOn() throws CalendarException {
    String errorMessage = "Expected 'on' after 'status'.\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("show status at 2025-01-01T10:00\n"),
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testShowStatusMissingDateTime() throws CalendarException {
    String errorMessage = "Missing <dateStringTtimeString>\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("show status on\n"),
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()),
            inputs("q\n"));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testEditCommandMissingEverything() throws CalendarException {
    String errorMessage = "Missing 'event' keyword after 'create'.\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("edit\n"),
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testEditCommandMissingProperty() throws CalendarException {
    String errorMessage = "Missing event property.\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("edit event\n"),
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testEditCommandInvalidProperty() throws CalendarException {
    String errorMessage = "Invalid property: fake_prop, valid values are: [SUBJECT, START, END, " +
            "DESCRIPTION, LOCATION, STATUS]\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("edit event fake_prop\n"),
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testEditCommandMissingSubject() throws CalendarException {
    String errorMessage = "Missing event subject\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("edit event subject\n"),
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testEditCommandMissingFromKeyword() throws CalendarException {
    String errorMessage = "Incomplete command, missing <dateStringTtimeString>.\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("edit event subject some_event\n"),
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testEditCommandMissingNewValue() throws CalendarException {
    String errorMessage = "Missing <NewPropertyValue>.\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test from 2025-01-01T10:00 to 2025-01-01T11:00\n"),
            prints(getSuccessfulEventMessage("test", LocalDateTime.of(2025,1,
                            1,10,0),
                    LocalDateTime.of(2025,1,1,11,0))),
            prints(getExpectedEnterCommandPrompt()),
            inputs("edit events subject test from 2025-01-01T10:00 with\n"),
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testEditStandaloneEventSubject() throws CalendarException {
    String subject = "test";
    LocalDate date = LocalDate.of(2025, 1, 1);
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test on 2025-01-01\n"),
            prints(getSuccessfulAllDayMessage(subject, date)),
            prints(getExpectedEnterCommandPrompt()),
            inputs("edit event subject test from 2025-01-01T08:00 to " +
                    "2025-01-01T17:00 with new_subject\n"),
            prints(getSuccessfulEditMessage(subject, "subject", "new_subject")),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testEditStandaloneEventSubjectUsingEvents() throws CalendarException {
    String subject = "test";
    LocalDate date = LocalDate.of(2025, 1, 1);
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test on 2025-01-01\n"),
            prints(getSuccessfulAllDayMessage(subject, date)),
            prints(getExpectedEnterCommandPrompt()),
            // use 'events' instead of 'event', should return the same thing
            inputs("edit events subject test from 2025-01-01T08:00 with new_subject\n"),
            prints(getSuccessfulEditMessage(subject, "subject", "new_subject")),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testEditEventNotFound() throws CalendarException {
    String errorMessage = "No events found.\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("edit event subject fake from 2025-01-01T08:00 to 2025-01-01T17:00 with new\n"),
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testEditEventCausesDuplicateConflict() throws CalendarException {
    String errorMessage = "Event conflicts with existing event\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event eventA on 2025-02-02\n"), // 8am-5pm
            prints(getSuccessfulAllDayMessage("eventA", LocalDate.of(2025, 2,
                    2))),
            prints(getExpectedEnterCommandPrompt()),
            // Now create a different event to edit
            inputs("create event eventB from 2025-02-02T08:00 to 2025-02-02T17:00\n"),
            prints("Event 'eventB' created from 2025-02-02T08:00 to 2025-02-02T17:00.\n"),
            prints(getExpectedEnterCommandPrompt()),
            // Try to edit eventC to conflict with eventA
            inputs("edit event subject eventB from 2025-02-02T08:00 to 2025-02-02T17:00 with " +
                    "eventA\n"),
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testEditSeriesAllInstances() throws CalendarException {
    String successCreate = "Event series 'meeting' created on 2025-03-01 from 09:00 to 10:00 "
            + "repeating on [MONDAY] 2 times." + System.lineSeparator();
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event meeting from 2025-03-01T09:00 to 2025-03-01T10:00 " +
                    "repeats M for 2 times\n"),
            prints(successCreate),
            prints(getExpectedEnterCommandPrompt()),
            // Edit all instances of the series
            inputs("edit series subject meeting from 2025-03-03T09:00 with new_meeting\n"),
            prints(getSuccessfulSeriesEditMessage("meeting", "subject",
                    "new_meeting")),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testEditEventsThisAndFuture() throws CalendarException {
    String successCreate = "Event series 'review' created on 2025-04-01 from 14:00 to 15:00 "
            + "repeating on [TUESDAY] 4 times." + System.lineSeparator();
    LocalDateTime firstInstance = LocalDateTime.of(2025, 4, 1, 14,
            0);
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event review from 2025-04-01T14:00 to 2025-04-01T15:00 " +
                    "repeats T for 4 times\n"),
            prints(successCreate),
            prints(getExpectedEnterCommandPrompt()),
            // Edit the series starting from the first instance
            inputs("edit events subject review from 2025-04-01T14:00 with sprint_review\n"),
            prints(getSuccessfulSeriesFromDateEditMessage("review", "subject",
                    "sprint_review",
                    firstInstance)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testEditWithAmbiguousFind() throws CalendarException {
    String errorMessage = "Error: multiple events found.\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // Create two events with same subject and start, but different end
            inputs("create event ambiguous from 2025-05-05T10:00 to 2025-05-05T11:00\n"),
            prints("Event 'ambiguous' created from 2025-05-05T10:00 to 2025-05-05T11:00.\n"),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event ambiguous from 2025-05-05T10:00 to 2025-05-05T12:00\n"),
            prints("Event 'ambiguous' created from 2025-05-05T10:00 to 2025-05-05T12:00.\n"),
            prints(getExpectedEnterCommandPrompt()),
            // Try to edit using only subject and start time, which is ambiguous
            inputs("edit events subject ambiguous from 2025-05-05T10:00 with new_subject\n"),
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testEditWithOnlyKeyword() throws CalendarException {
    String errorMessage = "Missing 'event' keyword after 'create'.\n"; // Your parser throws this
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("edit\n"),
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testEditEventMissingProperty() throws CalendarException {
    String errorMessage = "Missing event property.\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("edit event\n"),
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testEditEventInvalidProperty() throws CalendarException {
    String errorMessage = "Invalid property: color, valid values are: " +
            "[SUBJECT, START, END, DESCRIPTION, LOCATION, STATUS]\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("edit event color\n"),
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testEditEventMissingSubject() throws CalendarException {
    String errorMessage = "Missing event subject\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("edit event subject\n"),
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testEditEventMissingFrom() throws CalendarException {
    String errorMessage = "Incomplete command, missing <dateStringTtimeString>.\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("edit event subject Some Event\n"),
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testEditEventMissingTo() throws CalendarException {
    String errorMessage = "Missing 'to' after from <dateStringTTimeString>.\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("edit event subject Some Event from 2025-01-01T10:00\n"),
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testEditEventMissingWith() throws CalendarException {
    String errorMessage = "Expected 'with' after 'to <dateStringTTimeString>.\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("edit event subject Some Event from 2025-01-01T10:00 to " +
                    "2025-01-01T11:00 for new_value\n"),
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testEditEventMissingNewValue() throws CalendarException {
    String errorMessage = "Missing <NewPropertyValue>.\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test from 2025-01-01T10:00 to 2025-01-01T11:00\n"),
            prints(getSuccessfulEventMessage("test", LocalDateTime.of(2025, 1,
                            1, 10, 0),
                    LocalDateTime.of(2025, 1, 1, 11, 0))),
            prints(getExpectedEnterCommandPrompt()),
            inputs("edit event subject test from 2025-01-01T10:00 to 2025-01-01T11:00 with"),
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }

  @Test
  public void testEditEventStartAfterEnd() throws CalendarException {
    String errorMessage = "New start time cannot be after current end time\n";
    String[] array = testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event original on 2025-04-04\n"),
            prints(getSuccessfulAllDayMessage("original", LocalDate.of(2025, 4,
                    4))),
            prints(getExpectedEnterCommandPrompt()),
            // Try to edit start time to be after the 17:00 end time
            inputs("edit event start original from 2025-04-04T08:00 to 2025-04-04T17:00 with " +
                    "2025-04-04T18:00\n"),
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
    assertEquals(array[0], array[1]);
  }
}






