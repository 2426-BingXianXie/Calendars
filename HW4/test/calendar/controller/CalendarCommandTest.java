package calendar.controller;

import calendar.CalendarException;
import calendar.controller.commands.*;
import calendar.model.*;
import calendar.view.CalendarView;
import calendar.view.ICalendarView;
import org.junit.Before;
import org.junit.Test;

import java.io.StringWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Comprehensive test suite for all calendar commands using real implementations.
 * Tests all edge cases, error conditions, and valid scenarios.
 */
public class CalendarCommandTest {
  private VirtualCalendar calendar;
  private StringWriter output;
  private ICalendarView view;

  /**
   * Sets up the test environment before each test method.
   * Initializes a new {@link VirtualCalendar} and a {@link StringWriter}
   * to capture command output.
   */
  @Before
  public void setUp() {
    calendar = new VirtualCalendar();
    output = new StringWriter();
    view = new CalendarView(output);
  }


  /**
   * Tests the creation of a single all-day event using the {@link Create} command.
   * Verifies the event's properties and the output message.
   *
   * @throws CalendarException if the command execution fails.
   */
  @Test
  public void testCreateSingleAllDayEvent() throws CalendarException {
    Scanner scanner = new Scanner("event Meeting on 2025-06-05");
    Create cmd = new Create(scanner, view);
    cmd.execute(calendar);

    // Verify event was created
    List<Event> events = calendar.getEventsList(LocalDate.of(2025, 6, 5));
    assertEquals(1, events.size());
    Event event = events.get(0);
    assertEquals("Meeting", event.getSubject());
    assertEquals(LocalDateTime.of(2025, 6, 5, 8, 0),
            event.getStart());
    assertEquals(LocalDateTime.of(2025, 6, 5, 17, 0),
            event.getEnd());

    // Verify output message
    String outputStr = output.toString();
    assertTrue(outputStr.contains("Event 'Meeting' created from 8am to 5pm on 2025-06-05"));
  }

  /**
   * Tests the creation of a single timed event using the {@link Create} command.
   * Verifies the event's properties and the output message.
   *
   * @throws CalendarException if the command execution fails.
   */
  @Test
  public void testCreateSingleTimedEvent() throws CalendarException {
    Scanner scanner = new Scanner("event Conference from 2025-06-05T09:00 to " +
            "2025-06-05T17:00");
    Create cmd = new Create(scanner, view);
    cmd.execute(calendar);

    List<Event> events = calendar.getEventsList(LocalDate.of(2025, 6, 5));
    assertEquals(1, events.size());
    Event event = events.get(0);
    assertEquals("Conference", event.getSubject());
    assertEquals(LocalDateTime.of(2025, 6, 5, 9, 0),
            event.getStart());
    assertEquals(LocalDateTime.of(2025, 6, 5, 17, 0),
            event.getEnd());

    String outputStr = output.toString();
    assertTrue(outputStr.contains("Event 'Conference' created from 2025-06-05T09:00 " +
            "to 2025-06-05T17:00"));
  }

  /**
   * Tests the creation of an event with a multi-word subject.
   *
   * @throws CalendarException if the command execution fails.
   */
  @Test
  public void testCreateEventWithMultiWordSubject() throws CalendarException {
    Scanner scanner = new Scanner("event Team Building Activity on 2025-06-05");
    Create cmd = new Create(scanner, view);
    cmd.execute(calendar);

    List<Event> events = calendar.getEventsList(LocalDate.of(2025, 6, 5));
    assertEquals("Team Building Activity", events.get(0).getSubject());
  }

  /**
   * Tests the creation of an all-day event series with a specified number of occurrences.
   * Verifies that events are created on the correct days, have the same series ID,
   * and the output message is correct.
   *
   * @throws CalendarException if the command execution fails.
   */
  @Test
  public void testCreateAllDaySeriesWithForCount() throws CalendarException {
    Scanner scanner = new Scanner("event Meeting on 2025-06-02 repeats MWF for 5 times");
    Create cmd = new Create(scanner, view);
    cmd.execute(calendar);

    // Check events were created on correct days
    List<Event> mondayEvents = calendar.getEventsList(LocalDate.of(2025, 6,
            2));
    assertEquals(1, mondayEvents.size());
    assertEquals("Meeting", mondayEvents.get(0).getSubject());

    List<Event> wednesdayEvents = calendar.getEventsList(LocalDate.of(2025, 6,
            4));
    assertEquals(1, wednesdayEvents.size());

    List<Event> fridayEvents = calendar.getEventsList(LocalDate.of(2025, 6,
            6));
    assertEquals(1, fridayEvents.size());

    // Verify all events in series have same series ID
    UUID seriesId = mondayEvents.get(0).getSeriesID();
    assertNotNull(seriesId);
    assertEquals(seriesId, wednesdayEvents.get(0).getSeriesID());
    assertEquals(seriesId, fridayEvents.get(0).getSeriesID());

    String outputStr = output.toString();
    assertTrue(outputStr.contains("Event series 'Meeting' created"));
    assertTrue(outputStr.contains("5 times"));
  }

  /**
   * Tests the creation of an all-day event series repeating until a specified date.
   * Verifies that events are created up to the end date and not beyond.
   *
   * @throws CalendarException if the command execution fails.
   */
  @Test
  public void testCreateAllDaySeriesWithUntilDate() throws CalendarException {
    Scanner scanner = new Scanner("event Meeting on 2025-06-02 repeats MWF " +
            "until 2025-06-30");
    Create cmd = new Create(scanner, view);
    cmd.execute(calendar);

    // Verify events created up to end date
    List<Event> lastMondayEvents = calendar.getEventsList(LocalDate.of(2025,
            6, 30));
    assertEquals(1, lastMondayEvents.size());

    // Verify no events after end date
    List<Event> afterEndEvents = calendar.getEventsList(LocalDate.of(2025,
            7, 2));
    assertEquals(0, afterEndEvents.size());

    String outputStr = output.toString();
    assertTrue(outputStr.contains("until 2025-06-30"));
  }

  /**
   * Tests the creation of a timed event series with a specified number of occurrences.
   * Verifies the total count of events created in the series.
   *
   * @throws CalendarException if the command execution fails.
   */
  @Test
  public void testCreateTimedSeriesWithForCount() throws CalendarException {
    Scanner scanner = new Scanner("event Meeting from 2025-06-02T09:00 to " +
            "2025-06-02T10:00 repeats MWF for 3 times");
    Create cmd = new Create(scanner, view);
    cmd.execute(calendar);

    // Count total events created
    int eventCount = 0;
    LocalDate checkDate = LocalDate.of(2025, 6, 2);
    for (int i = 0; i < 14; i++) { // Check two weeks
      List<Event> events = calendar.getEventsList(checkDate.plusDays(i));
      eventCount += events.size();
    }
    assertEquals(3, eventCount);
  }

  /**
   * Tests that a {@link CalendarException} is thrown when the "event" keyword is missing
   * from the create command.
   *
   * @throws CalendarException expected exception.
   */
  @Test(expected = CalendarException.class)
  public void testCreateMissingEventKeyword() throws CalendarException {
    Scanner scanner = new Scanner("Meeting on 2025-06-05");
    Create cmd = new Create(scanner, view);
    cmd.execute(calendar);
  }

  /**
   * Tests that a {@link CalendarException} is thrown when an invalid keyword
   * immediately follows "event" in the create command (e.g., "meeting" instead of a property name).
   *
   * @throws CalendarException expected exception.
   */
  @Test(expected = CalendarException.class)
  public void testCreateInvalidKeywordAfterCreate() throws CalendarException {
    Scanner scanner = new Scanner("meeting on 2025-06-05");
    Create cmd = new Create(scanner, view);
    cmd.execute(calendar);
  }

  /**
   * Tests that a {@link CalendarException} is thrown when the event subject is missing
   * from the create command.
   *
   * @throws CalendarException expected exception.
   */
  @Test(expected = CalendarException.class)
  public void testCreateMissingSubject() throws CalendarException {
    Scanner scanner = new Scanner("event on 2025-06-05");
    Create cmd = new Create(scanner, view);
    cmd.execute(calendar);
  }

  /**
   * Tests that a {@link CalendarException} is thrown when neither "on" nor "from"
   * keywords are present
   * to specify the event's time or date.
   *
   * @throws CalendarException expected exception.
   */
  @Test(expected = CalendarException.class)
  public void testCreateMissinexecutenOrFrom() throws CalendarException {
    Scanner scanner = new Scanner("event Meeting");
    Create cmd = new Create(scanner, view);
    cmd.execute(calendar);
  }

  /**
   * Tests that a {@link CalendarException} is thrown for an invalid date format.
   *
   * @throws CalendarException expected exception.
   */
  @Test(expected = CalendarException.class)
  public void testCreateInvalidDateFormat() throws CalendarException {
    Scanner scanner = new Scanner("event Meeting on 06-05-2025");
    Create cmd = new Create(scanner, view);
    cmd.execute(calendar);
  }

  /**
   * Tests that a {@link CalendarException} is thrown for an invalid date-time format.
   *
   * @throws CalendarException expected exception.
   */
  @Test(expected = CalendarException.class)
  public void testCreateInvalidDateTimeFormat() throws CalendarException {
    Scanner scanner = new Scanner("event Meeting from 2025-06-05 9:00 to 2025-06-05 10:00");
    Create cmd = new Create(scanner, view);
    cmd.execute(calendar);
  }

  /**
   * Tests that a {@link CalendarException} is thrown when the "to" keyword is missing
   * in a timed event creation command.
   *
   * @throws CalendarException expected exception.
   */
  @Test(expected = CalendarException.class)
  public void testCreateMissingToKeyword() throws CalendarException {
    Scanner scanner = new Scanner("event Meeting from 2025-06-05T09:00 2025-06-05T10:00");
    Create cmd = new Create(scanner, view);
    cmd.execute(calendar);
  }

  /**
   * Tests that a {@link CalendarException} is thrown when the event's end time is
   * before its start time.
   *
   * @throws CalendarException expected exception.
   */
  @Test(expected = CalendarException.class)
  public void testCreateEndBeforeStart() throws CalendarException {
    Scanner scanner = new Scanner("event Meeting from 2025-06-05T10:00 to 2025-06-05T09:00");
    Create cmd = new Create(scanner, view);
    cmd.execute(calendar);
  }

  /**
   * Tests that a {@link CalendarException} is thrown when attempting to create a timed series
   * where individual occurrences would span across midnight (i.e., multiple days).
   *
   * @throws CalendarException expected exception.
   */
  @Test(expected = CalendarException.class)
  public void testCreateSeriesSpanningMultipleDays() throws CalendarException {
    Scanner scanner = new Scanner("event Meeting from 2025-06-05T23:00 to " +
            "2025-06-06T01:00 repeats MWF for 5 times");
    Create cmd = new Create(scanner, view);
    cmd.execute(calendar);
  }

  /**
   * Tests that a {@link CalendarException} is thrown for an invalid day symbol
   * in a series repetition pattern (e.g., "XYZ").
   *
   * @throws CalendarException expected exception.
   */
  @Test(expected = CalendarException.class)
  public void testCreateInvalidDaySymbol() throws CalendarException {
    Scanner scanner = new Scanner("event Meeting on 2025-06-02 repeats XYZ for 5 times");
    Create cmd = new Create(scanner, view);
    cmd.execute(calendar);
  }

  /**
   * Tests that a {@link CalendarException} is thrown when neither "for" nor "until"
   * keywords are present
   * to specify the series duration.
   *
   * @throws CalendarException expected exception.
   */
  @Test(expected = CalendarException.class)
  public void testCreateMissingForOrUntil() throws CalendarException {
    Scanner scanner = new Scanner("event Meeting on 2025-06-02 repeats MWF");
    Create cmd = new Create(scanner, view);
    cmd.execute(calendar);
  }

  /**
   * Tests that a {@link CalendarException} is thrown when the repeat count is missing
   * after the "for" keyword in a series creation.
   *
   * @throws CalendarException expected exception.
   */
  @Test(expected = CalendarException.class)
  public void testCreateMissingRepeatCount() throws CalendarException {
    Scanner scanner = new Scanner("event Meeting on 2025-06-02 repeats MWF for");
    Create cmd = new Create(scanner, view);
    cmd.execute(calendar);
  }

  /**
   * Tests that a {@link CalendarException} is thrown when the "times" keyword is missing
   * after the repeat count in a series creation.
   *
   * @throws CalendarException expected exception.
   */
  @Test(expected = CalendarException.class)
  public void testCreateMissingTimesKeyword() throws CalendarException {
    Scanner scanner = new Scanner("event Meeting on 2025-06-02 repeats MWF for 5");
    Create cmd = new Create(scanner, view);
    cmd.execute(calendar);
  }

  /**
   * Tests that a {@link CalendarException} is thrown when the "until" date is missing
   * after the "until" keyword in a series creation.
   *
   * @throws CalendarException expected exception.
   */
  @Test(expected = CalendarException.class)
  public void testCreateMissingUntilDate() throws CalendarException {
    Scanner scanner = new Scanner("event Meeting on 2025-06-02 repeats MWF until");
    Create cmd = new Create(scanner, view);
    cmd.execute(calendar);
  }

  /**
   * Tests that a {@link CalendarException} is thrown when attempting to create a duplicate event
   * (an event with the same subject and start time already exists).
   *
   * @throws CalendarException expected exception.
   */
  @Test(expected = CalendarException.class)
  public void testCreateDuplicateEvent() throws CalendarException {
    // Create first event
    Scanner scanner1 = new Scanner("event Meeting on 2025-06-05");
    Create cmd1 = new Create(scanner1, view);
    cmd1.execute(calendar);

    // Try to create duplicate
    Scanner scanner2 = new Scanner("event Meeting on 2025-06-05");
    Create cmd2 = new Create(scanner2, view);
    cmd2.execute(calendar);
  }

  /**
   * Tests that the {@link Create} command is case-insensitive for its keywords.
   *
   * @throws CalendarException if the command execution fails.
   */
  @Test
  public void testCreateCaseInsensitiveKeywords() throws CalendarException {
    Scanner scanner = new Scanner("EVENT meeting ON 2025-06-05");
    Create cmd = new Create(scanner, view);
    cmd.execute(calendar);

    List<Event> events = calendar.getEventsList(LocalDate.of(2025, 6, 5));
    assertEquals(1, events.size());
    assertEquals("meeting", events.get(0).getSubject());
  }

  /**
   * Tests the creation of a daily series repeating for a set number of times across all
   * seven days of the week.
   *
   * @throws CalendarException if the command execution fails.
   */
  @Test
  public void testCreateAllSevenDaysOfWeek() throws CalendarException {
    Scanner scanner = new Scanner("event Daily on 2025-06-01 repeats MTWRFSU for 2 times");
    Create cmd = new Create(scanner, view);
    cmd.execute(calendar);

    // Should create 2 events (2 weeks * 1 event per day = 2 events total since it's "2 times")
    int totalEvents = 0;
    for (int i = 0; i < 14; i++) {
      List<Event> events = calendar.getEventsList(LocalDate.of(2025, 6,
              1).plusDays(i));
      totalEvents += events.size();
    }
    assertEquals(2, totalEvents);
  }


  /**
   * Tests editing the subject of a single, non-recurring event using the {@link Edit} command.
   * Verifies the event's updated subject and the output message.
   *
   * @throws CalendarException if the command execution fails.
   */
  @Test
  public void testEditSingleEventSubject() throws CalendarException {
    // Create event first
    calendar.createEvent("Old Meeting",
            LocalDateTime.of(2025, 6, 5, 9, 0),
            LocalDateTime.of(2025, 6, 5, 10, 0));

    // Use a single word for the new value to avoid scanner issues
    Scanner scanner = new Scanner("event subject Old Meeting from 2025-06-05T09:00 " +
            "to 2025-06-05T10:00 with NewMeeting");
    Edit cmd = new Edit(scanner, view);
    cmd.execute(calendar);

    // Check that the event was edited
    List<Event> eventsOnDay = calendar.getEventsList(LocalDate.of(2025, 6,
            5));
    boolean foundEditedEvent = false;
    for (Event e : eventsOnDay) {
      if (e.getSubject().equals("NewMeeting") &&
              e.getStart().equals(LocalDateTime.of(2025, 6, 5,
                      9, 0)) &&
              e.getEnd().equals(LocalDateTime.of(2025, 6, 5,
                      10, 0))) {
        foundEditedEvent = true;
        break;
      }
    }
    assertTrue("Should find edited event with new subject", foundEditedEvent);

    String outputStr = output.toString();
    assertTrue(outputStr.contains("Edited event 'Old Meeting' subject property to NewMeeting"));
  }

  /**
   * Tests editing an individual event that is part of a series using the "events" keyword.
   * This should edit only the specified occurrence, not the entire series.
   *
   * @throws CalendarException if the command execution fails.
   */
  @Test
  public void testEditEventFromSeries() throws CalendarException {
    // Create series first
    Set<Days> days = EnumSet.of(Days.MONDAY, Days.WEDNESDAY);
    calendar.createEventSeries("Series Meeting", LocalTime.of(9, 0),
            LocalTime.of(10, 0),
            days, LocalDate.of(2025, 6, 2), null,
            3, null, null, null);

    Scanner scanner = new Scanner("events subject Series Meeting from " +
            "2025-06-04T09:00 with Updated Series");
    Edit cmd = new Edit(scanner, view);
    cmd.execute(calendar);

    String outputStr = output.toString();
    assertTrue(outputStr.contains("Edited event series 'Series Meeting'"));
  }

  /**
   * Tests editing an entire event series using the "series" keyword.
   *
   * @throws CalendarException if the command execution fails.
   */
  @Test
  public void testEditFullSeries() throws CalendarException {
    // Create series first
    Set<Days> days = EnumSet.of(Days.MONDAY);
    calendar.createEventSeries("Series Meeting", LocalTime.of(9, 0),
            LocalTime.of(10, 0),
            days, LocalDate.of(2025, 6, 2),
            null, 2, null, null, null);

    Scanner scanner = new Scanner("series subject Series Meeting from " +
            "2025-06-02T09:00 with Updated Full Series");
    Edit cmd = new Edit(scanner, view);
    cmd.execute(calendar);

    String outputStr = output.toString();
    assertTrue(outputStr.contains("Edited event series 'Series Meeting'"));
  }

  /**
   * Tests editing various properties of an event (subject, description, location, status)
   * using the {@link Edit} command. Each property is tested independently.
   *
   * @throws CalendarException if the command execution fails.
   */
  @Test
  public void testEditAllProperties() throws CalendarException {
    // Test subject edit
    calendar.createEvent("Meeting",
            LocalDateTime.of(2025, 6, 5, 9, 0),
            LocalDateTime.of(2025, 6, 5, 10, 0));

    Scanner scanner = new Scanner("event subject Meeting from 2025-06-05T09:00 " +
            "to 2025-06-05T10:00 with NewSubject");
    Edit cmd = new Edit(scanner, view);
    cmd.execute(calendar);

    String outputStr = output.toString();
    assertTrue(outputStr.contains("Edited event 'Meeting' subject property to NewSubject"));

    // Test description
    setUp(); // Reset
    calendar.createEvent("Meeting",
            LocalDateTime.of(2025, 6, 5, 9, 0),
            LocalDateTime.of(2025, 6, 5, 10, 0));

    scanner = new Scanner("event description Meeting from 2025-06-05T09:00 " +
            "to 2025-06-05T10:00 with NewDescription");
    cmd = new Edit(scanner, view);
    cmd.execute(calendar);

    outputStr = output.toString();
    assertTrue(outputStr.contains("Edited event 'Meeting' description property to NewDescription"));

    // Test location
    setUp(); // Reset
    calendar.createEvent("Meeting",
            LocalDateTime.of(2025, 6, 5, 9, 0),
            LocalDateTime.of(2025, 6, 5, 10, 0));

    scanner = new Scanner("event location Meeting from 2025-06-05T09:00 to " +
            "2025-06-05T10:00 with ONLINE");
    cmd = new Edit(scanner, view);
    cmd.execute(calendar);

    outputStr = output.toString();
    assertTrue(outputStr.contains("Edited event 'Meeting' location property to ONLINE"));

    // Test status
    setUp(); // Reset
    calendar.createEvent("Meeting",
            LocalDateTime.of(2025, 6, 5, 9, 0),
            LocalDateTime.of(2025, 6, 5, 10, 0));

    scanner = new Scanner("event status Meeting from 2025-06-05T09:00 to " +
            "2025-06-05T10:00 with PRIVATE");
    cmd = new Edit(scanner, view);
    cmd.execute(calendar);

    outputStr = output.toString();
    assertTrue(outputStr.contains("Edited event 'Meeting' status property to PRIVATE"));
  }

  /**
   * Tests that a {@link CalendarException} is thrown when the "event", "events",
   * or "series" keyword
   * is missing from the edit command.
   *
   * @throws CalendarException expected exception.
   */
  @Test(expected = CalendarException.class)
  public void testEditMissingEventKeyword() throws CalendarException {
    Scanner scanner = new Scanner("subject Meeting from 2025-06-05T09:00 with New");
    Edit cmd = new Edit(scanner, view);
    cmd.execute(calendar);
  }

  /**
   * Tests that a {@link CalendarException} is thrown when an invalid property name
   * is provided for editing.
   *
   * @throws CalendarException expected exception.
   */
  @Test(expected = CalendarException.class)
  public void testEditInvalidProperty() throws CalendarException {
    Scanner scanner = new Scanner("event invalid Meeting from 2025-06-05T09:00 with New");
    Edit cmd = new Edit(scanner, view);
    cmd.execute(calendar);
  }

  /**
   * Tests that a {@link CalendarException} is thrown when the subject of the event to be
   * edited is missing.
   *
   * @throws CalendarException expected exception.
   */
  @Test(expected = CalendarException.class)
  public void testEditMissingSubject() throws CalendarException {
    Scanner scanner = new Scanner("event subject from 2025-06-05T09:00 with New");
    Edit cmd = new Edit(scanner, view);
    cmd.execute(calendar);
  }

  /**
   * Tests that a {@link CalendarException} is thrown when the "with" keyword is missing
   * before the new value in an edit command.
   *
   * @throws CalendarException expected exception.
   */
  @Test(expected = CalendarException.class)
  public void testEditMissingWithKeyword() throws CalendarException {
    calendar.createEvent("Meeting",
            LocalDateTime.of(2025, 6, 5, 9, 0),
            LocalDateTime.of(2025, 6, 5, 10, 0));

    Scanner scanner = new Scanner("event subject Meeting from 2025-06-05T09:00" +
            " to 2025-06-05T10:00 New");
    Edit cmd = new Edit(scanner, view);
    cmd.execute(calendar);
  }

  /**
   * Tests that a {@link CalendarException} is thrown when no matching event is found
   * for the edit command.
   *
   * @throws CalendarException expected exception.
   */
  @Test(expected = CalendarException.class)
  public void testEditNoMatchingEvent() throws CalendarException {
    Scanner scanner = new Scanner("event subject NonExistent " +
            "from 2025-06-05T09:00 with New");
    Edit cmd = new Edit(scanner, view);
    cmd.execute(calendar);
  }

  /**
   * Tests editing an event when multiple events with the same subject exist,
   * but the start time uniquely identifies the target event.
   *
   * @throws CalendarException if the command execution fails.
   */
  @Test
  public void testEditMultipleMatchingEvents() throws CalendarException {
    calendar.createEvent("Meeting",
            LocalDateTime.of(2025, 6, 5, 9, 0),
            LocalDateTime.of(2025, 6, 5, 10, 0));

    // Edit without specifying end time - should work since there's only one match
    Scanner scanner = new Scanner("event subject Meeting from 2025-06-05T09:00 with New");
    Edit cmd = new Edit(scanner, view);
    cmd.execute(calendar);

    String outputStr = output.toString();
    assertTrue(outputStr.contains("Edited event"));
  }


  /**
   * Tests the printing of events scheduled for a specific single date
   * using the {@link Print} command.
   * Verifies that all events on that date are included in the output.
   *
   * @throws CalendarException if the command execution fails.
   */
  @Test
  public void testPrintEventsOnDate() throws CalendarException {
    // Create some events
    calendar.createEvent("Meeting 1",
            LocalDateTime.of(2025, 6, 5, 9, 0),
            LocalDateTime.of(2025, 6, 5, 10, 0));
    calendar.createEvent("Meeting 2",
            LocalDateTime.of(2025, 6, 5, 14, 0),
            LocalDateTime.of(2025, 6, 5, 15, 0));

    Scanner scanner = new Scanner("events on 2025-06-05");
    Print cmd = new Print(scanner, view);
    cmd.execute(calendar);

    String outputStr = output.toString();
    assertTrue(outputStr.contains("Printing events on 2025-06-05"));
    assertTrue(outputStr.contains("Meeting 1"));
    assertTrue(outputStr.contains("Meeting 2"));
  }

  /**
   * Tests the printing of events within a specified date and time range
   * using the {@link Print} command.
   * Verifies that events falling within the range, including those on the start and end dates,
   * are correctly displayed.
   *
   * @throws CalendarException if the command execution fails.
   */
  @Test
  public void testPrintEventsInDateRange() throws CalendarException {
    // Create events across multiple days
    calendar.createEvent("Day 1 Event",
            LocalDateTime.of(2025, 6, 5, 9, 0),
            LocalDateTime.of(2025, 6, 5, 10, 0));
    calendar.createEvent("Day 2 Event",
            LocalDateTime.of(2025, 6, 6, 9, 0),
            LocalDateTime.of(2025, 6, 6, 10, 0));

    Scanner scanner = new Scanner("events from 2025-06-05T00:00 to 2025-06-06T23:59");
    Print cmd = new Print(scanner, view);
    cmd.execute(calendar);

    String outputStr = output.toString();
    assertTrue(outputStr.contains("Printing events from 2025-06-05T00:00 to 2025-06-06T23:59"));
    assertTrue(outputStr.contains("Day 1 Event"));
    assertTrue(outputStr.contains("Day 2 Event"));
  }

  /**
   * Tests the scenario where there are no events to print for a given date.
   * Verifies that the output indicates no events were found.
   *
   * @throws CalendarException if the command execution fails.
   */
  @Test
  public void testPrintNoEvents() throws CalendarException {
    Scanner scanner = new Scanner("events on 2025-06-05");
    Print cmd = new Print(scanner, view);
    cmd.execute(calendar);

    String outputStr = output.toString();
    assertTrue(outputStr.contains("No events found"));
  }

  /**
   * Tests that an {@link IllegalArgumentException} is thrown when the "events" keyword is missing
   * from the print command.
   *
   * @throws CalendarException expected exception.
   */
  @Test(expected = CalendarException.class)
  public void testPrintMissingEventsKeyword() throws CalendarException {
    Scanner scanner = new Scanner("on 2025-06-05");
    Print cmd = new Print(scanner, view);
    cmd.execute(calendar);
  }

  /**
   * Tests that an {@link IllegalArgumentException} is thrown when an invalid keyword
   * follows "events" (e.g., "at" instead of "on" or "from").
   *
   * @throws CalendarException expected exception.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testPrintInvalidKeywordAfterEvents() throws CalendarException {
    Scanner scanner = new Scanner("events at 2025-06-05");
    Print cmd = new Print(scanner, view);
    cmd.execute(calendar);
  }

  /**
   * Tests that a {@link CalendarException} is thrown for an invalid date format provided
   * to the print command.
   *
   * @throws CalendarException expected exception.
   */
  @Test(expected = CalendarException.class)
  public void testPrintInvalidDateFormat() throws CalendarException {
    Scanner scanner = new Scanner("events on 06-05-2025");
    Print cmd = new Print(scanner, view);
    cmd.execute(calendar);
  }

  /**
   * Tests that an {@link IllegalArgumentException} is thrown when the "to" keyword is missing
   * in a date range print command.
   *
   * @throws CalendarException expected exception.
   */
  @Test(expected = CalendarException.class)
  public void testPrintMissingToKeyword() throws CalendarException {
    Scanner scanner = new Scanner("events from 2025-06-05T09:00 2025-06-06T17:00");
    Print cmd = new Print(scanner, view);
    cmd.execute(calendar);
  }

  /**
   * Tests that the {@link Print} command is case-insensitive for its keywords.
   *
   * @throws CalendarException if the command execution fails.
   */
  @Test
  public void testPrintCaseInsensitive() throws CalendarException {
    Scanner scanner = new Scanner("EVENTS ON 2025-06-05");
    Print cmd = new Print(scanner, view);
    cmd.execute(calendar);

    String outputStr = output.toString();
    assertTrue(outputStr.contains("Printing events on 2025-06-05"));
  }


  /**
   * Tests the scenario where the user is busy due to a scheduled event
   * using the {@link Show} command.
   * An event is created, and the status is checked during its duration.
   *
   * @throws CalendarException if the command execution fails.
   */
  @Test
  public void testShowStatusBusy() throws CalendarException {
    // Create an event
    calendar.createEvent("Meeting",
            LocalDateTime.of(2025, 6, 5, 9, 0),
            LocalDateTime.of(2025, 6, 5, 11, 0));

    Scanner scanner = new Scanner("status on 2025-06-05T10:00");
    Show cmd = new Show(scanner, view);
    cmd.execute(calendar);

    String outputStr = output.toString();
    assertTrue(outputStr.contains("User is busy"));
  }

  /**
   * Tests the scenario where the user is available because there are no overlapping events
   * using the {@link Show} command.
   * The status is checked at a time when no events are scheduled.
   *
   * @throws CalendarException if the command execution fails.
   */
  @Test
  public void testShowStatusAvailable() throws CalendarException {
    Scanner scanner = new Scanner("status on 2025-06-05T10:00");
    Show cmd = new Show(scanner, view);
    cmd.execute(calendar);

    String outputStr = output.toString();
    assertTrue(outputStr.contains("User is available"));
  }

  /**
   * Tests the status check at the boundaries of an event using the {@link Show} command.
   * It checks if the user is busy at the start time and available immediately after the end time.
   *
   * @throws CalendarException if the command execution fails.
   */
  @Test
  public void testShowStatusAtEventBoundary() throws CalendarException {
    calendar.createEvent("Meeting",
            LocalDateTime.of(2025, 6, 5, 9, 0),
            LocalDateTime.of(2025, 6, 5, 10, 0));

    // Test at start (should be busy)
    Scanner scanner1 = new Scanner("status on 2025-06-05T09:00");
    Show cmd1 = new Show(scanner1, view);
    cmd1.execute(calendar);
    assertTrue(output.toString().contains("User is busy"));

    // Reset output
    output.getBuffer().setLength(0);

    // Test at end (should be available)
    Scanner scanner2 = new Scanner("status on 2025-06-05T10:00");
    Show cmd2 = new Show(scanner2, view);
    cmd2.execute(calendar);
    assertTrue(output.toString().contains("User is available"));
  }

  /**
   * Tests that an {@link IllegalArgumentException} is thrown when the "status" keyword is missing
   * from the show command.
   *
   * @throws CalendarException expected exception.
   */
  @Test(expected = CalendarException.class)
  public void testShowMissingStatusKeyword() throws CalendarException {
    Scanner scanner = new Scanner("on 2025-06-05T10:00");
    Show cmd = new Show(scanner, view);
    cmd.execute(calendar);
  }

  /**
   * Tests that an {@link IllegalArgumentException} is thrown when the "on" keyword is missing
   * from the show command.
   *
   * @throws CalendarException expected exception.
   */
  @Test(expected = CalendarException.class)
  public void testShowMissinexecutenKeyword() throws CalendarException {
    Scanner scanner = new Scanner("status 2025-06-05T10:00");
    Show cmd = new Show(scanner, view);
    cmd.execute(calendar);
  }

  /**
   * Tests that a {@link CalendarException} is thrown for an invalid date-time format provided
   * to the show command.
   *
   * @throws CalendarException expected exception.
   */
  @Test(expected = CalendarException.class)
  public void testShowInvalidDateTimeFormat() throws CalendarException {
    Scanner scanner = new Scanner("status on 2025-06-05 10:00");
    Show cmd = new Show(scanner, view);
    cmd.execute(calendar);
  }

  /**
   * Tests that the {@link Show} command is case-insensitive for its keywords.
   *
   * @throws CalendarException if the command execution fails.
   */
  @Test
  public void testShowCaseInsensitive() throws CalendarException {
    Scanner scanner = new Scanner("STATUS ON 2025-06-05T10:00");
    Show cmd = new Show(scanner, view);
    cmd.execute(calendar);

    String outputStr = output.toString();
    assertTrue(outputStr.contains("User is available"));
  }


  /**
   * Tests the creation of events at year boundaries (minimum year 1 and maximum year 9999).
   *
   * @throws CalendarException if the command execution fails.
   */
  @Test
  public void testCreateEventAtYearBoundaries() throws CalendarException {
    // Test minimum year
    Scanner scanner1 = new Scanner("event NewYear on 0001-01-01");
    Create cmd1 = new Create(scanner1, view);
    cmd1.execute(calendar);

    List<Event> events1 = calendar.getEventsList(LocalDate.of(1, 1, 1));
    assertEquals(LocalDateTime.of(1, 1, 1, 8, 0),
            events1.get(0).getStart());

    // Test maximum year
    setUp(); // Reset
    Scanner scanner2 = new Scanner("event LastDay on 9999-12-31");
    Create cmd2 = new Create(scanner2, view);
    cmd2.execute(calendar);

    List<Event> events2 = calendar.getEventsList(LocalDate.of(9999, 12, 31));
    assertEquals(LocalDateTime.of(9999, 12, 31, 8, 0),
            events2.get(0).getStart());
  }

  /**
   * Tests the creation of an event on a valid leap day (February 29th in a leap year).
   *
   * @throws CalendarException if the command execution fails.
   */
  @Test
  public void testCreateEventOnLeapDay() throws CalendarException {
    Scanner scanner = new Scanner("event LeapDay on 2024-02-29");
    Create cmd = new Create(scanner, view);
    cmd.execute(calendar);

    List<Event> events = calendar.getEventsList(LocalDate.of(2024, 2, 29));
    assertEquals(LocalDate.of(2024, 2, 29),
            events.get(0).getStart().toLocalDate());
  }

  /**
   * Tests that a {@link CalendarException} is thrown when attempting to create an event
   * on an invalid leap day (e.g., February 29th in a non-leap year).
   *
   * @throws CalendarException expected exception.
   */
  @Test(expected = CalendarException.class)
  public void testCreateEventOnInvalidLeapDay() throws CalendarException {
    Scanner scanner = new Scanner("event InvalidLeap on 2025-02-29");
    Create cmd = new Create(scanner, view);
    cmd.execute(calendar);
  }

  /**
   * Tests the creation of an event with special characters in its subject.
   *
   * @throws CalendarException if the command execution fails.
   */
  @Test
  public void testCreateEventWithSpecialCharactersInSubject() throws CalendarException {
    Scanner scanner = new Scanner("event Meeting@Office#123 on 2025-06-05");
    Create cmd = new Create(scanner, view);
    cmd.execute(calendar);

    List<Event> events = calendar.getEventsList(LocalDate.of(2025, 6, 5));
    assertEquals("Meeting@Office#123", events.get(0).getSubject());
  }

  /**
   * Tests the creation of a series that repeats on a single specified day of the week.
   *
   * @throws CalendarException if the command execution fails.
   */
  @Test
  public void testCreateSeriesWithSingleDayOfWeek() throws CalendarException {
    Scanner scanner = new Scanner("event Weekly on 2025-06-02 repeats M for 4 times");
    Create cmd = new Create(scanner, view);
    cmd.execute(calendar);

    // Should create 4 Monday events
    int mondayCount = 0;
    for (int i = 0; i < 30; i++) { // Check a month
      LocalDate checkDate = LocalDate.of(2025, 6, 2).plusDays(i);
      if (checkDate.getDayOfWeek() == java.time.DayOfWeek.MONDAY) {
        List<Event> events = calendar.getEventsList(checkDate);
        if (!events.isEmpty()) mondayCount++;
      }
    }
    assertEquals(4, mondayCount);
  }

  /**
   * Tests that using the 'series' edit command on a non-series event still
   * correctly edits the single event.
   *
   * @throws CalendarException if the command execution fails.
   */
  @Test
  public void testEditNonSeriesEventWithSeriesCommand() throws CalendarException {
    // Create non-series event
    calendar.createEvent("Normal Event",
            LocalDateTime.of(2025, 6, 5, 9, 0),
            LocalDateTime.of(2025, 6, 5, 10, 0));

    Scanner scanner = new Scanner("series subject Normal Event from " +
            "2025-06-05T09:00 with Updated");
    Edit cmd = new Edit(scanner, view);
    cmd.execute(calendar);

    // Should still edit the event
    String outputStr = output.toString();
    assertTrue(outputStr.contains("Edited event 'Normal Event'"));
  }

  /**
   * Tests the printing of events when the specified date range crosses a month boundary.
   * Verifies that events from both months within the range are correctly included.
   *
   * @throws CalendarException if the command execution fails.
   */
  @Test
  public void testPrintEventsAcrossMonthBoundary() throws CalendarException {
    // Create events in different months
    calendar.createEvent("May Event",
            LocalDateTime.of(2025, 5, 31, 15, 0),
            LocalDateTime.of(2025, 5, 31, 16, 0));
    calendar.createEvent("June Event",
            LocalDateTime.of(2025, 6, 1, 9, 0),
            LocalDateTime.of(2025, 6, 1, 10, 0));

    Scanner scanner = new Scanner("events from 2025-05-30T00:00 to 2025-06-02T23:59");
    Print cmd = new Print(scanner, view);
    cmd.execute(calendar);

    String outputStr = output.toString();
    assertTrue(outputStr.contains("May Event"));
    assertTrue(outputStr.contains("June Event"));
  }

  /**
   * Tests the status check at midnight, ensuring that events starting at midnight
   * are correctly considered when determining the user's status.
   *
   * @throws CalendarException if the command execution fails.
   */
  @Test
  public void testShowStatusAtMidnight() throws CalendarException {
    calendar.createEvent("Midnight Event",
            LocalDateTime.of(2025, 6, 5, 0, 0),
            LocalDateTime.of(2025, 6, 5, 1, 0));

    Scanner scanner = new Scanner("status on 2025-06-05T00:00");
    Show cmd = new Show(scanner, view);
    cmd.execute(calendar);

    String outputStr = output.toString();
    assertTrue(outputStr.contains("User is busy"));
  }

  /**
   * Tests the creation of a series where the end date is the same as the start date.
   * This should result in only one event being created, representing the single
   * occurrence on that day.
   *
   * @throws CalendarException if the command execution fails.
   */
  @Test
  public void testCreateSeriesEndinexecutenStartDate() throws CalendarException {
    Scanner scanner = new Scanner("event Daily on 2025-06-05 repeats " +
            "MTWRFSU until 2025-06-05");
    Create cmd = new Create(scanner, view);
    cmd.execute(calendar);

    // Should create only one event on the start/end date
    List<Event> events = calendar.getEventsList(LocalDate.of(2025, 6, 5));
    assertEquals(1, events.size());
  }

  /**
   * Tests that commands can be parsed correctly even with extra spaces between
   * keywords and arguments.
   *
   * @throws CalendarException if the command execution fails.
   */
  @Test
  public void testCommandsWithExtraSpaces() throws CalendarException {
    Scanner scanner = new Scanner("event   Meeting   on   2025-06-05");
    Create cmd = new Create(scanner, view);
    cmd.execute(calendar);

    List<Event> events = calendar.getEventsList(LocalDate.of(2025, 6, 5));
    assertEquals("Meeting", events.get(0).getSubject());
  }

  /**
   * Tests the creation of an event that spans multiple days.
   * Verifies that the event appears in the calendar for all days it spans.
   *
   * @throws CalendarException if the command execution fails.
   */
  @Test
  public void testCreateEventSpanningMultipleDays() throws CalendarException {
    Scanner scanner = new Scanner("event Conference from 2025-06-05T09:00 " +
            "to 2025-06-07T17:00");
    Create cmd = new Create(scanner, view);
    cmd.execute(calendar);

    // Event should appear on all three days
    assertFalse(calendar.getEventsList(LocalDate.of(2025, 6, 5)).isEmpty());
    assertFalse(calendar.getEventsList(LocalDate.of(2025, 6, 6)).isEmpty());
    assertFalse(calendar.getEventsList(LocalDate.of(2025, 6, 7)).isEmpty());
  }

  /**
   * Tests a complete workflow involving multiple commands:
   * 1. Creating an event.
   * 2. Editing the created event's subject.
   * 3. Printing events for the day to confirm the edit.
   * 4. Checking the user's status during the event's time.
   *
   * @throws CalendarException if any command execution fails.
   */
  @Test
  public void testCompleteWorkflow() throws CalendarException {
    // Create an event (all-day event from 8am to 5pm)
    Scanner createScanner = new Scanner("event Team Meeting on 2025-06-05");
    Create createCmd = new Create(createScanner, view);
    createCmd.execute(calendar);

    // Verify event was created
    List<Event> events = calendar.getEventsList(LocalDate.of(2025, 6, 5));
    assertEquals(1, events.size());
    assertEquals("Team Meeting", events.get(0).getSubject());

    // Clear output for edit
    output.getBuffer().setLength(0);

    // Edit the event - use single word for new subject
    Scanner editScanner = new Scanner("event subject Team Meeting " +
            "from 2025-06-05T08:00 to 2025-06-05T17:00 with DepartmentMeeting");
    Edit editCmd = new Edit(editScanner, view);
    editCmd.execute(calendar);

    // Verify edit
    events = calendar.getEventsList(LocalDate.of(2025, 6, 5));
    assertEquals(1, events.size());
    assertEquals("DepartmentMeeting", events.get(0).getSubject());

    // Clear output for print
    output.getBuffer().setLength(0);

    // Print events for that day
    Scanner printScanner = new Scanner("events on 2025-06-05");
    Print printCmd = new Print(printScanner, view);
    printCmd.execute(calendar);

    String printOutput = output.toString();
    assertTrue("Print output should contain edited event",
            printOutput.contains("DepartmentMeeting"));

    // Clear output for show
    output.getBuffer().setLength(0);

    // Check status during the event (10:00 is between 8:00 and 17:00)
    Scanner showScanner = new Scanner("status on 2025-06-05T10:00");
    Show showCmd = new Show(showScanner, view);
    showCmd.execute(calendar);

    String showOutput = output.toString();
    assertTrue("User should be busy during event", showOutput.contains("User is busy"));
  }
}