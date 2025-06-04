package calendar.model;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import calendar.CalendarException;

/**
 * Represents a virtual calendar that allows you to create events and store them
 * in the calendar.
 *
 * <p>This calendar mimics the features found in widely-used calendar apps, such as
 * Google Calendar and Apple's Calendar app.
 */
public class VirtualCalendar implements ICalendar {
  private final Map<LocalDate, List<Event>> calendarEvents;
  private final Set<Event> uniqueEvents;
  private final Map<UUID, Event> eventsByID;
  private final Map<UUID, EventSeries> eventSeriesByID;

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
   * @return the created Event object.
   * @throws CalendarException if the given start date is chronologically after the end
   *                                  date, or if the event already exists.
   */
  @Override
  public Event createEvent(String subject, LocalDateTime startDate, LocalDateTime endDate)
          throws CalendarException {
    if (startDate.isAfter(endDate)) { // check for valid dates
      throw new CalendarException("Start date cannot be after end date");
    }
    Event event = new Event(subject, startDate, endDate);
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

  @Override
  public Event editEvent(UUID eventID, Property property, String newValue)
          throws CalendarException {
    Event event = eventsByID.get(eventID);
    if (event == null) {
      throw new CalendarException("Event not found");
    }

    // Store original values for uniqueness check
    String originalSubject = event.getSubject();
    LocalDateTime originalStart = event.getStart();
    LocalDateTime originalEnd = event.getEnd();
    UUID originalSeriesId = event.getSeriesID();

    // Check if editing would break series membership
    boolean breakingSeries = false;
    if (originalSeriesId != null &&
            (property == Property.START || property == Property.END)) {
      breakingSeries = true;
    }

    // Temporarily remove from uniqueEvents for uniqueness check
    uniqueEvents.remove(event);

    try {
      switch (property) {
        case SUBJECT:
          event.setSubject(newValue);
          break;
        case START:
          LocalDateTime newStart = LocalDateTime.parse(newValue);
          if (newStart.isAfter(event.getEnd())) {
            throw new CalendarException("New start time cannot be after current end time");
          }
          event.setStart(newStart);
          break;
        case END:
          LocalDateTime newEnd = LocalDateTime.parse(newValue);
          if (event.getStart().isAfter(newEnd)) {
            throw new CalendarException("New end time cannot be before current start time");
          }
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
          EventStatus newStatus = EventStatus.fromStr(newValue);
          event.setStatus(newStatus);
          break;
      }
    } catch (IllegalArgumentException | DateTimeParseException e) {
      // Revert to original values
      event.setSubject(originalSubject);
      event.setStart(originalStart);
      event.setEnd(originalEnd);
      uniqueEvents.add(event);
      throw new CalendarException("Invalid property value: " + e.getMessage());
    }

    // Handle series membership
    if (breakingSeries) {
      event.setSeriesId(null);
    }

    // Check for uniqueness with new values
    if (!uniqueEvents.add(event)) {
      // Revert to original values
      event.setSubject(originalSubject);
      event.setStart(originalStart);
      event.setEnd(originalEnd);
      event.setSeriesId(originalSeriesId);
      uniqueEvents.add(event);
      throw new CalendarException("Event conflicts with existing event");
    }

    // Update calendar events mapping if dates changed
    if (property == Property.START || property == Property.END) {
      LocalDate oldDate = originalStart.toLocalDate();
      LocalDate newDate = event.getStart().toLocalDate();

      // Remove from old date
      if (calendarEvents.containsKey(oldDate)) {
        calendarEvents.get(oldDate).removeIf(e -> e.getId().equals(eventID));
        if (calendarEvents.get(oldDate).isEmpty()) {
          calendarEvents.remove(oldDate);
        }
      }

      // Add to new date
      calendarEvents.computeIfAbsent(newDate, k -> new ArrayList<>()).add(event);
    }

    return event;
  }

  @Override
  public void editSeriesFromDate(UUID seriesID, Property property, String newValue)
          throws CalendarException {
    EventSeries series = eventSeriesByID.get(seriesID);
    if (series == null) return;

    // Find the first event to modify from
    LocalDateTime firstEventStart = null;
    for (Event event : uniqueEvents) {
      if (seriesID.equals(event.getSeriesID())) {
        firstEventStart = event.getStart();
        break;
      }
    }
    if (firstEventStart == null) return;

    // Edit all events from this event forward
    for (Event event : uniqueEvents) {
      if (seriesID.equals(event.getSeriesID()) &&
              !event.getStart().isBefore(firstEventStart)) {
        editEvent(event.getId(), property, newValue);
      }
    }
  }

  @Override
  public void editSeries(UUID seriesID, Property property, String newValue)
          throws CalendarException {
    EventSeries series = eventSeriesByID.get(seriesID);
    if (series == null) return;

    // Update series properties first
    switch (property) {
      case SUBJECT:
        series.setSubject(newValue);
        break;
      case START:
        LocalTime newStart = LocalTime.parse(newValue);
        series.setStartTime(newStart);
        break;
      case END:
        // When changing end time, we need to update duration
        LocalTime newEnd = LocalTime.parse(newValue);
        Duration newDuration = Duration.between(series.getStartTime(), newEnd);
        series.setDuration(newDuration);
        break;
      case DESCRIPTION:
      case LOCATION:
      case STATUS:
        // These don't affect series pattern, handled in events
        break;
    }

    // Edit all events in the series
    for (Event event : uniqueEvents) {
      if (seriesID.equals(event.getSeriesID())) {
        // For subject, start, and end - update through series properties
        if (property == Property.SUBJECT) {
          event.setSubject(newValue);
        } else if (property == Property.START) {
          LocalDateTime newStartTime = LocalDateTime.of(
                  event.getStart().toLocalDate(),
                  series.getStartTime()
          );
          event.setStart(newStartTime);
        } else if (property == Property.END) {
          LocalDateTime newEndTime = event.getStart().plus(series.getDuration());
          event.setEnd(newEndTime);
        } else {
          // For other properties, use standard edit
          editEvent(event.getId(), property, newValue);
        }
      }
    }
  }
}
