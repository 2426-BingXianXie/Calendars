package calendar.view;

import calendar.model.IEvent;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A panel that renders a week view with 7 day columns, each showing a vertical
 * time axis and event blocks, similar to a modern calendar week view.
 */
public class WeekViewPanel extends JPanel {
  private static final int HOURS_IN_DAY = 24;
  private static final int TOP_MARGIN = 20;
  private static final int BOTTOM_MARGIN = 20;
  private static final int LEFT_MARGIN = 50;
  private static final int RIGHT_MARGIN = 20;
  private static final int DAY_GAP = 4;

  private LocalDate weekStartDate;
  private Map<LocalDate, List<IEvent>> eventsByDay;

  public WeekViewPanel() {
    this.eventsByDay = new HashMap<>();
    setBackground(Color.WHITE);
    setPreferredSize(new Dimension(900, 400));
  }

  /**
   * Updates the week data to be displayed.
   *
   * @param weekStart the first day of the week
   * @param eventsByDay mapping from each date in the week to its events
   */
  public void setWeekData(LocalDate weekStart,
                          Map<LocalDate, List<IEvent>> eventsByDay) {
    this.weekStartDate = weekStart;
    this.eventsByDay = new HashMap<>();
    if (eventsByDay != null) {
      for (Map.Entry<LocalDate, List<IEvent>> entry : eventsByDay.entrySet()) {
        this.eventsByDay.put(entry.getKey(), new ArrayList<>(entry.getValue()));
      }
    }
    repaint();
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    if (weekStartDate == null) {
      return;
    }

    int width = getWidth();
    int height = getHeight();

    int timelineTop = TOP_MARGIN;
    int timelineBottom = height - BOTTOM_MARGIN;
    int timelineHeight = Math.max(1, timelineBottom - timelineTop);

    int availableWidth = width - LEFT_MARGIN - RIGHT_MARGIN;
    int dayColumnWidth = (availableWidth - (DAY_GAP * 6)) / 7;

    // Background for day columns
    g.setFont(g.getFont().deriveFont(Font.PLAIN, 11f));

    for (int i = 0; i < 7; i++) {
      int x = LEFT_MARGIN + i * (dayColumnWidth + DAY_GAP);
      g.setColor(new Color(245, 245, 245));
      g.fillRect(x, timelineTop, dayColumnWidth, timelineHeight);
      g.setColor(new Color(220, 220, 220));
      g.drawRect(x, timelineTop, dayColumnWidth, timelineHeight);

      LocalDate day = weekStartDate.plusDays(i);
      String label = day.getDayOfWeek().toString().substring(0, 3) + " "
          + day.getMonthValue() + "/" + day.getDayOfMonth();
      g.setColor(Color.DARK_GRAY);
      g.drawString(label, x + 4, timelineTop - 4);
    }

    // Hour grid and labels (shared across days)
    for (int hour = 0; hour < HOURS_IN_DAY; hour++) {
      int y = timelineTop + (int) ((hour / 24.0) * timelineHeight);
      g.setColor(new Color(230, 230, 230));
      g.drawLine(LEFT_MARGIN, y, width - RIGHT_MARGIN, y);

      g.setColor(Color.GRAY);
      String label = String.format("%02d:00", hour);
      g.drawString(label, 5, y + 4);
    }

    // Draw events per day
    for (int i = 0; i < 7; i++) {
      LocalDate day = weekStartDate.plusDays(i);
      List<IEvent> events = eventsByDay.get(day);
      if (events == null || events.isEmpty()) {
        continue;
      }

      int x = LEFT_MARGIN + i * (dayColumnWidth + DAY_GAP);
      int eventX = x + 3;
      int eventWidth = dayColumnWidth - 6;

      for (IEvent event : events) {
        LocalDateTime start = event.getStart();
        LocalDateTime end = event.getEnd();

        int startMinutes = start.getHour() * 60 + start.getMinute();
        int endMinutes = end.getHour() * 60 + end.getMinute();

        startMinutes = Math.max(0, Math.min(24 * 60, startMinutes));
        endMinutes = Math.max(startMinutes + 15, Math.min(24 * 60, endMinutes));

        double startRatio = startMinutes / (24.0 * 60.0);
        double endRatio = endMinutes / (24.0 * 60.0);

        int yStart = timelineTop + (int) (startRatio * timelineHeight);
        int yEnd = timelineTop + (int) (endRatio * timelineHeight);
        int eventHeight = Math.max(18, yEnd - yStart);

        g.setColor(new Color(102, 153, 255, 220));
        g.fillRoundRect(eventX, yStart + 1, eventWidth, eventHeight - 2, 8, 8);
        g.setColor(new Color(0, 70, 160));
        g.drawRoundRect(eventX, yStart + 1, eventWidth, eventHeight - 2, 8, 8);

        g.setColor(Color.WHITE);
        String timeRange = String.format("%02d:%02d-%02d:%02d",
            start.getHour(), start.getMinute(),
            end.getHour(), end.getMinute());
        String text = timeRange + " " + event.getSubject();
        g.drawString(text, eventX + 4, yStart + 14);
      }
    }
  }
}

