package calendar;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a virtual calendar that allows you to create events and store them
 * in the calendar.
 *
 * <p>This calendar mimics the features found in widely-used calendar apps, such as
 * Google Calendar and Apple's Calendar app.
 */
public class VirtualCalendar {
  private Map<LocalDate, List<Event>> calendarEvents;
  private Set<Event> uniqueEvents;
  private Map<UUID, Event> eventsByID;
  private Map<UUID, EventSeries> eventSeriesByID;

  public VirtualCalendar() {
    this.calendarEvents = new HashMap<LocalDate, List<Event>>();
    this.uniqueEvents = new HashSet<Event>();
    this.eventsByID = new HashMap<UUID, Event>();
    this.eventSeriesByID = new HashMap<UUID, EventSeries>();
  }

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
  public Event createEvent(String subject, LocalDateTime startDate, LocalDateTime endDate,
                           String description, Location location, EventStatus eventStatus)
          throws IllegalArgumentException {
    if (endDate == null) { // no end date, so event should be an "all day event"
      // set end date to same day, 5pm
      endDate = LocalDateTime.of(startDate.toLocalDate(), LocalTime.of(17, 0));
      // set start date to same day, 8am
      startDate = LocalDateTime.of(startDate.toLocalDate(), LocalTime.of(8, 0));
    }
    if (startDate.isAfter(endDate)) { // check for valid dates
      throw new IllegalArgumentException("Start date cannot be after end date");
    }
    Event event = new Event(subject, startDate, endDate, description, location, eventStatus, null);
    if (uniqueEvents.add(event)) { // check for duplicate event
      // add event to each day from start to end date
      for (LocalDate date = startDate.toLocalDate(); !date.isAfter(endDate.toLocalDate());
           date = date.plusDays(1)) {
        calendarEvents.computeIfAbsent(date, k -> new ArrayList<Event>()).add(event);
      }
      eventsByID.put(event.getId(), event);
      return event;
    } else {
      throw new IllegalArgumentException("Event already exists");
    }
  }

  public void createEventSeries(String subject, LocalTime startTime, Duration duration,
                                Set<Days> daysOfWeek, LocalDate startDate, LocalDate endDate,
                                int repeats, String description, Location location, EventStatus eventStatus) {

  }


  public Event editEvent(Event event, String property, String newProperty, LocalDateTime fromDate, LocalDateTime toDate) {

  }

  public void editSeries(Event event, String property, String newProperty, LocalDateTime fromDate) {

  }


}
