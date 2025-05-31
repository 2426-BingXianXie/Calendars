package calendar;

import java.time.*;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ICalendar {
  Event createEvent(String subject, LocalDateTime startDate, LocalDateTime endDate,
                    String description, Location location, EventStatus eventStatus)
          throws IllegalArgumentException;

  EventSeries createEventSeries(String subject, LocalTime startTime, Duration duration,
                                Set<Days> daysOfWeek, LocalDate startDate, LocalDate endDate,
                                int repeats, String description, Location location,
                                EventStatus eventStatus) throws IllegalArgumentException;

  List<Event> getEventsList(LocalDate date);

  List<Event> getEventsListInDateRange(LocalDate start, LocalDate end);

  // could implement Property enum
  Event editEvent(UUID eventID, String property, String newProperty);

  void editSeriesFromDate(UUID seriesID, String property, String newProperty);

  void editSeries(UUID seriesID, String property, String newProperty);


}
