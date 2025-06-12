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
 * Google Calendar.
 */
public class VirtualCalendar implements ICalendar {
  // A map to store events, with LocalDate as the key and a list of Events as the value.
  private final Map<LocalDate, List<IEvent>> calendarEvents;
  // A set to ensure that all stored events are unique.
  private final Set<Event> uniqueEvents;
  // A map to quickly retrieve events by their unique ID.
  private final Map<UUID, Event> eventsByID;
  // A map to quickly retrieve event series by their unique ID.
  private final Map<UUID, EventSeries> eventSeriesByID;

  /**
   * Constructs a new VirtualCalendar and initializes the internal data structures.
   */
  public VirtualCalendar() {
    this.calendarEvents = new HashMap<LocalDate, List<IEvent>>();
    this.uniqueEvents = new HashSet<Event>();
    this.eventsByID = new HashMap<UUID, Event>();
    this.eventSeriesByID = new HashMap<UUID, EventSeries>();
  }

  /**
   * Creates a single, non-recurring event.
   *
   * @param subject   the subject of the event
   * @param startDate the starting date and time of the event
   * @param endDate   the ending date and time of the event. If empty, make event an all-day event
   * @return the created Event object.
   * @throws CalendarException if the given start date is chronologically after the end
   *                           date, or if the event already exists.
   */
  @Override
  public Event createEvent(String subject, LocalDateTime startDate, LocalDateTime endDate)
          throws CalendarException {
    // Throws an exception if the start date is after the end date.
    if (startDate.isAfter(endDate)) { // check for valid dates
      throw new CalendarException("Start date cannot be after end date");
    }
    // Creates a new Event object.
    Event event = new Event(subject, startDate, endDate);
    // Attempts to add the event to the set of unique events.
    if (uniqueEvents.add(event)) { // check for duplicate event
      // Iterates from the start date to the end date (inclusive) to add the event
      // to each relevant day.
      // add event to each day from start to end date
      for (LocalDate date = startDate.toLocalDate(); !date.isAfter(endDate.toLocalDate());
           date = date.plusDays(1)) {
        // Adds the event to the list of events for the current date, creating a new
        // list if one doesn't exist.
        calendarEvents.computeIfAbsent(date, k -> new ArrayList<IEvent>()).add(event);
      }
      // Stores the event in the map, using its ID as the key.
      eventsByID.put(event.getId(), event);
      // Returns the newly created event.
      return event;
    } else {
      // Throws an exception if the event already exists.
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
   * @throws CalendarException if there's a duplicate event generated within the series.
   */
  @Override
  public void createEventSeries(String subject, LocalTime startTime, LocalTime endTime,
                                Set<Days> daysOfWeek, LocalDate startDate, LocalDate endDate,
                                int repeats, String description, Location location,
                                EventStatus eventStatus) throws CalendarException {
    // Converts the custom 'Days' enum set to Java's 'DayOfWeek' enum set.
    Set<DayOfWeek> dayOfWeeks = daysOfWeek.stream()
            .map(Days::toDayOfWeek)
            .collect(Collectors.toSet());

    // Creates the LocalDateTime for the start of the first event instance.
    LocalDateTime eventStart = LocalDateTime.of(startDate, startTime);
    // Creates the LocalDateTime for the end of the first event instance.
    LocalDateTime eventEnd = LocalDateTime.of(startDate, endTime);

    // Creates a new EventSeries object.
    EventSeries series = new EventSeries(
            subject, eventStart, eventEnd,
            dayOfWeeks, repeats > 0 ? repeats : null, // Sets repeats to null if 0 for infinite.
            endDate);
    // Generates all individual Event objects belonging to this series.
    Set<Event> events = series.generateEvents();

    // Iterates through each generated event in the series to add details and store them.
    for (Event event : events) {
      // Sets the description for the current event.
      event.setDescription(description);
      // Sets the location for the current event.
      event.setLocation(location);
      // Sets the status for the current event.
      event.setStatus(eventStatus);
      // Sets the series ID for the current event, linking it to its series.
      event.setSeriesId(series.getId());

      // Attempts to add the event to the set of unique events, throwing an exception
      // if it's a duplicate.
      if (!uniqueEvents.add(event)) {
        throw new CalendarException("Duplicate event in series: " + event.getSubject());
      }

      // Gets the LocalDate for the event's start.
      // Add to date index
      LocalDate date = event.getStart().toLocalDate();
      // Adds the event to the list of events for its specific date, creating a
      // new list if necessary.
      calendarEvents.computeIfAbsent(date, k -> new ArrayList<>()).add(event);
      // Stores the event in the map, using its ID as the key.
      eventsByID.put(event.getId(), event);
    }
    // Stores the created event series in the map, using its ID as the key.
    eventSeriesByID.put(series.getId(), series);
  }

  /**
   * Retrieves a list of events matching a given subject, start time, and end time.
   *
   * @param subject   the subject of the event to match.
   * @param startTime the starting date and time of the event to match.
   * @param endTime   the ending date and time of the event to match.
   * @return a list of events that precisely match the provided details.
   */
  public List<IEvent> getEventsByDetails(String subject, LocalDateTime startTime, LocalDateTime
          endTime) {
    // Initializes an empty list to store matching events.
    List<IEvent> matchingEvents = new ArrayList<>();
    // Retrieves all events scheduled for the day of the provided start time to narrow
    // down the search.
    // use getEventsList to narrow down
    List<IEvent> eventsOnDay = getEventsList(startTime.toLocalDate());
    // Iterates through the events on that day.
    for (IEvent event : eventsOnDay) {
      // Checks if the event's subject, start time, and end time match the given criteria
      if (event.getSubject().equalsIgnoreCase(subject) && event.getStart().equals(startTime) &&
              event.getEnd().equals(endTime)) {
        // Adds the matching event to the list.
        matchingEvents.add(event);
      }
    }
    // Returns the list of matching events.
    return matchingEvents;
  }

  /**
   * Retrieves a list of events matching a given subject and start time.
   *
   * @param subject   the subject of the event to match.
   * @param startTime the starting date and time of the event to match.
   * @return a list of events that precisely match the provided subject and start time.
   */
  @Override
  public List<IEvent> getEventsBySubjectAndStartTime(String subject, LocalDateTime startTime) {
    // Initializes an empty list to store matching events.
    List<IEvent> matchingEvents = new ArrayList<>();
    // Retrieves all events scheduled for the day of the provided start time to narrow
    // down the search.
    // use getEventsList to narrow down
    List<IEvent> eventsOnDay = getEventsList(startTime.toLocalDate());
    // Iterates through the events on that day.
    for (IEvent event : eventsOnDay) {
      // Checks if the event's subject and start time match the given criteria
      if (event.getSubject().equalsIgnoreCase(subject) && event.getStart().equals(startTime)) {
        // Adds the matching event to the list.
        matchingEvents.add(event);
      }
    }
    // Returns the list of matching events.
    return matchingEvents;
  }

  /**
   * Retrieves a list of events for a specific date.
   *
   * @param date the date for which to retrieve events
   * @return a list of events on the specified date, or an empty list if no events are found.
   */
  @Override
  public List<IEvent> getEventsList(LocalDate date) {
    // Retrieves the list of events associated with the given date, or returns an empty
    // ArrayList if the date is not found.
    return calendarEvents.getOrDefault(date, new ArrayList<>());
  }

  /**
   * Retrieves a list of events within a specified date range.
   *
   * @param start the start date and time of the range (inclusive).
   * @param end   the end date and time of the range (exclusive for the end time of events).
   * @return a list of events occurring between the start and end dates, inclusive.
   * @throws IllegalArgumentException if the start date is after the end date.
   */
  @Override
  public List<IEvent> getEventsListInDateRange(LocalDateTime start, LocalDateTime end) {
    // Throws an exception if the start date is after the end date.
    if (start.isAfter(end)) {
      throw new IllegalArgumentException("Start date must be before end date");
    }

    // Uses a set to automatically handle and prevent duplicate events from being added
    // to the result.
    // use set for duplicate events
    Set<IEvent> uniqueEvents = new HashSet<>();
    // Initializes an empty list to store the final result.
    List<IEvent> result = new ArrayList<>();
    // Iterates through each day from the start date to the end date (inclusive).
    for (LocalDate date = start.toLocalDate(); !date.isAfter(end.toLocalDate());
         date = date.plusDays(1)) {
      // Adds all events for the current date to the uniqueEvents set.
      uniqueEvents.addAll(getEventsList(date));
    }
    // Iterates through the unique events collected from the date range.
    // check that events fall within time intervals
    for (IEvent event : uniqueEvents) {
      // Gets the start time of the current event.
      LocalDateTime eventStart = event.getStart();
      // Gets the end time of the current event.
      LocalDateTime eventEnd = event.getEnd();

      // Checks if the event's time range overlaps with the query's time range.
      if (eventStart.isBefore(end) && eventEnd.isAfter(start)) {
        // Adds the overlapping event to the result list.
        result.add(event);
      }
    }
    // Returns the list of events within the specified date range.
    return result;
  }

  /**
   * Checks if the user is busy at a specific date and time.
   *
   * @param dateTime the date and time to check
   * @return true if the user has any event scheduled at the given time, false otherwise
   */
  @Override
  public boolean isBusyAt(LocalDateTime dateTime) {
    // Retrieves all events for the day corresponding to the given dateTime.
    // Get all events for the target date
    List<IEvent> daysEvents = getEventsList(dateTime.toLocalDate());

    // Iterates through each event scheduled for that day.
    // Check if any event overlaps with the given time
    for (IEvent event : daysEvents) {
      // Checks if the given dateTime falls within the duration of the current event.
      if (isDuringEvent(event, dateTime)) {
        // Returns true immediately if an overlapping event is found.
        return true;
      }
    }
    // Returns false if no overlapping events are found for the given dateTime.
    return false;
  }

  /**
   * Checks if a specific time falls within an event's duration.
   *
   * @param event    The event to check.
   * @param dateTime The time to verify.
   * @return true if the time is within the event's start (inclusive) and end (exclusive) range.
   */
  private boolean isDuringEvent(IEvent event, LocalDateTime dateTime) {
    // Gets the start time of the event.
    LocalDateTime start = event.getStart();
    // Gets the end time of the event.
    LocalDateTime end = event.getEnd();

    // Handles events with zero duration (start time equals end time).
    if (start.equals(end)) {
      // For instantaneous events, it's "during" only if the dateTime exactly matches the
      // start/end time.
      return dateTime.equals(start);
    }

    // Checks if the given dateTime is not before the event's start AND is before the event's end.
    // This defines the interval as (start, end).
    return !dateTime.isBefore(start) && dateTime.isBefore(end);
  }

  /**
   * Edits a specific property of an existing event.
   *
   * @param eventID  the unique identifier of the event to edit.
   * @param property the property of the event to change (e.g., SUBJECT, START, END).
   * @param newValue the new value for the specified property as a String.
   * @return the modified Event object.
   * @throws CalendarException if the event is not found, the new value is invalid,
   *                           or the edit would create a conflict with an existing event or
   *                           break series membership logic.
   */
  @Override
  public Event editEvent(UUID eventID, Property property, String newValue)
          throws CalendarException {
    Event event = findEventById(eventID);
    EventEditContext context = createEditContext(event, property);

    try {
      // Temporarily remove event from uniqueness tracking
      uniqueEvents.remove(event);

      // Store original values for potential rollback
      String originalSubject = event.getSubject();
      LocalDateTime originalStart = event.getStart();
      LocalDateTime originalEnd = event.getEnd();

      // Apply the property change
      applyPropertyChange(event, property, newValue);

      // Handle series membership if needed
      handleSeriesMembership(event, context);

      // Check for conflicts with other events (not including the current event)
      Event conflictingEvent = findConflictingEvent(event);
      if (conflictingEvent != null) {
        // Rollback changes
        event.setSubject(originalSubject);
        event.setStart(originalStart);
        event.setEnd(originalEnd);
        event.setSeriesId(context.getOriginalSeriesId());
        uniqueEvents.add(event);
        throw new CalendarException("Event conflicts with existing event");
      }

      // Re-add to tracking if no conflicts
      if (!uniqueEvents.add(event)) {
        // This shouldn't happen since we removed it earlier, but just in case
        event.setSubject(originalSubject);
        event.setStart(originalStart);
        event.setEnd(originalEnd);
        event.setSeriesId(context.getOriginalSeriesId());
        uniqueEvents.add(event);
        throw new CalendarException("Event conflicts with existing event");
      }

      // Update calendar mapping if dates changed
      updateCalendarMapping(event, context, property);

      return event;

    } catch (CalendarException e) {
      // Re-add original event back to tracking
      uniqueEvents.add(event);
      throw e;
    } catch (Exception e) {
      // Rollback changes and re-add original event
      rollbackChanges(event, context);
      throw new CalendarException("Failed to edit event: " + e.getMessage());
    }
  }

  /**
   * Finds a conflicting event based on subject, start, and end times.
   * This checks if another event exists with the same subject, start, and end times.
   */
  private Event findConflictingEvent(Event eventToCheck) {
    for (Event existingEvent : uniqueEvents) {
      // Skip the event we're editing (shouldn't happen since we removed it, but be safe)
      if (existingEvent.getId().equals(eventToCheck.getId())) {
        continue;
      }

      // Check if another event has the same subject, start, and end times
      if (existingEvent.getSubject().equals(eventToCheck.getSubject()) &&
              existingEvent.getStart().equals(eventToCheck.getStart()) &&
              existingEvent.getEnd().equals(eventToCheck.getEnd())) {
        return existingEvent;
      }
    }
    return null;
  }

  /**
   * Finds an event by its ID.
   */
  private Event findEventById(UUID eventID) throws CalendarException {
    Event event = eventsByID.get(eventID);
    if (event == null) {
      throw new CalendarException("Event not found");
    }
    return event;
  }

  /**
   * Creates a context object to store original values for potential rollback.
   */
  private EventEditContext createEditContext(Event event, Property property) {
    return new EventEditContext(
            event.getSubject(),
            event.getStart(),
            event.getEnd(),
            event.getSeriesID(),
            shouldBreakSeriesMembership(event, property)
    );
  }

  /**
   * Determines if editing this property would break series membership.
   */
  private boolean shouldBreakSeriesMembership(Event event, Property property) {
    return event.getSeriesID() != null &&
            (property == Property.START || property == Property.END);
  }

  /**
   * Applies the property change to the event.
   */
  private void applyPropertyChange(Event event, Property property, String newValue)
          throws CalendarException {
    try {
      switch (property) {
        case SUBJECT:
          event.setSubject(newValue);
          break;
        case START:
          validateAndSetStartTime(event, newValue);
          break;
        case END:
          validateAndSetEndTime(event, newValue);
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
        default:
          throw new CalendarException("Unsupported property: " + property);
      }
    } catch (IllegalArgumentException | DateTimeParseException e) {
      throw new CalendarException("Invalid property value: " + e.getMessage());
    }
  }

  /**
   * Validates and sets the start time for an event.
   */
  private void validateAndSetStartTime(Event event, String newValue) throws CalendarException {
    // Let DateTimeParseException bubble up to be caught by applyPropertyChange
    LocalDateTime newStart = LocalDateTime.parse(newValue);
    if (newStart.isAfter(event.getEnd())) {
      throw new CalendarException("New start time cannot be after current end time");
    }
    event.setStart(newStart);
  }

  /**
   * Validates and sets the end time for an event.
   */
  private void validateAndSetEndTime(Event event, String newValue) throws CalendarException {
    // Let DateTimeParseException bubble up to be caught by applyPropertyChange
    LocalDateTime newEnd = LocalDateTime.parse(newValue);
    if (event.getStart().isAfter(newEnd)) {
      throw new CalendarException("New end time cannot be before current start time");
    }
    event.setEnd(newEnd);
  }

  /**
   * Handles series membership changes if needed.
   */
  private void handleSeriesMembership(Event event, EventEditContext context) {
    if (context.shouldBreakSeries()) {
      event.setSeriesId(null);
    }
  }

  /**
   * Validates the edited event for conflicts and re-adds it to tracking.
   */
  private void validateAndReaddEvent(Event event, EventEditContext context)
          throws CalendarException {
    if (!uniqueEvents.add(event)) {
      throw new CalendarException("Event conflicts with existing event");
    }
  }

  /**
   * Updates the calendar date mapping if the event's dates changed.
   */
  private void updateCalendarMapping(Event event, EventEditContext context, Property property) {
    if (property == Property.START || property == Property.END) {
      removeFromOldDateMapping(context.getOriginalStart().toLocalDate(), event.getId());
      addToNewDateMapping(event.getStart().toLocalDate(), event);
    }
  }

  /**
   * Removes an event from its old date mapping.
   */
  private void removeFromOldDateMapping(LocalDate oldDate, UUID eventId) {
    if (calendarEvents.containsKey(oldDate)) {
      calendarEvents.get(oldDate).removeIf(e -> e.getId().equals(eventId));
      if (calendarEvents.get(oldDate).isEmpty()) {
        calendarEvents.remove(oldDate);
      }
    }
  }

  /**
   * Adds an event to its new date mapping.
   */
  private void addToNewDateMapping(LocalDate newDate, Event event) {
    calendarEvents.computeIfAbsent(newDate, k -> new ArrayList<>()).add(event);
  }

  /**
   * Rolls back all changes made to the event.
   */
  private void rollbackChanges(Event event, EventEditContext context) {
    event.setSubject(context.getOriginalSubject());
    event.setStart(context.getOriginalStart());
    event.setEnd(context.getOriginalEnd());
    event.setSeriesId(context.getOriginalSeriesId());
    uniqueEvents.add(event);
  }

  /**
   * Context class to hold original event values for rollback purposes.
   */
  private static class EventEditContext {
    private final String originalSubject;
    private final LocalDateTime originalStart;
    private final LocalDateTime originalEnd;
    private final UUID originalSeriesId;
    private final boolean shouldBreakSeries;

    public EventEditContext(String originalSubject, LocalDateTime originalStart,
                            LocalDateTime originalEnd, UUID originalSeriesId,
                            boolean shouldBreakSeries) {
      this.originalSubject = originalSubject;
      this.originalStart = originalStart;
      this.originalEnd = originalEnd;
      this.originalSeriesId = originalSeriesId;
      this.shouldBreakSeries = shouldBreakSeries;
    }

    // Getters
    public String getOriginalSubject() { return originalSubject; }
    public LocalDateTime getOriginalStart() { return originalStart; }
    public LocalDateTime getOriginalEnd() { return originalEnd; }
    public UUID getOriginalSeriesId() { return originalSeriesId; }
    public boolean shouldBreakSeries() { return shouldBreakSeries; }
  }

  /**
   * Edits a series of events from a specific date onwards.
   * This method is intended to modify events within a series that occur on or after a certain
   * point in time.
   *
   * @param seriesID the unique identifier of the event series to edit.
   * @param property the property of the events to change.
   * @param newValue the new value for the specified property as a String.
   * @throws CalendarException if any individual event in the series cannot be edited.
   */
  @Override
  public void editSeriesFromDate(UUID seriesID, Property property, String newValue)
          throws CalendarException {
    // Retrieves the EventSeries object by its ID.
    EventSeries series = eventSeriesByID.get(seriesID);
    // If the series is not found, simply return.
    if (series == null) {
      return;
    }

    // Finds the start time of the first event in the series to establish the modification point.
    // Find the first event to modify from
    LocalDateTime firstEventStart = null;
    // Iterates through all unique events to find the first event belonging to this series.
    for (Event event : uniqueEvents) {
      if (seriesID.equals(event.getSeriesID())) {
        firstEventStart = event.getStart();
        break; // Once found, break the loop.
      }
    }
    // If no events are found for this series, return.
    if (firstEventStart == null) {
      return;
    }

    // Collects all events in the series that need to be edited (on or after firstEventStart).
    // Collect events to edit to avoid concurrent modification
    List<IEvent> eventsToEdit = new ArrayList<>();
    // Iterates through all unique events.
    for (Event event : uniqueEvents) {
      // If the event belongs to the series and starts on or after the firstEventStart,
      // add it to the list.
      if (seriesID.equals(event.getSeriesID()) &&
              !event.getStart().isBefore(firstEventStart)) {
        eventsToEdit.add(event);
      }
    }
    // Iterates through the collected events and calls editEvent for each.
    for (IEvent event : eventsToEdit) {
      editEvent(event.getId(), property, newValue);
    }
  }

  /**
   * Edits a property for all events within a recurring event series.
   *
   * @param seriesID the unique identifier of the event series to edit.
   * @param property the property of the event series to change.
   * @param newValue the new value for the specified property as a String.
   * @throws CalendarException if the series is not found, the new value is invalid,
   *                           or the edit would cause an event to cross a day boundary.
   */
  @Override
  public void editSeries(UUID seriesID, Property property, String newValue)
          throws CalendarException {
    // Retrieves the EventSeries object by its ID.
    EventSeries series = eventSeriesByID.get(seriesID);
    // If the series is not found, simply return.
    if (series == null) {
      return;
    }

    // Updates the series properties first based on the specified property.
    // Update series properties first
    switch (property) {
      case SUBJECT:
        // Sets the new subject for the entire series.
        series.setSubject(newValue);
        break;
      case START:
        // Parses the new start time for the series.
        LocalTime newStart = LocalTime.parse(newValue);
        // Sets the new start time for the series.
        series.setStartTime(newStart);
        break;
      case END:
        // Parses the new end time for the series.
        LocalTime newEnd = LocalTime.parse(newValue);
        // Calculates the duration based on the series' current start time and the new end time.
        Duration newDuration = Duration.between(series.getStartTime(), newEnd);

        // Checks if the new duration is negative, which would imply the end time is
        // before the start time on the same day.
        // This would mean the event spans to the next day, which is not allowed
        if (newDuration.isNegative()) {
          throw new CalendarException("Event would cross day boundary");
        }

        // Additional validation to ensure the event doesn't cross a day boundary after
        // the duration change.
        // Additional validation: ensure the calculated end time is on the same day
        LocalDate testDate = LocalDate.of(2000, 1, 1);
        // Creates a test LocalDateTime with the arbitrary date and series start time.
        LocalDateTime testStart = LocalDateTime.of(testDate, series.getStartTime());
        // Calculates the test end time by adding the new duration to the test start time.
        LocalDateTime testEnd = testStart.plus(newDuration);

        // If the test start date and test end date are different, it means the event
        // would cross a day boundary.
        if (!testStart.toLocalDate().equals(testEnd.toLocalDate())) {
          throw new CalendarException("Event would cross day boundary");
        }

        // Sets the new duration for the series.
        series.setDuration(newDuration);
        break;
      default:
        break;
    }

    // Iterates through all individual events to update them based on the series'
    // modified properties.
    // Edit all events in the series
    for (Event event : uniqueEvents) {
      // Checks if the current event belongs to the series being edited.
      if (seriesID.equals(event.getSeriesID())) {
        // For subject, start, and end - update through series properties
        if (property == Property.SUBJECT) {
          // If the subject was changed for the series, update the individual event's subject.
          event.setSubject(newValue);
        } else if (property == Property.START) {
          // If the start time was changed for the series, update the individual event's start time.
          LocalDateTime newStartTime = LocalDateTime.of(
                  event.getStart().toLocalDate(), // Keep the original date of the event.
                  series.getStartTime() // Use the new series start time.
          );
          event.setStart(newStartTime);
        } else if (property == Property.END) {
          // If the end time (via duration) was changed for the series, update the
          // individual event's end time.
          LocalDateTime newEndTime = event.getStart().plus(series.getDuration());
          event.setEnd(newEndTime);
        } else {
          // For other properties (DESCRIPTION, LOCATION, STATUS), use the standard
          // editEvent method.
          // For other properties, use standard edit
          editEvent(event.getId(), property, newValue);
        }
      }
    }
  }

  /**
   * Retrieves an event series by its unique identifier.
   *
   * @param id the unique identifier of the event series.
   * @return the EventSeries object if found, null otherwise.
   */
  public EventSeries getEventSeriesByID(UUID id) {
    // Retrieves the EventSeries from the map using its ID.
    return eventSeriesByID.get(id);
  }
}