import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainMenu extends JFrame {
    public FarmingSimulator farmingSimulator;

    public MainMenu() {
        setTitle("Main Menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 900);
        setLocationRelativeTo(null);
        // Buttons
        JButton startButton = new JButton("Farming Simulator 🌾");
        JButton exitButton = new JButton("Click Here to Exit ❌");


        Font buttonFont = new Font("SansSerif", Font.BOLD, 50);
        startButton.setFont(buttonFont);
        exitButton.setFont(buttonFont);

        // Set background colors
        startButton.setBackground(new Color(255, 213, 110));
        exitButton.setBackground(new Color(255, 255, 210));

        // Set foreground (text) color to make it visible on the colored background
        startButton.setForeground(Color.WHITE);
        exitButton.setForeground(Color.ORANGE);

        startButton.setBorderPainted(false);
        exitButton.setBorderPainted(false);
        startButton.setOpaque(true);
        exitButton.setOpaque(true);
        // Remove focus border
        startButton.setFocusPainted(true);
        exitButton.setFocusPainted(true);


        // Listeners
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                farmingSimulator = new FarmingSimulator();
                farmingSimulator.setVisible(true);
            }
        });
        exitButton.addActionListener(e -> {
            // Close the application
            System.exit(0);
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 1));
        buttonPanel.add(startButton);
        buttonPanel.add(exitButton);

        add(buttonPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainMenu());
    }
}