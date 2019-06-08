package ru.ralsei.whatcanyousee.internalLogic;

import android.view.View;
import android.widget.ImageView;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import ru.ralsei.whatcanyousee.GameActivity.GameActivity;
import ru.ralsei.whatcanyousee.R;

/**
 * Class that runs infinitive loop for explorer to explore the maze.
 */
public class MazeGame {
    /**
     * Activity game was called from.
     */
    private final GameActivity gameActivity;

    /**
     * Executor that updates the current game stage each DELAY milliseconds.
     */
    private ScheduledExecutorService ticker = Executors.newScheduledThreadPool(0);
    private static final int DELAY = 200;

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
                bfs();

                for (MazeGameMap.Monster monster : map.getMonsters()) {
                    monster.updateOnTick();

                    if (monster.tryToKill()) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                activity.getGameStatistic().setDeadByMonster(true);
                            }
                        });
                    }

                    if (checkIfLostGame()) {
                        activity.getGameStatistic().setDeadByMonster(true);
                    }

                    if (monster.readyToMove()) {
                        MazeGameMap.Cell closestCell = null;

                        Collections.shuffle(vec4); //Monster moves randomly if there is several directions he could move to.

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

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            map.draw();
                            checkIfGameOver();
                        }
                    });
            }
        }, 0, DELAY, TimeUnit.MILLISECONDS);
    }

    public void setupListeners() {
        gameActivity.findViewById(R.id.upButton).setOnClickListener(onClickListener);
        gameActivity.findViewById(R.id.leftButton).setOnClickListener(onClickListener);
        gameActivity.findViewById(R.id.rightButton).setOnClickListener(onClickListener);
        gameActivity.findViewById(R.id.downButton).setOnClickListener(onClickListener);
        gameActivity.findViewById(R.id.useButton).setOnClickListener(onClickListener);
        gameActivity.findViewById(R.id.button_show_map).setOnClickListener(onClickListener);
        gameActivity.findViewById(R.id.button_giveUp_maze).setOnClickListener(onClickListener);
    }

    /**
     * On click listener for the maze game.
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.upButton:
                    react(MazeGame.Command.UP);
                    break;
                case R.id.downButton:
                    react(MazeGame.Command.DOWN);
                    break;
                case R.id.leftButton:
                    react(MazeGame.Command.LEFT);
                    break;
                case R.id.rightButton:
                    react(MazeGame.Command.RIGHT);
                    break;
                case R.id.useButton:
                    react(MazeGame.Command.USE);
                    break;
                case R.id.button_show_map:
                    ImageView mazeMapImage = gameActivity.findViewById(R.id.image_maze_map);

                    if (mazeMapImage.getVisibility() == View.VISIBLE) {
                        mazeMapImage.setVisibility(View.INVISIBLE);
                    } else {
                        mazeMapImage.setVisibility(View.VISIBLE);
                    }
                    break;
                case R.id.button_giveUp_maze: {
                    gameActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            gameActivity.getGameplayHandler().onMazeGameLost("You gave up.");
                        }
                    });
                    break;
                }
                default:
                    break;
            }
        }
    };

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
    private void react(Command command) {
        switch (command) {
            case RIGHT: case DOWN: case UP: case LEFT:
                if (checkIfGameOver()) {
                    return;
                }

                tryToMove(map, command);

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
     * True if player either won or lost.
     */
    private boolean checkIfGameOver() {
        if (map.hasLost()) {
            gameOverLost(map.getMessageLost());
            return true;
        }

        if (map.hasWon()) {
            gameOverWon();
            return true;
        }

        return false;
    }

    /**
     * True of player lost the game.
     */
    private boolean checkIfLostGame() {
        return map.hasLost();
    }

    /**
     * Calculate (update) current cell's distances to the player position.
     */
    private void bfs() {
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

    /**
     * Reaction on user losing the game.
     */
    private void gameOverLost(final String message) {
        onClose();

        gameActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                gameActivity.getGameplayHandler().onMazeGameLost(message);
            }
        });
    }

    /**
     * Reaction on user winning the game.
     */
    private void gameOverWon() {
        onClose();

        gameActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                gameActivity.getGameplayHandler().onMazeGameWon();
            }
        });
    }

    /**
     * Stops the ticker scheduled task.
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
    private void tryToMove(MazeGameMap map, Command command) {
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

        if (map.hasMonster(newCoordinates)) {
            map.setPlayerWon(false);

            gameActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    gameActivity.getGameStatistic().setDeadByMonster(true);
                }
            });
        }

        map.setCurrentCoordinates(newCoordinates);
    }
}