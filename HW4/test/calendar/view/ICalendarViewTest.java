package calendar.view;

import calendar.model.Event;
import calendar.model.EventStatus;
import calendar.model.Location;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * JUnit 4 test class for the CalendarView.java,
 * which implements the ICalendarView interface.
 */
public class ICalendarViewTest {

  private StringWriter testOutput;
  private CalendarView view;

  /**
   * Sets up the test environment before each test method.
   * Initializes a new StringWriter to capture output and a new CalendarView
   * instance with the StringWriter.
   */
  @Before
  public void setUp() {
    testOutput = new StringWriter();
    view = new CalendarView(testOutput);
  }


  /**
   * Tests the {@code showMenu} method to ensure it prints the correct menu
   * content to the output.
   */
  @Test
  public void testShowMenu() {
    view.showMenu();
    String expectedOutput =
            "Welcome to the calendar program!" + System.lineSeparator() +
                    "Supported user instructions are: " + System.lineSeparator()
                    + "create event <eventSubject> from <dateStringTtimeString> to "
                    + "<dateStringTtimeString> (Create a singular event)" + System.lineSeparator()
                    + "create event <eventSubject> from <dateStringTtimeString> to "
                    + "<dateStringTtimeString> repeats <weekdays> for <N> times "
                    + "(Creates an event series that repeats N times on specific weekdays)"
                    + System.lineSeparator() + "create event <eventSubject> from " +
                    "<dateStringTtimeString> to "
                    + "<dateStringTtimeString> repeats <weekdays> until <dateString> "
                    + "(Creates an event series until a specific date (inclusive))"
                    + System.lineSeparator() +
                    "create event <eventSubject> on <dateString> " +
                    "(Creates a single all day event.)" + System.lineSeparator() +
                    "create event <eventSubject> on <dateString> repeats <weekdays> for <N> times" +
                    "(Creates a series of all day events that repeats N times on specific weekdays)"
                    + System.lineSeparator() +
                    "create event <eventSubject> on <dateString> repeats <weekdays> until " +
                    "<dateString>" + "(Creates a series of all day events until a specific date " +
                    "(inclusive)." + System.lineSeparator() +
                    "edit event <property> <eventSubject> from <dateStringTtimeString> to " +
                    "<dateStringTtimeString> with <NewPropertyValue>" +
                    "(Changes the property of the given event)." + System.lineSeparator() +
                    "edit events <property> <eventSubject> from <dateStringTtimeString> " +
                    "with <NewPropertyValue>" +
                    "(Identify the event that has the given subject and starts at the given date " +
                    "and time and edit its property. If this event is part of a series then the " +
                    "properties of all events in that series that start at or after the given date "
                    + "and time is changed)." + System.lineSeparator() +
                    "edit series <property> <eventSubject> from <dateStringTtimeString> " +
                    "with <NewPropertyValue>" +
                    "(Identify the event that has the given subject and starts " +
                    "at the given date and " +
                    "time and edit its property. If this event is part of a series " +
                    "then the properties " +
                    "of all events in that series is changed)." + System.lineSeparator() +
                    "print events on <dateString>" +
                    "(Prints a bulleted list of all events on that day along with " +
                    "their start and " +
                    "end time and location (if any))." + System.lineSeparator() +
                    "print events from <dateStringTtimeString> to <dateStringTtimeString>" +
                    "(Prints a bulleted list of all events in the given interval including " +
                    "their start " +
                    "and end times and location (if any))." + System.lineSeparator() +
                    "show status on <dateStringTtimeString>" +
                    "(Prints busy status if the user has events scheduled on a given day " +
                    "and time, " +
                    "otherwise, available)." + System.lineSeparator() +
                    "menu (Print supported instruction list)" + System.lineSeparator() +
                    "q or quit (quit the program) " + System.lineSeparator();

    assertEquals(expectedOutput, testOutput.toString());
  }

  /**
   * Tests the {@code showMenu} method again, ensuring the exact content
   * and formatting are as expected. This test is redundant with {@link #testShowMenu()}
   * but kept for demonstration.
   */
  @Test
  public void testShowMenu_ExactContent() {
    view.showMenu();
    String expected = "Welcome to the calendar program!"
            + System.lineSeparator()
            + "Supported user instructions are: " + System.lineSeparator()
            + "create event <eventSubject> from <dateStringTtimeString> to "
            + "<dateStringTtimeString> (Create a singular event)"
            + System.lineSeparator()
            + "create event <eventSubject> from <dateStringTtimeString> to "
            + "<dateStringTtimeString> repeats <weekdays> for <N> times "
            + "(Creates an event series that repeats N times on specific weekdays)"
            + System.lineSeparator() + "create event <eventSubject> from <dateStringTtimeString> "
            + "to "
            + "<dateStringTtimeString> repeats <weekdays> until <dateString> "
            + "(Creates an event series until a specific date (inclusive))"
            + System.lineSeparator() + "create event <eventSubject> on <dateString> "
            + "(Creates a single all day event.)"
            + System.lineSeparator()
            + "create event <eventSubject> on <dateString> repeats <weekdays> for <N> times"
            + "(Creates a series of all day events that repeats N times on specific weekdays)"
            + System.lineSeparator()
            + "create event <eventSubject> on <dateString> repeats <weekdays> until "
            + "<dateString>" + "(Creates a series of all day events until a specific date "
            + "(inclusive)." + System.lineSeparator()
            + "edit event <property> <eventSubject> from <dateStringTtimeString> to "
            + "<dateStringTtimeString> with <NewPropertyValue>"
            + "(Changes the property of the given event)."
            + System.lineSeparator()
            + "edit events <property> <eventSubject> from <dateStringTtimeString> "
            + "with <NewPropertyValue>"
            + "(Identify the event that has the given subject and starts at the given date "
            + "and time and edit its property. If this event is part of a series then the "
            + "properties of all events in that series that start at or after the given date "
            + "and time is changed)."
            + System.lineSeparator()
            + "edit series <property> <eventSubject> from <dateStringTtimeString> "
            + "with <NewPropertyValue>"
            + "(Identify the event that has the given subject and starts at the given date and "
            + "time and edit its property. If this event is part of a series then the properties "
            + "of all events in that series is changed)."
            + System.lineSeparator()
            + "print events on <dateString>"
            + "(Prints a bulleted list of all events on that day along with their start and "
            + "end time and location (if any))."
            + System.lineSeparator()
            + "print events from <dateStringTtimeString> to <dateStringTtimeString>"
            + "(Prints a bulleted list of all events in the given interval including their start "
            + "and end times and location (if any))."
            + System.lineSeparator()
            + "show status on <dateStringTtimeString>"
            + "(Prints busy status if the user has events scheduled on a given day and time, "
            + "otherwise, available)."
            + System.lineSeparator()
            + "menu (Print supported instruction list)"
            + System.lineSeparator()
            + "q or quit (quit the program) "
            + System.lineSeparator();
    assertEquals(expected, testOutput.toString());
  }

  /**
   * Tests the {@code farewellMessage} method to verify it prints the correct
   * thank you message.
   */
  @Test
  public void testFarewellMessage() {
    view.farewellMessage();
    assertEquals("Thank you for using this program!", testOutput.toString());
  }

  /**
   * Tests {@code showCalendarEvents} with a list of events that do not have
   * a specified location.
   */
  @Test
  public void testShowCalendarEvents_WithEventsNoLocation() {
    LocalDate date = LocalDate.of(2025, 6, 4);
    Event event1 = new Event("Meeting",
            LocalDateTime.of(2025, 6, 4, 9, 0),
            LocalDateTime.of(2025, 6, 4, 10, 0));
    Event event2 = new Event("Lunch",
            LocalDateTime.of(2025, 6, 4, 12, 0),
            LocalDateTime.of(2025, 6, 4, 13, 0));
    List<Event> events = Arrays.asList(event1, event2);

    view.showCalendarEvents(events, date);

    String expectedOutput =
            "Printing events on 2025-06-04." + System.lineSeparator()
                    + "Event 'Meeting' on 2025-06-04 from 09:00 to 10:00"
                    + System.lineSeparator() + "Event 'Lunch' on 2025-06-04 from 12:00 to 13:00"
                    + System.lineSeparator();

    assertEquals(expectedOutput, testOutput.toString());
  }

  /**
   * Tests {@code showCalendarEvents} with a list of events that include
   * specified locations (online and physical).
   */
  @Test
  public void testShowCalendarEvents_WithEventsWithLocation() {
    LocalDate date = LocalDate.of(2025, 6, 5);
    Event event1 = new Event("Online Call",
            LocalDateTime.of(2025, 6, 5, 9, 0),
            LocalDateTime.of(2025, 6, 5, 10, 0),
            "Discussion", Location.ONLINE, "zoom.us/123",
            EventStatus.PUBLIC, null);
    Event event2 = new Event("Physical Meeting",
            LocalDateTime.of(2025, 6, 5, 14, 0),
            LocalDateTime.of(2025, 6, 5, 15, 0),
            "Presentation", Location.PHYSICAL, "Office Rm 300",
            EventStatus.PRIVATE, null);
    List<Event> events = Arrays.asList(event1, event2);

    view.showCalendarEvents(events, date);

    String expectedOutput =
            "Printing events on 2025-06-05." + System.lineSeparator() +
                    "ONLINE event 'Online Call' on 2025-06-05' from 09:00 to 10:00"
                    + System.lineSeparator() +
                    "PHYSICAL event 'Physical Meeting' on 2025-06-05' from 14:00 to 15:00"
                    + System.lineSeparator();

    assertEquals(expectedOutput, testOutput.toString());
  }

  /**
   * Tests {@code showCalendarEvents} when there are no events for the
   * specified date.
   */
  @Test
  public void testShowCalendarEvents_NoEvents() {
    LocalDate date = LocalDate.of(2025, 6, 6);
    List<Event> events = Collections.emptyList();

    view.showCalendarEvents(events, date);

    String expectedOutput =
            "Printing events on 2025-06-06." + System.lineSeparator() +
                    "No events found" + System.lineSeparator();

    assertEquals(expectedOutput, testOutput.toString());
  }

  /**
   * Tests {@code showCalendarEvents} with an event that spans across midnight.
   * Verifies correct printing for both days involved.
   */
  @Test
  public void testShowCalendarEvents_EventOverlappingMidnight() {
    LocalDate date1 = LocalDate.of(2025, 6, 7);
    LocalDate date2 = LocalDate.of(2025, 6, 8);
    Event overnightEvent = new Event("Overnight Trip",
            LocalDateTime.of(2025, 6, 7, 22, 0),
            LocalDateTime.of(2025, 6, 8, 3, 0));

    // Test for the first day of the event
    view.showCalendarEvents(Arrays.asList(overnightEvent), date1);
    String expectedOutput1 = "Printing events on 2025-06-07." + System.lineSeparator() +
            "Event 'Overnight Trip' on 2025-06-07 from 22:00 to 03:00" + System.lineSeparator();
    assertEquals(expectedOutput1, testOutput.toString());

    // Reset view for the second day
    testOutput = new StringWriter();
    view = new CalendarView(testOutput);

    // Test for the second day of the event
    view.showCalendarEvents(Arrays.asList(overnightEvent), date2);
    String expectedOutput2 = "Printing events on 2025-06-08." + System.lineSeparator() +
            "Event 'Overnight Trip' on 2025-06-07 from 22:00 to 03:00" + System.lineSeparator();
    assertEquals(expectedOutput2, testOutput.toString());
  }

  /**
   * Tests {@code showCalendarEventsInDateRange} with a list of events that
   * fall within the specified date range.
   */
  @Test
  public void testShowCalendarEventsInDateRange_WithEvents() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 4, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 5, 17, 0);
    Event event1 = new Event("Event Day 1",
            LocalDateTime.of(2025, 6, 4, 10, 0),
            LocalDateTime.of(2025, 6, 4, 11, 0));
    Event event2 = new Event("Event Day 2",
            LocalDateTime.of(2025, 6, 5, 10, 0),
            LocalDateTime.of(2025, 6, 5, 11, 0));
    List<Event> events = Arrays.asList(event1, event2);

    view.showCalendarEventsInDateRange(start, end, events);

    String expectedOutput =
            "Printing events from 2025-06-04T09:00 to 2025-06-05T17:00." + System.lineSeparator()
                    + "Event 'Event Day 1' on 2025-06-04 from 10:00 to 11:00"
                    + System.lineSeparator()
                    + "Event 'Event Day 2' on 2025-06-05 from 10:00 to 11:00"
                    + System.lineSeparator();

    assertEquals(expectedOutput, testOutput.toString());
  }

  /**
   * Tests {@code showCalendarEventsInDateRange} with events that partially
   * overlap with the specified date range.
   */
  @Test
  public void testShowCalendarEventsInDateRange_OverlappingEvents() {
    LocalDateTime rangeStart
            = LocalDateTime.of(2025, 6, 10, 8, 0);
    LocalDateTime rangeEnd
            = LocalDateTime.of(2025, 6, 10, 18, 0);

    Event event1 = new Event("Early Event",
            LocalDateTime.of(2025, 6, 10, 7, 0),
            LocalDateTime.of(2025, 6, 10, 9, 0));
    Event event2 = new Event("Mid Event",
            LocalDateTime.of(2025, 6, 10, 10, 0),
            LocalDateTime.of(2025, 6, 10, 12, 0));
    Event event3 = new Event("Late Event",
            LocalDateTime.of(2025, 6, 10, 17, 0),
            LocalDateTime.of(2025, 6, 10, 19, 0));

    List<Event> eventsToShow = Arrays.asList(event1, event2, event3);

    view.showCalendarEventsInDateRange(rangeStart, rangeEnd, eventsToShow);

    String expectedOutput = "Printing events from 2025-06-10T08:00 to 2025-06-10T18:00."
            + System.lineSeparator() +
            "Event 'Early Event' on 2025-06-10 from 07:00 to 09:00" + System.lineSeparator()
            + "Event 'Mid Event' on 2025-06-10 from 10:00 to 12:00" + System.lineSeparator()
            + "Event 'Late Event' on 2025-06-10 from 17:00 to 19:00" + System.lineSeparator();
    assertEquals(expectedOutput, testOutput.toString());
  }

  /**
   * Tests {@code showCalendarEventsInDateRange} when there are no events
   * within the specified date range.
   */
  @Test
  public void testShowCalendarEventsInDateRange_NoEvents() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 7, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 8, 17, 0);
    List<Event> events = Collections.emptyList();

    view.showCalendarEventsInDateRange(start, end, events);

    String expectedOutput =
            "Printing events from 2025-06-07T09:00 to 2025-06-08T17:00." + System.lineSeparator()
                    + "No events found" + System.lineSeparator();

    assertEquals(expectedOutput, testOutput.toString());
  }

  /**
   * Tests {@code showCalendarEventsInDateRange} with an empty date range
   * (start and end times are the same).
   */
  @Test
  public void testShowCalendarEventsInDateRange_EmptyRange() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 11, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 11, 10, 0);
    Event event = new Event("Test", LocalDateTime.of(2025, 6, 11,
            9, 0), LocalDateTime.of(2025, 6, 11, 11,
            0));
    List<Event> events = Arrays.asList(event);

    view.showCalendarEventsInDateRange(start, end, events);
    String expectedOutput = "Printing events from 2025-06-11T10:00 to 2025-06-11T10:00."
            + System.lineSeparator() +
            "Event 'Test' on 2025-06-11 from 09:00 to 11:00" + System.lineSeparator();
    assertEquals(expectedOutput, testOutput.toString());
  }


  /**
   * Tests the {@code writeMessage} method with a standard message.
   */
  @Test
  public void testWriteMessage_StandardMessage() {
    String message = "Hello, world!";
    view.writeMessage(message);
    assertEquals(message, testOutput.toString());
  }

  /**
   * Tests the {@code writeMessage} method with an empty string message.
   */
  @Test
  public void testWriteMessage_EmptyMessage() {
    String message = "";
    view.writeMessage(message);
    assertEquals(message, testOutput.toString());
  }

  /**
   * Tests the {@code writeMessage} method with a message containing line separators.
   */
  @Test
  public void testWriteMessage_MessageWithLineSeparators() {
    String message = "Line 1" + System.lineSeparator() + "Line 2";
    view.writeMessage(message);
    assertEquals(message, testOutput.toString());
  }

  /**
   * Tests the {@code writeMessage} method with a null message.
   * Expects "null" to be printed.
   */
  @Test
  public void testWriteMessage_NullMessage() {
    String message = null;
    view.writeMessage(message);
    assertEquals("null", testOutput.toString());
  }

  /**
   * Tests the {@code writeMessage} method when an IOException occurs
   * during writing. It expects an IllegalStateException to be thrown.
   */
  @Test(expected = IllegalStateException.class)
  public void testWriteMessage_IOException() {
    // Creates a mock Appendable that always throws an IOException.
    Appendable brokenAppendable = new Appendable() {
      @Override
      public Appendable append(CharSequence csq) throws IOException {
        throw new java.io.IOException("Simulated IO Exception");
      }

      @Override
      public Appendable append(CharSequence csq, int start, int end) throws IOException {
        throw new java.io.IOException("Simulated IO Exception");
      }

      @Override
      public Appendable append(char c) throws IOException {
        throw new java.io.IOException("Simulated IO Exception");
      }
    };
    // Creates a CalendarView with the broken Appendable.
    CalendarView errorView = new CalendarView(brokenAppendable);
    // Attempts to write a message, which should trigger the IOException.
    errorView.writeMessage("This should fail");
  }
}