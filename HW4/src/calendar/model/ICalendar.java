package calendar.model;

import java.time.*;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import calendar.CalendarException;

/**
 * Represents a calendar interface for managing events.
 * Provides methods to create, edit, and retrieve events.
 *
 * This interface supports both single events and recurring event series.
 * Events are uniquely identified by their subject, start time, and end time.
 * No two events can have the same combination of these three properties.
 */
public interface ICalendar {

  /**
   * Creates a single, non-recurring event.
   *
   * @param subject   the subject of the event
   * @param startDate the starting date and time of the event
   * @param endDate   the ending date and time of the event. If null, creates
   *                  an all-day event from 8am to 5pm
   * @return the created Event object
   * @throws CalendarException if the given start date is chronologically after
   *                          the end date, or if the event already exists
   */
  Event createEvent(String subject, LocalDateTime startDate, LocalDateTime endDate)
          throws CalendarException;

  /**
   * Creates a series of recurring events that repeat on specific days of the week.
   *
   * Each event in the series must start and end on the same day. The series
   * can be terminated either by a specific number of occurrences or by an end date.
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
   * The subject comparison is case-insensitive.
   *
   * @param subject   the subject to search for (case-insensitive)
   * @param startTime the exact start time to match
   * @return a list of events matching the criteria, or empty list if none found
   */
  List<Event> getEventsBySubjectAndStartTime(String subject, LocalDateTime startTime);

  /**
   * Retrieves events that match the specified subject, start time, and end time.
   *
   * All three parameters must match exactly. The subject comparison is
   * case-insensitive.
   *
   * @param subject   the subject to search for (case-insensitive)
   * @param startTime the exact start time to match
   * @param endTime   the exact end time to match
   * @return a list of events matching all criteria, or empty list if none found
   */
  List<Event> getEventsByDetails(String subject, LocalDateTime startTime,
                                 LocalDateTime endTime);

  /**
   * Retrieves all events scheduled on a specific date.
   *
   * This includes events that start on the specified date, regardless of
   * when they end. Multi-day events will appear in the results for each day
   * they span.
   *
   * @param date the date to query for events
   * @return a list of events on the specified date, or empty list if none
   */
  List<Event> getEventsList(LocalDate date);

  /**
   * Retrieves all events that occur within a specified date and time range.
   *
   * An event is included if it overlaps with the specified range in any way.
   * This includes events that start before the range and end within it,
   * events that start within the range, and events that span the entire range.
   *
   * @param start the start of the date and time range (inclusive)
   * @param end   the end of the date and time range (exclusive)
   * @return a list of events within the specified range, or empty list if none
   * @throws IllegalArgumentException if start is after end
   */
  List<Event> getEventsListInDateRange(LocalDateTime start, LocalDateTime end);

  /**
   * Checks if the user is busy at a specific date and time.
   *
   * A user is considered busy if any event is scheduled at the given time.
   * The check uses a half-open interval (start, end), meaning the user is
   * busy from the event start time up to (but not including) the event end time.
   *
   * @param dateTime the specific date and time to check
   * @return true if the user has any event scheduled at the given time,
   *         false otherwise
   */
  boolean isBusyAt(LocalDateTime dateTime);

  /**
   * Edits a specific property of an individual event.
   *
   * If the event is part of a series and the start or end time is modified,
   * the event will be removed from the series. The edited event must not
   * conflict with any existing events.
   *
   * @param eventID     the unique identifier of the event to edit
   * @param property    the property to modify (subject, start, end, description,
   *                    location, or status)
   * @param newProperty the new value for the property, as a string
   * @return the edited Event object
   * @throws CalendarException if the event is not found, the property value is
   *                          invalid, or the edit would create a conflict
   */
  Event editEvent(UUID eventID, Property property, String newProperty)
          throws CalendarException;

  /**
   * Edits a property for all events in a series starting from a specific event.
   *
   * This method finds the event series containing the specified event and
   * modifies the given property for that event and all subsequent events in
   * the series. If the specified event is not part of a series, only that
   * event is modified.
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
   * This method modifies the specified property for every event in the
   * series, regardless of when they occur. For start and end time modifications,
   * the series definition itself is updated and all events are regenerated
   * with the new times.
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
}