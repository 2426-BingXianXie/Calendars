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

public class Create extends AbstractCommand {

  public Create(Scanner sc, ICalendarView view) {
    super(sc, view);
  }

  @Override
  public void go(ICalendar calendar) throws CalendarException {
    handleCreate(calendar);
  }

  private void handleCreate(ICalendar model) throws CalendarException {
    checkForEvent(sc);
    if (!sc.hasNext()) throw new CalendarException("Missing event subject.");
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
    if (subject.isEmpty()) throw new CalendarException("Missing event subject");
    // check that user inputted 'on' or 'from'
    if (keywordAfterSubject.isEmpty()) throw new CalendarException(
            "Incomplete command, expected 'on' or 'from'.");
    if (keywordAfterSubject.equals("on")) {
      handleCreateOnVariants(subject, sc, model);
    } else { // keyword 'from'
      handleCreateFromVariants(subject, sc, model);
    }
  }

  private void checkForEvent(Scanner sc) throws CalendarException {
    if (!sc.hasNext()) throw new CalendarException(
            "Missing 'event' keyword after 'create'.");
    String next = sc.next();
    // check for 'event' after create
    if (!next.equalsIgnoreCase("event")) throw new CalendarException(
            "Invalid command 'create " + next + "'.");
  }

  private void handleCreateOnVariants(String subject, Scanner sc, ICalendar model)
          throws CalendarException {
    // check that there is an input after 'on'
    LocalDate onDate = parseDate(sc);
    // Check if there's a "repeats" keyword next
    if (sc.hasNext()) {
      String repeatsKeyword = sc.next();
      if (repeatsKeyword.equalsIgnoreCase("repeats")) {
        // series of all-day events
        // set onDate to start at 8am
        LocalDateTime onDateTime = LocalDateTime.of(onDate, LocalTime.of(8, 0));
        LocalTime startTime = onDateTime.toLocalTime();
        // set end time to 5pm
        LocalTime endTime = LocalTime.of(17, 0);
        handleSeriesDetails(subject, onDateTime, true, sc, model, startTime, endTime); // Pass 'true' for isAllDaySeries
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
      throw new IllegalArgumentException("End date must be after start date");
    }
    // check if there's a 'repeats' keyword next
    if (sc.hasNext()) {
      String repeatsKeyword = sc.next();
      if (repeatsKeyword.equalsIgnoreCase("repeats")) { // is a series of events
        // check that each event in a series only lasts 1 day
        if (!fromDate.toLocalDate().isEqual(toDate.toLocalDate())) {
          throw new IllegalArgumentException("Each event in a series can only last one day");
        }
        handleSeriesDetails(subject, fromDate, false, sc, model, fromDate.toLocalTime(), toDate.toLocalTime());
      } else {
        throw new CalendarException("Expected 'repeats' or end of command for single timed event.");
      }
    } else { // no more inputs, so it's a single event
      model.createEvent(subject, fromDate, toDate);
      view.writeMessage("Event '" + subject + "' created from " + fromDate + " to " + toDate + "."
              + System.lineSeparator());
    }
  }

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
