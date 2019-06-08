package ru.ralsei.whatcanyousee.maps.levergame;

import java.util.ArrayList;
import java.util.List;

import ru.ralsei.whatcanyousee.GameActivity.GameActivity;
import ru.ralsei.whatcanyousee.R;
import ru.ralsei.whatcanyousee.internalLogic.LeverGameMap;

/**
 * Simple map with 3 valve -- red, blue, green, in order to win (other) player must create the
 * brown color (red+green), in order to lose -- black color (red+blue+green).
 */
public class LeverGameMap_Test1 extends LeverGameMap {
    private class Test1State extends State {
        private boolean greenRotated;
        private boolean redRotated;
        private boolean blueRotated;

        private Test1State(int imageID, boolean greenRotated, boolean redRotated, boolean blueRotated) {
            super(imageID);
            this.greenRotated = greenRotated;
            this.redRotated = redRotated;
            this.blueRotated = blueRotated;
        }

        private Test1State(Test1State state) {
            super(state.getImageID());
            this.greenRotated = state.greenRotated;
            this.redRotated = state.redRotated;
            this.blueRotated = state.blueRotated;
        }

        private Test1State applyLever(String lever) {
            Test1State state = new Test1State(this);

            switch (lever) {
                case "red valve":
                    state.redRotated = !state.redRotated;
                    break;
                case "blue valve":
                    state.blueRotated = !state.blueRotated;
                    break;
                case "green valve":
                    state.greenRotated = !state.greenRotated;
                    break;
            }

            return state;
        }

        private boolean mEquals(Test1State state) {
            return this.greenRotated == state.greenRotated && this.redRotated == state.redRotated && this.blueRotated == state.blueRotated;
        }
    }

    public LeverGameMap_Test1(GameActivity activity) {
        super(activity);

        List<State> states = getStates();
        ArrayList<String> levers = new ArrayList<>();

        levers.add("red valve");
        levers.add("blue valve");
        levers.add("green valve");

        setLevers(levers.toArray(getLevers()));

        states.add(new Test1State(R.drawable.levergame_test1_state_empty, false, false, false));
        states.add(new Test1State(R.drawable.levergame_test1_state_red, false, true, false));
        states.add(new Test1State(R.drawable.levergame_test1_state_blue, false, false, true));
        states.add(new Test1State(R.drawable.levergame_test1_state_green, true, false, false));
        states.add(new Test1State(R.drawable.levergame_test1_state_red_green, true, true, false));
        states.add(new Test1State(R.drawable.levergame_test1_state_red_blue, false, true, true));
        states.add(new Test1State(R.drawable.levergame_test1_state_green_blue, true, false, true));
        states.add(new Test1State(R.drawable.levergame_test1_state_red_green_blue, true, true, true));

        states.get(7).setLoseState(true);
        states.get(4).setWinState(true);
        setCurrentState(0);
    }

    @Override
    public void applyLever(String leverName) {
        Test1State state = ((Test1State) getCurrentState()).applyLever(leverName);
        for (int i = 0; i < getStates().size(); i++) {
            if (((Test1State) getStates().get(i)).mEquals(state)) {
                setCurrentState(i);
                return;
            }
        }
    }
}