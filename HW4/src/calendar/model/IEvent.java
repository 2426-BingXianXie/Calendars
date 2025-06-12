package calendar.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Interface representing an event in a calendar system.
 * This interface defines the contract for all event implementations,
 * providing abstraction and flexibility for different event types.
 */
public interface IEvent {

  /**
   * Returns the unique identifier for this event.
   *
   * @return the UUID of the event
   */
  UUID getId();

  /**
   * Returns the subject of this event.
   *
   * @return the subject of the event
   */
  String getSubject();

  /**
   * Sets the subject of this event.
   *
   * @param subject the new subject for the event
   */
  void setSubject(String subject);

  /**
   * Returns the start time of this event.
   *
   * @return the start time as a LocalDateTime
   */
  LocalDateTime getStart();

  /**
   * Sets the start time of this event.
   *
   * @param start the new start time for the event
   * @throws IllegalArgumentException if start is after the current end time
   */
  void setStart(LocalDateTime start);

  /**
   * Returns the end time of this event.
   *
   * @return the end time as a LocalDateTime
   */
  LocalDateTime getEnd();

  /**
   * Sets the end time of this event.
   *
   * @param end the new end time for the event
   * @throws IllegalArgumentException if end is before the current start time
   */
  void setEnd(LocalDateTime end);

  /**
   * Returns the description of this event.
   *
   * @return the description as a String, or null if not set
   */
  String getDescription();

  /**
   * Sets the description of this event.
   *
   * @param description the new description for the event
   */
  void setDescription(String description);

  /**
   * Returns the location of this event.
   *
   * @return the location as a Location enum, or null if not set
   */
  Location getLocation();

  /**
   * Sets the location of this event.
   *
   * @param location the new location for the event
   */
  void setLocation(Location location);

  /**
   * Returns the location detail of this event.
   *
   * @return the location detail as a String, or null if not set
   */
  String getLocationDetail();

  /**
   * Sets the location detail of this event.
   *
   * @param locationDetail the new location detail for the event
   */
  void setLocationDetail(String locationDetail);

  /**
   * Returns the status of this event.
   *
   * @return the status as an EventStatus enum, or null if not set
   */
  EventStatus getStatus();

  /**
   * Sets the status of this event.
   *
   * @param status the new status for the event
   */
  void setStatus(EventStatus status);

  /**
   * Returns the series ID associated with this event, if any.
   *
   * @return the UUID of the series, or null if not part of a series
   */
  UUID getSeriesID();

  /**
   * Sets the series ID for this event.
   *
   * @param seriesID the UUID of the series to associate with this event
   */
  void setSeriesId(UUID seriesID);

  /**
   * Returns a formatted string representation of the event's location.
   *
   * @return a formatted string representing the event's location
   */
  String getLocationDisplay();

  /**
   * Creates a copy of this event with new start and end times.
   * Useful for copying events to different time slots or calendars.
   *
   * @param newStart the new start time
   * @param newEnd   the new end time
   * @return a new IEvent with updated times
   */
  IEvent copyWithNewTimes(LocalDateTime newStart, LocalDateTime newEnd);

  /**
   * Checks if this event conflicts with another event based on time overlap.
   *
   * @param other the other event to check against
   * @return true if the events have overlapping time periods
   */
  default boolean conflictsWith(IEvent other) {
    if (other == null || this.getStart() == null || this.getEnd() == null ||
            other.getStart() == null || other.getEnd() == null) {
      return false;
    }

    // Events conflict if they overlap in time
    return this.getStart().isBefore(other.getEnd()) &&
            this.getEnd().isAfter(other.getStart());
  }

  /**
   * Checks if this event occurs at the specified date and time.
   *
   * @param dateTime the date and time to check
   * @return true if the event is active at the given time
   */
  default boolean isActiveAt(LocalDateTime dateTime) {
    if (dateTime == null || this.getStart() == null || this.getEnd() == null) {
      return false;
    }

    // Handle zero-duration events
    if (this.getStart().equals(this.getEnd())) {
      return dateTime.equals(this.getStart());
    }

    // Check if dateTime falls within [start, end) interval
    return !dateTime.isBefore(this.getStart()) && dateTime.isBefore(this.getEnd());
  }
}