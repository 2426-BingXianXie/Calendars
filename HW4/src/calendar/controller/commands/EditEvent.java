package calendar.controller.commands;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import calendar.CalendarException;
import calendar.model.Event;
import calendar.model.ICalendar;
import calendar.model.ICalendarSystem;
import calendar.model.IEvent;
import calendar.model.Property;
import calendar.view.ICalendarView;

/**
 * Represents the "edit event" command for the calendar application.
 * This command handles editing properties of both single events and event series.
 */
public class EditEvent extends AbstractCommand {

  /**
   * Constructs an {@code Edit} command.
   *
   * @param sc   The {@link Scanner} from which command arguments are read.
   * @param view The {@link ICalendarView} used for displaying messages to the user.
   */
  public EditEvent(Scanner sc, ICalendarView view) {
    super(sc, view);
  }

  /**
   * Executes the "edit" command to modify event properties.
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
    if (!sc.hasNext()) {
      throw new CalendarException("Missing 'event' keyword after 'create'.");
    }
    String next = sc.next();

    if (next.equalsIgnoreCase("event")) {
      handleEditEvent(sc, model, false, false);
    } else if (next.equalsIgnoreCase("events")) {
      handleEditEvent(sc, model, false, true);
    } else if (next.equalsIgnoreCase("series")) {
      handleEditEvent(sc, model, true, true);
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
   * @param isSeries   True if this is a series operation, false for single event.
   * @throws CalendarException if there are missing or invalid inputs.
   */
  private void handleEditEvent(Scanner sc, ICalendar model, boolean fullSeries, boolean isSeries)
          throws CalendarException {
    Property property = checkValidProperty(sc);
    String subject = checkName(sc, "from");

    if (subject.isEmpty()) {
      throw new CalendarException("Missing event subject");
    }

    if (!sc.hasNext()) {
      throw new CalendarException("Incomplete command, missing <dateStringTtimeString>.");
    }

    handleEditFromVariants(sc, model, subject, property, fullSeries, isSeries);
  }

  /**
   * Validates and parses the {@link Property} to be edited from the scanner.
   *
   * @param sc The {@link Scanner} to read the property string.
   * @return The parsed {@link Property} enum.
   * @throws CalendarException if the property string is missing or invalid.
   */
  private Property checkValidProperty(Scanner sc) throws CalendarException {
    if (!sc.hasNext()) {
      throw new CalendarException("Missing event property.");
    }
    return Property.fromStr(sc.next());
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
   * @param isSeries       A boolean indicating if this is a series edit operation.
   * @throws CalendarException if there are missing or invalid inputs, or if multiple
   *                           events match the description.
   */
  private void handleEditFromVariants(Scanner sc, ICalendar model, String subject,
                                      Property property, boolean editFullSeries, boolean isSeries)
          throws CalendarException {
    LocalDateTime fromDate = parseDateTime(sc);
    String nextKeyword = getNextKeyword(sc);

    EditCommandContext context = new EditCommandContext(subject, property, fromDate,
            editFullSeries, isSeries);

    if (isTimedEventEdit(nextKeyword, isSeries)) {
      handleTimedEventEdit(sc, model, context, nextKeyword);
    } else if (isSeriesEdit(nextKeyword)) {
      handleDirectSeriesEdit(sc, model, context);
    } else {
      throw new CalendarException("Expected 'to' or 'with' after from <dateStringTTimeString>.");
    }
  }

  /**
   * Gets the next keyword from the scanner with validation.
   *
   * @param sc the scanner to read from
   * @return the next keyword
   * @throws CalendarException if no keyword is found
   */
  private String getNextKeyword(Scanner sc) throws CalendarException {
    if (!sc.hasNext()) {
      throw new CalendarException("Missing 'to' after from <dateStringTTimeString>.");
    }
    return sc.next();
  }

  /**
   * Determines if this is a timed event edit (single event with start and end times).
   *
   * @param keyword the keyword found after the from date
   * @param isSeries whether this is a series operation
   * @return true if this is a timed event edit
   */
  private boolean isTimedEventEdit(String keyword, boolean isSeries) {
    return keyword.equalsIgnoreCase("to") && !isSeries;
  }

  /**
   * Determines if this is a direct series edit (no end time specified).
   *
   * @param keyword the keyword found after the from date
   * @return true if this is a direct series edit
   */
  private boolean isSeriesEdit(String keyword) {
    return keyword.equalsIgnoreCase("with");
  }

  /**
   * Handles editing a single timed event with specific start and end times.
   *
   * @param sc the scanner for further input
   * @param model the calendar model
   * @param context the edit command context
   * @param keyword the keyword that was found (should be "to")
   * @throws CalendarException if validation fails or event is not found
   */
  private void handleTimedEventEdit(Scanner sc, ICalendar model, EditCommandContext context,
                                    String keyword) throws CalendarException {
    LocalDateTime toDate = parseDateTime(sc);
    validateWithKeyword(sc);
    String newProperty = getNewPropertyValue(sc);

    List<IEvent> events = model.getEventsByDetails(context.subject, context.fromDate, toDate);
    IEvent event = validateSingleEventFound(events);

    editSingleEvent(model, event, context.property, newProperty);
    displayEditConfirmation(context.subject, context.property, newProperty);
  }

  /**
   * Handles editing a series or event without specifying an end time.
   *
   * @param sc the scanner for further input
   * @param model the calendar model
   * @param context the edit command context
   * @throws CalendarException if validation fails or event is not found
   */
  private void handleDirectSeriesEdit(Scanner sc, ICalendar model, EditCommandContext context)
          throws CalendarException {
    String newProperty = getNewPropertyValue(sc);

    List<IEvent> events = model.getEventsBySubjectAndStartTime(context.subject, context.fromDate);
    IEvent event = validateSingleEventFound(events);

    if (isNotPartOfSeries(event)) {
      editSingleEvent(model, event, context.property, newProperty);
      displayEditConfirmation(context.subject, context.property, newProperty);
    } else {
      editEventSeries(model, event, context, newProperty);
    }
  }

  /**
   * Validates that the "with" keyword is present.
   *
   * @param sc the scanner to read from
   * @throws CalendarException if "with" keyword is missing
   */
  private void validateWithKeyword(Scanner sc) throws CalendarException {
    if (!sc.hasNext()) {
      throw new CalendarException("Missing input after 'to <dateStringTTimeString>.");
    }
    if (!sc.next().equalsIgnoreCase("with")) {
      throw new CalendarException("Expected 'with' after 'to <dateStringTTimeString>.");
    }
  }

  /**
   * Gets the new property value from the scanner.
   *
   * @param sc the scanner to read from
   * @return the new property value
   * @throws CalendarException if no value is provided
   */
  private String getNewPropertyValue(Scanner sc) throws CalendarException {
    if (!sc.hasNext()) {
      throw new CalendarException("Missing <NewPropertyValue>.");
    }
    return sc.next();
  }

  /**
   * Validates that exactly one event was found matching the criteria.
   *
   * @param events the list of events found
   * @return the single event found
   * @throws CalendarException if no events or multiple events are found
   */
  private IEvent validateSingleEventFound(List<IEvent> events) throws CalendarException {
    if (events.isEmpty()) {
      throw new CalendarException("No events found.");
    } else if (events.size() > 1) {
      throw new CalendarException("Error: multiple events found.");
    }
    return events.get(0);
  }

  /**
   * Checks if an event is not part of a series.
   *
   * @param event the event to check
   * @return true if the event is not part of a series
   */
  private boolean isNotPartOfSeries(IEvent event) {
    return event.getSeriesID() == null;
  }

  /**
   * Edits a single event.
   *
   * @param model the calendar model
   * @param event the event to edit
   * @param property the property to change
   * @param newProperty the new property value
   * @throws CalendarException if the edit fails
   */
  private void editSingleEvent(ICalendar model, IEvent event, Property property, String newProperty)
          throws CalendarException {
    model.editEvent(event.getId(), property, newProperty);
  }

  /**
   * Handles editing an event series based on the edit context.
   *
   * @param model the calendar model
   * @param event the event that is part of the series
   * @param context the edit command context
   * @param newProperty the new property value
   * @throws CalendarException if the edit fails
   */
  private void editEventSeries(ICalendar model, IEvent event, EditCommandContext context,
                               String newProperty) throws CalendarException {
    if (context.editFullSeries) {
      model.editSeries(event.getSeriesID(), context.property, newProperty);
      displaySeriesEditConfirmation(context.subject, context.property, newProperty, true);
    } else {
      model.editSeriesFromDate(event.getSeriesID(), context.property, newProperty);
      displaySeriesEditConfirmation(context.subject, context.property, newProperty, false);
    }
  }

  /**
   * Displays confirmation message for single event edits.
   *
   * @param subject the event subject
   * @param property the property that was changed
   * @param newProperty the new property value
   */
  private void displayEditConfirmation(String subject, Property property, String newProperty) {
    view.writeMessage("Edited event '" + subject + "' " + property.getStr() +
            " property to " + newProperty + System.lineSeparator());
  }

  /**
   * Displays confirmation message for series edits.
   *
   * @param subject the series subject
   * @param property the property that was changed
   * @param newProperty the new property value
   * @param isFullSeries whether the entire series was edited
   */
  private void displaySeriesEditConfirmation(String subject, Property property, String newProperty,
                                             boolean isFullSeries) {
    String scope = isFullSeries ? "" : " from " + System.lineSeparator();
    view.writeMessage("Edited event series '" + subject + "' " + property.getStr() +
            " property to " + newProperty + scope + System.lineSeparator());
  }

  /**
   * Context class to hold edit command parameters.
   * This class encapsulates the parameters needed for edit operations,
   * reducing method parameter complexity and improving code organization.
   */
  private static class EditCommandContext {
    final String subject;
    final Property property;
    final LocalDateTime fromDate;
    final boolean editFullSeries;
    final boolean isSeries;

    /**
     * Constructs an EditCommandContext with the specified parameters.
     *
     * @param subject the subject of the event or series being edited
     * @param property the property being modified
     * @param fromDate the start date/time of the event
     * @param editFullSeries whether to edit the entire series
     * @param isSeries whether this is a series operation
     */
    EditCommandContext(String subject, Property property, LocalDateTime fromDate,
                       boolean editFullSeries, boolean isSeries) {
      this.subject = subject;
      this.property = property;
      this.fromDate = fromDate;
      this.editFullSeries = editFullSeries;
      this.isSeries = isSeries;
    }
  }
}