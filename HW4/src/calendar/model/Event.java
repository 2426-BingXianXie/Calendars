package calendar.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Concrete implementation of IEvent representing an event in a calendar.
 * Each event has a unique ID, subject, start and end times, description, location, status,
 * and optional series ID. Updated to support timezone-aware operations and proper null handling.
 */
public class Event implements IEvent {
  private final UUID id;
  private String subject;
  private LocalDateTime startDate;
  private LocalDateTime endDate;
  private String description;
  private Location location;
  private String locationDetail;
  private EventStatus status;
  private UUID seriesID;

  /**
   * Constructs an Event with the specified details.
   *
   * @param subject        the subject of the event
   * @param startDate      the start time of the event
   * @param endDate        the end time of the event
   * @param description    an optional description of the event
   * @param location       an optional location for the event
   * @param locationDetail additional details about the location, if applicable
   * @param status         the status of the event (e.g., PUBLIC, PRIVATE)
   * @param seriesID       an optional UUID for a series this event belongs to
   */
  public Event(String subject, LocalDateTime startDate, LocalDateTime endDate, String description,
               Location location, String locationDetail, EventStatus status, UUID seriesID) {
    this.id = UUID.randomUUID();
    this.subject = subject;
    this.startDate = startDate;
    this.endDate = endDate;
    this.description = description;
    this.location = location;
    this.locationDetail = locationDetail;
    this.status = status;
    this.seriesID = seriesID;
  }

  /**
   * Constructs an Event with the specified subject, start and end times.
   * Optional fields like description, location, status, and series ID are set to null.
   *
   * @param subject   the subject of the event
   * @param startDate the start time of the event
   * @param endDate   the end time of the event
   */
  public Event(String subject, LocalDateTime startDate, LocalDateTime endDate) {
    this(subject, startDate, endDate, null, null, null, null, null);
  }

  /**
   * Constructs an Event with the specified subject and start time.
   * The end time is set to null, and other optional fields are set to null.
   *
   * @param subject   the subject of the event
   * @param startDate the start time of the event
   */
  public Event(String subject, LocalDateTime startDate) {
    this(subject, startDate, null, null, null, null, null, null);
  }

  /**
   * Copy constructor - creates a new Event with the same properties as the source event.
   * This is useful for copying events between calendars.
   *
   * @param source the event to copy
   */
  public Event(IEvent source) {
    this.id = UUID.randomUUID(); // Generate new ID for the copy
    this.subject = source.getSubject();
    this.startDate = source.getStart();
    this.endDate = source.getEnd();
    this.description = source.getDescription();
    this.location = source.getLocation();
    this.locationDetail = source.getLocationDetail();
    this.status = source.getStatus();
    this.seriesID = source.getSeriesID(); // Keep series ID for copied events
  }

  // Implementation of IEvent interface methods

  @Override
  public UUID getId() {
    return id;
  }

  @Override
  public String getSubject() {
    return subject;
  }

  @Override
  public void setSubject(String subject) {
    this.subject = subject;
  }

  @Override
  public LocalDateTime getStart() {
    return startDate;
  }

  @Override
  public void setStart(LocalDateTime start) {
    this.startDate = start;
  }

  @Override
  public LocalDateTime getEnd() {
    return endDate;
  }

  @Override
  public void setEnd(LocalDateTime end) {
    if (end != null && this.startDate != null && end.isBefore(this.startDate)) {
      throw new IllegalArgumentException("End cannot be before start");
    }
    this.endDate = end;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public Location getLocation() {
    return location;
  }

  @Override
  public void setLocation(Location location) {
    this.location = location;
  }

  @Override
  public String getLocationDetail() {
    return locationDetail;
  }

  @Override
  public void setLocationDetail(String locationDetail) {
    this.locationDetail = locationDetail;
  }

  @Override
  public EventStatus getStatus() {
    return status;
  }

  @Override
  public void setStatus(EventStatus status) {
    this.status = status;
  }

  @Override
  public UUID getSeriesID() {
    return seriesID;
  }

  @Override
  public void setSeriesId(UUID seriesID) {
    this.seriesID = seriesID;
  }

  @Override
  public String getLocationDisplay() {
    if (location == null) {
      return "";
    }
    return location.name() + (locationDetail != null && !locationDetail.isEmpty()
            ? ": " + locationDetail : "");
  }

  @Override
  public IEvent copyWithNewTimes(LocalDateTime newStart, LocalDateTime newEnd) {
    return new Event(this.subject, newStart, newEnd, this.description,
            this.location, this.locationDetail, this.status,
            null); // Reset series ID for copies to different times
  }

  // Object methods

  /**
   * Checks if two events are equal.
   * Two events are considered equal if they have the same subject, start time, and end time.
   * This method properly handles null values for all three comparison fields.
   *
   * @param o the object to compare with this event
   * @return true if the object is an IEvent with the same subject, start, and end times;
   *         false otherwise
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IEvent)) {
      return false;
    }
    IEvent event = (IEvent) o;
    return Objects.equals(subject, event.getSubject()) &&
            Objects.equals(startDate, event.getStart()) &&
            Objects.equals(endDate, event.getEnd());
  }

  /**
   * Returns a hash code for this event.
   * The hash code is based on the subject, start, and end times.
   * This method properly handles null values.
   *
   * @return the hash code as an int
   */
  @Override
  public int hashCode() {
    return Objects.hash(subject, startDate, endDate);
  }

  /**
   * Returns a string representation of the event.
   * The format includes the subject, start and end times, and location if available.
   *
   * @return a formatted string representing the event
   */
  @Override
  public String toString() {
    return String.format("%s (%s to %s)%s",
            subject,
            startDate,
            endDate,
            location != null ? " @ " + getLocationDisplay() : "");
  }
}