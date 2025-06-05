package calendar.controller;

import calendar.CalendarException;

/**
 * This interface defines the contract for calendar controllers.
 * It provides a method to start the calendar application's main loop or primary action.
 */
public interface ICalendarController {
  /**
   * Starts the execution of the calendar application.
   * This method is typically the main entry point for the controller's logic.
   *
   * @throws CalendarException if an unrecoverable error occurs during the application's execution.
   */
  void go() throws CalendarException;
}
