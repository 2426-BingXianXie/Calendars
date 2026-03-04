package calendar.view;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A simple year overview panel that shows 12 months in a grid and allows
 * clicking a month to drill down into that month view.
 */
public class YearViewPanel extends JPanel {
  private static final int ROWS = 4;
  private static final int COLS = 3;
  private static final int CELL_PADDING = 10;

  private int year;
  private Consumer<YearMonth> monthClickListener;
  private final Map<YearMonth, CellBounds> monthBounds = new HashMap<>();

  public YearViewPanel() {
    this.year = java.time.LocalDate.now().getYear();
    setBackground(Color.WHITE);
    setPreferredSize(new Dimension(800, 400));

    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        handleClick(e.getX(), e.getY());
      }
    });
  }

  /**
   * Sets which year to show.
   *
   * @param year the year to display
   */
  public void setYear(int year) {
    this.year = year;
    repaint();
  }

  /**
   * Registers a listener to be notified when a month is clicked.
   *
   * @param listener consumer that receives the clicked YearMonth
   */
  public void setMonthClickListener(Consumer<YearMonth> listener) {
    this.monthClickListener = listener;
  }

  private void handleClick(int x, int y) {
    for (Map.Entry<YearMonth, CellBounds> entry : monthBounds.entrySet()) {
      if (entry.getValue().contains(x, y)) {
        if (monthClickListener != null) {
          monthClickListener.accept(entry.getKey());
        }
        break;
      }
    }
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    Graphics2D g2 = (Graphics2D) g.create();
    int width = getWidth();
    int height = getHeight();

    int cellWidth = width / COLS;
    int cellHeight = height / ROWS;

    g2.setFont(g2.getFont().deriveFont(Font.BOLD, 14f));
    String[] monthNames = {
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    };

    monthBounds.clear();

    int monthIndex = 0;
    for (int row = 0; row < ROWS; row++) {
      for (int col = 0; col < COLS; col++) {
        if (monthIndex >= 12) {
          break;
        }

        int x = col * cellWidth;
        int y = row * cellHeight;

        g2.setColor(new Color(245, 245, 245));
        g2.fillRect(x + 1, y + 1, cellWidth - 2, cellHeight - 2);
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawRect(x + 1, y + 1, cellWidth - 2, cellHeight - 2);

        String label = monthNames[monthIndex] + " " + year;
        g2.setColor(Color.DARK_GRAY);
        g2.drawString(label, x + CELL_PADDING, y + CELL_PADDING + 4);

        YearMonth ym = YearMonth.of(year, monthIndex + 1);
        monthBounds.put(ym, new CellBounds(x + 1, y + 1, cellWidth - 2, cellHeight - 2));

        monthIndex++;
      }
    }

    g2.dispose();
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

