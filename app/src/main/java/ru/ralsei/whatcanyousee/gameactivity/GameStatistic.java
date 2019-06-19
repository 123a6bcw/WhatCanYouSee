package ru.ralsei.whatcanyousee.gameactivity;

import lombok.Data;

/**
 * Class for storing statistic (and achievements) of the game.
 */
@Data
public class GameStatistic {
    private long mazeGameTime = -1;
    private long codeGameTime = -1;
    private long leverGameTime = -1;
    private int codeGameMistakeTaken = -1;
    private boolean deadByMonsterInMazeGame = false;
    private boolean teammateKilledInCodeGame = false;

    void resetStatisticToDefault() {
        mazeGameTime = -1;
        codeGameTime = -1;
        leverGameTime = -1;
        codeGameMistakeTaken = -1;

        deadByMonsterInMazeGame = false;
        teammateKilledInCodeGame = false;
    }

    public void incrementCodeGameMistakeTaken() {
        codeGameMistakeTaken++;
    }
}
