import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public class NumberGuessingGui extends JFrame {
    private final NumberGuessingLogic game;
    private final JTextField guessField;
    private final JButton guessButton;
    private final JButton playAgainButton;
    private final JLabel feedbackLabel;
    private final JLabel hintLabel;
    private final JLabel timerLabel;
    private final JLabel pointsLabel;
    private final JButton insightButton;
    private final JButton clarityButton;
    private final JButton timeWarpButton;
    private final JButton precisionButton;
    private final JButton secondChanceButton;
    private Timer countdownTimer;
    private int timeLeft;

    // Custom panel with themed background
    private static class TexturedPanel extends JPanel {
        private final boolean showGuessing;
        private BufferedImage texture;
        private float twinklePhase;
        private Symbol[] symbols; // Array to store moving symbols

        private static class Symbol {
            float x, y; // Position
            float vx, vy; // Velocity
            String value; // Symbol (e.g., "?", "0", "★")
            float phaseOffset; // For twinkling

            Symbol(String value, float x, float y, float vx, float vy, float phaseOffset) {
                this.value = value;
                this.x = x;
                this.y = y;
                this.vx = vx;
                this.vy = vy;
                this.phaseOffset = phaseOffset;
            }
        }

        public TexturedPanel(boolean showGuessing) {
            this.showGuessing = showGuessing;
            setOpaque(true);
            generateTexture();
            // Initialize symbols for animation
            if (showGuessing) {
                Random rand = new Random();
                String[] symbolValues = {"?", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "★", "✦"};
                symbols = new Symbol[35]; // Reduced to 20 symbols
                for (int i = 0; i < symbols.length; i++) {
                    float x = rand.nextInt(500);
                    float y = rand.nextInt(450);
                    float angle = rand.nextFloat() * 2 * (float) Math.PI;
                    float speed = 0.5f + rand.nextFloat() * 1.0f; // Speed between 0.5 and 1.5 pixels per frame
                    symbols[i] = new Symbol(
                        symbolValues[rand.nextInt(symbolValues.length)],
                        x, y,
                        (float) Math.cos(angle) * speed,
                        (float) Math.sin(angle) * speed,
                        rand.nextFloat() * 2 * (float) Math.PI
                    );
                }
            }
            // Timer for animation (100ms for slower effect)
            new Timer(50, e -> {
                twinklePhase = (twinklePhase + 0.05f) % (2 * (float) Math.PI);
                if (showGuessing) {
                    updateSymbols();
                }
                repaint();
            }).start();
        }

        private void generateTexture() {
            int w = 500;
            int h = 450;
            texture = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = texture.createGraphics();
            Random rand = new Random();

            // Teal-to-green gradient background
            GradientPaint cosmicGradient = new GradientPaint(
                0, 0, new Color(10, 50, 60), // Deep teal
                0, h, new Color(20, 80, 40)  // Dark green
            );
            g2d.setPaint(cosmicGradient);
            g2d.fillRect(0, 0, w, h);

            // Add starry specks
            for (int i = 0; i < 100; i++) {
                int x = rand.nextInt(w);
                int y = rand.nextInt(h);
                int size = rand.nextInt(3) + 1;
                g2d.setColor(new Color(255, 255, 255, 100 + rand.nextInt(100)));
                g2d.fillOval(x, y, size, size);
            }

            g2d.dispose();
        }

        private void updateSymbols() {
            int w = getWidth();
            int h = getHeight();
            for (Symbol symbol : symbols) {
                // Update position
                symbol.x += symbol.vx;
                symbol.y += symbol.vy;
                // Bounce off edges
                if (symbol.x < 0 || symbol.x > w) symbol.vx = -symbol.vx;
                if (symbol.y < 0 || symbol.y > h) symbol.vy = -symbol.vy;
                // Keep within bounds
                symbol.x = Math.max(0, Math.min(w, symbol.x));
                symbol.y = Math.max(0, Math.min(h, symbol.y));
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();

            // Draw cached texture, scaled to panel size
            g2d.drawImage(texture, 0, 0, w, h, null);

            // Draw dynamic symbols if enabled
            if (showGuessing && symbols != null) {
                g2d.setFont(new Font("Serif", Font.BOLD, 20));
                for (Symbol symbol : symbols) {
                    int alpha = (int) (100 + 50 * Math.sin(twinklePhase + symbol.phaseOffset));
                    g2d.setColor(new Color(180, 255, 200, Math.min(alpha, 255))); // Light teal
                    g2d.drawString(symbol.value, symbol.x, symbol.y);
                }
            }

            // Add glowing overlay
            GradientPaint glow = new GradientPaint(
                0, 0, new Color(255, 255, 255, 20),
                0, h, new Color(100, 255, 150, 50) // Light teal-green
            );
            g2d.setPaint(glow);
            g2d.fillRect(0, 0, w, h);
        }
    }

    public NumberGuessingGui() {
        setTitle("🎯 Number Guessing Game");
        setSize(500, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        game = new NumberGuessingLogic();

        TexturedPanel mainPanel = new TexturedPanel(false);
        mainPanel.setLayout(new BorderLayout());

        TexturedPanel gamePanel = new TexturedPanel(true);
        gamePanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel titleLabel = new JLabel("🔍 Guess a number between 0 and 50 (5 attempts)");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setOpaque(false);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gamePanel.add(titleLabel, gbc);

        timerLabel = new JLabel("⏳ Time Left: 30s");
        timerLabel.setFont(new Font("Arial", Font.BOLD, 14));
        timerLabel.setForeground(Color.ORANGE);
        timerLabel.setOpaque(false);
        gbc.gridy = 1;
        gamePanel.add(timerLabel, gbc);

        guessField = new JTextField(10);
        guessField.setFont(new Font("Arial", Font.PLAIN, 14));
        guessField.setOpaque(true);
        guessField.setBackground(new Color(255, 255, 255, 200));
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gamePanel.add(guessField, gbc);

        guessButton = new JButton("🔍 Guess");
        guessButton.setFont(new Font("Arial", Font.PLAIN, 14));
        guessButton.setBackground(new Color(50, 50, 50));
        guessButton.setForeground(Color.WHITE);
        gbc.gridx = 1;
        gamePanel.add(guessButton, gbc);

        feedbackLabel = new JLabel(" ");
        feedbackLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        feedbackLabel.setForeground(Color.YELLOW);
        feedbackLabel.setOpaque(false);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gamePanel.add(feedbackLabel, gbc);

        hintLabel = new JLabel(game.getHint());
        hintLabel.setFont(new Font("Arial", Font.ITALIC, 13));
        hintLabel.setForeground(Color.CYAN);
        hintLabel.setOpaque(false);
        gbc.gridy = 4;
        gamePanel.add(hintLabel, gbc);

        playAgainButton = new JButton("🔁 Play Again");
        playAgainButton.setFont(new Font("Arial", Font.BOLD, 14));
        playAgainButton.setBackground(new Color(50, 50, 50));
        playAgainButton.setForeground(Color.WHITE);
        playAgainButton.setVisible(false);
        gbc.gridy = 5;
        gamePanel.add(playAgainButton, gbc);

        TexturedPanel enchantPanel = new TexturedPanel(false);
        enchantPanel.setLayout(new GridLayout(2, 3, 10, 10));
        enchantPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.WHITE), "Enchantments", 0, 0,
            new Font("Arial", Font.BOLD, 14), Color.WHITE));

        pointsLabel = new JLabel("Points: " + game.getEnchantmentPoints());
        pointsLabel.setFont(new Font("Arial", Font.BOLD, 14));
        pointsLabel.setForeground(Color.GREEN);
        pointsLabel.setOpaque(false);
        enchantPanel.add(pointsLabel);

        insightButton = new JButton("Insight (5)");
        insightButton.setFont(new Font("Arial", Font.PLAIN, 12));
        insightButton.setBackground(new Color(50, 50, 50));
        insightButton.setForeground(Color.WHITE);
        enchantPanel.add(insightButton);

        clarityButton = new JButton("Clarity (3)");
        clarityButton.setFont(new Font("Arial", Font.PLAIN, 12));
        clarityButton.setBackground(new Color(50, 50, 50));
        clarityButton.setForeground(Color.WHITE);
        enchantPanel.add(clarityButton);

        timeWarpButton = new JButton("Time Warp (8)");
        timeWarpButton.setFont(new Font("Arial", Font.PLAIN, 12));
        timeWarpButton.setBackground(new Color(50, 50, 50));
        timeWarpButton.setForeground(Color.WHITE);
        enchantPanel.add(timeWarpButton);

        precisionButton = new JButton("Precision (4)");
        precisionButton.setFont(new Font("Arial", Font.PLAIN, 12));
        precisionButton.setBackground(new Color(50, 50, 50));
        precisionButton.setForeground(Color.WHITE);
        enchantPanel.add(precisionButton);

        secondChanceButton = new JButton("Second Chance (10)");
        secondChanceButton.setFont(new Font("Arial", Font.PLAIN, 12));
        secondChanceButton.setBackground(new Color(50, 50, 50));
        secondChanceButton.setForeground(Color.WHITE);
        enchantPanel.add(secondChanceButton);

        mainPanel.add(gamePanel, BorderLayout.CENTER);
        mainPanel.add(enchantPanel, BorderLayout.SOUTH);
        add(mainPanel);

        guessButton.addActionListener(e -> processGuess());
        guessField.addActionListener(e -> processGuess());
        playAgainButton.addActionListener(e -> resetGame());
        insightButton.addActionListener(e -> applyEnchantment("Insight"));
        clarityButton.addActionListener(e -> applyEnchantment("Clarity"));
        timeWarpButton.addActionListener(e -> applyEnchantment("Time Warp"));
        precisionButton.addActionListener(e -> applyEnchantment("Precision"));
        secondChanceButton.addActionListener(e -> applyEnchantment("Second Chance"));

        updateEnchantmentButtons();
        showRules();
        startTimer();
    }

    private void showRules() {
        JDialog rulesDialog = new JDialog(this, "Rules and Regulations", true);
        rulesDialog.setSize(600, 500);
        rulesDialog.setLocationRelativeTo(this);
        rulesDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        TexturedPanel rulesPanel = new TexturedPanel(false);
        rulesPanel.setLayout(new BorderLayout());
        rulesPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextPane rulesText = new JTextPane();
        rulesText.setContentType("text/html");
        rulesText.setEditable(false);
        rulesText.setOpaque(false);
        rulesText.setFont(new Font("Arial", Font.PLAIN, 14));
        String rules = "<html>" +
                       "<h1 style='color: white; text-align: center;'>Welcome to the Number Guessing Game!</h1>" +
                       "<h2 style='color: orange;'>Objective</h2>" +
                       "<p style='color: white;'>Guess a number between 0 and 50 within 5 attempts or 30 seconds.</p>" +
                       "<h2 style='color: orange;'>Rules</h2>" +
                       "<ul style='color: white;'>" +
                       "<li>Enter a number in the text field and click 'Guess' or press Enter.</li>" +
                       "<li>You’ll get feedback ('Too high!', 'Too low!', or 'Correct!') and hints.</li>" +
                       "<li>The game ends if you guess correctly, run out of attempts, or time expires.</li>" +
                       "<li>Click 'Play Again' to start a new game.</li>" +
                       "</ul>" +
                       "<h2 style='color: orange;'>Enchantments</h2>" +
                       "<p style='color: white;'>Use Enchantment Points (earned 5 per win) to activate power-ups:</p>" +
                       "<ul style='color: white;'>" +
                       "<li><b>Insight (5 points)</b>: Adds range hint (0-24 or 25-50) to all hints.</li>" +
                       "<li><b>Clarity (3 points)</b>: Reveals if the number is even/odd on first attempt.</li>" +
                       "<li><b>Time Warp (8 points)</b>: Extends timer to 40 seconds.</li>" +
                       "<li><b>Precision (4 points)</b>: Next hint shows proximity (e.g., within 5).</li>" +
                       "<li><b>Second Chance (10 points)</b>: Grants one extra attempt.</li>" +
                       "</ul>" +
                       "<p style='color: cyan; text-align: center;'>Start with a hint about whether the number is even or odd. Use enchantments wisely!</p>" +
                       "</html>";
        rulesText.setText(rules);
        rulesText.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(rulesText);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);

        JButton okButton = new JButton("Start Game");
        okButton.setFont(new Font("Arial", Font.BOLD, 14));
        okButton.setBackground(new Color(50, 50, 50));
        okButton.setForeground(Color.WHITE);
        okButton.addActionListener(e -> rulesDialog.dispose());

        rulesPanel.add(scrollPane, BorderLayout.CENTER);
        rulesPanel.add(okButton, BorderLayout.SOUTH);
        rulesDialog.add(rulesPanel);
        rulesDialog.setVisible(true);
    }

    private void processGuess() {
        String input = guessField.getText().trim();
        if (input.isEmpty()) {
            feedbackLabel.setText("❗ Please enter a number.");
            return;
        }

        try {
            int guess = Integer.parseInt(input);
            if (guess < 0 || guess > 50) {
                feedbackLabel.setText("❗ Please enter a number between 0 and 50.");
                return;
            }
            String result = game.checkGuess(guess);

            if (result.equals("correct")) {
                feedbackLabel.setText("🎉 Correct! You guessed in " + game.getAttempts() + " attempts. +5 Points!");
                endGame();
            } else if (result.equals("Game over")) {
                feedbackLabel.setText("💀 Game Over! The number was " + game.getTargetNumber());
                endGame();
            } else {
                feedbackLabel.setText(result + " | Attempts left: " + (game.getMaxAttempts() - game.getAttempts()));
                hintLabel.setText(game.getHint());
            }
            pointsLabel.setText("Points: " + game.getEnchantmentPoints());
            updateEnchantmentButtons();
        } catch (NumberFormatException e) {
            feedbackLabel.setText("❗ Invalid input! Enter a valid number.");
        }

        guessField.setText("");
        guessField.requestFocus();
    }

    private void applyEnchantment(String enchantment) {
        if (game.activateEnchantment(enchantment)) {
            feedbackLabel.setText("✅ " + enchantment + " activated!");
            if (enchantment.equals("Insight") || enchantment.equals("Clarity")) {
                hintLabel.setText(game.getHint());
            } else if (enchantment.equals("Time Warp")) {
                timeLeft += 10;
                timerLabel.setText("⏳ Time Left: " + timeLeft + "s");
            } else if (enchantment.equals("Second Chance")) {
                feedbackLabel.setText("✅ Second Chance activated! Attempts left: " + (game.getMaxAttempts() - game.getAttempts()));
            }
            pointsLabel.setText("Points: " + game.getEnchantmentPoints());
            updateEnchantmentButtons();
        } else {
            feedbackLabel.setText("❌ Not enough points or enchantment already used!");
        }
    }

    private void updateEnchantmentButtons() {
        insightButton.setEnabled(game.getEnchantmentPoints() >= 5);
        clarityButton.setEnabled(game.getEnchantmentPoints() >= 3);
        timeWarpButton.setEnabled(game.getEnchantmentPoints() >= 8);
        precisionButton.setEnabled(game.getEnchantmentPoints() >= 4);
        secondChanceButton.setEnabled(game.getEnchantmentPoints() >= 10);
    }

    private void startTimer() {
        timeLeft = game.isTimeWarpActive() ? 40 : 30;
        timerLabel.setText("⏳ Time Left: " + timeLeft + "s");

        countdownTimer = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                timeLeft--;
                timerLabel.setText("⏳ Time Left: " + timeLeft + "s");

                if (timeLeft <= 0) {
                    countdownTimer.stop();
                    feedbackLabel.setText("⏰ Time's up! The number was " + game.getTargetNumber());
                    endGame();
                }
            }
        });

        countdownTimer.start();
    }

    private void endGame() {
        guessButton.setEnabled(false);
        guessField.setEnabled(false);
        playAgainButton.setVisible(true);
        if (countdownTimer != null) {
            countdownTimer.stop();
        }
        updateEnchantmentButtons();
    }

    private void resetGame() {
        game.resetGame();
        feedbackLabel.setText(" ");
        hintLabel.setText(game.getHint());
        guessField.setEnabled(true);
        guessButton.setEnabled(true);
        guessField.setText("");
        guessField.requestFocus();
        playAgainButton.setVisible(false);
        pointsLabel.setText("Points: " + game.getEnchantmentPoints());
        updateEnchantmentButtons();
        startTimer();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new NumberGuessingGui().setVisible(true));
    }
}