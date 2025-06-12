package calendar.model;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.LocalDate;

import calendar.CalendarException;

/**
 * Represents a calendar interface for managing events using IEvent abstraction.
 * Provides methods to create, edit, and retrieve events with improved flexibility.
 */
public interface ICalendar {

  /**
   * Creates a single, non-recurring event.
   *
   * @param subject   the subject of the event
   * @param startDate the starting date and time of the event
   * @param endDate   the ending date and time of the event. If null, creates
   *                  an all-day event from 8am to 5pm
   * @return the created IEvent object
   * @throws CalendarException if the given start date is chronologically after
   *                          the end date, or if the event already exists
   */
  IEvent createEvent(String subject, LocalDateTime startDate, LocalDateTime endDate)
          throws CalendarException;

  /**
   * Creates a series of recurring events that repeat on specific days of the week.
   *
   * @param subject     the subject for all events in the series
   * @param startTime   the starting time for each event in the series
   * @param endTime     the ending time for each event in the series
   * @param daysOfWeek  the set of days of the week on which events should occur
   * @param startDate   the starting date for the series
   * @param endDate     the ending date for the series (inclusive), or null if
   *                    using occurrence count
   * @param repeats     the number of times to repeat the event, or 0 if using
   *                    end date termination
   * @param description an optional description for all events in the series
   * @param location    an optional location for all events in the series
   * @param eventStatus the status for all events in the series (e.g., PUBLIC, PRIVATE)
   * @throws CalendarException if any event in the series would be invalid,
   *                          spans multiple days, or conflicts with existing events
   */
  void createEventSeries(String subject, LocalTime startTime, LocalTime endTime,
                         Set<Days> daysOfWeek, LocalDate startDate, LocalDate endDate,
                         int repeats, String description, Location location,
                         EventStatus eventStatus) throws CalendarException;

  /**
   * Retrieves events that match the specified subject and start time.
   *
   * @param subject   the subject to search for (case-insensitive)
   * @param startTime the exact start time to match
   * @return a list of IEvent objects matching the criteria, or empty list if none found
   */
  List<IEvent> getEventsBySubjectAndStartTime(String subject, LocalDateTime startTime);

  /**
   * Retrieves events that match the specified subject, start time, and end time.
   *
   * @param subject   the subject to search for (case-insensitive)
   * @param startTime the exact start time to match
   * @param endTime   the exact end time to match
   * @return a list of IEvent objects matching all criteria, or empty list if none found
   */
  List<IEvent> getEventsByDetails(String subject, LocalDateTime startTime,
                                  LocalDateTime endTime);

  /**
   * Retrieves all events scheduled on a specific date.
   *
   * @param date the date to query for events
   * @return a list of IEvent objects on the specified date, or empty list if none
   */
  List<IEvent> getEventsList(LocalDate date);

  /**
   * Retrieves all events that occur within a specified date and time range.
   *
   * @param start the start of the date and time range (inclusive)
   * @param end   the end of the date and time range (exclusive)
   * @return a list of IEvent objects within the specified range, or empty list if none
   * @throws IllegalArgumentException if start is after end
   */
  List<IEvent> getEventsListInDateRange(LocalDateTime start, LocalDateTime end);

  /**
   * Checks if the user is busy at a specific date and time.
   *
   * @param dateTime the specific date and time to check
   * @return true if the user has any event scheduled at the given time,
   *         false otherwise
   */
  boolean isBusyAt(LocalDateTime dateTime);

  /**
   * Edits a specific property of an individual event.
   *
   * @param eventID     the unique identifier of the event to edit
   * @param property    the property to modify (subject, start, end, description,
   *                    location, or status)
   * @param newProperty the new value for the property, as a string
   * @return the edited IEvent object
   * @throws CalendarException if the event is not found, the property value is
   *                          invalid, or the edit would create a conflict
   */
  IEvent editEvent(UUID eventID, Property property, String newProperty)
          throws CalendarException;

  /**
   * Edits a property for all events in a series starting from a specific event.
   *
   * @param seriesID    the unique identifier of the event series
   * @param property    the property to modify (subject, start, end, description,
   *                    location, or status)
   * @param newProperty the new value for the property, as a string
   * @throws CalendarException if the series is not found or the property value
   *                          is invalid
   */
  void editSeriesFromDate(UUID seriesID, Property property, String newProperty)
          throws CalendarException;

  /**
   * Edits a property for all events in an entire series.
   *
   * @param seriesID    the unique identifier of the event series
   * @param property    the property to modify (subject, start, end, description,
   *                    location, or status)
   * @param newProperty the new value for the property, as a string
   * @throws CalendarException if the series is not found, the property value is
   *                          invalid, or the modification would cause events to
   *                          span multiple days
   */
  void editSeries(UUID seriesID, Property property, String newProperty)
          throws CalendarException;

  /**
   * Retrieves an event series by its unique identifier.
   *
   * @param id the unique identifier of the event series
   * @return the EventSeries object if found, null otherwise
   */
  EventSeries getEventSeriesByID(UUID id);

  /**
   * Finds events that conflict with the given event.
   *
   * @param event the event to check for conflicts
   * @return a list of IEvent objects that conflict with the given event
   */
  default List<IEvent> findConflictingEvents(IEvent event) {
    if (event == null || event.getStart() == null || event.getEnd() == null) {
      return List.of();
    }

    return getEventsListInDateRange(event.getStart(), event.getEnd())
            .stream()
            .filter(existingEvent -> !existingEvent.getId().equals(event.getId()))
            .filter(event::conflictsWith)
            .collect(java.util.stream.Collectors.toList());
  }
}
