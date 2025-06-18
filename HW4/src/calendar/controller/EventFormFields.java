package calendar.controller;

import javax.swing.JComboBox;
import javax.swing.JTextField;

/**
 * A data-transfer-object (DTO) to hold all the Swing components
 * from the event creation/editing form. This makes it easier to pass
 * form data between methods.
 */
public class EventFormFields {

  /** Text field for entering the event subject/title */
  public JTextField subjectField;

  /** Text field for entering the event start date */
  public JTextField startDateField;

  /** Text field for entering the event start time */
  public JTextField startTimeField;

  /** Text field for entering the event end date */
  public JTextField endDateField;

  /** Text field for entering the event end time */
  public JTextField endTimeField;

  /** Text field for entering the detailed event description */
  public JTextField descriptionField;

  /** Combo box for selecting the event location */
  public JComboBox<String> locationCombo;

  /** Text field for entering additional location details */
  public JTextField locationDetailField;

  /** Combo box for selecting the event status */
  public JComboBox<String> statusCombo;
}