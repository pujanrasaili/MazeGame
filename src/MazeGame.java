import javax.swing.*;

public class MazeGame {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Maze Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        // Show menu first
        showMenu(frame);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void showMenu(JFrame frame) {
        frame.getContentPane().removeAll();

        MenuPanel menu = new MenuPanel(mode -> {
            // When a mode card is clicked, start the game
            startGame(frame, mode);
        });

        frame.add(menu);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.repaint();
        frame.revalidate();
    }

    public static void startGame(JFrame frame, String mode) {
        frame.getContentPane().removeAll();

        MazePanel panel = new MazePanel(mode, () -> {
            // When player clicks "Back to Menu"
            showMenu(frame);
        });

        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.revalidate();
        frame.repaint();
        panel.requestFocusInWindow();
    }
}