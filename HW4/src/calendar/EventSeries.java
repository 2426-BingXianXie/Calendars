package calendar;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class EventSeries {
  private String subject;
  private LocalTime startTime;
  private LocalDate seriesStartDate;
  private LocalDate seriesEndDate;
  private Duration duration; // check that an event only lasts 1 day
  private int numOccurrences;
  private UUID id;
  private Set<Days> daysOfRecurrence = new HashSet<Days>();
}
