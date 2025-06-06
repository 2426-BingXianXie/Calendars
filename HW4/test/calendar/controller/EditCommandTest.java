package calendar.controller;

import calendar.CalendarException;
import calendar.controller.commands.Edit;
import calendar.model.Event;
import org.junit.Test;
import java.util.List;

import static org.junit.Assert.assertTrue;


/**
 * Test class for the Edit command.
 */
public class EditCommandTest extends CommandTestHelper {

  /**
   * Tests editing the subject of a single, non-recurring event.
   *
   * @throws CalendarException if the command execution fails.
   */
  @Test
  public void testEditSingleEventSubject() throws CalendarException {
    // Create event first
    createTestEvent("Old Meeting",
            TestDates.dateTime(TestDates.JUNE52025, TestDates.TIME9AM),
            TestDates.dateTime(TestDates.JUNE52025, TestDates.TIME10AM));

    executeCommand("event subject Old Meeting from 2025-06-05T09:00 " +
                    "to 2025-06-05T10:00 with NewMeeting",
            Edit.class);

    // Check that the event was edited
    List<Event> eventsOnDay = calendar.getEventsList(TestDates.JUNE52025);
    boolean foundEditedEvent = false;
    for (Event e : eventsOnDay) {
      if (e.getSubject().equals("NewMeeting") &&
              e.getStart().equals(TestDates.dateTime(TestDates.JUNE52025, TestDates.TIME9AM)) &&
              e.getEnd().equals(TestDates.dateTime(TestDates.JUNE52025, TestDates.TIME10AM))) {
        foundEditedEvent = true;
        break;
      }
    }
    assertTrue("Should find edited event with new subject", foundEditedEvent);

    assertOutputContains("Edited event 'Old Meeting' subject property to NewMeeting");
  }

  /**
   * Tests editing an individual event that is part of a series.
   * This should edit only the specified occurrence, not the entire series.
   *
   * @throws CalendarException if the command execution fails.
   */
  @Test
  public void testEditEventFromSeries() throws CalendarException {
    // Create series first
    createTestSeries("Series Meeting", TestDates.TIME9AM, TestDates.TIME10AM,
            DaySets.MW, TestDates.JUNE22025, null, 3);

    executeCommand("events subject Series Meeting from 2025-06-04T09:00 with UpdatedSeries",
            Edit.class);

    assertOutputContains("Edited event series 'Series Meeting'");
  }

  /**
   * Tests editing an entire event series.
   *
   * @throws CalendarException if the command execution fails.
   */
  @Test
  public void testEditFullSeries() throws CalendarException {
    // Create series first
    createTestSeries("Series Meeting", TestDates.TIME9AM, TestDates.TIME10AM,
            DaySets.M, TestDates.JUNE22025, null, 2);

    executeCommand("series subject Series Meeting from 2025-06-02T09:00 " +
                    "with UpdatedFullSeries",
            Edit.class);

    assertOutputContains("Edited event series 'Series Meeting'");
  }

  /**
   * Tests editing various properties of an event (subject, description, location, status).
   * Each property is tested independently.
   *
   * @throws CalendarException if the command execution fails.
   */
  @Test
  public void testEditAllProperties() throws CalendarException {
    // Test subject
    createTestEvent("Meeting",
            TestDates.dateTime(TestDates.JUNE52025, TestDates.TIME9AM),
            TestDates.dateTime(TestDates.JUNE52025, TestDates.TIME10AM));

    executeCommand("event subject Meeting from 2025-06-05T09:00 to " +
                    "2025-06-05T10:00 with NewSubject",
            Edit.class);
    assertOutputContains("Edited event 'Meeting' subject property to NewSubject");

    // Test description
    setUp(); // Reset
    createTestEvent("Meeting",
            TestDates.dateTime(TestDates.JUNE52025, TestDates.TIME9AM),
            TestDates.dateTime(TestDates.JUNE52025, TestDates.TIME10AM));

    executeCommand("event description Meeting from 2025-06-05T09:00 to " +
                    "2025-06-05T10:00 with NewDescription",
            Edit.class);
    assertOutputContains("Edited event 'Meeting' description property to NewDescription");

    // Test location
    setUp(); // Reset
    createTestEvent("Meeting",
            TestDates.dateTime(TestDates.JUNE52025, TestDates.TIME9AM),
            TestDates.dateTime(TestDates.JUNE52025, TestDates.TIME10AM));

    executeCommand("event location Meeting from 2025-06-05T09:00 to " +
                    "2025-06-05T10:00 with ONLINE",
            Edit.class);
    assertOutputContains("Edited event 'Meeting' location property to ONLINE");

    // Test status
    setUp(); // Reset
    createTestEvent("Meeting",
            TestDates.dateTime(TestDates.JUNE52025, TestDates.TIME9AM),
            TestDates.dateTime(TestDates.JUNE52025, TestDates.TIME10AM));

    executeCommand("event status Meeting from 2025-06-05T09:00 to " +
                    "2025-06-05T10:00 with PRIVATE",
            Edit.class);
    assertOutputContains("Edited event 'Meeting' status property to PRIVATE");
  }

  /**
   * Tests editing an event when multiple events with the same subject exist,
   * but the start time uniquely identifies the target event.
   *
   * @throws CalendarException if the command execution fails.
   */
  @Test
  public void testEditMultipleMatchingEvents() throws CalendarException {
    createTestEvent("Meeting",
            TestDates.dateTime(TestDates.JUNE52025, TestDates.TIME9AM),
            TestDates.dateTime(TestDates.JUNE52025, TestDates.TIME10AM));

    // Edit without specifying end time - should work since there's only one match
    executeCommand("event subject Meeting from 2025-06-05T09:00 with New", Edit.class);

    assertOutputContains("Edited event");
  }

  /**
   * Tests that using the 'series' edit command on a non-series event still correctly
   * edits the single event.
   *
   * @throws CalendarException if the command execution fails.
   */
  @Test
  public void testEditNonSeriesEventWithSeriesCommand() throws CalendarException {
    // Create non-series event
    createTestEvent("Normal Event",
            TestDates.dateTime(TestDates.JUNE52025, TestDates.TIME9AM),
            TestDates.dateTime(TestDates.JUNE52025, TestDates.TIME10AM));

    executeCommand("series subject Normal Event from 2025-06-05T09:00" +
            " with Updated", Edit.class);

    // Should still edit the event
    assertOutputContains("Edited event 'Normal Event'");
  }

  /**
   * Tests that a {@link CalendarException} is thrown when the "event" or "events"/"series"
   * keyword is missing.
   *
   * @throws CalendarException expected exception.
   */
  @Test(expected = CalendarException.class)
  public void testEditMissingEventKeyword() throws CalendarException {
    executeCommand("subject Meeting from 2025-06-05T09:00 with New", Edit.class);
  }

  /**
   * Tests that a {@link CalendarException} is thrown when an invalid property name is
   * provided for editing.
   *
   * @throws CalendarException expected exception.
   */
  @Test(expected = CalendarException.class)
  public void testEditInvalidProperty() throws CalendarException {
    executeCommand("event invalid Meeting from 2025-06-05T09:00 with New", Edit.class);
  }

  /**
   * Tests that a {@link CalendarException} is thrown when the subject of the event to be
   * edited is missing.
   *
   * @throws CalendarException expected exception.
   */
  @Test(expected = CalendarException.class)
  public void testEditMissingSubject() throws CalendarException {
    executeCommand("event subject from 2025-06-05T09:00 with New", Edit.class);
  }

  /**
   * Tests that a {@link CalendarException} is thrown when the "with" keyword is missing
   * before the new value.
   *
   * @throws CalendarException expected exception.
   */
  @Test(expected = CalendarException.class)
  public void testEditMissingWithKeyword() throws CalendarException {
    createTestEvent("Meeting",
            TestDates.dateTime(TestDates.JUNE52025, TestDates.TIME9AM),
            TestDates.dateTime(TestDates.JUNE52025, TestDates.TIME10AM));

    executeCommand("event subject Meeting from 2025-06-05T09:00 to 2025-06-05T10:00 New",
            Edit.class);
  }

  /**
   * Tests that a {@link CalendarException} is thrown when no matching event is found
   * for the edit command.
   *
   * @throws CalendarException expected exception.
   */
  @Test(expected = CalendarException.class)
  public void testEditNoMatchingEvent() throws CalendarException {
    executeCommand("event subject NonExistent from 2025-06-05T09:00 with New", Edit.class);
  }
}