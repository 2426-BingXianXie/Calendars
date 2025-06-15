package calendar.controller;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

import calendar.CalendarException;
import calendar.model.ICalendar;
import calendar.model.ICalendarSystem;
import calendar.model.IEvent;
import calendar.model.Location;
import calendar.model.EventStatus;
import calendar.view.CalendarGUIView;

/**
 * GUI Controller for the calendar application following MVC principles.
 * Handles user interactions and coordinates between the model and view.
 * Maintains clean separation of concerns with proper delegation to view layer.
 */
public class CalendarGUIController implements ICalendarController {
  private final ICalendarSystem calendarSystem;
  private final CalendarGUIView view;
  private LocalDate currentStartDate;

  // Constants
  private static final int MAX_EVENTS_DISPLAY = 10;
  private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  /**
   * Constructs a new CalendarGUIController with the specified calendar system.
   *
   * @param calendarSystem the calendar system to manage
   */
  public CalendarGUIController(ICalendarSystem calendarSystem) {
    this.calendarSystem = calendarSystem;
    this.view = new CalendarGUIView();
    this.currentStartDate = LocalDate.now();
  }

  /**
   * Starts the GUI application by creating the view and setting up event handlers.
   */
  @Override
  public void execute() throws CalendarException {
    SwingUtilities.invokeLater(() -> {
      try {
        initializeGUI();
      } catch (Exception e) {
        view.showErrorDialog(null, "Error starting application", e.getMessage());
        System.exit(1);
      }
    });
  }

  /**
   * Initializes the GUI by creating the view and setting up all event handlers.
   */
  private void initializeGUI() {
    // Create the main window
    JFrame mainFrame = view.createMainWindow("Calendar Application");

    // Set up all event handlers
    setupEventHandlers();

    // Initialize the display
    updateCalendarInfo();
    updateScheduleView();

    // Add window listener for cleanup
    mainFrame.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });

    // Show the window
    mainFrame.setVisible(true);
  }

  /**
   * Sets up all event handlers for GUI components.
   */
  private void setupEventHandlers() {
    // Navigation buttons
    view.getPrevWeekButton().addActionListener(e -> navigateDate(-7));
    view.getNextWeekButton().addActionListener(e -> navigateDate(7));
    view.getGoToDateButton().addActionListener(e -> showDatePickerDialog());

    // Action buttons
    view.getCreateEventButton().addActionListener(e -> showCreateEventDialog());
    view.getCreateSeriesButton().addActionListener(e -> showCreateSeriesDialog());
    view.getRefreshButton().addActionListener(e -> {
      updateScheduleView();
      view.showInfoDialog(view.getMainFrame(), "Schedule refreshed successfully!");
    });
    view.getShowStatusButton().addActionListener(e -> showStatusDialog());
    view.getSearchEventsButton().addActionListener(e -> showSearchEventsDialog());

    // Calendar management
    view.getNewCalendarButton().addActionListener(e -> showCreateCalendarDialog());
    view.addCalendarSelectorListener(e -> handleCalendarSelection());

    // Events list interactions
    view.addEventsListMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
          showEventDetails();
        } else if (e.getClickCount() == 1 && SwingUtilities.isRightMouseButton(e)) {
          showEventContextMenu(e);
        }
      }
    });
  }

  /**
   * Handles calendar selection changes.
   */
  private void handleCalendarSelection() {
    String selectedCalendar = (String) view.getCalendarSelector().getSelectedItem();
    if (selectedCalendar != null && !selectedCalendar.isEmpty()) {
      try {
        String currentCalendar = calendarSystem.getCurrentCalendarName();
        if (!selectedCalendar.equals(currentCalendar)) {
          calendarSystem.useCalendar(selectedCalendar);
          updateCalendarInfo();
          updateScheduleView();
        }
      } catch (Exception ex) {
        view.showErrorDialog(view.getMainFrame(), "Error",
                "Could not switch calendar: " + ex.getMessage());
        revertCalendarSelection();
      }
    }
  }

  /**
   * Reverts calendar selection if switch fails.
   */
  private void revertCalendarSelection() {
    try {
      String currentCalendar = calendarSystem.getCurrentCalendarName();
      if (currentCalendar != null) {
        view.getCalendarSelector().setSelectedItem(currentCalendar);
      }
    } catch (Exception revertEx) {
      // Ignore revert errors
    }
  }

  /**
   * Updates the calendar information display.
   */
  private void updateCalendarInfo() {
    try {
      String calendarName = calendarSystem.getCurrentCalendarName();
      String timezone = calendarSystem.getCurrentCalendar() != null ?
              calendarSystem.getCalendarTimezone(calendarName).toString() : "Unknown";
      view.updateCalendarInfo(calendarName, timezone);

      // Update calendar selector
      List<String> calendarNames = calendarSystem.getCalendarNames();
      view.updateCalendarSelector(calendarNames, calendarName);
    } catch (Exception e) {
      view.updateCalendarInfo(null, null);
    }
  }

  /**
   * Navigates the current date by the specified number of days.
   */
  private void navigateDate(int days) {
    currentStartDate = currentStartDate.plusDays(days);
    updateScheduleView();
  }

  /**
   * Updates the schedule view with events from the current start date.
   */
  private void updateScheduleView() {
    // Update date label
    LocalDate endDate = currentStartDate.plusDays(6);
    view.updateCurrentDateLabel(currentStartDate, endDate);

    // Clear existing events
    view.getEventsListModel().clear();

    try {
      ICalendar calendar = calendarSystem.getCurrentCalendar();
      if (calendar == null) {
        view.getEventsListModel().addElement("No calendar in use");
        return;
      }

      // Get events for the week starting from current start date
      LocalDateTime startDateTime = currentStartDate.atStartOfDay();
      LocalDateTime endDateTime = currentStartDate.plusDays(7).atStartOfDay();

      List<IEvent> events = calendar.getEventsListInDateRange(startDateTime, endDateTime);

      if (events.isEmpty()) {
        view.getEventsListModel().addElement("No events found for this week");
        view.getEventsListModel().addElement("    Click 'Create Event' to add your first event!");
      } else {
        // Sort events by start time
        events.sort((e1, e2) -> e1.getStart().compareTo(e2.getStart()));

        // Add events to the list (limit to MAX_EVENTS_DISPLAY)
        int count = 0;
        for (IEvent event : events) {
          if (count >= MAX_EVENTS_DISPLAY) break;

          String eventDisplay = view.formatEventForDisplay(event, count + 1);
          view.getEventsListModel().addElement(eventDisplay);
          count++;
        }

        // Add indicator if there are more events
        if (events.size() > MAX_EVENTS_DISPLAY) {
          view.getEventsListModel().addElement("");
          view.getEventsListModel().addElement("... and " + (events.size() - MAX_EVENTS_DISPLAY) +
                  " more events (showing top " + MAX_EVENTS_DISPLAY + ")");
        }
      }
    } catch (Exception e) {
      view.getEventsListModel().addElement("Error loading events: " + e.getMessage());
      System.err.println("Error updating schedule view: " + e.getMessage());
    }
  }

  /**
   * Shows a date picker dialog to jump to a specific date.
   */
  private void showDatePickerDialog() {
    JDialog dialog = view.createDialog(view.getMainFrame(), "Go to Date", 350, 200);

    JPanel formPanel = view.createFormPanel();
    GridBagConstraints gbc = new GridBagConstraints();

    // Instructions
    gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
    gbc.insets = new Insets(0, 0, 15, 0);
    JLabel instructionLabel = new JLabel("Enter the date you want to navigate to:");
    formPanel.add(instructionLabel, gbc);

    // Date input
    gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
    gbc.insets = new Insets(5, 0, 5, 10);
    gbc.anchor = GridBagConstraints.EAST;
    formPanel.add(new JLabel("Date (YYYY-MM-DD):"), gbc);

    gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.WEST;
    JTextField dateField = view.createMonospaceTextField(
            currentStartDate.format(view.getDateFormatter()), 15);
    formPanel.add(dateField, gbc);

    dialog.add(formPanel, BorderLayout.CENTER);

    // Button panel
    JPanel buttonPanel = view.createButtonPanel();
    JButton goButton = new JButton("Go");
    JButton cancelButton = new JButton("Cancel");

    goButton.addActionListener(e -> {
      try {
        String input = dateField.getText().trim();
        LocalDate newDate = LocalDate.parse(input, view.getDateFormatter());
        currentStartDate = newDate;
        updateScheduleView();
        dialog.dispose();
      } catch (DateTimeParseException ex) {
        view.showErrorDialog(dialog, "Invalid Date Format",
                "Please enter the date in YYYY-MM-DD format.\n\nExample: " +
                        LocalDate.now().format(view.getDateFormatter()));
        dateField.selectAll();
        dateField.requestFocus();
      }
    });

    cancelButton.addActionListener(e -> dialog.dispose());
    dateField.addActionListener(e -> goButton.doClick());

    buttonPanel.add(goButton);
    buttonPanel.add(cancelButton);
    dialog.add(buttonPanel, BorderLayout.SOUTH);

    dialog.setVisible(true);
  }

  /**
   * Shows the create new calendar dialog.
   */
  private void showCreateCalendarDialog() {
    JDialog dialog = view.createDialog(view.getMainFrame(), "Create New Calendar", 450, 250);

    JPanel formPanel = view.createFormPanel();
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(10, 10, 10, 10);
    gbc.anchor = GridBagConstraints.WEST;

    // Calendar name
    gbc.gridx = 0; gbc.gridy = 0;
    formPanel.add(new JLabel("Calendar Name: *"), gbc);
    gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
    JTextField nameField = view.createStyledTextField("", 20);
    formPanel.add(nameField, gbc);

    // Timezone
    gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
    formPanel.add(new JLabel("Timezone: *"), gbc);
    gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;

    String[] commonTimezones = {
            "America/New_York", "America/Chicago", "America/Denver", "America/Los_Angeles",
            "Europe/London", "Europe/Paris", "Europe/Berlin", "Asia/Tokyo", "Asia/Shanghai",
            "Australia/Sydney", "UTC"
    };
    JComboBox<String> timezoneCombo = view.createStyledComboBox(commonTimezones);
    timezoneCombo.setSelectedItem(java.time.ZoneId.systemDefault().toString());
    timezoneCombo.setEditable(true);
    formPanel.add(timezoneCombo, gbc);

    // Help text
    gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
    JLabel helpLabel = new JLabel("<html><small>Select a timezone or enter a custom one (e.g., America/New_York)</small></html>");
    helpLabel.setForeground(Color.GRAY);
    formPanel.add(helpLabel, gbc);

    dialog.add(formPanel, BorderLayout.CENTER);

    // Button panel
    JPanel buttonPanel = view.createButtonPanel();
    JButton createButton = new JButton("Create Calendar");
    JButton cancelButton = new JButton("Cancel");

    createButton.addActionListener(e -> {
      try {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
          throw new IllegalArgumentException("Calendar name cannot be empty");
        }

        String timezoneStr = (String) timezoneCombo.getSelectedItem();
        if (timezoneStr == null || timezoneStr.trim().isEmpty()) {
          throw new IllegalArgumentException("Timezone cannot be empty");
        }

        java.time.ZoneId timezone = java.time.ZoneId.of(timezoneStr.trim());
        calendarSystem.createCalendar(name, timezone);
        calendarSystem.useCalendar(name);

        updateCalendarInfo();
        updateScheduleView();

        dialog.dispose();
        view.showInfoDialog(view.getMainFrame(),
                "Calendar '" + name + "' created and activated successfully!");

      } catch (Exception ex) {
        view.showErrorDialog(dialog, "Error Creating Calendar", ex.getMessage());
      }
    });

    cancelButton.addActionListener(e -> dialog.dispose());

    buttonPanel.add(createButton);
    buttonPanel.add(cancelButton);
    dialog.add(buttonPanel, BorderLayout.SOUTH);

    nameField.requestFocus();
    dialog.getRootPane().setDefaultButton(createButton);
    dialog.setVisible(true);
  }

  /**
   * Shows the create event dialog.
   */
  private void showCreateEventDialog() {
    JDialog dialog = view.createDialog(view.getMainFrame(), "Create New Event", 550, 650);

    JPanel formPanel = view.createFormPanel();
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(8, 8, 8, 8);
    gbc.anchor = GridBagConstraints.WEST;

    // Create form fields
    JTextField subjectField = createEventFormFields(formPanel, gbc);
    JTextField startDateField = (JTextField) ((JPanel) formPanel.getComponent(3)).getComponent(0);
    JTextField startTimeField = (JTextField) ((JPanel) formPanel.getComponent(5)).getComponent(0);
    JTextField endDateField = (JTextField) ((JPanel) formPanel.getComponent(7)).getComponent(0);
    JTextField endTimeField = (JTextField) ((JPanel) formPanel.getComponent(9)).getComponent(0);
    JTextField descriptionField = (JTextField) ((JPanel) formPanel.getComponent(11)).getComponent(0);
    JComboBox<String> locationCombo = (JComboBox<String>) ((JPanel) formPanel.getComponent(13)).getComponent(0);
    JTextField locationDetailField = (JTextField) ((JPanel) formPanel.getComponent(15)).getComponent(0);
    JComboBox<String> statusCombo = (JComboBox<String>) ((JPanel) formPanel.getComponent(17)).getComponent(0);

    dialog.add(formPanel, BorderLayout.CENTER);

    // Button panel
    JPanel buttonPanel = view.createButtonPanel();
    JButton createButton = view.createStyledButton("Create Event");
    JButton cancelButton = view.createStyledButton("Cancel");

    createButton.addActionListener(e -> handleCreateEvent(dialog, subjectField, startDateField,
            startTimeField, endDateField, endTimeField, descriptionField, locationCombo,
            locationDetailField, statusCombo));

    cancelButton.addActionListener(e -> dialog.dispose());

    buttonPanel.add(createButton);
    buttonPanel.add(cancelButton);
    dialog.add(buttonPanel, BorderLayout.SOUTH);

    subjectField.requestFocus();
    dialog.getRootPane().setDefaultButton(createButton);
    dialog.setVisible(true);
  }

  /**
   * Creates form fields for event creation/editing.
   */
  private JTextField createEventFormFields(JPanel formPanel, GridBagConstraints gbc) {
    // Subject field
    gbc.gridx = 0; gbc.gridy = 0;
    formPanel.add(new JLabel("Subject: *"), gbc);
    gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
    JTextField subjectField = view.createStyledTextField("", 25);
    formPanel.add(subjectField, gbc);

    // Start date field
    gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
    formPanel.add(new JLabel("Start Date: *"), gbc);
    gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
    JTextField startDateField = view.createMonospaceTextField(
            currentStartDate.format(view.getDateFormatter()), 25);
    formPanel.add(startDateField, gbc);

    // Start time field
    gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
    formPanel.add(new JLabel("Start Time: *"), gbc);
    gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
    JTextField startTimeField = view.createMonospaceTextField("09:00", 25);
    formPanel.add(startTimeField, gbc);

    // End date field
    gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
    formPanel.add(new JLabel("End Date: *"), gbc);
    gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
    JTextField endDateField = view.createMonospaceTextField(
            currentStartDate.format(view.getDateFormatter()), 25);
    formPanel.add(endDateField, gbc);

    // End time field
    gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
    formPanel.add(new JLabel("End Time: *"), gbc);
    gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
    JTextField endTimeField = view.createMonospaceTextField("10:00", 25);
    formPanel.add(endTimeField, gbc);

    // Description field
    gbc.gridx = 0; gbc.gridy = 5; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
    formPanel.add(new JLabel("Description:"), gbc);
    gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
    JTextField descriptionField = view.createStyledTextField("", 25);
    formPanel.add(descriptionField, gbc);

    // Location field
    gbc.gridx = 0; gbc.gridy = 6; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
    formPanel.add(new JLabel("Location:"), gbc);
    gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
    JComboBox<String> locationCombo = view.createStyledComboBox(
            new String[]{"", "PHYSICAL", "ONLINE"});
    formPanel.add(locationCombo, gbc);

    // Location detail field
    gbc.gridx = 0; gbc.gridy = 7; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
    formPanel.add(new JLabel("Location Detail:"), gbc);
    gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
    JTextField locationDetailField = view.createStyledTextField("", 25);
    locationDetailField.setToolTipText("Enter address for physical location or URL for online location");
    formPanel.add(locationDetailField, gbc);

    // Status field
    gbc.gridx = 0; gbc.gridy = 8; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
    formPanel.add(new JLabel("Status:"), gbc);
    gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
    JComboBox<String> statusCombo = view.createStyledComboBox(
            new String[]{"", "PUBLIC", "PRIVATE"});
    formPanel.add(statusCombo, gbc);

    // Help text
    gbc.gridx = 0; gbc.gridy = 9; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
    JLabel helpLabel = new JLabel("<html><small><b>Format Guidelines:</b><br>" +
            "• Date: YYYY-MM-DD (e.g., " + LocalDate.now().format(view.getDateFormatter()) + ")<br>" +
            "• Time: HH:MM (e.g., 14:30 for 2:30 PM)<br>" +
            "• Fields marked with * are required</small></html>");
    helpLabel.setForeground(Color.DARK_GRAY);
    formPanel.add(helpLabel, gbc);

    return subjectField;
  }

  /**
   * Handles event creation form submission.
   */
  private void handleCreateEvent(JDialog dialog, JTextField subjectField,
                                 JTextField startDateField, JTextField startTimeField, JTextField endDateField,
                                 JTextField endTimeField, JTextField descriptionField, JComboBox<String> locationCombo,
                                 JTextField locationDetailField, JComboBox<String> statusCombo) {

    try {
      // Validate and parse form data
      String subject = subjectField.getText().trim();
      if (subject.isEmpty()) {
        throw new IllegalArgumentException("Subject is required and cannot be empty");
      }

      LocalDate startDate = LocalDate.parse(startDateField.getText().trim(), view.getDateFormatter());
      LocalDate endDate = LocalDate.parse(endDateField.getText().trim(), view.getDateFormatter());
      LocalTime startTime = LocalTime.parse(startTimeField.getText().trim());
      LocalTime endTime = LocalTime.parse(endTimeField.getText().trim());

      LocalDateTime startDateTime = LocalDateTime.of(startDate, startTime);
      LocalDateTime endDateTime = LocalDateTime.of(endDate, endTime);

      if (endDateTime.isBefore(startDateTime) || endDateTime.equals(startDateTime)) {
        throw new IllegalArgumentException("End time must be after start time");
      }

      ICalendar calendar = calendarSystem.getCurrentCalendar();
      if (calendar == null) {
        throw new CalendarException("No calendar is currently in use");
      }

      // Check for conflicts
      if (!checkForConflicts(calendar, startDateTime, endDateTime, null, dialog)) {
        return; // User chose not to create conflicting event
      }

      // Create the event
      IEvent newEvent = calendar.createEvent(subject, startDateTime, endDateTime);

      // Set additional properties
      setEventProperties(newEvent, descriptionField, locationCombo, locationDetailField, statusCombo);

      dialog.dispose();
      updateScheduleView();

      view.showInfoDialog(view.getMainFrame(),
              "Event '" + subject + "' created successfully!\n\n" +
                      "Date/Time: " + startDateTime.format(dateTimeFormatter) + " to " +
                      endDateTime.format(dateTimeFormatter));

    } catch (DateTimeParseException ex) {
      view.showErrorDialog(dialog, "Invalid Date/Time Format",
              "Please check your date and time formats:\n\n" +
                      "• Date format: YYYY-MM-DD\n" +
                      "• Time format: HH:MM (24-hour format)\n\n" +
                      "Error details: " + ex.getMessage());
    } catch (Exception ex) {
      view.showErrorDialog(dialog, "Error Creating Event", ex.getMessage());
    }
  }

  /**
   * Sets additional properties on an event from form fields.
   */
  private void setEventProperties(IEvent event, JTextField descriptionField,
                                  JComboBox<String> locationCombo, JTextField locationDetailField,
                                  JComboBox<String> statusCombo) throws CalendarException {

    String description = descriptionField.getText().trim();
    if (!description.isEmpty()) {
      event.setDescription(description);
    }

    String locationStr = (String) locationCombo.getSelectedItem();
    if (!locationStr.isEmpty()) {
      Location location = Location.fromStr(locationStr);
      event.setLocation(location);
    }

    String locationDetail = locationDetailField.getText().trim();
    if (!locationDetail.isEmpty()) {
      event.setLocationDetail(locationDetail);
    }

    String statusStr = (String) statusCombo.getSelectedItem();
    if (!statusStr.isEmpty()) {
      EventStatus status = EventStatus.fromStr(statusStr);
      event.setStatus(status);
    }
  }

  /**
   * Checks for event conflicts and shows user confirmation if needed.
   *
   * @return true if event should be created, false if user cancelled
   */
  private boolean checkForConflicts(ICalendar calendar, LocalDateTime startDateTime,
                                    LocalDateTime endDateTime, IEvent excludeEvent, Component parent) {

    try {
      List<IEvent> conflictingEvents = calendar.getEventsListInDateRange(startDateTime, endDateTime);
      conflictingEvents = conflictingEvents.stream()
              .filter(event -> excludeEvent == null || !event.getId().equals(excludeEvent.getId()))
              .filter(event -> eventsOverlap(startDateTime, endDateTime, event.getStart(), event.getEnd()))
              .collect(Collectors.toList());

      if (!conflictingEvents.isEmpty()) {
        StringBuilder conflicts = new StringBuilder("This event conflicts with existing events:\n\n");
        for (IEvent conflict : conflictingEvents) {
          conflicts.append("• ").append(conflict.getSubject())
                  .append(" (").append(conflict.getStart().format(dateTimeFormatter))
                  .append(" - ").append(conflict.getEnd().format(dateTimeFormatter))
                  .append(")\n");
        }
        conflicts.append("\nDo you want to create this event anyway?");

        return view.showConfirmDialog(parent, conflicts.toString(), "Schedule Conflict");
      }

      return true;
    } catch (Exception e) {
      view.showErrorDialog(parent, "Error", "Could not check for conflicts: " + e.getMessage());
      return false;
    }
  }

  /**
   * Checks if two time periods overlap.
   */
  private boolean eventsOverlap(LocalDateTime start1, LocalDateTime end1,
                                LocalDateTime start2, LocalDateTime end2) {
    return start1.isBefore(end2) && start2.isBefore(end1);
  }

  /**
   * Shows event details for the currently selected event.
   */
  private void showEventDetails() {
    int selectedIndex = view.getEventsList().getSelectedIndex();
    if (selectedIndex < 0 || !isValidEventIndex(selectedIndex)) {
      return;
    }

    try {
      IEvent event = getEventAtIndex(selectedIndex);
      if (event != null) {
        showEventDetailsDialog(event);
      }
    } catch (Exception e) {
      view.showErrorDialog(view.getMainFrame(), "Error",
              "Could not load event details: " + e.getMessage());
    }
  }

  /**
   * Shows a dialog with detailed event information.
   */
  private void showEventDetailsDialog(IEvent event) {
    JDialog dialog = view.createDialog(view.getMainFrame(), "Event Details", 450, 350);

    JPanel contentPanel = view.createFormPanel();
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.anchor = GridBagConstraints.WEST;

    // Event details
    addDetailRow(contentPanel, gbc, "Subject:", event.getSubject(), 0);
    addDetailRow(contentPanel, gbc, "Start:", event.getStart().format(dateTimeFormatter), 1);
    addDetailRow(contentPanel, gbc, "End:", event.getEnd().format(dateTimeFormatter), 2);
    addDetailRow(contentPanel, gbc, "Duration:", calculateDuration(event.getStart(), event.getEnd()), 3);

    if (event.getLocation() != null) {
      addDetailRow(contentPanel, gbc, "Location:", event.getLocationDisplay(), 4);
    }

    if (event.getDescription() != null && !event.getDescription().trim().isEmpty()) {
      addDetailRow(contentPanel, gbc, "Description:", event.getDescription(), 5);
    }

    if (event.getSeriesID() != null) {
      addDetailRow(contentPanel, gbc, "Series:", "Part of recurring series", 6);
    }

    dialog.add(contentPanel, BorderLayout.CENTER);

    // Button panel
    JPanel buttonPanel = view.createButtonPanel();
    JButton closeButton = new JButton("Close");
    closeButton.addActionListener(e -> dialog.dispose());
    buttonPanel.add(closeButton);
    dialog.add(buttonPanel, BorderLayout.SOUTH);

    dialog.setVisible(true);
  }

  /**
   * Adds a detail row to the event details panel.
   */
  private void addDetailRow(JPanel panel, GridBagConstraints gbc, String label, String value, int row) {
    gbc.gridx = 0; gbc.gridy = row;
    gbc.fill = GridBagConstraints.NONE;
    JLabel labelComponent = new JLabel(label);
    labelComponent.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
    panel.add(labelComponent, gbc);

    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    JLabel valueComponent = new JLabel(value);
    panel.add(valueComponent, gbc);
    gbc.weightx = 0;
  }

  /**
   * Calculates duration between two LocalDateTime objects.
   */
  private String calculateDuration(LocalDateTime start, LocalDateTime end) {
    long minutes = java.time.Duration.between(start, end).toMinutes();
    long hours = minutes / 60;
    minutes = minutes % 60;

    if (hours > 0) {
      return hours + " hour" + (hours != 1 ? "s" : "") +
              (minutes > 0 ? " " + minutes + " minute" + (minutes != 1 ? "s" : "") : "");
    } else {
      return minutes + " minute" + (minutes != 1 ? "s" : "");
    }
  }

  /**
   * Shows event context menu for right-click actions.
   */
  private void showEventContextMenu(MouseEvent e) {
    int selectedIndex = view.getEventsList().getSelectedIndex();
    if (selectedIndex < 0 || !isValidEventIndex(selectedIndex)) {
      return;
    }

    JPopupMenu contextMenu = new JPopupMenu();

    JMenuItem viewItem = new JMenuItem("View Details");
    viewItem.addActionListener(ae -> showEventDetails());
    contextMenu.add(viewItem);

    JMenuItem editItem = new JMenuItem("Edit Event");
    editItem.addActionListener(ae -> showEditEventDialog());
    contextMenu.add(editItem);

    contextMenu.show(view.getEventsList(), e.getX(), e.getY());
  }

  /**
   * Checks if the selected index corresponds to a valid event.
   */
  private boolean isValidEventIndex(int index) {
    if (index < 0 || index >= view.getEventsListModel().getSize()) {
      return false;
    }
    String item = view.getEventsListModel().get(index);
    return item.matches("^\\s*\\d+\\..*");
  }

  /**
   * Gets the event at the specified index.
   */
  private IEvent getEventAtIndex(int index) {
    try {
      ICalendar calendar = calendarSystem.getCurrentCalendar();
      LocalDateTime startDateTime = currentStartDate.atStartOfDay();
      LocalDateTime endDateTime = currentStartDate.plusDays(7).atStartOfDay();
      List<IEvent> events = calendar.getEventsListInDateRange(startDateTime, endDateTime);

      if (index >= 0 && index < events.size()) {
        events.sort((e1, e2) -> e1.getStart().compareTo(e2.getStart()));
        return events.get(index);
      }
    } catch (Exception e) {
      System.err.println("Error getting event at index: " + e.getMessage());
    }
    return null;
  }

  // Placeholder methods for remaining functionality
  private void showEditEventDialog() {
    view.showInfoDialog(view.getMainFrame(), "Edit event functionality - implementation details omitted for brevity");
  }

  private void showCreateSeriesDialog() {
    view.showInfoDialog(view.getMainFrame(), "Create series functionality - implementation details omitted for brevity");
  }

  private void showStatusDialog() {
    view.showInfoDialog(view.getMainFrame(), "Status check functionality - implementation details omitted for brevity");
  }

  private void showSearchEventsDialog() {
    view.showInfoDialog(view.getMainFrame(), "Search events functionality - implementation details omitted for brevity");
  }
}