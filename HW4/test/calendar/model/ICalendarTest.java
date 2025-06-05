package calendar.model;

import org.junit.Before;
import org.junit.Test;
import calendar.CalendarException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;

import static org.junit.Assert.*;

/**
 * JUnit 4 test class for the {@link VirtualCalendar} model,
 * which implements the {@link ICalendar} interface.
 */
public class ICalendarTest {

  private VirtualCalendar calendar;
  private LocalDateTime today9AM;
  private LocalDateTime today10AM;
  private LocalDateTime tomorrow9AM;
  private LocalDateTime tomorrow10AM;
  private LocalDateTime dayAfterTomorrow9AM;

  /**
   * Sets up the test environment before each test method is executed.
   * Initializes a new {@link VirtualCalendar} instance and common {@link LocalDateTime} objects.
   */
  @Before
  public void setUp() {
    calendar = new VirtualCalendar();
    today9AM = LocalDateTime.of(2025, 6, 4, 9, 0);
    today10AM = LocalDateTime.of(2025, 6, 4, 10, 0);
    tomorrow9AM = LocalDateTime.of(2025, 6, 5, 9, 0);
    tomorrow10AM = LocalDateTime.of(2025, 6, 5, 10, 0);
    dayAfterTomorrow9AM = LocalDateTime.of(2025, 6, 6, 9, 0);
  }

  /**
   * Tests the constructor of {@link VirtualCalendar}.
   * Asserts that a newly created calendar has no events for the current date.
   */
  @Test
  public void testVirtualCalendarConstructor() {
    assertTrue(calendar.getEventsList(LocalDate.now()).isEmpty());
  }


  /**
   * Tests the {@code createEvent} method with valid inputs for a single-day event.
   * Asserts that the event is created successfully, has the correct properties,
   * and is present in the calendar's event list for the corresponding day.
   *
   * @throws CalendarException if the event creation fails (unexpected for valid input).
   */
  @Test
  public void testCreateEventValidSingleEvent() throws CalendarException {
    Event event = calendar.createEvent("Test Event", today9AM, today10AM);
    assertNotNull(event);
    assertEquals("Test Event", event.getSubject());
    assertEquals(today9AM, event.getStart());
    assertEquals(today10AM, event.getEnd());

    List<Event> eventsOnDay = calendar.getEventsList(today9AM.toLocalDate());
    assertEquals(1, eventsOnDay.size());
    assertTrue(eventsOnDay.contains(event));
  }

  /**
   * Tests the {@code createEvent} method for an event that spans multiple days.
   * Asserts that the event is correctly added to the calendar and appears in
   * the event lists for all days it spans.
   *
   * @throws CalendarException if the event creation fails.
   */
  @Test
  public void testCreateEventEventSpanningMultipleDays() throws CalendarException {
    LocalDateTime start = LocalDateTime.of(2025, 6, 4, 22, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 5, 2, 0);
    Event event = calendar.createEvent("Overnight Event", start, end);

    List<Event> eventsOnDay1 = calendar.getEventsList(start.toLocalDate());
    assertTrue(eventsOnDay1.contains(event));
    List<Event> eventsOnDay2 = calendar.getEventsList(end.toLocalDate());
    assertTrue(eventsOnDay2.contains(event));
  }

  /**
   * Tests the {@code createEvent} method with an invalid scenario where the start time
   * is after the end time.
   * Expects a {@link CalendarException}.
   *
   * @throws CalendarException expected exception for invalid event times.
   */
  @Test(expected = CalendarException.class)
  public void testCreateEventStartTimeAfterEndTime() throws CalendarException {
    calendar.createEvent("Invalid Event", today10AM, today9AM);
  }

  /**
   * Tests the {@code createEvent} method with a duplicate event.
   * Expects a {@link CalendarException} as duplicate events are not allowed.
   *
   * @throws CalendarException expected exception for creating a duplicate event.
   */
  @Test(expected = CalendarException.class)
  public void testCreateEventDuplicateEvent() throws CalendarException {
    calendar.createEvent("Duplicate", today9AM, today10AM);
    calendar.createEvent("Duplicate", today9AM, today10AM);
  }

  /**
   * Tests the {@code createEvent} method for an implicitly all-day event.
   * Verifies that an event spanning almost a full day is handled correctly.
   *
   * @throws CalendarException if the event creation fails.
   */
  @Test
  public void testCreateEventAllDayEventImplicitly() throws CalendarException {
    LocalDateTime start = LocalDate.of(2025, 6, 4).atStartOfDay();
    LocalDateTime end = LocalDate.of(2025, 6, 4).atTime(23,
            59, 59);
    Event event = calendar.createEvent("All Day Meeting", start, end);
    assertNotNull(event);
    assertEquals(start, event.getStart());
    assertEquals(end, event.getEnd());
    List<Event> events = calendar.getEventsList(LocalDate.of(2025, 6, 4));
    assertTrue(events.contains(event));
  }

  /**
   * Tests the {@code createEventSeries} method for a count-based series.
   * Verifies that the correct number of events are generated and added to the calendar
   * on the specified recurrence days.
   *
   * @throws CalendarException if the event series creation fails.
   */
  @Test
  public void testCreateEventSeriesCountBased() throws CalendarException {
    Set<Days> days = EnumSet.of(Days.MONDAY, Days.WEDNESDAY);
    calendar.createEventSeries("Series Test", LocalTime.of(9, 0),
            LocalTime.of(10, 0),
            days, LocalDate.of(2025, 6, 2), null, 5,
            "Series Description", Location.PHYSICAL, EventStatus.PUBLIC);

    // Verify first event
    List<Event> eventsOnMon = calendar.getEventsList(LocalDate.of(2025, 6,
            2));
    assertEquals(1, eventsOnMon.size());
    assertEquals("Series Test", eventsOnMon.get(0).getSubject());

    // Verify second event
    List<Event> eventsOnWed = calendar.getEventsList(LocalDate.of(2025, 6,
            4));
    assertEquals(1, eventsOnWed.size());

    // Verify a later event
    List<Event> eventsOnNextMon = calendar.getEventsList(LocalDate.of(2025, 6,
            9));
    assertEquals(1, eventsOnNextMon.size());

    // Collect all events belonging to this series and count distinct occurrences
    List<Event> allSeriesEvents = new ArrayList<>();
    for (LocalDate date = LocalDate.of(2025, 6, 2);
         date.isBefore(LocalDate.of(2025, 6, 17));
         date = date.plusDays(1)) {
      for (Event e : calendar.getEventsList(date)) {
        if (e.getSubject().equals("Series Test") && e.getSeriesID() != null) {
          allSeriesEvents.add(e);
        }
      }
    }
    long distinctSeriesEventsCount = allSeriesEvents.stream().distinct().count();
    assertEquals(5, distinctSeriesEventsCount);
  }

  /**
   * Tests the {@code createEventSeries} method for a date-based series.
   * Verifies that events are generated on the specified recurrence days up to the end date,
   * and no events are generated after the end date.
   *
   * @throws CalendarException if the event series creation fails.
   */
  @Test
  public void testCreateEventSeriesDateBased() throws CalendarException {
    Set<Days> days = EnumSet.of(Days.TUESDAY, Days.THURSDAY);
    calendar.createEventSeries("Date Series", LocalTime.of(14, 0),
            LocalTime.of(15, 0),
            days, LocalDate.of(2025, 6, 3),
            LocalDate.of(2025, 6, 10), 0,
            "Date Series Desc", Location.ONLINE, EventStatus.PRIVATE);

    // Verify first event
    List<Event> eventsOnTue = calendar.getEventsList(LocalDate.of(2025,
            6, 3));
    assertEquals(1, eventsOnTue.size());
    assertEquals("Date Series", eventsOnTue.get(0).getSubject());

    // Verify an intermediate event
    List<Event> eventsOnThu = calendar.getEventsList(LocalDate.of(2025,
            6, 5));
    assertEquals(1, eventsOnThu.size());

    // Verify event on the end date
    List<Event> eventsOnNextTue = calendar.getEventsList(LocalDate.of(2025,
            6, 10));
    assertEquals(1, eventsOnNextTue.size());

    // Verify no events after the end date
    List<Event> eventsOnNextThu = calendar.getEventsList(LocalDate.of(2025,
            6, 12));
    assertTrue(eventsOnNextThu.isEmpty());
  }

  /**
   * Tests the scenario where creating an event series fails due to a duplicate event.
   * This might occur if a generated event from the series clashes with an existing event.
   * Expects a {@link CalendarException}.
   *
   * @throws CalendarException expected exception if an event in the series is a duplicate.
   */
  @Test(expected = CalendarException.class)
  public void testCreateEventSeriesEventCreationFailsInSeries() throws CalendarException {
    Set<Days> days = EnumSet.of(Days.MONDAY);
    // Create a series that generates an event on June 2nd, 9-10 AM
    calendar.createEventSeries("Duplicate Series Event",
            LocalTime.of(9, 0), LocalTime.of(10, 0),
            days, LocalDate.of(2025, 6, 2),
            LocalDate.of(2025, 6, 2), 1,
            "Description", Location.ONLINE, EventStatus.PUBLIC);

    // Attempt to create another series (or single event) that clashes with the first one
    calendar.createEventSeries("Duplicate Series Event",
            LocalTime.of(9, 0), LocalTime.of(10, 0),
            days, LocalDate.of(2025, 6, 2),
            LocalDate.of(2025, 6, 2), 1,
            "Description", Location.ONLINE, EventStatus.PUBLIC);
  }


  /**
   * Tests the {@code getEventsBySubjectAndStartTime} method when matching events are found.
   * Asserts that only the events matching both subject and start time are returned.
   *
   * @throws CalendarException if event creation fails.
   */
  @Test
  public void testGetEventsBySubjectAndStartTimeFound() throws CalendarException {
    Event event1 = calendar.createEvent("Meeting", today9AM, today10AM);
    Event event2 = calendar.createEvent("Meeting", tomorrow9AM, tomorrow10AM);
    Event event3 = calendar.createEvent("Other Event", today9AM, today10AM);

    List<Event> result = calendar.getEventsBySubjectAndStartTime("Meeting", today9AM);
    assertEquals(1, result.size());
    assertTrue(result.contains(event1));
    assertFalse(result.contains(event2));
    assertFalse(result.contains(event3));
  }

  /**
   * Tests the {@code getEventsBySubjectAndStartTime} method when no matching events are found.
   * Asserts that an empty list is returned.
   *
   * @throws CalendarException if event creation fails.
   */
  @Test
  public void testGetEventsBySubjectAndStartTimeNotFound() throws CalendarException {
    calendar.createEvent("Meeting", today9AM, today10AM);
    List<Event> result = calendar.getEventsBySubjectAndStartTime("Non Existent", today9AM);
    assertTrue(result.isEmpty());
  }

  /**
   * Tests the {@code getEventsBySubjectAndStartTime} method with case-insensitive subject matching.
   * Asserts that events are found regardless of subject casing.
   *
   * @throws CalendarException if event creation fails.
   */
  @Test
  public void testGetEventsBySubjectAndStartTimeCaseInsensitiveSubject() throws CalendarException {
    Event event = calendar.createEvent("Case Test", today9AM, today10AM);
    List<Event> result = calendar.getEventsBySubjectAndStartTime("case test", today9AM);
    assertEquals(1, result.size());
    assertTrue(result.contains(event));
  }


  /**
   * Tests the {@code getEventsByDetails} method when matching events are found.
   * Asserts that only the events matching subject, start time, and end time are returned.
   *
   * @throws CalendarException if event creation fails.
   */
  @Test
  public void testGetEventsByDetailsFound() throws CalendarException {
    Event event1 = calendar.createEvent("Project Sync", today9AM, today10AM);
    Event event2 = calendar.createEvent("Project Sync", tomorrow9AM, tomorrow10AM);
    Event event3 = calendar.createEvent("Project Sync", today9AM, tomorrow10AM);

    List<Event> result = calendar.getEventsByDetails("Project Sync", today9AM, today10AM);
    assertEquals(1, result.size());
    assertTrue(result.contains(event1));
  }

  /**
   * Tests the {@code getEventsByDetails} method when no matching events are found.
   * Asserts that an empty list is returned.
   *
   * @throws CalendarException if event creation fails.
   */
  @Test
  public void testGetEventsByDetailsNotFound() throws CalendarException {
    calendar.createEvent("Project Sync", today9AM, today10AM);
    List<Event> result = calendar.getEventsByDetails("Project Sync",
            today9AM, today9AM.plusMinutes(30));
    assertTrue(result.isEmpty());
  }


  /**
   * Tests the {@code getEventsList} method for a date that has multiple events.
   * Asserts that all events on that specific date are returned.
   *
   * @throws CalendarException if event creation fails.
   */
  @Test
  public void testGetEventsListDateWithEvents() throws CalendarException {
    Event event1 = calendar.createEvent("Morning Event", today9AM, today10AM);
    Event event2 = calendar.createEvent("Afternoon Event", today10AM,
            today10AM.plusHours(1));

    List<Event> events = calendar.getEventsList(today9AM.toLocalDate());
    assertEquals(2, events.size());
    assertTrue(events.contains(event1));
    assertTrue(events.contains(event2));
  }

  /**
   * Tests the {@code getEventsList} method for a date that has no events.
   * Asserts that an empty list is returned.
   */
  @Test
  public void testGetEventsListDateWithoutEvents() {
    List<Event> events = calendar.getEventsList(tomorrow9AM.toLocalDate());
    assertTrue(events.isEmpty());
  }

  /**
   * Tests the {@code getEventsList} method for an event that spans multiple days.
   * Asserts that the overnight event is listed on both days it spans.
   *
   * @throws CalendarException if event creation fails.
   */
  @Test
  public void testGetEventsListEventSpanningMultipleDaysIsListedOnBoth() throws CalendarException {
    LocalDateTime start = LocalDateTime.of(2025, 6, 4, 23, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 5, 1, 0);
    Event overnightEvent = calendar.createEvent("Overnight Event", start, end);

    List<Event> eventsDay1 = calendar.getEventsList(start.toLocalDate());
    assertEquals(1, eventsDay1.size());
    assertTrue(eventsDay1.contains(overnightEvent));

    List<Event> eventsDay2 = calendar.getEventsList(end.toLocalDate());
    assertEquals(1, eventsDay2.size());
    assertTrue(eventsDay2.contains(overnightEvent));
  }


  /**
   * Tests the {@code getEventsListInDateRange} method for a range covering multiple days
   * with events. Asserts that all events within the date range are returned.
   *
   * @throws CalendarException if event creation fails.
   */
  @Test
  public void testGetEventsListInDateRangeMultipleDays() throws CalendarException {
    Event event1 = calendar.createEvent("Day 1 Event", today9AM, today10AM);
    Event event2 = calendar.createEvent("Day 2 Event", tomorrow9AM, tomorrow10AM);
    Event event3 = calendar.createEvent("Day 3 Event", dayAfterTomorrow9AM,
            dayAfterTomorrow9AM.plusHours(1));

    List<Event> result = calendar.getEventsListInDateRange(today9AM.minusDays(1),
            dayAfterTomorrow9AM.plusDays(1));
    assertEquals(3, result.size());
    assertTrue(result.contains(event1));
    assertTrue(result.contains(event2));
    assertTrue(result.contains(event3));
  }

  /**
   * Tests the {@code getEventsListInDateRange} method for a range that partially overlaps an event.
   * Asserts that the event is still included if any part of it falls within the range.
   *
   * @throws CalendarException if event creation fails.
   */
  @Test
  public void testGetEventsListInDateRangePartialOverlap() throws CalendarException {
    Event event = calendar.createEvent("Partial", today9AM, today10AM); // 9:00 - 10:00

    // Range: 9:30 - 10:30 (overlaps end)
    List<Event> result1 = calendar.getEventsListInDateRange(today9AM.plusMinutes(30),
            today10AM.plusMinutes(30));
    assertEquals(1, result1.size());
    assertTrue(result1.contains(event));

    // Range: 8:00 - 9:30 (overlaps start)
    List<Event> result2 = calendar.getEventsListInDateRange(today9AM.minusHours(1),
            today9AM.plusMinutes(30));
    assertEquals(1, result2.size());
    assertTrue(result2.contains(event));

    // Range: 10:30 - 11:00 (no overlap)
    List<Event> result3 = calendar.getEventsListInDateRange(today10AM.plusMinutes(30),
            today10AM.plusHours(1));
    assertTrue(result3.isEmpty());
  }

  /**
   * Tests the {@code getEventsListInDateRange} method when there are no events in the calendar.
   * Asserts that an empty list is returned.
   *
   * @throws CalendarException if event creation fails.
   */
  @Test
  public void testGetEventsListInDateRangeNoEvents() throws CalendarException {
    List<Event> result = calendar.getEventsListInDateRange(today9AM, today10AM);
    assertTrue(result.isEmpty());
  }

  /**
   * Tests the {@code getEventsListInDateRange} method with an invalid range where the
   * start date/time is after the end date/time.
   * Expects an {@link IllegalArgumentException}.
   *
   * @throws CalendarException if event creation fails.
   * @throws IllegalArgumentException expected exception for invalid date range.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testGetEventsListInDateRangeStartAfterEnd() throws CalendarException {
    calendar.getEventsListInDateRange(today10AM, today9AM);
  }

  /**
   * Tests the {@code isBusyAt} method when the queried time falls within an event.
   * Asserts that the method returns true.
   *
   * @throws CalendarException if event creation fails.
   */
  @Test
  public void testIsBusyAtWithinEvent() throws CalendarException {
    calendar.createEvent("Busy Slot", today9AM, today10AM);
    assertTrue(calendar.isBusyAt(today9AM.plusMinutes(30)));
  }

  /**
   * Tests the {@code isBusyAt} method when the queried time is exactly at the start of an event.
   * Asserts that the method returns true (start time is inclusive).
   *
   * @throws CalendarException if event creation fails.
   */
  @Test
  public void testIsBusyAtExactlyAtStart() throws CalendarException {
    calendar.createEvent("Busy Slot", today9AM, today10AM);
    assertTrue(calendar.isBusyAt(today9AM));
  }

  /**
   * Tests the {@code isBusyAt} method when the queried time is exactly at the end of an event.
   * Asserts that the method returns false (end time is exclusive).
   *
   * @throws CalendarException if event creation fails.
   */
  @Test
  public void testIsBusyAtExactlyAtEnd() throws CalendarException {
    calendar.createEvent("Busy Slot", today9AM, today10AM);
    assertFalse(calendar.isBusyAt(today10AM));
  }

  /**
   * Tests the {@code isBusyAt} method when the queried time is just before an event.
   * Asserts that the method returns false.
   *
   * @throws CalendarException if event creation fails.
   */
  @Test
  public void testIsBusyAtBeforeEvent() throws CalendarException {
    calendar.createEvent("Busy Slot", today9AM, today10AM);
    assertFalse(calendar.isBusyAt(today9AM.minusMinutes(1)));
  }

  /**
   * Tests the {@code isBusyAt} method when the queried time is just after an event.
   * Asserts that the method returns false.
   *
   * @throws CalendarException if event creation fails.
   */
  @Test
  public void testIsBusyAtAfterEvent() throws CalendarException {
    calendar.createEvent("Busy Slot", today9AM, today10AM);
    assertFalse(calendar.isBusyAt(today10AM.plusMinutes(1)));
  }

  /**
   * Tests the {@code isBusyAt} method when there are no events in the calendar.
   * Asserts that the method returns false.
   */
  @Test
  public void testIsBusyAtNoEvents() {
    assertFalse(calendar.isBusyAt(today9AM));
  }

  /**
   * Tests the {@code isBusyAt} method's handling of an overnight event.
   * Asserts that the method returns true for times within the event's duration,
   * even if it crosses a day boundary, and false at the exclusive end.
   *
   * @throws CalendarException if event creation fails.
   */
  @Test
  public void testIsBusyAtOvernightEventCoverage() throws CalendarException {
    LocalDateTime overnightStart = LocalDateTime.of(2025, 6, 4,
            23, 0);
    LocalDateTime overnightEnd = LocalDateTime.of(2025, 6, 5,
            1, 0);
    calendar.createEvent("Overnight Event", overnightStart, overnightEnd);

    assertTrue(calendar.isBusyAt(overnightStart.plusMinutes(30)));
    assertTrue(calendar.isBusyAt(overnightEnd.minusMinutes(1)));
    assertFalse(calendar.isBusyAt(overnightEnd));
  }

  /**
   * Tests the {@code editEvent} method for changing the subject of an event.
   * Asserts that the subject is updated correctly in the event object and in the calendar.
   *
   * @throws CalendarException if event creation or editing fails.
   */
  @Test
  public void testEditEventSubject() throws CalendarException {
    Event event = calendar.createEvent("Old Subject", today9AM, today10AM);
    Event editedEvent = calendar.editEvent(event.getId(), Property.SUBJECT, "New Subject");
    assertEquals("New Subject", editedEvent.getSubject());
    assertEquals("New Subject",
            calendar.getEventsList(today9AM.toLocalDate()).get(0).getSubject());
  }

  /**
   * Tests the {@code editEvent} method for changing the start time of an event.
   * Asserts that the start time is updated correctly and the event remains in the calendar.
   *
   * @throws CalendarException if event creation or editing fails.
   */
  @Test
  public void testEditEventStartTime() throws CalendarException {
    Event event = calendar.createEvent("Edit Time", today9AM, today10AM);
    LocalDateTime newStartTime = today9AM.minusHours(1);
    Event editedEvent = calendar.editEvent(event.getId(), Property.START, newStartTime.toString());
    assertEquals(newStartTime, editedEvent.getStart());
    List<Event> eventsOnDay = calendar.getEventsList(today9AM.toLocalDate());
    assertEquals(1, eventsOnDay.size());
    assertEquals(newStartTime, eventsOnDay.get(0).getStart());
  }

  /**
   * Tests the {@code editEvent} method when attempting to set a start time
   * that is after the current end time, creating an invalid duration.
   * Expects a {@link CalendarException}.
   *
   * @throws CalendarException expected exception for invalid time edit.
   */
  @Test(expected = CalendarException.class)
  public void testEditEventStartTimeAfterEndTime() throws CalendarException {
    Event event = calendar.createEvent("Test", today9AM, today10AM);
    calendar.editEvent(event.getId(), Property.START, today10AM.plusMinutes(1).toString());
  }

  /**
   * Tests the {@code editEvent} method for changing the end time of an event.
   * Asserts that the end time is updated correctly and the event remains in the calendar.
   *
   * @throws CalendarException if event creation or editing fails.
   */
  @Test
  public void testEditEventEndTime() throws CalendarException {
    Event event = calendar.createEvent("Edit Time", today9AM, today10AM);
    LocalDateTime newEndTime = today10AM.plusHours(1);
    Event editedEvent = calendar.editEvent(event.getId(), Property.END, newEndTime.toString());
    assertEquals(newEndTime, editedEvent.getEnd());
    assertEquals(1, calendar.getEventsList(today9AM.toLocalDate()).size());
  }

  /**
   * Tests the {@code editEvent} method when attempting to set an end time
   * that is before the current start time, creating an invalid duration.
   * Expects a {@link CalendarException}.
   *
   * @throws CalendarException expected exception for invalid time edit.
   */
  @Test(expected = CalendarException.class)
  public void testEditEventEndTimeBeforeStartTime() throws CalendarException {
    Event event = calendar.createEvent("Test", today9AM, today10AM);
    calendar.editEvent(event.getId(), Property.END, today9AM.minusMinutes(1).toString());
  }

  /**
   * Tests the {@code editEvent} method for changing the description of an event.
   * Asserts that the description is updated correctly.
   *
   * @throws CalendarException if event creation or editing fails.
   */
  @Test
  public void testEditEventDescription() throws CalendarException {
    Event event = calendar.createEvent("Desc Test", today9AM, today10AM);
    Event editedEvent = calendar.editEvent(event.getId(), Property.DESCRIPTION,
            "New Description");
    assertEquals("New Description", editedEvent.getDescription());
  }

  /**
   * Tests the {@code editEvent} method for changing the location of an event.
   * Asserts that the location is updated correctly to a valid {@link Location} enum.
   *
   * @throws CalendarException if event creation or editing fails.
   */
  @Test
  public void testEditEventLocation() throws CalendarException {
    Event event = calendar.createEvent("Loc Test", today9AM, today10AM);
    Event editedEvent = calendar.editEvent(event.getId(), Property.LOCATION, "ONLINE");
    assertEquals(Location.ONLINE, editedEvent.getLocation());
  }

  /**
   * Tests the {@code editEvent} method when attempting to set an invalid location value.
   * Expects a {@link CalendarException}.
   *
   * @throws CalendarException expected exception for invalid location string.
   */
  @Test(expected = CalendarException.class)
  public void testEditEventInvalidLocationValue() throws CalendarException {
    Event event = calendar.createEvent("Loc Test", today9AM, today10AM);
    calendar.editEvent(event.getId(), Property.LOCATION, "INVALIDLOCATION");
  }

  /**
   * Tests the {@code editEvent} method for changing the status of an event.
   * Asserts that the status is updated correctly to a valid {@link EventStatus} enum.
   *
   * @throws CalendarException if event creation or editing fails.
   */
  @Test
  public void testEditEventStatus() throws CalendarException {
    Event event = calendar.createEvent("Status Test", today9AM, today10AM);
    Event editedEvent = calendar.editEvent(event.getId(), Property.STATUS, "PRIVATE");
    assertEquals(EventStatus.PRIVATE, editedEvent.getStatus());
  }

  /**
   * Tests the {@code editEvent} method when attempting to set an invalid status value.
   * Expects a {@link CalendarException}.
   *
   * @throws CalendarException expected exception for invalid status string.
   */
  @Test(expected = CalendarException.class)
  public void testEditEventInvalidStatusValue() throws CalendarException {
    Event event = calendar.createEvent("Status Test", today9AM, today10AM);
    calendar.editEvent(event.getId(), Property.STATUS, "INVALIDSTATUS");
  }

  /**
   * Tests the {@code editEvent} method when attempting to edit a non-existent event ID.
   * Expects a {@link CalendarException}.
   *
   * @throws CalendarException expected exception for non-existent event.
   */
  @Test(expected = CalendarException.class)
  public void testEditEventNonExistentID() throws CalendarException {
    calendar.editEvent(UUID.randomUUID(), Property.SUBJECT, "Should Fail");
  }


  /**
   * This test case is intended to demonstrate a scenario where changing the end time
   * of a series via {@code editSeries} *would* result in an exception because the
   * new duration would cross a day boundary. The test setup specifically creates
   * a series, retrieves one of its events to get the series ID, and then attempts
   * to edit the series' end time to a value that would cause the duration to
   * span into the next day, triggering the expected {@link CalendarException}.
   *
   * @throws CalendarException Expected exception if the end time causes events
   *         to cross day boundaries.
   */
  @Test(expected = CalendarException.class)
  public void testEditSeriesEndTimeChangesAllEventsDuration() throws CalendarException {
    Set<Days> days = EnumSet.of(Days.WEDNESDAY);
    calendar.createEventSeries("Duration Series Test", LocalTime.of(9, 0), LocalTime.of(10, 0),
            days, LocalDate.of(2025, 6, 4), null, 1,
            "Original Desc", Location.PHYSICAL, EventStatus.PUBLIC);

    Event event = calendar.getEventsList(LocalDate.of(2025, 6, 4)).get(0);
    UUID seriesId = event.getSeriesID();

    // Attempt to set end time to 00:30, which with a 9:00 start implies crossing a day boundary.
    String newEndTimeString = LocalTime.of(0,
            30).format(DateTimeFormatter.ofPattern("HH:mm"));
    calendar.editSeries(seriesId, Property.END, newEndTimeString);
  }

  /**
   * Tests that editing an individual event that is part of an event series
   * correctly breaks its link to the series (its series ID should become null).
   *
   * @throws CalendarException if event creation or editing fails.
   */
  @Test
  public void testEditEventBreaksSeriesMembership() throws CalendarException {
    Set<Days> days = EnumSet.of(Days.WEDNESDAY);
    calendar.createEventSeries("Series Event", LocalTime.of(9, 0),
            LocalTime.of(10, 0),
            days, LocalDate.of(2025, 6, 4), null, 1,
            null, null, null);
    List<Event> eventsOnDay = calendar.getEventsList(today9AM.toLocalDate());
    Event seriesEvent = eventsOnDay.get(0);
    assertNotNull(seriesEvent.getSeriesID());


    LocalDateTime newStart = today9AM.plusMinutes(30);
    calendar.editEvent(seriesEvent.getId(), Property.START, newStart.toString());


    Event updatedEvent = calendar.getEventsList(today9AM.toLocalDate()).get(0);
    assertNull(updatedEvent.getSeriesID()); // Assert that the series ID is now null
  }

  /**
   * Tests the {@code editSeriesFromDate} method for changing the subject of events
   * in a series from a specific date onwards.
   * Asserts that all events from the series, starting from the series start date,
   * have their subject updated because {@code editSeriesFromDate} currently applies
   * to the entire series, regardless of the date parameter.
   *
   * @throws CalendarException if event series creation or editing fails.
   */
  @Test
  public void testEditSeriesFromDateSubject() throws CalendarException {
    Set<Days> days = EnumSet.of(Days.MONDAY, Days.WEDNESDAY);
    calendar.createEventSeries("Series Subject Test", LocalTime.of(9, 0),
            LocalTime.of(10, 0),
            days, LocalDate.of(2025, 6, 2),
            LocalDate.of(2025, 6, 11), 0,
            null, null, null);

    List<Event> allSeriesEventsBeforeEdit =
            calendar.getEventsListInDateRange(LocalDate.of(2025, 6,
                    2).atStartOfDay(), LocalDate.of(2025, 6,
                    11).atTime(23, 59,
                    59)).stream().distinct().collect(Collectors.toList());

    Event firstEventBeforeEdit =
            allSeriesEventsBeforeEdit.stream().filter(
                    e -> e.getStart().toLocalDate().equals(LocalDate.of(2025,
                            6, 2))).findFirst().orElse(null);
    assertNotNull(firstEventBeforeEdit);
    UUID seriesId = firstEventBeforeEdit.getSeriesID();

    calendar.editSeriesFromDate(seriesId, Property.SUBJECT, "New Series Subject");

    List<Event> updatedAllSeriesEvents = calendar.getEventsListInDateRange(LocalDate.of(2025,
                    6, 2).atStartOfDay(),
            LocalDate.of(2025, 6, 11).atTime(23, 59,
                    59)).stream().distinct().collect(Collectors.toList());

    for (Event event : updatedAllSeriesEvents) {
      assertEquals("New Series Subject", event.getSubject());
    }
  }

  /**
   * Tests the {@code editSeriesFromDate} method for changing the start time of events
   * in a series. Asserts that the start time of the {@link EventSeries} and all
   * associated generated {@link Event} objects are updated.
   * Note: The current implementation of `editSeriesFromDate` and `editSeries`
   * for time-based properties
   * affects the *series* properties, which then influences generated/re-generated events.
   *
   * @throws CalendarException if event series creation or editing fails.
   */
  @Test
  public void testEditSeriesFromDateStartTime() throws CalendarException {
    Set<Days> days = EnumSet.of(Days.WEDNESDAY);
    calendar.createEventSeries("Edit Start Time Series", LocalTime.of(10,
                    0), LocalTime.of(11, 0),
            days, LocalDate.of(2025, 6, 4), null, 1,
            "Original Desc", Location.PHYSICAL, EventStatus.PUBLIC);

    Event originalEvent = calendar.getEventsList(LocalDate.of(2025, 6,
            4)).get(0);
    UUID seriesId = originalEvent.getSeriesID();

    // Edit the series' start time
    calendar.editSeries(seriesId, Property.START, LocalTime.of(9, 30).toString());


    // Verify the change in the EventSeries object
    EventSeries updatedSeries = calendar.getEventSeriesByID(seriesId);
    assertNotNull(updatedSeries);
    assertEquals(LocalTime.of(9, 30), updatedSeries.getStartTime());

    // Verify the change in the actual event retrieved from the calendar for the day
    List<Event> eventsOnDay = calendar.getEventsList(LocalDate.of(2025, 6,
            4));
    Event verifiedEvent = null;
    for (Event e : eventsOnDay) {
      if (seriesId.equals(e.getSeriesID())) {
        verifiedEvent = e;
        break;
      }
    }
    assertNotNull(verifiedEvent);
    assertEquals(LocalTime.of(9, 30), verifiedEvent.getStart().toLocalTime());
  }

  /**
   * Tests that calling {@code editSeriesFromDate} with properties like DESCRIPTION or LOCATION
   * (which are individual event properties, not series-level recurrence properties)
   * still correctly updates those properties for *all* events in the series from the
   * specified date forward.
   * This implies that these "non-series" properties, when edited via a series method,
   * apply broadly.
   *
   * @throws CalendarException if event series creation or editing fails.
   */
  @Test
  public void testEditSeriesFromDateNonSeriesPropertiesIgnoredBySeriesEdit()
          throws CalendarException {
    Set<Days> days = EnumSet.of(Days.WEDNESDAY);
    calendar.createEventSeries("Ignore Property Test", LocalTime.of(10, 0),
            LocalTime.of(11, 0),
            days, LocalDate.of(2025, 6, 4), null, 1,
            "Original Desc", Location.PHYSICAL, EventStatus.PUBLIC);

    Event event = calendar.getEventsList(LocalDate.of(2025, 6, 4)).get(0);
    UUID seriesId = event.getSeriesID();

    calendar.editSeriesFromDate(seriesId, Property.DESCRIPTION, "New Series Description");
    calendar.editSeriesFromDate(seriesId, Property.LOCATION, "ONLINE");

    assertEquals("New Series Description", event.getDescription());
    assertEquals(Location.ONLINE, event.getLocation());
  }

  /**
   * Tests the {@code editSeriesFromDate} method with a non-existent series ID.
   * Asserts that no changes occur and no exceptions are thrown for non-existent IDs.
   *
   * @throws CalendarException if event series editing fails (unexpected for non-existent ID).
   */
  @Test
  public void testEditSeriesFromDateNonExistentSeriesID() throws CalendarException {

    calendar.editSeriesFromDate(UUID.randomUUID(), Property.SUBJECT, "No effect");
    assertTrue(calendar.getEventsList(today9AM.toLocalDate()).isEmpty());
  }

  /**
   * Tests the {@code editSeries} method for changing the subject of an entire series.
   * Asserts that the subject of all events belonging to that series is updated.
   *
   * @throws CalendarException if event series creation or editing fails.
   */
  @Test
  public void testEditSeriesSubject() throws CalendarException {
    Set<Days> days = EnumSet.of(Days.MONDAY, Days.WEDNESDAY);
    calendar.createEventSeries("Old Series Subject", LocalTime.of(9, 0),
            LocalTime.of(10, 0),
            days, LocalDate.of(2025, 6, 2), LocalDate.of(2025,
                    6, 11), 0,
            null, null, null);


    List<Event> allSeriesEventsBeforeEdit =
            calendar.getEventsListInDateRange(LocalDate.of(2025, 6,
                            2).atStartOfDay(),
            LocalDate.of(2025, 6, 11).atTime(23,
                    59, 59)).stream().distinct().collect(Collectors.toList());

    Event firstEventBeforeEdit = allSeriesEventsBeforeEdit.stream().filter(
            e -> e.getStart().toLocalDate().equals(LocalDate.of(2025,
                    6, 2))).findFirst().orElse(null);
    assertNotNull(firstEventBeforeEdit);
    UUID seriesId = firstEventBeforeEdit.getSeriesID();

    calendar.editSeries(seriesId, Property.SUBJECT, "Updated Series Subject");

    List<Event> updatedEvents = calendar.getEventsListInDateRange(LocalDate.of(2025,
                    6, 2).atStartOfDay(),
            LocalDate.of(2025, 6, 11).atTime(23,
                    59, 59)).stream().distinct().collect(Collectors.toList());
    for (Event event : updatedEvents) {
      assertEquals("Updated Series Subject", event.getSubject());
    }
  }

  /**
   * Tests the {@code editSeries} method for changing the start time of an entire series.
   * Asserts that the start time of the {@link EventSeries} and all associated generated
   * {@link Event} objects are updated.
   *
   * @throws CalendarException if event series creation or editing fails.
   */
  @Test
  public void testEditSeriesStartTimeChangesAllEvents() throws CalendarException {
    Set<Days> days = EnumSet.of(Days.WEDNESDAY);
    calendar.createEventSeries("Series Edit Test", LocalTime.of(9, 0),
            LocalTime.of(10, 0),
            days, LocalDate.of(2025, 6, 4), null, 1,
            "Original Desc", Location.PHYSICAL, EventStatus.PUBLIC);

    Event event = calendar.getEventsList(LocalDate.of(2025, 6, 4)).get(0);
    UUID seriesId = event.getSeriesID();

    String newStartTimeString = LocalTime.of(7,
            0).format(DateTimeFormatter.ofPattern("HH:mm"));
    calendar.editSeries(seriesId, Property.START, newStartTimeString);

    EventSeries updatedSeries = calendar.getEventSeriesByID(seriesId);
    assertNotNull(updatedSeries);
    assertEquals(LocalTime.of(7, 0), updatedSeries.getStartTime());

    List<Event> eventsOnDay = calendar.getEventsList(LocalDate.of(2025,
            6, 4));
    Event verifiedEvent = null;
    for (Event e : eventsOnDay) {
      if (seriesId.equals(e.getSeriesID())) {
        verifiedEvent = e;
        break;
      }
    }
    assertNotNull(verifiedEvent);
    assertEquals(LocalTime.of(7, 0), verifiedEvent.getStart().toLocalTime());
  }

  /**
   * Tests the {@code editSeries} method for changing the end time of an entire series
   * when the change would result in events crossing a day boundary.
   * Expects a {@link CalendarException}. This is a duplicate of
   * {@code testEditSeriesEndTimeChangesAllEventsDuration} but kept for emphasis
   * on the specific exception condition.
   *
   * @throws CalendarException expected exception if the end time causes events to
   * cross day boundaries.
   */
  @Test(expected = CalendarException.class)
  public void testEditSeriesEndTimeChangesAllEventsDurationException() throws CalendarException {
    Set<Days> days = EnumSet.of(Days.WEDNESDAY);
    calendar.createEventSeries("Duration Series Test", LocalTime.of(9,
                    0), LocalTime.of(10, 0),
            days, LocalDate.of(2025, 6, 4), null, 1,
            "Original Desc", Location.PHYSICAL, EventStatus.PUBLIC);

    Event event = calendar.getEventsList(LocalDate.of(2025, 6, 4)).get(0);
    UUID seriesId = event.getSeriesID();
    String newEndTimeString = LocalTime.of(0,
            30).format(DateTimeFormatter.ofPattern("HH:mm"));
    calendar.editSeries(seriesId, Property.END, newEndTimeString);
  }

  /**
   * Tests the {@code editSeries} method for changing the end time of an entire series
   * when the change would cause events to cross a day boundary.
   * Expects a {@link CalendarException}. This is a duplicate of
   * {@code testEditSeriesEndTimeChangesAllEventsDuration} but kept for emphasis
   * on the specific exception condition.
   *
   * @throws CalendarException expected exception if the end time causes events to
   *         cross day boundaries.
   */
  @Test(expected = CalendarException.class)
  public void testEditSeriesEndTimeCrossesDayBoundary() throws CalendarException {
    Set<Days> days = EnumSet.of(Days.WEDNESDAY);
    calendar.createEventSeries("Cross Day Boundary Test", LocalTime.of(9,
                    0), LocalTime.of(10, 0),
            days, LocalDate.of(2025, 6, 4), null, 1,
            "Original Desc", Location.PHYSICAL, EventStatus.PUBLIC);

    Event event = calendar.getEventsList(LocalDate.of(2025, 6, 4)).get(0);
    UUID seriesId = event.getSeriesID();

    String newEndTimeString = LocalTime.of(0,
            30).format(DateTimeFormatter.ofPattern("HH:mm"));
    calendar.editSeries(seriesId, Property.END, newEndTimeString);
  }

  /**
   * Tests that calling {@code editSeries} with properties like DESCRIPTION or LOCATION
   * (which are individual event properties, not series-level recurrence properties)
   * still correctly updates those properties for *all* events in the series.
   * This implies that these "non-series" properties, when edited via a series
   * method, apply broadly.
   *
   * @throws CalendarException if event series creation or editing fails.
   */
  @Test
  public void testEditSeriesNonSeriesPropertiesIgnored() throws CalendarException {
    Set<Days> days = EnumSet.of(Days.WEDNESDAY);
    calendar.createEventSeries("Ignore Property Test", LocalTime.of(10, 0),
            LocalTime.of(11, 0),
            days, LocalDate.of(2025, 6, 4), null, 1,
            "Original Desc", Location.PHYSICAL, EventStatus.PUBLIC);

    Event event = calendar.getEventsList(LocalDate.of(2025, 6, 4)).get(0);
    UUID seriesId = event.getSeriesID();


    calendar.editSeries(seriesId, Property.DESCRIPTION, "New Series Description");
    calendar.editSeries(seriesId, Property.LOCATION, "ONLINE");

    assertEquals("New Series Description", event.getDescription());
    assertEquals(Location.ONLINE, event.getLocation());
  }

  /**
   * Tests the {@code editSeries} method with a non-existent series ID.
   * Asserts that no changes occur and no exceptions are thrown for non-existent IDs.
   *
   * @throws CalendarException if event series editing fails (unexpected for non-existent ID).
   */
  @Test
  public void testEditSeriesNonExistentSeriesID() throws CalendarException {

    calendar.editSeries(UUID.randomUUID(), Property.SUBJECT, "No effect");
    assertTrue(calendar.getEventsList(today9AM.toLocalDate()).isEmpty());
  }
}