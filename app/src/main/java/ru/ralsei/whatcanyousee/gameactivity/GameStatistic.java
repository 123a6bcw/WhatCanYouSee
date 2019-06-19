package ru.ralsei.whatcanyousee.gameactivity;

import lombok.Data;

/**
 * Class for storing statistic (and achievements) of the game.
 */
@Data
public class GameStatistic {
    /**
     * Time for winning the maze game.
     */
    private long mazeGameTime = -1;

    /**
     * Time for winning the code game.
     */
    private long codeGameTime = -1;

    /**
     * Time for winning the lever game.
     */
    private long leverGameTime = -1;

    /**
     * How many mistakes were taking when playing the code game.
     */
    private int codeGameMistakeTaken = -1;

    /**
     * True if player have died by the monster during the maze game.
     */
    private boolean deadByMonster = false;

    /**
     * True if player have killed his friend during the code game.
     */
    private boolean killYourFriend = false;

    /**
     * Resets all statistic to default.
     */
    void clear() {
        mazeGameTime = -1;
        codeGameTime = -1;
        leverGameTime = -1;
        codeGameMistakeTaken = -1;

        deadByMonster = false;
        killYourFriend = false;
    }

    public void incrementCodeGameMistakeTaken() {
        codeGameMistakeTaken++;
    }
}
