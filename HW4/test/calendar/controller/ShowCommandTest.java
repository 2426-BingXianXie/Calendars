package calendar.controller;

import calendar.CalendarException;
import calendar.controller.commands.Show;
import org.junit.Test;

/**
 * Test class for the Show command.
 */
public class ShowCommandTest extends MethodForTest {

  /**
   * Tests the scenario where the user is busy due to a scheduled event.
   * An event is created, and the status is checked during its duration.
   *
   * @throws CalendarException if a calendar operation fails.
   */
  @Test
  public void testShowStatusBusy() throws CalendarException {
    // Create an event
    createTestEvent("Meeting",
            TestDates.dateTime(TestDates.JUNE52025, TestDates.TIME9AM),
            TestDates.dateTime(TestDates.JUNE52025, TestDates.TIME10AM.plusHours(1)));

    executeCommand("status on 2025-06-05T10:00", Show.class);

    assertOutputContains("User is busy");
  }

  /**
   * Tests the scenario where the user is available because there are no overlapping events.
   * The status is checked at a time when no events are scheduled.
   *
   * @throws CalendarException if a calendar operation fails.
   */
  @Test
  public void testShowStatusAvailable() throws CalendarException {
    executeCommand("status on 2025-06-05T10:00", Show.class);

    assertOutputContains("User is available");
  }

  /**
   * Tests the status check at the boundaries of an event.
   * It checks if the user is busy at the start time and available immediately after the end time.
   *
   * @throws CalendarException if a calendar operation fails.
   */
  @Test
  public void testShowStatusAtEventBoundary() throws CalendarException {
    createTestEvent("Meeting",
            TestDates.dateTime(TestDates.JUNE52025, TestDates.TIME9AM),
            TestDates.dateTime(TestDates.JUNE52025, TestDates.TIME10AM));

    executeCommand("status on 2025-06-05T09:00", Show.class);
    assertOutputContains("User is busy");

    clearOutput();
    executeCommand("status on 2025-06-05T10:00", Show.class);
    assertOutputContains("User is available");
  }

  /**
   * Tests that the show command is case-insensitive for keywords like "status" and "on".
   *
   * @throws CalendarException if a calendar operation fails.
   */
  @Test
  public void testShowCaseInsensitive() throws CalendarException {
    executeCommand("STATUS ON 2025-06-05T10:00", Show.class);

    assertOutputContains("User is available");
  }

  /**
   * Tests the status check at midnight, ensuring that events starting at midnight
   * are correctly considered when determining the user's status.
   *
   * @throws CalendarException if a calendar operation fails.
   */
  @Test
  public void testShowStatusAtMidnight() throws CalendarException {
    createTestEvent("Midnight Event",
            TestDates.dateTime(TestDates.JUNE52025, java.time.LocalTime.MIDNIGHT),
            TestDates.dateTime(TestDates.JUNE52025, java.time.LocalTime.of(1, 0)));

    executeCommand("status on 2025-06-05T00:00", Show.class);

    assertOutputContains("User is busy");
  }

  /**
   * Tests that an {@link IllegalArgumentException} is thrown when the "status" keyword is missing
   * from the show command.
   */
  @Test
  public void testShowMissingStatusKeyword() {
    expectIllegalArgumentException("on 2025-06-05T10:00", Show.class);
  }

  /**
   * Tests that an {@link IllegalArgumentException} is thrown when the "on" keyword is missing
   * from the show command.
   */
  @Test
  public void testShowMissingOnKeyword() {
    expectIllegalArgumentException("status 2025-06-05T10:00", Show.class);
  }

  /**
   * Tests that a {@link CalendarException} is thrown for an invalid date-time format provided
   * to the show command.
   */
  @Test
  public void testShowInvalidDateTimeFormat() {
    expectCalendarException("status on 2025-06-05 10:00", Show.class);
  }
}