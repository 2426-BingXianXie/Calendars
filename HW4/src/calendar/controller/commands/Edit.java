package calendar.controller.commands;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import calendar.CalendarException;
import calendar.controller.AbstractCommand;
import calendar.model.Event;
import calendar.model.ICalendar;
import calendar.model.Property;
import calendar.view.ICalendarView;

public class Edit extends AbstractCommand {

  public Edit(Scanner sc, ICalendarView view) {
    super(sc, view);
  }

  @Override
  public void go(ICalendar calendar) throws CalendarException {
    handleEdit(calendar);
  }

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

  private void handleEditEvent(Scanner sc, ICalendar model, boolean fullSeries) throws CalendarException {
    Property property = checkValidProperty(sc);
    String subject = checkValidSubject(sc);
    // check that there is a valid subject
    if (subject.isEmpty()) throw new CalendarException("Missing event subject");
    // check that user inputted date
    if (!sc.hasNext()) throw new CalendarException(
            "Incomplete command, missing <dateStringTtimeString>.");
    handleEditFromVariants(sc, model, subject, property, fullSeries);
  }

  private Property checkValidProperty(Scanner sc) throws CalendarException {
    // check that user inputted an event property
    if (!sc.hasNext()) throw new CalendarException("Missing event property.");
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
                                      Property property, boolean editFullSeries) throws CalendarException {
    // attempt to parse from date input
    LocalDateTime fromDate = parseDateTime(sc);
    if (!sc.hasNext()) throw new CalendarException(
            "Missing 'to' after from <dateStringTTimeString>.");
    String nextKeyword = sc.next();
    if (nextKeyword.equalsIgnoreCase("to")) { // means edit single event
      // attempt to parse end date input
      LocalDateTime toDate = parseDateTime(sc);
      // check for 'with' input after end date input
      if (!sc.hasNext()) throw new CalendarException(
              "Missing input after 'to <dateStringTTimeString>.");
      if (!sc.next().equalsIgnoreCase("with")) throw new CalendarException(
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
      if (!sc.hasNext()) throw new CalendarException(
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

  private Event checkAmbiguousEvents(List<Event> events) throws CalendarException {
    if (events.isEmpty()) { // check for no matching events
      throw new CalendarException("No events found.");
    } else if (events.size() > 1) { // check if multiple events match description
      throw new CalendarException("Error: multiple events found.");
    }
    return events.get(0);
  }
}
