package calendar.controller;

import javax.swing.JComboBox;
import javax.swing.JTextField;

/**
 * A data-transfer-object (DTO) to hold all the Swing components
 * from the event creation/editing form. This makes it easier to pass
 * form data between methods.
 */
public class EventFormFields {
  public JTextField subjectField;
  public JTextField startDateField;
  public JTextField startTimeField;
  public JTextField endDateField;
  public JTextField endTimeField;
  public JTextField descriptionField;
  public JComboBox<String> locationCombo;
  public JTextField locationDetailField;
  public JComboBox<String> statusCombo;
}