package calendar.view;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import calendar.model.Event;

public interface ICalendarView {
  void showMenu();

  void writeMessage(String message);

  void showCalendarEvents(List<Event> events, LocalDate date);

  void showCalendarEventsInDateRange(LocalDateTime start, LocalDateTime end,
                                     List<Event> events);

  void farewellMessage();
}
