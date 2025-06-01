package calendar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents an event in a calendar.
 * Each event has a unique ID, subject, start and end times, description, location, status, and optional series ID.
 */
public class Event {
  private final UUID id;
  private String subject;
  private LocalDateTime startDate;
  private LocalDateTime endDate;
  private String description;
  private Location location;
  private String locationDetail;
  private EventStatus status;
  private UUID seriesID;

  public Event(String subject, LocalDateTime startDate, LocalDateTime endDate, String description,
               Location location, String locationDetail, EventStatus status, UUID seriesID) {
    this.id = UUID.randomUUID();
    this.subject = subject;
    this.startDate = startDate;
    this.endDate = endDate;
    this.description = description;
    this.location = location;
    this.status = status;
    this.seriesID = seriesID;
  }

  // constructor without description, location, status and series id
  public Event(String subject, LocalDateTime startDate, LocalDateTime endDate) {
    this(subject, startDate, endDate, null, null, null, null);
  }

  // constructor without all optional details
  public Event(String subject, LocalDateTime startDate) {
    this(subject, startDate, null, null, null, null, null);
  }

  /**
   * Returns the unique identifier for this event.
   * @return the UUID of the event
   */
  public UUID getId() {
    return id;
  }

  /**
   * Returns the subject of this event.
   * @return the subject of the event
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Sets the subject of this event.
   * @param subject the new subject for the event
   */
  public void setSubject(String subject) {
    this.subject = subject;
  }

  /**
   * Returns the start time of this event.
   * @return the start time as a LocalDateTime
   */
  public LocalDateTime getStart() {
    return startDate;
  }

  /**
   * Sets the start time of this event.
   * @param start the new start time for the event
   */
  public void setStart(LocalDateTime start) {
    this.startDate = start;
  }

  /**
   * Returns the end time of this event.
   * @return the end time as a LocalDateTime
   */
  public LocalDateTime getEnd() {
    return endDate;
  }

  /**
   * Sets the end time of this event.
   * @param end the new end time for the event
   */
  public void setEnd(LocalDateTime end) {
    this.endDate = end;
  }

  /**
   * Returns the description of this event.
   * @return the description as a String
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets the description of this event.
   * @param description the new description for the event
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Returns the location of this event.
   * @return the location as a String
   */
  public Location getLocation() {
    return location;
  }

  /**
   * Sets the location of this event.
   * @param location the new location for the event
   */
  public void setLocation(Location location) {
    this.location = location;
  }

  /**
   * Returns the status of this event.
   * @return the status as an EventStatus enum
   */
  public EventStatus getStatus() {
    return status;
  }

  /**
   * Sets the status of this event.
   * @param status the new status for the event
   */
  public void setStatus(EventStatus status) {
    this.status = status;
  }

  /**
   * Returns the series ID associated with this event, if any.
   * @return the UUID of the series, or null if not part of a series
   */
  public UUID getSeriesID() {
    return seriesID;
  }

  /**
   * Sets the series ID for this event.
   * @param seriesID the UUID of the series to associate with this event
   */
  public void setSeriesId(UUID seriesID) {
    this.seriesID = seriesID;
  }

  /**
   * @param o the object to compare with this event
   * @return true if the object is an Event with the same subject, start, and end times; false otherwise
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Event event = (Event) o;
    return subject.equals(event.subject) &&
            startDate.equals(event.startDate) &&
            endDate.equals(event.endDate);
  }

  /**
   * Returns a hash code for this event.
   * The hash code is based on the subject, start, and end times.
   * @return the hash code as an int
   */
  @Override
  public int hashCode() {
    return Objects.hash(subject, startDate, endDate);
  }

  /**
   * Returns a string representation of the event's location.
   * If the location is CUSTOM, it returns the custom location detail.
   * Otherwise, it returns the name of the location along with any additional details.
   * @return a formatted string representing the event's location
   */
  public String getLocationDisplay() {
    if (location == null) {
      return "";
    }
    return location == Location.CUSTOM
            ? locationDetail
            : location.name() + (locationDetail != null ? ": " + locationDetail : "");
  }

  /**
   * Returns a string representation of the event.
   * The format includes the subject, start and end times, and location if available.
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
