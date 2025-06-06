package calendar.controller;

import calendar.CalendarException;
import calendar.controller.commands.Create;
import calendar.model.Event;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test class for the Create command.
 */
public class CreateCommandTest extends MethodForTest {

  /**
   * Tests the creation of a single all-day event.
   *
   * @throws CalendarException if the command execution fails.
   */
  @Test
  public void testCreateSingleAllDayEvent() throws CalendarException {
    executeCommand("event Meeting on 2025-06-05", Create.class);

    Event event = getSingleEvent(TestDates.JUNE52025);
    assertEventProperties(event, "Meeting",
            TestDates.dateTime(TestDates.JUNE52025,
                    TestDates.TIME9AM.minusHours(1)),
            TestDates.dateTime(TestDates.JUNE52025, TestDates.TIME5PM));

    assertOutputContains("Event 'Meeting' created from 8am to 5pm on 2025-06-05");
  }

  /**
   * Tests the creation of a single timed event with specific start and end times.
   *
   * @throws CalendarException if the command execution fails.
   */
  @Test
  public void testCreateSingleTimedEvent() throws CalendarException {
    executeCommand("event Conference from 2025-06-05T09:00 to " +
            "2025-06-05T17:00", Create.class);

    Event event = getSingleEvent(TestDates.JUNE52025);
    assertEventProperties(event, "Conference",
            TestDates.dateTime(TestDates.JUNE52025, TestDates.TIME9AM),
            TestDates.dateTime(TestDates.JUNE52025, TestDates.TIME5PM));

    assertOutputContains("Event 'Conference' created from 2025-06-05T09:00 to " +
            "2025-06-05T17:00");
  }

  /**
   * Tests the creation of an event with a multi-word subject.
   *
   * @throws CalendarException if the command execution fails.
   */
  @Test
  public void testCreateEventWithMultiWordSubject() throws CalendarException {
    executeCommand("event Team Building Activity on 2025-06-05", Create.class);

    Event event = getSingleEvent(TestDates.JUNE52025);
    assertEquals("Team Building Activity", event.getSubject());
  }

  /**
   * Tests the creation of an all-day event series with a specified number of occurrences.
   *
   * @throws CalendarException if the command execution fails.
   */
  @Test
  public void testCreateAllDaySeriesWithForCount() throws CalendarException {
    executeCommand("event Meeting on 2025-06-02 repeats MWF for 5 times", Create.class);

    // Check specific days
    Event mondayEvent = getSingleEvent(TestDates.JUNE22025);
    Event wednesdayEvent = getSingleEvent(TestDates.JUNE42025);
    Event fridayEvent = getSingleEvent(TestDates.JUNE62025);

    // Verify series
    assertSameSeries(List.of(mondayEvent, wednesdayEvent, fridayEvent));

    // Verify output
    assertOutputContainsAll("Event series 'Meeting' created", "5 times");
  }

  /**
   * Tests the creation of an all-day event series repeating until a specified date.
   *
   * @throws CalendarException if the command execution fails.
   */
  @Test
  public void testCreateAllDaySeriesWithUntilDate() throws CalendarException {
    executeCommand("event Meeting on 2025-06-02 repeats MWF until 2025-06-30", Create.class);

    // Verify events created up to end date
    getEventsAndAssertCount(TestDates.JUNE302025, 1);

    // Verify no events after end date
    assertNoEvents(LocalDate.of(2025, 7, 2));

    assertOutputContains("until 2025-06-30");
  }

  /**
   * Tests the creation of a timed event series with a specified number of occurrences.
   *
   * @throws CalendarException if the command execution fails.
   */
  @Test
  public void testCreateTimedSeriesWithForCount() throws CalendarException {
    executeCommand("event Meeting from 2025-06-02T09:00 to 2025-06-02T10:00 " +
                    "repeats MWF for 3 times",
            Create.class);

    int eventCount = countEventsInRange(TestDates.JUNE22025, 14);
    assertEquals(3, eventCount);
  }

  /**
   * Tests that the create command is case-insensitive for keywords.
   *
   * @throws CalendarException if the command execution fails.
   */
  @Test
  public void testCreateCaseInsensitiveKeywords() throws CalendarException {
    executeCommand("EVENT meeting ON 2025-06-05", Create.class);

    Event event = getSingleEvent(TestDates.JUNE52025);
    assertEquals("meeting", event.getSubject());
  }

  /**
   * Tests the creation of an all-day series repeating every day of the week.
   *
   * @throws CalendarException if the command execution fails.
   */
  @Test
  public void testCreateAllSevenDaysOfWeek() throws CalendarException {
    executeCommand("event Daily on 2025-06-01 repeats MTWRFSU for 2 times", Create.class);

    int totalEvents = countEventsInRange(LocalDate.of(2025, 6, 1), 14);
    assertEquals(2, totalEvents);
  }

  /**
   * Tests that a {@link CalendarException} is thrown when the "event" keyword is missing.
   *
   * @throws CalendarException expected exception.
   */
  @Test(expected = CalendarException.class)
  public void testCreateMissingEventKeyword() throws CalendarException {
    executeCommand("Meeting on 2025-06-05", Create.class);
  }

  /**
   * Tests that a {@link CalendarException} is thrown for an invalid keyword immediately
   * after "event".
   *
   * @throws CalendarException expected exception.
   */
  @Test(expected = CalendarException.class)
  public void testCreateInvalidKeywordAfterCreate() throws CalendarException {
    executeCommand("meeting on 2025-06-05", Create.class);
  }

  /**
   * Tests that a {@link CalendarException} is thrown when the event subject is missing.
   *
   * @throws CalendarException expected exception.
   */
  @Test(expected = CalendarException.class)
  public void testCreateMissingSubject() throws CalendarException {
    executeCommand("event on 2025-06-05", Create.class);
  }

  /**
   * Tests that a {@link CalendarException} is thrown when neither "on" nor "from"
   * keywords are present
   * for specifying the event time.
   *
   * @throws CalendarException expected exception.
   */
  @Test(expected = CalendarException.class)
  public void testCreateMissingOnOrFrom() throws CalendarException {
    executeCommand("event Meeting", Create.class);
  }

  /**
   * Tests that a {@link CalendarException} is thrown for an invalid date format.
   *
   * @throws CalendarException expected exception.
   */
  @Test(expected = CalendarException.class)
  public void testCreateInvalidDateFormat() throws CalendarException {
    executeCommand("event Meeting on 06-05-2025", Create.class);
  }

  /**
   * Tests that a {@link CalendarException} is thrown for an invalid date-time format.
   *
   * @throws CalendarException expected exception.
   */
  @Test(expected = CalendarException.class)
  public void testCreateInvalidDateTimeFormat() throws CalendarException {
    executeCommand("event Meeting from 2025-06-05 9:00 to 2025-06-05 10:00", Create.class);
  }

  /**
   * Tests that a {@link CalendarException} is thrown when the "to" keyword is missing
   * for a timed event.
   *
   * @throws CalendarException expected exception.
   */
  @Test(expected = CalendarException.class)
  public void testCreateMissingToKeyword() throws CalendarException {
    executeCommand("event Meeting from 2025-06-05T09:00 2025-06-05T10:00", Create.class);
  }

  /**
   * Tests that a {@link CalendarException} is thrown when the event's end time is before
   * its start time.
   *
   * @throws CalendarException expected exception.
   */
  @Test(expected = CalendarException.class)
  public void testCreateEndBeforeStart() throws CalendarException {
    executeCommand("event Meeting from 2025-06-05T10:00 to 2025-06-05T09:00", Create.class);
  }

  /**
   * Tests that a {@link CalendarException} is thrown when attempting to create a timed series
   * where individual occurrences would span multiple days.
   *
   * @throws CalendarException expected exception.
   */
  @Test(expected = CalendarException.class)
  public void testCreateSeriesSpanningMultipleDays() throws CalendarException {
    executeCommand("event Meeting from 2025-06-05T23:00 to 2025-06-06T01:00 " +
                    "repeats MWF for 5 times",
            Create.class);
  }

  /**
   * Tests that a {@link CalendarException} is thrown for an invalid day symbol in a series
   * repetition pattern.
   *
   * @throws CalendarException expected exception.
   */
  @Test(expected = CalendarException.class)
  public void testCreateInvalidDaySymbol() throws CalendarException {
    executeCommand("event Meeting on 2025-06-02 repeats XYZ for 5 times", Create.class);
  }

  /**
   * Tests that a {@link CalendarException} is thrown when "for" or "until" keywords are missing
   * for a series.
   *
   * @throws CalendarException expected exception.
   */
  @Test(expected = CalendarException.class)
  public void testCreateMissingForOrUntil() throws CalendarException {
    executeCommand("event Meeting on 2025-06-02 repeats MWF", Create.class);
  }

  /**
   * Tests that a {@link CalendarException} is thrown when the repeat count is missing for a series.
   *
   * @throws CalendarException expected exception.
   */
  @Test(expected = CalendarException.class)
  public void testCreateMissingRepeatCount() throws CalendarException {
    executeCommand("event Meeting on 2025-06-02 repeats MWF for", Create.class);
  }

  /**
   * Tests that a {@link CalendarException} is thrown when the "times" keyword is missing after
   * the repeat count.
   *
   * @throws CalendarException expected exception.
   */
  @Test(expected = CalendarException.class)
  public void testCreateMissingTimesKeyword() throws CalendarException {
    executeCommand("event Meeting on 2025-06-02 repeats MWF for 5", Create.class);
  }

  /**
   * Tests that a {@link CalendarException} is thrown when the "until" date is missing for a series.
   *
   * @throws CalendarException expected exception.
   */
  @Test(expected = CalendarException.class)
  public void testCreateMissingUntilDate() throws CalendarException {
    executeCommand("event Meeting on 2025-06-02 repeats MWF until", Create.class);
  }

  /**
   * Tests that a {@link CalendarException} is thrown when attempting to create a duplicate event
   * (same subject and start time).
   *
   * @throws CalendarException expected exception.
   */
  @Test(expected = CalendarException.class)
  public void testCreateDuplicateEvent() throws CalendarException {
    executeCommand("event Meeting on 2025-06-05", Create.class);
    executeCommand("event Meeting on 2025-06-05", Create.class);
  }

  // ==================== EDGE CASES ====================

  /**
   * Tests the creation of events at year boundaries (minimum and maximum supported years).
   *
   * @throws CalendarException if the command execution fails.
   */
  @Test
  public void testCreateEventAtYearBoundaries() throws CalendarException {
    // Test minimum year
    executeCommand("event NewYear on 0001-01-01", Create.class);
    Event event1 = getSingleEvent(LocalDate.of(1, 1, 1));
    assertEquals(LocalDateTime.of(1, 1, 1, 8, 0),
            event1.getStart());

    // Test maximum year
    setUp(); // Reset
    executeCommand("event LastDay on 9999-12-31", Create.class);
    Event event2 = getSingleEvent(LocalDate.of(9999, 12, 31));
    assertEquals(LocalDateTime.of(9999, 12, 31, 8, 0),
            event2.getStart());
  }

  /**
   * Tests the creation of an event on a valid leap day.
   *
   * @throws CalendarException if the command execution fails.
   */
  @Test
  public void testCreateEventOnLeapDay() throws CalendarException {
    executeCommand("event LeapDay on 2024-02-29", Create.class);

    Event event = getSingleEvent(LocalDate.of(2024, 2, 29));
    assertEquals(LocalDate.of(2024, 2, 29), event.getStart().toLocalDate());
  }

  /**
   * Tests that a {@link CalendarException} is thrown when attempting to create an event on an
   * invalid leap day
   * (e.g., February 29th in a non-leap year).
   *
   * @throws CalendarException expected exception.
   */
  @Test(expected = CalendarException.class)
  public void testCreateEventOnInvalidLeapDay() throws CalendarException {
    executeCommand("event InvalidLeap on 2025-02-29", Create.class);
  }

  /**
   * Tests the creation of an event with special characters in its subject.
   *
   * @throws CalendarException if the command execution fails.
   */
  @Test
  public void testCreateEventWithSpecialCharactersInSubject() throws CalendarException {
    executeCommand("event Meeting@Office#123 on 2025-06-05", Create.class);

    Event event = getSingleEvent(TestDates.JUNE52025);
    assertEquals("Meeting@Office#123", event.getSubject());
  }

  /**
   * Tests the creation of a series that repeats on a single specified day of the week.
   *
   * @throws CalendarException if the command execution fails.
   */
  @Test
  public void testCreateSeriesWithSingleDayOfWeek() throws CalendarException {
    executeCommand("event Weekly on 2025-06-02 repeats M for 4 times", Create.class);

    int mondayCount = countEventsOnDaysOfWeek(TestDates.JUNE22025, 30,
            java.time.DayOfWeek.MONDAY);
    assertEquals(4, mondayCount);
  }

  /**
   * Tests the creation of a series where the end date is the same as the start date.
   * This should result in only one event being created.
   *
   * @throws CalendarException if the command execution fails.
   */
  @Test
  public void testCreateSeriesEndingOnStartDate() throws CalendarException {
    executeCommand("event Daily on 2025-06-05 repeats MTWRFSU until 2025-06-05",
            Create.class);

    // Should create only one event on the start/end date
    getEventsAndAssertCount(TestDates.JUNE52025, 1);
  }

  /**
   * Tests that the create command can handle extra spaces in the input string.
   *
   * @throws CalendarException if the command execution fails.
   */
  @Test
  public void testCommandsWithExtraSpaces() throws CalendarException {
    executeCommand("event   Meeting   on   2025-06-05", Create.class);

    Event event = getSingleEvent(TestDates.JUNE52025);
    assertEquals("Meeting", event.getSubject());
  }

  /**
   * Tests the creation of an event that spans multiple days.
   *
   * @throws CalendarException if the command execution fails.
   */
  @Test
  public void testCreateEventSpanningMultipleDays() throws CalendarException {
    executeCommand("event Conference from 2025-06-05T09:00 to 2025-06-07T17:00",
            Create.class);

    // Event should appear on all three days
    assertFalse(calendar.getEventsList(TestDates.JUNE52025).isEmpty());
    assertFalse(calendar.getEventsList(TestDates.JUNE62025).isEmpty());
    assertFalse(calendar.getEventsList(LocalDate.of(2025, 6, 7)).isEmpty());
  }
}