package calendar.controller;

import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import calendar.CalendarException;
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
    String errorMessage = "Missing <dateString> after 'on'.\n";
    testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            inputs("create event test on"), // input 'on' keyword without date
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
  }

  @Test
  public void testInputOnlyOnAfterSubjectWithMultipleWords() throws CalendarException {
    String errorMessage = "Missing <dateString> after 'on'.\n";
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
    String errorMessage = "Missing <dateString> after 'on'.\n";
    testRun(model,
            prints(getExpectedFullMenuOutput()),
            prints(getExpectedEnterCommandPrompt()),
            // input 'on' keyword without date in all capital letters
            // controller should disregard capital letters
            inputs("create event test ON"),
            prints(getErrorMessage(errorMessage)),
            prints(getExpectedEnterCommandPrompt()));
  }
}





