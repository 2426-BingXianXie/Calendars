package calendar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;

public class CalendarController implements ICalendarController {
  private final Readable in;
  private final ICalendarView view;
  private final ICalendar model;

  public CalendarController(ICalendar model, ICalendarView view, Readable in) {
    this.model = model;
    this.in = in;
    this.view = view;
  }

  public void go() {
    Scanner sc = new Scanner(in);
    boolean quit = false;
    view.showMenu(); //prompt for the instruction name
    //print the welcome message
    while (!quit) { //continue until the user quits
      view.writeMessage("Enter command: ");
      if (!sc.hasNextLine()) { // end of input
        break;
      }
      String line = sc.nextLine().trim();
      if (line.isEmpty()) { // if the user just pressed enter
        continue;
      }
      Scanner lineScanner = new Scanner(line);
      String userInstruction = lineScanner.next(); //take an instruction name
      if (userInstruction.equals("quit") || userInstruction.equals("q")) {
        quit = true;
      } else if (userInstruction.equals("menu")) {
        view.showMenu();
      } else {
        processInput(userInstruction, lineScanner, model);
      }
    }
    //after the user has quit, print farewell message
    view.farewellMessage();
  }

  private void processInput(String userInstruction, Scanner sc, ICalendar calendar)
          throws IllegalArgumentException {
    try {
      switch (userInstruction) {
        case "create":
          handleCreate(sc, calendar);
          break;
        case "edit":
          handleEdit(sc, calendar);
          break;
        case "print":
          handlePrint(sc, calendar);
          break;
        case "show":
          handleShow(sc, calendar);
          break;
        default:
          throw new IllegalArgumentException("Unknown instruction: " + userInstruction);
      }
    } catch (IllegalArgumentException e) {
      view.writeMessage("Error processing command: " + e.getMessage() + System.lineSeparator());

    }
  }

  private void handleCreate(Scanner sc, ICalendar model) throws IllegalArgumentException {
    checkForEvent(sc);
    if (!sc.hasNext()) throw new IllegalArgumentException("Missing event subject.");
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
    if (subject.isEmpty()) throw new IllegalArgumentException("Missing event subject");
    // check that user inputted 'on' or 'from'
    if (keywordAfterSubject.isEmpty()) throw new IllegalArgumentException(
            "Incomplete command, expected 'on' or 'from'.");
    if (keywordAfterSubject.equals("on")) {
      handleCreateOnVariants(subject, sc, model);
    } else { // keyword 'from'
      handleCreateFromVariants(subject, sc, model);
    }
  }

  private void checkForEvent(Scanner sc) throws IllegalArgumentException {
    if (!sc.hasNext()) throw new IllegalArgumentException(
            "Missing 'event' keyword after 'create'.");
    String next = sc.next();
    // check for 'event' after create
    if (!next.equalsIgnoreCase("event")) throw new IllegalArgumentException(
            "Invalid command 'create " + next + "'.");
  }

  private void handleCreateOnVariants(String subject, Scanner sc, ICalendar model)
          throws IllegalArgumentException {
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
        throw new IllegalArgumentException("Unexpected token. Expected 'repeats' or end of " +
                "command for single all-day event.");
      }
    } else { // no repeats keyword, so event is an all-day event
      model.createEvent(subject,
              LocalDateTime.of(onDate, LocalTime.of(8, 0)),
              LocalDateTime.of(onDate, LocalTime.of(17, 0)),
              null, null, null);
      view.writeMessage("Event '" + subject + "' created from 8am to 5pm on " + onDate +
              System.lineSeparator());
    }
  }

  private LocalDate parseDate(Scanner sc) throws IllegalArgumentException {
    // check that there is a valid date input
    if (!sc.hasNext()) throw new IllegalArgumentException("Missing <dateString> after 'on'.");
    String dateString = sc.next();
    try { // check for valid date format
      return LocalDate.parse(dateString);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid date format for <dateString>. " +
              "Expected YYYY-MM-DD");
    }
  }

  private void handleCreateFromVariants(String subject, Scanner sc, ICalendar model)
          throws IllegalArgumentException {
    // attempt to parse date input
    LocalDateTime fromDate = parseDateTime(sc);
    // check that next word is 'to'
    if (!sc.hasNext() || !sc.next().equalsIgnoreCase("to")) {
      throw new IllegalArgumentException("Missing 'to' keyword.");
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
        throw new IllegalArgumentException("Expected 'repeats' after <dateStringTtimeString>.");
      }
    } else { // no more inputs, so it's a single event
      model.createEvent(subject, fromDate, toDate, null, null, null);
      view.writeMessage("Event '" + subject + "' created from " + fromDate + " to " + toDate + "."
              + System.lineSeparator());
    }
  }

  private LocalDateTime parseDateTime(Scanner sc) throws IllegalArgumentException {
    // check that there is a valid date input
    if (!sc.hasNext()) throw new IllegalArgumentException("Missing <dateStringTtimeString>");
    String dateString = sc.next();
    try { // check for valid starting date format
      return LocalDateTime.parse(dateString);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid date format for <dateStringTtimeString>. " +
              "Expected YYYY-MM-DDThh:mm");
    }
  }

  private void handleSeriesDetails(String subject, LocalDateTime startDate, boolean isAllDay,
                                   Scanner sc, ICalendar model, LocalTime startTime,
                                   LocalTime endTime) throws IllegalArgumentException {
    if (!sc.hasNext()) throw new IllegalArgumentException("Missing <dateString> after 'on'.");
    String daysString = sc.next();
    Set<Days> daysOfWeek = new HashSet<Days>();
    char[] chars = daysString.toCharArray();
    for (char c : chars) { // parse through weekday string to get the given days
      daysOfWeek.add(Days.fromSymbol(c));
    }
    if (!sc.hasNext()) throw new IllegalArgumentException("Missing 'for' or 'until'.");
    String terminatorKeyword = sc.next();
    Integer repeatsCount = null;
    LocalDate seriesEndDate = null;

    if (terminatorKeyword.equalsIgnoreCase("for")) {
      // check that there is a valid input for number of repeated times
      if (!sc.hasNextInt()) throw new IllegalArgumentException("Missing <N> after 'for'.");
      // has valid input, set repeatsCount as input, will throw error if not integer
      repeatsCount = sc.nextInt();
      // check that user inputted "times" after number
      if (!sc.hasNext() || !sc.next().equalsIgnoreCase("times")) {
        throw new IllegalArgumentException("Missing 'times' after 'for <N>'.");
      }
    } else if (terminatorKeyword.equalsIgnoreCase("until")) {
      // check that there is valid input for end date
      seriesEndDate = parseDate(sc);
    } else {
      throw new IllegalArgumentException("Expected 'for' or 'until' after 'repeats <weekdays>'.");
    }
    // generate event series
    model.createEventSeries(subject, startTime, endTime, daysOfWeek, startDate.toLocalDate(),
            seriesEndDate, repeatsCount != null ? repeatsCount : 0,
            null, null, null);

    view.writeMessage("Event series '" + subject + "' created on " + startDate.toLocalDate() +
            " from " + startTime + " to " + endTime + " repeating on " + daysOfWeek + " " +
            repeatsCount + " times." + System.lineSeparator());
  }

  private void handleEdit(Scanner sc, ICalendar model) throws IllegalArgumentException {
    if (!sc.hasNext()) throw new IllegalArgumentException(
            "Missing 'event' keyword after 'create'.");
    String next = sc.next();
    // check for input after create
    if (next.equalsIgnoreCase("event") ||
            next.equalsIgnoreCase("events")) {
      handleEditEvent(sc, model, false);
    } else if (next.equalsIgnoreCase("series")) {
      handleEditEvent(sc, model, true);
    } else {
      throw new IllegalArgumentException("Unknown event keyword: " + next);
    }
  }

  private void handleEditEvent(Scanner sc, ICalendar model, boolean fullSeries) {
    Property property = checkValidProperty(sc);
    String subject = checkValidSubject(sc);
    // check that there is a valid subject
    if (subject.isEmpty()) throw new IllegalArgumentException("Missing event subject");
    // check that user inputted date
    if (!sc.hasNext()) throw new IllegalArgumentException(
            "Incomplete command, missing <dateStringTtimeString>.");
    handleEditFromVariants(sc, model, subject, property, fullSeries);
  }

  private Property checkValidProperty(Scanner sc) {
    // check that user inputted an event property
    if (!sc.hasNext()) throw new IllegalArgumentException("Missing event property.");
    // attempt to store property, will result in error if invalid property
    return Property.fromStr(sc.next());
  }

  private String checkValidSubject(Scanner sc) {
    StringBuilder subjectBuilder = new StringBuilder();
    while (sc.hasNext()) {
      String token = sc.next();
      // check if subject only contains 1 word
      if (token.equalsIgnoreCase("from")) {
        break; // keyword found, subject is complete
      }
      // if subject already contains word, add a space
      if (subjectBuilder.length() > 0) {
        subjectBuilder.append(" ");
      }
      // append next word to subject
      subjectBuilder.append(token);
    }
    return subjectBuilder.toString();
  }

  private void handleEditFromVariants(Scanner sc, ICalendar model, String subject,
                                      Property property, boolean editFullSeries) {
    // attempt to parse from date input
    LocalDateTime fromDate = parseDateTime(sc);
    if (!sc.hasNext()) throw new IllegalArgumentException(
            "Missing 'to' after from <dateStringTTimeString>.");
    String nextKeyword = sc.next();
    if (nextKeyword.equalsIgnoreCase("to")) { // means edit single event
      // attempt to parse end date input
      LocalDateTime toDate = parseDateTime(sc);
      // check for 'with' input after end date input
      if (!sc.hasNext()) throw new IllegalArgumentException(
              "Missing input after 'to <dateStringTTimeString>.");
      if (!sc.next().equalsIgnoreCase("with")) throw new IllegalArgumentException(
              "Expected 'with' after 'to <dateStringTTimeString>.");
      String newProperty = sc.next();
      // retrieve events with matching details
      List<Event> events = model.getEventsByDetails(subject, fromDate, toDate);
      Event event = checkAmbiguousEvents(events);
      UUID eventId = event.getId();
      model.editEvent(eventId, property, newProperty);
      view.writeMessage("Edited event '" + subject + "' " + property.getStr() +
              " property to " + newProperty + System.lineSeparator());
    }
    // no specified end date
    else if (nextKeyword.equalsIgnoreCase("with")) {
      if (!sc.hasNext()) throw new IllegalArgumentException(
              "Missing <NewPropertyValue>.");
      String newProperty = sc.next();
      // retrieve events with matching details
      List<Event> events = model.getEventsBySubjectAndStartTime(subject, fromDate);
      Event event = checkAmbiguousEvents(events);
      if (event.getSeriesID() == null) { // not part of series, edit event by itself
        model.editEvent(event.getId(), property, newProperty);
        view.writeMessage("Edited event '" + subject + "' " + property.getStr() +
                " property to " + newProperty + System.lineSeparator());
      } else if (!editFullSeries) { // edit event series starting from this event instance
        view.writeMessage("Edited event series '" + subject + "' " + property.getStr() +
                " property to " + newProperty + " from " + event.getStart() +
                System.lineSeparator());
        model.editSeriesFromDate(event.getSeriesID(), property, newProperty);
      } else { // edit the full series
        view.writeMessage("Edited event series '" + subject + "' " + property.getStr() +
                " property to " + newProperty + System.lineSeparator());
        model.editSeries(event.getSeriesID(), property, newProperty);
      }
    }
  }

  private Event checkAmbiguousEvents(List<Event> events) throws IllegalArgumentException {
    if (events.isEmpty()) { // check for no matching events
      throw new IllegalArgumentException("No events found.");
    } else if (events.size() > 1) { // check if multiple events match description
      throw new IllegalArgumentException("Error: multiple events found.");
    }
    return events.get(0);
  }


  private void handlePrint(Scanner sc, ICalendar model) {
    if (!sc.hasNext()) throw new IllegalArgumentException("Missing input after 'print'.");
    // check that input entered after 'print' is 'events'
    if (!sc.next().equalsIgnoreCase("events")) {
      throw new IllegalArgumentException("Expected 'events' after 'print'.");
    } else {
      // input is valid, check for next keyword after events
      if (!sc.hasNext()) throw new IllegalArgumentException("Missing input after 'events'.");
      String nextKeyword = sc.next();
      // check if next word is 'on', print events only on that day if so
      if (nextKeyword.equalsIgnoreCase("on")) {
        // check for valid input after 'on'
        // attempt to parse date input
        LocalDate date = parseDate(sc);
        List<Event> events = model.getEventsList(date);
        view.showCalendarEvents(events, date);
        // check if next word is 'from'
      } else if (nextKeyword.equalsIgnoreCase("from")) {
        if (!sc.hasNext()) throw new IllegalArgumentException("Missing input after 'from'.");
        // attempt to parse start date input
        LocalDateTime fromDate = parseDateTime(sc);
        if (!sc.hasNext()) throw new IllegalArgumentException(
                "Missing input after <dateStringTTimeString>.");
        if (!sc.next().equalsIgnoreCase("to")) throw new IllegalArgumentException(
                "Expected 'to' after <dateStringTTimeString>.");
        // attempt to parse end date input
        LocalDateTime toDate = parseDateTime(sc);
        List<Event> events = model.getEventsListInDateRange(fromDate, toDate);
        view.showCalendarEventsInDateRange(fromDate, toDate, events);
      }
    }
  }

  private void handleShow(Scanner sc, ICalendar model) {
    if (!sc.hasNext()) throw new IllegalArgumentException("Missing input after 'show'.");
    // check that input after 'show' is 'status'
    if (!sc.next().equalsIgnoreCase("status")) {
      throw new IllegalArgumentException("Expected 'status' after 'show'.");
    }
    if (!sc.hasNext()) throw new IllegalArgumentException("Missing input after 'status'.");
    // check that input after 'status' is 'on'
    if (!sc.next().equalsIgnoreCase("on")) {
      throw new IllegalArgumentException("Expected 'on' after 'status'.");
    }
    // attempt to parse date string
    LocalDateTime date = parseDateTime(sc);
    boolean isBusy = model.isBusyAt(date); // check if user already has event on this dateTime
    if (isBusy) {
      view.writeMessage("User is busy, already has an event scheduled on " + date + "."
              + System.lineSeparator());
    } else {
      view.writeMessage("User is available on " + date + "."
              + System.lineSeparator());
    }
  }
}
