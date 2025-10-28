import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FarmingSimulator extends JFrame {
    private MainMenu mainMenu;
    private BackgroundPanel backgroundPanel;

    public FarmingSimulator() {
        setTitle("Farming Simulator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 1000);
        setLocationRelativeTo(null);
        setResizable(false);

        // Initialize the menu window
        mainMenu = new MainMenu();
        // Display the main menu
        mainMenu.setVisible(true);

        // Create a custom panel for the background
        backgroundPanel = new BackgroundPanel();
        getContentPane().add(backgroundPanel);

        // Create buttons
        JButton plantButton = new JButton("Plant");
        JButton harvestButton = new JButton("Harvest");
        JButton rainButton = new JButton("Rain");
        JButton exitButton = new JButton("Exit");
        JButton resetButton = new JButton("Reset");

        // Add action listeners to buttons
        plantButton.addActionListener(e -> {
            backgroundPanel.plantCrop();
            backgroundPanel.repaint();
        });

        harvestButton.addActionListener(e -> {
            backgroundPanel.harvestCrop();
            backgroundPanel.repaint();
        });

        rainButton.addActionListener(e -> {
            backgroundPanel.triggerRain();
            backgroundPanel.repaint();
        });

        resetButton.addActionListener(e -> {
            backgroundPanel.resetGame();
            backgroundPanel.repaint();
        });

        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Dispose of the current FarmingSimulator window
                dispose();
                // Show the MainMenu window again
                mainMenu.setVisible(true);
            }
        });

        // Panels for Buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(plantButton);
        buttonPanel.add(harvestButton);
        buttonPanel.add(rainButton);
        buttonPanel.add(exitButton);
        buttonPanel.add(resetButton);
        add(buttonPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(false);
        backgroundPanel.initializeCropPlots();
    }

    // Custom JPanel for the background
    private class BackgroundPanel extends JPanel {
        private List<Wheat> wheatList;
        private List<CropPlot> cropPlots;
        private int score;
        private int cloud1X = 100;
        private int cloud2X = 300;
        private int cloud3X = 250;
        private int cloud4X = 500;
        private int cloudX;
        private int cloudY;
        private boolean raining;
        private int highScore;
        private int rainEffect =1;
        private boolean rainButtonEnabled = true;
        private int rainCooldown = 30; // in seconds
        private Timer rainCooldownTimer;
        private int gameTimerSeconds = 120;  // Initial game time in seconds
        private Timer gameTimer;
        private boolean gameRunning = false;
        public BackgroundPanel() {
            wheatList = new ArrayList<>();
            cropPlots = new ArrayList<>();
            initializeCropPlots();
            
            rainCooldownTimer = new Timer(1000, e -> {
                if (rainCooldown > 0) {
                    rainCooldown--;
                    repaint();
                } else {
                    rainButtonEnabled = true;
                    rainCooldownTimer.stop();
                    repaint();
                }
            });
            // Create a timer for the game countdown
            gameTimer = new Timer(1000, e -> {
    gameTimerSeconds--;
    repaint();

    if (gameTimerSeconds <= 0) {
        gameRunning = false;
        gameTimer.stop();
        showGameOverDialog();
    }
});

            // Create a timer to simulate wheat growth
            final Timer growthTimer = new Timer(500, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    for (Wheat wheat : wheatList) {
                        wheat.grow();
                    }
                    repaint();
                }
            });
            Timer cloudTimer = new Timer(30, e -> {
                animateClouds();
                repaint();
            });
        
            //Start cloud Timer
            cloudTimer.start();
            // Plant timers
            growthTimer.start();
            gameTimer.start();
          
            gameRunning = true;
            // Add mouse listener for raining
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (cloudX != -1 && cloudY != -1 && isMouseOnCloud(e.getX(), e.getY())) {
                        raining = true;
                        repaint();
                    }
                }
            });
        }

        public void resetGame() {
            wheatList.clear();
            for (CropPlot cropPlot : cropPlots) {
                cropPlot.setOccupied(false);
            }
            
            // Check if the current score is greater than the high score
            if (score > highScore) {
                highScore = score;
            }
            gameTimer.stop();
            gameTimerSeconds = 120;  // Reset game time
            gameRunning = false;
            score = 0;
            
            repaint();
        }
        public int getHighScore() {
            return highScore;
        }
        public void triggerRain() {
            if (rainButtonEnabled) {
                cloudX = 50;
                cloudY = 100;
                raining = true;
                rainEffect = 2;
        
                // Start the rain cooldown timer
                rainCooldownTimer.start();
                rainButtonEnabled = false;
        
                // Create a timer to stop raining after a short duration (e.g., 2 seconds)
                Timer stopRainingTimer = new Timer(1000, stopEvent -> {
                    raining = false;
                    rainEffect = 1;
        
                    // Grow all crops to their maximum height when it rains
                    for (Wheat wheat : wheatList) {
                        wheat.growToMaxHeight();
                    }
        
                    repaint();
                });
                stopRainingTimer.setRepeats(false); // Run only once
                stopRainingTimer.start();
            }
        }
        

        private boolean isMouseOnCloud(int mouseX, int mouseY) {
            int cloudWidth = 60;
            int cloudHeight = 30;
            return mouseX >= cloudX && mouseX <= cloudX + cloudWidth &&
                    mouseY >= cloudY && mouseY <= cloudY + cloudHeight;
        }
        private void showGameOverDialog() {
            int result = JOptionPane.showConfirmDialog(this, "Game Over!\nYour Score: " + score,
                    "Game Over", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
        
            if (result == JOptionPane.OK_OPTION) {
                resetGame();
            }
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // Draw the sky
            g.setColor(new Color(135, 206, 250)); // Light blue color
            g.fillRect(0, 0, getWidth(), getHeight());

            // Draw the grass
            g.setColor(new Color(34, 139, 34)); // Dark green color
            g.fillRect(0, getHeight() / 2, getWidth(), getHeight() / 2);

            // Draw some clouds with animation
            animateClouds();
            drawCloud(g, cloud1X, 100, 50, 20);
            drawCloud(g, cloud2X, 150, 40, 15);
            drawCloud(g, cloud3X, 80, 60, 25);
            drawCloud(g, cloud4X, 10, 60, 25);
            drawRainCloud(g, cloudX, cloudY, 60, 30);
            
            // Draw the crop plots
            g.setColor(new Color(139, 69, 19)); // Brown color for crop plots
            for (CropPlot cropPlot : cropPlots) {
                g.fillRect(cropPlot.getX(), cropPlot.getY(), cropPlot.getWidth(), cropPlot.getHeight());
            }

            // Draw the wheat
            for (Wheat wheat : wheatList) {
                wheat.draw(g);
            }
             // Draw the game timer
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.drawString("Time: " + gameTimerSeconds + "s", getWidth() / 2 - 50, 20);
            // Draw the score
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.drawString("Score: " + score, 10, 20);
            g.drawString("High Score: " + getHighScore(), 10, 40);
            // Draw rain effect for when it's raining
            if (raining) {
                drawRain(g);
            }
            //Barn
            g.setColor(new Color(180,44,6));
            g.fillRect(900,400,300,400);
            g.setColor(Color.WHITE);
            g.fillRect(975, 600, 150, 200);
            g.setColor(Color.BLACK);
            //Roof
            g.fillPolygon(new int[]{800,1300,1050}, new int[] {450,450,300}, 3);
            //Door
            g.fillPolygon(new int[]{1000,1100,1050}, new int[] {790,790,710}, 3);
            g.fillPolygon(new int[]{1000,1100,1050}, new int[] {610,610,690}, 3);
            g.fillPolygon(new int[]{990,990,1040}, new int[] {625,775,700}, 3);
            g.fillPolygon(new int[]{1110,1110,1060}, new int[] {625,775,700}, 3);

            // Draw rain cooldown timer
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.drawString("Rain Cooldown: " + rainCooldown + "s", getWidth() - 150, 20);
        
        
        }



        private void drawCloud(Graphics g, int x, int y, int width, int height) {
            g.setColor(Color.white);
            g.fillOval(x, y, width, height);
            g.fillOval(x + width / 2, y - height / 2, width, height);
            g.fillOval(x + width, y, width, height);
            g.fillOval(x + width / 2, y + height / 2, width, height);
        }
        private void drawRainCloud(Graphics g, int x, int y, int width, int height) {
            g.setColor(new Color(105,105,105));
            g.fillOval(x, y, width, height);
            g.fillOval(x + width / 2, y - height / 2, width, height);
            g.fillOval(x + width, y, width, height);
            g.fillOval(x + width / 2, y + height / 2, width, height);
        }

        private void drawRain(Graphics g) {
            g.setColor(Color.BLUE);
            Random rand = new Random();
            for (int i = 0; i < 100; i++) {
                int x = rand.nextInt(getWidth());
                int y = rand.nextInt(getHeight() / 2);
                g.drawLine(x, y, x, y + 9);
            }
        }

        private void animateClouds() {
            // Update cloud positions
            cloud1X += 2;
            cloud2X += 1;
            cloud3X += 2;
            cloud4X += 1;
            // rainy cloud position
            cloudX += 8;
        int cloudWidth = 60;
        if (cloud1X > getWidth()+cloudWidth){
            cloud1X=-cloudWidth;
        }
        if (cloud2X > getWidth()+cloudWidth){
            cloud2X=-cloudWidth;
        }
        if (cloud3X > getWidth()+cloudWidth){
            cloud3X=-cloudWidth;
        }
        if (cloud4X > getWidth()+cloudWidth){
            cloud4X=-cloudWidth;
        }
        
        }

        public void plantCrop() {
             gameTimer.start();
          
            gameRunning = true;
            // Find an available crop plot
            for (CropPlot cropPlot : cropPlots) {
                if (!cropPlot.isOccupied()) {
                    cropPlot.setOccupied(true);
                    wheatList.add(new Wheat(cropPlot.getX() + cropPlot.getWidth() / 2,
                            cropPlot.getY() + cropPlot.getHeight() / 2));
                    break;
                }
            }
        }

        public void harvestCrop() {
            // Create a copy of the wheat list for iteration
            List<Wheat> fullyGrownWheat = new ArrayList<>();

            for (Wheat wheat : wheatList) {
                if (wheat.isFullyGrown()) {
                    fullyGrownWheat.add(wheat);
                    markCropPlotAsUnoccupied(wheat.getX(), wheat.getY());
                }
            }
            // Remove fully grown wheat from the list
            wheatList.removeAll(fullyGrownWheat);

            // Reset the score
            score += 10 * fullyGrownWheat.size(); // Adjust the points per crop as needed

            repaint();
        }

        private void markCropPlotAsUnoccupied(int x, int y) {
            for (CropPlot cropPlot : cropPlots) {
                if (cropPlot.containsPoint(x, y)) {
                    cropPlot.setOccupied(false);
                    break;
                }
            }
        }

        private void initializeCropPlots() {
            // Add brown rectangles to represent crop plots in the grass area
            int plotWidth = 80;
            int plotHeight = 80;
            int padding = 10; // Adjust this value for the desired spacing between plots
        
            int numRows = 5;
            int numCols = 8;
        
            cropPlots.clear(); // Clear existing crop plots
        
        int startY = getHeight() / 2 + 465; // Adjust this value to set the starting position
            for (int row = 0; row < numRows; row++) {
                for (int col = 0; col < numCols; col++) {
                    int x = col * (plotWidth + padding) + padding;
                    int y = startY + row * (plotHeight + padding) + padding;
                    cropPlots.add(new CropPlot(x, y, plotWidth, plotHeight));
                }
            }
        }
      
    
    } 

    private static class Wheat {
        private int x;
        private int y;
        private int height;
        private int maxGrowth = 50; // Adjust the maximum growth height as needed
        private Color stemColor;

        public Wheat(int x, int y) {
            this.x = x;
            this.y = y;
            this.height = 1; // Start with a small height
            this.stemColor = Color.GREEN;
        }

        public void draw(Graphics g) {
            // Draw the wheat stem

            // Set color to brown for the stem
            g.setColor(new Color(255, 215, 0)); // Brown color

            // This will set the plant color to green
            g.setColor(stemColor);

            // Draw the long stem
            int stemWidth = 5;
            int stemHeight = height;
            int stemX = x - stemWidth / 2; // Centered stem
            int stemY = y - stemHeight;

            g.fillRect(stemX, stemY, stemWidth, stemHeight);

            if (isFullyGrown()) {
                g.setColor(new Color(255, 215, 0)); // Gold color
            } else {
                g.setColor(stemColor);
            }

            // Draw the first rectangle going straight up
            int rect1Width = 5;
            int rect1Height = 20;
            int rect1X = x - rect1Width / 2; // Centered rectangle
            int rect1Y = stemY - rect1Height;

            g.fillRect(rect1X, rect1Y, rect1Width, rect1Height);

            // Draw the second rectangle at an angle
            int rect2Width = 5;
            int rect2Height = 20;
            int rect2X = x - rect2Width / 2; // Centered rectangle
            int rect2Y = stemY - rect2Height;

            Graphics2D g2d = (Graphics2D) g;
            g2d.rotate(Math.toRadians(45), x, stemY);
            g.fillRect(rect2X, rect2Y, rect2Width, rect2Height);
            g2d.rotate(-Math.toRadians(45), x, stemY);

            // Draw the third rectangle at the opposite angle
            int rect3Width = 5;
            int rect3Height = 20;
            int rect3X = x - rect3Width / 2; // Centered rectangle
            int rect3Y = stemY - rect3Height;

            g2d.rotate(Math.toRadians(-45), x, stemY);
            g.fillRect(rect3X, rect3Y, rect3Width, rect3Height);
            g2d.rotate(-Math.toRadians(-45), x, stemY);
        }

        public void grow() {
            if (height < maxGrowth) {
                height += 5; // Adjust the growth speed with the rain effect
                if (isFullyGrown()) {
                    stemColor = new Color(255, 215, 0); // Gold color
                }
            }
        }
        
        public void growToMaxHeight() {
            height = maxGrowth;
            stemColor = new Color(255, 215, 0); // Set color to gold when fully grown
        
        }
        
        public boolean isFullyGrown() {
            return height >= maxGrowth;
        }

        public int getY() {
            return y;
        }

        public int getX() {
            return x;
        }
    }

    // CropPlot class
    private static class CropPlot {
        private int x;
        private int y;
        private int width;
        private int height;
        private boolean occupied;

        public CropPlot(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.occupied = false;
        }

        public boolean containsPoint(int x2, int y2) {
            return x2 >= x && x2 <= x + width && y2 >= y && y2 <= y + height;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public boolean isOccupied() {
            return occupied;
        }

        public void setOccupied(boolean occupied) {
            this.occupied = occupied;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FarmingSimulator::new);
    }
}
