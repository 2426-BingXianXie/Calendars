package calendar.controller;


import calendar.CalendarException;
import calendar.model.ICalendar;
import calendar.view.ICalendarView;

public interface CalendarCommand {
  void go(ICalendar calendar) throws CalendarException;
}
