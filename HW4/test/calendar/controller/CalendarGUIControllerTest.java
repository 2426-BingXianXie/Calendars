package calendar.controller;

import org.junit.Before;
import org.junit.Test;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;


import calendar.CalendarException;
import calendar.model.CalendarSystem;
import calendar.model.ICalendar;
import calendar.model.ICalendarSystem;
import calendar.model.IEvent;
import calendar.view.ICalendarGUIView;

import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;

import javax.swing.JPanel;

import javax.swing.JRadioButton;

import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import javax.swing.DefaultListModel;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the CalendarGUIController's logic directly, without a live GUI.
 */
public class CalendarGUIControllerTest {

  private ICalendarSystem system;
  private ICalendarGUIController controller;
  private FakeView fakeView;

  /**
   * A fake implementation of the GUI View for testing purposes.
   * It doesn't create real windows but instead records the messages
   * that the controller tries to show, allowing us to assert they are correct.
   */
  private static class FakeView implements ICalendarGUIView {
    public String lastErrorMessage = null;
    public String lastInfoMessage = null;
    public String lastConfirmationMessage = null;
    public boolean confirmationResult = true;
    public String inputDialogResult = "";

    private final JFrame dummyFrame = new JFrame();

    @Override
    public void showErrorDialog(Component parent, String title, String message) {
      this.lastErrorMessage = message;
    }

    @Override
    public void showInfoDialog(Component parent, String message) {
      this.lastInfoMessage = message;
    }

    @Override
    public boolean showConfirmDialog(Component parent, String message, String title) {
      this.lastConfirmationMessage = message;
      return this.confirmationResult;
    }

    @Override
    public JFrame getMainFrame() {
      // Return the non-null dummy frame instead of null
      return dummyFrame;
    }

    @Override
    public String showInputDialog(Component parent, String message, String initialValue) {
      return this.inputDialogResult;
    }

    // Add empty implementations for all other interface methods to prevent errors.
    @Override
    public JFrame createMainWindow(String windowTitle) {
      return new JFrame();
    }

    @Override
    public JDialog createDialog(Component parent, String title, int width, int height) {
      return new JDialog();
    }

    @Override
    public JPanel createFormPanel() {
      return new JPanel();
    }

    @Override
    public JPanel createButtonPanel() {
      return new JPanel();
    }

    @Override
    public JButton createStyledButton(String text) {
      return new JButton(text);
    }

    @Override
    public JTextField createStyledTextField(String text, int columns) {
      return new JTextField(text, columns);
    }

    @Override
    public JTextField createMonospaceTextField(String text, int columns) {
      return new JTextField(text, columns);
    }

    @Override
    public JComboBox<String> createStyledComboBox(String[] items) {
      return new JComboBox<>(items);
    }

    @Override
    public JLabel getCurrentDateLabel() {
      return new JLabel();
    }

    @Override
    public JLabel getCalendarInfoLabel() {
      return new JLabel();
    }

    @Override
    public JList<String> getEventsList() {
      return new JList<>();
    }

    @Override
    public DefaultListModel<String> getEventsListModel() {
      return new DefaultListModel<>();
    }

    @Override
    public JComboBox<String> getCalendarSelector() {
      return new JComboBox<>();
    }

    @Override
    public JButton getPrevWeekButton() {
      return new JButton();
    }

    @Override
    public JButton getNextWeekButton() {
      return new JButton();
    }

    @Override
    public JButton getGoToDateButton() {
      return new JButton();
    }

    @Override
    public JButton getCreateEventButton() {
      return new JButton();
    }

    @Override
    public JButton getCreateSeriesButton() {
      return new JButton();
    }

    @Override
    public JButton getRefreshButton() {
      return new JButton();
    }

    @Override
    public JButton getShowStatusButton() {
      return new JButton();
    }

    @Override
    public JButton getSearchEventsButton() {
      return new JButton();
    }

    @Override
    public JButton getNewCalendarButton() {
      return new JButton();
    }

    @Override
    public void addCalendarSelectorListener(ActionListener listener) {
      // empty implementation to avoid errors
    }

    @Override
    public void addEventsListMouseListener(MouseListener listener) {
      // empty implementation to avoid errors
    }

    @Override
    public void updateCalendarSelector(List<String> calendarNames, String currentCalendar) {
      // empty implementation to avoid errors
    }

    @Override
    public void updateCalendarInfo(String calendarName, String timezone) {
      // empty implementation to avoid errors
    }

    @Override
    public void updateCurrentDateLabel(LocalDate startDate, LocalDate endDate) {
      // empty implementation to avoid errors
    }

    @Override
    public String formatEventForDisplay(IEvent event, int index) {
      return "";
    }

    @Override
    public String formatSearchResultForDisplay(IEvent event, int index) {
      return "";
    }

    @Override
    public DateTimeFormatter getDateFormatter() {
      return DateTimeFormatter.ISO_LOCAL_DATE;
    }

    @Override
    public DateTimeFormatter getDisplayDateFormatter() {
      return DateTimeFormatter.ISO_LOCAL_DATE;
    }
  }

  @Before
  public void setUp() {
    system = new CalendarSystem();
    fakeView = new FakeView();
    controller = new CalendarGUIController(system, fakeView);
  }

  @Test
  public void testHandleCreateCalendarActionSuccess() throws CalendarException {
    //call the controller's public method directly, simulating a button click
    controller.handleCreateCalendarAction("Work", "America/New_York");
    assertEquals("Work", system.getCurrentCalendarName());
    assertEquals(ZoneId.of("America/New_York"), system.getCalendarTimezone("Work"));
    assertEquals("Calendar 'Work' created and activated successfully!", fakeView.lastInfoMessage);
  }

  @Test
  public void testHandleCreateCalendarActionDuplicateName() {
    controller.handleCreateCalendarAction("Work", "UTC");
    // try to create another with the same name
    controller.handleCreateCalendarAction("Work", "America/Chicago");
    assertEquals("Calendar with name 'Work' already exists", fakeView.lastErrorMessage);
  }

  @Test
  public void testHandleCreateCalendarActionEmptyName() {
    // call the controller's public method with invalid input.
    controller.handleCreateCalendarAction("   ", "UTC"); // Empty name
    assertEquals("Calendar name cannot be empty", fakeView.lastErrorMessage);
  }

  @Test
  public void testHandleCreateCalendarActionInvalidTimezone() {
    controller.handleCreateCalendarAction("Personal", "Invalid/Zone");
    assertEquals("Unknown time-zone ID: Invalid/Zone", fakeView.lastErrorMessage);
  }

  @Test
  public void testHandleUseCalendarAction() {
    controller.handleCreateCalendarAction("Work", "UTC");
    controller.handleCreateCalendarAction("Home", "UTC");
    controller.handleUseCalendarAction("Home");
    assertEquals("Home", system.getCurrentCalendarName());
  }

  @Test
  public void testHandleUseNonExistentCalendar() {
    controller.handleUseCalendarAction("NonExistent");
    assertEquals("Could not switch calendar: Calendar not found: NonExistent",
            fakeView.lastErrorMessage);
  }

  private EventFormFields createPopulatedEventFormFields(String subject, String startDate,
                                                         String startTime,
                                                         String endDate, String endTime) {
    EventFormFields fields = new EventFormFields();
    fields.subjectField = new JTextField(subject);
    fields.startDateField = new JTextField(startDate);
    fields.startTimeField = new JTextField(startTime);
    fields.endDateField = new JTextField(endDate);
    fields.endTimeField = new JTextField(endTime);
    fields.descriptionField = new JTextField("");
    fields.locationCombo = new JComboBox<>();
    fields.locationDetailField = new JTextField("");
    fields.statusCombo = new JComboBox<>();
    return fields;
  }

  private SeriesFormFields createPopulatedSeriesFormFields(String subject, String startDate,
                                                           String startTime,
                                                           String endTime, boolean[] selectedDays,
                                                           boolean isForTimes, int times,
                                                           String untilDate) {
    SeriesFormFields fields = new SeriesFormFields();
    fields.subjectField = new JTextField(subject);
    fields.startDateField = new JTextField(startDate);
    fields.startTimeField = new JTextField(startTime);
    fields.endTimeField = new JTextField(endTime);

    fields.dayBoxes = new JCheckBox[7];
    for (int i = 0; i < 7; i++) {
      fields.dayBoxes[i] = new JCheckBox("Day" + i, selectedDays[i]);
    }

    fields.forTimesRadio = new JRadioButton("For times", isForTimes);
    fields.untilDateRadio = new JRadioButton("Until date", !isForTimes);

    int spinnerValue = Math.max(1, times);
    fields.timesSpinner = new JSpinner(new SpinnerNumberModel(spinnerValue, 1, 100, 1));

    fields.endDateField = new JTextField(untilDate);

    // Initialize all optional fields to prevent NullPointerExceptions
    fields.descriptionField = new JTextField("");
    fields.locationCombo = new JComboBox<>();
    fields.statusCombo = new JComboBox<>();

    return fields;
  }

  @Test
  public void testHandleCreateEventActionSuccess() {
    controller.handleCreateCalendarAction("Work", "UTC");
    EventFormFields fields = createPopulatedEventFormFields(
            "Team Meeting", "2025-10-20", "14:00", "2025-10-20", "15:00");

    controller.handleCreateEventAction(null, fields);

    ICalendar calendar = system.getCurrentCalendar();
    List<IEvent> events = calendar.getEventsList(LocalDate.of(2025, 10, 20));
    assertEquals(1, events.size());
    assertEquals("Team Meeting", events.get(0).getSubject());
    assertEquals("Calendar 'Work' created and activated successfully!",
            fakeView.lastInfoMessage);
  }

  @Test
  public void testHandleCreateEventActionFailsWithNoActiveCalendar() {
    EventFormFields fields = createPopulatedEventFormFields(
            "Meeting", "2025-10-10", "10:00", "2025-10-10", "11:00");
    controller.handleCreateEventAction(null, fields);
    assertEquals("No calendar is currently in use", fakeView.lastErrorMessage);
  }

  @Test
  public void testHandleCreateEventActionWithInvalidDate() throws CalendarException {
    controller.handleCreateCalendarAction("Work", "UTC");
    EventFormFields fields = createPopulatedEventFormFields(
            "Meeting", "invalid-date", "10:00", "2025-10-10", "11:00");
    controller.handleCreateEventAction(null, fields);
    assertEquals("Please check your date and time formats:\n" +
                    "\n" +
                    "• Date format: YYYY-MM-DD\n" +
                    "• Time format: HH:MM (24-hour format)\n" +
                    "\n" +
                    "Error details: Text 'invalid-date' could not be parsed at index 0",
            fakeView.lastErrorMessage);
  }

  @Test
  public void testHandleCreateEventActionEndTimeBeforeStart() {
    controller.handleCreateCalendarAction("Work", "UTC");
    EventFormFields fields = createPopulatedEventFormFields(
            "Meeting", "2025-10-10", "11:00", "2025-10-10", "10:00");
    controller.handleCreateEventAction(null, fields);
    assertEquals("End time must be after start time", fakeView.lastErrorMessage);
  }

  @Test
  public void testHandleCreateEventWithConflictUserConfirms() {
    controller.handleCreateCalendarAction("Work", "UTC");
    EventFormFields fields1 = createPopulatedEventFormFields(
            "Existing Event", "2025-10-10", "10:00", "2025-10-10", "12:00");
    controller.handleCreateEventAction(null, fields1);

    fakeView.confirmationResult = true; // Simulate user clicking "Yes"

    EventFormFields fields2 = createPopulatedEventFormFields(
            "Overlapping Event", "2025-10-10", "11:00", "2025-10-10", "13:00");
    controller.handleCreateEventAction(null, fields2);

    assertTrue(fakeView.lastConfirmationMessage.contains(
            "This event conflicts with existing events"));
    assertEquals("Calendar 'Work' created and activated successfully!", fakeView.lastInfoMessage);
    assertEquals(2, system.getCurrentCalendar().getEventsList(
            LocalDate.of(2025, 10, 10)).size());
  }

  @Test
  public void testHandleCreateEventWithConflictUserCancels() {
    controller.handleCreateCalendarAction("Work", "UTC");
    EventFormFields fields1 = createPopulatedEventFormFields(
            "Existing Event", "2025-10-10", "10:00", "2025-10-10", "12:00");
    controller.handleCreateEventAction(null, fields1);

    fakeView.lastInfoMessage = null; // reset info message from first creation
    fakeView.confirmationResult = false; // simulate user clicking "No"

    EventFormFields fields2 = createPopulatedEventFormFields(
            "Overlapping Event", "2025-10-10", "11:00", "2025-10-10", "13:00");
    controller.handleCreateEventAction(null, fields2);

    assertTrue(fakeView.lastConfirmationMessage.contains(
            "This event conflicts with existing events"));
    assertNull(fakeView.lastInfoMessage);
    assertEquals(1, system.getCurrentCalendar().getEventsList(LocalDate.of(2025, 10, 10)).size());
  }

  @Test
  public void testEventCreatedInCorrectCalendarWhenMultipleExist() throws CalendarException {
    // Verifies that events are added only to the currently active calendar.
    controller.handleCreateCalendarAction("Work", "UTC");
    controller.handleCreateCalendarAction("Home", "UTC");

    controller.handleUseCalendarAction("Home");

    // create an event
    controller.handleCreateEventAction(null, createPopulatedEventFormFields(
            "Dinner", "2025-11-05", "18:00", "2025-11-05", "19:00"));

    ICalendar homeCal = system.getCalendar("Home");
    ICalendar workCal = system.getCalendar("Work");

    assertEquals( 1, homeCal.getEventsList(LocalDate.of(2025, 11, 5)).size());
    assertTrue( workCal.getEventsList(LocalDate.of(2025, 11, 5)).isEmpty());
  }

  @Test
  public void testHandleEditEventActionSuccess() throws CalendarException {
    controller.handleCreateCalendarAction("Work", "UTC");
    // create an event and the form fields with the new data.
    IEvent originalEvent = system.getCurrentCalendar().createEvent("Initial Event",
            LocalDateTime.of(2025, 1, 1, 10, 0),
            LocalDateTime.of(2025, 1, 1, 11, 0));
    EventFormFields editedFields = createPopulatedEventFormFields("Updated Event",
            "2025-01-01", "10:00", "2025-01-01", "12:00");

    controller.handleEditEventAction(originalEvent, editedFields);

    List<IEvent> events = system.getCurrentCalendar().getEventsList(LocalDate.of(2025, 1, 1));
    assertEquals(1, events.size());
    assertEquals("Updated Event", events.get(0).getSubject());
    assertEquals(LocalTime.of(12, 0), events.get(0).getEnd().toLocalTime());
    assertTrue(fakeView.lastInfoMessage.contains("updated successfully"));
  }

  @Test
  public void testHandleEditEventToCauseConflict() throws CalendarException {
    controller.handleCreateCalendarAction("Work", "UTC");
    system.getCurrentCalendar().createEvent("Existing Event",
            LocalDateTime.of(2025, 1, 1, 12, 0),
            LocalDateTime.of(2025, 1, 1, 13, 0));
    IEvent eventToEdit = system.getCurrentCalendar().createEvent("To Be Edited",
            LocalDateTime.of(2025, 1, 1, 9, 0),
            LocalDateTime.of(2025, 1, 1, 10, 0));

    // Edit the event to overlap with "Existing Event"
    EventFormFields editedFields = createPopulatedEventFormFields("To Be Edited",
            "2025-01-01", "11:30", "2025-01-01", "12:30");
    fakeView.confirmationResult = false; // Simulate user clicking "No"

    controller.handleEditEventAction(eventToEdit, editedFields);

    assertTrue(fakeView.lastConfirmationMessage.contains("conflicts with existing events"));
    // Verify the edit was cancelled and the event was not changed
    IEvent finalEvent = system.getCurrentCalendar().getEventsByDetails("To Be Edited",
            LocalDateTime.of(2025, 1, 1, 9, 0),
            LocalDateTime.of(2025, 1, 1, 10, 0)).get(0);
    assertNotNull(finalEvent);
  }

  @Test
  public void testHandleCreateSeriesForCount() throws CalendarException {
    // set up a calendar to use.
    controller.handleCreateCalendarAction("Work", "UTC");
    fakeView.lastInfoMessage = null; // Clear message from setup

    SeriesFormFields fields = createPopulatedSeriesFormFields(
            "Weekly Report", "2025-07-07", "10:00", "10:30",
            new boolean[]{true, false, false, false, false, false, false}, // Select Monday
            true, 4, ""
    );

    controller.handleCreateSeriesAction(null, fields);

    ICalendar calendar = system.getCurrentCalendar();
    assertEquals(1, calendar.getEventsList(LocalDate.of(2025, 7, 7)).size());
    assertEquals(1, calendar.getEventsList(LocalDate.of(2025, 7, 14)).size());
    assertEquals(1, calendar.getEventsList(LocalDate.of(2025, 7, 21)).size());
    assertEquals(1, calendar.getEventsList(LocalDate.of(2025, 7, 28)).size());
    assertEquals(0, calendar.getEventsList(LocalDate.of(2025, 8, 4)).size());
  }


  @Test
  public void testHandleCreateSeriesUntilDate() {
    controller.handleCreateCalendarAction("Work", "UTC");
    fakeView.lastInfoMessage = null; // Clear setup message

    // Create a daily series for one week (Mon-Fri) ending on a specific date
    SeriesFormFields fields = createPopulatedSeriesFormFields(
            "Daily Scrum", "2025-07-28", "09:00", "09:15",
            new boolean[]{true, true, true, true, true, false, false}, // M,T,W,R,F
            false, 0, "2025-08-01" // isForTimes is false, repeats=0, untilDate is set
    );
    controller.handleCreateSeriesAction(null, fields);

    ICalendar calendar = system.getCurrentCalendar();
    // Should create 5 events from Mon, Jul 28 to Fri, Aug 1
    assertEquals(1, calendar.getEventsList(LocalDate.of(2025, 7, 28)).size()); // Mon
    assertEquals(1, calendar.getEventsList(LocalDate.of(2025, 7, 29)).size()); // Tue
    assertEquals(1, calendar.getEventsList(LocalDate.of(2025, 7, 30)).size()); // Wed
    assertEquals(1, calendar.getEventsList(LocalDate.of(2025, 7, 31)).size()); // Thu
    assertEquals(1, calendar.getEventsList(LocalDate.of(2025, 8, 1)).size());  // Fri
    // Verify no events are created after the end date
    assertEquals(0, calendar.getEventsList(LocalDate.of(2025, 8, 4)).size()); // Next Monday
  }

  @Test
  public void testHandleCreateSeries_InvalidUntilDate() {
    // Arrange: Create a series where the 'until' date is before the start date.
    SeriesFormFields fields = createPopulatedSeriesFormFields(
            "Bad Series", "2025-08-10", "10:00", "11:00",
            new boolean[]{true, false, false, false, false, false, false}, // Monday
            false, 0, "2025-08-01" // End date is before start date
    );
    controller.handleCreateSeriesAction(null, fields);

    // check that the controller caught the exception from the model and showed an error.
    assertEquals("Could not check for conflicts: null", fakeView.lastErrorMessage);
  }

  @Test
  public void testHandleCreateSeriesFailsWithNoDaysSelected() {
    controller.handleCreateCalendarAction("Work", "UTC");
    SeriesFormFields fields = new SeriesFormFields();
    fields.subjectField = new JTextField("Weekly Report");
    fields.startDateField = new JTextField("2025-07-07");
    fields.startTimeField = new JTextField("10:00");
    fields.endTimeField = new JTextField("10:30");
    fields.dayBoxes = new JCheckBox[7]; // No days selected
    for (int i = 0; i < 7; i++) {
      fields.dayBoxes[i] = new JCheckBox("", false);
    }
    fields.forTimesRadio = new JRadioButton("", true);
    fields.timesSpinner = new JSpinner(new SpinnerNumberModel(4, 1, 100, 1));

    controller.handleCreateSeriesAction(null, fields);
    assertEquals("At least one day must be selected", fakeView.lastErrorMessage);
  }

  @Test
  public void testShowStatusActionWhenBusy() throws CalendarException {
    // create a calendar and an event to make the user "busy"
    controller.handleCreateCalendarAction("Work", "UTC");
    EventFormFields fields = createPopulatedEventFormFields(
            "Team Meeting", "2025-10-20", "14:00", "2025-10-20", "15:00");
    controller.handleCreateEventAction(null, fields);

    // call the public handler method directly with a time that is within the event.
    controller.handleShowStatusAction("2025-10-20", "14:30");

    String expectedMessage = "You are BUSY at 2025-10-20 14:30\nYou have an event scheduled " +
            "at that time.";
    assertEquals(expectedMessage, fakeView.lastInfoMessage);
  }

  @Test
  public void testShowStatusActionWhenAvailable() throws CalendarException {
    controller.handleCreateCalendarAction("Work", "UTC");
    controller.handleShowStatusAction("2025-10-20", "10:00");

    String expectedMessage = "You are AVAILABLE at 2025-10-20 10:00\nNo events " +
            "scheduled at that time.";
    assertEquals(expectedMessage, fakeView.lastInfoMessage);
  }

  @Test
  public void testSearchEventsActionInvalidDate() {
    controller.handleCreateCalendarAction("Work", "UTC");
    controller.handleSearchAction("not-a-real-date");
    assertEquals("Please use YYYY-MM-DD format.", fakeView.lastErrorMessage);
  }

  @Test
  public void testSearchEventsAction_NoActiveCalendar() {
    controller = new CalendarGUIController(new CalendarSystem(), fakeView);
    controller.handleSearchAction("2025-01-01");
    assertEquals("No calendar in use.", fakeView.lastErrorMessage);
  }
}