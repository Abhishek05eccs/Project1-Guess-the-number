public class NumberGuessingLogic {
    private int targetNumber;
    private int attempts;
    private int maxAttempts = 5;
    private int lastGuess; // Store the last guess for hint generation
    private int enchantmentPoints; // Track player's enchantment points
    private boolean insightActive; // Insight enchantment status
    private boolean timeWarpActive; // Time Warp enchantment status
    private boolean precisionActive; // Precision enchantment status (for next guess)
    private boolean clarityUsed; // Clarity enchantment used in this game
    private boolean secondChanceUsed; // Second Chance enchantment used in this game

    public NumberGuessingLogic() {
        resetGame();
    }

    public void resetGame() {
        targetNumber = (int) (Math.random() * 51); // 0 to 50
        attempts = 0;
        lastGuess = -1;
        insightActive = false;
        timeWarpActive = false;
        precisionActive = false;
        clarityUsed = false;
        secondChanceUsed = false;
        maxAttempts = 5; // Reset max attempts in case Second Chance was used
    }

    public int getAttempts() {
        return attempts;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public int getTargetNumber() {
        return targetNumber;
    }

    public int getEnchantmentPoints() {
        return enchantmentPoints;
    }

    public void addEnchantmentPoints(int points) {
        enchantmentPoints += points;
    }

    public boolean activateEnchantment(String enchantment) {
        switch (enchantment) {
            case "Insight":
                if (enchantmentPoints >= 5 && !insightActive) {
                    enchantmentPoints -= 5;
                    insightActive = true;
                    return true;
                }
                return false;
            case "Clarity":
                if (enchantmentPoints >= 3 && !clarityUsed) {
                    enchantmentPoints -= 3;
                    clarityUsed = true;
                    return true;
                }
                return false;
            case "Time Warp":
                if (enchantmentPoints >= 8 && !timeWarpActive) {
                    enchantmentPoints -= 8;
                    timeWarpActive = true;
                    return true;
                }
                return false;
            case "Precision":
                if (enchantmentPoints >= 4 && !precisionActive) {
                    enchantmentPoints -= 4;
                    precisionActive = true;
                    return true;
                }
                return false;
            case "Second Chance":
                if (enchantmentPoints >= 10 && !secondChanceUsed) {
                    enchantmentPoints -= 10;
                    secondChanceUsed = true;
                    maxAttempts++;
                    return true;
                }
                return false;
            default:
                return false;
        }
    }

    public String checkGuess(int guess) {
        attempts++;
        lastGuess = guess;
        if (guess == targetNumber) {
            addEnchantmentPoints(5); // Award 5 points for winning
            return "correct";
        } else if (attempts >= maxAttempts) {
            return "Game over";
        } else if (guess < targetNumber) {
            return "Too low!";
        } else {
            return "Too high!";
        }
    }

    public String getHint() {
        // Provide a basic hint before the first attempt
        if (attempts == 0) {
            String hint = targetNumber % 2 == 0 ? "Starting Hint: The number is even." : "Starting Hint: The number is odd.";
            if (insightActive) {
                hint += " Insight: The number is " + (targetNumber < 25 ? "between 0 and 24." : "between 25 and 50.");
            }
            return hint;
        }

        // Handle Clarity enchantment (triggered, overrides other hints if active)
        if (clarityUsed && attempts == 1) {
            return "Clarity: The number is " + (targetNumber % 2 == 0 ? "even." : "odd.");
        }

        // Calculate difference between guess and target
        int difference = Math.abs(targetNumber - lastGuess);

        // Early attempts (1-2): General range or property-based hints
        if (attempts <= 2) {
            if (precisionActive) {
                precisionActive = false; // Consume Precision
                return difference <= 5 ? "Precision: You're within 5 of the number!" : "Precision: You're within 10 of the number!";
            } else if (difference <= 5) {
                return "Hint: You're very close! Within 5 of the number.";
            } else if (difference <= 10) {
                return "Hint: You're close! Within 10 of the number.";
            } else if (insightActive) {
                return "Insight: The number is " + (targetNumber < 25 ? "between 0 and 24." : "between 25 and 50.");
            } else if (targetNumber < 25) {
                return "Hint: The number is in the lower half (0-24).";
            } else {
                return "Hint: The number is in the upper half (25-50).";
            }
        }
        // Mid attempts (3-4): More specific hints
        else if (attempts <= 4) {
            if (precisionActive) {
                precisionActive = false; // Consume Precision
                return difference <= 3 ? "Precision: You're within 3 of the number!" : "Precision: You're within 5 of the number!";
            } else if (difference <= 3) {
                return "Hint: You're extremely close! Within 3 of the number.";
            } else if (targetNumber % 5 == 0) {
                return "Hint: The number is a multiple of 5.";
            } else if (targetNumber % 10 == 0) {
                return "Hint: The number is a multiple of 10.";
            } else if (lastGuess < targetNumber) {
                return "Hint: Try a number higher than " + lastGuess + ".";
            } else {
                return "Hint: Try a number lower than " + lastGuess + ".";
            }
        }
        // Final attempt (5): Very specific hint
        else {
            if (precisionActive) {
                precisionActive = false; // Consume Precision
                return difference <= 2 ? "Precision: You're within 2 of the number!" : "Precision: You're within 5 of the number!";
            } else if (difference <= 2) {
                return "Final Hint: You're within 2 of the number!";
            } else if (targetNumber % 3 == 0) {
                return "Final Hint: The number is divisible by 3.";
            } else if (targetNumber >= 40) {
                return "Final Hint: The number is 40 or higher.";
            } else if (targetNumber >= 30) {
                return "Final Hint: The number is between 30 and 39.";
            } else if (targetNumber >= 20) {
                return "Final Hint: The number is between 20 and 29.";
            } else if (targetNumber >= 10) {
                return "Final Hint: The number is between 10 and 19.";
            } else {
                return "Final Hint: The number is a single-digit number.";
            }
        }
    }

    public boolean isTimeWarpActive() {
        return timeWarpActive;
    }
}