package calendar;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class VirtualCalendar {
  private Map<LocalDate, List<Event>> calendarEvents = new HashMap<LocalDate, List<Event>>();
  private Set<Event> uniqueEvents = new HashSet<Event>();
  private Map<UUID, Event> eventsByID = new HashMap<UUID, Event>();
  private Map<UUID, EventSeries> eventSeriesById = new HashMap<UUID, EventSeries>();



}
