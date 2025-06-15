package calendar.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import calendar.model.IEvent;

/**
 * GUI View implementation for the calendar application following MVC principles.
 * This class handles all GUI component creation, layout, and basic display logic,
 * while delegating user interaction handling to the controller.
 */
public class CalendarGUIView {
  private JFrame mainFrame;
  private JLabel currentDateLabel;
  private JLabel calendarInfoLabel;
  private JList<String> eventsList;
  private DefaultListModel<String> eventsListModel;
  private JButton prevWeekButton;
  private JButton nextWeekButton;
  private JButton goToDateButton;
  private JButton createEventButton;
  private JButton createSeriesButton;
  private JButton refreshButton;
  private JButton showStatusButton;
  private JButton searchEventsButton;
  private JButton newCalendarButton;
  private JComboBox<String> calendarSelector;

  // Constants for consistent styling
  private static final Font TITLE_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 16);
  private static final Font LABEL_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
  private static final Font MONOSPACE_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");

  /**
   * Creates and displays the main GUI window.
   * Sets up the entire user interface but delegates event handling to controller.
   *
   * @param windowTitle the title for the main window
   * @return the created main frame
   */
  public JFrame createMainWindow(String windowTitle) {
    mainFrame = new JFrame(windowTitle);
    mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    mainFrame.setSize(1500, 700);
    mainFrame.setMinimumSize(new Dimension(700, 500));

    setupLookAndFeel();

    // Create main layout
    mainFrame.setLayout(new BorderLayout(10, 10));

    // Add padding to the main frame
    JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
    contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

    // Create all panels
    contentPanel.add(createHeaderPanel(), BorderLayout.NORTH);
    contentPanel.add(createMainContentPanel(), BorderLayout.CENTER);
    contentPanel.add(createFooterPanel(), BorderLayout.SOUTH);

    mainFrame.add(contentPanel);
    mainFrame.setLocationRelativeTo(null);

    return mainFrame;
  }

  /**
   * Sets up the system look and feel for better native appearance.
   */
  private void setupLookAndFeel() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
      System.err.println("Could not set system look and feel: " + e.getMessage());
    }
  }

  /**
   * Creates the header panel with calendar management and navigation controls.
   *
   * @return configured header panel
   */
  private JPanel createHeaderPanel() {
    JPanel headerPanel = new JPanel(new BorderLayout());
    headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createRaisedBevelBorder(),
            new EmptyBorder(10, 15, 10, 15)
    ));

    headerPanel.add(createCalendarManagementPanel(), BorderLayout.WEST);
    headerPanel.add(createNavigationPanel(), BorderLayout.CENTER);

    return headerPanel;
  }

  /**
   * Creates the calendar management panel with selector and creation button.
   *
   * @return configured calendar management panel
   */
  private JPanel createCalendarManagementPanel() {
    JPanel managementPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

    // Calendar info label
    calendarInfoLabel = new JLabel();
    calendarInfoLabel.setFont(TITLE_FONT);
    calendarInfoLabel.setForeground(new Color(0, 100, 0));
    managementPanel.add(calendarInfoLabel);

    // Calendar selector dropdown
    calendarSelector = new JComboBox<>();
    calendarSelector.setPreferredSize(new Dimension(150, 30));
    calendarSelector.setToolTipText("Select active calendar");
    managementPanel.add(calendarSelector);

    // Create new calendar button
    newCalendarButton = createStyledButton("New Calendar");
    newCalendarButton.setPreferredSize(new Dimension(120, 30));
    newCalendarButton.setToolTipText("Create a new calendar");
    managementPanel.add(newCalendarButton);

    return managementPanel;
  }

  /**
   * Creates the date navigation panel with week navigation and date picker.
   *
   * @return configured navigation panel
   */
  private JPanel createNavigationPanel() {
    JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));

    // Previous week button
    prevWeekButton = createNavigationButton("◄ Previous Week");
    prevWeekButton.setToolTipText("Go back one week");
    prevWeekButton.setBackground(new Color(240, 240, 240));
    navPanel.add(prevWeekButton);

    // Current date display
    currentDateLabel = new JLabel();
    currentDateLabel.setFont(LABEL_FONT);
    currentDateLabel.setHorizontalAlignment(SwingConstants.CENTER);
    currentDateLabel.setPreferredSize(new Dimension(250, 30));
    currentDateLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLoweredBevelBorder(),
            new EmptyBorder(5, 15, 5, 15)
    ));
    currentDateLabel.setOpaque(true);
    currentDateLabel.setBackground(Color.WHITE);
    navPanel.add(currentDateLabel);

    // Next week button
    nextWeekButton = createNavigationButton("Next Week ►");
    nextWeekButton.setToolTipText("Go forward one week");
    nextWeekButton.setBackground(new Color(240, 240, 240));
    navPanel.add(nextWeekButton);

    // Separator
    navPanel.add(Box.createHorizontalStrut(20));

    // Go to date button
    goToDateButton = createStyledButton("Go to Date...");
    goToDateButton.setToolTipText("Jump to a specific date");
    goToDateButton.setBackground(new Color(220, 220, 255));
    navPanel.add(goToDateButton);

    return navPanel;
  }

  /**
   * Creates the main content panel with the events list.
   *
   * @return configured main content panel
   */
  private JPanel createMainContentPanel() {
    JPanel contentPanel = new JPanel(new BorderLayout(0, 10));

    // Title panel
    JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JLabel titleLabel = new JLabel("Schedule View (Up to 10 Events)");
    titleLabel.setFont(TITLE_FONT);
    titlePanel.add(titleLabel);
    contentPanel.add(titlePanel, BorderLayout.NORTH);

    // Events list
    eventsListModel = new DefaultListModel<>();
    eventsList = new JList<>(eventsListModel);
    eventsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    eventsList.setFont(MONOSPACE_FONT);
    eventsList.setCellRenderer(new EventListCellRenderer());

    // Scroll pane for the events list
    JScrollPane scrollPane = new JScrollPane(eventsList);
    scrollPane.setPreferredSize(new Dimension(800, 350));
    scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Events"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
    ));

    contentPanel.add(scrollPane, BorderLayout.CENTER);

    // Status panel
    JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JLabel statusLabel = new JLabel("Tip: Double-click to view details | Right-click to edit events");
    statusLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 11));
    statusLabel.setForeground(Color.GRAY);
    statusPanel.add(statusLabel);
    contentPanel.add(statusPanel, BorderLayout.SOUTH);

    return contentPanel;
  }

  /**
   * Creates the footer panel with action buttons.
   *
   * @return configured footer panel
   */
  private JPanel createFooterPanel() {
    JPanel footerPanel = new JPanel(new BorderLayout());
    footerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createRaisedBevelBorder(),
            new EmptyBorder(10, 15, 10, 15)
    ));

    // Action buttons on the left
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

    createEventButton = createStyledButton("Create Event");
    createEventButton.setToolTipText("Create a new event");
    buttonPanel.add(createEventButton);

    createSeriesButton = createStyledButton("Create Series");
    createSeriesButton.setToolTipText("Create a recurring event series");
    buttonPanel.add(createSeriesButton);

    refreshButton = createStyledButton("Refresh");
    refreshButton.setToolTipText("Refresh the event list");
    buttonPanel.add(refreshButton);

    showStatusButton = createStyledButton("Check Status");
    showStatusButton.setToolTipText("Check if you're busy at a specific time");
    buttonPanel.add(showStatusButton);

    searchEventsButton = createStyledButton("Search Events");
    searchEventsButton.setToolTipText("Search for next 10 events from a specific date");
    buttonPanel.add(searchEventsButton);

    footerPanel.add(buttonPanel, BorderLayout.WEST);

    // Status info on the right
    JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JLabel infoLabel = new JLabel("Calendar Management System");
    infoLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 11));
    infoLabel.setForeground(Color.DARK_GRAY);
    infoPanel.add(infoLabel);
    footerPanel.add(infoPanel, BorderLayout.EAST);

    return footerPanel;
  }

  /**
   * Creates a styled button with consistent appearance.
   *
   * @param text the button text
   * @return configured button
   */
  public JButton createStyledButton(String text) {
    JButton button = new JButton(text);
    button.setFont(LABEL_FONT);
    button.setPreferredSize(new Dimension(140, 35));
    button.setFocusPainted(false);
    return button;
  }

  /**
   * Creates a styled navigation button with distinct appearance.
   *
   * @param text the button text
   * @return configured navigation button
   */
  private JButton createNavigationButton(String text) {
    JButton button = new JButton(text);
    button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
    button.setPreferredSize(new Dimension(150, 35));
    button.setFocusPainted(false);
    button.setBorder(BorderFactory.createRaisedBevelBorder());
    return button;
  }

  /**
   * Creates a dialog with standard layout and styling.
   *
   * @param parent the parent component
   * @param title the dialog title
   * @param width the dialog width
   * @param height the dialog height
   * @return configured dialog
   */
  public JDialog createDialog(Component parent, String title, int width, int height) {
    JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(parent), title, true);
    dialog.setSize(width, height);
    dialog.setLayout(new BorderLayout(10, 10));
    dialog.setLocationRelativeTo(parent);
    return dialog;
  }

  /**
   * Creates a form panel with GridBagLayout and standard styling.
   *
   * @return configured form panel
   */
  public JPanel createFormPanel() {
    JPanel formPanel = new JPanel(new GridBagLayout());
    formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
    return formPanel;
  }

  /**
   * Creates a button panel with standard flow layout.
   *
   * @return configured button panel
   */
  public JPanel createButtonPanel() {
    return new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
  }

  /**
   * Creates a titled text field with consistent styling.
   *
   * @param text the initial text
   * @param columns the number of columns
   * @return configured text field
   */
  public JTextField createStyledTextField(String text, int columns) {
    JTextField field = new JTextField(text, columns);
    field.setFont(LABEL_FONT);
    return field;
  }

  /**
   * Creates a monospace text field for dates and times.
   *
   * @param text the initial text
   * @param columns the number of columns
   * @return configured monospace text field
   */
  public JTextField createMonospaceTextField(String text, int columns) {
    JTextField field = new JTextField(text, columns);
    field.setFont(MONOSPACE_FONT);
    return field;
  }

  /**
   * Creates a combo box with standard styling.
   *
   * @param items the items for the combo box
   * @return configured combo box
   */
  public JComboBox<String> createStyledComboBox(String[] items) {
    JComboBox<String> comboBox = new JComboBox<>(items);
    comboBox.setFont(LABEL_FONT);
    return comboBox;
  }

  // Getters for components that need external access
  public JFrame getMainFrame() { return mainFrame; }
  public JLabel getCurrentDateLabel() { return currentDateLabel; }
  public JLabel getCalendarInfoLabel() { return calendarInfoLabel; }
  public JList<String> getEventsList() { return eventsList; }
  public DefaultListModel<String> getEventsListModel() { return eventsListModel; }
  public JComboBox<String> getCalendarSelector() { return calendarSelector; }

  // Button getters for controller to add listeners
  public JButton getPrevWeekButton() { return prevWeekButton; }
  public JButton getNextWeekButton() { return nextWeekButton; }
  public JButton getGoToDateButton() { return goToDateButton; }
  public JButton getCreateEventButton() { return createEventButton; }
  public JButton getCreateSeriesButton() { return createSeriesButton; }
  public JButton getRefreshButton() { return refreshButton; }
  public JButton getShowStatusButton() { return showStatusButton; }
  public JButton getSearchEventsButton() { return searchEventsButton; }
  public JButton getNewCalendarButton() { return newCalendarButton; }

  /**
   * Adds action listener to calendar selector.
   *
   * @param listener the action listener
   */
  public void addCalendarSelectorListener(ActionListener listener) {
    calendarSelector.addActionListener(listener);
  }

  /**
   * Adds mouse listener to events list.
   *
   * @param listener the mouse listener
   */
  public void addEventsListMouseListener(MouseListener listener) {
    eventsList.addMouseListener(listener);
  }

  /**
   * Updates the calendar selector with available calendars.
   *
   * @param calendarNames list of calendar names
   * @param currentCalendar the currently selected calendar
   */
  public void updateCalendarSelector(List<String> calendarNames, String currentCalendar) {
    // Temporarily remove listeners to avoid triggering events during update
    ActionListener[] listeners = calendarSelector.getActionListeners();
    for (ActionListener listener : listeners) {
      calendarSelector.removeActionListener(listener);
    }

    calendarSelector.removeAllItems();
    for (String name : calendarNames) {
      calendarSelector.addItem(name);
    }

    if (currentCalendar != null) {
      calendarSelector.setSelectedItem(currentCalendar);
    }

    // Re-add listeners
    for (ActionListener listener : listeners) {
      calendarSelector.addActionListener(listener);
    }
  }

  /**
   * Updates the calendar information display.
   *
   * @param calendarName the calendar name
   * @param timezone the calendar timezone
   */
  public void updateCalendarInfo(String calendarName, String timezone) {
    if (calendarName != null && timezone != null) {
      calendarInfoLabel.setText("Current: " + calendarName + " (" + timezone + ")");
    } else {
      calendarInfoLabel.setText("Calendar Information Unavailable");
    }
  }

  /**
   * Updates the current date label with date range.
   *
   * @param startDate the start date
   * @param endDate the end date
   */
  public void updateCurrentDateLabel(LocalDate startDate, LocalDate endDate) {
    currentDateLabel.setText(startDate.format(DISPLAY_DATE_FORMATTER) +
            " -> " + endDate.format(DateTimeFormatter.ofPattern("MMM d")));
  }

  /**
   * Shows an error message dialog.
   *
   * @param parent the parent component
   * @param title the dialog title
   * @param message the error message
   */
  public void showErrorDialog(Component parent, String title, String message) {
    JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
  }

  /**
   * Shows an information message dialog.
   *
   * @param parent the parent component
   * @param message the information message
   */
  public void showInfoDialog(Component parent, String message) {
    JOptionPane.showMessageDialog(parent, message, "Information", JOptionPane.INFORMATION_MESSAGE);
  }

  /**
   * Shows a confirmation dialog.
   *
   * @param parent the parent component
   * @param message the confirmation message
   * @param title the dialog title
   * @return true if user confirms, false otherwise
   */
  public boolean showConfirmDialog(Component parent, String message, String title) {
    int result = JOptionPane.showConfirmDialog(parent, message, title,
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
    return result == JOptionPane.YES_OPTION;
  }

  /**
   * Shows an input dialog for text input.
   *
   * @param parent the parent component
   * @param message the prompt message
   * @param initialValue the initial value
   * @return the user input, or null if cancelled
   */
  public String showInputDialog(Component parent, String message, String initialValue) {
    return JOptionPane.showInputDialog(parent, message, initialValue);
  }

  /**
   * Formats an event for display in the list.
   *
   * @param event the event to format
   * @param index the display index
   * @return formatted event string
   */
  public String formatEventForDisplay(IEvent event, int index) {
    StringBuilder sb = new StringBuilder();

    // Event number
    sb.append(String.format("%2d. ", index));

    // Date with day of week
    LocalDate eventDate = event.getStart().toLocalDate();
    sb.append(eventDate.format(DateTimeFormatter.ofPattern("EEE MM/dd")));
    sb.append(" ");

    // Time range
    sb.append(event.getStart().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
    sb.append("-");
    sb.append(event.getEnd().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
    sb.append(" | ");

    // Subject
    sb.append(event.getSubject());

    // Location if available
    if (event.getLocation() != null && !event.getLocationDisplay().isEmpty()) {
      sb.append(" @ ");
      sb.append(event.getLocationDisplay());
    }

    return sb.toString();
  }

  /**
   * Formats a search result event for display.
   *
   * @param event the event to format
   * @param index the display index
   * @return formatted search result string
   */
  public String formatSearchResultForDisplay(IEvent event, int index) {
    StringBuilder sb = new StringBuilder();

    // Event number
    sb.append(String.format("%2d. ", index));

    // Full date with day of week
    LocalDate eventDate = event.getStart().toLocalDate();
    sb.append(eventDate.format(DateTimeFormatter.ofPattern("EEE, MMM dd, yyyy")));
    sb.append(" ");

    // Time range
    sb.append(event.getStart().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
    sb.append(" - ");
    sb.append(event.getEnd().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
    sb.append(" | ");

    // Subject
    sb.append(event.getSubject());

    // Location if available
    if (event.getLocation() != null && !event.getLocationDisplay().isEmpty()) {
      sb.append(" @ ");
      sb.append(event.getLocationDisplay());
    }

    return sb.toString();
  }

  /**
   * Gets the date formatter for consistent date formatting.
   *
   * @return the date formatter
   */
  public DateTimeFormatter getDateFormatter() {
    return DATE_FORMATTER;
  }

  /**
   * Gets the display date formatter for user-friendly date display.
   *
   * @return the display date formatter
   */
  public DateTimeFormatter getDisplayDateFormatter() {
    return DISPLAY_DATE_FORMATTER;
  }

  /**
   * Custom cell renderer for the events list with color coding.
   */
  private static class EventListCellRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
      super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

      String text = value.toString();

      // Different colors for different types of entries
      if (text.startsWith("No") || text.startsWith("Error")) {
        setForeground(isSelected ? Color.WHITE : Color.RED);
      } else if (text.contains("Click 'Create Event'")) {
        setForeground(isSelected ? Color.WHITE : Color.GRAY);
      } else if (text.startsWith("...")) {
        setForeground(isSelected ? Color.WHITE : Color.BLUE);
      } else if (text.matches("^\\s*\\d+\\..*")) {
        // Regular event entries
        setForeground(isSelected ? Color.WHITE : Color.BLACK);
      }

      // Set background colors
      if (isSelected) {
        setBackground(new Color(51, 153, 255));
      } else {
        setBackground(Color.WHITE);
      }

      return this;
    }
  }
}