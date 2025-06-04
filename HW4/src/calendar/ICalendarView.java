package calendar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

public interface ICalendarView {
  void showMenu();

  void writeMessage(String message);

  void showCalendarEvents(List<Event> events, LocalDate date);

  void showCalendarEventsInDateRange(LocalDateTime start, LocalDateTime end,
                                     List<Event> events);

  void farewellMessage();
}
