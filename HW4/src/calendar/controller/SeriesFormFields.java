package calendar.controller;

import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;

/**
 * Container class for form fields used in creating event series.
 * Holds references to all UI components needed for series creation dialog.
 */

public class SeriesFormFields {

  /**
   * Text field for entering the event subject/title.
   */
  public JTextField subjectField;

  /**
   * Text field for entering the start date.
   */
  public JTextField startDateField;

  /**
   * Text field for entering the start time.
   */
  public JTextField startTimeField;

  /**
   * Text field for entering the end time.
   */
  public JTextField endTimeField;

  /**
   * Checkboxes for selecting days of the week.
   */
  public JCheckBox[] dayBoxes;

  /**
   * Radio button for specifying number of occurrences.
   */
  public JRadioButton forTimesRadio;

  /**
   * Radio button for specifying end date.
   */
  public JRadioButton untilDateRadio;

  /**
   * Spinner for selecting number of occurrences.
   */
  public JSpinner timesSpinner;

  /**
   * Text field for entering the end date.
   */
  public JTextField endDateField;

  /**
   * Text field for entering the event description.
   */
  public JTextField descriptionField;

  /**
   * Combo box for selecting the event location.
   */
  public JComboBox<String> locationCombo;

  /**
   * Combo box for selecting the event status.
   */
  public JComboBox<String> statusCombo;
}
