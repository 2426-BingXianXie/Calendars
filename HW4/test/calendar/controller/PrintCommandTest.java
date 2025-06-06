package calendar.controller;

import calendar.CalendarException;
import calendar.controller.commands.Print;
import org.junit.Test;
import java.time.LocalDateTime;

/**
 * Test class for the Print command.
 */
public class PrintCommandTest extends CommandTestHelper {

  /**
   * Tests the printing of events scheduled for a specific single date.
   * Verifies that all events on that date are included in the output.
   *
   * @throws CalendarException if a calendar operation fails.
   */
  @Test
  public void testPrintEventsOnDate() throws CalendarException {
    // Create some events
    createTestEvent("Meeting 1",
            TestDates.dateTime(TestDates.JUNE52025, TestDates.TIME9AM),
            TestDates.dateTime(TestDates.JUNE52025, TestDates.TIME10AM));
    createTestEvent("Meeting 2",
            LocalDateTime.of(2025, 6, 5, 14, 0),
            LocalDateTime.of(2025, 6, 5, 15, 0));

    executeCommand("events on 2025-06-05", Print.class);

    assertOutputContainsAll("Printing events on 2025-06-05", "Meeting 1", "Meeting 2");
  }

  /**
   * Tests the printing of events within a specified date and time range.
   * Verifies that events falling within the range, including those on the start and end dates,
   * are correctly displayed.
   *
   * @throws CalendarException if a calendar operation fails.
   */
  @Test
  public void testPrintEventsInDateRange() throws CalendarException {
    // Create events across multiple days
    createTestEvent("Day 1 Event",
            TestDates.dateTime(TestDates.JUNE52025, TestDates.TIME9AM),
            TestDates.dateTime(TestDates.JUNE52025, TestDates.TIME10AM));
    createTestEvent("Day 2 Event",
            TestDates.dateTime(TestDates.JUNE62025, TestDates.TIME9AM),
            TestDates.dateTime(TestDates.JUNE62025, TestDates.TIME10AM));

    executeCommand("events from 2025-06-05T00:00 to 2025-06-06T23:59", Print.class);

    assertOutputContainsAll(
            "Printing events from 2025-06-05T00:00 to 2025-06-06T23:59",
            "Day 1 Event",
            "Day 2 Event"
    );
  }

  /**
   * Tests the scenario where there are no events to print for a given date.
   * Verifies that the output indicates no events were found.
   *
   * @throws CalendarException if a calendar operation fails.
   */
  @Test
  public void testPrintNoEvents() throws CalendarException {
    executeCommand("events on 2025-06-05", Print.class);

    assertOutputContains("No events found");
  }

  /**
   * Tests that the print command is case-insensitive for keywords like "events" and "on".
   *
   * @throws CalendarException if a calendar operation fails.
   */
  @Test
  public void testPrintCaseInsensitive() throws CalendarException {
    executeCommand("EVENTS ON 2025-06-05", Print.class);

    assertOutputContains("Printing events on 2025-06-05");
  }

  /**
   * Tests the printing of events when the specified date range crosses a month boundary.
   * Verifies that events from both months within the range are correctly included.
   *
   * @throws CalendarException if a calendar operation fails.
   */
  @Test
  public void testPrintEventsAcrossMonthBoundary() throws CalendarException {
    // Create events in different months
    createTestEvent("May Event",
            LocalDateTime.of(2025, 5, 31, 15, 0),
            LocalDateTime.of(2025, 5, 31, 16, 0));
    createTestEvent("June Event",
            LocalDateTime.of(2025, 6, 1, 9, 0),
            LocalDateTime.of(2025, 6, 1, 10, 0));

    executeCommand("events from 2025-05-30T00:00 to 2025-06-02T23:59", Print.class);

    assertOutputContainsAll("May Event", "June Event");
  }

  /**
   * Tests that an {@link IllegalArgumentException} is thrown when the "events" keyword is missing
   * from the print command.
   */
  @Test
  public void testPrintMissingEventsKeyword() {
    expectIllegalArgumentException("on 2025-06-05", Print.class);
  }

  /**
   * Tests that an {@link IllegalArgumentException} is thrown when an invalid keyword
   * follows "events" (e.g., "at" instead of "on" or "from").
   */
  @Test
  public void testPrintInvalidKeywordAfterEvents() {
    expectIllegalArgumentException("events at 2025-06-05", Print.class);
  }

  /**
   * Tests that a {@link CalendarException} is thrown for an invalid date format provided
   * to the print command.
   */
  @Test
  public void testPrintInvalidDateFormat() {
    expectCalendarException("events on 06-05-2025", Print.class);
  }

  /**
   * Tests that an {@link IllegalArgumentException} is thrown when the "to" keyword is missing
   * in a date range print command.
   */
  @Test
  public void testPrintMissingToKeyword() {
    expectIllegalArgumentException("events from 2025-06-05T09:00 2025-06-06T17:00", Print.class);
  }
}