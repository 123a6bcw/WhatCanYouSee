package ru.ralsei.whatcanyousee.gameactivity;

/**
 * Class for storing statistic (and achievements) of the game.
 */
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

    long getMazeGameTime() {
        return mazeGameTime;
    }

    void setMazeGameTime(long mazeGameTime) {
        this.mazeGameTime = mazeGameTime;
    }

    long getCodeGameTime() {
        return codeGameTime;
    }

    void setCodeGameTime(long codeGameTime) {
        this.codeGameTime = codeGameTime;
    }

    long getLeverGameTime() {
        return leverGameTime;
    }

    void setLeverGameTime(long leverGameTime) {
        this.leverGameTime = leverGameTime;
    }

    int getCodeGameMistakeTaken() {
        return codeGameMistakeTaken;
    }

    public void incrementCodeGameMistakeTaken() {
        this.codeGameMistakeTaken++;
    }

    boolean isDeadByMonster() {
        return deadByMonster;
    }

    public void setDeadByMonster(boolean deadByMonster) {
        this.deadByMonster = deadByMonster;
    }

    boolean isKillYourFriend() {
        return killYourFriend;
    }

    void setKillYourFriend() {
        this.killYourFriend = true;
    }
}
