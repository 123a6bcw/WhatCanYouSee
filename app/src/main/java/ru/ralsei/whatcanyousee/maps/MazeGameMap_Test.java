package ru.ralsei.whatcanyousee.maps;

import ru.ralsei.whatcanyousee.GameActivity;
import ru.ralsei.whatcanyousee.R;
import ru.ralsei.whatcanyousee.internalLogic.MazeGameMap;

public class MazeGameMap_Test extends MazeGameMap {
    private boolean tooglePressed = false;

    @Override
    protected void setupMetaData() {
        setxSize(12);
        setySize(20);
        setExitCoordinates(new Coordinates(10, 17));
        setInitialCoordinates(new Coordinates(9, 10));
        setImageID(R.drawable.maze_game_test_map);
    }

    @Override
    protected void setupCells() {
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

                GameActivity.SoundPlayer.playTrack(getActivity(), R.raw.lolyoudead);
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

            private SimpleMonster() {
                this.setImageId(R.drawable.lev);
                setInitialX(9);
                setInitialY(7);
            }

            @Override
            protected void updateOnTick() {
                ticksToMove = decreaseTick(ticksToMove, ticksPerMove);
                ticksToPlay = decreaseTick(ticksToPlay, ticksPerPlay);

                if (ticksToPlay == 0) {
                    GameActivity.SoundPlayer.playTrackWithVolume(getActivity(), R.raw.scary_monster, 10 - this.getCurrentCell().getDistance());
                }
            }

            @Override
            protected void tryToKill() {
                if (getCurrentCell().getDistance() == 0) {
                    setPlayerWon(false);
                }
            }

            @Override
            protected boolean readyToMove() {
                return ticksToMove == 0;
            }
        }

        addMonster(new SimpleMonster());
    }

    @Override
    protected boolean checkConditionToExit() {
        return tooglePressed;
    }
}