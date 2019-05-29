package ru.ralsei.whatcanyousee.internalLogic;

import android.app.Activity;
import android.media.MediaPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import ru.ralsei.whatcanyousee.GameActivity;

/**
 * Class that runs infinitive loop for explorer to explore the maze.
 */
public class MazeGame {
    /**
     * Activity game was called from.
     */
    private final GameActivity gameActivity;

    /**
     * Executor that will recalc distances from cell's to player.
     */
    private ExecutorService bfsExecutor = Executors.newSingleThreadExecutor();

    /**
     *
     */
    private ScheduledExecutorService ticker = Executors.newScheduledThreadPool(0);

    /**
     * Map of this maze.
     */
    private final MazeGameMap map;

    public MazeGame(final MazeGameMap map, final GameActivity activity) {
        this.map = map;
        this.gameActivity = activity;

        bfs();

        ticker.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                    for (MazeGameMap.Monster monster : map.getMonsters()) {
                        monster.updateOnTick();
                        monster.tryToKill();

                        if (checkIfLostGame()) {
                            activity.getGameStatistic().setDeadByMonster(true);
                        }

                        if (monster.readyToMove()) {
                            synchronized (map.getCells()) {
                                MazeGameMap.Cell closestCell = null;

                                Collections.shuffle(vec4); //Monster moves randomly if there is several directions hw could move to.

                                for (MazeGameMap.Coordinates coordinates : vec4) {
                                    MazeGameMap.Cell cell = map.getRelatedCell(monster.getCurrentCoordinates(), coordinates);
                                    if (cell == null) {
                                        continue;
                                    }

                                    if (closestCell == null || closestCell.getDistance() > cell.getDistance()) {
                                        closestCell = cell;
                                    }
                                }

                                if (closestCell == null) {
                                    throw new IllegalStateException("Monster trapped somewhere!");
                                }

                                MazeGameMap.Cell currentCell = map.getCell(monster.getCurrentCoordinates());
                                currentCell.resetImage();

                                closestCell.setImage(monster.getImageId());
                                monster.moveTo(closestCell);
                            }
                        }
                    }

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            map.draw();
                            checkIfGameOver();
                        }
                    });
            }
        }, 0, 200, TimeUnit.MILLISECONDS);
    }

    /**
     * User commands from the screen buttons.
     */
    public enum Command {
        DOWN, LEFT, UP, RIGHT, USE
    }

    /**
     * Related coordinates of all 8 cells around the player.
     */
    private MazeGameMap.Coordinates[] vec8 = {
            new MazeGameMap.Coordinates(-1, 0),
            new MazeGameMap.Coordinates(-1, -1),
            new MazeGameMap.Coordinates(0, -1),
            new MazeGameMap.Coordinates(1, -1),
            new MazeGameMap.Coordinates(1, 0),
            new MazeGameMap.Coordinates(1, 1),
            new MazeGameMap.Coordinates(0, 1),
            new MazeGameMap.Coordinates(-1, 1),
    };

    /**
     * Related coordinates of 4 cells (left, up, right, down) around the player.
     */
    private List<MazeGameMap.Coordinates> vec4 = Arrays.asList(new MazeGameMap.Coordinates(-1, 0),
            new MazeGameMap.Coordinates(0, -1),
            new MazeGameMap.Coordinates(1, 0),
            new MazeGameMap.Coordinates(0, 1));

    /**
     * React to user command.
     */
    public void react(Command command) {
        switch (command) {
            case RIGHT: case DOWN: case UP: case LEFT:
                if (checkIfGameOver()) {
                    return;
                }

                tryToMove(map, command);

                bfsExecutor.submit(new Runnable() {
                    @Override
                    public void run() {
                        bfs();
                    }
                });

                if (checkIfGameOver()) {
                    return;
                }

                map.draw();
                break;
            case USE:
                for (MazeGameMap.Coordinates coordinates : vec8) {
                    MazeGameMap.Cell cell = map.getRelatedCell(coordinates);

                    if (cell != null && cell.isToogle()) {
                        cell.getToogle().use();
                    }
                }
                map.draw();
                break;
        }
    }

    /**
     * TODO synchron.
     * True if player either won or lost.
     */
    private boolean checkIfGameOver() {
        synchronized (map.getGameResultLock()) {
            if (map.hasLost()) {
                gameOverLost();
                return true;
            }

            if (map.hasWon()) {
                gameOverWon();
                return true;
            }

            return false;
        }
    }

    /**
     * TODO
     */
    private boolean checkIfLostGame() {
        synchronized (map.getGameResultLock()) {
            return map.hasLost();

        }
    }

    /**
     * Calculate (update) current cell's distances to the player position.
     */
    private void bfs() {
        synchronized (map.getCells()) {
            map.increaseDistanceId();
            int currentDistanceId = map.getDistanceId() + 1;

            MazeGameMap.Cell initialCell = map.getCurrentCell();
            initialCell.setDistance(0);
            initialCell.setDistanceId(currentDistanceId);

            LinkedList<MazeGameMap.Cell> queue = new LinkedList<>();
            queue.push(initialCell);
            while (!queue.isEmpty()) {
                MazeGameMap.Cell cell = queue.pop();
                for (MazeGameMap.Coordinates coordinates : vec4) {
                    MazeGameMap.Cell neighbourCell = map.getRelatedCell(cell, coordinates);

                    if (neighbourCell == null || neighbourCell.isWall()) {
                        continue;
                    }

                    int possibleDistance = cell.getDistance() + 1;
                    if (neighbourCell.getDistanceId() < currentDistanceId || neighbourCell.getDistance() > possibleDistance) {
                        neighbourCell.setDistance(cell.getDistance() + 1);
                        neighbourCell.setDistanceId(currentDistanceId);
                        queue.push(neighbourCell);
                    }
                }
            }
        }
    }

    /**
     * Reaction on user losing the game.
     */
    private void gameOverLost() {
        onClose();
        gameActivity.getGameplayHandler().onMazeGameLost();
    }

    /**
     * Reaction on user winning the game.
     */
    private void gameOverWon() {
        onClose();
        gameActivity.getGameplayHandler().onMazeGameWon();
    }

    /**
     * TODO
     */
    public void onClose() {
        ticker.shutdown();
    }

    /**
     * Tries to move to new location from user command and returns new coordinates.
     * If new position is either wall or exit, but condition to exit is not fulfilled yet, returns old coordinates.
     * If new position is exit and condition to exit has been fulfilled, returns old coordinates .
     * If new position is trap, applies this trap. May return loseCoordinates meaning player has lost the game.
     * Otherwise, just return corresponding new coordinates (either move player to left, right, up or down).
     */
    private static void tryToMove(MazeGameMap map, Command command) {
        MazeGameMap.Coordinates newCoordinates = new MazeGameMap.Coordinates(map.getCurrentCoordinates());
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
                map.setPlayerWon(true);
            } else {
                return;
            }
        }

        map.setCurrentCoordinates(newCoordinates);
    }
}