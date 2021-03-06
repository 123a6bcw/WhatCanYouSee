package ru.ralsei.whatcanyousee.maps.mazegame;

import ru.ralsei.whatcanyousee.gameactivity.GameActivity;
import ru.ralsei.whatcanyousee.logic.MazeGameMap;
import ru.ralsei.whatcanyousee.R;


/**
 * Simple map with no monsters, traps or levers.
 */
public class MazeGameMap_Simple extends MazeGameMap {

    @Override
    protected void setupMetaData() {

    }

    @Override
    protected void setupCells() {

    }

    @Override
    protected void setupTraps() {

    }

    @Override
    protected void setupToogles() {

    }

    @Override
    protected void setupMonsters() {

    }

    public MazeGameMap_Simple(GameActivity activity) {
        super(activity);

        setImageID(R.drawable.maze_game_simple_map);

        setWidth(6);
        setHeight(6);
        setExitCoordinates(new Coordinates(2, 2));
        setInitialCoordinates(new Coordinates(1, 1));
        Cell[][] cells = new Cell[getWidth()][getHeight()];
        setCells(cells);
        for (int i = 0; i < getWidth(); i++) {
            for (int j = 0; j < getHeight(); j++) {
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

        cells[2][2].setDefaultImage(R.drawable.exit);
    }

    @Override
    protected boolean checkConditionToExit() {
        return true;
    }
}
