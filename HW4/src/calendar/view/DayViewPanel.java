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
import java.util.Comparator;
import java.util.List;

/**
 * A panel that renders a single day as a vertical timeline with events drawn
 * as blocks, similar to a modern calendar day view.
 */
public class DayViewPanel extends JPanel {
  private static final int HOURS_IN_DAY = 24;
  private static final int LEFT_MARGIN = 60;
  private static final int RIGHT_MARGIN = 20;
  private static final int TOP_MARGIN = 20;
  private static final int BOTTOM_MARGIN = 20;

  private LocalDate currentDate;
  private List<IEvent> currentEvents;

  public DayViewPanel() {
    this.currentEvents = new ArrayList<>();
    setBackground(Color.WHITE);
    setPreferredSize(new Dimension(800, 400));
  }

  /**
   * Updates this panel with the date and events to display.
   *
   * @param date   the day to display
   * @param events the events occurring on that day
   */
  public void setDayData(LocalDate date, List<IEvent> events) {
    this.currentDate = date;
    this.currentEvents = new ArrayList<>(events);
    this.currentEvents.sort(Comparator.comparing(IEvent::getStart));
    repaint();
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    int width = getWidth();
    int height = getHeight();

    int timelineTop = TOP_MARGIN;
    int timelineBottom = height - BOTTOM_MARGIN;
    int timelineHeight = Math.max(1, timelineBottom - timelineTop);

    // Draw background for the day column
    g.setColor(new Color(245, 245, 245));
    g.fillRect(LEFT_MARGIN, timelineTop, width - LEFT_MARGIN - RIGHT_MARGIN, timelineHeight);

    // Draw hour grid and labels
    g.setFont(g.getFont().deriveFont(Font.PLAIN, 11f));
    g.setColor(Color.GRAY);
    for (int hour = 0; hour < HOURS_IN_DAY; hour++) {
      int y = timelineTop + (int) ((hour / 24.0) * timelineHeight);
      g.setColor(new Color(220, 220, 220));
      g.drawLine(LEFT_MARGIN, y, width - RIGHT_MARGIN, y);

      g.setColor(Color.DARK_GRAY);
      String label = String.format("%02d:00", hour);
      g.drawString(label, 5, y + 4);
    }

    // Draw events
    if (currentEvents == null || currentEvents.isEmpty()) {
      return;
    }

    int eventAreaWidth = width - LEFT_MARGIN - RIGHT_MARGIN;
    int eventX = LEFT_MARGIN + 4;
    int eventWidth = eventAreaWidth - 8;

    g.setFont(g.getFont().deriveFont(Font.PLAIN, 12f));

    for (IEvent event : currentEvents) {
      LocalDateTime start = event.getStart();
      LocalDateTime end = event.getEnd();

      int startMinutes = start.getHour() * 60 + start.getMinute();
      int endMinutes = end.getHour() * 60 + end.getMinute();

      // Clamp to a reasonable range within the day
      startMinutes = Math.max(0, Math.min(24 * 60, startMinutes));
      endMinutes = Math.max(startMinutes + 15, Math.min(24 * 60, endMinutes));

      double startRatio = startMinutes / (24.0 * 60.0);
      double endRatio = endMinutes / (24.0 * 60.0);

      int yStart = timelineTop + (int) (startRatio * timelineHeight);
      int yEnd = timelineTop + (int) (endRatio * timelineHeight);
      int eventHeight = Math.max(18, yEnd - yStart);

      // Event block
      g.setColor(new Color(102, 153, 255, 220));
      g.fillRoundRect(eventX, yStart + 1, eventWidth, eventHeight - 2, 8, 8);
      g.setColor(new Color(0, 70, 160));
      g.drawRoundRect(eventX, yStart + 1, eventWidth, eventHeight - 2, 8, 8);

      // Event text: time range + subject
      g.setColor(Color.WHITE);
      String timeRange = String.format("%02d:%02d-%02d:%02d",
              start.getHour(), start.getMinute(),
              end.getHour(), end.getMinute());
      String text = timeRange + "  " + event.getSubject();

      int textY = yStart + 14;
      g.drawString(text, eventX + 6, textY);
    }
  }
}

