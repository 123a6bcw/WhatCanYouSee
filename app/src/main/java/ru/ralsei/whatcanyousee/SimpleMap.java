package ru.ralsei.whatcanyousee;

import android.app.Activity;

public class SimpleMap extends MazeMap {

    public SimpleMap(Activity activity) {
        super(activity);

        setxSize(6);
        setySize(6);
        setExitCoordinates(new Coordinates(2, 2));
        setInitialCoordinates(new Coordinates(1, 1));
        Cell[][] cells = new Cell[getxSize()][getySize()];
        setCells(cells);
        for (int i = 0; i < getxSize(); i++) {
            for (int j = 0; j < getySize(); j++) {
                cells[i][j] = new Cell();
            }
        }

        for (int i = 0; i < 6; i++) {
            cells[i][0].makeWall();
            cells[i][5].makeWall();
        }

        for (int j = 0; j < 6; j++) {
            cells[0][j].makeWall();
            cells[5][j].makeWall();
        }

    }

    @Override
    boolean checkConditionToExit() {
        return false;
    }
}
