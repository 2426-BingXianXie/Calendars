package calendar;

import java.time.*;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ICalendar {

  /**
   * Creates a single, non-recurring event.
   *
   * @param subject     the subject of the event
   * @param startDate   the starting date and time of the event
   * @param endDate     the ending date and time of the event. If empty, make event an all-day event
   * @param description the description of the event. Can be left blank
   * @param location    the location of the event, either in-person or online. Can be left blank
   * @param eventStatus the status of the event, either public or private. Can be left blank
   * @return the created Event object.
   * @throws IllegalArgumentException if the given start date is chronologically after the end
   *                                  date, or if the event already exists.
   */
  Event createEvent(String subject, LocalDateTime startDate, LocalDateTime endDate,
                    String description, Location location, EventStatus eventStatus)
          throws IllegalArgumentException;

  void createEventSeries(String subject, LocalTime startTime, LocalTime endTime,
                                Set<Days> daysOfWeek, LocalDate startDate, LocalDate endDate,
                                int repeats, String description, Location location,
                                EventStatus eventStatus) throws IllegalArgumentException;

  List<Event> getEventsBySubjectAndStartTime(String subject, LocalDateTime startTime);

  List<Event> findEventsByDetails(String subject, LocalDateTime startTime, LocalDateTime endTime);

  List<Event> getEventsList(LocalDate date);

  List<Event> getEventsListInDateRange(LocalDate start, LocalDate end);

  boolean isBusyAt(LocalDateTime dateTime);

  // could implement Property enum
  Event editEvent(UUID eventID, Property property, String newProperty);

  void editSeriesFromDate(UUID seriesID, Property property, String newProperty);

  void editSeries(UUID seriesID, Property property, String newProperty);


}
