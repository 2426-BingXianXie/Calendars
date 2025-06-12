package calendar.view;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import calendar.model.IEvent;

/**
 * Represents the view interface for a calendar application using IEvent abstraction.
 * Updated to work with IEvent interface for better flexibility.
 */
public interface ICalendarView {

  /**
   * Displays the main menu with available commands and instructions.
   */
  void showMenu();

  /**
   * Displays a general message to the user.
   *
   * @param message the message to display to the user
   * @throws IllegalStateException if there is an error writing the message to
   *                               the output destination
   */
  void writeMessage(String message);

  /**
   * Displays all events scheduled for a specific date.
   *
   * @param events the list of IEvent objects to display; may be empty but should not be null
   * @param date   the date for which events are being displayed
   */
  void showCalendarEvents(List<IEvent> events, LocalDate date);

  /**
   * Displays all events scheduled within a specific date and time range.
   *
   * @param start  the start of the date and time range (inclusive)
   * @param end    the end of the date and time range (exclusive)
   * @param events the list of IEvent objects to display; may be empty but should not be null
   */
  void showCalendarEventsInDateRange(LocalDateTime start, LocalDateTime end,
                                     List<IEvent> events);

  /**
   * Displays a farewell message when the application is terminating.
   */
  void farewellMessage();
}