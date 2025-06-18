package calendar.controller;

import org.junit.Before;
import org.junit.Test;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;


import javax.swing.*;

import calendar.CalendarException;
import calendar.model.CalendarSystem;
import calendar.model.ICalendar;
import calendar.model.ICalendarSystem;
import calendar.model.IEvent;
import calendar.view.ICalendarGUIView;

import java.util.List;

import static org.junit.Assert.assertEquals;
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
    public boolean confirmationResult = true; //

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
    public JFrame getMainFrame() {
      return null;
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
    }

    @Override
    public void addEventsListMouseListener(MouseListener listener) {
    }

    @Override
    public void updateCalendarSelector(List<String> calendarNames, String currentCalendar) {
    }

    @Override
    public void updateCalendarInfo(String calendarName, String timezone) {
    }

    @Override
    public void updateCurrentDateLabel(LocalDate startDate, LocalDate endDate) {
    }

    @Override
    public String showInputDialog(Component parent, String message, String initialValue) {
      return "";
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
  public void testHandleCreateCalendarAction_Success() throws CalendarException {
    //call the controller's public method directly, simulating a button click
    controller.handleCreateCalendarAction("Work", "America/New_York");
    assertEquals("Work", system.getCurrentCalendarName());
    assertEquals(ZoneId.of("America/New_York"), system.getCalendarTimezone("Work"));
    assertEquals("Calendar 'Work' created and activated successfully!", fakeView.lastInfoMessage);
  }

  @Test
  public void testHandleCreateCalendarAction_DuplicateName() {
    controller.handleCreateCalendarAction("Work", "UTC");
    // try to create another with the same name
    controller.handleCreateCalendarAction("Work", "America/Chicago");
    assertEquals("Calendar with name 'Work' already exists", fakeView.lastErrorMessage);
  }

  @Test
  public void testHandleCreateCalendarAction_EmptyName() {
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

  private EventFormFields createPopulatedEventFormFields(String subject, String startDate, String startTime,
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

  @Test
  public void testHandleCreateEventAction_Success() {
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
  public void testHandleCreateEventAction_FailsWithNoActiveCalendar() {
    EventFormFields fields = createPopulatedEventFormFields(
            "Meeting", "2025-10-10", "10:00", "2025-10-10", "11:00");
    controller.handleCreateEventAction(null, fields);
    assertEquals("No calendar is currently in use", fakeView.lastErrorMessage);
  }

  @Test
  public void testHandleCreateEventAction_WithInvalidDate() throws CalendarException {
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
  public void testHandleCreateEventAction_EndTimeBeforeStart() {
    controller.handleCreateCalendarAction("Work", "UTC");
    EventFormFields fields = createPopulatedEventFormFields(
            "Meeting", "2025-10-10", "11:00", "2025-10-10", "10:00");
    controller.handleCreateEventAction(null, fields);
    assertEquals("End time must be after start time", fakeView.lastErrorMessage);
  }

  @Test
  public void testHandleCreateEventWithConflict_UserConfirms() {
    controller.handleCreateCalendarAction("Work", "UTC");
    EventFormFields fields1 = createPopulatedEventFormFields(
            "Existing Event", "2025-10-10", "10:00", "2025-10-10", "12:00");
    controller.handleCreateEventAction(null, fields1);

    fakeView.confirmationResult = true; // Simulate user clicking "Yes"

    EventFormFields fields2 = createPopulatedEventFormFields(
            "Overlapping Event", "2025-10-10", "11:00", "2025-10-10", "13:00");
    controller.handleCreateEventAction(null, fields2);

    assertTrue(fakeView.lastConfirmationMessage.contains("This event conflicts with existing events"));
    assertEquals("Calendar 'Work' created and activated successfully!", fakeView.lastInfoMessage);
    assertEquals(2, system.getCurrentCalendar().getEventsList(LocalDate.of(2025, 10, 10)).size());
  }

  @Test
  public void testHandleCreateEventWithConflict_UserCancels() {
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


}