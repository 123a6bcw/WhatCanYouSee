package ru.ralsei.whatcanyousee.internalLogic;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 */
public abstract class LeverGameMap {
    /**
     * TODO
     */
    private int currentStateNumber;

    /**
     * TODO
     */
    private String[] levers = new String[0];

    /**
     * TODO
     */
    private ArrayList<State> states = new ArrayList<>();

    /**
     * TODO
     */
    public abstract class State {
        private int imageID;
        private boolean winState = false;
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
     * TODO
     */
    public abstract void applyLever(String leverName);

    public State getCurrentState() {
        return states.get(currentStateNumber);
    }

    public int getCurrentStateNumber() {
        return currentStateNumber;
    }

    /**
     * TODO
     */
    public int getStateNumber(State currentState) {
        for (int i = 0; i < states.size(); i++) {
            if (states.get(i) == currentState) {
                return i;
            }
        }

        return -1; //TODO throw exception? (state was not found)
    }

    public void setCurrentState(int currentStateNumber) {
        this.currentStateNumber = currentStateNumber;
    }

    protected void addState(State state) {
        states.add(state);
    }

    public String[] getLevers() {
        return levers;
    }

    public void setLevers(String[] levers) {
        this.levers = levers;
    }

    public List<State> getStates() {
        return states;
    }
}
