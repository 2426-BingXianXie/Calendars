package calendar.controller;

import calendar.CalendarException;
import calendar.controller.commands.*;
import calendar.model.Event;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Integration tests that combine multiple commands.
 */
public class CalendarIntegrationTest extends MethodForTest {

  /**
   * Tests a complete workflow of creating an event, editing it, printing events for the day,
   * and checking the user's status during the event.
   *
   * @throws CalendarException if a calendar operation fails.
   */
  @Test
  public void testCompleteWorkflow() throws CalendarException {
    // Create an event (all-day event from 8am to 5pm)
    executeCommand("event Team Meeting on 2025-06-05", Create.class);

    // Verify event was created
    List<Event> events = getEventsAndAssertCount(TestDates.JUNE52025, 1);
    assertEquals("Team Meeting", events.get(0).getSubject());

    // Edit the event - use single word for new subject
    executeCommand("event subject Team Meeting from 2025-06-05T08:00 to 2025-06-05T17:00 " +
                    "with DepartmentMeeting",
            Edit.class);

    // Verify edit
    events = getEventsAndAssertCount(TestDates.JUNE52025, 1);
    assertEquals("DepartmentMeeting", events.get(0).getSubject());

    // Print events for that day
    String printOutput = executeCommand("events on 2025-06-05", Print.class);
    assertTrue("Print output should contain edited event",
            printOutput.contains("DepartmentMeeting"));

    // Check status during the event (10:00 is between 8:00 and 17:00)
    String showOutput = executeCommand("status on 2025-06-05T10:00", Show.class);
    assertTrue("User should be busy during event",
            showOutput.contains("User is busy"));
  }

  /**
   * Tests a workflow involving creating a recurring event series, editing it from a specific date,
   * printing events in a range to observe the series, and checking the user's busy status during
   * one of the series events.
   *
   * @throws CalendarException if a calendar operation fails.
   */
  @Test
  public void testSeriesWorkflow() throws CalendarException {
    // Create a series
    executeCommand("event Team Standup on 2025-06-02 repeats MWF for 4 times", Create.class);

    // Verify series created
    Event mondayEvent = getSingleEvent(TestDates.JUNE22025);
    assertNotNull(mondayEvent.getSeriesID());

    // Edit the series from a specific date
    executeCommand("events description Team Standup from 2025-06-09T08:00 with RemoteStandup",
            Edit.class);
    assertOutputContains("Edited event series");

    // Print events in range to see the series
    clearOutput();
    executeCommand("events from 2025-06-02T00:00 to 2025-06-13T23:59", Print.class);
    assertOutputContains("Team Standup");

    // Check busy status during one of the events
    clearOutput();
    executeCommand("status on 2025-06-04T09:00", Show.class);
    assertOutputContains("User is busy");
  }

  /**
   * Tests the creation of multiple events on the same day, followed by printing all events
   * for that day and checking the user's status at different times (during an event and
   * between events).
   *
   * @throws CalendarException if a calendar operation fails.
   */
  @Test
  public void testMultipleEventsOnSameDay() throws CalendarException {
    // Create multiple events
    executeCommand("event Morning Meeting from 2025-06-05T09:00 to 2025-06-05T10:00",
            Create.class);
    executeCommand("event Lunch from 2025-06-05T12:00 to 2025-06-05T13:00",
            Create.class);
    executeCommand("event Afternoon Review from 2025-06-05T15:00 to 2025-06-05T16:00",
            Create.class);

    // Print all events
    clearOutput();
    executeCommand("events on 2025-06-05", Print.class);
    assertOutputContainsAll("Morning Meeting", "Lunch", "Afternoon Review");

    // Check status at different times
    clearOutput();
    executeCommand("status on 2025-06-05T09:30", Show.class);
    assertOutputContains("User is busy");

    clearOutput();
    executeCommand("status on 2025-06-05T11:00", Show.class);
    assertOutputContains("User is available");

    clearOutput();
    executeCommand("status on 2025-06-05T12:30", Show.class);
    assertOutputContains("User is busy");
  }

  /**
   * Tests the creation of an event that spans multiple days, verifying its presence across
   * those days, editing one of its properties, and checking the user's status during the
   * multi-day event.
   *
   * @throws CalendarException if a calendar operation fails.
   */
  @Test
  public void testEventSpanningMultipleDays() throws CalendarException {
    // Create multi-day event
    executeCommand("event Conference from 2025-06-05T09:00 to 2025-06-07T17:00",
            Create.class);

    // Verify event appears on all days
    assertFalse(calendar.getEventsList(TestDates.JUNE52025).isEmpty());
    assertFalse(calendar.getEventsList(TestDates.JUNE62025).isEmpty());
    assertFalse(calendar.getEventsList(java.time.LocalDate.of(2025, 6,
            7)).isEmpty());

    // Edit the event
    clearOutput();
    executeCommand("event location Conference from 2025-06-05T09:00 to 2025-06-07T17:00 " +
                    "with PHYSICAL",
            Edit.class);
    assertOutputContains("Edited event 'Conference' location property to PHYSICAL");

    // Check status during the conference
    clearOutput();
    executeCommand("status on 2025-06-06T14:00", Show.class);
    assertOutputContains("User is busy");
  }
}