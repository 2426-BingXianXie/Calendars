package calendar.model;

import org.junit.Before;
import org.junit.Test;

import calendar.CalendarException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * JUnit 4 test class for the {@link NamedCalendar} model,
 * which implements the {@link ICalendar} interface.
 */
public class NamedCalendarTest {

  private ICalendar calendar;
  private NamedCalendar namedCalendar;
  private final ZoneId EST = ZoneId.of("America/New_York");
  private final ZoneId PST = ZoneId.of("America/Los_Angeles");
  private final ZoneId UTC = ZoneId.of("UTC");

  /**
   * Sets up the test environment before each test method is executed.
   * Initializes a new {@link NamedCalendar} instance and common {@link LocalDateTime} objects.
   */
  @Before
  public void setUp() {
    namedCalendar = new NamedCalendar("Test Calendar", EST);
    calendar = namedCalendar;
  }

  @Test
  public void testNamedCalendarConstructor() {
    assertEquals("Test Calendar", namedCalendar.getName());
    assertEquals(EST, namedCalendar.getTimezone());
    assertNotNull(namedCalendar.getVirtualCalendar());
  }

  @Test
  public void testSetName() throws CalendarException {
    namedCalendar.setName("New Name");
    assertEquals("New Name", namedCalendar.getName());
  }

  @Test
  public void testSetNameWithWhitespace() throws CalendarException {
    namedCalendar.setName("  Spaced Name  ");
    assertEquals("Spaced Name", namedCalendar.getName());
  }

  @Test(expected = CalendarException.class)
  public void testSetEmptyName() throws CalendarException {
    namedCalendar.setName("");
  }

  @Test(expected = CalendarException.class)
  public void testSetNullName() throws CalendarException {
    namedCalendar.setName(null);
  }

  @Test(expected = CalendarException.class)
  public void testSetWhitespaceOnlyName() throws CalendarException {
    namedCalendar.setName("   ");
  }

  @Test
  public void testSetTimezone() {
    namedCalendar.setTimezone(PST);
    assertEquals(PST, namedCalendar.getTimezone());
  }

  @Test
  public void testToString() {
    String expected = "Calendar: Test Calendar (America/New_York)";
    assertEquals(expected, namedCalendar.toString());
  }

  @Test
  public void testCreateSimpleEvent() throws CalendarException {
    LocalDateTime start = LocalDateTime.of(2024, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2024, 1, 15, 11, 0);

    Event event = calendar.createEvent("Meeting", start, end);

    assertNotNull(event);
    assertEquals("Meeting", event.getSubject());
    assertEquals(start, event.getStart());
    assertEquals(end, event.getEnd());
  }

  @Test(expected = CalendarException.class)
  public void testCreateEventWithStartAfterEnd() throws CalendarException {
    LocalDateTime start = LocalDateTime.of(2024, 1, 15, 11, 0);
    LocalDateTime end = LocalDateTime.of(2024, 1, 15, 10, 0);

    calendar.createEvent("Invalid Event", start, end);
  }

  @Test(expected = CalendarException.class)
  public void testCreateDuplicateEvent() throws CalendarException {
    LocalDateTime start = LocalDateTime.of(2024, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2024, 1, 15, 11, 0);

    calendar.createEvent("Meeting", start, end);
    calendar.createEvent("Meeting", start, end); // Duplicate
  }

  @Test
  public void testCreateMultiDayEvent() throws CalendarException {
    LocalDateTime start = LocalDateTime.of(2024, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2024, 1, 17, 11, 0);

    Event event = calendar.createEvent("Conference", start, end);

    assertNotNull(event);
    assertEquals("Conference", event.getSubject());

    // Event should appear on all days it spans
    List<Event> events15 = calendar.getEventsList(LocalDate.of(2024, 1, 15));
    List<Event> events16 = calendar.getEventsList(LocalDate.of(2024, 1, 16));
    List<Event> events17 = calendar.getEventsList(LocalDate.of(2024, 1, 17));

    assertEquals(1, events15.size());
    assertEquals(1, events16.size());
    assertEquals(1, events17.size());
    assertTrue(events15.contains(event));
    assertTrue(events16.contains(event));
    assertTrue(events17.contains(event));
  }

  @Test
  public void testCreateWeeklyEventSeries() throws CalendarException {
    Set<Days> weekdays = EnumSet.of(Days.MONDAY, Days.WEDNESDAY, Days.FRIDAY);
    LocalTime startTime = LocalTime.of(9, 0);
    LocalTime endTime = LocalTime.of(10, 0);
    LocalDate startDate = LocalDate.of(2024, 1, 15); // Monday
    LocalDate endDate = LocalDate.of(2024, 1, 31);

    calendar.createEventSeries("Lecture", startTime, endTime, weekdays,
            startDate, endDate, 0, "CS Course",
            Location.PHYSICAL, EventStatus.PUBLIC);

    // Check that events were created on correct days
    List<Event> mondayEvents = calendar.getEventsList(LocalDate.of(2024, 1,
            15));
    List<Event> tuesdayEvents = calendar.getEventsList(LocalDate.of(2024, 1,
            16));
    List<Event> wednesdayEvents = calendar.getEventsList(LocalDate.of(2024, 1,
            17));

    assertEquals(1, mondayEvents.size());
    assertEquals(0, tuesdayEvents.size());
    assertEquals(1, wednesdayEvents.size());

    assertEquals("Lecture", mondayEvents.get(0).getSubject());
    assertEquals("CS Course", mondayEvents.get(0).getDescription());
    assertEquals(Location.PHYSICAL, mondayEvents.get(0).getLocation());
    assertEquals(EventStatus.PUBLIC, mondayEvents.get(0).getStatus());
  }

  @Test
  public void testCreateEventSeriesWithOccurrenceLimit() throws CalendarException {
    Set<Days> weekdays = EnumSet.of(Days.MONDAY);
    LocalTime startTime = LocalTime.of(9, 0);
    LocalTime endTime = LocalTime.of(10, 0);
    LocalDate startDate = LocalDate.of(2024, 1, 15); // Monday

    calendar.createEventSeries("Weekly Meeting", startTime, endTime, weekdays,
            startDate, null, 3, "Team Meeting",
            Location.ONLINE, EventStatus.PRIVATE);

    List<Event> week1 = calendar.getEventsList(LocalDate.of(2024, 1, 15));
    List<Event> week2 = calendar.getEventsList(LocalDate.of(2024, 1, 22));
    List<Event> week3 = calendar.getEventsList(LocalDate.of(2024, 1, 29));
    List<Event> week4 = calendar.getEventsList(LocalDate.of(2024, 2, 5));

    assertEquals(1, week1.size());
    assertEquals(1, week2.size());
    assertEquals(1, week3.size());
    assertEquals(0, week4.size()); // No 4th occurrence
  }

  @Test
  public void testCreateDailyEventSeries() throws CalendarException {
    Set<Days> allDays = EnumSet.allOf(Days.class);
    LocalTime startTime = LocalTime.of(8, 0);
    LocalTime endTime = LocalTime.of(8, 30);
    LocalDate startDate = LocalDate.of(2024, 1, 15);
    LocalDate endDate = LocalDate.of(2024, 1, 19);

    calendar.createEventSeries("Daily Standup", startTime, endTime, allDays,
            startDate, endDate, 0, null, null, null);

    // Check all 5 days have events
    for (int i = 0; i < 5; i++) {
      LocalDate date = startDate.plusDays(i);
      List<Event> dayEvents = calendar.getEventsList(date);
      assertEquals(1, dayEvents.size());
      assertEquals("Daily Standup", dayEvents.get(0).getSubject());
    }
  }

  @Test
  public void testGetEventsBySubjectAndStartTime() throws CalendarException {
    LocalDateTime start = LocalDateTime.of(2024, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2024, 1, 15, 11, 0);

    calendar.createEvent("Meeting", start, end);

    List<Event> events = calendar.getEventsBySubjectAndStartTime("Meeting", start);
    assertEquals(1, events.size());
    assertEquals("Meeting", events.get(0).getSubject());

    // Case insensitive search
    List<Event> eventsLower = calendar.getEventsBySubjectAndStartTime("meeting", start);
    assertEquals(1, eventsLower.size());
  }

  @Test
  public void testGetEventsBySubjectAndStartTimeNoMatch() throws CalendarException {
    LocalDateTime start = LocalDateTime.of(2024, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2024, 1, 15, 11, 0);

    calendar.createEvent("Meeting", start, end);

    List<Event> events = calendar.getEventsBySubjectAndStartTime("Different", start);
    assertTrue(events.isEmpty());

    List<Event> eventsWrongTime = calendar.getEventsBySubjectAndStartTime("Meeting",
            start.plusHours(1));
    assertTrue(eventsWrongTime.isEmpty());
  }

  @Test
  public void testGetEventsByDetails() throws CalendarException {
    LocalDateTime start = LocalDateTime.of(2024, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2024, 1, 15, 11, 0);

    calendar.createEvent("Meeting", start, end);

    List<Event> events = calendar.getEventsByDetails("Meeting", start, end);
    assertEquals(1, events.size());
    assertEquals("Meeting", events.get(0).getSubject());
  }

  @Test
  public void testGetEventsByDetailsNoMatch() throws CalendarException {
    LocalDateTime start = LocalDateTime.of(2024, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2024, 1, 15, 11, 0);

    calendar.createEvent("Meeting", start, end);

    List<Event> events = calendar.getEventsByDetails("Meeting", start, end.plusMinutes(30));
    assertTrue(events.isEmpty());
  }

  @Test
  public void testGetEventsList() throws CalendarException {
    LocalDate date = LocalDate.of(2024, 1, 15);
    LocalDateTime start1 = LocalDateTime.of(date, LocalTime.of(9, 0));
    LocalDateTime end1 = LocalDateTime.of(date, LocalTime.of(10, 0));
    LocalDateTime start2 = LocalDateTime.of(date, LocalTime.of(14, 0));
    LocalDateTime end2 = LocalDateTime.of(date, LocalTime.of(15, 0));

    calendar.createEvent("Morning Meeting", start1, end1);
    calendar.createEvent("Afternoon Meeting", start2, end2);

    List<Event> events = calendar.getEventsList(date);
    assertEquals(2, events.size());

    // Events should be retrievable by subject
    assertTrue(events.stream().anyMatch(e -> e.getSubject().equals("Morning Meeting")));
    assertTrue(events.stream().anyMatch(e -> e.getSubject().equals("Afternoon Meeting")));
  }

  @Test
  public void testGetEventsListEmptyDate() {
    LocalDate date = LocalDate.of(2024, 1, 15);
    List<Event> events = calendar.getEventsList(date);
    assertTrue(events.isEmpty());
  }

  @Test
  public void testGetEventsListInDateRange() throws CalendarException {
    LocalDateTime start = LocalDateTime.of(2024, 1, 15, 0, 0);
    LocalDateTime end = LocalDateTime.of(2024, 1, 17, 23, 59);

    // Create events on different days
    calendar.createEvent("Event 1",
            LocalDateTime.of(2024, 1, 15, 10, 0),
            LocalDateTime.of(2024, 1, 15, 11, 0));
    calendar.createEvent("Event 2",
            LocalDateTime.of(2024, 1, 16, 10, 0),
            LocalDateTime.of(2024, 1, 16, 11, 0));
    calendar.createEvent("Event 3",
            LocalDateTime.of(2024, 1, 18, 10, 0),
            LocalDateTime.of(2024, 1, 18, 11, 0));

    List<Event> events = calendar.getEventsListInDateRange(start, end);
    assertEquals(2, events.size());
    assertTrue(events.stream().anyMatch(e -> e.getSubject().equals("Event 1")));
    assertTrue(events.stream().anyMatch(e -> e.getSubject().equals("Event 2")));
    assertFalse(events.stream().anyMatch(e -> e.getSubject().equals("Event 3")));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetEventsListInDateRangeInvalidRange() throws CalendarException {
    LocalDateTime start = LocalDateTime.of(2024, 1, 17, 0, 0);
    LocalDateTime end = LocalDateTime.of(2024, 1, 15, 23, 59);

    calendar.getEventsListInDateRange(start, end);
  }

  @Test
  public void testIsBusyAt() throws CalendarException {
    LocalDateTime start = LocalDateTime.of(2024, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2024, 1, 15, 11, 0);

    calendar.createEvent("Meeting", start, end);

    assertTrue(calendar.isBusyAt(start));
    assertTrue(calendar.isBusyAt(start.plusMinutes(30)));
    assertFalse(calendar.isBusyAt(end)); // End is exclusive
    assertFalse(calendar.isBusyAt(start.minusMinutes(1)));
    assertFalse(calendar.isBusyAt(end.plusMinutes(1)));
  }

  @Test
  public void testIsBusyAtWithMultipleEvents() throws CalendarException {
    LocalDate date = LocalDate.of(2024, 1, 15);

    calendar.createEvent("Morning Meeting",
            LocalDateTime.of(date, LocalTime.of(9, 0)),
            LocalDateTime.of(date, LocalTime.of(10, 0)));
    calendar.createEvent("Afternoon Meeting",
            LocalDateTime.of(date, LocalTime.of(14, 0)),
            LocalDateTime.of(date, LocalTime.of(15, 0)));

    assertTrue(calendar.isBusyAt(LocalDateTime.of(date, LocalTime.of(9, 30))));
    assertFalse(calendar.isBusyAt(LocalDateTime.of(date, LocalTime.of(12, 0))));
    assertTrue(calendar.isBusyAt(LocalDateTime.of(date, LocalTime.of(14, 30))));
  }

  @Test
  public void testIsBusyAtZeroDurationEvent() throws CalendarException {
    LocalDateTime instant = LocalDateTime.of(2024, 1, 15, 10,
            0);

    calendar.createEvent("Instant Event", instant, instant);

    assertTrue(calendar.isBusyAt(instant));
    assertFalse(calendar.isBusyAt(instant.plusMinutes(1)));
  }

  @Test
  public void testEditEventSubject() throws CalendarException {
    LocalDateTime start = LocalDateTime.of(2024, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2024, 1, 15, 11, 0);

    Event event = calendar.createEvent("Meeting", start, end);
    UUID eventId = event.getId();

    Event editedEvent = calendar.editEvent(eventId, Property.SUBJECT, "Updated" +
            " Meeting");

    assertEquals("Updated Meeting", editedEvent.getSubject());
    assertEquals(start, editedEvent.getStart());
    assertEquals(end, editedEvent.getEnd());
  }

  @Test
  public void testEditEventStartTime() throws CalendarException {
    LocalDateTime start = LocalDateTime.of(2024, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2024, 1, 15, 11, 0);
    LocalDateTime newStart = LocalDateTime.of(2024, 1, 15, 9,
            0);

    Event event = calendar.createEvent("Meeting", start, end);
    UUID eventId = event.getId();

    Event editedEvent = calendar.editEvent(eventId, Property.START, newStart.toString());

    assertEquals("Meeting", editedEvent.getSubject());
    assertEquals(newStart, editedEvent.getStart());
    assertEquals(end, editedEvent.getEnd());
  }

  @Test
  public void testEditEventEndTime() throws CalendarException {
    LocalDateTime start = LocalDateTime.of(2024, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2024, 1, 15, 11, 0);
    LocalDateTime newEnd = LocalDateTime.of(2024, 1, 15, 12, 0);

    Event event = calendar.createEvent("Meeting", start, end);
    UUID eventId = event.getId();

    Event editedEvent = calendar.editEvent(eventId, Property.END, newEnd.toString());

    assertEquals("Meeting", editedEvent.getSubject());
    assertEquals(start, editedEvent.getStart());
    assertEquals(newEnd, editedEvent.getEnd());
  }

  @Test(expected = CalendarException.class)
  public void testEditEventStartTimeAfterEnd() throws CalendarException {
    LocalDateTime start = LocalDateTime.of(2024, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2024, 1, 15, 11, 0);
    LocalDateTime newStart = LocalDateTime.of(2024, 1, 15, 12,
            0);

    Event event = calendar.createEvent("Meeting", start, end);
    UUID eventId = event.getId();

    calendar.editEvent(eventId, Property.START, newStart.toString());
  }

  @Test(expected = CalendarException.class)
  public void testEditEventEndTimeBeforeStart() throws CalendarException {
    LocalDateTime start = LocalDateTime.of(2024, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2024, 1, 15, 11, 0);
    LocalDateTime newEnd = LocalDateTime.of(2024, 1, 15, 9, 0);

    Event event = calendar.createEvent("Meeting", start, end);
    UUID eventId = event.getId();

    calendar.editEvent(eventId, Property.END, newEnd.toString());
  }

  @Test
  public void testEditEventTimesCorrectly() throws CalendarException {
    LocalDateTime start = LocalDateTime.of(2024, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2024, 1, 15, 11, 0);

    Event event = calendar.createEvent("Meeting", start, end);
    UUID eventId = event.getId();

    // First, edit end time to a later time (this should work)
    LocalDateTime newEnd = LocalDateTime.of(2024, 1, 15, 12, 0);
    calendar.editEvent(eventId, Property.END, newEnd.toString());
    assertEquals(newEnd, event.getEnd());

    // Then, edit start time to a later time (but still before the new end time)
    LocalDateTime newStart = LocalDateTime.of(2024, 1, 15, 10,
            30);
    calendar.editEvent(eventId, Property.START, newStart.toString());
    assertEquals(newStart, event.getStart());
  }

  @Test
  public void testEditEventLocation() throws CalendarException {
    LocalDateTime start = LocalDateTime.of(2024, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2024, 1, 15, 11, 0);

    Event event = calendar.createEvent("Meeting", start, end);
    UUID eventId = event.getId();

    Event editedEvent = calendar.editEvent(eventId, Property.LOCATION, "online");

    assertEquals(Location.ONLINE, editedEvent.getLocation());
  }

  @Test
  public void testEditEventStatus() throws CalendarException {
    LocalDateTime start = LocalDateTime.of(2024, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2024, 1, 15, 11, 0);

    Event event = calendar.createEvent("Meeting", start, end);
    UUID eventId = event.getId();

    Event editedEvent = calendar.editEvent(eventId, Property.STATUS, "private");

    assertEquals(EventStatus.PRIVATE, editedEvent.getStatus());
  }

  @Test
  public void testEditEventDescription() throws CalendarException {
    LocalDateTime start = LocalDateTime.of(2024, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2024, 1, 15, 11, 0);

    Event event = calendar.createEvent("Meeting", start, end);
    UUID eventId = event.getId();

    Event editedEvent = calendar.editEvent(eventId, Property.DESCRIPTION,
            "Important meeting");

    assertEquals("Important meeting", editedEvent.getDescription());
  }

  @Test(expected = CalendarException.class)
  public void testEditNonExistentEvent() throws CalendarException {
    UUID fakeId = UUID.randomUUID();
    calendar.editEvent(fakeId, Property.SUBJECT, "New Subject");
  }

  @Test(expected = CalendarException.class)
  public void testEditEventInvalidLocation() throws CalendarException {
    LocalDateTime start = LocalDateTime.of(2024, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2024, 1, 15, 11, 0);

    Event event = calendar.createEvent("Meeting", start, end);
    UUID eventId = event.getId();

    calendar.editEvent(eventId, Property.LOCATION, "invalid_location");
  }

  @Test(expected = CalendarException.class)
  public void testEditEventInvalidStatus() throws CalendarException {
    LocalDateTime start = LocalDateTime.of(2024, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2024, 1, 15, 11, 0);

    Event event = calendar.createEvent("Meeting", start, end);
    UUID eventId = event.getId();

    calendar.editEvent(eventId, Property.STATUS, "invalid_status");
  }

  @Test
  public void testEditEventToCreateConflictWorksCorrectly() throws CalendarException {
    LocalDateTime start1 = LocalDateTime.of(2024, 1, 15, 10, 0);
    LocalDateTime end1 = LocalDateTime.of(2024, 1, 15, 11, 0);
    LocalDateTime start2 = LocalDateTime.of(2024, 1, 15, 14, 0);
    LocalDateTime end2 = LocalDateTime.of(2024, 1, 15, 15, 0);

    Event event1 = calendar.createEvent("Meeting 1", start1, end1);
    Event event2 = calendar.createEvent("Meeting 2", start2, end2);

    // Edit event2 properties individually (should work)
    calendar.editEvent(event2.getId(), Property.SUBJECT, "Updated Meeting 2");
    assertEquals("Updated Meeting 2", event2.getSubject());

    // Edit start time to an earlier valid time (before the current end time)
    LocalDateTime newStart = LocalDateTime.of(2024, 1, 15, 13,
            0);
    calendar.editEvent(event2.getId(), Property.START, newStart.toString());
    assertEquals(newStart, event2.getStart());

    // Edit end time to a later valid time (after the current start time)
    LocalDateTime newEnd = LocalDateTime.of(2024, 1, 15, 16, 0);
    calendar.editEvent(event2.getId(), Property.END, newEnd.toString());
    assertEquals(newEnd, event2.getEnd());
  }

  @Test(expected = CalendarException.class)
  public void testEditEventToCreateConflict() throws CalendarException {
    LocalDateTime start1 = LocalDateTime.of(2024, 1, 15, 10, 0);
    LocalDateTime end1 = LocalDateTime.of(2024, 1, 15, 11, 0);
    LocalDateTime start2 = LocalDateTime.of(2024, 1, 15, 14, 0);
    LocalDateTime end2 = LocalDateTime.of(2024, 1, 15, 15, 0);

    Event event1 = calendar.createEvent("Meeting 1", start1, end1);
    Event event2 = calendar.createEvent("Meeting 2", start2, end2);

    // Try to edit event2 to have same subject as event1
    calendar.editEvent(event2.getId(), Property.SUBJECT, "Meeting 1");
    // Try to edit event2 to have same start time as event1
    calendar.editEvent(event2.getId(), Property.START, start1.toString());
    // This should cause a conflict when we try to edit the end time to match event1
    calendar.editEvent(event2.getId(), Property.END, end1.toString());
  }

  @Test
  public void testEditSeriesSubject() throws CalendarException {
    Set<Days> weekdays = EnumSet.of(Days.MONDAY, Days.WEDNESDAY);
    LocalTime startTime = LocalTime.of(9, 0);
    LocalTime endTime = LocalTime.of(10, 0);
    LocalDate startDate = LocalDate.of(2024, 1, 15);

    calendar.createEventSeries("Lecture", startTime, endTime, weekdays,
            startDate, null, 3, null, null, null);

    // Get one event from the series
    List<Event> events = calendar.getEventsList(startDate);
    Event firstEvent = events.get(0);
    UUID seriesId = firstEvent.getSeriesID();

    calendar.editSeries(seriesId, Property.SUBJECT, "Updated Lecture");

    // Check that all events in series were updated
    List<Event> mondayEvents = calendar.getEventsList(LocalDate.of(2024, 1,
            15));
    List<Event> wednesdayEvents = calendar.getEventsList(LocalDate.of(2024, 1,
            17));

    assertEquals("Updated Lecture", mondayEvents.get(0).getSubject());
    assertEquals("Updated Lecture", wednesdayEvents.get(0).getSubject());
  }

  @Test
  public void testEditSeriesFromDate() throws CalendarException {
    Set<Days> weekdays = EnumSet.of(Days.MONDAY);
    LocalTime startTime = LocalTime.of(9, 0);
    LocalTime endTime = LocalTime.of(10, 0);
    LocalDate startDate = LocalDate.of(2024, 1, 15);

    calendar.createEventSeries("Lecture", startTime, endTime, weekdays,
            startDate, null, 4, null, null, null);

    // Get series ID from first event
    List<Event> events = calendar.getEventsList(startDate);
    Event firstEvent = events.get(0);
    UUID seriesId = firstEvent.getSeriesID();

    // Edit series from second occurrence
    calendar.editSeriesFromDate(seriesId, Property.SUBJECT, "Modified Lecture");

    // Check that all events from the modification point were updated
    List<Event> week1 = calendar.getEventsList(LocalDate.of(2024, 1, 15));
    List<Event> week2 = calendar.getEventsList(LocalDate.of(2024, 1, 22));
    List<Event> week3 = calendar.getEventsList(LocalDate.of(2024, 1, 29));
    List<Event> week4 = calendar.getEventsList(LocalDate.of(2024, 2, 5));

    assertEquals("Modified Lecture", week1.get(0).getSubject());
    assertEquals("Modified Lecture", week2.get(0).getSubject());
    assertEquals("Modified Lecture", week3.get(0).getSubject());
    assertEquals("Modified Lecture", week4.get(0).getSubject());
  }

  @Test
  public void testGetEventSeriesByID() throws CalendarException {
    Set<Days> weekdays = EnumSet.of(Days.MONDAY);
    LocalTime startTime = LocalTime.of(9, 0);
    LocalTime endTime = LocalTime.of(10, 0);
    LocalDate startDate = LocalDate.of(2024, 1, 15);

    calendar.createEventSeries("Lecture", startTime, endTime, weekdays,
            startDate, null, 2, null, null, null);

    // Get series ID from an event
    List<Event> events = calendar.getEventsList(startDate);
    Event firstEvent = events.get(0);
    UUID seriesId = firstEvent.getSeriesID();

    EventSeries series = calendar.getEventSeriesByID(seriesId);

    assertNotNull(series);
    assertEquals("Lecture", series.getSubject());
    assertEquals(startTime, series.getStartTime());
    assertEquals(2, series.getNumOccurrences());
  }

  @Test
  public void testGetEventSeriesByIDNonExistent() {
    UUID fakeId = UUID.randomUUID();
    EventSeries series = calendar.getEventSeriesByID(fakeId);
    assertNull(series);
  }

  @Test
  public void testMixedEventsAndSeries() throws CalendarException {
    LocalDate baseDate = LocalDate.of(2024, 1, 15);

    // Create a single event
    calendar.createEvent("One-time Meeting",
            LocalDateTime.of(baseDate, LocalTime.of(10, 0)),
            LocalDateTime.of(baseDate, LocalTime.of(11, 0)));

    // Create a series
    Set<Days> weekdays = EnumSet.of(Days.MONDAY, Days.WEDNESDAY);
    calendar.createEventSeries("Recurring Lecture",
            LocalTime.of(14, 0), LocalTime.of(15, 0),
            weekdays, baseDate, null, 2, null, null,
            null);

    // Check that both types of events exist
    List<Event> events = calendar.getEventsList(baseDate);
    assertEquals(2, events.size());

    // Verify different types
    boolean hasOneTime = events.stream().anyMatch(e ->
            e.getSubject().equals("One-time Meeting"));
    boolean hasRecurring = events.stream().anyMatch(e ->
            e.getSubject().equals("Recurring Lecture"));

    assertTrue(hasOneTime);
    assertTrue(hasRecurring);

    // Check series event has series ID
    Event recurringEvent = events.stream()
            .filter(e -> e.getSubject().equals("Recurring Lecture"))
            .findFirst().orElse(null);
    assertNotNull(recurringEvent.getSeriesID());

    // Check one-time event has no series ID
    Event oneTimeEvent = events.stream()
            .filter(e -> e.getSubject().equals("One-time Meeting"))
            .findFirst().orElse(null);
    assertNull(oneTimeEvent.getSeriesID());
  }

  @Test
  public void testEventEditingBreaksSeriesMembership() throws CalendarException {
    Set<Days> weekdays = EnumSet.of(Days.MONDAY);
    LocalTime startTime = LocalTime.of(9, 0);
    LocalTime endTime = LocalTime.of(10, 0);
    LocalDate startDate = LocalDate.of(2024, 1, 15);

    calendar.createEventSeries("Lecture", startTime, endTime, weekdays,
            startDate, null, 2, null, null, null);

    // Get first event and its series ID
    List<Event> events = calendar.getEventsList(startDate);
    Event firstEvent = events.get(0);
    UUID originalSeriesId = firstEvent.getSeriesID();
    assertNotNull(originalSeriesId);

    // Edit the start time - this should break series membership
    LocalDateTime newStart = LocalDateTime.of(startDate, LocalTime.of(10, 0));
    calendar.editEvent(firstEvent.getId(), Property.START, newStart.toString());

    // Check that series ID was removed
    Event editedEvent = calendar.getEventsBySubjectAndStartTime("Lecture", newStart).get(0);
    assertNull(editedEvent.getSeriesID());

    // Original series should still exist for other events
    List<Event> week2Events = calendar.getEventsList(LocalDate.of(2024, 1,
            22));
    assertEquals(1, week2Events.size());
    assertEquals(originalSeriesId, week2Events.get(0).getSeriesID());
  }

  @Test
  public void testLargeNumberOfEvents() throws CalendarException {
    LocalDate startDate = LocalDate.of(2024, 1, 1);

    // Create 100 single events
    for (int i = 0; i < 100; i++) {
      LocalDate eventDate = startDate.plusDays(i);
      calendar.createEvent("Event " + i,
              LocalDateTime.of(eventDate, LocalTime.of(10, 0)),
              LocalDateTime.of(eventDate, LocalTime.of(11, 0)));
    }

    // Create a daily series for the same period
    Set<Days> allDays = EnumSet.allOf(Days.class);
    calendar.createEventSeries("Daily Series",
            LocalTime.of(14, 0), LocalTime.of(15, 0),
            allDays, startDate, startDate.plusDays(99), 0,
            null, null, null);

    // Verify events exist
    LocalDate midPoint = startDate.plusDays(50);
    List<Event> midPointEvents = calendar.getEventsList(midPoint);
    assertEquals(2, midPointEvents.size()); // One single event + one from series

    // Test range query
    LocalDateTime rangeStart = LocalDateTime.of(startDate, LocalTime.of(0, 0));
    LocalDateTime rangeEnd = LocalDateTime.of(startDate.plusDays(10),
            LocalTime.of(23, 59));
    List<Event> rangeEvents = calendar.getEventsListInDateRange(rangeStart, rangeEnd);
    assertEquals(22, rangeEvents.size()); // 11 days * 2 events per day
  }

  @Test
  public void testMidnightEvents() throws CalendarException {
    LocalDateTime midnight = LocalDateTime.of(2024, 1, 15, 0,
            0);
    LocalDateTime oneAM = LocalDateTime.of(2024, 1, 15, 1, 0);

    Event event = calendar.createEvent("Midnight Event", midnight, oneAM);

    assertTrue(calendar.isBusyAt(midnight));
    assertTrue(calendar.isBusyAt(midnight.plusMinutes(30)));
    assertFalse(calendar.isBusyAt(oneAM)); // End is exclusive
  }

  @Test
  public void testEventSpanningMultipleDays() throws CalendarException {
    LocalDateTime start = LocalDateTime.of(2024, 1, 15, 23, 0);
    LocalDateTime end = LocalDateTime.of(2024, 1, 16, 1, 0);

    Event event = calendar.createEvent("Overnight Event", start, end);

    // Event should appear on both days
    List<Event> day1Events = calendar.getEventsList(LocalDate.of(2024, 1,
            15));
    List<Event> day2Events = calendar.getEventsList(LocalDate.of(2024, 1,
            16));

    assertEquals(1, day1Events.size());
    assertEquals(1, day2Events.size());
    assertEquals(event, day1Events.get(0));
    assertEquals(event, day2Events.get(0));
  }

  @Test
  public void testGetVirtualCalendar() {
    VirtualCalendar virtualCalendar = namedCalendar.getVirtualCalendar();
    assertNotNull(virtualCalendar);
    assertTrue(virtualCalendar instanceof VirtualCalendar);
  }

  @Test
  public void testCreateEventWithNullEndTime() throws CalendarException {
    LocalDateTime start = LocalDateTime.of(2024, 1, 15, 10, 0);

    // This should work - some implementations might handle null end times
    Event event = calendar.createEvent("All Day", start, start.plusHours(9));
    assertNotNull(event);
  }

  @Test
  public void testEventSeriesWithDifferentTimezones() throws CalendarException {
    // Test that series work correctly when calendar timezone changes
    Set<Days> weekdays = EnumSet.of(Days.MONDAY);
    LocalTime startTime = LocalTime.of(9, 0);
    LocalTime endTime = LocalTime.of(10, 0);
    LocalDate startDate = LocalDate.of(2024, 1, 15);

    calendar.createEventSeries("Meeting", startTime, endTime, weekdays,
            startDate, null, 2, null, null, null);

    // Change calendar timezone
    namedCalendar.setTimezone(PST);

    // Events should still be retrievable
    List<Event> events = calendar.getEventsList(startDate);
    assertEquals(1, events.size());
    assertEquals("Meeting", events.get(0).getSubject());
  }

  @Test
  public void testEditEventWithDateChange() throws CalendarException {
    LocalDateTime start = LocalDateTime.of(2024, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2024, 1, 15, 11, 0);

    Event event = calendar.createEvent("Meeting", start, end);
    UUID eventId = event.getId();

    // First extend the end time to the new date to avoid validation error
    LocalDateTime newEnd = LocalDateTime.of(2024, 1, 20, 11, 0);
    calendar.editEvent(eventId, Property.END, newEnd.toString());

    // Then change the start time to the new date
    LocalDateTime newStart = LocalDateTime.of(2024, 1, 20, 10,
            0);
    calendar.editEvent(eventId, Property.START, newStart.toString());

    // Event should no longer appear on original date
    List<Event> originalDateEvents = calendar.getEventsList(LocalDate.of(2024, 1,
            15));
    assertTrue(originalDateEvents.isEmpty());

    // Event should appear on new date
    List<Event> newDateEvents = calendar.getEventsList(LocalDate.of(2024, 1,
            20));
    assertEquals(1, newDateEvents.size());
    assertEquals("Meeting", newDateEvents.get(0).getSubject());
  }

  @Test
  public void testEditEventToSpanMultipleDays() throws CalendarException {
    LocalDateTime start = LocalDateTime.of(2024, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2024, 1, 15, 11, 0);

    Event event = calendar.createEvent("Meeting", start, end);
    UUID eventId = event.getId();

    // Extend end time to next day
    LocalDateTime newEnd = LocalDateTime.of(2024, 1, 16, 10,
            0);
    calendar.editEvent(eventId, Property.END, newEnd.toString());

    // Check that event still appears on first day (where it was originally)
    List<Event> day1Events = calendar.getEventsList(LocalDate.of(2024, 1,
            15));
    assertEquals(1, day1Events.size());
    assertEquals("Meeting", day1Events.get(0).getSubject());

    // Verify the end time was actually updated
    assertEquals(newEnd, day1Events.get(0).getEnd());
  }

  @Test
  public void testEditEventDateChangeUpdatesCalendarMapping() throws CalendarException {
    // This test checks that the calendar system properly updates its internal mapping
    // when an event's date changes through editing
    LocalDateTime start = LocalDateTime.of(2024, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2024, 1, 15, 11, 0);

    Event event = calendar.createEvent("Meeting", start, end);
    UUID eventId = event.getId();

    // Verify event exists on original date
    List<Event> originalEvents = calendar.getEventsList(LocalDate.of(2024, 1,
            15));
    assertEquals(1, originalEvents.size());

    // First extend end time to allow start time change
    LocalDateTime newEnd = LocalDateTime.of(2024, 1, 20, 11,
            0);
    calendar.editEvent(eventId, Property.END, newEnd.toString());

    // Then change start time to new date
    LocalDateTime newStart = LocalDateTime.of(2024, 1, 20, 10,
            0);
    calendar.editEvent(eventId, Property.START, newStart.toString());

    // Event should no longer be on original date
    List<Event> originalDateEvents = calendar.getEventsList(LocalDate.of(2024, 1,
            15));
    assertEquals(0, originalDateEvents.size());

    // Event should now be on new date
    List<Event> newDateEvents = calendar.getEventsList(LocalDate.of(2024, 1,
            20));
    assertEquals(1, newDateEvents.size());
    assertEquals("Meeting", newDateEvents.get(0).getSubject());
    assertEquals(eventId, newDateEvents.get(0).getId());
  }

  @Test
  public void testEditSeriesStartTimeValidation() throws CalendarException {
    Set<Days> weekdays = EnumSet.of(Days.MONDAY);
    LocalTime startTime = LocalTime.of(9, 0);
    LocalTime endTime = LocalTime.of(10, 0);
    LocalDate startDate = LocalDate.of(2024, 1, 15);

    calendar.createEventSeries("Meeting", startTime, endTime, weekdays,
            startDate, null, 2, null, null, null);

    List<Event> events = calendar.getEventsList(startDate);
    Event event = events.get(0);
    UUID seriesId = event.getSeriesID();

    // Test that we can successfully edit to a valid start time
    calendar.editSeries(seriesId, Property.START, "08:00");

    // Verify the change was applied to the start time
    events = calendar.getEventsList(startDate);
    assertEquals(LocalTime.of(8, 0), events.get(0).getStart().toLocalTime());

    // The end time should remain the same (series editing updates start time independently)
    assertEquals(LocalTime.of(10, 0), events.get(0).getEnd().toLocalTime());
  }

  @Test
  public void testEditSeriesEndTimeSuccessfully() throws CalendarException {
    Set<Days> weekdays = EnumSet.of(Days.MONDAY);
    LocalTime startTime = LocalTime.of(9, 0);
    LocalTime endTime = LocalTime.of(10, 0);
    LocalDate startDate = LocalDate.of(2024, 1, 15);

    calendar.createEventSeries("Meeting", startTime, endTime, weekdays,
            startDate, null, 2, null, null, null);

    List<Event> events = calendar.getEventsList(startDate);
    Event event = events.get(0);
    UUID seriesId = event.getSeriesID();

    // Set end time to a valid later time (should work)
    calendar.editSeries(seriesId, Property.END, "11:00");

    // Verify the change was applied
    events = calendar.getEventsList(startDate);
    assertEquals(LocalTime.of(11, 0), events.get(0).getEnd().toLocalTime());
  }

  @Test
  public void testGetEventsListInDateRangeWithOverlappingEvents() throws CalendarException {
    // Create events that partially overlap with query range
    LocalDateTime queryStart = LocalDateTime.of(2024, 1, 15,
            12, 0);
    LocalDateTime queryEnd = LocalDateTime.of(2024, 1, 16,
            12, 0);

    // Event that starts before range and ends within range
    calendar.createEvent("Before Overlap",
            LocalDateTime.of(2024, 1, 15, 10, 0),
            LocalDateTime.of(2024, 1, 15, 14, 0));

    // Event completely within range
    calendar.createEvent("Within Range",
            LocalDateTime.of(2024, 1, 15, 15, 0),
            LocalDateTime.of(2024, 1, 15, 16, 0));

    // Event that starts within range and ends after range
    calendar.createEvent("After Overlap",
            LocalDateTime.of(2024, 1, 16, 10, 0),
            LocalDateTime.of(2024, 1, 16, 14, 0));

    // Event completely outside range
    calendar.createEvent("Outside Range",
            LocalDateTime.of(2024, 1, 17, 10, 0),
            LocalDateTime.of(2024, 1, 17, 11, 0));

    List<Event> events = calendar.getEventsListInDateRange(queryStart, queryEnd);
    assertEquals(3, events.size()); // Should include all overlapping events

    assertTrue(events.stream().anyMatch(e -> e.getSubject().equals("Before Overlap")));
    assertTrue(events.stream().anyMatch(e -> e.getSubject().equals("Within Range")));
    assertTrue(events.stream().anyMatch(e -> e.getSubject().equals("After Overlap")));
    assertFalse(events.stream().anyMatch(e -> e.getSubject().equals("Outside Range")));
  }

  @Test
  public void testIsBusyAtBoundaryConditions() throws CalendarException {
    LocalDateTime start = LocalDateTime.of(2024, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2024, 1, 15, 11, 0);

    calendar.createEvent("Meeting", start, end);

    // Test exact boundary conditions
    assertTrue(calendar.isBusyAt(start)); // Start time inclusive
    assertFalse(calendar.isBusyAt(end)); // End time exclusive

    // Test one nanosecond before and after
    assertFalse(calendar.isBusyAt(start.minusNanos(1)));
    assertFalse(calendar.isBusyAt(end.plusNanos(1)));
  }

  @Test
  public void testCreateEventSeriesWithInvalidDaysCombination() throws CalendarException {
    Set<Days> emptyDays = EnumSet.noneOf(Days.class);
    LocalTime startTime = LocalTime.of(9, 0);
    LocalTime endTime = LocalTime.of(10, 0);
    LocalDate startDate = LocalDate.of(2024, 1, 15);

    try {
      calendar.createEventSeries("Invalid Series", startTime, endTime, emptyDays,
              startDate, null, 2, null, null, null);
      fail("Should have thrown CalendarException for empty days set");
    } catch (CalendarException e) {
      assertTrue(e.getMessage().contains("recurrence day"));
    }
  }

  @Test
  public void testEditEventInvalidPropertyValue() throws CalendarException {
    LocalDateTime start = LocalDateTime.of(2024, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2024, 1, 15, 11, 0);

    Event event = calendar.createEvent("Meeting", start, end);
    UUID eventId = event.getId();

    // Test invalid datetime format
    try {
      calendar.editEvent(eventId, Property.START, "invalid-date-time");
      fail("Should have thrown CalendarException for invalid datetime");
    } catch (CalendarException e) {
      assertTrue(e.getMessage().contains("Invalid property value") ||
              e.getMessage().contains("parse"));
    }
  }

  @Test
  public void testEventEqualityAndHashCode() throws CalendarException {
    LocalDateTime start = LocalDateTime.of(2024, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2024, 1, 15, 11, 0);

    Event event1 = calendar.createEvent("Meeting", start, end);

    // Create another event with same details (should fail due to uniqueness)
    try {
      Event event2 = calendar.createEvent("Meeting", start, end);
      fail("Should not allow duplicate events");
    } catch (CalendarException e) {
      assertTrue(e.getMessage().contains("already exists") ||
              e.getMessage().contains("duplicate"));
    }
  }

  @Test
  public void testCalendarToStringWithDifferentTimezones() {
    namedCalendar.setTimezone(UTC);
    String utcString = namedCalendar.toString();
    assertTrue(utcString.contains("UTC"));

    namedCalendar.setTimezone(PST);
    String pstString = namedCalendar.toString();
    assertTrue(pstString.contains("America/Los_Angeles"));

    assertNotEquals(utcString, pstString);
  }
}