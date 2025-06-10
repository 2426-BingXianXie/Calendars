package calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import calendar.CalendarException;

/**
 * Implementation of the calendar system that manages multiple calendars.
 * Supports creating, editing, and managing calendars with timezone support,
 * as well as copying events between calendars.
 */
public class CalendarSystem implements ICalendarSystem {
  private final Map<String, NamedCalendar> calendars;
  private NamedCalendar currentCalendar;

  /**
   * Constructs a new CalendarSystem with no calendars.
   */
  public CalendarSystem() {
    this.calendars = new HashMap<>();
    this.currentCalendar = null;
  }

  /**
   * Creates a new calendar with the specified name and timezone.
   *
   * @param name     the name of the calendar to create.
   * @param timezone the timezone for the new calendar.
   * @throws CalendarException if the calendar name is empty or already exists, or if the timezone is invalid.
   */
  @Override
  public void createCalendar(String name, ZoneId timezone) throws CalendarException {
    if (name == null || name.trim().isEmpty()) {
      throw new CalendarException("Calendar name cannot be empty");
    }

    String trimmedName = name.trim();
    if (calendars.containsKey(trimmedName)) {
      throw new CalendarException("Calendar with name '" + trimmedName + "' already exists");
    }

    try {
      NamedCalendar calendar = new NamedCalendar(trimmedName, timezone);
      calendars.put(trimmedName, calendar);
    } catch (Exception e) {
      throw new CalendarException("Invalid timezone: " + timezone);
    }
  }

  /**
   * Edits a property of an existing calendar.
   *
   * @param name     the name of the calendar to edit.
   * @param property the property to edit ("name" or "timezone").
   * @param newValue the new value for the property.
   * @throws CalendarException if the calendar is not found, the property is invalid,
   * the new name is empty or already exists, or the new timezone is invalid.
   */
  @Override
  public void editCalendar(String name, CalendarProperty property, String newValue)
          throws CalendarException {
    NamedCalendar calendar = calendars.get(name);
    if (calendar == null) {
      throw new CalendarException("Calendar not found: " + name);
    }

    switch (property) {
      case NAME:
        if (newValue == null || newValue.trim().isEmpty()) {
          throw new CalendarException("Calendar name cannot be empty");
        }
        String newName = newValue.trim();
        if (!newName.equals(name) && calendars.containsKey(newName)) {
          throw new CalendarException("Calendar with name '" + newName + "' already exists");
        }

        // Update the map
        calendars.remove(name);
        calendar.setName(newName);
        calendars.put(newName, calendar);

        // Update current calendar reference if needed
        if (currentCalendar == calendar) {
          currentCalendar = calendar;
        }
        break;

      case TIMEZONE:
        try {
          ZoneId newTimezone = ZoneId.of(newValue);
          calendar.setTimezone(newTimezone);
        } catch (Exception e) {
          throw new CalendarException("Invalid timezone: " + newValue);
        }
        break;

      default:
        throw new CalendarException("Invalid property: " + property +
                ". Valid properties are 'name' and 'timezone'");
    }
  }

  /**
   * Sets the specified calendar as the current active calendar.
   *
   * @param name the name of the calendar to use.
   * @throws CalendarException if the calendar with the given name is not found.
   */
  @Override
  public void useCalendar(String name) throws CalendarException {
    NamedCalendar calendar = calendars.get(name);
    if (calendar == null) {
      throw new CalendarException("Calendar not found: " + name);
    }
    this.currentCalendar = calendar;
  }

  /**
   * Returns the currently active calendar.
   *
   * @return the current calendar, or null if no calendar is in use.
   */
  @Override
  public ICalendar getCurrentCalendar() {
    return currentCalendar;
  }

  /**
   * Returns the name of the currently active calendar.
   *
   * @return the name of the current calendar, or null if no calendar is in use.
   */
  @Override
  public String getCurrentCalendarName() {
    return currentCalendar != null ? currentCalendar.getName() : null;
  }

  /**
   * Retrieves a calendar by its name.
   *
   * @param name the name of the calendar to retrieve.
   * @return the ICalendar object corresponding to the given name.
   * @throws CalendarException if the calendar with the given name is not found.
   */
  @Override
  public ICalendar getCalendar(String name) throws CalendarException {
    NamedCalendar calendar = calendars.get(name);
    if (calendar == null) {
      throw new CalendarException("Calendar not found: " + name);
    }
    return calendar;
  }

  /**
   * Retrieves the timezone of a calendar by its name.
   *
   * @param name the name of the calendar.
   * @return the ZoneId representing the timezone of the specified calendar.
   * @throws CalendarException if the calendar with the given name is not found.
   */
  @Override
  public ZoneId getCalendarTimezone(String name) throws CalendarException {
    NamedCalendar calendar = calendars.get(name);
    if (calendar == null) {
      throw new CalendarException("Calendar not found: " + name);
    }
    return calendar.getTimezone();
  }

  /**
   * Returns a list of all calendar names currently managed by the system.
   *
   * @return a list of strings, each representing the name of a calendar.
   */
  @Override
  public List<String> getCalendarNames() {
    return new ArrayList<>(calendars.keySet());
  }

  /**
   * Copies a specific event from the current calendar to a target calendar.
   * The new event in the target calendar will have its start and end times
   * adjusted based on the target time and maintaining the original event's duration.
   *
   * @param eventName        the subject/name of the event to copy.
   * @param sourceTime       the start time of the event in the current calendar.
   * @param targetCalendarName the name of the calendar to which the event will be copied.
   * @param targetTime       the desired new start time for the event in the target calendar.
   * @throws CalendarException if no calendar is in use, the target calendar is not found,
   * the event is not found in the current calendar, or multiple events
   * match the criteria.
   */
  @Override
  public void copyEvent(String eventName, LocalDateTime sourceTime, String targetCalendarName,
                        LocalDateTime targetTime) throws CalendarException {
    if (currentCalendar == null) {
      throw new CalendarException("No calendar in use. Use 'use calendar' command first.");
    }

    NamedCalendar targetCalendar = calendars.get(targetCalendarName);
    if (targetCalendar == null) {
      throw new CalendarException("Target calendar not found: " + targetCalendarName);
    }

    // Find the event to copy
    List<Event> events = currentCalendar.getEventsBySubjectAndStartTime(eventName, sourceTime);
    if (events.isEmpty()) {
      throw new CalendarException("Event not found: " + eventName + " starting at " + sourceTime);
    }
    if (events.size() > 1) {
      throw new CalendarException("Multiple events found with the same name and start time");
    }

    Event sourceEvent = events.get(0);

    // Convert target time from target calendar's timezone to source calendar's timezone for duration calculation
    ZonedDateTime sourceZoned = sourceTime.atZone(currentCalendar.getTimezone());
    ZonedDateTime targetZoned = targetTime.atZone(targetCalendar.getTimezone());

    // Calculate duration of original event
    long durationMinutes = ChronoUnit.MINUTES.between(sourceEvent.getStart(), sourceEvent.getEnd());

    // Create new event in target calendar
    LocalDateTime targetEndTime = targetTime.plusMinutes(durationMinutes);

    Event newEvent = new Event(sourceEvent.getSubject(), targetTime, targetEndTime,
            sourceEvent.getDescription(), sourceEvent.getLocation(),
            sourceEvent.getLocationDetail(), sourceEvent.getStatus(), null);

    // Add to target calendar
    targetCalendar.createEvent(newEvent.getSubject(), newEvent.getStart(), newEvent.getEnd());
  }

  /**
   * Copies all events from a specific date in the current calendar to a target date
   * in another calendar. Timezone conversions are handled.
   *
   * @param sourceDate        the date in the current calendar from which events will be copied.
   * @param targetCalendarName the name of the calendar to which events will be copied.
   * @param targetDate        the date in the target calendar where events will be placed.
   * @throws CalendarException if no calendar is in use, or the target calendar is not found.
   */
  @Override
  public void copyEventsOnDate(LocalDate sourceDate, String targetCalendarName,
                               LocalDate targetDate) throws CalendarException {
    if (currentCalendar == null) {
      throw new CalendarException("No calendar in use. Use 'use calendar' command first.");
    }

    NamedCalendar targetCalendar = calendars.get(targetCalendarName);
    if (targetCalendar == null) {
      throw new CalendarException("Target calendar not found: " + targetCalendarName);
    }

    List<Event> sourceEvents = currentCalendar.getEventsList(sourceDate);

    for (Event sourceEvent : sourceEvents) {
      // Calculate time difference for the new date
      LocalDateTime sourceStart = sourceEvent.getStart();
      LocalDateTime sourceEnd = sourceEvent.getEnd();

      // Convert times to target calendar's timezone
      ZonedDateTime sourceStartZoned = sourceStart.atZone(currentCalendar.getTimezone());
      ZonedDateTime sourceEndZoned = sourceEnd.atZone(currentCalendar.getTimezone());

      ZonedDateTime targetStartZoned = sourceStartZoned.withZoneSameInstant(targetCalendar.getTimezone());
      ZonedDateTime targetEndZoned = sourceEndZoned.withZoneSameInstant(targetCalendar.getTimezone());

      // Adjust for target date
      LocalDateTime targetStart = targetDate.atTime(targetStartZoned.toLocalTime());
      LocalDateTime targetEnd = targetDate.atTime(targetEndZoned.toLocalTime());

      // Handle day boundary crossing
      if (targetEndZoned.toLocalDate().isAfter(targetStartZoned.toLocalDate())) {
        targetEnd = targetEnd.plusDays(1);
      }

      try {
        targetCalendar.createEvent(sourceEvent.getSubject(), targetStart, targetEnd);
      } catch (CalendarException e) {
        // Continue with other events if one fails
        System.err.println("Failed to copy event '" + sourceEvent.getSubject() + "': " + e.getMessage());
      }
    }
  }

  /**
   * Copies events within a specified date range from the current calendar to a target calendar,
   * shifting them by a relative number of days.
   *
   * @param startDate         the start date of the range in the current calendar.
   * @param endDate           the end date of the range in the current calendar.
   * @param targetCalendarName the name of the calendar to which events will be copied.
   * @param targetStartDate   the desired start date in the target calendar. The difference
   * between this and {@code startDate} will be applied to all events.
   * @throws CalendarException if no calendar is in use, the target calendar is not found,
   * or the start date is after the end date.
   */
  @Override
  public void copyEventsBetweenDates(LocalDate startDate, LocalDate endDate,
                                     String targetCalendarName, LocalDate targetStartDate)
          throws CalendarException {
    if (currentCalendar == null) {
      throw new CalendarException("No calendar in use. Use 'use calendar' command first.");
    }

    NamedCalendar targetCalendar = calendars.get(targetCalendarName);
    if (targetCalendar == null) {
      throw new CalendarException("Target calendar not found: " + targetCalendarName);
    }

    if (startDate.isAfter(endDate)) {
      throw new CalendarException("Start date cannot be after end date");
    }

    // Calculate the date range difference
    long daysDifference = ChronoUnit.DAYS.between(startDate, targetStartDate);

    // Get all events in the date range
    LocalDateTime rangeStart = startDate.atStartOfDay();
    LocalDateTime rangeEnd = endDate.plusDays(1).atStartOfDay(); // Exclusive end

    List<Event> sourceEvents = currentCalendar.getEventsListInDateRange(rangeStart, rangeEnd);

    for (Event sourceEvent : sourceEvents) {
      // Calculate new dates by adding the difference
      LocalDate newStartDate = sourceEvent.getStart().toLocalDate().plusDays(daysDifference);
      LocalDate newEndDate = sourceEvent.getEnd().toLocalDate().plusDays(daysDifference);

      LocalDateTime newStart = newStartDate.atTime(sourceEvent.getStart().toLocalTime());
      LocalDateTime newEnd = newEndDate.atTime(sourceEvent.getEnd().toLocalTime());

      // Convert timezone if different calendars
      if (!currentCalendar.getTimezone().equals(targetCalendar.getTimezone())) {
        ZonedDateTime sourceStartZoned = sourceEvent.getStart().atZone(currentCalendar.getTimezone());
        ZonedDateTime sourceEndZoned = sourceEvent.getEnd().atZone(currentCalendar.getTimezone());

        ZonedDateTime targetStartZoned = sourceStartZoned.withZoneSameInstant(targetCalendar.getTimezone());
        ZonedDateTime targetEndZoned = sourceEndZoned.withZoneSameInstant(targetCalendar.getTimezone());

        newStart = newStartDate.atTime(targetStartZoned.toLocalTime());
        newEnd = newEndDate.atTime(targetEndZoned.toLocalTime());
      }

      try {
        targetCalendar.createEvent(sourceEvent.getSubject(), newStart, newEnd);
      } catch (CalendarException e) {
        // Continue with other events if one fails
        System.err.println("Failed to copy event '" + sourceEvent.getSubject() + "': " + e.getMessage());
      }
    }
  }
}