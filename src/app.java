import javax.swing.*;

public class app {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Weather().setVisible(true));
    }
}
