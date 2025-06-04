package calendar;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Represents a virtual calendar that allows you to create events and store them
 * in the calendar.
 *
 * <p>This calendar mimics the features found in widely-used calendar apps, such as
 * Google Calendar and Apple's Calendar app.
 */
public class VirtualCalendar implements ICalendar {
  private Map<LocalDate, List<Event>> calendarEvents;
  private Set<Event> uniqueEvents;
  private Map<UUID, Event> eventsByID;
  private Map<UUID, EventSeries> eventSeriesByID;

  public VirtualCalendar() {
    this.calendarEvents = new HashMap<LocalDate, List<Event>>();
    this.uniqueEvents = new HashSet<Event>();
    this.eventsByID = new HashMap<UUID, Event>();
    this.eventSeriesByID = new HashMap<UUID, EventSeries>();
  }

  /**
   * Creates a single, non-recurring event.
   *
   * @param subject     the subject of the event
   * @param startDate   the starting date and time of the event
   * @param endDate     the ending date and time of the event. If empty, make event an all-day event
   * @param description the description of the event. Can be left blank
   * @param location    the location of the event, either in-person or online. Can be left blank
   * @param eventStatus the status of the event, either public or private. Can be left blank
   * @return the created Event object.
   * @throws CalendarException if the given start date is chronologically after the end
   *                                  date, or if the event already exists.
   */
  @Override
  public Event createEvent(String subject, LocalDateTime startDate, LocalDateTime endDate,
                           String description, Location location, EventStatus eventStatus)
          throws CalendarException {
    if (startDate.isAfter(endDate)) { // check for valid dates
      throw new CalendarException("Start date cannot be after end date");
    }
    Event event = new Event(subject, startDate, endDate, description, location, null, eventStatus, null);
    if (uniqueEvents.add(event)) { // check for duplicate event
      // add event to each day from start to end date
      for (LocalDate date = startDate.toLocalDate(); !date.isAfter(endDate.toLocalDate());
           date = date.plusDays(1)) {
        calendarEvents.computeIfAbsent(date, k -> new ArrayList<Event>()).add(event);
      }
      eventsByID.put(event.getId(), event);
      return event;
    } else {
      throw new CalendarException("Event already exists");
    }
  }

  /**
   * Creates a series of recurring events.
   *
   * @param subject     the subject of the event series
   * @param startTime   the starting time of the events in the series
   * @param endTime     the ending time of each event in the series
   * @param daysOfWeek  the days of the week on which the events occur
   * @param startDate   the starting date for the series
   * @param endDate     the ending date for the series
   * @param repeats     number of times to repeat, or 0 for infinite
   * @param description description of the event series
   * @param location    location of the event series
   * @param eventStatus status of the event series
   */
  @Override
  public void createEventSeries(String subject, LocalTime startTime, LocalTime endTime,
                                Set<Days> daysOfWeek, LocalDate startDate, LocalDate endDate,
                                int repeats, String description, Location location,
                                EventStatus eventStatus)  throws CalendarException {
    // Convert Days to DayOfWeek
    Set<DayOfWeek> dayOfWeeks = daysOfWeek.stream()
            .map(Days::toDayOfWeek)
            .collect(Collectors.toSet());

    // Create first event instance
    LocalDateTime eventStart = LocalDateTime.of(startDate, startTime);
    LocalDateTime eventEnd = LocalDateTime.of(startDate, endTime);

    // Create series
    EventSeries series = new EventSeries(
            subject, eventStart, eventEnd,
            dayOfWeeks, repeats > 0 ? repeats : null,
            endDate);
    // Generate all events in series
    Set<Event> events = series.generateEvents();

    // Add each event with details
    for (Event event : events) {
      event.setDescription(description);
      event.setLocation(location);
      event.setStatus(eventStatus);
      event.setSeriesId(series.getId());

      if (!uniqueEvents.add(event)) {
        throw new CalendarException("Duplicate event in series: " + event.getSubject());
      }

      // Add to date index
      LocalDate date = event.getStart().toLocalDate();
      calendarEvents.computeIfAbsent(date, k -> new ArrayList<>()).add(event);
      eventsByID.put(event.getId(), event);
    }
    eventSeriesByID.put(series.getId(), series);
  }

  public List<Event> getEventsByDetails(String subject, LocalDateTime startTime, LocalDateTime endTime) {
    List<Event> matchingEvents = new ArrayList<>();
    // use getEventsList to narrow down
    List<Event> eventsOnDay = getEventsList(startTime.toLocalDate());
    for (Event event : eventsOnDay) {
      if (event.getSubject().equalsIgnoreCase(subject) && event.getStart().equals(startTime) &&
      event.getEnd().equals(endTime)) {
        matchingEvents.add(event);
      }
    }
    return matchingEvents;
  }

  @Override
  public List<Event> getEventsBySubjectAndStartTime(String subject, LocalDateTime startTime) {
    List<Event> matchingEvents = new ArrayList<>();
    // use getEventsList to narrow down
    List<Event> eventsOnDay = getEventsList(startTime.toLocalDate());
    for (Event event : eventsOnDay) {
      if (event.getSubject().equalsIgnoreCase(subject) && event.getStart().equals(startTime)) {
        matchingEvents.add(event);
      }
    }
    return matchingEvents;
  }

  /**
   * Retrieves a list of events for a specific date.
   *
   * @param date the date for which to retrieve events
   * @return a list of events on the specified date
   */
  @Override
  public List<Event> getEventsList(LocalDate date) {
    return calendarEvents.getOrDefault(date, new ArrayList<>());
  }

  /**
   * Retrieves a list of events within a specified date range.
   *
   * @param start the start date of the range
   * @param end   the end date of the range
   * @return a list of events occurring between the start and end dates, inclusive
   */
  @Override
  public List<Event> getEventsListInDateRange(LocalDateTime start, LocalDateTime end) {
    // use set for duplicate events
    Set<Event> uniqueEvents = new HashSet<>();
    List<Event> result = new ArrayList<>();
    for (LocalDate date = start.toLocalDate(); !date.isAfter(end.toLocalDate());
         date = date.plusDays(1)) {
      uniqueEvents.addAll(getEventsList(date));
    }
    // check that events fall within time intervals
    for (Event event : uniqueEvents) {
      LocalDateTime eventStart = event.getStart();
      LocalDateTime eventEnd = event.getEnd();

      if (eventStart.isBefore(end) && eventEnd.isAfter(start)) {
        result.add(event);
      }
    }
    return result;
  }

  /**
   * Checks if the user is busy at a specific date and time.
   *
   * @param dateTime the date and time to check
   * @return true if the user has any event scheduled at the given time,
   *         false otherwise
   */
  @Override
  public boolean isBusyAt(LocalDateTime dateTime) {
    // Get all events for the target date
    List<Event> daysEvents = getEventsList(dateTime.toLocalDate());

    // Check if any event overlaps with the given time
    for (Event event : daysEvents) {
      if (isDuringEvent(event, dateTime)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks if a specific time falls within an event's duration
   *
   * @param event The event to check
   * @param dateTime The time to verify
   * @return true if the time is within the event's start-end range
   */
  private boolean isDuringEvent(Event event, LocalDateTime dateTime) {
    LocalDateTime start = event.getStart();
    LocalDateTime end = event.getEnd();

    // Handle zero-duration events (instantaneous)
    if (start.equals(end)) {
      return dateTime.equals(start);
    }

    // Check if time falls within [start, end) interval
    return !dateTime.isBefore(start) && dateTime.isBefore(end);
  }

  // could implement Property enum
  @Override
  public Event editEvent(UUID eventID, Property property, String newValue) {
    Event event = eventsByID.get(eventID);
    if (event == null) return null;

    switch (property) {
      case SUBJECT:
        event.setSubject(newValue);
        break;
      case START:
        LocalDateTime newStart = LocalDateTime.parse(newValue);
        event.setStart(newStart);
        break;
      case END:
        LocalDateTime newEnd = LocalDateTime.parse(newValue);
        event.setEnd(newEnd);
        break;
      case DESCRIPTION:
        event.setDescription(newValue);
        break;
      case LOCATION:
        Location newLocation = Location.fromStr(newValue);
        event.setLocation(newLocation);
        break;
      case STATUS:
        EventStatus newStatus = EventStatus.valueOf(newValue);
        event.setStatus(newStatus);
        break;
    }
    return event;
  }

  @Override
  public void editSeriesFromDate(UUID seriesID, Property property, String newValue) {
    EventSeries series = eventSeriesByID.get(seriesID);
    if (series == null) return;

    // Find the first event to modify from
    Event firstEvent = null;
    for (Event event : uniqueEvents) {
      if (seriesID.equals(event.getSeriesID())) {
        firstEvent = event;
        break;
      }
    }
    if (firstEvent == null) return;

    // Edit all events from this event forward
    for (Event event : uniqueEvents) {
      if (seriesID.equals(event.getSeriesID()) &&
              !event.getStart().isBefore(firstEvent.getStart())) {
        editEvent(event.getId(), property, newValue);
      }
    }
  }

  @Override
  public void editSeries(UUID seriesID, Property property, String newValue) {
    EventSeries series = eventSeriesByID.get(seriesID);
    if (series == null) return;

    // Edit all events in the series
    for (Event event : uniqueEvents) {
      if (seriesID.equals(event.getSeriesID())) {
        editEvent(event.getId(), property, newValue);
      }
    }

    // Update series properties if needed
    if ("subject".equals(property.getStr())) {
      series.setSubject(newValue);
    }
  }
}
