package ru.ralsei.whatcanyousee.logic;

import android.graphics.Path;
import android.widget.ImageView;

import com.google.android.gms.common.api.Api;

import java.util.ArrayList;
import java.util.Optional;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import ru.ralsei.whatcanyousee.gameactivity.GameActivity;
import ru.ralsei.whatcanyousee.R;


public abstract class MazeGameMap {
    /**
     * Setup the meta data (like game width/height and other things to initialize).
     */
    abstract protected void setupMetaData();

    abstract protected void setupCells();

    abstract protected void setupTraps();

    abstract protected void setupToogles();

    abstract protected void setupMonsters();

    /**
     * How much cells does player see horizontally.
     */
    public static final int WIDTH_VIEW = 7;

    /**
     * How much cells does player see vertically.
     */
    public static final int HEIGHT_VIEW = 7;

    /**
     * Position of the central cell, where does player stands (horizontally).
     */
    private static final int CENTRAL_X = 3;

    /**
     * Same for vertically.
     */
    private static final int CENTRAL_Y = 3;

    /**
     * Inifinitely-like number.
     */
    private static final int INFINITY = 1000000;

    /**
     * Map's id of this maze that will be shown to the other player.
     */
    @Getter @Setter(AccessLevel.PROTECTED)
    private int imageID;

    /**
     * Activity map was created from.
     */
    @Getter(AccessLevel.PROTECTED)
    private final GameActivity activity;

    /**
     * Size of the map in number of cells.
     */
    @Getter(AccessLevel.PROTECTED) @Setter(AccessLevel.PROTECTED)
    private int width;

    @Getter(AccessLevel.PROTECTED) @Setter(AccessLevel.PROTECTED)
    private int height;

    /**
     * Exit coordinates. Player can reach this position only if special condition of leaving the maze has been fulfilled.
     */
    @Setter(AccessLevel.PROTECTED)
    private Coordinates exitCoordinates;

    /**
     * Each cell has coordinates in ranges [0, width) and [0, height).
     * This gives info about type of corresponding cell.
     */
    @Getter(AccessLevel.PROTECTED) @Setter(AccessLevel.PROTECTED)
    private Cell[][] cells;

    /**
     * Corresponding cell's ImageView.
     */
    @Getter
    private int[][] imageIds;

    /**
     * Current player's position. Initial position is implementation-specific.
     */
    @Getter(AccessLevel.PACKAGE) @Setter(AccessLevel.PACKAGE)
    private Coordinates currentCoordinates;

    protected void setInitialCoordinates(Coordinates coordinates) {
        this.currentCoordinates = coordinates;
    }

    public MazeGameMap(GameActivity activity) {
        this.activity = activity;

        imageIds = new int[WIDTH_VIEW][HEIGHT_VIEW];

        imageIds[0][0] = R.id.cellImage00;
        imageIds[0][1] = R.id.cellImage01;
        imageIds[0][2] = R.id.cellImage02;
        imageIds[0][3] = R.id.cellImage03;
        imageIds[0][4] = R.id.cellImage04;
        imageIds[0][5] = R.id.cellImage05;
        imageIds[0][6] = R.id.cellImage06;

        imageIds[1][0] = R.id.cellImage10;
        imageIds[1][1] = R.id.cellImage11;
        imageIds[1][2] = R.id.cellImage12;
        imageIds[1][3] = R.id.cellImage13;
        imageIds[1][4] = R.id.cellImage14;
        imageIds[1][5] = R.id.cellImage15;
        imageIds[1][6] = R.id.cellImage16;

        imageIds[2][0] = R.id.cellImage20;
        imageIds[2][1] = R.id.cellImage21;
        imageIds[2][2] = R.id.cellImage22;
        imageIds[2][3] = R.id.cellImage23;
        imageIds[2][4] = R.id.cellImage24;
        imageIds[2][5] = R.id.cellImage25;
        imageIds[2][6] = R.id.cellImage26;

        imageIds[3][0] = R.id.cellImage30;
        imageIds[3][1] = R.id.cellImage31;
        imageIds[3][2] = R.id.cellImage32;
        imageIds[3][3] = R.id.cellImage33;
        imageIds[3][4] = R.id.cellImage34;
        imageIds[3][5] = R.id.cellImage35;
        imageIds[3][6] = R.id.cellImage36;

        imageIds[4][0] = R.id.cellImage40;
        imageIds[4][1] = R.id.cellImage41;
        imageIds[4][2] = R.id.cellImage42;
        imageIds[4][3] = R.id.cellImage43;
        imageIds[4][4] = R.id.cellImage44;
        imageIds[4][5] = R.id.cellImage45;
        imageIds[4][6] = R.id.cellImage46;

        imageIds[5][0] = R.id.cellImage50;
        imageIds[5][1] = R.id.cellImage51;
        imageIds[5][2] = R.id.cellImage52;
        imageIds[5][3] = R.id.cellImage53;
        imageIds[5][4] = R.id.cellImage54;
        imageIds[5][5] = R.id.cellImage55;
        imageIds[5][6] = R.id.cellImage56;

        imageIds[6][0] = R.id.cellImage60;
        imageIds[6][1] = R.id.cellImage61;
        imageIds[6][2] = R.id.cellImage62;
        imageIds[6][3] = R.id.cellImage63;
        imageIds[6][4] = R.id.cellImage64;
        imageIds[6][5] = R.id.cellImage65;
        imageIds[6][6] = R.id.cellImage66;

        setupMetaData();
        setupCells();
        setupTraps();
        setupToogles();
        setupMonsters();

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                cells[i][j].setX(i);
                cells[i][j].setY(j);
                cells[i][j].setDistance(INFINITY);
                cells[i][j].resetImageToDefault();
            }
        }

        for (Monster monster : monsters) {
            getCell(monster.getCurrentCoordinates()).setImage(monster.getImageId());
        }
    }

    /**
     * Sets to each cell's imageView correct (current) image.
     */
    public void draw() {
        for (int i = 0; i < WIDTH_VIEW; i++) {
            for (int j = 0; j < HEIGHT_VIEW; j++) {
                if (i == CENTRAL_X && j == CENTRAL_Y) {
                    ImageView imageView = (activity.findViewById(imageIds[i][j]));
                    if (imageView != null) {
                        imageView.setImageResource(R.drawable.you);
                    }
                } else {
                    int dfx = i - CENTRAL_X;
                    int dfy = j - CENTRAL_Y;
                    int nx = currentCoordinates.getX() + dfx;
                    int ny = currentCoordinates.getY() + dfy;

                    int image = R.drawable.wall;
                    if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                        image = cells[nx][ny].image;
                    }

                    ImageView imageView = (activity.findViewById(imageIds[j][i]));
                    if (imageView != null) {
                        imageView.setImageResource(image);
                    }
                }
            }
        }
    }

    /**
     * True if x coordinate is not within maze bounds, meaning not within [0, width);
     */
    private boolean notInHorizontalBounds(int x) {
        return x < 0 || x >= width;
    }

    /**
     * True if y coordinate is not within maze bounds, meaning not within [0, height);
     */
    private boolean notInVerticalBounds(int y) {
        return y < 0 || y >= height;
    }

    /**
     * True if given coordinates are not within maze bounds [0, width) x [0, height);
     */
    private boolean notInBounds(Coordinates coordinates) {
        return notInHorizontalBounds(coordinates.getX()) || notInVerticalBounds(coordinates.getY());
    }

    /**
     * Make the cells walls with coordinates from xBegin to xEnd inclusive and with y = y.
     */
    protected void makeHorizontalWall(int xBegin, int xEnd, int y) {
        makeWall(xBegin, xEnd, y, y);
    }

    /**
     * Make the cells walls with coordinates from yBegin to yEnd inclusive and with y = y.
     */
    protected void makeVerticalWall(int x, int yBegin, int yEnd) {
        makeWall(x, x, yBegin, yEnd);
    }

    /**
     * Makes the cells walls with x coordinates from xBegin to xEnd inclusive and y coordinates from
     * yBegin to yEnd.
     */
    protected void makeWall(int xBegin, int xEnd, int yBegin, int yEnd) {
        if (notInHorizontalBounds(xBegin) || notInHorizontalBounds(xEnd) || notInVerticalBounds(yBegin) || notInVerticalBounds(yEnd)) {
            throw new IllegalArgumentException("Coordinates is not within maze bounds");
        }

        if (xBegin > xEnd) {
            throw new IllegalArgumentException("Left coordinate is higher than right coordinate");
        }

        if (yBegin > yEnd) {
            throw new IllegalArgumentException("Bottom coordinate is higher than up coordinate");
        }

        for (int i = xBegin; i <= xEnd; i++) {
            for (int j = yBegin; j <= yEnd; j++) {
                cells[i][j].makeWall();
            }
        }
    }

    /**
     * Each cell has distanceId. If this distanceId is lower than this number, cell's distance is invalid
     * and has to be updated.
     */
    @Getter(AccessLevel.PACKAGE)
    private int distanceId = 0;

    void increaseDistanceId() {
        distanceId++;
    }

    boolean hasMonster(Coordinates newCoordinates) {
        return getCell(newCoordinates).numberOfMonstersInCell > 0;
    }

    /**
     * Class for storing information about each cell (in all maze, not just cell that players sees).
     */
    public class Cell {
        /**
         * True if cell is wall, meaning you can't go through this cell.
         */
        @Getter(AccessLevel.PACKAGE)
        private boolean isWall = false;

        private int numberOfMonstersInCell = 0;

        /**
         * Cell coordinates.
         */
        @Getter(AccessLevel.PACKAGE) @Setter(AccessLevel.PRIVATE)
        private int x;

        @Getter(AccessLevel.PACKAGE) @Setter(AccessLevel.PRIVATE)
        private int y;

        /**
         * True if cell contains trap, meaning special event triggers when you step on this cell.
         */
        @Getter(AccessLevel.PACKAGE) @Setter
        private Trap trap = null;

        /**
         * True if cell contains toogle, meaning pressing it (by using key 'use') causing some special
         * event (for example, opening some door).
         */
        @Getter(AccessLevel.PACKAGE) @Setter
        private Toogle toogle = null;

        /**
         * Current distance between the player and this cell.
         */
        @Getter @Setter(AccessLevel.PACKAGE)
        private int distance = 0;

        /**
         * If lower than map.distanceId, distance of this cell is invalid and should be updated.
         */
        @Getter(AccessLevel.PACKAGE) @Setter(AccessLevel.PACKAGE)
        private int distanceId = 0;

        /**
         * Current cell's image.
         */
        @Setter
        private int image = R.drawable.emptycell;

        /**
         * Default cell's image.
         */
        @Setter(AccessLevel.PUBLIC)
        private int defaultImage = R.drawable.emptycell;

        void resetImageToDefault() {
            this.image = defaultImage;
        }

        boolean isTrap() {
            return trap != null;
        }

        boolean isToogle() {
            return toogle != null;
        }

        public void makeWall() {
            isWall = true;
            defaultImage = R.drawable.wall;
            image = R.drawable.wall;
        }

        public void makeNotWall() {
            isWall = false;
            defaultImage = R.drawable.emptycell;
            image = R.drawable.emptycell;
        }
    }

    /**
     * If game is over, shows if player either won or lost.
     */
    private boolean isWin;
    private boolean isOver = false;

    private final Object winLock = new Object();

    /**
     * Message to show when players loosing the game.
     */
    @Getter(AccessLevel.PACKAGE) @Setter(AccessLevel.PROTECTED)
    private String messageLost = "";

    protected void setPlayerWon(boolean result) {
        synchronized (winLock) {
            isWin = result;
            isOver = true;
        }
    }

    /**
     * Returns true if game is over and player has lost the game, for example player has been killed by stepping on a trap.
     */
    boolean hasLost() {
        synchronized (winLock) {
            return isOver && !isWin;
        }
    }

    /**
     * Returns true if game is over and player has won, meaning successfully reaching the exit position.
     */
    boolean hasWon() {
        synchronized (winLock) {
            return isOver && isWin;
        }
    }

    /**
     * Returns cell that player are standing at.
     */
    Cell getCurrentCell() {
        return cells[currentCoordinates.getX()][currentCoordinates.getY()];
    }

    Cell getCell(Coordinates coordinates) {
        if (notInBounds(coordinates)) {
            return null;
        }

        return cells[coordinates.getX()][coordinates.getY()];
    }

    /**
     * Returns cell with coordinates currentCoordinates + coordinates (currentX + coordinates.getX(), currentY + coordinates.getY()).
     */
    Cell getRelatedCell(Coordinates coordinates) {
        return getCell(new Coordinates(currentCoordinates.getX() + coordinates.getX(), currentCoordinates.getY() + coordinates.getY()));
    }

    /**
     * Returns cell with coordinates cellCoordinates + relatedCoordinates.
     */
    Cell getRelatedCell(Coordinates cellCoordinates, Coordinates relatedCoordinates) {
        return getCell(new Coordinates(cellCoordinates.getX() + relatedCoordinates.getX(), cellCoordinates.getY() + relatedCoordinates.getY()));
    }

    /**
     * Returns cell with coordinates cell.coordinates + coordinates.
     */
    Cell getRelatedCell(Cell cell, Coordinates coordinates) {
        return getCell(new Coordinates(cell.getX() + coordinates.getX(), cell.getY() + coordinates.getY()));
    }

    /**
     * Returns if cell with given coordinates is wall, meaning you can't go through this cell.
     */
    boolean isWall(Coordinates coordinates) {
        Cell cell = getCell(coordinates);
        return cell != null && cell.isWall();
    }

    /**
     * Returns if cell with given coordinates is trap, meaning stepping on this cell causing a special event.
     */
    boolean isTrap(Coordinates coordinates) {
        Cell cell = getCell(coordinates);
        return cell != null && cell.isTrap();
    }

    /**
     * If player is staying on the trap, causing a special event of this trap.
     */
    void applyTrap() throws IllegalStateException {
        if (!isTrap(currentCoordinates)) {
            throw new IllegalStateException("Position with coordinates " + currentCoordinates.getX() + " " + currentCoordinates.getY() + " is not a trap");
        }

        getCurrentCell().getTrap().apply();
    }

    boolean isExit(Coordinates coordinates) {
        return exitCoordinates.equals(coordinates);
    }

    /**
     * Returns true if conditions to exit has been fulfilled and player can win maze by just stepping on exit cell.
     */
    protected abstract boolean checkConditionToExit();

    /**
     * Trap is a special cell that causing a special event when you stepping on it.
     * Basically, to create a trap one just has to implement that special event.
     */
    protected static abstract class Trap {
        /**
         * Event that happens when player steps on a trap.
         */
        protected abstract void apply();
    }

    /**
     * Toogle is a special cell that causing a special event (opening door for instance)
     * when player press button 'use' near toogle.
     *
     * Basically, to create a toogle one just has to implement that special event.
     */
    protected abstract class Toogle {
        /**
         * Special event cause by pressing this toogle.
         */
        protected abstract void use();
    }

    @Getter(AccessLevel.PROTECTED)
    private final ArrayList<Monster> monsters = new ArrayList<>();

    protected void addMonster(Monster monster) {
        monsters.add(monster);
    }

    /**
     * Class representing the monster in maze.
     * Monster updates it's state on each tick of the ticker and moves to the closest cell to the player
     * if he is ready to move (for example, he can move each 5 ticks). On each tick he also tries to
     * kill the player (i.e. if he is standing on the cell with the player).
     */
    protected abstract class Monster {
        /**
         * Image of this monster.
         */
        @Getter(AccessLevel.PACKAGE) @Setter(AccessLevel.PROTECTED)
        private int imageId;

        @Getter(AccessLevel.PROTECTED)
        private int initialX;

        @Getter(AccessLevel.PROTECTED)
        private int initialY;

        private int currentX;
        private int currentY;

        /**
         * True if monster is ready to move -- he will be moved on the next tick.
         */
        abstract protected boolean readyToMove();

        void moveTo(Cell cell) {
            this.getCurrentCell().numberOfMonstersInCell--;
            currentX = cell.getX();
            currentY = cell.getY();
            cell.numberOfMonstersInCell++;
        }

        /**
         * Updates monster's state on gameplay tick -- for instance, monster could be moving each
         * 5 ticks, so here he updates his counter.
         */
        abstract protected void updateOnTick();

        protected void setInitialX(int initialX) {
            this.initialX = initialX;
            currentX = initialX;
        }

        protected void setInitialY(int initialY) {
            this.initialY = initialY;
            currentY = initialY;
        }

        Coordinates getCurrentCoordinates() {
           return new Coordinates(currentX, currentY);
        }

        protected Cell getCurrentCell() {
            return getCell(getCurrentCoordinates());
        }

        /**
         * Monster tries to kill the player -- usually if he stepped on the cell with the player.
         */
        abstract protected boolean tryToKill();

        /**
         * Decreasing tickTo, but if it's 0, sets it to tickPer.
         */
        protected int decreaseTick(int tickTo, int tickPer) {
            if (tickTo == 0) {
                return tickPer - 1;
            }

            return tickTo - 1;
        }
    }

    /**
     * Class for storing cell's coordinates, where the cell on the bottom-left corner of the screen has
     * (0, 0) coordinates.
     */
    @Data
    @AllArgsConstructor(access = AccessLevel.PUBLIC)
    protected static class Coordinates {
        /**
         * X coordinate.
         */
        @Getter(AccessLevel.PACKAGE) @Setter(AccessLevel.NONE)
        private int x;

        /**
         * Y coordinate.
         */
        @Getter(AccessLevel.PACKAGE) @Setter(AccessLevel.NONE)
        private int y;

        Coordinates(Coordinates other) {
            x = other.getX();
            y = other.getY();
        }

        void moveToVector(MazeGame.Command command) {
            switch (command) {
                case LEFT:
                    x--;
                    break;
                case RIGHT:
                    x++;
                    break;
                case DOWN:
                    y--;
                    break;
                case UP:
                    y++;
                    break;
                default:
                    throw new IllegalArgumentException("wrong command to move");
            }
        }
    }
}
