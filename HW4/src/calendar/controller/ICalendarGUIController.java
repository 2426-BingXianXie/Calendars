package calendar.controller;

import javax.swing.*;

import calendar.model.IEvent;


/**
 * Interface for the GUI controller component of the calendar application.
 * Extends the base calendar controller with GUI-specific action handlers
 * for managing calendars and events through the graphical user interface.
 */
public interface ICalendarGUIController extends ICalendarController {

  /**
   * Handles the action of creating a new calendar.
   *
   * @param name        the name for the new calendar
   * @param timezoneStr the timezone string for the new calendar
   */
  void handleCreateCalendarAction(String name, String timezoneStr);

  /**
   * Handles the action of switching to use a different calendar.
   *
   * @param calendarName the name of the calendar to switch to
   */
  void handleUseCalendarAction(String calendarName);

  /**
   * Handles the action of creating a new event from form data.
   *
   * @param dialog the dialog containing the event form
   * @param fields the form fields containing event data
   */
  void handleCreateEventAction(JDialog dialog, EventFormFields fields);

  /**
   * Handles the action of editing an existing event with updated form data.
   *
   * @param dialog        the dialog containing the event form
   * @param selectedEvent the event being edited
   * @param fields        the form fields containing updated event data
   */
  void handleEditEventAction(JDialog dialog, IEvent selectedEvent, EventFormFields fields);
}
