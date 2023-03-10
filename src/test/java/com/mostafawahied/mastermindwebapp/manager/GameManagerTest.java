package com.mostafawahied.mastermindwebapp.manager;

import com.mostafawahied.mastermindwebapp.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameManagerTest {

    List<String> correctResult = Arrays.asList("1","2","2","3");
    private Game game = new Game(correctResult, GameType.NUMBERS, GameDifficulty.EASY);
    private GameManager gameManager;
    @BeforeEach
    void setUp() {
        gameManager = new GameManager(game);
    }

    @Test
    public void test() {
        List<String> firstGuess = Arrays.asList("1","5","3","7");
        Game game = gameManager.handleGuess(firstGuess);
        GameHistory firstHistory = game.getGameHistory().get(0);
        assertEquals(firstHistory.getCorrectNumbers(), 2);
        assertEquals(firstHistory.getCorrectLocations(), 1);

        List<String> secondGuess = Arrays.asList("1","2","2","3");
        game = gameManager.handleGuess(secondGuess);
        GameHistory secondHistory = game.getGameHistory().get(1);
        assertEquals(secondHistory.getCorrectNumbers(), 3);
        assertEquals(secondHistory.getCorrectLocations(), 4);
        assertEquals(game.getGameState(), GameState.WON);
    }
}