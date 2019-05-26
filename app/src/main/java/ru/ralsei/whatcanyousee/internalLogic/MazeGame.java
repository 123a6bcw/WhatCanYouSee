package ru.ralsei.whatcanyousee.internalLogic;

import android.app.Activity;
import android.media.MediaPlayer;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import ru.ralsei.whatcanyousee.GameActivity;

/**
 * Class that runs infinitive loop for explorer to explore the maze.
 */
public class MazeGame {

    //TODO
    private static final MediaPlayer[] players = new MediaPlayer[8];

    /**
     * TODO
     * TODO release
     */
    public static void playTrack(Activity activity, int trackId) {
        synchronized (players) {
            for (int i = 0; i < players.length; i++) {
                if (players[i] == null) {
                    players[i] = MediaPlayer.create(activity, trackId);
                    players[i].start();
                    return;
                }

                MediaPlayer mediaPlayer = players[i];

                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.selectTrack(trackId);
                    mediaPlayer.start();
                    break;
                }
            }
        }
    }

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
    private final MazeMap map;

    public MazeGame(final MazeMap map, final GameActivity activity) {
        this.map = map;
        this.gameActivity = activity;

        bfs();

        // TODO thread safe
        //TODO fix dermo nashel bagu
        ticker.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                    for (MazeMap.Monster monster : map.getMonsters()) {
                        monster.updateOnTick();
                        monster.tryToKill();
                        if (monster.readyToMove()) {
                            synchronized (map.getCells()) {
                                MazeMap.Cell closestCell = null;
                                for (MazeMap.Coordinates coordinates : vec4) {
                                    MazeMap.Cell cell = map.getRelatedCell(monster.getCurrentCoordinates(), coordinates);
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

                                MazeMap.Cell currentCell = map.getCell(monster.getCurrentCoordinates());
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
        DOWN, LEFT, UP, RIGHT, USE;
    }

    /**
     * Last command user has used by pressing on screen.
     */
    private volatile Command currentCommand = null;

    public void setCurrentCommand(Command currentCommand) {
        this.currentCommand = currentCommand;
    }

    /**
     * Related coordinates of all 8 cells around the player.
     */
    private MazeMap.Coordinates[] vec8 = {
            new MazeMap.Coordinates(-1, 0),
            new MazeMap.Coordinates(-1, -1),
            new MazeMap.Coordinates(0, -1),
            new MazeMap.Coordinates(1, -1),
            new MazeMap.Coordinates(1, 0),
            new MazeMap.Coordinates(1, 1),
            new MazeMap.Coordinates(0, 1),
            new MazeMap.Coordinates(-1, 1),
    };

    /**
     * Related coordinates of 4 cells (left, up, right, down) around the player.
     */
    private MazeMap.Coordinates[] vec4 = {
            new MazeMap.Coordinates(-1, 0),
            new MazeMap.Coordinates(0, -1),
            new MazeMap.Coordinates(1, 0),
            new MazeMap.Coordinates(0, 1)
    };

    /**
     * React to user command.
     */
    public void react(Command command) {
        currentCommand = null;

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
                for (MazeMap.Coordinates coordinates : vec8) {
                    MazeMap.Cell cell = map.getRelatedCell(coordinates);

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
     * Calculate (update) current cell's distances to the player position.
     */
    private void bfs() {
        synchronized (map.getCells()) {
            map.increaseDistanceId();
            int currentDistanceId = map.getDistanceId() + 1;

            MazeMap.Cell initialCell = map.getCurrentCell();
            initialCell.setDistance(0);
            initialCell.setDistanceId(currentDistanceId);

            LinkedList<MazeMap.Cell> queue = new LinkedList<>();
            queue.push(initialCell);
            while (!queue.isEmpty()) {
                MazeMap.Cell cell = queue.pop();
                for (MazeMap.Coordinates coordinates : vec4) {
                    MazeMap.Cell neighbourCell = map.getRelatedCell(cell, coordinates);

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
        for (MediaPlayer player : players) {
            if (player != null) {
                player.release();
            }
        }

        ticker.shutdown();
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
                map.setPlayerWon(true);
            } else {
                return;
            }
        }

        map.setCurrentCoordinates(newCoordinates);
    }
}