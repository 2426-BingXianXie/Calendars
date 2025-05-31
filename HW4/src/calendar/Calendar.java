package calendar;

import java.time.*;

// optional?
public interface Calendar {
  void createEvent(String subject, LocalDateTime fromDate, LocalDateTime toDate);

  void addEvent(int repeats, String daysOfWeek, LocalDateTime toDate); // call createEvent multiple times

  // could implement Property enum
  void editEvent(Event event, String property, String newProperty, LocalDateTime fromDate, LocalDateTime toDate); // should call helper in Event class

  void editSeries(Event event, String property, String newProperty, LocalDateTime fromDate);

  String printEvents(LocalDateTime fromDate, LocalDateTime toDate);

  String printStatus(LocalDateTime date);

}
