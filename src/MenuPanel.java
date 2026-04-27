import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class MenuPanel extends JPanel {

    public interface ModeSelectListener {
        void onModeSelected(String mode);
    }

    private ModeSelectListener listener;

    private final Color BG_TOP    = new Color(10, 10, 30);
    private final Color BG_BOTTOM = new Color(20, 20, 60);

    private String[] modes     = {"NORMAL", "TIMED", "FOG OF WAR", "CHALLENGE"};
    private String[] subtitles = {
            "Classic maze, no pressure",
            "Beat the clock!",
            "Limited visibility",
            "Timed + Fog of War"
    };
    private Color[] cardColors = {
            new Color(50, 150, 255),
            new Color(255, 150, 50),
            new Color(150, 80, 255),
            new Color(255, 60, 100)
    };
    private String[] icons = {"🧩", "⏱️", "🌫️", "🏆"};

    private int hoveredCard = -1;

    public MenuPanel(ModeSelectListener listener) {
        this.listener = listener;
        setPreferredSize(new Dimension(600, 500));
        setBackground(BG_TOP);

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int prev = hoveredCard;
                hoveredCard = getCardAt(e.getX(), e.getY());
                if (hoveredCard != prev) repaint();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int card = getCardAt(e.getX(), e.getY());
                if (card >= 0) listener.onModeSelected(modes[card]);
            }
        });
    }

    private int getCardAt(int mx, int my) {
        int[][] positions = getCardPositions();
        int w = 220, h = 140;
        for (int i = 0; i < 4; i++) {
            int x = positions[i][0], y = positions[i][1];
            if (mx >= x && mx <= x + w && my >= y && my <= y + h) return i;
        }
        return -1;
    }

    private int[][] getCardPositions() {
        int startX = 60, startY = 160;
        int gapX = 260, gapY = 165;
        return new int[][]{
                {startX,        startY},
                {startX+gapX,   startY},
                {startX,        startY+gapY},
                {startX+gapX,   startY+gapY}
        };
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background gradient
        GradientPaint bgGrad = new GradientPaint(0, 0, BG_TOP, 0, getHeight(), BG_BOTTOM);
        g2.setPaint(bgGrad);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Title
        g2.setFont(new Font("Arial", Font.BOLD, 52));
        String title = "MAZE GAME";
        FontMetrics fm = g2.getFontMetrics();
        int tx = (getWidth() - fm.stringWidth(title)) / 2;
        // Shadow
        g2.setColor(new Color(0, 0, 0, 120));
        g2.drawString(title, tx + 3, 83);
        // Gradient title
        GradientPaint titleGrad = new GradientPaint(tx, 40, new Color(0, 220, 150), tx + fm.stringWidth(title), 80, new Color(50, 150, 255));
        g2.setPaint(titleGrad);
        g2.drawString(title, tx, 80);

        // Subtitle
        g2.setColor(new Color(180, 180, 220));
        g2.setFont(new Font("Arial", Font.PLAIN, 16));
        String sub = "Choose your game mode";
        fm = g2.getFontMetrics();
        g2.drawString(sub, (getWidth() - fm.stringWidth(sub)) / 2, 115);

        // Mode cards
        int[][] positions = getCardPositions();
        int w = 220, h = 140;
        for (int i = 0; i < 4; i++) {
            int x = positions[i][0];
            int y = positions[i][1];
            boolean hovered = (hoveredCard == i);

            // Card shadow
            g2.setColor(new Color(0, 0, 0, 80));
            g2.fill(new RoundRectangle2D.Float(x + 4, y + 4, w, h, 20, 20));

            // Card background
            Color base = cardColors[i];
            Color dark = base.darker().darker();
            GradientPaint cardGrad = new GradientPaint(x, y, hovered ? base.brighter() : base, x, y + h, dark);
            g2.setPaint(cardGrad);
            g2.fill(new RoundRectangle2D.Float(x, y, w, h, 20, 20));

            // Border glow on hover
            if (hovered) {
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2.5f));
                g2.draw(new RoundRectangle2D.Float(x, y, w, h, 20, 20));
            }

            // Icon
            g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
            g2.setColor(Color.WHITE);
            fm = g2.getFontMetrics();
            g2.drawString(icons[i], x + (w - fm.stringWidth(icons[i])) / 2, y + 48);

            // Mode name
            g2.setFont(new Font("Arial", Font.BOLD, 18));
            fm = g2.getFontMetrics();
            g2.setColor(Color.WHITE);
            g2.drawString(modes[i], x + (w - fm.stringWidth(modes[i])) / 2, y + 82);

            // Subtitle
            g2.setFont(new Font("Arial", Font.PLAIN, 13));
            fm = g2.getFontMetrics();
            g2.setColor(new Color(255, 255, 255, 200));
            g2.drawString(subtitles[i], x + (w - fm.stringWidth(subtitles[i])) / 2, y + 108);
        }

        // Quit button
        int qx = (getWidth() - 120) / 2;
        int qy = 460;
        boolean qHovered = hoveredCard == -2;
        g2.setColor(new Color(80, 80, 120));
        g2.fill(new RoundRectangle2D.Float(qx, qy, 120, 35, 10, 10));
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 15));
        fm = g2.getFontMetrics();
        g2.drawString("QUIT", qx + (120 - fm.stringWidth("QUIT")) / 2, qy + 23);
    }
}