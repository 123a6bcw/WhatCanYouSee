package ru.ralsei.whatcanyousee.maps.mazegame;

import java.util.List;

import ru.ralsei.whatcanyousee.GameActivity.GameActivity;
import ru.ralsei.whatcanyousee.R;
import ru.ralsei.whatcanyousee.internalLogic.MazeGameMap;

public class MazeGameMap_Test2 extends MazeGameMap {
    private boolean leftFirstToogle = false;
    private boolean rightFirstToogle = false;

    private boolean exitToogle = false;

    @Override
    protected void setupMetaData() {
        setxSize(12);
        setySize(16);
        setExitCoordinates(new Coordinates(7, 11));
        setInitialCoordinates(new Coordinates(4, 3));
        setImageID(R.drawable.maze_game_test2_map);
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

        makeVerticalWall(2, 0, 4);
        makeHorizontalWall(0, 4, 5);
        makeVerticalWall(0, 5, 11);
        makeHorizontalWall(0, 6, 11);
        makeHorizontalWall(8, 11, 11);
        makeVerticalWall(11, 5, 11);
        makeHorizontalWall(6, 11, 5);
        makeVerticalWall(9, 0, 5);
        makeHorizontalWall(2, 9, 0);
        makeVerticalWall(6, 12, 15);
        makeVerticalWall(8, 12, 15);
        cells[5][5].makeWall();

        cells[7][3].setDefaultImage(R.drawable.icon);
    }

    @Override
    protected void setupTraps() {
        final Cell[][] cells = getCells();

        cells[4][6].setDefaultImage(R.drawable.kill_cell);
        cells[4][7].setDefaultImage(R.drawable.kill_cell);
        cells[4][8].setDefaultImage(R.drawable.kill_cell);

        cells[6][6].setDefaultImage(R.drawable.kill_cell);
        cells[6][7].setDefaultImage(R.drawable.kill_cell);
        cells[6][8].setDefaultImage(R.drawable.kill_cell);

        Trap trap = new Trap() {
            @Override
            protected void apply() {
                setPlayerWon(false);

                setMessageLost("It's a trap!!!");
                getActivity().getSoundPlayer().playTrack(R.raw.lolyoudead);
            }
        };

        cells[4][6].setTrap(trap);
        cells[4][7].setTrap(trap);
        cells[4][8].setTrap(trap);

        cells[6][6].setTrap(trap);
        cells[6][7].setTrap(trap);
        cells[6][8].setTrap(trap);

        Trap scaryTrap = new Trap() {
            @Override
            protected void apply() {
                getActivity().getSoundPlayer().playTrack(R.raw.screamer);
            }
        };
        cells[5][9].setTrap(scaryTrap);
    }

    @Override
    protected void setupToogles() {
        final Cell[][] cells = getCells();

        cells[2][3].setDefaultImage(R.drawable.pressme2);
        cells[2][3].setToogle(new Toogle() {
            @Override
            protected void use() {
                leftFirstToogle = !leftFirstToogle;
                if (leftFirstToogle) {
                    cells[2][3].setImage(R.drawable.iampressed2);
                } else {
                    cells[2][3].setImage(R.drawable.pressme2);
                }

                if (leftFirstToogle && rightFirstToogle) {
                    cells[5][5].makeNotWall();
                } else {
                    cells[5][5].makeWall();
                }
            }
        });

        cells[9][3].setDefaultImage(R.drawable.pressme2);
        cells[9][3].setToogle(new Toogle() {
            @Override
            protected void use() {
                rightFirstToogle = !rightFirstToogle;
                if (rightFirstToogle) {
                    cells[9][3].setImage(R.drawable.iampressed2);
                } else {
                    cells[9][3].setImage(R.drawable.pressme2);
                }

                if (leftFirstToogle && rightFirstToogle) {
                    cells[5][5].makeNotWall();
                } else {
                    cells[5][5].makeWall();
                }
            }
        });

        cells[11][9].setDefaultImage(R.drawable.pressme);
        cells[11][9].setToogle(new Toogle() {
            @Override
            protected void use() {
                exitToogle = !exitToogle;
                if (exitToogle) {
                    cells[11][9].setImage(R.drawable.iampressed);
                    cells[7][11].setImage(R.drawable.emptycell);
                } else {
                    cells[11][9].setImage(R.drawable.pressme);
                    cells[7][11].setImage(R.drawable.exit);
                }
            }
        });

        cells[4][9].setDefaultImage(R.drawable.pressme3);
        cells[4][9].setToogle(new Toogle() {
            @Override
            protected void use() {

                List<Monster> monsters = getMonsters();
                SimpleMonster monster = (SimpleMonster) monsters.get(0);
                SimpleMonster monster1 = (SimpleMonster) monsters.get(1);

                killMonster(monster);
                killMonster(monster1);
            }

            private void killMonster(SimpleMonster monster) {
                if (monster != null) {
                    if (!monster.dead) {
                        monster.ticksToMove = 10000;
                        monster.ticksToPlay = 10000;
                        monster.dead = true;
                        cells[4][9].setImage(R.drawable.iampressed3);
                    } else {
                        monster.ticksToMove = monster.ticksPerMove;
                        monster.ticksToPlay = monster.ticksPerPlay;
                        monster.dead = false;
                        cells[4][9].setImage(R.drawable.pressme3);
                    }
                }
            }
        });
    }

    public MazeGameMap_Test2(GameActivity activity) {
        super(activity);
    }

    private class SimpleMonster extends Monster {
        private boolean dead = false;

        private final int ticksPerMove = 25;
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
                getActivity().getSoundPlayer().playTrack(R.raw.scary_monster);
            }
        }

        @Override
        protected boolean tryToKill() {
            if (getCurrentCell().getDistance() == 0) {
                setPlayerWon(false);
                return true;
            }

            setMessageLost("Killed by a monster!!");

            return false;
        }

        @Override
        protected boolean readyToMove() {
            return ticksToMove == 0;
        }
    }

    @Override
    protected void setupMonsters() {
        addMonster(new SimpleMonster(6, 1));
        addMonster(new SimpleMonster(8, 1));
    }

    @Override
    protected boolean checkConditionToExit() {
        return exitToogle;
    }
}