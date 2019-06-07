package ru.ralsei.whatcanyousee.internalLogic;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class representing the lever game map.
 */
public abstract class LeverGameMap {
    /**
     * Id of the current state.
     */
    private int currentStateNumber;

    /**
     * All levers exists for this map (they will be shown on the other player's screen).
     */
    private String[] levers = new String[0];

    /**
     * List of all possible states in the map.
     */
    private ArrayList<State> states = new ArrayList<>();

    /**
     * Class representing the state of the lever game. All states represents the vertexes of a directed
     * graph, with levers representing the edges.
     */
    public abstract class State {
        /**
         * Image to show on this state.
         */
        private int imageID;

        /**
         * True if enters this state meaning winning the game.
         */
        private boolean winState = false;

        /**
         * True if enters this state meaning loosing the game.
         */
        private boolean loseState = false;

        protected State(int imageID) {
            this.imageID = imageID;
        }

        public int getImageID() {
            return imageID;
        }

        public void setImageID(int imageID) {
            this.imageID = imageID;
        }

        public boolean isWinState() {
            return winState;
        }

        public void setWinState(boolean winState) {
            this.winState = winState;
        }

        public boolean isLoseState() {
            return loseState;
        }

        public void setLoseState(boolean loseState) {
            this.loseState = loseState;
        }
    }

    public LeverGameMap() {
    }

    /**
     * Handles the pressed lever (by the other player).
     */
    public abstract void applyLever(String leverName);

    public State getCurrentState() {
        return states.get(currentStateNumber);
    }

    public int getCurrentStateNumber() {
        return currentStateNumber;
    }

    public int getStateNumber(State currentState) {
        for (int i = 0; i < states.size(); i++) {
            if (states.get(i) == currentState) {
                return i;
            }
        }

        return -1;
    }

    protected void setCurrentState(int currentStateNumber) {
        this.currentStateNumber = currentStateNumber;
    }

    protected void addState(State state) {
        states.add(state);
    }

    protected String[] getLevers() {
        return levers;
    }

    protected void setLevers(String[] levers) {
        this.levers = levers;
    }

    protected List<State> getStates() {
        return states;
    }
}
