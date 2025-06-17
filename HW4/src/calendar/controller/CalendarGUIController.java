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
import calendar.model.*;
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
  private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(
          "yyyy-MM-dd HH:mm");

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
    JLabel helpLabel = new JLabel(
            "<html><small>Select a timezone or enter a custom one " +
                    "(e.g., America/New_York)</small></html>");
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
    // Check if there's a calendar in use first
    if (calendarSystem.getCurrentCalendar() == null) {
      view.showErrorDialog(view.getMainFrame(), "No Calendar Selected",
              "Please create or select a calendar first before creating events.");
      return;
    }

    JDialog dialog = view.createDialog(view.getMainFrame(), "Create New Event", 550, 650);

    JPanel formPanel = view.createFormPanel();
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(8, 8, 8, 8);
    gbc.anchor = GridBagConstraints.WEST;

    // Create all form fields and store references
    EventFormFields formFields = createEventFormFields(formPanel, gbc);

    dialog.add(formPanel, BorderLayout.CENTER);

    // Button panel
    JPanel buttonPanel = view.createButtonPanel();
    JButton createButton = view.createStyledButton("Create Event");
    JButton cancelButton = view.createStyledButton("Cancel");

    createButton.addActionListener(e -> handleCreateEvent(dialog, formFields));
    cancelButton.addActionListener(e -> dialog.dispose());

    buttonPanel.add(createButton);
    buttonPanel.add(cancelButton);
    dialog.add(buttonPanel, BorderLayout.SOUTH);

    formFields.subjectField.requestFocus();
    dialog.getRootPane().setDefaultButton(createButton);
    dialog.setVisible(true);
  }

  /**
   * Inner class to hold form field references
   */
  private static class EventFormFields {
    JTextField subjectField;
    JTextField startDateField;
    JTextField startTimeField;
    JTextField endDateField;
    JTextField endTimeField;
    JTextField descriptionField;
    JComboBox<String> locationCombo;
    JTextField locationDetailField;
    JComboBox<String> statusCombo;
  }

  /**
   * Creates form fields for event creation/editing.
   */
  private EventFormFields createEventFormFields(JPanel formPanel, GridBagConstraints gbc) {
    EventFormFields fields = new EventFormFields();

    // Subject field
    gbc.gridx = 0; gbc.gridy = 0;
    formPanel.add(new JLabel("Subject: *"), gbc);
    gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
    fields.subjectField = view.createStyledTextField("", 25);
    formPanel.add(fields.subjectField, gbc);

    // Start date field
    gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
    formPanel.add(new JLabel("Start Date: *"), gbc);
    gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
    fields.startDateField = view.createMonospaceTextField(
            currentStartDate.format(view.getDateFormatter()), 25);
    formPanel.add(fields.startDateField, gbc);

    // Start time field
    gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
    formPanel.add(new JLabel("Start Time: *"), gbc);
    gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
    fields.startTimeField = view.createMonospaceTextField("09:00", 25);
    formPanel.add(fields.startTimeField, gbc);

    // End date field
    gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
    formPanel.add(new JLabel("End Date: *"), gbc);
    gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
    fields.endDateField = view.createMonospaceTextField(
            currentStartDate.format(view.getDateFormatter()), 25);
    formPanel.add(fields.endDateField, gbc);

    // End time field
    gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
    formPanel.add(new JLabel("End Time: *"), gbc);
    gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
    fields.endTimeField = view.createMonospaceTextField("10:00", 25);
    formPanel.add(fields.endTimeField, gbc);

    // Description field
    gbc.gridx = 0; gbc.gridy = 5; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
    formPanel.add(new JLabel("Description:"), gbc);
    gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
    fields.descriptionField = view.createStyledTextField("", 25);
    formPanel.add(fields.descriptionField, gbc);

    // Location field
    gbc.gridx = 0; gbc.gridy = 6; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
    formPanel.add(new JLabel("Location:"), gbc);
    gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
    fields.locationCombo = view.createStyledComboBox(
            new String[]{"", "PHYSICAL", "ONLINE"});
    formPanel.add(fields.locationCombo, gbc);

    // Location detail field
    gbc.gridx = 0; gbc.gridy = 7; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
    formPanel.add(new JLabel("Location Detail:"), gbc);
    gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
    fields.locationDetailField = view.createStyledTextField("", 25);
    fields.locationDetailField.setToolTipText(
            "Enter address for physical location or URL for online location");
    formPanel.add(fields.locationDetailField, gbc);

    // Status field
    gbc.gridx = 0; gbc.gridy = 8; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
    formPanel.add(new JLabel("Status:"), gbc);
    gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
    fields.statusCombo = view.createStyledComboBox(
            new String[]{"", "PUBLIC", "PRIVATE"});
    formPanel.add(fields.statusCombo, gbc);

    // Help text
    gbc.gridx = 0; gbc.gridy = 9; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
    JLabel helpLabel = new JLabel("<html><small><b>Format Guidelines:</b><br>" +
            "• Date: YYYY-MM-DD (e.g., " + LocalDate.now().format(view.getDateFormatter()) +
            ")<br>" +
            "• Time: HH:MM (e.g., 14:30 for 2:30 PM)<br>" +
            "• Fields marked with * are required</small></html>");
    helpLabel.setForeground(Color.DARK_GRAY);
    formPanel.add(helpLabel, gbc);

    return fields;
  }

  /**
   * Handles event creation form submission.
   */
  private void handleCreateEvent(JDialog dialog, EventFormFields fields) {
    try {
      // Validate and parse form data
      String subject = fields.subjectField.getText().trim();
      if (subject.isEmpty()) {
        throw new IllegalArgumentException("Subject is required and cannot be empty");
      }

      LocalDate startDate = LocalDate.parse(fields.startDateField.getText().trim(),
              view.getDateFormatter());
      LocalDate endDate = LocalDate.parse(fields.endDateField.getText().trim(),
              view.getDateFormatter());
      LocalTime startTime = LocalTime.parse(fields.startTimeField.getText().trim());
      LocalTime endTime = LocalTime.parse(fields.endTimeField.getText().trim());

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
      setEventProperties(newEvent, fields);

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
  private void setEventProperties(IEvent event, EventFormFields fields) throws CalendarException {
    String description = fields.descriptionField.getText().trim();
    if (!description.isEmpty()) {
      event.setDescription(description);
    }

    String locationStr = (String) fields.locationCombo.getSelectedItem();
    if (locationStr != null && !locationStr.isEmpty()) {
      Location location = Location.fromStr(locationStr);
      event.setLocation(location);
    }

    String locationDetail = fields.locationDetailField.getText().trim();
    if (!locationDetail.isEmpty()) {
      event.setLocationDetail(locationDetail);
    }

    String statusStr = (String) fields.statusCombo.getSelectedItem();
    if (statusStr != null && !statusStr.isEmpty()) {
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
                                    LocalDateTime endDateTime, IEvent excludeEvent,
                                    Component parent) {
    try {
      List<IEvent> conflictingEvents = calendar.getEventsListInDateRange(
              startDateTime, endDateTime);
      conflictingEvents = conflictingEvents.stream()
              .filter(event -> excludeEvent == null || !event.getId().equals(excludeEvent.getId()))
              .filter(event -> eventsOverlap(startDateTime, endDateTime, event.getStart(),
                      event.getEnd()))
              .collect(Collectors.toList());

      if (!conflictingEvents.isEmpty()) {
        StringBuilder conflicts = new StringBuilder(
                "This event conflicts with existing events:\n\n");
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
    addDetailRow(contentPanel, gbc, "Duration:", calculateDuration(event.getStart(),
            event.getEnd()), 3);

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
  private void addDetailRow(JPanel panel, GridBagConstraints gbc, String label, String value,
                            int row) {
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

  /**
   * Shows the edit event dialog for the selected event.
   */
  private void showEditEventDialog() {
    try {
      int selectedIndex = view.getEventsList().getSelectedIndex();
      if (!isValidEventIndex(selectedIndex)) {
        return;
      }

      IEvent selectedEvent = getEventAtIndex(selectedIndex);
      if (selectedEvent == null) {
        view.showErrorDialog(view.getMainFrame(), "Error", "Could not find the selected event.");
        return;
      }

      JDialog dialog = view.createDialog(view.getMainFrame(), "Edit Event", 600, 550);

      JPanel formPanel = view.createFormPanel();
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.insets = new Insets(8, 8, 8, 8);
      gbc.anchor = GridBagConstraints.WEST;

      // Create form fields with existing event data
      EventFormFields formFields = createEditEventFormFields(formPanel, gbc, selectedEvent);

      dialog.add(formPanel, BorderLayout.CENTER);

      // Button panel
      JPanel buttonPanel = view.createButtonPanel();
      JButton saveButton = view.createStyledButton("Save Changes");
      JButton cancelButton = view.createStyledButton("Cancel");

      saveButton.addActionListener(e -> handleEditEvent(dialog, selectedEvent, formFields));
      cancelButton.addActionListener(e -> dialog.dispose());

      buttonPanel.add(saveButton);
      buttonPanel.add(cancelButton);
      dialog.add(buttonPanel, BorderLayout.SOUTH);

      dialog.setVisible(true);

    } catch (Exception e) {
      view.showErrorDialog(view.getMainFrame(), "Error", "Could not open edit dialog: "
              + e.getMessage());
    }
  }

  /**
   * Creates form fields for event editing with existing data.
   */
  private EventFormFields createEditEventFormFields(JPanel formPanel, GridBagConstraints gbc,
                                                    IEvent event) {
    EventFormFields fields = new EventFormFields();

    // Subject field
    gbc.gridx = 0; gbc.gridy = 0;
    formPanel.add(new JLabel("Subject: *"), gbc);
    gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
    fields.subjectField = view.createStyledTextField(event.getSubject(), 25);
    formPanel.add(fields.subjectField, gbc);

    // Start date field
    gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
    formPanel.add(new JLabel("Start Date: *"), gbc);
    gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
    fields.startDateField = view.createMonospaceTextField(
            event.getStart().toLocalDate().format(view.getDateFormatter()), 25);
    formPanel.add(fields.startDateField, gbc);

    // Start time field
    gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
    formPanel.add(new JLabel("Start Time: *"), gbc);
    gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
    fields.startTimeField = view.createMonospaceTextField(
            event.getStart().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")), 25);
    formPanel.add(fields.startTimeField, gbc);

    // End date field
    gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
    formPanel.add(new JLabel("End Date: *"), gbc);
    gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
    fields.endDateField = view.createMonospaceTextField(
            event.getEnd().toLocalDate().format(view.getDateFormatter()), 25);
    formPanel.add(fields.endDateField, gbc);

    // End time field
    gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
    formPanel.add(new JLabel("End Time: *"), gbc);
    gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
    fields.endTimeField = view.createMonospaceTextField(
            event.getEnd().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")), 25);
    formPanel.add(fields.endTimeField, gbc);

    // Description field
    gbc.gridx = 0; gbc.gridy = 5; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
    formPanel.add(new JLabel("Description:"), gbc);
    gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
    fields.descriptionField = view.createStyledTextField(
            event.getDescription() != null ? event.getDescription() : "", 25);
    formPanel.add(fields.descriptionField, gbc);

    // Location field
    gbc.gridx = 0; gbc.gridy = 6; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
    formPanel.add(new JLabel("Location:"), gbc);
    gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
    fields.locationCombo = view.createStyledComboBox(new String[]{"", "PHYSICAL", "ONLINE"});
    if (event.getLocation() != null) {
      fields.locationCombo.setSelectedItem(event.getLocation().name());
    }
    formPanel.add(fields.locationCombo, gbc);

    // Location detail field
    gbc.gridx = 0; gbc.gridy = 7; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
    formPanel.add(new JLabel("Location Detail:"), gbc);
    gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
    fields.locationDetailField = view.createStyledTextField(
            event.getLocationDetail() != null ? event.getLocationDetail() : "", 25);
    formPanel.add(fields.locationDetailField, gbc);

    // Status field
    gbc.gridx = 0; gbc.gridy = 8; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
    formPanel.add(new JLabel("Status:"), gbc);
    gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
    fields.statusCombo = view.createStyledComboBox(new String[]{"", "PUBLIC", "PRIVATE"});
    if (event.getStatus() != null) {
      fields.statusCombo.setSelectedItem(event.getStatus().name());
    }
    formPanel.add(fields.statusCombo, gbc);

    // Series info (read-only)
    if (event.getSeriesID() != null) {
      gbc.gridx = 0; gbc.gridy = 9; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
      formPanel.add(new JLabel("Series:"), gbc);
      gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
      JLabel seriesLabel = new JLabel("Part of recurring series (editing will break from series)");
      seriesLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 12));
      seriesLabel.setForeground(Color.BLUE);
      formPanel.add(seriesLabel, gbc);
    }

    return fields;
  }

  /**
   * Handles edit event form submission.
   */
  private void handleEditEvent(JDialog dialog, IEvent selectedEvent, EventFormFields fields) {
    try {
      // Validate and update event
      String subject = fields.subjectField.getText().trim();
      if (subject.isEmpty()) {
        throw new IllegalArgumentException("Subject cannot be empty");
      }

      LocalDate startDate = LocalDate.parse(fields.startDateField.getText().trim(),
              view.getDateFormatter());
      LocalDate endDate = LocalDate.parse(fields.endDateField.getText().trim(),
              view.getDateFormatter());
      LocalTime startTime = LocalTime.parse(fields.startTimeField.getText().trim());
      LocalTime endTime = LocalTime.parse(fields.endTimeField.getText().trim());

      LocalDateTime startDateTime = LocalDateTime.of(startDate, startTime);
      LocalDateTime endDateTime = LocalDateTime.of(endDate, endTime);

      if (endDateTime.isBefore(startDateTime) || endDateTime.equals(startDateTime)) {
        throw new IllegalArgumentException("End time must be after start time");
      }

      ICalendar calendar = calendarSystem.getCurrentCalendar();

      // Check for conflicts (excluding current event)
      if (!checkForConflicts(calendar, startDateTime, endDateTime, selectedEvent, dialog)) {
        return; // User chose not to save conflicting changes
      }

      // Update event properties
      calendar.editEvent(selectedEvent.getId(), Property.SUBJECT, subject);
      calendar.editEvent(selectedEvent.getId(), Property.START, startDateTime.format(
              dateTimeFormatter));
      calendar.editEvent(selectedEvent.getId(), Property.END, endDateTime.format(
              dateTimeFormatter));

      String description = fields.descriptionField.getText().trim();
      if (!description.isEmpty()) {
        calendar.editEvent(selectedEvent.getId(), Property.DESCRIPTION, description);
      }

      String locationStr = (String) fields.locationCombo.getSelectedItem();
      if (locationStr != null && !locationStr.isEmpty()) {
        calendar.editEvent(selectedEvent.getId(), Property.LOCATION, locationStr);
      }

      String locationDetail = fields.locationDetailField.getText().trim();
      if (!locationDetail.isEmpty()) {
        selectedEvent.setLocationDetail(locationDetail);
      }

      String statusStr = (String) fields.statusCombo.getSelectedItem();
      if (statusStr != null && !statusStr.isEmpty()) {
        calendar.editEvent(selectedEvent.getId(), Property.STATUS, statusStr);
      }

      dialog.dispose();
      updateScheduleView();
      view.showInfoDialog(view.getMainFrame(), "Event '" + subject + "' updated successfully!");

    } catch (Exception ex) {
      view.showErrorDialog(dialog, "Error Updating Event", ex.getMessage());
    }
  }

  /**
   * Shows the create event series dialog.
   */
  private void showCreateSeriesDialog() {
    JDialog dialog = view.createDialog(view.getMainFrame(), "Create Event Series", 600, 650);

    JPanel formPanel = view.createFormPanel();
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(8, 8, 8, 8);
    gbc.anchor = GridBagConstraints.WEST;

    // Create series form
    SeriesFormFields seriesFields = createSeriesFormFields(formPanel, gbc);

    dialog.add(formPanel, BorderLayout.CENTER);

    // Button panel
    JPanel buttonPanel = view.createButtonPanel();
    JButton createButton = view.createStyledButton("Create Series");
    JButton cancelButton = view.createStyledButton("Cancel");

    createButton.addActionListener(e -> handleCreateSeries(dialog, seriesFields));
    cancelButton.addActionListener(e -> dialog.dispose());

    buttonPanel.add(createButton);
    buttonPanel.add(cancelButton);
    dialog.add(buttonPanel, BorderLayout.SOUTH);

    dialog.setVisible(true);
  }

  /**
   * Inner class to hold series form field references
   */
  private static class SeriesFormFields {
    JTextField subjectField;
    JTextField startDateField;
    JTextField startTimeField;
    JTextField endTimeField;
    JCheckBox[] dayBoxes;
    JRadioButton forTimesRadio;
    JRadioButton untilDateRadio;
    JSpinner timesSpinner;
    JTextField endDateField;
    JTextField descriptionField;
    JComboBox<String> locationCombo;
    JComboBox<String> statusCombo;
  }

  /**
   * Creates form fields for series creation.
   */
  private SeriesFormFields createSeriesFormFields(JPanel formPanel, GridBagConstraints gbc) {
    SeriesFormFields fields = new SeriesFormFields();

    // Subject field
    gbc.gridx = 0; gbc.gridy = 0;
    formPanel.add(new JLabel("Subject: *"), gbc);
    gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
    fields.subjectField = view.createStyledTextField("", 25);
    formPanel.add(fields.subjectField, gbc);

    // Start date
    gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
    formPanel.add(new JLabel("Start Date: *"), gbc);
    gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
    fields.startDateField = view.createMonospaceTextField(currentStartDate.format(
            view.getDateFormatter()), 25);
    formPanel.add(fields.startDateField, gbc);

    // Start time
    gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
    formPanel.add(new JLabel("Start Time: *"), gbc);
    gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
    fields.startTimeField = view.createMonospaceTextField("09:00", 25);
    formPanel.add(fields.startTimeField, gbc);

    // End time
    gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
    formPanel.add(new JLabel("End Time: *"), gbc);
    gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
    fields.endTimeField = view.createMonospaceTextField("10:00", 25);
    formPanel.add(fields.endTimeField, gbc);

    // Days of week
    gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
    formPanel.add(new JLabel("Days of Week: *"), gbc);
    gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
    JPanel daysPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    fields.dayBoxes = new JCheckBox[7];
    String[] dayNames = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    for (int i = 0; i < 7; i++) {
      fields.dayBoxes[i] = new JCheckBox(dayNames[i]);
      daysPanel.add(fields.dayBoxes[i]);
    }
    formPanel.add(daysPanel, gbc);

    // Repetition type
    gbc.gridx = 0; gbc.gridy = 5; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
    formPanel.add(new JLabel("Repeat: *"), gbc);
    gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
    fields.forTimesRadio = new JRadioButton("For specific number of times");
    fields.untilDateRadio = new JRadioButton("Until specific date");
    ButtonGroup repeatGroup = new ButtonGroup();
    repeatGroup.add(fields.forTimesRadio);
    repeatGroup.add(fields.untilDateRadio);
    fields.forTimesRadio.setSelected(true);
    JPanel repeatPanel = new JPanel(new GridLayout(2, 1));
    repeatPanel.add(fields.forTimesRadio);
    repeatPanel.add(fields.untilDateRadio);
    formPanel.add(repeatPanel, gbc);

    // Number of times
    gbc.gridx = 0; gbc.gridy = 6; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
    formPanel.add(new JLabel("Number of Times:"), gbc);
    gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
    fields.timesSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 100, 1));
    formPanel.add(fields.timesSpinner, gbc);

    // End date
    gbc.gridx = 0; gbc.gridy = 7; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
    formPanel.add(new JLabel("End Date:"), gbc);
    gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
    fields.endDateField = view.createMonospaceTextField(currentStartDate.plusDays(30).format(
            view.getDateFormatter()), 25);
    formPanel.add(fields.endDateField, gbc);

    // Description
    gbc.gridx = 0; gbc.gridy = 8; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
    formPanel.add(new JLabel("Description:"), gbc);
    gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
    fields.descriptionField = view.createStyledTextField("", 25);
    formPanel.add(fields.descriptionField, gbc);

    // Location
    gbc.gridx = 0; gbc.gridy = 9; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
    formPanel.add(new JLabel("Location:"), gbc);
    gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
    fields.locationCombo = view.createStyledComboBox(new String[]{"", "PHYSICAL", "ONLINE"});
    formPanel.add(fields.locationCombo, gbc);

    // Status
    gbc.gridx = 0; gbc.gridy = 10; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
    formPanel.add(new JLabel("Status:"), gbc);
    gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
    fields.statusCombo = view.createStyledComboBox(new String[]{"", "PUBLIC", "PRIVATE"});
    formPanel.add(fields.statusCombo, gbc);

    return fields;
  }

  /**
   * Handles series creation form submission.
   */
  private void handleCreateSeries(JDialog dialog, SeriesFormFields fields) {
    try {
      // Validate inputs
      String subject = fields.subjectField.getText().trim();
      if (subject.isEmpty()) {
        throw new IllegalArgumentException("Subject is required");
      }

      // Get selected days
      java.util.Set<Days> selectedDays = new java.util.HashSet<>();
      Days[] daysEnum = {Days.MONDAY, Days.TUESDAY, Days.WEDNESDAY, Days.THURSDAY,
              Days.FRIDAY, Days.SATURDAY, Days.SUNDAY};

      for (int i = 0; i < 7; i++) {
        if (fields.dayBoxes[i].isSelected()) {
          selectedDays.add(daysEnum[i]);
        }
      }

      if (selectedDays.isEmpty()) {
        throw new IllegalArgumentException("At least one day must be selected");
      }

      LocalDate startDate = LocalDate.parse(fields.startDateField.getText().trim(),
              view.getDateFormatter());
      LocalTime startTime = LocalTime.parse(fields.startTimeField.getText().trim());
      LocalTime endTime = LocalTime.parse(fields.endTimeField.getText().trim());

      int repeats = 0;
      LocalDate endDate = null;

      if (fields.forTimesRadio.isSelected()) {
        repeats = (Integer) fields.timesSpinner.getValue();
      } else {
        endDate = LocalDate.parse(fields.endDateField.getText().trim(), view.getDateFormatter());
      }

      String description = fields.descriptionField.getText().trim();
      String locationStr = (String) fields.locationCombo.getSelectedItem();
      String statusStr = (String) fields.statusCombo.getSelectedItem();

      Location location = null;
      if (locationStr != null && !locationStr.isEmpty()) {
        location = Location.fromStr(locationStr);
      }

      EventStatus status = null;
      if (statusStr != null && !statusStr.isEmpty()) {
        status = EventStatus.fromStr(statusStr);
      }

      // Create the series
      ICalendar calendar = calendarSystem.getCurrentCalendar();

      // Basic conflict check for first event
      LocalDateTime firstEventStart = LocalDateTime.of(startDate, startTime);
      LocalDateTime firstEventEnd = LocalDateTime.of(startDate, endTime);

      if (!checkForConflicts(calendar, firstEventStart, firstEventEnd, null, dialog)) {
        return; // User chose not to create conflicting series
      }

      calendar.createEventSeries(subject, startTime, endTime, selectedDays, startDate,
              endDate, repeats, description.isEmpty() ? null : description, location, status);

      dialog.dispose();
      updateScheduleView();
      view.showInfoDialog(view.getMainFrame(), "Event series '" + subject + "' " +
              "created successfully!");

    } catch (Exception ex) {
      view.showErrorDialog(dialog, "Error Creating Series", ex.getMessage());
    }
  }

  /**
   * Shows the status checking dialog.
   */
  private void showStatusDialog() {
    JDialog dialog = view.createDialog(view.getMainFrame(), "Check Status", 400, 250);

    JPanel formPanel = view.createFormPanel();
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(10, 10, 10, 10);

    gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
    formPanel.add(new JLabel("Check if you're busy at:"), gbc);

    // Date field
    gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
    gbc.anchor = GridBagConstraints.EAST;
    formPanel.add(new JLabel("Date:"), gbc);
    gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
    JTextField dateField = view.createMonospaceTextField(currentStartDate.format(
            view.getDateFormatter()), 20);
    dateField.setPreferredSize(new Dimension(200, 25));
    formPanel.add(dateField, gbc);

    // Time field
    gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
    gbc.anchor = GridBagConstraints.EAST;
    formPanel.add(new JLabel("Time:"), gbc);
    gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
    JTextField timeField = view.createMonospaceTextField("14:00", 20);
    timeField.setPreferredSize(new Dimension(200, 25));
    formPanel.add(timeField, gbc);

    dialog.add(formPanel, BorderLayout.CENTER);

    JPanel buttonPanel = view.createButtonPanel();
    JButton checkButton = view.createStyledButton("Check Status");
    JButton cancelButton = view.createStyledButton("Cancel");

    checkButton.addActionListener(e -> {
      try {
        LocalDate date = LocalDate.parse(dateField.getText().trim(), view.getDateFormatter());
        LocalTime time = LocalTime.parse(timeField.getText().trim());
        LocalDateTime dateTime = LocalDateTime.of(date, time);

        ICalendar calendar = calendarSystem.getCurrentCalendar();
        boolean isBusy = calendar.isBusyAt(dateTime);

        dialog.dispose();
        if (isBusy) {
          view.showInfoDialog(view.getMainFrame(), "You are BUSY at " +
                  dateTime.format(dateTimeFormatter) +
                  "\nYou have an event scheduled at that time.");
        } else {
          view.showInfoDialog(view.getMainFrame(), "You are AVAILABLE at " +
                  dateTime.format(dateTimeFormatter) +
                  "\nNo events scheduled at that time.");
        }
      } catch (Exception ex) {
        view.showErrorDialog(dialog, "Error", "Invalid date/time format: " + ex.getMessage());
      }
    });

    cancelButton.addActionListener(e -> dialog.dispose());

    buttonPanel.add(checkButton);
    buttonPanel.add(cancelButton);
    dialog.add(buttonPanel, BorderLayout.SOUTH);

    dialog.setVisible(true);
  }

  /**
   * Shows the search events dialog to find next 10 events from a specific date.
   */
  private void showSearchEventsDialog() {
    JDialog dialog = view.createDialog(view.getMainFrame(), "Search Events", 400, 200);

    JPanel formPanel = view.createFormPanel();
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(10, 10, 10, 10);

    gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
    JLabel instructionLabel = new JLabel("Enter a date to find the next 10 events:");
    formPanel.add(instructionLabel, gbc);

    gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
    formPanel.add(new JLabel("Search from Date:"), gbc);
    gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
    JTextField searchDateField = view.createMonospaceTextField(currentStartDate.format(
            view.getDateFormatter()), 15);
    formPanel.add(searchDateField, gbc);

    gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
    JLabel helpLabel = new JLabel("<html><small>Format: YYYY-MM-DD (e.g., " +
            LocalDate.now().format(view.getDateFormatter()) + ")</small></html>");
    helpLabel.setForeground(Color.GRAY);
    formPanel.add(helpLabel, gbc);

    dialog.add(formPanel, BorderLayout.CENTER);

    JPanel buttonPanel = view.createButtonPanel();
    JButton searchButton = view.createStyledButton("Search");
    JButton cancelButton = view.createStyledButton("Cancel");

    searchButton.addActionListener(e -> {
      try {
        LocalDate searchDate = LocalDate.parse(searchDateField.getText().trim(),
                view.getDateFormatter());
        dialog.dispose();
        showSearchResultsDialog(searchDate);
      } catch (DateTimeParseException ex) {
        view.showErrorDialog(dialog, "Invalid Date Format",
                "Please enter the date in YYYY-MM-DD format.\n\nExample: " +
                        LocalDate.now().format(view.getDateFormatter()));
        searchDateField.selectAll();
        searchDateField.requestFocus();
      }
    });

    cancelButton.addActionListener(e -> dialog.dispose());

    // Make Enter key trigger the Search button
    searchDateField.addActionListener(e -> searchButton.doClick());

    buttonPanel.add(searchButton);
    buttonPanel.add(cancelButton);
    dialog.add(buttonPanel, BorderLayout.SOUTH);

    // Set focus and default button
    searchDateField.requestFocus();
    searchDateField.selectAll();
    dialog.getRootPane().setDefaultButton(searchButton);

    dialog.setVisible(true);
  }

  /**
   * Shows the search results dialog with the next 10 events from the specified date.
   */
  private void showSearchResultsDialog(LocalDate searchFromDate) {
    JDialog dialog = view.createDialog(view.getMainFrame(), "Search Results", 700, 500);

    // Title panel
    JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    titlePanel.setBorder(new javax.swing.border.EmptyBorder(10, 10, 5, 10));
    JLabel titleLabel = new JLabel("Next 10 Events from " + searchFromDate.format(
            view.getDisplayDateFormatter()));
    titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
    titlePanel.add(titleLabel);
    dialog.add(titlePanel, BorderLayout.NORTH);

    // Results list
    DefaultListModel<String> searchResultsModel = new DefaultListModel<>();
    JList<String> searchResultsList = new JList<>(searchResultsModel);
    searchResultsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    searchResultsList.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

    try {
      ICalendar calendar = calendarSystem.getCurrentCalendar();
      if (calendar == null) {
        searchResultsModel.addElement("No calendar in use");
      } else {
        // Search for events starting from the specified date
        LocalDateTime searchStart = searchFromDate.atStartOfDay();
        // Search 6 months ahead
        LocalDateTime searchEnd = searchFromDate.plusMonths(6).atStartOfDay();

        List<IEvent> allEvents = calendar.getEventsListInDateRange(searchStart, searchEnd);

        if (allEvents.isEmpty()) {
          searchResultsModel.addElement("No events found from " + searchFromDate.format(
                  view.getDateFormatter()));
          searchResultsModel.addElement(
                  "Try searching from an earlier date or create some events!");
        } else {
          // Sort events by start time and take the first 10
          allEvents.sort((e1, e2) -> e1.getStart().compareTo(e2.getStart()));

          int count = 0;
          for (IEvent event : allEvents) {
            if (count >= 10) break;

            String eventDisplay = view.formatSearchResultForDisplay(event, count + 1);
            searchResultsModel.addElement(eventDisplay);
            count++;
          }

          // Add summary info
          if (allEvents.size() > 10) {
            searchResultsModel.addElement("");
            searchResultsModel.addElement("Found " + allEvents.size() + " " +
                    "total events (showing first 10)");
          } else {
            searchResultsModel.addElement("");
            searchResultsModel.addElement("Found " + allEvents.size() + " total events");
          }
        }
      }
    } catch (Exception e) {
      searchResultsModel.addElement("Error searching events: " + e.getMessage());
      System.err.println("Error searching events: " + e.getMessage());
    }

    // Add double-click listener for event details
    searchResultsList.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
          int selectedIndex = searchResultsList.getSelectedIndex();
          if (selectedIndex >= 0 && isValidSearchResultIndex(selectedIndex, searchResultsModel)) {
            showSearchResultEventDetails(searchFromDate, selectedIndex);
          }
        }
      }
    });

    JScrollPane scrollPane = new JScrollPane(searchResultsList);
    scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Search Results (Double-click to view details)"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
    ));
    dialog.add(scrollPane, BorderLayout.CENTER);

    // Button panel
    JPanel buttonPanel = view.createButtonPanel();
    JButton newSearchButton = view.createStyledButton("New Search");
    JButton jumpToDateButton = view.createStyledButton("Jump to First Event");
    JButton closeButton = view.createStyledButton("Close");

    newSearchButton.addActionListener(e -> {
      dialog.dispose();
      showSearchEventsDialog();
    });

    jumpToDateButton.addActionListener(e -> {
      try {
        ICalendar calendar = calendarSystem.getCurrentCalendar();
        if (calendar != null) {
          LocalDateTime searchStart = searchFromDate.atStartOfDay();
          LocalDateTime searchEnd = searchFromDate.plusMonths(6).atStartOfDay();
          List<IEvent> allEvents = calendar.getEventsListInDateRange(searchStart, searchEnd);

          if (!allEvents.isEmpty()) {
            allEvents.sort((e1, e2) -> e1.getStart().compareTo(e2.getStart()));
            LocalDate firstEventDate = allEvents.get(0).getStart().toLocalDate();

            // Update the main calendar view to show the first event's date
            currentStartDate = firstEventDate;
            updateScheduleView();

            dialog.dispose();
            view.showInfoDialog(view.getMainFrame(), "Jumped to " + firstEventDate.format(
                    view.getDisplayDateFormatter()) +
                    "\nShowing week containing the first event from your search.");
          }
        }
      } catch (Exception ex) {
        view.showErrorDialog(dialog, "Error", "Could not jump to first event: " + ex.getMessage());
      }
    });

    closeButton.addActionListener(e -> dialog.dispose());

    buttonPanel.add(newSearchButton);
    buttonPanel.add(jumpToDateButton);
    buttonPanel.add(closeButton);
    dialog.add(buttonPanel, BorderLayout.SOUTH);

    dialog.setVisible(true);
  }

  /**
   * Checks if the selected index in search results corresponds to a valid event.
   */
  private boolean isValidSearchResultIndex(int index, DefaultListModel<String> model) {
    if (index < 0 || index >= model.getSize()) {
      return false;
    }
    String item = model.get(index);
    return item.matches("^\\s*\\d+\\..*");
  }

  /**
   * Shows event details for a selected search result.
   */
  private void showSearchResultEventDetails(LocalDate searchFromDate, int selectedIndex) {
    try {
      ICalendar calendar = calendarSystem.getCurrentCalendar();
      if (calendar != null) {
        LocalDateTime searchStart = searchFromDate.atStartOfDay();
        LocalDateTime searchEnd = searchFromDate.plusMonths(6).atStartOfDay();
        List<IEvent> allEvents = calendar.getEventsListInDateRange(searchStart, searchEnd);

        if (!allEvents.isEmpty() && selectedIndex < allEvents.size()) {
          allEvents.sort((e1, e2) -> e1.getStart().compareTo(e2.getStart()));
          IEvent event = allEvents.get(selectedIndex);
          showEventDetailsDialog(event);
        }
      }
    } catch (Exception e) {
      view.showErrorDialog(view.getMainFrame(), "Error", "Could not load event details: "
              + e.getMessage());
    }
  }
}