package calendar.model;

import org.junit.Before;
import org.junit.Test;

import calendar.CalendarException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class NamedCalendarTest {

  private ICalendar calendar;

  /**
   * Sets up the test environment before each test method is executed.
   * Initializes a new {@link NamedCalendar} instance and common {@link LocalDateTime} objects.
   */
  @Before
  public void setUp() {
  }

}
