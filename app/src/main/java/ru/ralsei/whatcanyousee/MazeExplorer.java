package ru.ralsei.whatcanyousee;

import android.app.Activity;
import android.content.Intent;

/**
 * Class that runs infinitive loop for explorer to explore the maze.
 */
public class MazeExplorer {
    Activity activity;

    MazeMap map = null;

    public MazeExplorer(MazeMap map, Activity activity) {
        this.map = map;
        this.activity = activity;
    }

    /**
     * User commands from the screen buttons.
     */
    enum Command {
        DOWN, LEFT, UP, RIGHT, USE;
    }

    private volatile Command currentCommand = null;

    public void setCurrentCommand(Command currentCommand) {
        this.currentCommand = currentCommand;
    }

    /**
     * Runs infinitive loop for the game.
     */
    void cycle(Command command) {
        currentCommand = null;

        switch (command) {
            case RIGHT: case DOWN: case UP: case LEFT:
                tryToMove(map, command);
                if (map.hasLost()) {
                    gameOverLost();
                    return;
                }
                if (map.hasWon()) {
                    gameOverWon();
                    return;
                }
                map.draw();
                break;
            case USE:
                MazeMap.Coordinates[] vec = {
                        new MazeMap.Coordinates(-1, 0),
                        new MazeMap.Coordinates(-1, -1),
                        new MazeMap.Coordinates(0, -1),

                        new MazeMap.Coordinates(1, -1),
                        new MazeMap.Coordinates(1, 0),
                        new MazeMap.Coordinates(1, 1),

                        new MazeMap.Coordinates(0, 1),
                        new MazeMap.Coordinates(-1, 1),
                };

                for (int i = 0; i < 8; i++) {
                    MazeMap.Cell cell = map.getRelatedCell(vec[i]);

                    if (cell != null && cell.isToogle()) {
                        cell.getToogle().use();
                    }
                }
                map.draw();
                break;
        }
    }

    private Command getCommandFromUser() throws InterruptedException {
        while (true) {
            if (currentCommand != null) {
                return currentCommand;
            }
        }
    }

    private void gameOverLost() {
        Intent intent = new Intent(activity, GameLostActivity.class);
        activity.startActivity(intent);
    }

    private void gameOverWon() {
        Intent intent = new Intent(activity, GameWonActivity.class);
        activity.startActivity(intent);
    }

    /**
     * Tries to move to new location from user command and returns new coordinates.
     * If new position is either wall or exit, but condition to exit is not fulfilled yet, returns old coordinates.
     * If new position is exit and condition to exit has been fulfilled, returns old coordinates .
     * If new position is trap, applies this trap. May return loseCoordinates meaning player has lost the game.
     * Otherwise, just return corresponding new coordinates (either move player to left, right, up or down).
     */
    private static void tryToMove(MazeMap map, Command command) {
        MazeMap.Coordinates newCoordinates = new MazeMap.Coordinates(map.getCurrentCoordinates());
        newCoordinates.moveToVector(command);

        if (map.isWall(newCoordinates)) {
            return;
        }

        if (map.isTrap(newCoordinates)) {
            map.setCurrentCoordinates(newCoordinates);
            map.applyTrap();
            return;
        }

        if (map.isExit(newCoordinates)) {
            if (map.checkConditionToExit()) {
                map.playerWon();
            } else {
                return;
            }
        }

        map.setCurrentCoordinates(newCoordinates);
    }
}
