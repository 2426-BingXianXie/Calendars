package calendar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class Event { // need to override equals
  String subject;
  LocalDateTime startDate;
  LocalDateTime finishDate;
  String description;
  EventStatus status;
  Location location;
  UUID id;
  UUID seriesId; // null if not part of a series
}
