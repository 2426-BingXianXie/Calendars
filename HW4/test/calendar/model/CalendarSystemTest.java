package calendar.model;

import org.junit.Before;
import org.junit.Test;

import calendar.CalendarException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.Assert.*;

/**
 * JUnit 4 test class for the {@link CalendarSystem} model,
 * which implements the {@link ICalendarSystem} interface.
 */
public class CalendarSystemTest {

  private ICalendarSystem system;
  private final ZoneId EST = ZoneId.of("America/New_York");
  private final ZoneId PST = ZoneId.of("America/Los_Angeles");
  private final ZoneId UTC = ZoneId.of("UTC");

  /**
   * Sets up the test environment before each test method is executed.
   * Initializes a new {@link CalendarSystem} instance and common {@link LocalDateTime} objects.
   */
  @Before
  public void setUp() {
    system = new CalendarSystem();
  }

  @Test
  public void testCreateCalendarSuccess() throws CalendarException {
    system.createCalendar("Work", EST);
    List<String> names = system.getCalendarNames();
    assertTrue(names.contains("Work"));
    assertEquals(1, names.size());
  }

  @Test
  public void testCreateMultipleCalendars() throws CalendarException {
    system.createCalendar("Work", EST);
    system.createCalendar("Personal", PST);
    system.createCalendar("School", UTC);

    List<String> names = system.getCalendarNames();
    assertEquals(3, names.size());
    assertTrue(names.contains("Work"));
    assertTrue(names.contains("Personal"));
    assertTrue(names.contains("School"));
  }

  @Test(expected = CalendarException.class)
  public void testCreateCalendarWithEmptyName() throws CalendarException {
    system.createCalendar("", EST);
  }

  @Test(expected = CalendarException.class)
  public void testCreateCalendarWithNullName() throws CalendarException {
    system.createCalendar(null, EST);
  }

  @Test(expected = CalendarException.class)
  public void testCreateCalendarWithWhitespaceOnlyName() throws CalendarException {
    system.createCalendar("   ", EST);
  }

  @Test(expected = CalendarException.class)
  public void testCreateCalendarWithDuplicateName() throws CalendarException {
    system.createCalendar("Work", EST);
    system.createCalendar("Work", PST);
  }

  @Test
  public void testCreateCalendarWithDifferentCase() throws CalendarException {
    system.createCalendar("Work", EST);
    // Names should be case-sensitive, so this should succeed
    system.createCalendar("work", PST);

    List<String> names = system.getCalendarNames();
    assertEquals(2, names.size());
    assertTrue(names.contains("Work"));
    assertTrue(names.contains("work"));
  }

  @Test
  public void testCreateCalendarWithWhitespaceInName() throws CalendarException {
    system.createCalendar("  Work Calendar  ", EST);
    List<String> names = system.getCalendarNames();
    assertTrue(names.contains("Work Calendar"));
  }

  @Test
  public void testEditCalendarName() throws CalendarException {
    system.createCalendar("Work", EST);
    system.editCalendar("Work", CalendarProperty.NAME, "Office");

    List<String> names = system.getCalendarNames();
    assertTrue(names.contains("Office"));
    assertFalse(names.contains("Work"));
  }

  @Test
  public void testEditCalendarTimezone() throws CalendarException {
    system.createCalendar("Work", EST);
    system.editCalendar("Work", CalendarProperty.TIMEZONE, "America/Los_Angeles");

    ZoneId timezone = system.getCalendarTimezone("Work");
    assertEquals(PST, timezone);
  }

  @Test(expected = CalendarException.class)
  public void testEditNonExistentCalendar() throws CalendarException {
    system.editCalendar("NonExistent", CalendarProperty.NAME, "NewName");
  }

  @Test(expected = CalendarException.class)
  public void testEditCalendarToEmptyName() throws CalendarException {
    system.createCalendar("Work", EST);
    system.editCalendar("Work", CalendarProperty.NAME, "");
  }

  @Test(expected = CalendarException.class)
  public void testEditCalendarToNullName() throws CalendarException {
    system.createCalendar("Work", EST);
    system.editCalendar("Work", CalendarProperty.NAME, null);
  }

  @Test(expected = CalendarException.class)
  public void testEditCalendarToDuplicateName() throws CalendarException {
    system.createCalendar("Work", EST);
    system.createCalendar("Personal", PST);
    system.editCalendar("Work", CalendarProperty.NAME, "Personal");
  }

  @Test(expected = CalendarException.class)
  public void testEditCalendarToInvalidTimezone() throws CalendarException {
    system.createCalendar("Work", EST);
    system.editCalendar("Work", CalendarProperty.TIMEZONE, "Invalid/Timezone");
  }

  @Test
  public void testEditCalendarToSameName() throws CalendarException {
    system.createCalendar("Work", EST);
    system.editCalendar("Work", CalendarProperty.NAME, "Work");

    List<String> names = system.getCalendarNames();
    assertTrue(names.contains("Work"));
    assertEquals(1, names.size());
  }

  @Test
  public void testEditCalendarUpdateCurrentCalendarReference() throws CalendarException {
    system.createCalendar("Work", EST);
    system.useCalendar("Work");
    assertEquals("Work", system.getCurrentCalendarName());

    // Edit the name of the current calendar
    system.editCalendar("Work", CalendarProperty.NAME, "Office");

    // Current calendar reference should be updated
    assertEquals("Office", system.getCurrentCalendarName());
    assertNotNull(system.getCurrentCalendar());
  }

  @Test
  public void testUseCalendar() throws CalendarException {
    system.createCalendar("Work", EST);
    system.useCalendar("Work");

    assertNotNull(system.getCurrentCalendar());
    assertEquals("Work", system.getCurrentCalendarName());
  }

  @Test(expected = CalendarException.class)
  public void testUseNonExistentCalendar() throws CalendarException {
    system.useCalendar("NonExistent");
  }

  @Test
  public void testSwitchBetweenCalendars() throws CalendarException {
    system.createCalendar("Work", EST);
    system.createCalendar("Personal", PST);

    system.useCalendar("Work");
    assertEquals("Work", system.getCurrentCalendarName());

    system.useCalendar("Personal");
    assertEquals("Personal", system.getCurrentCalendarName());
  }

  @Test
  public void testCurrentCalendarInitiallyNull() {
    assertNull(system.getCurrentCalendar());
    assertNull(system.getCurrentCalendarName());
  }

  @Test
  public void testGetCalendar() throws CalendarException {
    system.createCalendar("Work", EST);
    ICalendar calendar = system.getCalendar("Work");
    assertNotNull(calendar);
  }

  @Test(expected = CalendarException.class)
  public void testGetNonExistentCalendar() throws CalendarException {
    system.getCalendar("NonExistent");
  }

  @Test
  public void testGetCalendarTimezone() throws CalendarException {
    system.createCalendar("Work", EST);
    ZoneId timezone = system.getCalendarTimezone("Work");
    assertEquals(EST, timezone);
  }

  @Test(expected = CalendarException.class)
  public void testGetTimezoneOfNonExistentCalendar() throws CalendarException {
    system.getCalendarTimezone("NonExistent");
  }

  @Test
  public void testGetCalendarNamesEmpty() {
    List<String> names = system.getCalendarNames();
    assertNotNull(names);
    assertTrue(names.isEmpty());
  }

  @Test
  public void testGetCalendarNamesReturnsIndependentList() throws CalendarException {
    system.createCalendar("Cal1", EST);
    system.createCalendar("Cal2", PST);

    List<String> names1 = system.getCalendarNames();
    List<String> names2 = system.getCalendarNames();

    // Should be different list instances
    assertNotSame(names1, names2);

    // Modifying one shouldn't affect the other
    names1.clear();
    assertEquals(2, names2.size());

    // Original system should be unaffected
    List<String> names3 = system.getCalendarNames();
    assertEquals(2, names3.size());
  }

  @Test
  public void testCopyEventBetweenCalendars() throws CalendarException {
    // Setup calendars
    system.createCalendar("Source", EST);
    system.createCalendar("Target", PST);

    // Use source calendar and create event
    system.useCalendar("Source");
    ICalendar sourceCalendar = system.getCurrentCalendar();
    LocalDateTime eventStart = LocalDateTime.of(2024, 1, 15, 10,
            0);
    LocalDateTime eventEnd = LocalDateTime.of(2024, 1, 15, 11,
            0);
    sourceCalendar.createEvent("Meeting", eventStart, eventEnd);

    // Copy event to target calendar (source calendar is current)
    LocalDateTime targetStart = LocalDateTime.of(2024, 1, 20, 14,
            0);
    system.copyEvent("Meeting", eventStart, "Target", targetStart);

    // Verify event was copied
    ICalendar targetCalendar = system.getCalendar("Target");
    List<Event> targetEvents = targetCalendar.getEventsList(LocalDate.of(2024, 1,
            20));
    assertEquals(1, targetEvents.size());
    assertEquals("Meeting", targetEvents.get(0).getSubject());
    assertEquals(targetStart, targetEvents.get(0).getStart());

    // Verify original event still exists
    List<Event> sourceEvents = sourceCalendar.getEventsList(LocalDate.of(2024, 1,
            15));
    assertEquals(1, sourceEvents.size());
    assertEquals("Meeting", sourceEvents.get(0).getSubject());
  }

  @Test
  public void testCopyEventWithDurationPreservation() throws CalendarException {
    system.createCalendar("Source", EST);
    system.createCalendar("Target", PST);

    system.useCalendar("Source");
    ICalendar sourceCalendar = system.getCurrentCalendar();

    // Create event with 2-hour duration
    LocalDateTime eventStart = LocalDateTime.of(2024, 1, 15, 10,
            0);
    LocalDateTime eventEnd = LocalDateTime.of(2024, 1, 15, 12,
            0);
    sourceCalendar.createEvent("Long Meeting", eventStart, eventEnd);

    // Copy to target calendar
    LocalDateTime targetStart = LocalDateTime.of(2024, 1, 20, 14,
            0);
    system.copyEvent("Long Meeting", eventStart, "Target", targetStart);

    // Verify duration is preserved (2 hours)
    ICalendar targetCalendar = system.getCalendar("Target");
    List<Event> targetEvents = targetCalendar.getEventsList(LocalDate.of(2024, 1,
            20));
    assertEquals(1, targetEvents.size());

    Event copiedEvent = targetEvents.get(0);
    assertEquals(targetStart, copiedEvent.getStart());
    assertEquals(targetStart.plusHours(2), copiedEvent.getEnd());
  }

  @Test(expected = CalendarException.class)
  public void testCopyEventWithoutCurrentCalendar() throws CalendarException {
    system.createCalendar("Target", PST);
    LocalDateTime eventStart = LocalDateTime.of(2024, 1, 15, 10,
            0);
    LocalDateTime targetStart = LocalDateTime.of(2024, 1, 20, 14,
            0);
    system.copyEvent("Meeting", eventStart, "Target", targetStart);
  }

  @Test(expected = CalendarException.class)
  public void testCopyEventToNonExistentCalendar() throws CalendarException {
    system.createCalendar("Source", EST);
    system.useCalendar("Source");

    LocalDateTime eventStart = LocalDateTime.of(2024, 1, 15, 10,
            0);
    LocalDateTime targetStart = LocalDateTime.of(2024, 1, 20, 14,
            0);
    system.copyEvent("Meeting", eventStart, "NonExistent", targetStart);
  }

  @Test(expected = CalendarException.class)
  public void testCopyNonExistentEvent() throws CalendarException {
    system.createCalendar("Source", EST);
    system.createCalendar("Target", PST);
    system.useCalendar("Source");

    LocalDateTime eventStart = LocalDateTime.of(2024, 1, 15, 10,
            0);
    LocalDateTime targetStart = LocalDateTime.of(2024, 1, 20, 14,
            0);
    system.copyEvent("NonExistent", eventStart, "Target", targetStart);
  }

  @Test
  public void testCopyEventsOnDate() throws CalendarException {
    // Setup calendars
    system.createCalendar("Source", EST);
    system.createCalendar("Target", PST);

    // Use source calendar and create multiple events on same date
    system.useCalendar("Source");
    ICalendar sourceCalendar = system.getCurrentCalendar();
    LocalDate eventDate = LocalDate.of(2024, 1, 15);

    sourceCalendar.createEvent("Meeting 1",
            LocalDateTime.of(eventDate, LocalTime.of(9, 0)),
            LocalDateTime.of(eventDate, LocalTime.of(10, 0)));
    sourceCalendar.createEvent("Meeting 2",
            LocalDateTime.of(eventDate, LocalTime.of(14, 0)),
            LocalDateTime.of(eventDate, LocalTime.of(15, 0)));

    // Copy events to target date
    LocalDate targetDate = LocalDate.of(2024, 1, 20);
    system.copyEventsOnDate(eventDate, "Target", targetDate);

    // Verify events were copied
    ICalendar targetCalendar = system.getCalendar("Target");
    List<Event> targetEvents = targetCalendar.getEventsList(targetDate);
    assertEquals(2, targetEvents.size());
  }

  @Test(expected = CalendarException.class)
  public void testCopyEventsOnDateWithoutCurrentCalendar() throws CalendarException {
    system.createCalendar("Target", PST);
    LocalDate sourceDate = LocalDate.of(2024, 1, 15);
    LocalDate targetDate = LocalDate.of(2024, 1, 20);
    system.copyEventsOnDate(sourceDate, "Target", targetDate);
  }

  @Test
  public void testCopyEventsWithTimezoneConversion() throws CalendarException {
    system.createCalendar("EST_Cal", EST);
    system.createCalendar("UTC_Cal", UTC);

    system.useCalendar("EST_Cal");
    ICalendar estCalendar = system.getCurrentCalendar();

    // Create event at 3 PM EST
    LocalDateTime eventTime = LocalDateTime.of(2024, 1, 15, 15,
            0);
    estCalendar.createEvent("Meeting", eventTime, eventTime.plusHours(1));

    // Copy to UTC calendar
    system.copyEventsOnDate(LocalDate.of(2024, 1, 15),
            "UTC_Cal", LocalDate.of(2024, 1, 15));

    // Verify event exists in UTC calendar
    ICalendar utcCalendar = system.getCalendar("UTC_Cal");
    List<Event> utcEvents = utcCalendar.getEventsList(LocalDate.of(2024, 1,
            15));
    assertEquals(1, utcEvents.size());
    assertEquals("Meeting", utcEvents.get(0).getSubject());
  }

  @Test
  public void testCopyEventsBetweenDates() throws CalendarException {
    // Setup calendars
    system.createCalendar("Source", EST);
    system.createCalendar("Target", PST);

    // Use source calendar and create events across multiple dates
    system.useCalendar("Source");
    ICalendar sourceCalendar = system.getCurrentCalendar();

    LocalDate startDate = LocalDate.of(2024, 1, 15);
    LocalDate endDate = LocalDate.of(2024, 1, 17);

    // Create events on different dates within range
    sourceCalendar.createEvent("Event 1",
            LocalDateTime.of(2024, 1, 15, 9, 0),
            LocalDateTime.of(2024, 1, 15, 10, 0));
    sourceCalendar.createEvent("Event 2",
            LocalDateTime.of(2024, 1, 16, 14, 0),
            LocalDateTime.of(2024, 1, 16, 15, 0));
    sourceCalendar.createEvent("Event 3",
            LocalDateTime.of(2024, 1, 17, 11, 0),
            LocalDateTime.of(2024, 1, 17, 12, 0));

    // Copy events to target date range starting later
    LocalDate targetStartDate = LocalDate.of(2024, 2, 1);
    system.copyEventsBetweenDates(startDate, endDate, "Target", targetStartDate);

    // Verify events were copied with correct date offset
    ICalendar targetCalendar = system.getCalendar("Target");
    List<Event> targetEvents1 = targetCalendar.getEventsList(LocalDate.of(2024, 2,
            1));
    List<Event> targetEvents2 = targetCalendar.getEventsList(LocalDate.of(2024, 2,
            2));
    List<Event> targetEvents3 = targetCalendar.getEventsList(LocalDate.of(2024, 2,
            3));

    assertEquals(1, targetEvents1.size());
    assertEquals(1, targetEvents2.size());
    assertEquals(1, targetEvents3.size());
    assertEquals("Event 1", targetEvents1.get(0).getSubject());
    assertEquals("Event 2", targetEvents2.get(0).getSubject());
    assertEquals("Event 3", targetEvents3.get(0).getSubject());
  }

  @Test(expected = CalendarException.class)
  public void testCopyEventsBetweenDatesInvalidRange() throws CalendarException {
    system.createCalendar("Source", EST);
    system.createCalendar("Target", PST);
    system.useCalendar("Source");

    LocalDate startDate = LocalDate.of(2024, 1, 20);
    LocalDate endDate = LocalDate.of(2024, 1, 15); // End before start
    LocalDate targetStartDate = LocalDate.of(2024, 2, 1);

    system.copyEventsBetweenDates(startDate, endDate, "Target", targetStartDate);
  }

  @Test(expected = CalendarException.class)
  public void testCopyEventsBetweenDatesWithoutCurrentCalendar() throws CalendarException {
    system.createCalendar("Target", PST);
    LocalDate startDate = LocalDate.of(2024, 1, 15);
    LocalDate endDate = LocalDate.of(2024, 1, 17);
    LocalDate targetStartDate = LocalDate.of(2024, 2, 1);

    system.copyEventsBetweenDates(startDate, endDate, "Target", targetStartDate);
  }

  @Test
  public void testCompleteWorkflow() throws CalendarException {
    // Create multiple calendars
    system.createCalendar("Work", EST);
    system.createCalendar("Personal", PST);

    // Use work calendar and create events
    system.useCalendar("Work");
    ICalendar workCalendar = system.getCurrentCalendar();
    workCalendar.createEvent("Daily Standup",
            LocalDateTime.of(2024, 1, 15, 9, 0),
            LocalDateTime.of(2024, 1, 15, 9, 30));

    // Copy event from work to personal (while still using work calendar)
    system.copyEvent("Daily Standup",
            LocalDateTime.of(2024, 1, 15, 9, 0),
            "Personal",
            LocalDateTime.of(2024, 1, 16, 10, 0));

    // Switch to personal calendar
    system.useCalendar("Personal");
    assertEquals("Personal", system.getCurrentCalendarName());

    // Verify event was copied
    ICalendar personalCalendar = system.getCurrentCalendar();
    List<Event> personalEvents = personalCalendar.getEventsList(LocalDate.of(2024, 1,
            16));
    assertEquals(1, personalEvents.size());
    assertEquals("Daily Standup", personalEvents.get(0).getSubject());

    // Edit calendar name
    system.editCalendar("Personal", CalendarProperty.NAME, "Home");
    assertEquals("Home", system.getCurrentCalendarName());

    // Verify calendar list updated
    List<String> names = system.getCalendarNames();
    assertTrue(names.contains("Work"));
    assertTrue(names.contains("Home"));
    assertFalse(names.contains("Personal"));
  }

  @Test
  public void testTimezoneHandlingInCopyOperations() throws CalendarException {
    // Create calendars in different timezones
    system.createCalendar("EST_Cal", EST);
    system.createCalendar("PST_Cal", PST);

    // Create event in EST calendar
    system.useCalendar("EST_Cal");
    ICalendar estCalendar = system.getCurrentCalendar();
    LocalDateTime eventTime = LocalDateTime.of(2024, 1, 15, 15,
            0);
    estCalendar.createEvent("Conference Call", eventTime, eventTime.plusHours(1));

    // Copy to PST calendar on same date
    system.copyEventsOnDate(LocalDate.of(2024, 1, 15),
            "PST_Cal", LocalDate.of(2024, 1, 15));

    // Verify event exists in PST calendar
    ICalendar pstCalendar = system.getCalendar("PST_Cal");
    List<Event> pstEvents = pstCalendar.getEventsList(LocalDate.of(2024, 1,
            15));
    assertEquals(1, pstEvents.size());
    assertEquals("Conference Call", pstEvents.get(0).getSubject());
  }

  @Test
  public void testCopyEmptyDateRange() throws CalendarException {
    system.createCalendar("Source", EST);
    system.createCalendar("Target", PST);
    system.useCalendar("Source");

    // No events created, so copying should succeed but copy nothing
    LocalDate startDate = LocalDate.of(2024, 1, 15);
    LocalDate endDate = LocalDate.of(2024, 1, 17);
    LocalDate targetStartDate = LocalDate.of(2024, 2, 1);

    system.copyEventsBetweenDates(startDate, endDate, "Target", targetStartDate);

    // Verify no events were copied
    ICalendar targetCalendar = system.getCalendar("Target");
    List<Event> targetEvents = targetCalendar.getEventsList(LocalDate.of(2024, 2,
            1));
    assertTrue(targetEvents.isEmpty());
  }

  @Test
  public void testCopyToSameCalendar() throws CalendarException {
    system.createCalendar("Source", EST);
    system.useCalendar("Source");

    ICalendar calendar = system.getCurrentCalendar();
    LocalDateTime eventStart = LocalDateTime.of(2024, 1, 15, 10,
            0);
    calendar.createEvent("Meeting", eventStart, eventStart.plusHours(1));

    // Copy event to same calendar but different time
    LocalDateTime targetStart =  LocalDateTime.of(2024, 1, 16, 14,
            0);
    system.copyEvent("Meeting", eventStart, "Source", targetStart);

    // Verify both events exist
    List<Event> events15 = calendar.getEventsList(LocalDate.of(2024, 1, 15));
    List<Event> events16 = calendar.getEventsList(LocalDate.of(2024, 1, 16));

    assertEquals(1, events15.size());
    assertEquals(1, events16.size());
    assertEquals("Meeting", events15.get(0).getSubject());
    assertEquals("Meeting", events16.get(0).getSubject());
  }
}