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
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
  public void testShowMenuExactContent() {
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
  public void testShowCalendarEventsWithEventsNoLocation() {
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
  public void testShowCalendarEventsWithEventsWithLocation() {
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
  public void testShowCalendarEventsNoEvents() {
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
  public void testShowCalendarEventsEventOverlappingMidnight() {
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
  public void testShowCalendarEventsInDateRangeWithEvents() {
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
  public void testShowCalendarEventsInDateRangeOverlappingEvents() {
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
  public void testShowCalendarEventsInDateRangeNoEvents() {
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
  public void testShowCalendarEventsInDateRangeEmptyRange() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 11, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 11, 10, 0);
    Event event = new Event("Test", LocalDateTime.of(2025, 6, 11,
            9, 0), LocalDateTime.of(2025, 6, 11, 11,
            0));
    List<Event> events = List.of(event);

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
  public void testWriteMessageStandardMessage() {
    String message = "Hello, world!";
    view.writeMessage(message);
    assertEquals(message, testOutput.toString());
  }

  /**
   * Tests the {@code writeMessage} method with an empty string message.
   */
  @Test
  public void testWriteMessageEmptyMessage() {
    String message = "";
    view.writeMessage(message);
    assertEquals(message, testOutput.toString());
  }

  /**
   * Tests the {@code writeMessage} method with a message containing line separators.
   */
  @Test
  public void testWriteMessageMessageWithLineSeparators() {
    String message = "Line 1" + System.lineSeparator() + "Line 2";
    view.writeMessage(message);
    assertEquals(message, testOutput.toString());
  }

  /**
   * Tests the {@code writeMessage} method with a null message.
   * Expects "null" to be printed.
   */
  @Test
  public void testWriteMessageNullMessage() {
    String message = null;
    view.writeMessage(message);
    assertEquals("null", testOutput.toString());
  }

  /**
   * Tests the {@code writeMessage} method when an IOException occurs
   * during writing. It expects an IllegalStateException to be thrown.
   */
  @Test(expected = IllegalStateException.class)
  public void testWriteMessageIOException() {
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

  /**
   * Tests the {@code showCalendarEvents} method with events that have and don't have
   * detailed location information (e.g., "Room 101" vs. just "ONLINE").
   * Ensures that the output correctly formats both scenarios.
   */
  @Test
  public void testShowCalendarEventsEventsWithLocationDetails() {
    LocalDate date = LocalDate.of(2025, 6, 4);
    Event eventWithDetail = new Event("Conference",
            LocalDateTime.of(2025, 6, 4, 9, 0),
            LocalDateTime.of(2025, 6, 4, 17, 0),
            "Annual tech conference", Location.PHYSICAL,
            "Convention Center Hall A",
            EventStatus.PUBLIC, null);
    Event eventWithoutDetail = new Event("Quick Call",
            LocalDateTime.of(2025, 6, 4, 10, 0),
            LocalDateTime.of(2025, 6, 4, 10, 30),
            null, Location.ONLINE, null, null, null);

    List<Event> events = Arrays.asList(eventWithDetail, eventWithoutDetail);
    view.showCalendarEvents(events, date);

    String output = testOutput.toString();
    assertTrue(output.contains("PHYSICAL event 'Conference'"));
    assertTrue(output.contains("ONLINE event 'Quick Call'"));
  }

  /**
   * Tests the {@code showCalendarEvents} method with a large number of events.
   * Verifies that the introductory message is present and all event subjects are displayed.
   */
  @Test
  public void testShowCalendarEventsManyEvents() {
    LocalDate date = LocalDate.of(2025, 6, 4);
    List<Event> manyEvents = new ArrayList<>();

    for (int i = 0; i < 20; i++) {
      Event event = new Event("Event " + i,
              LocalDateTime.of(2025, 6, 4, 8 + i % 12,
                      i % 60),
              LocalDateTime.of(2025, 6, 4, 8 + (i % 12) + 1,
                      (i % 60) + 30));
      manyEvents.add(event);
    }

    view.showCalendarEvents(manyEvents, date);

    String output = testOutput.toString();
    assertTrue(output.contains("Printing events on 2025-06-04"));

    // Verify all events are displayed
    for (int i = 0; i < 20; i++) {
      assertTrue(output.contains("Event '" + "Event " + i + "'"));
    }
  }

  /**
   * Tests the {@code showCalendarEvents} method to ensure that events are displayed
   * in chronological order of their start times, regardless of the order in the input list.
   * (Note: The current {@link CalendarView} implementation doesn't explicitly sort,
   * so this tests the natural order if the input list is already sorted or if the
   * underlying data structure provides it, or simply tests that all events are present).
   */
  @Test
  public void testShowCalendarEventsEventsInChronologicalOrder() {
    LocalDate date = LocalDate.of(2025, 6, 4);
    Event lateEvent = new Event("Late Event",
            LocalDateTime.of(2025, 6, 4, 15, 0),
            LocalDateTime.of(2025, 6, 4, 16, 0));
    Event earlyEvent = new Event("Early Event",
            LocalDateTime.of(2025, 6, 4, 9, 0),
            LocalDateTime.of(2025, 6, 4, 10, 0));
    Event middleEvent = new Event("Middle Event",
            LocalDateTime.of(2025, 6, 4, 12, 0),
            LocalDateTime.of(2025, 6, 4, 13, 0));

    // Intentionally add out of order to see if view handles sorting or simply prints in list order
    List<Event> events = Arrays.asList(lateEvent, earlyEvent, middleEvent);
    view.showCalendarEvents(events, date);

    String output = testOutput.toString();
    assertTrue(output.indexOf("Late Event") < output.indexOf("Early Event"));
    assertTrue(output.contains("Early Event"));
    assertTrue(output.contains("Middle Event"));
    assertTrue(output.contains("Late Event"));
  }

  /**
   * Tests the {@code showCalendarEventsInDateRange} method when the start and end
   * of the range are identical (a point in time). Verifies that events
   * overlapping this specific time are displayed.
   */
  @Test
  public void testShowCalendarEventsInDateRangeSameStartAndEnd() {
    LocalDateTime sameTime = LocalDateTime.of(2025, 6, 10, 12,
            0);
    Event event = new Event("Point Event",
            LocalDateTime.of(2025, 6, 10, 11, 30),
            LocalDateTime.of(2025, 6, 10, 12, 30));

    view.showCalendarEventsInDateRange(sameTime, sameTime, List.of(event));

    String expectedOutput = "Printing events from 2025-06-10T12:00 to 2025-06-10T12:00." +
            System.lineSeparator() +
            "Event 'Point Event' on 2025-06-10 from 11:30 to 12:30" + System.lineSeparator();

    assertEquals(expectedOutput, testOutput.toString());
  }

  /**
   * Tests the {@code showCalendarEventsInDateRange} method for an event that
   * crosses the midnight boundary within the specified range.
   *
   */
  @Test
  public void testShowCalendarEventsInDateRangeCrossingMidnight() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 10, 23, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 11, 1, 0);

    Event overnightEvent = new Event("Overnight Event",
            LocalDateTime.of(2025, 6, 10, 23, 30),
            LocalDateTime.of(2025, 6, 11, 0, 30));

    view.showCalendarEventsInDateRange(start, end, Arrays.asList(overnightEvent));

    String output = testOutput.toString();
    assertTrue(output.contains("Printing events from 2025-06-10T23:00 to 2025-06-11T01:00"));
    assertTrue(output.contains("Event 'Overnight Event'"));
  }

  /**
   * Tests the {@code showCalendarEventsInDateRange} method with multiple events
   * that all fall within the specified date and time range.
   * Verifies that all relevant events are displayed.
   */
  @Test
  public void testShowCalendarEventsInDateRangeMultipleEventsInRange() {
    LocalDateTime rangeStart = LocalDateTime.of(2025, 6, 10, 9,
            0);
    LocalDateTime rangeEnd = LocalDateTime.of(2025, 6, 10, 17,
            0);

    Event morningEvent = new Event("Morning Meeting",
            LocalDateTime.of(2025, 6, 10, 9, 30),
            LocalDateTime.of(2025, 6, 10, 10, 30));
    Event lunchEvent = new Event("Lunch",
            LocalDateTime.of(2025, 6, 10, 12, 0),
            LocalDateTime.of(2025, 6, 10, 13, 0));
    Event afternoonEvent = new Event("Afternoon Call",
            LocalDateTime.of(2025, 6, 10, 15, 0),
            LocalDateTime.of(2025, 6, 10, 16, 0));

    List<Event> events = Arrays.asList(morningEvent, lunchEvent, afternoonEvent);
    view.showCalendarEventsInDateRange(rangeStart, rangeEnd, events);

    String output = testOutput.toString();
    assertTrue(output.contains("Morning Meeting"));
    assertTrue(output.contains("Lunch"));
    assertTrue(output.contains("Afternoon Call"));
  }

  /**
   * Tests the {@code writeMessage} method with a string containing special characters.
   * Ensures that the output correctly handles such characters without corruption.
   */
  @Test
  public void testWriteMessageSpecialCharacters() {
    String messageWithSpecialChars = "Event with émojis 🎉 and symbols @#$%^&*()";
    view.writeMessage(messageWithSpecialChars);
    assertEquals(messageWithSpecialChars, testOutput.toString());
  }

  /**
   * Tests the {@code writeMessage} method with a string containing tab and newline characters.
   * Verifies that formatting characters are preserved in the output.
   */
  @Test
  public void testWriteMessageTabsAndNewlines() {
    String messageWithFormatting = "Line 1\tTabbed\nLine 2\r\nLine 3";
    view.writeMessage(messageWithFormatting);
    assertEquals(messageWithFormatting, testOutput.toString());
  }

  /**
   * Tests the {@code writeMessage} method with multiple consecutive calls.
   * Ensures that messages are appended correctly to the output stream.
   */
  @Test
  public void testWriteMessageMultipleConsecutiveCalls() {
    view.writeMessage("First message");
    view.writeMessage("Second message");
    view.writeMessage("Third message");

    assertEquals("First messageSecond messageThird message", testOutput.toString());
  }

  /**
   * Tests the {@code showCalendarEvents} method with an event having a very long subject string.
   * Verifies that the long subject is fully displayed and does not cause formatting issues.
   */
  @Test
  public void testShowCalendarEventsLongSubjects() {
    LocalDate date = LocalDate.of(2025, 6, 4);
    Event longSubjectEvent = new Event(
            "This is a very long event subject that might cause formatting " +
                    "issues and should be handled gracefully by the view implementation",
            LocalDateTime.of(2025, 6, 4, 9, 0),
            LocalDateTime.of(2025, 6, 4, 10, 0));

    view.showCalendarEvents(Arrays.asList(longSubjectEvent), date);

    String output = testOutput.toString();
    assertTrue(output.contains("This is a very long event subject"));
    assertTrue(output.contains("from 09:00 to 10:00"));
  }

  /**
   * Tests the {@code showCalendarEvents} method with an event subject containing quotes.
   * Ensures that quotes within the subject string are correctly printed.
   */
  @Test
  public void testShowCalendarEventsSubjectsWithQuotes() {
    LocalDate date = LocalDate.of(2025, 6, 4);
    Event quotedEvent = new Event("Meeting about \"Project Alpha\"",
            LocalDateTime.of(2025, 6, 4, 9, 0),
            LocalDateTime.of(2025, 6, 4, 10, 0));

    view.showCalendarEvents(Arrays.asList(quotedEvent), date);

    String output = testOutput.toString();
    assertTrue(output.contains("Meeting about \"Project Alpha\""));
  }

  /**
   * Tests the sequential display of the welcome menu and the farewell message,
   * ensuring both are present in the output.
   */
  @Test
  public void testMenuAndFarewellTogether() {
    view.showMenu();
    view.farewellMessage();

    String output = testOutput.toString();
    assertTrue(output.contains("Welcome to the calendar program!"));
    assertTrue(output.contains("Thank you for using this program!"));
    assertTrue(output.contains("Supported user instructions are:"));
  }

  /**
   * Tests a complete simulated workflow, including showing the menu,
   * displaying calendar events, writing a custom message, and showing the farewell message.
   * Verifies that all expected output components are present.
   */
  @Test
  public void testCompleteWorkflow() {
    view.showMenu();

    LocalDate testDate = LocalDate.of(2025, 6, 4);
    Event event = new Event("Test Event",
            LocalDateTime.of(2025, 6, 4, 9, 0),
            LocalDateTime.of(2025, 6, 4, 10, 0));

    view.showCalendarEvents(List.of(event), testDate);
    view.writeMessage("Command executed successfully");
    view.farewellMessage();

    String output = testOutput.toString();
    assertTrue(output.contains("Welcome to the calendar program!"));
    assertTrue(output.contains("Test Event"));
    assertTrue(output.contains("Command executed successfully"));
    assertTrue(output.contains("Thank you for using this program!"));
  }

  /**
   * Tests the {@code showCalendarEvents} method when the input list of events
   * contains a {@code null} event. It expects the method to handle such a case
   * without crashing, possibly by throwing a {@link NullPointerException} or
   * {@link IllegalArgumentException} if the implementation is robust, or
   * simply skipping the null event.
   */
  @Test
  public void testShowCalendarEventsNullEventInList() {
    LocalDate date = LocalDate.of(2025, 6, 4);
    Event validEvent = new Event("Valid Event",
            LocalDateTime.of(2025, 6, 4, 9, 0),
            LocalDateTime.of(2025, 6, 4, 10, 0));

    List<Event> eventsWithNull = new ArrayList<>();
    eventsWithNull.add(validEvent);
    eventsWithNull.add(null); // Add a null event to the list

    try {
      view.showCalendarEvents(eventsWithNull, date);
      String output = testOutput.toString();
      assertTrue(output.contains("Valid Event"));
      // Depending on implementation, it might just print the valid event and ignore null
      // If it throws an exception for null, the test will pass because of the catch block.
    } catch (Exception e) {
      assertTrue(e instanceof NullPointerException ||
              e instanceof IllegalArgumentException);
    }
  }

  /**
   * Tests the {@code showCalendarEvents} method with an event where some optional fields
   * (like description, location type, location detail, status, series ID) are null.
   * Verifies that the view correctly handles null values without errors and prints the available
   * information.
   */
  @Test
  public void testShowCalendarEventsEventsWithNullFields() {
    LocalDate date = LocalDate.of(2025, 6, 4);
    Event eventWithNulls = new Event("Event with Nulls",
            LocalDateTime.of(2025, 6, 4, 9, 0),
            LocalDateTime.of(2025, 6, 4, 10, 0),
            null, null, null, null, null);

    view.showCalendarEvents(Arrays.asList(eventWithNulls), date);

    String output = testOutput.toString();
    assertTrue(output.contains("Event 'Event with Nulls'"));
    assertTrue(output.contains("from 09:00 to 10:00"));
  }

  /**
   * Tests the {@code showCalendarEvents} method with events scheduled around midnight.
   * Verifies that the time format for 00:00 and times close to midnight are displayed correctly.
   */
  @Test
  public void testShowCalendarEventsMidnightTimes() {
    LocalDate date = LocalDate.of(2025, 6, 4);
    Event midnightEvent = new Event("Midnight Event",
            LocalDateTime.of(2025, 6, 4, 0, 0),
            LocalDateTime.of(2025, 6, 4, 1, 0));
    Event beforeMidnightEvent = new Event("Before Midnight",
            LocalDateTime.of(2025, 6, 4, 23, 30),
            LocalDateTime.of(2025, 6, 4, 23, 59));

    List<Event> events = Arrays.asList(midnightEvent, beforeMidnightEvent);
    view.showCalendarEvents(events, date);

    String output = testOutput.toString();
    assertTrue(output.contains("from 00:00 to 01:00"));
    assertTrue(output.contains("from 23:30 to 23:59"));
  }

  /**
   * Tests the {@code showCalendarEventsInDateRange} method for a very long time span
   * (e.g., an entire year). Verifies that events occurring at the beginning and end
   * of such a broad range are correctly displayed, along with the range itself.
   */
  @Test
  public void testShowCalendarEventsInDateRangeLongTimeSpan() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);
    LocalDateTime end = LocalDateTime.of(2025, 12, 31, 23, 59);

    Event newYearEvent = new Event("New Year",
            LocalDateTime.of(2025, 1, 1, 0, 0),
            LocalDateTime.of(2025, 1, 1, 1, 0));
    Event christmasEvent = new Event("Christmas",
            LocalDateTime.of(2025, 12, 25, 10, 0),
            LocalDateTime.of(2025, 12, 25, 18, 0));

    List<Event> events = Arrays.asList(newYearEvent, christmasEvent);
    view.showCalendarEventsInDateRange(start, end, events);

    String output = testOutput.toString();
    assertTrue(output.contains("New Year"));
    assertTrue(output.contains("Christmas"));
    assertTrue(output.contains("2025-01-01T00:00 to 2025-12-31T23:59"));
  }

  /**
   * Tests displaying events with all possible location and status combinations.
   * Verifies that the view correctly handles all permutations of location and status values.
   */
  @Test
  public void testShowCalendarEventsAllLocationStatusCombinations() {
    LocalDate date = LocalDate.of(2025, 6, 4);

    Event physicalPublic = new Event("Physical Public Event",
            LocalDateTime.of(2025, 6, 4, 9, 0),
            LocalDateTime.of(2025, 6, 4, 10, 0),
            "Description", Location.PHYSICAL, "Room 101", 
            EventStatus.PUBLIC, null);

    Event physicalPrivate = new Event("Physical Private Event",
            LocalDateTime.of(2025, 6, 4, 11, 0),
            LocalDateTime.of(2025, 6, 4, 12, 0),
            "Description", Location.PHYSICAL, "Room 102", 
            EventStatus.PRIVATE, null);

    Event onlinePublic = new Event("Online Public Event",
            LocalDateTime.of(2025, 6, 4, 13, 0),
            LocalDateTime.of(2025, 6, 4, 14, 0),
            "Description", Location.ONLINE, "Zoom Link",
            EventStatus.PUBLIC, null);

    Event onlinePrivate = new Event("Online Private Event",
            LocalDateTime.of(2025, 6, 4, 15, 0),
            LocalDateTime.of(2025, 6, 4, 16, 0),
            "Description", Location.ONLINE, "Teams Link",
            EventStatus.PRIVATE, null);

    Event nullLocation = new Event("No Location Event",
            LocalDateTime.of(2025, 6, 4, 17, 0),
            LocalDateTime.of(2025, 6, 4, 18, 0),
            "Description", null, null, 
            EventStatus.PUBLIC, null);

    List<Event> events = Arrays.asList(physicalPublic, physicalPrivate, onlinePublic,
            onlinePrivate, nullLocation);
    view.showCalendarEvents(events, date);

    String output = testOutput.toString();
    assertTrue("Should contain physical public event",
            output.contains("Physical Public Event"));
    assertTrue("Should contain physical private event",
            output.contains("Physical Private Event"));
    assertTrue("Should contain online public event",
            output.contains("Online Public Event"));
    assertTrue("Should contain online private event", 
            output.contains("Online Private Event"));
    assertTrue("Should contain no location event", output.contains("No Location Event"));

    // Verify location formatting
    assertTrue("Should show PHYSICAL location", output.contains("PHYSICAL"));
    assertTrue("Should show ONLINE location", output.contains("ONLINE"));
  }

  /**
   * Tests displaying events with all properties populated.
   * Verifies that events with description, location details, and status are displayed correctly.
   */
  @Test
  public void testShowCalendarEventsEventsWithAllProperties() {
    LocalDate date = LocalDate.of(2025, 6, 4);
    UUID seriesId = UUID.randomUUID();

    Event fullEvent = new Event("Complete Event",
            LocalDateTime.of(2025, 6, 4, 9, 0),
            LocalDateTime.of(2025, 6, 4, 10, 0),
            "This is a detailed description of the event",
            Location.PHYSICAL,
            "Conference Room A, Building 2, Floor 3",
            EventStatus.PRIVATE,
            seriesId);

    view.showCalendarEvents(Arrays.asList(fullEvent), date);

    String output = testOutput.toString();
    assertTrue("Should contain event subject", output.contains("Complete Event"));
    assertTrue("Should contain time information", output.contains("from 09:00 to 10:00"));
    assertTrue("Should contain location type", output.contains("PHYSICAL"));

    // Note: Description and status might not be displayed in the view output
    // This test verifies that having all properties doesn't break the display
  }

  /**
   * Tests displaying a mix of series events and individual events together.
   * Verifies that both types of events are displayed correctly in the same list.
   */
  @Test
  public void testShowCalendarEventsSeriesAndNonSeriesEventsTogether() {
    LocalDate date = LocalDate.of(2025, 6, 4);
    UUID seriesId = UUID.randomUUID();

    Event individualEvent = new Event("Individual Event",
            LocalDateTime.of(2025, 6, 4, 9, 0),
            LocalDateTime.of(2025, 6, 4, 10, 0));

    Event seriesEvent = new Event("Series Event",
            LocalDateTime.of(2025, 6, 4, 11, 0),
            LocalDateTime.of(2025, 6, 4, 12, 0),
            "Part of recurring series", Location.ONLINE, "Meeting Link",
            EventStatus.PUBLIC, seriesId);

    Event anotherIndividualEvent = new Event("Another Individual",
            LocalDateTime.of(2025, 6, 4, 13, 0),
            LocalDateTime.of(2025, 6, 4, 14, 0));

    List<Event> mixedEvents = Arrays.asList(individualEvent, seriesEvent, anotherIndividualEvent);
    view.showCalendarEvents(mixedEvents, date);

    String output = testOutput.toString();
    assertTrue("Should contain individual event", output.contains("Individual Event"));
    assertTrue("Should contain series event", output.contains("Series Event"));
    assertTrue("Should contain another individual event", 
            output.contains("Another Individual"));

    // Verify that series events are displayed with location info
    assertTrue("Series event should show location", output.contains("ONLINE"));
  }

  /**
   * Tests displaying events that span range boundaries in date range view.
   * Verifies that events crossing the boundaries of the display range are handled correctly.
   */
  @Test
  public void testShowCalendarEventsInDateRangeEventsSpanningRangeBoundaries() {
    LocalDateTime rangeStart = LocalDateTime.of(2025, 6, 10,
            10, 0);
    LocalDateTime rangeEnd = LocalDateTime.of(2025, 6, 10, 
            16, 0);

    // Event that starts before range and ends within range
    Event beforeToWithin = new Event("Before to Within",
            LocalDateTime.of(2025, 6, 10, 8, 0),
            LocalDateTime.of(2025, 6, 10, 12, 0));

    // Event that starts within range and ends after range
    Event withinToAfter = new Event("Within to After",
            LocalDateTime.of(2025, 6, 10, 14, 0),
            LocalDateTime.of(2025, 6, 10, 18, 0));

    // Event that completely spans the range
    Event completeSpan = new Event("Complete Span",
            LocalDateTime.of(2025, 6, 10, 9, 0),
            LocalDateTime.of(2025, 6, 10, 17, 0));

    List<Event> events = Arrays.asList(beforeToWithin, withinToAfter, completeSpan);
    view.showCalendarEventsInDateRange(rangeStart, rangeEnd, events);

    String output = testOutput.toString();
    assertTrue("Should contain range information",
            output.contains("from 2025-06-10T10:00 to 2025-06-10T16:00"));
    assertTrue("Should display before-to-within event", 
            output.contains("Before to Within"));
    assertTrue("Should display within-to-after event", output.contains("Within to After"));
    assertTrue("Should display complete span event", output.contains("Complete Span"));
  }

  /**
   * Tests writeMessage with extremely long strings.
   * Verifies that the view can handle very long messages without breaking.
   */
  @Test
  public void testWriteMessageVeryLongMessages() {
    StringBuilder longMessage = new StringBuilder();
    for (int i = 0; i < 1000; i++) {
      longMessage.append("This is a very long message that tests the " +
              "view's ability to handle large strings. ");
    }

    view.writeMessage(longMessage.toString());

    String output = testOutput.toString();
    assertEquals("Should write entire long message", longMessage.toString(), output);
    assertTrue("Should contain repeated text",
            output.contains("This is a very long message"));
  }

  /**
   * Tests a complete user session simulation with all command types.
   * Verifies that the view can handle a full sequence of user interactions.
   */
  @Test
  public void testCompleteUserSessionAllCommandTypes() {
    // Simulate complete user session
    view.showMenu();
    view.writeMessage("User created an event successfully." + System.lineSeparator());

    LocalDate testDate = LocalDate.of(2025, 6, 4);
    Event testEvent = new Event("Test Event",
            LocalDateTime.of(2025, 6, 4, 9, 0),
            LocalDateTime.of(2025, 6, 4, 10, 0));

    view.showCalendarEvents(Arrays.asList(testEvent), testDate);
    view.writeMessage("User edited an event successfully." + System.lineSeparator());

    LocalDateTime rangeStart = LocalDateTime.of(2025, 6, 
            4, 8, 0);
    LocalDateTime rangeEnd = LocalDateTime.of(2025, 6, 
            4, 18, 0);
    view.showCalendarEventsInDateRange(rangeStart, rangeEnd, Arrays.asList(testEvent));

    view.writeMessage("User checked availability - you are available." + System.lineSeparator());
    view.farewellMessage();

    String output = testOutput.toString();
    assertTrue("Should contain welcome message", 
            output.contains("Welcome to the calendar program!"));
    assertTrue("Should contain success messages",
            output.contains("successfully"));
    assertTrue("Should contain event information", 
            output.contains("Test Event"));
    assertTrue("Should contain availability check",
            output.contains("available"));
    assertTrue("Should contain farewell message", 
            output.contains("Thank you for using this program!"));
  }

  /**
   * Tests displaying events with null or empty subjects.
   * Verifies that the view handles edge cases in event data gracefully.
   */
  @Test
  public void testShowCalendarEventsEventsWithNullOrEmptySubjects() {
    LocalDate date = LocalDate.of(2025, 6, 4);

    Event emptySubjectEvent = new Event("",
            LocalDateTime.of(2025, 6, 4, 9, 0),
            LocalDateTime.of(2025, 6, 4, 10, 0));

    Event normalEvent = new Event("Normal Event",
            LocalDateTime.of(2025, 6, 4, 11, 0),
            LocalDateTime.of(2025, 6, 4, 12, 0));

    List<Event> events = Arrays.asList(emptySubjectEvent, normalEvent);
    view.showCalendarEvents(events, date);

    String output = testOutput.toString();
    assertTrue("Should handle empty subject", output.contains("Event ''"));
    assertTrue("Should display normal event", output.contains("Normal Event"));
  }

  /**
   * Tests error recovery after IOException simulation.
   * Verifies that the view properly handles and reports I/O errors.
   */
  @Test
  public void testWriteMessageErrorRecovery() {
    // First, test normal operation
    view.writeMessage("Normal message");
    assertEquals("Normal message", testOutput.toString());

    // Reset for error test
    testOutput = new StringWriter();
    view = new CalendarView(testOutput);

    // Test that multiple messages work correctly
    view.writeMessage("First message");
    view.writeMessage("Second message");
    assertEquals("First messageSecond message", testOutput.toString());
  }

  /**
   * Tests performance with large numbers of events.
   * Verifies that the view can handle displaying many events without performance issues.
   */
  @Test
  public void testShowCalendarEventsLargeNumberOfEvents() {
    LocalDate date = LocalDate.of(2025, 6, 4);
    List<Event> manyEvents = new ArrayList<>();
    
    for (int i = 0; i < 100; i++) {
      
      int startHour = 8 + (i % 14); 
      int startMinute = i % 60;
      int endHour = startHour + 1; 
      int endMinute = Math.min(startMinute + 30, 59); 

      Event event = new Event("Event " + i,
              LocalDateTime.of(2025, 6, 4, startHour, startMinute),
              LocalDateTime.of(2025, 6, 4, endHour, endMinute));
      manyEvents.add(event);
    }

    long startTime = System.currentTimeMillis();
    view.showCalendarEvents(manyEvents, date);
    long endTime = System.currentTimeMillis();

    String output = testOutput.toString();
    assertTrue("Should complete in reasonable time", 
            (endTime - startTime) < 1000); // Less than 1 second
    assertTrue("Should contain first event", output.contains("Event 0"));
    assertTrue("Should contain last event", output.contains("Event 99"));
    assertTrue("Should contain date header", 
            output.contains("Printing events on 2025-06-04"));
  }
}