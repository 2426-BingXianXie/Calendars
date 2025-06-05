package calendar.model;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import calendar.CalendarException;

/**
 * Represents a series of recurring events in a calendar.
 * This class allows you to define a series of events that occur on specific days of the week,
 * starting from a given date and time, and lasting for a specified duration.
 */
public class EventSeries {
  private String subject;
  private LocalTime startTime;
  private final LocalDate seriesStartDate;
  private final LocalDate seriesEndDate;
  private final int numOccurrences;
  private UUID id;
  private Set<DayOfWeek> daysOfRecurrence = new HashSet<>();
  private Duration duration;

  /**
   * Constructs an EventSeries with the specified parameters.
   * The series must start and end on the same day, and at least one day of
   * recurrence must be specified.
   * The duration must not cross into the next day.
   *
   * @param subject          the subject of the event series
   * @param startDateTime    the start date and time of the first event in the series
   * @param endDateTime      the end date and time of the first event in the series
   * @param daysOfRecurrence a set of days of the week on which this series recurs
   * @param occurrenceCount  the number of occurrences for this series (0 means no limit)
   * @param endDate          an optional end date for the series (can be null)
   * @throws CalendarException if any validation fails
   */
  public EventSeries(String subject,
                     LocalDateTime startDateTime,
                     LocalDateTime endDateTime,
                     Set<DayOfWeek> daysOfRecurrence,
                     Integer occurrenceCount,
                     LocalDate endDate) throws CalendarException {

    // Validate that the start and end date of the event series are on the same day.
    if (!startDateTime.toLocalDate().equals(endDateTime.toLocalDate())) {
      throw new CalendarException("Series events must start and end on the same day");
    }

    // Validate that at least one day of recurrence has been specified.
    if (daysOfRecurrence == null || daysOfRecurrence.isEmpty()) {
      throw new CalendarException("At least one recurrence day required");
    }

    this.subject = subject;
    // Extract the time component from startDateTime to set the series' start time.
    this.startTime = startDateTime.toLocalTime();
    // Extract the date component from startDateTime to set the series' start date.
    this.seriesStartDate = startDateTime.toLocalDate();
    // Calculate the duration of each event in the series.
    this.duration = Duration.between(startDateTime, endDateTime);
    // Generate a new unique identifier for this event series.
    this.id = UUID.randomUUID();
    // Create a new HashSet to store the days of recurrence, ensuring mutability and independence.
    this.daysOfRecurrence = new HashSet<>(daysOfRecurrence);
    // Set the number of occurrences; if occurrenceCount is null, default to 0 (no fixed limit).
    this.numOccurrences = occurrenceCount != null ? occurrenceCount : 0;
    // Set the end date for the series; can be null if count-based.
    this.seriesEndDate = endDate;

    // Validate that either an occurrence count or an end date is provided for series termination.
    if (this.numOccurrences <= 0 && this.seriesEndDate == null) {
      throw new CalendarException("Must specify either occurrence count or end date");
    }

    // Validate that the duration of an individual event does not cross into the next day.
    validateDuration(startDateTime);
  }

  /**
   * Validates that the duration of an event does not cause it to cross a day boundary.
   *
   * @param referenceStart The start {@link LocalDateTime} to use for calculating the end time.
   * @throws CalendarException if the duration would cause the event to cross a day boundary.
   */
  private void validateDuration(LocalDateTime referenceStart) throws CalendarException {
    // Calculate the potential end time of an event by adding the duration to the reference start.
    LocalDateTime testEnd = referenceStart.plus(duration);
    // Check if the date component of the calculated end time is different
    // from the reference start date.
    if (!referenceStart.toLocalDate().equals(testEnd.toLocalDate())) {
      throw new CalendarException("Duration would cross day boundary");
    }
  }

  /**
   * Generates a set of events based on the series configuration.
   * The events will occur on the specified days of the week, starting from the series start date,
   * and will not exceed the specified number of occurrences or end date.
   *
   * @return a set of generated Event objects
   */
  public Set<Event> generateEvents() throws CalendarException {
    Set<Event> events = new HashSet<>();
    // Initialize the current date to the series start date.
    LocalDate current = seriesStartDate;
    // Initialize a counter for the number of generated events.
    int count = 0;

    // Loop continues as long as the series limit (either end date or occurrence count)
    // is not exceeded.
    while (!exceedsLimit(current, count)) {
      // Check if the current day of the week is one of the specified recurrence days.
      if (daysOfRecurrence.contains(current.getDayOfWeek())) {
        // Create the full start DateTime for the current event using the current date
        // and series' start time.
        LocalDateTime eventStart = LocalDateTime.of(current, startTime);
        // Calculate the end DateTime for the current event by adding the series' duration
        // to its start time.
        LocalDateTime eventEnd = eventStart.plus(duration);

        // Re-validate that the generated event does not span multiple days.
        if (!eventStart.toLocalDate().equals(eventEnd.toLocalDate())) {
          throw new CalendarException(
                  "Generated event spans multiple days: " + eventStart + " to " + eventEnd);
        }

        // Create a new Event object with the determined subject, start, and end times.
        Event event = new Event(subject, eventStart, eventEnd);
        // Set the series ID for the newly created event, linking it to this series.
        event.setSeriesId(id);
        // Add the generated event to the set of events.
        events.add(event);
        // Increment the count of generated events.
        count++;
      }
      // Move to the next day to continue checking for recurrence.
      current = current.plusDays(1);
    }
    return events;
  }

  /**
   * Checks if the series generation limit has been exceeded.
   *
   * @param date  The current date being considered.
   * @param count The current number of events generated.
   * @return True if the limit is exceeded, false otherwise.
   */
  private boolean exceedsLimit(LocalDate date, int count) {
    // If a series end date is defined, check if the current date is after it.
    if (seriesEndDate != null) return date.isAfter(seriesEndDate);
    // If a number of occurrences is defined (greater than 0), check if the current count
    // meets or exceeds it.
    if (numOccurrences > 0) return count >= numOccurrences;
    // If neither an end date nor a positive occurrence count is defined, the limit is 
    // not exceeded (should be caught by constructor).
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
   * @throws CalendarException if the duration crosses into the next day
   */
  public void setDuration(Duration duration) throws CalendarException {
    // Create a temporary LocalDateTime using the current start time and an arbitrary current 
    // date for validation.
    LocalDateTime testStart = LocalDateTime.now().with(startTime);
    // Calculate the potential end time by adding the new duration to the test start time.
    LocalDateTime testEnd = testStart.plus(duration);

    // Check if the new duration causes the event to cross a day boundary.
    if (!testStart.toLocalDate().equals(testEnd.toLocalDate())) {
      throw new CalendarException("Duration would cross day boundary");
    }
    // If valid, set the new duration for the event series.
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

  /**
   * Returns the start date of the series.
   *
   * @return the start date as a {@link LocalDate}
   */
  public LocalDate getSeriesStartDate() {
    return this.seriesStartDate;
  }

  /**
   * Returns the end date of the series.
   *
   * @return the end date as a {@link LocalDate}
   */
  public LocalDate getSeriesEndDate() {
    return this.seriesEndDate;
  }

  /**
   * Returns the number of occurrences for this event series.
   * If the value is 0, the series is considered to have no fixed occurrence limit
   * and may be limited by the end date instead.
   *
   * @return the number of occurrences for the event series
   */
  public int getNumOccurrences() {
    return this.numOccurrences;
  }
}