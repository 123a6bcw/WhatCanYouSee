package ru.ralsei.whatcanyousee.internalLogic;

import android.app.Activity;
import android.widget.ImageView;

import java.util.ArrayList;

import ru.ralsei.whatcanyousee.GameActivity;
import ru.ralsei.whatcanyousee.R;

/**
 * Abstract class that show required logic for interacting with game map.
 */
@SuppressWarnings("WeakerAccess")
public abstract class MazeGameMap {
    /**
     * TODO
     */
    abstract protected void setupMetaData();

    /**
     * TODO
     */
    abstract protected void setupCells();

    /**
     * TODO
     */
    abstract protected void setupTraps();

    /**
     * TODO
     */
    abstract protected void setupToogles();

    /**
     * TODO
     */
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
     * TODO
     */
    private static final int INFINITY = 1000000;

    /**
     * Map's id of this maze that will be shown to the other player.
     */
    private int imageID;

    public void setImageID(int imageID) {
        this.imageID = imageID;
    }

    /**
     * Activity map was created from.
     */
    private final GameActivity activity;

    /**
     * Activity map was created from.
     */
    public GameActivity getActivity() {
        return activity;
    }

    /**
     * Size of the map (width and height).
     */
    private int xSize;
    private int ySize;

    /**
     * Exit coordinates. Player can reach this position only if special condition of leaving the maze has been fulfilled.
     */
    private Coordinates exitCoordinates;

    /**
     * Each cell has coordinates in ranges [0, xSize) and [0, ySize).
     * This gives info about type of corresponding cell.
     */
    private Cell[][] cells;

    /**
     * Corresponding cell's ImageView.
     */
    private int[][] imageIds;

    /**
     * Current player's position. Initial position is implementation-specific.
     */
    private Coordinates currentCoordinates;

    public int getxSize() {
        return xSize;
    }

    public int getySize() {
        return ySize;
    }

    public void setxSize(int xSize) {
        this.xSize = xSize;
    }

    public void setySize(int ySize) {
        this.ySize = ySize;
    }

    public int[][] getImageIds() {
        return imageIds;
    }

    public Coordinates getExitCoordinates() {
        return exitCoordinates;
    }

    public void setExitCoordinates(Coordinates exitCoordinates) {
        this.exitCoordinates = exitCoordinates;
    }

    public void setInitialCoordinates(Coordinates coordinates) {
        this.currentCoordinates = coordinates;
    }

    public MazeGameMap.Cell[][] getCells() {
        return cells;
    }

    protected void setCells(Cell[][] cells) {
        this.cells = cells;
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

        for (int i = 0; i < xSize; i++) {
            for (int j = 0; j < ySize; j++) {
                cells[i][j].setX(i);
                cells[i][j].setY(j);
                cells[i][j].setDistance(INFINITY);
                cells[i][j].resetImage();

                //TODO ImageView imageView =  ((ImageView) activity.findViewById(imageIds[i][j]));
                //TODO imageView.setMinimumHeight();
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
        synchronized (this) {
            for (int i = 0; i < WIDTH_VIEW; i++) {
                for (int j = 0; j < HEIGHT_VIEW; j++) {
                    if (i == CENTRAL_X && j == CENTRAL_Y) {
                        ((ImageView) (activity.findViewById(imageIds[i][j]))).setImageResource(R.drawable.you);
                    } else {
                        int dfx = i - CENTRAL_X;
                        int dfy = j - CENTRAL_Y;
                        int nx = currentCoordinates.getX() + dfx;
                        int ny = currentCoordinates.getY() + dfy;

                        int image = R.drawable.wall;
                        if (nx >= 0 && nx < xSize && ny >= 0 && ny < ySize) {
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
    }

    /**
     * True if x coordinate is not within maze bounds, meaning not within [0, xSize);
     */
    private boolean notInHorizontalBounds(int x) {
        return x < 0 || x >= xSize;
    }

    /**
     * True if y coordinate is not within maze bounds, meaning not within [0, ySize);
     */
    private boolean notInVerticalBounds(int y) {
        return y < 0 || y >= ySize;
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
     * Each cell has distanceId. If this distanceId is lower than this number, cell distance is invalid
     * and has to bee updated.
     */
    private int distanceId = 0;

    public int getDistanceId() {
        return distanceId;
    }

    public void increaseDistanceId() {
        distanceId++;
    }

    /**
     * Class for storing information about each cell (in all maze, not just cell that players sees).
     */
    public class Cell {
        /**
         * True if cell is wall, meaning you can't go through this cell.
         */
        private boolean isWall = false;

        /**
         * Cell coordinates.
         */
        private int x;
        private int y;

        /**
         * True if cell contains trap, meaning special event triggers when you step on this cell/
         */
        private Trap trap = null;

        /**
         * True if cell contains toogle, meaning pressing it (by using key 'use') causing some special
         * event (for example, opening some door).
         */
        private Toogle toogle = null;

        /**
         * False if you cannot see this cell right now, for example, because it is behind the wall/closed door
         */
        private boolean isVisible = false;

        /**
         * Current distance between the player and this cell.
         */
        private int distance = 0;

        /**
         * If lower than map.distanceId, distance of this cell is invalid and should be updated.
         */
        private int distanceId = 0;

        /**
         * Current cell's image.
         */
        private int image = R.drawable.emptycell;

        /**
         * Default cell's image.
         */
        private int defaultImage = R.drawable.emptycell;

        public void setToogle(Toogle toogle) {
            this.toogle = toogle;
        }

        public void setTrap(Trap trap) {
            this.trap = trap;
        }

        public void setImage(int image) {
            this.image = image;
        }

        public void setDefaultImage(int image) {
            this.defaultImage = image;
        }

        public void resetImage() {
            this.image = defaultImage;
        }

        protected boolean isWall() {
            return isWall;
        }

        protected boolean isTrap() {
            return trap != null;
        }

        protected boolean isToogle() {
            return toogle != null;
        }

        protected Trap getTrap() {
            return trap;
        }

        protected Toogle getToogle() {
            return toogle;
        }

        /**
         * Makes given cell a wall cell.
         */
        public void makeWall() {
            isWall = true;
            defaultImage = R.drawable.wall;
        }

        public int getDistance() {
            return distance;
        }

        public void setDistance(int distance) {
            this.distance = distance;
        }

        public int getX() {
            return x;
        }

        private void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        private void setY(int y) {
            this.y = y;
        }

        public int getDistanceId() {
            return distanceId;
        }

        public void setDistanceId(int distanceId) {
            this.distanceId = distanceId;
        }
    }

    /**
     * Returns current player's coordinates.
     */
    Coordinates getCurrentCoordinates() {
        return currentCoordinates;
    }

    /**
     * Sets current player's coordinates to another.
     */
    void setCurrentCoordinates(Coordinates coordinates) {
        this.currentCoordinates = coordinates;
    }


    /**
     * TODO
     */
    private final Object gameResultLock = new Object();

    /**
     * TODO
     */
    public Object getGameResultLock() {
        return gameResultLock;
    }

    /**
     * Game is over if player either lost or correctly reached the exit position.
     */
    private boolean isOver = false;

    /**
     * If game is over, shows if player either won or lost.
     */
    private boolean isWin = false;

    /**
     * TODO
     */
    private String message = "You lost.";

    /**
     * TODO
     */
    protected void setPlayerWon(boolean result) {
        synchronized (gameResultLock) {
            isOver = true;
            isWin = result;
        }
    }

    /**
     * TODO
     */
    protected void setPlayerWon(boolean result, String message) {
        synchronized (gameResultLock) {
            isOver = true;
            isWin = result;
            this.message = message;
        }
    }

    /**
     * Returns true if game is over and player has lost the game, for example player has been killed by stepping on a trap.
     */
    boolean hasLost() {
        synchronized (gameResultLock) {
            return isOver && !isWin;
        }
    }

    /**
     * Returns true if game is over and player has won, meaning successfully reaching the exit position.
     */
    boolean hasWon() {
        synchronized (gameResultLock) {
            return isOver && isWin;
        }
    }

    /**
     * Returns cell that player are standing at.
     */
    Cell getCurrentCell() {
        return cells[currentCoordinates.getX()][currentCoordinates.getY()];
    }

    /**
     * Returns cell by coordinates.
     */
    Cell getCell(Coordinates coordinates) {
        if (coordinates.getX() < 0 || coordinates.getX() >= xSize || coordinates.getY() < 0 || coordinates.getY() >= ySize) {
            return null;
        }

        return cells[coordinates.getX()][coordinates.getY()];
    }

    /**
     * Returns cell with coordinates (currentX + dx, currentY + dy).
     */
    Cell getRelatedCell(Coordinates coordinates) {
        return getCell(new Coordinates(currentCoordinates.getX() + coordinates.getX(), currentCoordinates.getY() + coordinates.getY()));
    }

    /**
     * Returns cell with coordinates (currentX + dx, currentY + dy).
     */
    Cell getRelatedCell(Coordinates cellCoordinates, Coordinates relatedCoordinates) {
        return getCell(new Coordinates(cellCoordinates.getX() + relatedCoordinates.getX(), cellCoordinates.getY() + relatedCoordinates.getY()));
    }

    /**
     * Returns cell with coordinates (cellX + dx, cellY + dy).
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

    /**
     * Returns true if cell with given coordinates is exit position.
     */
    boolean isExit(Coordinates coordinates) {
        return exitCoordinates.equals(coordinates);
    }

    /**
     * Returns true if conditions to exit has been fulfilled and player can win maze by just stepping on exit cell.
     */
    protected abstract boolean checkConditionToExit();

    /**
     * Press toogle with given coordinates.
     */
    private void use(Coordinates coordinates) {
        Cell cell = getCell(coordinates);

        if (cell == null) {
            throw new IllegalArgumentException("Cell is out of bound in maze.");
        }

        if (!cell.isToogle()) {
            throw new IllegalStateException("Position with given coordinates does not contain toogle");
        }

        cell.getToogle().use();
    }

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

    /**
     * TODO javadocs
     */
    private final ArrayList<Monster> monsters = new ArrayList<>(0);

    protected void addMonster(Monster monster) {
        monsters.add(monster);
    }

    public ArrayList<Monster> getMonsters() {
        return monsters;
    }

    /**
     * TODO
     */
    protected abstract class Monster {
        /**
         * TODO
         */
        private int imageId;

        /**
         * TODO
         */
        private int initialX;

        /**
         * TODO
         */
        private int initialY;

        /**
         * TODO
         */
        private int currentX;

        /**
         * TODO
         */
        private int currentY;

        /**
         * TODO
         */
        abstract protected boolean readyToMove();

        /**
         * TODO
         */
        protected void moveTo(Cell cell) {
           currentX = cell.getX();
           currentY = cell.getY();
        }

        /**
         * TODO
         */
        abstract protected void updateOnTick();

        /**
         * TODO
         */
        protected int getImageId() {
            return imageID;
        }

        protected void setImageId(int imageId) {
            this.imageId = imageId;
        }

        protected int getInitialX() {
            return initialX;
        }

        protected void setInitialX(int initialX) {
            this.initialX = initialX;
            currentX = initialX;
        }

        protected int getInitialY() {
            return initialY;
        }

        protected void setInitialY(int initialY) {
            this.initialY = initialY;
            currentY = initialY;
        }

        protected Coordinates getCurrentCoordinates() {
           return new Coordinates(currentX, currentY);
        }

        protected Cell getCurrentCell() {
            return getCell(getCurrentCoordinates());
        }

        /**
         * TODO
         */
        abstract protected void tryToKill();

        /**
         * TODO
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
    protected static class Coordinates {
        /**
         * X coordinate.
         */
        private int x;

        /**
         * Y coordinate.
         */
        private int y;

        public Coordinates(int x, int y) {
            this.x = x;
            this.y = y;
        }

        protected Coordinates(Coordinates other) {
            this.x = other.x;
            this.y = other.y;
        }

        protected int getX() {
            return x;
        }

        protected int getY() {
            return y;
        }

        protected void moveToVector(MazeGame.Command command) {
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

        protected boolean equals(Coordinates coordinates) {
            return x == coordinates.getX() && y == coordinates.getY();
        }
    }
}
