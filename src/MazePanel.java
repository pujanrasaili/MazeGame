import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MazePanel extends JPanel implements KeyListener {

    private static final int CELL_SIZE = 40;
    private static final int FOG_RADIUS = 3;
    private static final int HUD_HEIGHT = 55;

    private String mode;
    private Runnable onBackToMenu;

    private int level = 1;
    private int rows, cols;
    private Maze maze;
    private Player player;
    private boolean gameWon = false;
    private int moveCount = 0;
    private int secondsElapsed = 0;
    private Timer gameTimer;

    // Mode flags
    private boolean hasFog = false;
    private boolean hasTimed = false;
    private int timeLimit = 60;

    // Colors
    private final Color COLOR_WALL       = new Color(30, 30, 60);
    private final Color COLOR_PATH       = new Color(240, 240, 255);
    private final Color COLOR_PLAYER     = new Color(0, 200, 100);
    private final Color COLOR_START      = new Color(50, 150, 255);
    private final Color COLOR_END        = new Color(255, 80, 80);
    private final Color COLOR_FOG        = new Color(10, 10, 20);
    private final Color COLOR_WIN_BG     = new Color(0, 0, 0, 200);
    private final Color COLOR_HUD_BG     = new Color(15, 15, 35);

    public MazePanel(String mode, Runnable onBackToMenu) {
        this.mode = mode;
        this.onBackToMenu = onBackToMenu;

        // Set mode flags
        switch (mode) {
            case "TIMED"      -> { hasTimed = true; }
            case "FOG OF WAR" -> { hasFog = true; }
            case "CHALLENGE"  -> { hasTimed = true; hasFog = true; }
        }

        setFocusable(true);
        addKeyListener(this);
        startLevel();
    }

    private void startLevel() {
        rows = 9 + level * 4;
        cols = 9 + level * 4;
        if (rows % 2 == 0) rows++;
        if (cols % 2 == 0) cols++;

        maze   = new Maze(rows, cols);
        player = new Player(1, 1);
        gameWon = false;
        moveCount = 0;
        secondsElapsed = 0;
        timeLimit = 30 + level * 20; // more time per level

        setPreferredSize(new Dimension(cols * CELL_SIZE, rows * CELL_SIZE + HUD_HEIGHT));

        if (gameTimer != null) gameTimer.stop();
        gameTimer = new Timer(1000, e -> {
            if (!gameWon) {
                secondsElapsed++;
                // Time's up!
                if (hasTimed && secondsElapsed >= timeLimit) {
                    gameTimer.stop();
                    showTimeUpDialog();
                }
                repaint();
            }
        });
        gameTimer.start();

        Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.pack();
            window.setLocationRelativeTo(null);
        }

        repaint();
    }

    private void showTimeUpDialog() {
        int result = JOptionPane.showOptionDialog(
                this,
                "⏰ Time's up! You ran out of time on Level " + level + ".",
                "Time's Up!",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                new String[]{"Try Again", "Main Menu"},
                "Try Again"
        );
        if (result == 0) startLevel();
        else onBackToMenu.run();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // HUD background
        g2.setColor(COLOR_HUD_BG);
        g2.fillRect(0, 0, getWidth(), HUD_HEIGHT);

        // HUD divider line
        g2.setColor(new Color(60, 60, 100));
        g2.fillRect(0, HUD_HEIGHT - 2, getWidth(), 2);

        // Level
        g2.setColor(new Color(0, 200, 100));
        g2.setFont(new Font("Arial", Font.BOLD, 15));
        g2.drawString("Level " + level, 15, 22);

        // Mode badge
        g2.setColor(getModeColor());
        g2.fillRoundRect(15, 30, 90, 18, 8, 8);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 11));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(mode, 15 + (90 - fm.stringWidth(mode)) / 2, 43);

        // Timer
        if (hasTimed) {
            int remaining = timeLimit - secondsElapsed;
            Color timerColor = remaining <= 10 ? new Color(255, 80, 80) : new Color(255, 200, 50);
            g2.setColor(timerColor);
            g2.setFont(new Font("Arial", Font.BOLD, 20));
            String timeStr = "⏱ " + formatTime(remaining);
            fm = g2.getFontMetrics();
            g2.drawString(timeStr, (getWidth() - fm.stringWidth(timeStr)) / 2, 25);
        } else {
            g2.setColor(new Color(255, 200, 50));
            g2.setFont(new Font("Arial", Font.BOLD, 15));
            String timeStr = "⏱ " + formatTime(secondsElapsed);
            fm = g2.getFontMetrics();
            g2.drawString(timeStr, (getWidth() - fm.stringWidth(timeStr)) / 2, 25);
        }

        // Moves
        g2.setColor(new Color(100, 180, 255));
        g2.setFont(new Font("Arial", Font.BOLD, 15));
        g2.drawString("👣 " + moveCount, (getWidth() - 60) / 2, 46);

        // Controls hint
        g2.setColor(new Color(160, 160, 200));
        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        g2.drawString("M=Menu  R=Restart", getWidth() - 130, 25);
        if (hasFog) g2.drawString("F=Toggle Fog", getWidth() - 115, 42);

        // Draw maze cells
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int x = c * CELL_SIZE;
                int y = r * CELL_SIZE + HUD_HEIGHT;

                boolean inFog = hasFog && !isVisible(r, c);

                if (inFog) {
                    g2.setColor(COLOR_FOG);
                    g2.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                } else if (maze.getCell(r, c) == Maze.WALL) {
                    g2.setColor(COLOR_WALL);
                    g2.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                    g2.setColor(new Color(60, 60, 100));
                    g2.drawRect(x, y, CELL_SIZE, CELL_SIZE);
                } else {
                    g2.setColor(COLOR_PATH);
                    g2.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                }
            }
        }

        // Start cell
        if (isVisible(1, 1))
            drawSpecialCell(g2, 1, 1, COLOR_START, "S");

        // End cell
        if (isVisible(rows - 2, cols - 2))
            drawSpecialCell(g2, rows - 2, cols - 2, COLOR_END, "E");

        // Player
        int px  = player.getCol() * CELL_SIZE;
        int py  = player.getRow() * CELL_SIZE + HUD_HEIGHT;
        int pad = 6;
        g2.setColor(COLOR_PLAYER);
        g2.fillOval(px + pad, py + pad, CELL_SIZE - pad * 2, CELL_SIZE - pad * 2);
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2));
        g2.drawOval(px + pad, py + pad, CELL_SIZE - pad * 2, CELL_SIZE - pad * 2);

        // Win screen
        if (gameWon) {
            g2.setColor(COLOR_WIN_BG);
            g2.fillRect(0, HUD_HEIGHT, getWidth(), getHeight() - HUD_HEIGHT);

            g2.setColor(new Color(0, 255, 150));
            g2.setFont(new Font("Arial", Font.BOLD, 48));
            String msg = "LEVEL " + level + " CLEAR!";
            fm = g2.getFontMetrics();
            int cx = (getWidth() - fm.stringWidth(msg)) / 2;
            int cy = (getHeight() + HUD_HEIGHT) / 2 - 50;
            g2.drawString(msg, cx, cy);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.PLAIN, 18));
            String stats = "Time: " + formatTime(secondsElapsed) + "   Moves: " + moveCount;
            fm = g2.getFontMetrics();
            g2.drawString(stats, (getWidth() - fm.stringWidth(stats)) / 2, cy + 45);

            g2.setColor(new Color(255, 220, 50));
            g2.setFont(new Font("Arial", Font.BOLD, 20));
            String next = "N = Next Level    R = Restart    M = Menu";
            fm = g2.getFontMetrics();
            g2.drawString(next, (getWidth() - fm.stringWidth(next)) / 2, cy + 90);
        }
    }

    private Color getModeColor() {
        return switch (mode) {
            case "TIMED"      -> new Color(255, 150, 50);
            case "FOG OF WAR" -> new Color(150, 80, 255);
            case "CHALLENGE"  -> new Color(255, 60, 100);
            default           -> new Color(50, 150, 255);
        };
    }

    private boolean isVisible(int r, int c) {
        if (!hasFog) return true;
        return Math.abs(r - player.getRow()) <= FOG_RADIUS &&
                Math.abs(c - player.getCol()) <= FOG_RADIUS;
    }

    private void drawSpecialCell(Graphics2D g2, int row, int col, Color color, String label) {
        int x = col * CELL_SIZE;
        int y = row * CELL_SIZE + HUD_HEIGHT;
        g2.setColor(color);
        g2.fillRect(x, y, CELL_SIZE, CELL_SIZE);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 18));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(label, x + (CELL_SIZE - fm.stringWidth(label)) / 2,
                y + (CELL_SIZE + fm.getAscent()) / 2 - 3);
    }

    private String formatTime(int secs) {
        if (secs < 0) secs = 0;
        return String.format("%02d:%02d", secs / 60, secs % 60);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_M) {
            if (gameTimer != null) gameTimer.stop();
            onBackToMenu.run();
            return;
        }
        if (key == KeyEvent.VK_R) { startLevel(); return; }
        if (key == KeyEvent.VK_N && gameWon) { level++; startLevel(); return; }
        if (key == KeyEvent.VK_F && hasFog) {
            hasFog = !hasFog;
            repaint();
            return;
        }
        if (gameWon) return;

        int row = player.getRow();
        int col = player.getCol();

        switch (key) {
            case KeyEvent.VK_UP    -> row--;
            case KeyEvent.VK_DOWN  -> row++;
            case KeyEvent.VK_LEFT  -> col--;
            case KeyEvent.VK_RIGHT -> col++;
            default -> { return; }
        }

        if (maze.getCell(row, col) == Maze.PATH) {
            player.setRow(row);
            player.setCol(col);
            moveCount++;
        }

        if (player.getRow() == rows - 2 && player.getCol() == cols - 2) {
            gameWon = true;
            if (gameTimer != null) gameTimer.stop();
        }

        repaint();
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}