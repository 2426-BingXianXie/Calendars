package calendar.controller.commands;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import calendar.CalendarException;
import calendar.model.Event;
import calendar.model.ICalendar;
import calendar.model.Property;
import calendar.view.ICalendarView;

/**
 * Represents the "edit" command for the calendar application.
 * This command handles editing properties of both single events and event series.
 */
public class Edit extends AbstractCommand {

  /**
   * Constructs an {@code Edit} command.
   *
   * @param sc   The {@link Scanner} from which command arguments are read.
   * @param view The {@link ICalendarView} used for displaying messages to the user.
   */
  public Edit(Scanner sc, ICalendarView view) {
    super(sc, view);
  }

  /**
   * Executes the "edit" command to modify event properties.
   *
   * @param calendar The {@link ICalendar} model on which the command will operate.
   * @throws CalendarException if an error occurs during command execution.
   */
  @Override
  public void go(ICalendar calendar) throws CalendarException {
    handleEdit(calendar);
  }

  /**
   * Handles the overall process of editing an event or event series based on user input.
   * It determines whether to edit a single event or a series, then delegates
   * to {@code handleEditEvent}.
   *
   * @param model The {@link ICalendar} model to perform the edit operation on.
   * @throws CalendarException if the input keyword after "edit" is invalid.
   */
  private void handleEdit(ICalendar model) throws CalendarException {
    if (!sc.hasNext()) throw new CalendarException(
            "Missing 'event' keyword after 'create'.");
    String next = sc.next();
    // check for input after create
    if (next.equalsIgnoreCase("event") ||
            next.equalsIgnoreCase("events")) {
      handleEditEvent(sc, model, false);
    } else if (next.equalsIgnoreCase("series")) {
      handleEditEvent(sc, model, true);
    } else {
      throw new CalendarException("Unknown event keyword: " + next);
    }
  }

  /**
   * Handles the detailed editing of an event or series based on parsed properties.
   *
   * @param sc         The {@link Scanner} for further command input.
   * @param model      The {@link ICalendar} model.
   * @param fullSeries True if the entire series should be edited, false if only a single event or
   *                   the series from a specific date should be edited.
   * @throws CalendarException if there are missing or invalid inputs.
   */
  private void handleEditEvent(Scanner sc, ICalendar model, boolean fullSeries)
          throws CalendarException {
    Property property = checkValidProperty(sc);
    String subject = checkValidSubject(sc);
    // check that there is a valid subject
    if (subject.isEmpty()) {
      throw new CalendarException("Missing event subject");
    }
    // check that user inputted date
    if (!sc.hasNext()) {
      throw new CalendarException(
              "Incomplete command, missing <dateStringTtimeString>.");
    }
    handleEditFromVariants(sc, model, subject, property, fullSeries);
  }

  /**
   * Validates and parses the {@link Property} to be edited from the scanner.
   *
   * @param sc The {@link Scanner} to read the property string.
   * @return The parsed {@link Property} enum.
   * @throws CalendarException if the property string is missing or invalid.
   */
  private Property checkValidProperty(Scanner sc) throws CalendarException {
    // check that user inputted an event property
    if (!sc.hasNext()) {
      throw new CalendarException("Missing event property.");
    }
    // attempt to store property, will result in error if invalid property
    return Property.fromStr(sc.next());
  }

  /**
   * Extracts the subject of the event from the scanner, consuming tokens until "from" is encountered.
   *
   * @param sc The {@link Scanner} containing the subject tokens.
   * @return The concatenated subject string.
   */
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

  /**
   * Handles the "from" variants of the edit command, dealing with single events,
   * editing a series from a specific date, or editing the full series.
   *
   * @param sc             The {@link Scanner} for further command input.
   * @param model          The {@link ICalendar} model.
   * @param subject        The subject of the event(s) to be edited.
   * @param property       The {@link Property} to change.
   * @param editFullSeries A boolean indicating if the entire series should be edited.
   * @throws CalendarException if there are missing or invalid inputs, or if multiple
   *                           events match the description.
   */
  private void handleEditFromVariants(Scanner sc, ICalendar model, String subject,
                                      Property property, boolean editFullSeries)
          throws CalendarException {
    // attempt to parse from date input
    LocalDateTime fromDate = parseDateTime(sc);
    if (!sc.hasNext()) throw new CalendarException(
            "Missing 'to' after from <dateStringTTimeString>.");
    String nextKeyword = sc.next();
    if (nextKeyword.equalsIgnoreCase("to")) { // means edit single event
      // attempt to parse end date input
      LocalDateTime toDate = parseDateTime(sc);
      // check for 'with' input after end date input
      if (!sc.hasNext()) {
        throw new CalendarException(
                "Missing input after 'to <dateStringTTimeString>.");
      }
      if (!sc.next().equalsIgnoreCase("with")) {
        throw new CalendarException(
                "Expected 'with' after 'to <dateStringTTimeString>.");
      }
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
      if (!sc.hasNext()) {
        throw new CalendarException("Missing <NewPropertyValue>.");
      }
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

  /**
   * Helper method to check for ambiguous event matches.
   * Throws an exception if no events are found or if multiple events match the description,
   * preventing an ambiguous edit operation.
   *
   * @param events The list of events found matching the criteria.
   * @return The single matching {@link Event}.
   * @throws CalendarException if no events or multiple events are found.
   */
  private Event checkAmbiguousEvents(List<Event> events) throws CalendarException {
    if (events.isEmpty()) { // check for no matching events
      throw new CalendarException("No events found.");
    } else if (events.size() > 1) { // check if multiple events match description
      throw new CalendarException("Error: multiple events found.");
    }
    return events.get(0);
  }
}