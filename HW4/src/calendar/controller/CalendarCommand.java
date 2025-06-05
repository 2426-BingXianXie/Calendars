package calendar.controller;


import calendar.CalendarException;
import calendar.model.ICalendar;

/**
 * Represents a command that can be executed on the calendar model.
 * Implementations of this interface encapsulate specific calendar operations
 * (e.g., create, edit, print, show).
 */
public interface CalendarCommand {
  /**
   * Executes the calendar command.
   *
   * @param calendar The {@link ICalendar} model on which the command will operate.
   * @throws CalendarException if an error occurs during command execution.
   */
  void go(ICalendar calendar) throws CalendarException;
}