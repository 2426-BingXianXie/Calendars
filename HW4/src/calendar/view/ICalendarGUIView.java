package calendar.view;

import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.DefaultListModel;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import calendar.model.IEvent;

/**
 * Interface for the GUI view component of the calendar application.
 * Defines the contract for GUI operations while maintaining separation
 * between the view implementation and the controller.
 *
 * <p>This interface follows the MVC pattern by providing abstraction
 * over the concrete GUI implementation, allowing for different
 * GUI implementations or testing with mock views.
 */
public interface ICalendarGUIView {

  /**
   * Creates and displays the main application window.
   *
   * @param windowTitle the title for the main window
   * @return the created main frame
   */
  JFrame createMainWindow(String windowTitle);

  /**
   * Creates a modal dialog with standard layout.
   *
   * @param parent the parent component
   * @param title  the dialog title
   * @param width  the dialog width
   * @param height the dialog height
   * @return configured dialog
   */
  JDialog createDialog(Component parent, String title, int width, int height);

  /**
   * Creates a form panel with standard layout and styling.
   *
   * @return configured form panel with GridBagLayout
   */
  JPanel createFormPanel();

  /**
   * Creates a button panel with standard flow layout.
   *
   * @return configured button panel
   */
  JPanel createButtonPanel();

  /**
   * Creates a styled button with consistent appearance.
   *
   * @param text the button text
   * @return configured button
   */
  JButton createStyledButton(String text);

  /**
   * Creates a styled text field with consistent appearance.
   *
   * @param text    the initial text
   * @param columns the number of columns
   * @return configured text field
   */
  JTextField createStyledTextField(String text, int columns);

  /**
   * Creates a monospace text field for dates and times.
   *
   * @param text    the initial text
   * @param columns the number of columns
   * @return configured monospace text field
   */
  JTextField createMonospaceTextField(String text, int columns);

  /**
   * Creates a combo box with standard styling.
   *
   * @param items the items for the combo box
   * @return configured combo box
   */
  JComboBox<String> createStyledComboBox(String[] items);

  /**
   * Gets the main application frame.
   *
   * @return the main frame
   */
  JFrame getMainFrame();

  /**
   * Gets the current date label component.
   *
   * @return the current date label
   */
  JLabel getCurrentDateLabel();

  /**
   * Gets the calendar information label component.
   *
   * @return the calendar info label
   */
  JLabel getCalendarInfoLabel();

  /**
   * Gets the events list component.
   *
   * @return the events list
   */
  JList<String> getEventsList();

  /**
   * Gets the events list model for direct manipulation.
   *
   * @return the events list model
   */
  DefaultListModel<String> getEventsListModel();

  /**
   * Gets the calendar selector combo box.
   *
   * @return the calendar selector
   */
  JComboBox<String> getCalendarSelector();

  /**
   * Gets the previous week navigation button.
   *
   * @return the previous week button
   */
  JButton getPrevWeekButton();

  /**
   * Gets the next week navigation button.
   *
   * @return the next week button
   */
  JButton getNextWeekButton();

  /**
   * Gets the go to date button.
   *
   * @return the go to date button
   */
  JButton getGoToDateButton();

  /**
   * Gets the create event button.
   *
   * @return the create event button
   */
  JButton getCreateEventButton();

  /**
   * Gets the create series button.
   *
   * @return the create series button
   */
  JButton getCreateSeriesButton();

  /**
   * Gets the refresh button.
   *
   * @return the refresh button
   */
  JButton getRefreshButton();

  /**
   * Gets the show status button.
   *
   * @return the show status button
   */
  JButton getShowStatusButton();

  /**
   * Gets the search events button.
   *
   * @return the search events button
   */
  JButton getSearchEventsButton();

  /**
   * Gets the new calendar button.
   *
   * @return the new calendar button
   */
  JButton getNewCalendarButton();

  /**
   * Adds an action listener to the calendar selector.
   *
   * @param listener the action listener to add
   */
  void addCalendarSelectorListener(ActionListener listener);

  /**
   * Adds a mouse listener to the events list.
   *
   * @param listener the mouse listener to add
   */
  void addEventsListMouseListener(MouseListener listener);

  /**
   * Updates the calendar selector with available calendars.
   *
   * @param calendarNames   list of calendar names
   * @param currentCalendar the currently selected calendar
   */
  void updateCalendarSelector(List<String> calendarNames, String currentCalendar);


  /**
   * Updates the calendar information display.
   *
   * @param calendarName the calendar name
   * @param timezone     the calendar timezone
   */
  void updateCalendarInfo(String calendarName, String timezone);

  /**
   * Updates the current date label with date range.
   *
   * @param startDate the start date
   * @param endDate   the end date
   */
  void updateCurrentDateLabel(LocalDate startDate, LocalDate endDate);

  /**
   * Shows an error message dialog.
   *
   * @param parent  the parent component
   * @param title   the dialog title
   * @param message the error message
   */
  void showErrorDialog(Component parent, String title, String message);

  /**
   * Shows an information message dialog.
   *
   * @param parent  the parent component
   * @param message the information message
   */
  void showInfoDialog(Component parent, String message);

  /**
   * Shows a confirmation dialog.
   *
   * @param parent  the parent component
   * @param message the confirmation message
   * @param title   the dialog title
   * @return true if user confirms, false otherwise
   */
  boolean showConfirmDialog(Component parent, String message, String title);

  /**
   * Shows an input dialog for text input.
   *
   * @param parent       the parent component
   * @param message      the prompt message
   * @param initialValue the initial value
   * @return the user input, or null if cancelled
   */
  String showInputDialog(Component parent, String message, String initialValue);

  /**
   * Formats an event for display in the schedule list.
   *
   * @param event the event to format
   * @param index the display index
   * @return formatted event string
   */
  String formatEventForDisplay(IEvent event, int index);

  /**
   * Formats a search result event for display.
   *
   * @param event the event to format
   * @param index the display index
   * @return formatted search result string
   */
  String formatSearchResultForDisplay(IEvent event, int index);

  /**
   * Gets the date formatter for consistent date formatting.
   *
   * @return the date formatter
   */
  DateTimeFormatter getDateFormatter();

  /**
   * Gets the display date formatter for user-friendly date display.
   *
   * @return the display date formatter
   */
  DateTimeFormatter getDisplayDateFormatter();
}