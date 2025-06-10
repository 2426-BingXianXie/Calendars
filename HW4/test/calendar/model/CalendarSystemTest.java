package calendar.model;

import org.junit.Before;

import java.time.LocalDateTime;


/**
 * JUnit 4 test class for the {@link CalendarSystem} model,
 * which implements the {@link ICalendarSystem} interface.
 */
public class CalendarSystemTest {

  private ICalendarSystem system;

  /**
   * Sets up the test environment before each test method is executed.
   * Initializes a new {@link CalendarSystem} instance and common {@link LocalDateTime} objects.
   */
  @Before
  public void setUp() {
    system = new CalendarSystem();
  }

}
