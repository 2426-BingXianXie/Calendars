package calendar;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Event {
  String subject;
  LocalDateTime startDate;
  LocalDateTime finishDate;
  String description;
  EventStatus status;
  Location location;
}
