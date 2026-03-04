package calendar.view;

import calendar.model.IEvent;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A panel that renders a month grid with one cell per day and compact
 * event listings inside each day block, similar to a modern month view.
 */
public class MonthViewPanel extends JPanel {
  private static final int HEADER_HEIGHT = 30;
  private static final int CELL_PADDING = 4;
  private static final int MAX_EVENTS_PER_DAY = 3;

  private YearMonth currentMonth;
  private Map<LocalDate, List<IEvent>> eventsByDay;
  private Consumer<LocalDate> dayClickListener;

  // Cached layout for click handling
  private final Map<LocalDate, CellBounds> dayBounds = new HashMap<>();

  public MonthViewPanel() {
    this.eventsByDay = new HashMap<>();
    setBackground(Color.WHITE);
    setPreferredSize(new Dimension(800, 400));

    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        handleMouseClick(e.getX(), e.getY());
      }
    });
  }

  /**
   * Sets the data for the month view.
   *
   * @param month       the month to display
   * @param eventsByDay mapping from LocalDate to that day's events
   */
  public void setMonthData(YearMonth month, Map<LocalDate, List<IEvent>> eventsByDay) {
    this.currentMonth = month;
    this.eventsByDay = new HashMap<>();
    if (eventsByDay != null) {
      for (Map.Entry<LocalDate, List<IEvent>> entry : eventsByDay.entrySet()) {
        this.eventsByDay.put(entry.getKey(), new ArrayList<>(entry.getValue()));
      }
    }
    repaint();
  }

  /**
   * Registers a listener that will be notified when a day cell is clicked.
   *
   * @param listener consumer that receives the clicked LocalDate
   */
  public void setDayClickListener(Consumer<LocalDate> listener) {
    this.dayClickListener = listener;
  }

  private void handleMouseClick(int x, int y) {
    for (Map.Entry<LocalDate, CellBounds> entry : dayBounds.entrySet()) {
      CellBounds bounds = entry.getValue();
      if (bounds.contains(x, y)) {
        if (dayClickListener != null) {
          dayClickListener.accept(entry.getKey());
        }
        return;
      }
    }
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    if (currentMonth == null) {
      return;
    }

    Graphics2D g2 = (Graphics2D) g.create();
    int width = getWidth();
    int height = getHeight();

    // Weekday header labels
    String[] weekdayLabels = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    int columnCount = 7;
    int cellWidth = width / columnCount;

    g2.setFont(g2.getFont().deriveFont(Font.BOLD, 12f));
    g2.setColor(new Color(245, 245, 245));
    g2.fillRect(0, 0, width, HEADER_HEIGHT);
    g2.setColor(Color.DARK_GRAY);
    for (int i = 0; i < columnCount; i++) {
      int x = i * cellWidth;
      g2.drawString(weekdayLabels[i], x + CELL_PADDING, HEADER_HEIGHT - 8);
    }

    int availableHeight = height - HEADER_HEIGHT;

    // Determine how many rows (weeks) are needed
    LocalDate firstOfMonth = currentMonth.atDay(1);
    DayOfWeek firstDow = firstOfMonth.getDayOfWeek();
    int firstColumnIndex = dayOfWeekToColumn(firstDow);
    int daysInMonth = currentMonth.lengthOfMonth();
    int totalCells = firstColumnIndex + daysInMonth;
    int rowCount = (int) Math.ceil(totalCells / (double) columnCount);
    rowCount = Math.max(rowCount, 5);

    int cellHeight = availableHeight / rowCount;
    int gridTop = HEADER_HEIGHT;

    dayBounds.clear();

    g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 11f));

    // Draw grid and days
    g2.setColor(Color.LIGHT_GRAY);
    g2.setStroke(new BasicStroke(1f));

    LocalDate date = firstOfMonth;
    int dayIndex = 0;

    for (int row = 0; row < rowCount; row++) {
      for (int col = 0; col < columnCount; col++) {
        int cellX = col * cellWidth;
        int cellY = gridTop + row * cellHeight;

        g2.setColor(Color.LIGHT_GRAY);
        g2.drawRect(cellX, cellY, cellWidth, cellHeight);

        // Skip cells before the first day
        if (row == 0 && col < firstColumnIndex) {
          continue;
        }
        if (dayIndex >= daysInMonth) {
          continue;
        }

        LocalDate currentDay = currentMonth.atDay(dayIndex + 1);
        dayIndex++;

        dayBounds.put(currentDay, new CellBounds(cellX, cellY, cellWidth, cellHeight));

        // Day number
        g2.setColor(Color.BLACK);
        g2.drawString(Integer.toString(currentDay.getDayOfMonth()),
                cellX + CELL_PADDING, cellY + 14);

        // Events (up to MAX_EVENTS_PER_DAY)
        List<IEvent> events = eventsByDay.getOrDefault(currentDay, new ArrayList<>());
        if (!events.isEmpty()) {
          int eventY = cellY + 18;
          int eventsShown = 0;
          for (IEvent event : events) {
            if (eventsShown >= MAX_EVENTS_PER_DAY) {
              break;
            }
            String label = buildEventLabel(event);

            g2.setColor(new Color(220, 235, 255));
            g2.fillRoundRect(cellX + CELL_PADDING, eventY - 11,
                    cellWidth - 2 * CELL_PADDING, 16, 6, 6);
            g2.setColor(new Color(70, 120, 200));
            g2.drawRoundRect(cellX + CELL_PADDING, eventY - 11,
                    cellWidth - 2 * CELL_PADDING, 16, 6, 6);

            g2.drawString(label, cellX + CELL_PADDING + 3, eventY);
            eventY += 18;
            eventsShown++;
          }

          int remaining = events.size() - eventsShown;
          if (remaining > 0 && eventY + 14 < cellY + cellHeight) {
            g2.setColor(new Color(80, 80, 80));
            g2.drawString("+" + remaining + " more",
                    cellX + CELL_PADDING + 3, eventY);
          }
        }
      }
    }

    g2.dispose();
  }

  private int dayOfWeekToColumn(DayOfWeek dow) {
    // Monday = 0, ..., Sunday = 6
    int index = dow.getValue() - DayOfWeek.MONDAY.getValue();
    if (index < 0) {
      index += 7;
    }
    return index;
  }

  private String buildEventLabel(IEvent event) {
    String time = String.format("%02d:%02d",
            event.getStart().getHour(), event.getStart().getMinute());
    return time + " " + event.getSubject();
  }

  private static class CellBounds {
    final int x;
    final int y;
    final int width;
    final int height;

    CellBounds(int x, int y, int width, int height) {
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
    }

    boolean contains(int px, int py) {
      return px >= x && px <= x + width && py >= y && py <= y + height;
    }
  }
}

