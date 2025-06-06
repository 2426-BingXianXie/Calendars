package calendar.controller.commands;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import calendar.CalendarException;
import calendar.model.Days;
import calendar.model.ICalendar;
import calendar.view.ICalendarView;

/**
 * Represents the "create" command for the calendar application.
 * This command handles the creation of both single events and recurring event series.
 * It parses the event details from the scanner input.
 */
public class Create extends AbstractCommand {

  /**
   * Constructs a {@code Create} command.
   *
   * @param sc   The {@link Scanner} from which command arguments are read.
   * @param view The {@link ICalendarView} used for displaying messages to the user.
   */
  public Create(Scanner sc, ICalendarView view) {
    super(sc, view);
  }

  /**
   * Executes the "create" command to create an event or event series.
   *
   * @param calendar The {@link ICalendar} model on which the command will operate.
   * @throws CalendarException if an error occurs during command execution.
   */
  @Override
  public void execute(ICalendar calendar) throws CalendarException {
    handleCreate(calendar);
  }

  /**
   * Handles the overall process of creating an event or event series based on user input.
   * It parses the event subject and determines if it's a single event or a series,
   * then delegates to appropriate helper methods.
   *
   * @param model The {@link ICalendar} model to create the event(s) in.
   * @throws CalendarException if there are missing or invalid inputs during parsing.
   */
  private void handleCreate(ICalendar model) throws CalendarException {
    checkForEvent(sc);
    if (!sc.hasNext()) {
      throw new CalendarException("Missing event subject.");
    }
    StringBuilder subjectBuilder = new StringBuilder();
    String keywordAfterSubject = ""; // This will store "on" or "from"

    while (sc.hasNext()) {
      String token = sc.next();
      // check if subject only contains 1 word
      if (token.equalsIgnoreCase("on") || token.equalsIgnoreCase("from")) {
        keywordAfterSubject = token.toLowerCase();
        break; // keyword found, subject is complete
      }
      // if subject already contains word, add a space
      if (subjectBuilder.length() > 0) {
        subjectBuilder.append(" ");
      }
      // append next word to subject
      subjectBuilder.append(token);
    }
    String subject = subjectBuilder.toString();
    // check that there is a valid subject
    if (subject.isEmpty()) {
      throw new CalendarException("Missing event subject");
    }
    // check that user inputted 'on' or 'from'
    if (keywordAfterSubject.isEmpty()) {
      throw new CalendarException(
              "Incomplete command, expected 'on' or 'from'.");
    }
    if (keywordAfterSubject.equals("on")) {
      handleCreateOnVariants(subject, sc, model);
    } else { // keyword 'from'
      handleCreateFromVariants(subject, sc, model);
    }
  }

  /**
   * Checks if the required "event" keyword follows the "create" command.
   *
   * @param sc The {@link Scanner} to read the next token.
   * @throws CalendarException if "event" is missing or an invalid keyword is found.
   */
  private void checkForEvent(Scanner sc) throws CalendarException {
    if (!sc.hasNext()) {
      throw new CalendarException(
              "Missing 'event' keyword after 'create'.");
    }
    String next = sc.next();
    // check for 'event' after create
    if (!next.equalsIgnoreCase("event")) {
      throw new CalendarException(
              "Invalid command 'create " + next + "'.");
    }
  }

  /**
   * Handles the creation of events when the "on" keyword is used, which typically implies
   * all-day events or all-day event series.
   *
   * @param subject  The subject of the event(s).
   * @param sc       The {@link Scanner} for further command input.
   * @param model    The {@link ICalendar} model.
   * @throws CalendarException if there are missing or invalid inputs.
   */
  private void handleCreateOnVariants(String subject, Scanner sc, ICalendar model)
          throws CalendarException {
    // check that there is an input after 'on'
    LocalDate onDate = parseDate(sc);
    // Check if there's a "repeats" keyword next
    if (sc.hasNext()) {
      String repeatsKeyword = sc.next();
      // is a series of all-day events
      if (repeatsKeyword.equalsIgnoreCase("repeats")) {
        // set onDate to start at 8am
        LocalDateTime onDateTime = LocalDateTime.of(onDate, LocalTime.of(8, 0));
        LocalTime startTime = onDateTime.toLocalTime();
        // set end time to 5pm
        LocalTime endTime = LocalTime.of(17, 0);
        // Pass 'true' for isAllDaySeries
        handleSeriesDetails(subject, onDateTime, true, sc, model, startTime, endTime);
      } else {
        throw new CalendarException("Unexpected token. Expected 'repeats' or end of " +
                "command for single all-day event.");
      }
    } else { // no repeats keyword, so event is an all-day event
      model.createEvent(subject,
              LocalDateTime.of(onDate, LocalTime.of(8, 0)),
              LocalDateTime.of(onDate, LocalTime.of(17, 0)));
      view.writeMessage("Event '" + subject + "' created from 8am to 5pm on " + onDate +
              System.lineSeparator());
    }
  }

  /**
   * Handles the creation of events when the "from" keyword is used, implying specific
   * start and end times, for both single events and timed event series.
   *
   * @param subject The subject of the event(s).
   * @param sc      The {@link Scanner} for further command input.
   * @param model   The {@link ICalendar} model.
   * @throws CalendarException if there are missing or invalid inputs, or if
   * a series event spans multiple days.
   */
  private void handleCreateFromVariants(String subject, Scanner sc, ICalendar model)
          throws CalendarException {
    // attempt to parse date input
    LocalDateTime fromDate = parseDateTime(sc);
    // check that next word is 'to'
    if (!sc.hasNext() || !sc.next().equalsIgnoreCase("to")) {
      throw new CalendarException("Missing 'to' keyword.");
    }
    // attempt to parse second date input
    LocalDateTime toDate = parseDateTime(sc);
    if (toDate.isBefore(fromDate)) { // check for valid end date input
      throw new CalendarException("End date must be after start date");
    }
    // check if there's a 'repeats' keyword next
    if (sc.hasNext()) {
      String repeatsKeyword = sc.next();
      if (repeatsKeyword.equalsIgnoreCase("repeats")) { // is a series of events
        // check that each event in a series only lasts 1 day
        if (!fromDate.toLocalDate().isEqual(toDate.toLocalDate())) {
          throw new CalendarException("Each event in a series can only last one day");
        }
        handleSeriesDetails(subject, fromDate, false, sc, model, fromDate.toLocalTime(),
                toDate.toLocalTime());
      } else {
        throw new CalendarException("Expected 'repeats' or end of command for single timed event.");
      }
    } else { // no more inputs, so it's a single event
      model.createEvent(subject, fromDate, toDate);
      view.writeMessage("Event '" + subject + "' created from " + fromDate + " to " + toDate + "."
              + System.lineSeparator());
    }
  }

  /**
   * Handles parsing and creating an event series, including repetition details
   * (days of the week, number of repeats or end date).
   *
   * @param subject      The subject of the event series.
   * @param startDate    The starting date and time of the series.
   * @param isAllDay     True if the series consists of all-day events, false otherwise.
   * @param sc           The {@link Scanner} for further command input.
   * @param model        The {@link ICalendar} model.
   * @param startTime    The start time of individual events in the series.
   * @param endTime      The end time of individual events in the series.
   * @throws CalendarException if there are missing or invalid inputs for series details.
   */
  private void handleSeriesDetails(String subject, LocalDateTime startDate, boolean isAllDay,
                                   Scanner sc, ICalendar model, LocalTime startTime,
                                   LocalTime endTime) throws CalendarException {
    if (!sc.hasNext()) throw new CalendarException("Missing days to repeat");
    String daysString = sc.next();
    Set<Days> daysOfWeek = new HashSet<Days>();
    char[] chars = daysString.toCharArray();
    for (char c : chars) { // parse through weekday string to get the given days
      daysOfWeek.add(Days.fromSymbol(c));
    }
    if (!sc.hasNext()) throw new CalendarException("Missing 'for' or 'until'.");
    String terminatorKeyword = sc.next();
    Integer repeatsCount = null;
    LocalDate seriesEndDate = null;

    if (terminatorKeyword.equalsIgnoreCase("for")) {
      // check that there is a valid input for number of repeated times
      if (!sc.hasNextInt()) throw new CalendarException("Missing <N> after 'for'.");
      // has valid input, set repeatsCount as input, will throw error if not integer
      repeatsCount = sc.nextInt();
      // check that user inputted "times" after number
      if (!sc.hasNext() || !sc.next().equalsIgnoreCase("times")) {
        throw new CalendarException("Missing 'times' after 'for <N>'.");
      }
    } else if (terminatorKeyword.equalsIgnoreCase("until")) {
      // check that there is valid input for end date
      seriesEndDate = parseDate(sc);
    } else {
      throw new CalendarException("Expected 'for' or 'until' after 'repeats <weekdays>'.");
    }
    // generate event series
    model.createEventSeries(subject, startTime, endTime, daysOfWeek, startDate.toLocalDate(),
            seriesEndDate, repeatsCount != null ? repeatsCount : 0,
            null, null, null);

    if (terminatorKeyword.equalsIgnoreCase("for")) {
      view.writeMessage("Event series '" + subject + "' created on " + startDate.toLocalDate() +
              " from " + startTime + " to " + endTime + " repeating on " + daysOfWeek + " " +
              repeatsCount + " times." + System.lineSeparator());
    } else {
      view.writeMessage("Event series '" + subject + "' created on " + startDate.toLocalDate() +
              " from " + startTime + " to " + endTime + " repeating on " + daysOfWeek + " until " +
              seriesEndDate + "." + System.lineSeparator());
    }
  }
}