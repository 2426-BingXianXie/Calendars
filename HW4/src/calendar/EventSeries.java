package calendar;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a series of recurring events in a calendar.
 * This class allows you to define a series of events that occur on specific days of the week,
 * starting from a given date and time, and lasting for a specified duration.
 */
public class EventSeries {
  private String subject;
  private LocalTime startTime;
  private LocalDate seriesStartDate;
  private LocalDate seriesEndDate;
  private int numOccurrences;
  private UUID id;
  private Set<DayOfWeek> daysOfRecurrence = new HashSet<>();
  private Duration duration;

  public EventSeries(String subject,
                     LocalDateTime startDateTime,
                     LocalDateTime endDateTime,
                     Set<DayOfWeek> daysOfRecurrence,
                     Integer occurrenceCount,
                     LocalDate endDate) {

    // Validate single-day event constraint
    if (!startDateTime.toLocalDate().equals(endDateTime.toLocalDate())) {
      throw new IllegalArgumentException("Series events must start and end on the same day");
    }

    // Validate recurrence days
    if (daysOfRecurrence == null || daysOfRecurrence.isEmpty()) {
      throw new IllegalArgumentException("At least one recurrence day required");
    }

    this.subject = subject;
    this.startTime = startDateTime.toLocalTime();
    this.seriesStartDate = startDateTime.toLocalDate();
    this.duration = Duration.between(startDateTime, endDateTime);
    this.id = UUID.randomUUID();
    this.daysOfRecurrence = new HashSet<>(daysOfRecurrence);
    this.numOccurrences = occurrenceCount != null ? occurrenceCount : 0;
    this.seriesEndDate = endDate;

    // Validate termination conditions
    if (this.numOccurrences <= 0 && this.seriesEndDate == null) {
      throw new IllegalArgumentException("Must specify either occurrence count or end date");
    }

    // Validate duration doesn't cross days
    validateDuration(startDateTime);
  }

  private void validateDuration(LocalDateTime referenceStart) {
    LocalDateTime testEnd = referenceStart.plus(duration);
    if (!referenceStart.toLocalDate().equals(testEnd.toLocalDate())) {
      throw new IllegalArgumentException("Duration would cross day boundary");
    }
  }

  /**
   * Generates a set of events based on the series configuration.
   * The events will occur on the specified days of the week, starting from the series start date,
   * and will not exceed the specified number of occurrences or end date.
   *
   * @return a set of generated Event objects
   */
  public Set<Event> generateEvents() {
    Set<Event> events = new HashSet<>();
    LocalDate current = seriesStartDate;
    int count = 0;

    while (!exceedsLimit(current, count)) {
      if (daysOfRecurrence.contains(current.getDayOfWeek())) {
        LocalDateTime eventStart = LocalDateTime.of(current, startTime);
        LocalDateTime eventEnd = eventStart.plus(duration);

        if (!eventStart.toLocalDate().equals(eventEnd.toLocalDate())) {
          throw new IllegalStateException(
                  "Generated event spans multiple days: " + eventStart + " to " + eventEnd);
        }

        Event event = new Event(subject, eventStart, eventEnd);
        event.setSeriesId(id);
        events.add(event);
        count++;
      }
      current = current.plusDays(1);
    }
    return events;
  }

  private boolean exceedsLimit(LocalDate date, int count) {
    if (seriesEndDate != null) return date.isAfter(seriesEndDate);
    if (numOccurrences > 0) return count >= numOccurrences;
    return false;
  }

  /**
   * Returns the unique identifier for this event series.
   *
   * @return the UUID of the event series
   */
  public UUID getId() {
    return id;
  }

  /**
   * Sets the unique identifier for this event series.
   *
   * @param id the UUID to set for the event series
   */
  public void setId(UUID id) {
    this.id = id;
  }

  /**
   * Returns the subject for this event series.
   *
   * @return the subject of the event series
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Sets the subject of the event series.
   *
   * @param subject the subject to set for the event series
   */
  public void setSubject(String subject) {
    this.subject = subject;
  }

  /**
   * Returns the start time of the event series.
   *
   * @return the LocalTime representing the start time
   */
  public LocalTime getStartTime() {
    return startTime;
  }

  /**
   * Sets the start time of the event series.
   *
   * @param startTime the LocalTime to set as the start time
   */
  public void setStartTime(LocalTime startTime) {
    this.startTime = startTime;
  }

  /**
   * Returns the duration of each event in the series.
   *
   * @return the duration as a {@link Duration}
   */
  public Duration getDuration() {
    return duration;
  }

  /**
   * Sets the duration of each event in the series.
   * Ensures that the duration does not cross a day boundary.
   *
   * @param duration the {@link Duration} to set
   * @throws IllegalArgumentException if the duration crosses into the next day
   */
  public void setDuration(Duration duration) {
    LocalDateTime testStart = LocalDateTime.now().with(startTime);
    LocalDateTime testEnd = testStart.plus(duration);

    if (!testStart.toLocalDate().equals(testEnd.toLocalDate())) {
      throw new IllegalArgumentException("Duration would cross day boundary");
    }
    this.duration = duration;
  }

  /**
   * Returns the set of days of the week on which the event series recurs.
   *
   * @return a set of {@link DayOfWeek} for recurrence
   */
  public Set<DayOfWeek> getDaysOfRecurrence() {
    return daysOfRecurrence;
  }

  /**
   * Sets the days of the week on which the event series recurs.
   *
   * @param daysOfRecurrence the set of {@link DayOfWeek} to set for recurrence
   */
  public void setDaysOfRecurrence(Set<DayOfWeek> daysOfRecurrence) {
    this.daysOfRecurrence = daysOfRecurrence;
  }
}