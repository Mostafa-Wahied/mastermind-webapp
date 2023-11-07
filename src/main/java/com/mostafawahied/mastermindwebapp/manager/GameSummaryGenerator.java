package com.mostafawahied.mastermindwebapp.manager;

import com.mostafawahied.mastermindwebapp.model.Game;
import com.mostafawahied.mastermindwebapp.model.GameHistory;
import com.mostafawahied.mastermindwebapp.model.GameState;
import com.mostafawahied.mastermindwebapp.model.User;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class GameSummaryGenerator {
    public String generateGameSummary(Game game, User currentUser) {
        if (currentUser == null) {
            return "";
        }

        StringBuilder summary = new StringBuilder();
        int points = 0;
        if (game.getGameState() == GameState.WON) {
            points = game.getGameDifficulty().winPoints;
        } else {
            points = game.getGameDifficulty().losePoints;
        }
        String badge = currentUser.getLatestAchievement();
        // Player's name and game details
        summary.append(String.format("👤 %s's Game: (%s, %s)\n",
                currentUser.getUsername(), game.getGameDifficulty(), game.getGameType()));
        // Badge, Score and points
        summary.append(String.format("%s 💰 Score: %d | 🎖️ %s\n",
                !Objects.equals(badge, "") ? "🏆 " + badge+ " | " : "", currentUser.getScore(), game.getGameState() == GameState.WON ? "+" + points : "-" + points));
        // Game attempts and results
        switch (game.getGameDifficulty()) {
            case EASY:
                summary.append(generateAttemptsForEasy(game)).append("\n");
                break;
            case MEDIUM:
//                summary.append(generateKeyAttemptsForMedium(game));
                break;
            case HARD:
//                summary.append(generateKeyAttemptsForHard(game));
                break;
        }
        // Game outcome
        if (game.getGameState().equals(GameState.WON)) {
            summary.append(String.format("🎉 WON in %d attempt/s!\n", game.getOriginalGuessCount() - game.getGameRemainingAttempts()));
        } else {
            summary.append(String.format("😢 LOST after %d attempt/s!\n", game.getOriginalGuessCount() - game.getGameRemainingAttempts()));
        }

        game.setGameSummary(summary.toString());
        return summary.toString();
    }

    private String generateAttemptsForEasy(Game game) {
        StringBuilder attemptsSummary = new StringBuilder();
        int attemptNumber = 1; // Initialize attempt number
        for (GameHistory history : game.getGameHistory()) {
            attemptsSummary.append(generateAttemptString(history, game, attemptNumber++)); // Increment attempt number
        }
        return attemptsSummary.toString();
    }

    private String generateAttemptString(GameHistory history, Game game, int attemptNumber) {
        StringBuilder attemptString = new StringBuilder();
        List<String> guess = history.getUserGuessList();
        List<String> solution = game.getSolution();

        Map<String, Integer> solutionFrequency = new HashMap<>();
        for (String number : solution) {
            solutionFrequency.put(number, solutionFrequency.getOrDefault(number, 0) + 1);
        }

        boolean[] correctPositions = new boolean[guess.size()];
        boolean[] matchedNumbers = new boolean[guess.size()];

        // First pass: check for correct positions (green squares)
        for (int i = 0; i < guess.size(); i++) {
            if (guess.get(i).equals(solution.get(i))) {
                correctPositions[i] = true;
                matchedNumbers[i] = true; // This position is matched
                int count = solutionFrequency.get(guess.get(i));
                solutionFrequency.put(guess.get(i), count - 1); // Decrement the frequency
            }
        }

        // Second pass: check for correct numbers but wrong positions (yellow squares)
        for (int i = 0; i < guess.size(); i++) {
            if (!correctPositions[i]) {
                if (solutionFrequency.getOrDefault(guess.get(i), 0) > 0) {
                    matchedNumbers[i] = true; // This number is matched
                    int count = solutionFrequency.get(guess.get(i));
                    solutionFrequency.put(guess.get(i), count - 1); // Decrement the frequency
                }
            }
        }

        // Construct the attempt string with green, yellow, and black squares
        for (int i = 0; i < guess.size(); i++) {
            if (correctPositions[i]) {
                attemptString.append("🟩");
            } else if (matchedNumbers[i]) {
                attemptString.append("🟨");
            } else {
                attemptString.append("⬛");
            }
        }

        // Append the pipe character only if it's not the last attempt
        if (attemptNumber < game.getGameHistory().size() || game.getGameState() != GameState.WON) {
            attemptString.append(" | ");
        }

        // Prepend the attempt number at the start
        attemptString.insert(0, String.format("%d️⃣ ", attemptNumber));
        return attemptString.toString();
    }


}


/*
the game summary designs we are going to use
Easy Difficulty, Numbers Type, Win
👤 Casey's Game: Mastermind (Easy, Numbers)
🏆 Streak Champion | 💰 Score: 500 | 🎖️ +100 Points
1️⃣ 🟨⬛⬛⬛ | 2️⃣ ⬛⬛🟨⬛ | 3️⃣ 🟨🟨⬛⬛ | 4️⃣ 🟩🟨⬛⬛
5️⃣ 🟩🟩🟨⬛ | 6️⃣ 🟩🟩🟩🟨 | 7️⃣ 🟩🟩🟩🟩
🎉 WON in 7 attempts!

Easy Difficulty, Numbers Type, Loss
👤 Casey's Game: Mastermind (Easy, Numbers)
💰 Score: 400 | 🎖️ -25 Points
1️⃣ 🟨⬛⬛⬛ | 2️⃣ ⬛⬛🟨⬛ | 3️⃣ 🟨🟨⬛⬛ | 4️⃣ ⬛🟨⬛⬛
5️⃣ ⬛🟩🟨⬛ | 6️⃣ ⬛🟩🟩🟨 | 7️⃣ ⬛🟩🟩⬛ | 8️⃣ ⬛⬛🟩🟨
9️⃣ ⬛🟩🟩⬛ | 🔟 ⬛🟩⬛🟨
😢 LOST after 10 attempts.

Medium Difficulty, Colors Type, Win
👤 Casey's Game: Mastermind (Medium, Colors)
🏆 Veteran | 💰 Score: 600 | 🎖️ +150 Points
🔑 First Correct: 2️⃣ 🟨⬛⬛⬛⬛
🌟 Best Attempt: 5️⃣ 🟨🟨🟨🟨⬛
🏁 Final Attempt: 8️⃣ 🟩🟩🟩🟩🟩
🎉 WON in 8 attempts!

Hard Difficulty, Numbers Type, Loss
👤 Casey's Game: Mastermind (Hard, Numbers)
💰 Score: 340 | 🎖️ -60 Points
🔑 First Correct: 5️⃣ 🟨⬛⬛⬛⬛⬛
🌟 Best Attempt: 25️⃣ 🟨🟨🟨🟨⬛⬛
🏁 Final Attempt: 40️⃣ 🟨🟨🟨🟨🟨⬛
😢 LOST after 40 attempts.
 */