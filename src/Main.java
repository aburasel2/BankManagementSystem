import view.LoginFrame;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Swing thread-safe করে run করা
        SwingUtilities.invokeLater(() -> {
            // Look and Feel সেট করা (সুন্দর দেখাবে)
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new LoginFrame();
        });
    }
}