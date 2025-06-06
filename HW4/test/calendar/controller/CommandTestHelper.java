package calendar.controller;

import calendar.CalendarException;
import calendar.model.*;
import calendar.view.CalendarView;
import calendar.view.ICalendarView;
import org.junit.Before;

import java.io.StringWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Abstract base class for calendar command tests.
 * Provides common setup, utilities, and helper methods for testing commands.
 */
public abstract class CommandTestHelper {
  protected VirtualCalendar calendar;
  protected StringWriter output;
  protected ICalendarView view;

  @Before
  public void setUp() {
    calendar = new VirtualCalendar();
    output = new StringWriter();
    view = new CalendarView(output);
  }

  /**
   * Creates an event using the calendar directly (not through commands).
   * Useful for setting up test data.
   */
  protected Event createTestEvent(String subject, LocalDateTime start, LocalDateTime end)
          throws CalendarException {
    return calendar.createEvent(subject, start, end);
  }

  /**
   * Creates a test event series using the calendar directly.
   */
  protected void createTestSeries(String subject, LocalTime startTime, LocalTime endTime,
                                  Set<Days> days, LocalDate startDate, LocalDate endDate,
                                  int repeats) throws CalendarException {
    calendar.createEventSeries(subject, startTime, endTime, days, startDate, endDate,
            repeats, null, null, null);
  }

  /**
   * Gets the current output as a string.
   */
  protected String getOutput() {
    return output.toString();
  }

  /**
   * Clears the output buffer.
   */
  protected void clearOutput() {
    output.getBuffer().setLength(0);
  }

  /**
   * Asserts that the output contains the expected string.
   */

  protected void assertOutputContains(String expected) {
    assertTrue("Output should contain: " + expected, getOutput().contains(expected));
  }

  /**
   * Asserts that the output contains all the expected strings.
   */
  protected void assertOutputContainsAll(String... expected) {
    String outputStr = getOutput();
    for (String exp : expected) {
      assertTrue("Output should contain: " + exp, outputStr.contains(exp));
    }
  }

  /**
   * Gets events for a specific date and asserts the count.
   */
  protected List<Event> getEventsAndAssertCount(LocalDate date, int expectedCount) {
    List<Event> events = calendar.getEventsList(date);
    assertEquals("Expected " + expectedCount + " events on " + date,
            expectedCount, events.size());
    return events;
  }

  /**
   * Gets a single event for a date and returns it.
   */
  protected Event getSingleEvent(LocalDate date) {
    List<Event> events = getEventsAndAssertCount(date, 1);
    return events.get(0);
  }

  /**
   * Asserts that no events exist on the given date.
   */
  protected void assertNoEvents(LocalDate date) {
    getEventsAndAssertCount(date, 0);
  }

  /**
   * Counts total events across a date range.
   */
  protected int countEventsInRange(LocalDate start, int days) {
    int count = 0;
    for (int i = 0; i < days; i++) {
      count += calendar.getEventsList(start.plusDays(i)).size();
    }
    return count;
  }

  /**
   * Verifies that all events in a list belong to the same series.
   */
  protected void assertSameSeries(List<Event> events) {
    if (events.isEmpty()) return;

    UUID seriesId = events.get(0).getSeriesID();
    assertNotNull("First event should have series ID", seriesId);

    for (Event event : events) {
      assertEquals("All events should have same series ID", seriesId, event.getSeriesID());
    }
  }

  /**
   * Creates test date/time constants for commonly used dates.
   */
  protected static class TestDates {
    public static final LocalDate JUNE52025 = LocalDate.of(2025, 6, 5);
    public static final LocalDate JUNE22025 = LocalDate.of(2025, 6, 2);
    public static final LocalDate JUNE42025 = LocalDate.of(2025, 6, 4);
    public static final LocalDate JUNE62025 = LocalDate.of(2025, 6, 6);
    public static final LocalDate JUNE302025 = LocalDate.of(2025, 6, 30);

    public static final LocalTime TIME9AM = LocalTime.of(9, 0);
    public static final LocalTime TIME10AM = LocalTime.of(10, 0);
    public static final LocalTime TIME5PM = LocalTime.of(17, 0);

    public static LocalDateTime dateTime(LocalDate date, LocalTime time) {
      return LocalDateTime.of(date, time);
    }
  }

  /**
   * Common day sets for series testing.
   */
  protected static class DaySets {
    public static final Set<Days> MWF = EnumSet.of(Days.MONDAY, Days.WEDNESDAY, Days.FRIDAY);
    public static final Set<Days> MW = EnumSet.of(Days.MONDAY, Days.WEDNESDAY);
    public static final Set<Days> M = EnumSet.of(Days.MONDAY);
  }

  /**
   * Runs a test that expects a CalendarException.
   */
  protected void expectCalendarException(String scannerInput,
                                         Class<? extends CalendarCommand> commandClass) {
    try {
      Scanner scanner = new Scanner(scannerInput);
      CalendarCommand cmd = createCommand(commandClass, scanner);
      cmd.execute(calendar);
      fail("Expected CalendarException but none was thrown");
    } catch (CalendarException e) {
      // Expected
    } catch (Exception e) {
      fail("Expected CalendarException but got: " + e.getClass().getName());
    }
  }

  /**
   * Runs a test that expects an IllegalArgumentException.
   */
  protected void expectIllegalArgumentException(String scannerInput,
                                                Class<? extends CalendarCommand> commandClass) {
    try {
      Scanner scanner = new Scanner(scannerInput);
      CalendarCommand cmd = createCommand(commandClass, scanner);
      cmd.execute(calendar);
      fail("Expected IllegalArgumentException but none was thrown");
    } catch (IllegalArgumentException e) {
      // Expected
    } catch (Exception e) {
      fail("Expected IllegalArgumentException but got: " + e.getClass().getName());
    }
  }

  /**
   * Creates a command instance based on the class type.
   */
  private CalendarCommand createCommand(Class<? extends
          CalendarCommand> commandClass, Scanner scanner)
          throws Exception {
    return commandClass.getConstructor(Scanner.class, ICalendarView.class)
            .newInstance(scanner, view);
  }

  /**
   * Executes a command and returns the output.
   */
  protected String executeCommand(String input, Class<? extends CalendarCommand> commandClass)
          throws CalendarException {
    clearOutput();
    Scanner scanner = new Scanner(input);
    try {
      CalendarCommand cmd = createCommand(commandClass, scanner);
      cmd.execute(calendar);
    } catch (Exception e) {
      if (e instanceof CalendarException) {
        throw (CalendarException) e;
      }
      throw new RuntimeException("Failed to create command", e);
    }
    return getOutput();
  }

  /**
   * Verifies event properties.
   */
  protected void assertEventProperties(Event event, String subject, LocalDateTime start,
                                       LocalDateTime end) {
    assertEquals("Event subject", subject, event.getSubject());
    assertEquals("Event start time", start, event.getStart());
    assertEquals("Event end time", end, event.getEnd());
  }

  /**
   * Gets the count of events on specific days of the week within a date range.
   */
  protected int countEventsOnDaysOfWeek(LocalDate start, int daysToCheck,
                                        java.time.DayOfWeek... daysOfWeek) {
    Set<java.time.DayOfWeek> targetDays = EnumSet.noneOf(java.time.DayOfWeek.class);
    for (java.time.DayOfWeek day : daysOfWeek) {
      targetDays.add(day);
    }

    int count = 0;
    for (int i = 0; i < daysToCheck; i++) {
      LocalDate checkDate = start.plusDays(i);
      if (targetDays.contains(checkDate.getDayOfWeek())) {
        count += calendar.getEventsList(checkDate).size();
      }
    }
    return count;
  }
}