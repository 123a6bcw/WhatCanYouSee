package ru.ralsei.whatcanyousee;

import android.app.Activity;
import android.media.MediaPlayer;
import android.widget.ImageView;

import ru.ralsei.whatcanyousee.R;

import java.util.Objects;

/**
 * Abstract class that show required logic for interacting with game map.
 */
abstract class MazeMap {
    private int imageID;

    public void setImageID(int imageID) {
        this.imageID = imageID;
    }



    MediaPlayer mp;

    Activity activity;

    int[][] imageIds;

    public MazeMap(Activity activity) {
        this.activity = activity;

        imageIds = new int[5][5];

        imageIds[0][0] = R.id.imageView0;
        imageIds[0][1] = R.id.imageView1;
        imageIds[0][2] = R.id.imageView2;
        imageIds[0][3] = R.id.imageView3;
        imageIds[0][4] = R.id.imageView4;

        imageIds[1][0] = R.id.imageView6;
        imageIds[1][1] = R.id.imageView7;
        imageIds[1][2] = R.id.imageView8;
        imageIds[1][3] = R.id.imageView9;
        imageIds[1][4] = R.id.imageView10;

        imageIds[2][0] = R.id.imageView12;
        imageIds[2][1] = R.id.imageView13;
        imageIds[2][2] = R.id.imageView14;
        imageIds[2][3] = R.id.imageView15;
        imageIds[2][4] = R.id.imageView16;

        imageIds[3][0] = R.id.imageView18;
        imageIds[3][1] = R.id.imageView19;
        imageIds[3][2] = R.id.imageView20;
        imageIds[3][3] = R.id.imageView21;
        imageIds[3][4] = R.id.imageView22;

        imageIds[4][0] = R.id.imageView24;
        imageIds[4][1] = R.id.imageView25;
        imageIds[4][2] = R.id.imageView26;
        imageIds[4][3] = R.id.imageView27;
        imageIds[4][4] = R.id.imageView28;
        //TODO make it ok
    }

    /**
     * Size of the map (width and height)
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
    private Cell[][] cells = new Cell[xSize][ySize];

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

    public void setExitCoordinates(Coordinates exitCoordinates) {
        this.exitCoordinates = exitCoordinates;
    }

    public void setInitialCoordinates(Coordinates coordinates) {
        this.currentCoordinates = coordinates;
    }

    public MazeMap.Cell[][] getCells() {
        return cells;
    }

    public void setCells(Cell[][] cells) {
        this.cells = cells;
    }

    /**
     *
     */
    public void draw() {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (i == 2 && j == 2) {
                    ((ImageView)(activity.findViewById(imageIds[i][j]))).setImageResource(R.drawable.you);
                } else {
                    int dfx = i - 2;
                    int dfy = j - 2;
                    int nx = currentCoordinates.getX() + dfx;
                    int ny = currentCoordinates.getY() + dfy;

                    int image = R.drawable.wall;
                    if (nx >= 0 && nx < xSize && ny >= 0 && ny < ySize) {
                        image = cells[nx][ny].image;
                    }
                    ((ImageView) (activity.findViewById(imageIds[j][i]))).setImageResource(image);
                }
            }
        }


    }

    /**
     *
     */
    class Cell {
        private boolean isWall = false;
        private Trap trap = null;
        private Toogle toogle = null;
        private Sign sign = null;
        private Torch torch = null;
        boolean isVisible = false;

        public void setToogle(Toogle toogle) {
            this.toogle = toogle;
        }

        public void setTrap(Trap trap) {
            this.trap = trap;
        }

        int image = R.drawable.emptycell;

        public void setImage(int image) {
            this.image = image;
        }

        public boolean isWall() {
            return isWall;
        }

        public boolean isTrap() {
            return trap != null;
        }

        public boolean isToogle() {
            return toogle != null;
        }

        public boolean isSign() {
            return sign != null;
        }

        public Trap getTrap() {
            return trap;
        }

        public Toogle getToogle() {
            return toogle;
        }

        public Sign getSign() {
            return sign;
        }

        public Torch getTorch() {
            return torch;
        }

        public void makeWall() {
            isWall = true;
            image = R.drawable.wall;
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
     * Game is over if player either lost or correctly reached the exit position.
     */
    protected boolean isOver = false;

    /**
     * If game is over, shows if player either won or lost.
     */
    protected boolean isWin = false;

    /**
     * Returns true if game is over and player has lost the game, for example player has been killed by stepping on a trap.
     */
    boolean hasLost() {
        return isOver && !isWin;
    }

    /**
     * Returns true if game is over and player has won, meaning successfully reaching the exit position.
     */
    boolean hasWon() {
        return isOver && isWin;
    }

    private Cell getCurrentCell() {
        return cells[currentCoordinates.getX()][currentCoordinates.getY()];
    }

    private Cell getCell(Coordinates coordinates) {
        if (coordinates.getX() < 0 || coordinates.getX() >= xSize || coordinates.getY() < 0 || coordinates.getY() >= ySize) {
            return null;
        }

        return cells[coordinates.getX()][coordinates.getY()];
    }

    Cell getRelatedCell(Coordinates coordinates) {
        return getCell(new Coordinates(currentCoordinates.getX() + coordinates.getX(), currentCoordinates.getY() + coordinates.getY()));
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
    abstract boolean checkConditionToExit();

    /**
     *
     */
    void use(Coordinates coordinates) {
        if (!getCurrentCell().isToogle()) {
            throw new IllegalStateException("Position with coordinates TODO write here");
        }

        getCurrentCell().getToogle().use();
    }

    void playerWon() {
        isOver = true;
        isWin = true;
    }

    private void playerLost() {
        isOver = true;
        isWin = false;
    }

    /**
     *
     */
    protected static abstract class Trap {
        abstract void apply();
    }

    /**
     *
     */
    protected abstract class Toogle {
        abstract void use();
    }

    /**
     *
     */
    protected class Sign {
        // Image
    }

    /**
     *
     */
    protected class Torch {
        int radius = 3;
    }


    /**
     * Class for storing coordinates on the screen.
     */
    static class Coordinates {
        private int x;
        private int y;

        Coordinates(int x, int y) {
            this.x = x;
            this.y = y;
        }

        Coordinates(Coordinates other) {
            this.x = other.x;
            this.y = other.y;
        }

        private int getX() {
            return x;
        }

        private int getY() {
            return y;
        }

        void moveToVector(MazeExplorer.Command command) {
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

        private boolean equals(Coordinates coordinates) {
            return x == coordinates.getX() && y == coordinates.getY();
        }
    }
}
