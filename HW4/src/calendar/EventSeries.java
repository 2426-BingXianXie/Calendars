package calendar;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class EventSeries {
  String subject;
  LocalTime startTime;
  LocalDate seriesStartDate;
  LocalDate seriesEndDate;
  Duration duration; // check that an event only lasts 1 day
  int numOccurrences;
  UUID id;
  Set<Days> daysOfRecurrence = new HashSet<Days>();
}
