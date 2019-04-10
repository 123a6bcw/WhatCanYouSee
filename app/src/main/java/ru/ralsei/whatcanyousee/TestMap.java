package ru.ralsei.whatcanyousee;

import android.app.Activity;
import android.media.MediaPlayer;

import ru.ralsei.whatcanyousee.R;

public class TestMap extends MazeMap {
    boolean tooglePressed = false;

    public TestMap(final Activity activity) {
        super(activity);

        setxSize(12);
        setySize(20);
        setExitCoordinates(new Coordinates(10, 17));
        setInitialCoordinates(new Coordinates(9, 10));
        final Cell[][] cells = new Cell[getxSize()][getySize()];
        setCells(cells);
        for (int i = 0; i < getxSize(); i++) {
            for (int j = 0; j < getySize(); j++) {
                cells[i][j] = new Cell();
            }
        }

        for (int j = 0; j <= 19; j++) {
            cells[0][j].makeWall();
        }

        for (int j = 0; j <= 8; j++) {
            cells[1][j].makeWall();
        }

        for (int j = 8; j <= 8; j++) {
            cells[1][j].makeWall();
        }

        for (int j = 13; j <= 13; j++) {
            cells[1][j].makeWall();
        }

        for (int j = 17; j <= 19; j++) {
            cells[1][j].makeWall();
        }

        //

        for (int j = 0; j <= 1; j++) {
            cells[2][j].makeWall();
        }

        for (int j = 5; j <= 7; j++) {
            cells[2][j].makeWall();
        }

        for (int j = 18; j <= 19; j++) {
            cells[2][j].makeWall();
        }

        //

        for (int j = 0; j <= 0; j++) {
            cells[3][j].makeWall();
        }

        for (int j = 5; j <= 6; j++) {
            cells[3][j].makeWall();
        }

        for (int j = 13; j <= 13; j++) {
            cells[3][j].makeWall();
        }

        for (int j = 19; j <= 19; j++) {
            cells[3][j].makeWall();
        }

        //

        for (int j = 0; j <= 0; j++) {
            cells[4][j].makeWall();
        }

        for (int j = 9; j <= 19; j++) {
            cells[4][j].makeWall();
        }

        //

        for (int j = 0; j <= 1; j++) {
            cells[5][j].makeWall();
        }

        for (int j = 5; j <= 7; j++) {
            cells[5][j].makeWall();
        }

        for (int j = 9; j <= 19; j++) {
            cells[5][j].makeWall();
        }

        //

        for (int j = 0; j <= 3; j++) {
            cells[6][j].makeWall();
        }

        for (int j = 5; j <= 7; j++) {
            cells[6][j].makeWall();
        }

        for (int j = 10; j <= 19; j++) {
            cells[6][j].makeWall();
        }

        //

        for (int j = 0; j <= 3; j++) {
            cells[7][j].makeWall();
        }

        for (int j = 5; j <= 7; j++) {
            cells[7][j].makeWall();
        }


        for (int j = 10; j <= 19; j++) {
            cells[7][j].makeWall();
        }

        //

        for (int j = 0; j <= 3; j++) {
            cells[8][j].makeWall();
        }

        for (int j = 5; j <= 6; j++) {
            cells[8][j].makeWall();
        }

        for (int j = 12; j <= 12; j++) {
            cells[8][j].makeWall();
        }

        for (int j = 15; j <= 19; j++) {
            cells[8][j].makeWall();
        }

        //
        for (int j = 0; j <= 3; j++) {
            cells[9][j].makeWall();
        }

        for (int j = 5; j <= 6; j++) {
            cells[9][j].makeWall();
        }

        for (int j = 16; j <= 19; j++) {
            cells[9][j].makeWall();
        }

        //

        for (int j = 0; j <= 3; j++) {
            cells[10][j].makeWall();
        }

        for (int j = 5; j <= 7; j++) {
            cells[10][j].makeWall();
        }

        //
        for (int j = 0; j <= 5; j++) {
            cells[11][j].makeWall();
        }

        for (int j = 7; j <= 19; j++) {
            cells[11][j].makeWall();
        }

        //

        cells[10][17].setImage(R.drawable.exit);

        cells[0][11].setToogle(new Toogle() {
            @Override
            void use() {
                tooglePressed = !tooglePressed;
                if (tooglePressed) {
                    cells[0][11].setImage(R.drawable.iampressed);
                    cells[10][17].setImage(R.drawable.emptycell);
                } else {
                    cells[0][11].setImage(R.drawable.pressme);
                    cells[10][17].setImage(R.drawable.exit);
                }
            }
        });

        cells[0][11].setImage(R.drawable.pressme);

        cells[2][13].setTrap(new Trap() {
            @Override
            void apply() {
                isOver = true;
                isWin = false;

                mp = MediaPlayer.create(activity, R.raw.lolyoudead);
                mp.start();
            }
        });

        cells[2][13].setImage(R.drawable.lev);
    }

    @Override
    boolean checkConditionToExit() {
        return tooglePressed;
    }
}