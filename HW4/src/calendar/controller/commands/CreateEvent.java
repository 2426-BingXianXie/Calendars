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
import calendar.model.ICalendarSystem;
import calendar.view.ICalendarView;

/**
 * Represents the "create event" command for the calendar application.
 * This command handles the creation of both single events and recurring event series.
 * It parses the event details from the scanner input.
 */
public class CreateEvent extends AbstractCommand {

  /**
   * Constructs a {@code Create} command.
   *
   * @param sc   The {@link Scanner} from which command arguments are read.
   * @param view The {@link ICalendarView} used for displaying messages to the user.
   */
  public CreateEvent(Scanner sc, ICalendarView view) {
    super(sc, view);
  }

  /**
   * Executes the "create" command to create an event or event series.
   *
   * @param system The {@link ICalendarSystem} model on which the command will operate.
   * @throws CalendarException if an error occurs during command execution.
   */
  @Override
  public void execute(ICalendarSystem system) throws CalendarException {
    ICalendar calendar = system.getCurrentCalendar();
    if (calendar == null) {
      throw new CalendarException("No calendar currently in use.");
    }
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
    if (!sc.hasNext()) {
      throw new CalendarException("Missing event subject.");
    }

    EventParsingContext context = parseEventSubjectAndKeyword();

    if (context.keyword.equals("on")) {
      handleCreateOnVariants(context.subject, sc, model);
    } else if (context.keyword.equals("from")) {
      handleCreateFromVariants(context.subject, sc, model);
    } else {
      throw new CalendarException("Incomplete command, expected 'on' or 'from'.");
    }
  }

  /**
   * Parses the event subject and the following keyword (on/from).
   *
   * @return EventParsingContext containing the subject and keyword
   * @throws CalendarException if subject is empty or keyword is missing
   */
  private EventParsingContext parseEventSubjectAndKeyword() throws CalendarException {
    StringBuilder subjectBuilder = new StringBuilder();
    String keywordAfterSubject = "";

    while (sc.hasNext()) {
      String token = sc.next();
      if (token.equalsIgnoreCase("on") || token.equalsIgnoreCase("from")) {
        keywordAfterSubject = token.toLowerCase();
        break;
      }
      if (subjectBuilder.length() > 0) {
        subjectBuilder.append(" ");
      }
      subjectBuilder.append(token);
    }

    String subject = subjectBuilder.toString();
    if (subject.isEmpty()) {
      throw new CalendarException("Missing event subject");
    }
    if (keywordAfterSubject.isEmpty()) {
      throw new CalendarException("Incomplete command, expected 'on' or 'from'.");
    }

    return new EventParsingContext(subject, keywordAfterSubject);
  }

  /**
   * Handles the creation of events when the "on" keyword is used, which typically implies
   * all-day events or all-day event series.
   *
   * @param subject The subject of the event(s).
   * @param sc      The {@link Scanner} for further command input.
   * @param model   The {@link ICalendar} model.
   * @throws CalendarException if there are missing or invalid inputs.
   */
  private void handleCreateOnVariants(String subject, Scanner sc, ICalendar model)
          throws CalendarException {
    LocalDate onDate = parseDate(sc);

    if (sc.hasNext()) {
      String repeatsKeyword = sc.next();
      if (repeatsKeyword.equalsIgnoreCase("repeats")) {
        createAllDayEventSeries(subject, onDate, sc, model);
      } else {
        throw new CalendarException("Unexpected token. Expected 'repeats' or end of " +
                "command for single all-day event.");
      }
    } else {
      createSingleAllDayEvent(subject, onDate, model);
    }
  }

  /**
   * Creates a single all-day event.
   *
   * @param subject the event subject
   * @param date the event date
   * @param model the calendar model
   * @throws CalendarException if event creation fails
   */
  private void createSingleAllDayEvent(String subject, LocalDate date, ICalendar model)
          throws CalendarException {
    model.createEvent(subject,
            LocalDateTime.of(date, LocalTime.of(8, 0)),
            LocalDateTime.of(date, LocalTime.of(17, 0)));
    view.writeMessage("Event '" + subject + "' created from 8am to 5pm on " + date +
            System.lineSeparator());
  }

  /**
   * Creates an all-day event series.
   *
   * @param subject the event subject
   * @param startDate the series start date
   * @param sc the scanner for additional input
   * @param model the calendar model
   * @throws CalendarException if series creation fails
   */
  private void createAllDayEventSeries(String subject, LocalDate startDate, Scanner sc,
                                       ICalendar model) throws CalendarException {
    LocalDateTime onDateTime = LocalDateTime.of(startDate, LocalTime.of(8, 0));
    LocalTime startTime = onDateTime.toLocalTime();
    LocalTime endTime = LocalTime.of(17, 0);

    handleSeriesDetails(subject, onDateTime, true, sc, model, startTime, endTime);
  }

  /**
   * Handles the creation of events when the "from" keyword is used, implying specific
   * start and end times, for both single events and timed event series.
   *
   * @param subject The subject of the event(s).
   * @param sc      The {@link Scanner} for further command input.
   * @param model   The {@link ICalendar} model.
   * @throws CalendarException if there are missing or invalid inputs, or if
   *                           a series event spans multiple days.
   */
  private void handleCreateFromVariants(String subject, Scanner sc, ICalendar model)
          throws CalendarException {
    LocalDateTime fromDate = parseDateTime(sc);
    validateToKeyword(sc);
    LocalDateTime toDate = parseDateTime(sc);

    validateEventTimes(fromDate, toDate);

    if (sc.hasNext()) {
      String repeatsKeyword = sc.next();
      if (repeatsKeyword.equalsIgnoreCase("repeats")) {
        validateSingleDayEvent(fromDate, toDate);
        handleSeriesDetails(subject, fromDate, false, sc, model,
                fromDate.toLocalTime(), toDate.toLocalTime());
      } else {
        throw new CalendarException("Expected 'repeats' or end of command for single timed event.");
      }
    } else {
      createSingleTimedEvent(subject, fromDate, toDate, model);
    }
  }

  /**
   * Validates that the "to" keyword is present.
   *
   * @param sc the scanner to read from
   * @throws CalendarException if "to" keyword is missing
   */
  private void validateToKeyword(Scanner sc) throws CalendarException {
    if (!sc.hasNext() || !sc.next().equalsIgnoreCase("to")) {
      throw new CalendarException("Missing 'to' keyword.");
    }
  }

  /**
   * Validates that the end time is after the start time.
   *
   * @param fromDate the start time
   * @param toDate the end time
   * @throws CalendarException if end time is not after start time
   */
  private void validateEventTimes(LocalDateTime fromDate, LocalDateTime toDate)
          throws CalendarException {
    if (toDate.isBefore(fromDate)) {
      throw new CalendarException("End date must be after start date");
    }
  }

  /**
   * Validates that an event in a series only spans a single day.
   *
   * @param fromDate the start time
   * @param toDate the end time
   * @throws CalendarException if the event spans multiple days
   */
  private void validateSingleDayEvent(LocalDateTime fromDate, LocalDateTime toDate)
          throws CalendarException {
    if (!fromDate.toLocalDate().isEqual(toDate.toLocalDate())) {
      throw new CalendarException("Each event in a series can only last one day");
    }
  }

  /**
   * Creates a single timed event.
   *
   * @param subject the event subject
   * @param fromDate the start time
   * @param toDate the end time
   * @param model the calendar model
   * @throws CalendarException if event creation fails
   */
  private void createSingleTimedEvent(String subject, LocalDateTime fromDate,
                                      LocalDateTime toDate, ICalendar model)
          throws CalendarException {
    model.createEvent(subject, fromDate, toDate);
    view.writeMessage("Event '" + subject + "' created from " + fromDate + " to " + toDate + "." +
            System.lineSeparator());
  }

  /**
   * Handles parsing and creating an event series, including repetition details
   * (days of the week, number of repeats or end date).
   *
   * @param subject   The subject of the event series.
   * @param startDate The starting date and time of the series.
   * @param isAllDay  True if the series consists of all-day events, false otherwise.
   * @param sc        The {@link Scanner} for further command input.
   * @param model     The {@link ICalendar} model.
   * @param startTime The start time of individual events in the series.
   * @param endTime   The end time of individual events in the series.
   * @throws CalendarException if there are missing or invalid inputs for series details.
   */
  private void handleSeriesDetails(String subject, LocalDateTime startDate, boolean isAllDay,
                                   Scanner sc, ICalendar model, LocalTime startTime,
                                   LocalTime endTime) throws CalendarException {
    Set<Days> daysOfWeek = parseDaysOfWeek(sc);
    RepetitionTerminator terminator = parseRepetitionTerminator(sc);

    createEventSeries(subject, startTime, endTime, daysOfWeek, startDate.toLocalDate(),
            terminator, model);

    displaySeriesCreationMessage(subject, startDate, startTime, endTime, daysOfWeek, terminator);
  }

  /**
   * Parses the days of the week for series recurrence.
   *
   * @param sc the scanner to read from
   * @return set of days for recurrence
   * @throws CalendarException if days string is missing or invalid
   */
  private Set<Days> parseDaysOfWeek(Scanner sc) throws CalendarException {
    if (!sc.hasNext()) {
      throw new CalendarException("Missing days to repeat");
    }

    String daysString = sc.next();
    Set<Days> daysOfWeek = new HashSet<>();
    char[] chars = daysString.toCharArray();

    for (char c : chars) {
      daysOfWeek.add(Days.fromSymbol(c));
    }

    return daysOfWeek;
  }

  /**
   * Parses the repetition terminator (for N times or until date).
   *
   * @param sc the scanner to read from
   * @return RepetitionTerminator containing termination details
   * @throws CalendarException if terminator is missing or invalid
   */
  private RepetitionTerminator parseRepetitionTerminator(Scanner sc) throws CalendarException {
    if (!sc.hasNext()) {
      throw new CalendarException("Missing 'for' or 'until'.");
    }

    String terminatorKeyword = sc.next();

    if (terminatorKeyword.equalsIgnoreCase("for")) {
      return parseForTerminator(sc);
    } else if (terminatorKeyword.equalsIgnoreCase("until")) {
      return parseUntilTerminator(sc);
    } else {
      throw new CalendarException("Expected 'for' or 'until' after 'repeats <weekdays>'.");
    }
  }

  /**
   * Parses the "for N times" terminator.
   *
   * @param sc the scanner to read from
   * @return RepetitionTerminator with count-based termination
   * @throws CalendarException if count is missing or invalid
   */
  private RepetitionTerminator parseForTerminator(Scanner sc) throws CalendarException {
    if (!sc.hasNextInt()) {
      throw new CalendarException("Missing <N> after 'for'.");
    }

    int repeatsCount = sc.nextInt();

    if (!sc.hasNext() || !sc.next().equalsIgnoreCase("times")) {
      throw new CalendarException("Missing 'times' after 'for <N>'.");
    }

    return new RepetitionTerminator(repeatsCount, null);
  }

  /**
   * Parses the "until date" terminator.
   *
   * @param sc the scanner to read from
   * @return RepetitionTerminator with date-based termination
   * @throws CalendarException if date is missing or invalid
   */
  private RepetitionTerminator parseUntilTerminator(Scanner sc) throws CalendarException {
    LocalDate seriesEndDate = parseDate(sc);
    return new RepetitionTerminator(0, seriesEndDate);
  }

  /**
   * Creates the event series in the calendar model.
   *
   * @param subject the series subject
   * @param startTime the start time for events
   * @param endTime the end time for events
   * @param daysOfWeek the days of recurrence
   * @param startDate the series start date
   * @param terminator the repetition terminator
   * @param model the calendar model
   * @throws CalendarException if series creation fails
   */
  private void createEventSeries(String subject, LocalTime startTime, LocalTime endTime,
                                 Set<Days> daysOfWeek, LocalDate startDate,
                                 RepetitionTerminator terminator, ICalendar model)
          throws CalendarException {
    model.createEventSeries(subject, startTime, endTime, daysOfWeek, startDate,
            terminator.endDate, terminator.count,
            null, null, null);
  }

  /**
   * Displays the series creation confirmation message.
   *
   * @param subject the series subject
   * @param startDate the series start date
   * @param startTime the event start time
   * @param endTime the event end time
   * @param daysOfWeek the days of recurrence
   * @param terminator the repetition terminator
   */
  private void displaySeriesCreationMessage(String subject, LocalDateTime startDate,
                                            LocalTime startTime, LocalTime endTime,
                                            Set<Days> daysOfWeek, RepetitionTerminator terminator) {
    String baseMessage = "Event series '" + subject + "' created on " + startDate.toLocalDate() +
            " from " + startTime + " to " + endTime + " repeating on " + daysOfWeek + " ";

    if (terminator.isCountBased()) {
      view.writeMessage(baseMessage + terminator.count + " times." + System.lineSeparator());
    } else {
      view.writeMessage(baseMessage + "until " + terminator.endDate + "." + System.lineSeparator());
    }
  }

  /**
   * Context class to hold event parsing results.
   */
  private static class EventParsingContext {
    final String subject;
    final String keyword;

    EventParsingContext(String subject, String keyword) {
      this.subject = subject;
      this.keyword = keyword;
    }
  }

  /**
   * Class to represent repetition termination parameters.
   */
  private static class RepetitionTerminator {
    final int count;
    final LocalDate endDate;

    RepetitionTerminator(int count, LocalDate endDate) {
      this.count = count;
      this.endDate = endDate;
    }

    /**
     * Checks if this terminator is count-based (for N times).
     *
     * @return true if count-based, false if date-based
     */
    boolean isCountBased() {
      return count > 0;
    }
  }
}