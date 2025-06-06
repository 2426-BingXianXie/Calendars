package calendar.controller;


/**
 * Represents an interaction between input and output data in the calendar application.
 * This interface defines a contract for processing string-based interactions where
 * input is transformed and written to an output buffer.
 */
public interface Interaction {

  /**
   * Processes the input data and writes the result to the output buffer.
   *
   * @param in  The input StringBuilder containing the data to be processed
   * @param out The output StringBuilder where the processed data will be written
   */
  void apply(StringBuilder in, StringBuilder out);
}
