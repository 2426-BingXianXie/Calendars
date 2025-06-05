package calendar.model;

import org.junit.Before;
import org.junit.Test;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import calendar.CalendarException;

import static org.junit.Assert.*;

/**
 * JUnit 4 test class for the {@link EventSeries} model.
 * Covers constructors, getters, setters, and event generation logic.
 */
public class EventSeriesTest {

  private String subject;
  private LocalDateTime startDateTime;
  private LocalDateTime endDateTime;
  private Set<DayOfWeek> daysOfRecurrence;
  private Integer occurrenceCount;
  private LocalDate endDate;

  /**
   * Sets up the test environment with default values for the {@link EventSeries}.
   * This method is called before each test to ensure a clean state.
   */
  @Before
  public void setUp() {
    subject = "Weekly Meeting";
    startDateTime = LocalDateTime.of(2025, 6, 2, 9, 0);
    endDateTime = LocalDateTime.of(2025, 6, 2, 10, 0);
    daysOfRecurrence = EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY);
    occurrenceCount = null;
    endDate = null;
  }


  /**
   * Tests the constructor with valid inputs for a count-based event series.
   * Asserts that all fields are correctly initialized.
   *
   * @throws CalendarException if there's an issue with calendar operations.
   */
  @Test
  public void testConstructorValidInputsCountBased() throws CalendarException {
    occurrenceCount = 5;
    EventSeries series = new EventSeries(subject, startDateTime, endDateTime,
            daysOfRecurrence, occurrenceCount, null);

    assertEquals(subject, series.getSubject());
    assertEquals(startDateTime.toLocalTime(), series.getStartTime());
    assertEquals(startDateTime.toLocalDate(), series.getSeriesStartDate());
    assertEquals(Duration.between(startDateTime, endDateTime), series.getDuration());
    assertEquals(daysOfRecurrence, series.getDaysOfRecurrence());
    assertNotNull(series.getId());
    assertEquals(5, series.getNumOccurrences());
    assertNull(series.getSeriesEndDate());
  }

  /**
   * Tests the constructor with valid inputs for a date-based event series.
   * Asserts that all fields are correctly initialized.
   *
   * @throws CalendarException if there's an issue with calendar operations.
   */
  @Test
  public void testConstructorValidInputsDateBased() throws CalendarException {

    endDate = LocalDate.of(2025, 6, 16);
    EventSeries series = new EventSeries(subject, startDateTime, endDateTime,
            daysOfRecurrence, null, endDate);

    assertEquals(subject, series.getSubject());
    assertEquals(startDateTime.toLocalTime(), series.getStartTime());
    assertEquals(startDateTime.toLocalDate(), series.getSeriesStartDate());
    assertEquals(Duration.between(startDateTime, endDateTime), series.getDuration());
    assertEquals(daysOfRecurrence, series.getDaysOfRecurrence());
    assertNotNull(series.getId());
    assertEquals(0, series.getNumOccurrences());
    assertEquals(endDate, series.getSeriesEndDate());
  }

  /**
   * Tests the constructor with an invalid scenario where the event's start date/time
   * is after its end date/time,
   * leading to a duration error.
   * Expects a {@link CalendarException}.
   *
   * @throws CalendarException expected exception if the event duration is invalid.
   */
  @Test(expected = CalendarException.class)
  public void testConstructorStartDateAfterEndDateDurationError() throws CalendarException {

    LocalDateTime seriesStart = LocalDateTime.of(2025, 6, 2,
            23, 0);
    LocalDateTime eventEndNextDay = LocalDateTime.of(2025, 6, 3,
            0, 30);
    new EventSeries(subject, seriesStart, eventEndNextDay, daysOfRecurrence, 1,
            null);
  }

  /**
   * Tests the constructor with an invalid scenario where a single event within the series
   * spans multiple days.
   * Expects a {@link CalendarException}.
   *
   * @throws CalendarException expected exception if a single event crosses a day boundary.
   */
  @Test(expected = CalendarException.class)
  public void testConstructorEventSpansMultipleDays() throws CalendarException {

    new EventSeries(subject,
            LocalDateTime.of(2025, 6, 2, 9, 0),
            LocalDateTime.of(2025, 6, 3, 9, 0),
            daysOfRecurrence, 1, null);
  }

  /**
   * Tests the constructor with a null set for days of recurrence.
   * Expects a {@link CalendarException}.
   *
   * @throws CalendarException expected exception if days of recurrence is null.
   */
  @Test(expected = CalendarException.class)
  public void testConstructorNullDaysOfRecurrence() throws CalendarException {
    new EventSeries(subject, startDateTime, endDateTime, null, 1,
            null);
  }

  /**
   * Tests the constructor with an empty set for days of recurrence.
   * Expects a {@link CalendarException}.
   *
   * @throws CalendarException expected exception if days of recurrence is empty.
   */
  @Test(expected = CalendarException.class)
  public void testConstructorEmptyDaysOfRecurrence() throws CalendarException {
    new EventSeries(subject, startDateTime, endDateTime, EnumSet.noneOf(DayOfWeek.class),
            1, null);
  }

  /**
   * Tests the constructor with no termination condition.
   * Expects a {@link CalendarException}.
   *
   * @throws CalendarException expected exception if no termination condition is provided.
   */
  @Test(expected = CalendarException.class)
  public void testConstructorNoTerminationCondition() throws CalendarException {
    new EventSeries(subject, startDateTime, endDateTime, daysOfRecurrence, null,
            null);
  }

  /**
   * Tests the constructor with zero occurrences and no end date, which is an invalid state.
   * Expects a {@link CalendarException}.
   *
   * @throws CalendarException expected exception if occurrence count is zero and no end date.
   */
  @Test(expected = CalendarException.class)
  public void testConstructorZeroOccurrencesAndNoEndDate() throws CalendarException {
    new EventSeries(subject, startDateTime, endDateTime, daysOfRecurrence, 0,
            null);
  }


  /**
   * Tests the {@code getId} method.
   * Asserts that a non-null UUID is returned.
   *
   * @throws CalendarException if there's an issue with calendar operations.
   */
  @Test
  public void testGetId() throws CalendarException {
    EventSeries series = new EventSeries(subject, startDateTime, endDateTime, daysOfRecurrence,
            1, null);
    assertNotNull(series.getId());
  }

  /**
   * Tests the {@code getSubject} method.
   * Asserts that the correct subject is returned.
   *
   * @throws CalendarException if there's an issue with calendar operations.
   */
  @Test
  public void testGetSubject() throws CalendarException {
    EventSeries series = new EventSeries(subject, startDateTime, endDateTime, daysOfRecurrence,
            1, null);
    assertEquals(subject, series.getSubject());
  }

  /**
   * Tests the {@code getStartTime} method.
   * Asserts that the correct start time is returned.
   *
   * @throws CalendarException if there's an issue with calendar operations.
   */
  @Test
  public void testGetStartTime() throws CalendarException {
    EventSeries series = new EventSeries(subject, startDateTime, endDateTime, daysOfRecurrence,
            1, null);
    assertEquals(startDateTime.toLocalTime(), series.getStartTime());
  }

  /**
   * Tests the {@code getDuration} method.
   * Asserts that the correct duration is returned.
   *
   * @throws CalendarException if there's an issue with calendar operations.
   */
  @Test
  public void testGetDuration() throws CalendarException {
    EventSeries series = new EventSeries(subject, startDateTime, endDateTime, daysOfRecurrence,
            1, null);
    assertEquals(Duration.between(startDateTime, endDateTime), series.getDuration());
  }

  /**
   * Tests the {@code getDaysOfRecurrence} method.
   * Asserts that the correct set of recurrence days is returned.
   *
   * @throws CalendarException if there's an issue with calendar operations.
   */
  @Test
  public void testGetDaysOfRecurrence() throws CalendarException {
    EventSeries series = new EventSeries(subject, startDateTime, endDateTime, daysOfRecurrence,
            1, null);
    assertEquals(daysOfRecurrence, series.getDaysOfRecurrence());
  }


  /**
   * Tests the {@code setId} method.
   * Asserts that the ID can be successfully updated.
   *
   * @throws CalendarException if there's an issue with calendar operations.
   */
  @Test
  public void testSetId() throws CalendarException {
    EventSeries series = new EventSeries(subject, startDateTime, endDateTime, daysOfRecurrence,
            1, null);
    UUID newId = UUID.randomUUID();
    series.setId(newId);
    assertEquals(newId, series.getId());
  }

  /**
   * Tests the {@code setSubject} method.
   * Asserts that the subject can be successfully updated.
   *
   * @throws CalendarException if there's an issue with calendar operations.
   */
  @Test
  public void testSetSubject() throws CalendarException {
    EventSeries series = new EventSeries(subject, startDateTime, endDateTime, daysOfRecurrence,
            1, null);
    String newSubject = "Daily Standup";
    series.setSubject(newSubject);
    assertEquals(newSubject, series.getSubject());
  }

  /**
   * Tests the {@code setStartTime} method.
   * Asserts that the start time can be successfully updated.
   *
   * @throws CalendarException if there's an issue with calendar operations.
   */
  @Test
  public void testSetStartTime() throws CalendarException {
    EventSeries series = new EventSeries(subject, startDateTime, endDateTime, daysOfRecurrence,
            1, null);
    LocalTime newStartTime = LocalTime.of(8, 30);
    series.setStartTime(newStartTime);
    assertEquals(newStartTime, series.getStartTime());
  }

  /**
   * Tests the {@code setDuration} method with a valid duration.
   * Asserts that the duration can be successfully updated.
   *
   * @throws CalendarException if there's an issue with calendar operations.
   */
  @Test
  public void testSetDurationValid() throws CalendarException {
    EventSeries series = new EventSeries(subject, startDateTime, endDateTime, daysOfRecurrence,
            1, null);
    Duration newDuration = Duration.ofMinutes(45);
    series.setDuration(newDuration);
    assertEquals(newDuration, series.getDuration());
  }

  /**
   * Tests the {@code setDuration} method with a duration that would cause the event to cross
   * a day boundary.
   * Expects a {@link CalendarException}.
   *
   * @throws CalendarException expected exception if the duration causes the event to cross
   *         a day boundary.
   */
  @Test(expected = CalendarException.class)
  public void testSetDurationCrossesDayBoundary() throws CalendarException {
    EventSeries series = new EventSeries(subject, startDateTime, endDateTime, daysOfRecurrence,
            1, null);
    LocalTime seriesStartTime = LocalTime.of(23, 0);
    series.setStartTime(seriesStartTime);
    Duration durationCrossingDay = Duration.ofHours(2);
    series.setDuration(durationCrossingDay);
  }

  /**
   * Tests the {@code setDaysOfRecurrence} method.
   * Asserts that the days of recurrence can be successfully updated.
   *
   * @throws CalendarException if there's an issue with calendar operations.
   */
  @Test
  public void testSetDaysOfRecurrence() throws CalendarException {
    EventSeries series = new EventSeries(subject, startDateTime, endDateTime, daysOfRecurrence,
            1, null);
    Set<DayOfWeek> newDays = EnumSet.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY);
    series.setDaysOfRecurrence(newDays);
    assertEquals(newDays, series.getDaysOfRecurrence());
  }


  /**
   * Tests the {@code generateEvents} method for a count-based event series.
   * Asserts that the correct number of events are generated with the correct details
   * and that they fall on the expected dates.
   *
   * @throws CalendarException if there's an issue during event generation.
   */
  @Test
  public void testGenerateEventsCountBased() throws CalendarException {
    occurrenceCount = 5;
    EventSeries series = new EventSeries(subject, startDateTime, endDateTime, daysOfRecurrence,
            occurrenceCount, null);
    Set<Event> generatedEvents = series.generateEvents();

    assertEquals(5, generatedEvents.size());

    Set<LocalDate> expectedDates = new HashSet<>();
    expectedDates.add(LocalDate.of(2025, 6, 2)); // Monday
    expectedDates.add(LocalDate.of(2025, 6, 4)); // Wednesday
    expectedDates.add(LocalDate.of(2025, 6, 9)); // Monday
    expectedDates.add(LocalDate.of(2025, 6, 11)); // Wednesday
    expectedDates.add(LocalDate.of(2025, 6, 16)); // Monday

    for (Event event : generatedEvents) {
      assertEquals(subject, event.getSubject());
      assertEquals(startDateTime.toLocalTime(), event.getStart().toLocalTime());
      assertEquals(endDateTime.toLocalTime(), event.getEnd().toLocalTime());
      assertTrue(expectedDates.contains(event.getStart().toLocalDate()));
      assertNotNull(event.getSeriesID());
      assertEquals(series.getId(), event.getSeriesID());
    }
  }

  /**
   * Tests the {@code generateEvents} method for a date-based event series.
   * Asserts that the correct number of events are generated and that they fall
   * on the expected dates within the specified end date.
   *
   * @throws CalendarException if there's an issue during event generation.
   */
  @Test
  public void testGenerateEventsDateBased() throws CalendarException {
    endDate = LocalDate.of(2025, 6, 10);
    EventSeries series = new EventSeries(subject, startDateTime, endDateTime, daysOfRecurrence,
            null, endDate);
    Set<Event> generatedEvents = series.generateEvents();

    assertEquals(3, generatedEvents.size());

    Set<LocalDate> expectedDates = new HashSet<>();
    expectedDates.add(LocalDate.of(2025, 6, 2)); // Monday
    expectedDates.add(LocalDate.of(2025, 6, 4)); // Wednesday
    expectedDates.add(LocalDate.of(2025, 6, 9)); // Monday

    for (Event event : generatedEvents) {
      assertTrue(expectedDates.contains(event.getStart().toLocalDate()));
    }
  }

  /**
   * Tests the {@code generateEvents} method when the initial series start date
   * does not fall on any of the specified recurrence days, but subsequent days do.
   * Asserts that the first generated event starts on the next matching day.
   *
   * @throws CalendarException if there's an issue during event generation.
   */
  @Test
  public void testGenerateEventsNoRecurrenceDaysMatch() throws CalendarException {

    Set<DayOfWeek> singleDayRecurrence = EnumSet.of(DayOfWeek.FRIDAY);
    // startDateTime is Monday, June 2nd. The next Friday is June 6th.
    EventSeries series = new EventSeries(subject, startDateTime, endDateTime, singleDayRecurrence,
            1, null);
    Set<Event> generatedEvents = series.generateEvents();
    assertEquals(1, generatedEvents.size());
    Event event = generatedEvents.iterator().next();
    assertEquals(LocalDate.of(2025, 6, 6), event.getStart().toLocalDate());
  }

  /**
   * Tests the EventSeries constructor when the number of occurrences is zero
   * and there's no end date (covered by constructor tests). This specifically tests
   * when the end date is set to a date before any events can be generated.
   * Asserts that an error is thrown.
   *
   * @throws CalendarException if there's an issue during event series creation.
   */
  @Test(expected = CalendarException.class)
  public void testGenerateEventsZeroOccurrences() throws CalendarException {
    endDate = LocalDate.of(2025, 6, 1); // Before startDateTime (June 2)
    EventSeries series = new EventSeries(subject, startDateTime, endDateTime, daysOfRecurrence,
            null, endDate);
  }

  /**
   * Tests the {@code generateEvents} method when the series end date is before the series
   * start date.
   * Asserts that an error is thrown.
   *
   * @throws CalendarException if there's an issue during event series creation.
   */
  @Test(expected = CalendarException.class)
  public void testGenerateEventsSeriesEndDateBeforeStartDate() throws CalendarException {
    endDate = LocalDate.of(2025, 5, 1); // Before startDateTime (June 2)
    EventSeries series = new EventSeries(subject, startDateTime, endDateTime, daysOfRecurrence,
            null, endDate);
  }

  /**
   * Tests the {@code generateEvents} method when there are no matching days of recurrence
   * within the specified date range.
   * Asserts that no events are generated.
   *
   * @throws CalendarException if there's an issue during event generation.
   */
  @Test
  public void testGenerateEventsNoMatchingDayInDateRange() throws CalendarException {
    startDateTime = LocalDateTime.of(2025, 6, 2, 9, 0);
    endDateTime = LocalDateTime.of(2025, 6, 2, 10, 0);
    daysOfRecurrence = EnumSet.of(DayOfWeek.SUNDAY); // Only Sunday
    endDate = LocalDate.of(2025, 6, 4);
    EventSeries series = new EventSeries(subject, startDateTime, endDateTime, daysOfRecurrence,
            null, endDate);
    Set<Event> generatedEvents = series.generateEvents();
    assertTrue(generatedEvents.isEmpty());
  }

  /**
   * Tests that a set containing multiple recurrence days (Friday and Sunday)
   * is correctly created and its size is as expected.
   */
  @Test
  public void testGenerateEventsMixedRecurrenceDays() {
    Set<DayOfWeek> mixedDays = EnumSet.of(DayOfWeek.FRIDAY, DayOfWeek.SUNDAY);
    assertEquals(mixedDays.size(), 2);
  }

  /**
   * Tests series generation with mixed weekend and weekday combinations.
   * Verifies that events are correctly generated for series that span weekends and weekdays.
   */
  @Test
  public void testGenerateEventsMixedWeekendWeekdays() throws CalendarException {
    Set<DayOfWeek> mixedDays = EnumSet.of(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY,
            DayOfWeek.SUNDAY, DayOfWeek.MONDAY);
    EventSeries series = new EventSeries(subject, startDateTime, endDateTime,
            mixedDays, 8, null);
    Set<Event> generatedEvents = series.generateEvents();

    assertEquals(8, generatedEvents.size());

    Set<DayOfWeek> actualDays = new HashSet<>();
    for (Event event : generatedEvents) {
      actualDays.add(event.getStart().getDayOfWeek());
    }

    // Should contain all specified days
    for (DayOfWeek day : mixedDays) {
      assertTrue("Should contain " + day, actualDays.contains(day));
    }
  }

  /**
   * Tests series generation with all seven days of the week.
   * Verifies that daily recurring events are generated correctly.
   */
  @Test
  public void testGenerateEventsAllSevenDays() throws CalendarException {
    Set<DayOfWeek> allDays = EnumSet.allOf(DayOfWeek.class);
    EventSeries series = new EventSeries(subject, startDateTime, endDateTime,
            allDays, 14, null);
    Set<Event> generatedEvents = series.generateEvents();

    assertEquals(14, generatedEvents.size());

    Set<DayOfWeek> actualDays = new HashSet<>();
    for (Event event : generatedEvents) {
      actualDays.add(event.getStart().getDayOfWeek());
    }

    assertEquals("Should have events on all 7 days", 7, actualDays.size());
    assertTrue("Should contain all weekdays", actualDays.containsAll(allDays));
  }

  /**
   * Tests series generation across leap year boundary.
   * Ensures that series generation handles leap year dates correctly.
   */
  @Test
  public void testGenerateEventsLeapYearHandling() throws CalendarException {
    // Create series that spans Feb 28 - Mar 1 in a leap year
    LocalDateTime leapYearStart = LocalDateTime.of(2024, 2, 28,
            9, 0); // 2024 is a leap year
    LocalDateTime leapYearEnd = LocalDateTime.of(2024, 2, 28,
            10, 0);

    Set<DayOfWeek> dailyRecurrence = EnumSet.of(DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY);
    EventSeries series = new EventSeries("Leap Year Test", leapYearStart, leapYearEnd,
            dailyRecurrence, null, LocalDate.of(2024, 3, 1));

    Set<Event> generatedEvents = series.generateEvents();

    assertTrue("Should generate at least 2 events", generatedEvents.size() >= 2);

    boolean hasLeapDay = generatedEvents.stream()
            .anyMatch(e -> e.getStart().toLocalDate().equals(LocalDate.of(2024,
                    2, 29)));

    assertTrue("Should include leap day event", hasLeapDay);
  }

  /**
   * Tests series generation with very short duration (1 minute).
   * Verifies that the series handles minimal duration events correctly.
   */
  @Test
  public void testGenerateEventsVeryShortDuration() throws CalendarException {
    LocalDateTime shortStart = LocalDateTime.of(2025, 6, 2,
            9, 0);
    LocalDateTime shortEnd = LocalDateTime.of(2025, 6, 2,
            9, 1);

    Set<DayOfWeek> daysOfRecurrence = EnumSet.of(DayOfWeek.MONDAY);
    EventSeries series = new EventSeries("Short Event", shortStart, shortEnd,
            daysOfRecurrence, 1, null);

    Set<Event> generatedEvents = series.generateEvents();
    assertEquals(1, generatedEvents.size());

    Event event = generatedEvents.iterator().next();
    assertEquals(Duration.ofMinutes(1), Duration.between(event.getStart(), event.getEnd()));
  }

  /**
   * Tests series validation with duration that would cross day boundary in different time zones.
   * Verifies that validation correctly prevents events from spanning multiple days.
   */
  @Test(expected = CalendarException.class)
  public void testConstructorDurationCrossingDayBoundaryAtMidnight() throws CalendarException {
    LocalDateTime lateStart = LocalDateTime.of(2025, 6, 2, 23,
            59);
    LocalDateTime earlyEnd = LocalDateTime.of(2025, 6, 3, 0,
            1);

    Set<DayOfWeek> daysOfRecurrence = EnumSet.of(DayOfWeek.MONDAY);
    new EventSeries("Midnight Crosser", lateStart, earlyEnd, daysOfRecurrence,
            1, null);
  }
}