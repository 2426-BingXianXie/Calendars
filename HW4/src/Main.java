import java.io.InputStreamReader;
import calendar.CalendarController;
import calendar.CalendarException;
import calendar.CalendarView;
import calendar.ICalendar;
import calendar.ICalendarController;
import calendar.ICalendarView;
import calendar.VirtualCalendar;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
  public static void main(String[] args) throws CalendarException {
    ICalendar model = new VirtualCalendar();
    Readable rd = new InputStreamReader(System.in);
    Appendable ap = System.out;
    ICalendarView view = new CalendarView(ap);
    ICalendarController controller = new CalendarController(model, view, rd);
    controller.go();
  }
}