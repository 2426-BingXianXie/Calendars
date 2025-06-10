package calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import calendar.CalendarException;

/**
 * Interface for managing multiple calendars in the calendar system.
 * This interface provides operations for creating, editing, and managing multiple calendars,
 * as well as copying events between calendars with timezone support.
 */
public interface ICalendarSystem {

  /**
   * Creates a new calendar with the specified name and timezone.
   *
   * @param name     the unique name of the calendar
   * @param timezone the timezone for the calendar
   * @throws CalendarException if a calendar with the same name already exists
   *                          or if the timezone is invalid
   */
  void createCalendar(String name, ZoneId timezone) throws CalendarException;

  /**
   * Edits a property of an existing calendar.
   *
   * @param name        the name of the calendar to edit
   * @param property    the property to edit (name or timezone)
   * @param newValue    the new value for the property
   * @throws CalendarException if the calendar doesn't exist, property is invalid,
   *                          or new value would cause conflicts
   */
  void editCalendar(String name, CalendarProperty property, String newValue)
          throws CalendarException;

  /**
   * Sets the current calendar context for event operations.
   *
   * @param name the name of the calendar to use
   * @throws CalendarException if the calendar doesn't exist
   */
  void useCalendar(String name) throws CalendarException;

  /**
   * Gets the currently active calendar.
   *
   * @return the current calendar, or null if no calendar is set
   */
  ICalendar getCurrentCalendar();

  /**
   * Gets the name of the currently active calendar.
   *
   * @return the current calendar name, or null if no calendar is set
   */
  String getCurrentCalendarName();

  /**
   * Gets a calendar by name.
   *
   * @param name the name of the calendar
   * @return the calendar with the specified name
   * @throws CalendarException if the calendar doesn't exist
   */
  ICalendar getCalendar(String name) throws CalendarException;

  /**
   * Gets the timezone of a calendar.
   *
   * @param name the name of the calendar
   * @return the timezone of the calendar
   * @throws CalendarException if the calendar doesn't exist
   */
  ZoneId getCalendarTimezone(String name) throws CalendarException;

  /**
   * Gets a list of all calendar names.
   *
   * @return a list of all calendar names
   */
  List<String> getCalendarNames();

  /**
   * Copies a single event to another calendar.
   *
   * @param eventName        the name of the event to copy
   * @param sourceTime       the start time of the source event
   * @param targetCalendar   the name of the target calendar
   * @param targetTime       the start time in the target calendar
   * @throws CalendarException if calendars don't exist, event not found, or conflicts occur
   */
  void copyEvent(String eventName, LocalDateTime sourceTime, String targetCalendar,
                 LocalDateTime targetTime) throws CalendarException;

  /**
   * Copies all events on a specific date to another calendar.
   *
   * @param sourceDate     the date of events to copy
   * @param targetCalendar the name of the target calendar
   * @param targetDate     the target date in the target calendar
   * @throws CalendarException if calendars don't exist or conflicts occur
   */
  void copyEventsOnDate(LocalDate sourceDate, String targetCalendar, LocalDate targetDate)
          throws CalendarException;

  /**
   * Copies all events within a date range to another calendar.
   *
   * @param startDate       the start date of the range (inclusive)
   * @param endDate         the end date of the range (inclusive)
   * @param targetCalendar  the name of the target calendar
   * @param targetStartDate the start date in the target calendar
   * @throws CalendarException if calendars don't exist or conflicts occur
   */
  void copyEventsBetweenDates(LocalDate startDate, LocalDate endDate, String targetCalendar,
                              LocalDate targetStartDate) throws CalendarException;
}