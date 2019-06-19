package ru.ralsei.whatcanyousee.maps.mazegame;

import ru.ralsei.whatcanyousee.gameactivity.GameActivity;
import ru.ralsei.whatcanyousee.R;
import ru.ralsei.whatcanyousee.logic.MazeGameMap;

public class MazeGameMap_Test extends MazeGameMap {
    private boolean tooglePressed = false;

    @Override
    protected void setupMetaData() {
        setWidth(12);
        setHeight(20);
        setExitCoordinates(new Coordinates(10, 17));
        setInitialCoordinates(new Coordinates(9, 10));
        setImageID(R.drawable.maze_game_test_map);
    }

    @Override
    protected void setupCells() {
        final Cell[][] cells = new Cell[getWidth()][getHeight()];
        setCells(cells);

        for (int i = 0; i < getWidth(); i++) {
            for (int j = 0; j < getHeight(); j++) {
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

        cells[10][17].setDefaultImage(R.drawable.exit);
    }

    @Override
    protected void setupTraps() {
        final Cell[][] cells = getCells();

        cells[0][11].setDefaultImage(R.drawable.pressme);

        cells[2][13].setTrap(new Trap() {
            @Override
            protected void apply() {
                setPlayerWon(false);
                setMessageLost("It's a trap!!!");

                getActivity().getSoundPlayer().playTrack(R.raw.lolyoudead);
            }
        });

        cells[2][13].setDefaultImage(R.drawable.lev);
    }

    @Override
    protected void setupToogles() {
        final Cell[][] cells = getCells();

        cells[0][11].setToogle(new Toogle() {
            @Override
            protected void use() {
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
    }

    public MazeGameMap_Test(GameActivity activity) {
        super(activity);
    }

    @Override
    protected void setupMonsters() {
        class SimpleMonster extends Monster {
            private final int ticksPerMove = 20;
            private int ticksToMove = ticksPerMove;

            private final int ticksPerPlay = 20;
            private int ticksToPlay = ticksPerPlay;

            private SimpleMonster(int initialX, int initialY) {
                this.setImageId(R.drawable.lev);
                setInitialX(initialX);
                setInitialY(initialY);
            }

            @Override
            protected void updateOnTick() {
                ticksToMove = decreaseTick(ticksToMove, ticksPerMove);
                ticksToPlay = decreaseTick(ticksToPlay, ticksPerPlay);

                if (ticksToPlay == 0) {
                    getActivity().getSoundPlayer().playTrackWithVolume(R.raw.scary_monster, 10 - this.getCurrentCell().getDistance());
                }
            }

            @Override
            protected boolean tryToKill() {
                if (getCurrentCell().getDistance() == 0) {
                    setPlayerWon(false);
                    return true;
                }

                setMessageLost("Killed by a monster.");

                return false;
            }

            @Override
            protected boolean readyToMove() {
                return ticksToMove == 0;
            }
        }

        addMonster(new SimpleMonster(9, 7));
        addMonster(new SimpleMonster(10, 8));
    }

    @Override
    protected boolean checkConditionToExit() {
        return tooglePressed;
    }
}