package calendar.view;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import calendar.model.Event;

/**
 * Represents the view interface for a calendar application.
 *
 * This interface defines methods for displaying calendar information to the user,
 * including events, menus, messages, and application status. Implementations of this
 * interface handle the presentation layer of the calendar application and can target
 * different output destinations such as console, GUI, or files.
 */
public interface ICalendarView {

  /**
   * Displays the main menu with available commands and instructions.
   *
   * The menu should include all supported user commands such as creating events,
   * editing events, printing schedules, and checking availability status. This method
   * is typically called when the application starts or when the user requests help.
   */
  void showMenu();

  /**
   * Displays a general message to the user.
   *
   * This method is used for displaying various types of feedback including
   * success confirmations, error messages, status updates, and general information.
   * The message is displayed as-is without additional formatting.
   *
   * @param message the message to display to the user
   * @throws IllegalStateException if there is an error writing the message to 
   *                              the output destination
   */
  void writeMessage(String message);

  /**
   * Displays all events scheduled for a specific date.
   *
   * This method presents a formatted list of events occurring on the specified date,
   * including event details such as subject, start time, end time, and location if
   * available. If no events are found for the date, an appropriate message is displayed.
   *
   * @param events the list of events to display; may be empty but should not be null
   * @param date   the date for which events are being displayed
   */
  void showCalendarEvents(List<Event> events, LocalDate date);

  /**
   * Displays all events scheduled within a specific date and time range.
   *
   * This method presents a formatted list of events occurring between the specified
   * start and end times, including event details such as subject, start time, end time,
   * and location if available. Events that partially overlap with the range are included.
   * If no events are found in the range, an appropriate message is displayed.
   *
   * @param start  the start of the date and time range (inclusive)
   * @param end    the end of the date and time range (exclusive)
   * @param events the list of events to display; may be empty but should not be null
   */
  void showCalendarEventsInDateRange(LocalDateTime start, LocalDateTime end,
                                     List<Event> events);

  /**
   * Displays a farewell message when the application is terminating.
   *
   * This method is called when the user exits the application and should display
   * an appropriate goodbye message. It provides closure to the user session and
   * confirms that the application is shutting down properly.
   */
  void farewellMessage();
}