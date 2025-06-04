import java.io.InputStreamReader;
import calendar.controller.CalendarController;
import calendar.CalendarException;
import calendar.view.CalendarView;
import calendar.model.ICalendar;
import calendar.controller.ICalendarController;
import calendar.view.ICalendarView;
import calendar.model.VirtualCalendar;

public class CalendarRunner {
  public static void main(String[] args) throws CalendarException {
    ICalendar model = new VirtualCalendar();
    Readable rd = new InputStreamReader(System.in);
    Appendable ap = System.out;
    ICalendarView view = new CalendarView(ap);
    ICalendarController controller = new CalendarController(model, view, rd);
    controller.go();
  }
}