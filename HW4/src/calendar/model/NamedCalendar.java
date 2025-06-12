package calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import calendar.CalendarException;

/**
 * A named calendar with timezone support that wraps a VirtualCalendar.
 * This class provides timezone-aware calendar functionality while delegating
 * the core calendar operations to the underlying VirtualCalendar.
 */
public class NamedCalendar implements ICalendar {
  private String name;
  private ZoneId timezone;
  private final VirtualCalendar calendar;

  /**
   * Constructs a new NamedCalendar with the specified name and timezone.
   *
   * @param name     the name of the calendar
   * @param timezone the timezone for the calendar
   */
  public NamedCalendar(String name, ZoneId timezone) {
    this.name = name;
    this.timezone = timezone;
    this.calendar = new VirtualCalendar();
  }

  /**
   * Gets the name of this calendar.
   *
   * @return the calendar name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name of this calendar.
   *
   * @param name the new name for the calendar
   * @throws CalendarException if the name is null or empty
   */
  public void setName(String name) throws CalendarException {
    if (name == null || name.trim().isEmpty()) {
      throw new CalendarException("Calendar name cannot be empty");
    }
    this.name = name.trim();
  }

  /**
   * Gets the timezone of this calendar.
   *
   * @return the calendar timezone
   */
  public ZoneId getTimezone() {
    return timezone;
  }

  /**
   * Sets the timezone of this calendar.
   *
   * @param timezone the new timezone for the calendar
   */
  public void setTimezone(ZoneId timezone) {
    this.timezone = timezone;
  }

  /**
   * Creates a new event in this calendar.
   *
   * @param subject   the subject of the event.
   * @param startDate the start date and time of the event.
   * @param endDate   the end date and time of the event.
   * @return the created Event object.
   * @throws CalendarException if there is an issue creating the event.
   */
  @Override
  public Event createEvent(String subject, LocalDateTime startDate, LocalDateTime endDate)
          throws CalendarException {
    return calendar.createEvent(subject, startDate, endDate);
  }

  /**
   * Creates a new series of recurring events in this calendar.
   *
   * @param subject     the subject of the event series.
   * @param startTime   the start time of the event for each day it occurs.
   * @param endTime     the end time of the event for each day it occurs.
   * @param daysOfWeek  a set of days of the week on which the event recurs.
   * @param startDate   the start date of the series.
   * @param endDate     the end date of the series.
   * @param repeats     the number of times the series repeats (0 for infinite).
   * @param description a description for the events in the series.
   * @param location    the location of the events in the series.
   * @param eventStatus the status of the events in the series.
   * @throws CalendarException if there is an issue creating the event series.
   */
  @Override
  public void createEventSeries(String subject, LocalTime startTime, LocalTime endTime,
                                Set<Days> daysOfWeek, LocalDate startDate, LocalDate endDate,
                                int repeats, String description, Location location,
                                EventStatus eventStatus) throws CalendarException {
    calendar.createEventSeries(subject, startTime, endTime, daysOfWeek, startDate, endDate,
            repeats, description, location, eventStatus);
  }

  /**
   * Retrieves a list of events matching a specific subject and start time.
   *
   * @param subject   the subject of the events to retrieve.
   * @param startTime the start time of the events to retrieve.
   * @return a list of Event objects that match the criteria.
   */
  @Override
  public List<IEvent> getEventsBySubjectAndStartTime(String subject, LocalDateTime startTime) {
    return calendar.getEventsBySubjectAndStartTime(subject, startTime);
  }

  /**
   * Retrieves a list of events matching a specific subject, start time, and end time.
   *
   * @param subject   the subject of the events to retrieve.
   * @param startTime the start time of the events to retrieve.
   * @param endTime   the end time of the events to retrieve.
   * @return a list of Event objects that match the criteria.
   */
  @Override
  public List<IEvent> getEventsByDetails(String subject, LocalDateTime startTime,
                                        LocalDateTime endTime) {
    return calendar.getEventsByDetails(subject, startTime, endTime);
  }

  /**
   * Retrieves a list of all events occurring on a specific date.
   *
   * @param date the date for which to retrieve events.
   * @return a list of Event objects occurring on the specified date.
   */
  @Override
  public List<IEvent> getEventsList(LocalDate date) {
    return calendar.getEventsList(date);
  }

  /**
   * Retrieves a list of all events occurring within a specified date and time range.
   *
   * @param start the start of the date and time range (inclusive).
   * @param end   the end of the date and time range (exclusive).
   * @return a list of Event objects occurring within the specified range.
   */
  @Override
  public List<IEvent> getEventsListInDateRange(LocalDateTime start, LocalDateTime end) {
    return calendar.getEventsListInDateRange(start, end);
  }

  /**
   * Checks if the calendar has any events scheduled at a specific date and time.
   *
   * @param dateTime the date and time to check.
   * @return true if the calendar is busy at the given dateTime, false otherwise.
   */
  @Override
  public boolean isBusyAt(LocalDateTime dateTime) {
    return calendar.isBusyAt(dateTime);
  }

  /**
   * Edits a property of a specific event identified by its ID.
   *
   * @param eventID     the unique ID of the event to edit.
   * @param property    the property of the event to modify (e.g., subject, time, location).
   * @param newProperty the new value for the specified property.
   * @return the modified Event object.
   * @throws CalendarException if the event is not found or the property/new value is invalid.
   */
  @Override
  public Event editEvent(UUID eventID, Property property, String newProperty)
          throws CalendarException {
    return calendar.editEvent(eventID, property, newProperty);
  }

  /**
   * Edits a series of events starting from a specific date.
   *
   * @param seriesID    the unique ID of the event series.
   * @param property    the property of the events in the series to modify.
   * @param newProperty the new value for the specified property.
   * @throws CalendarException if the series is not found or the property/new value is invalid.
   */
  @Override
  public void editSeriesFromDate(UUID seriesID, Property property, String newProperty)
          throws CalendarException {
    calendar.editSeriesFromDate(seriesID, property, newProperty);
  }

  /**
   * Edits an entire series of events.
   *
   * @param seriesID    the unique ID of the event series.
   * @param property    the property of the events in the series to modify.
   * @param newProperty the new value for the specified property.
   * @throws CalendarException if the series is not found or the property/new value is invalid.
   */
  @Override
  public void editSeries(UUID seriesID, Property property, String newProperty)
          throws CalendarException {
    calendar.editSeries(seriesID, property, newProperty);
  }

  /**
   * Gets the underlying VirtualCalendar for direct access if needed.
   *
   * @return the underlying VirtualCalendar
   */
  public VirtualCalendar getVirtualCalendar() {
    return calendar;
  }

  /**
   * Returns a string representation of this NamedCalendar, including its name and timezone.
   *
   * @return a string in the format "Calendar: [name] ([timezone])".
   */
  @Override
  public String toString() {
    return "Calendar: " + name + " (" + timezone + ")";
  }

  /**
   * Retrieves an event series by its unique identifier.
   *
   * @param seriesID the unique identifier of the event series to retrieve
   * @return the EventSeries object if found, null if no series exists with the given ID
   */
  @Override
  public EventSeries getEventSeriesByID(UUID seriesID) {
    return calendar.getEventSeriesByID(seriesID);
  }
}