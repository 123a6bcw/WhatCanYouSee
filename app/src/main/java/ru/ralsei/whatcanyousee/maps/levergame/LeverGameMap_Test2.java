package ru.ralsei.whatcanyousee.maps.levergame;

import java.util.ArrayList;

import ru.ralsei.whatcanyousee.gameactivity.GameActivity;
import ru.ralsei.whatcanyousee.R;
import ru.ralsei.whatcanyousee.logic.LeverGameMap;

/**
 * Simplest lever game map. In order to win, other play should press "press me".
 */
public class LeverGameMap_Test2 extends LeverGameMap {
    private class Test2State extends State {
        private boolean pressed;

        private Test2State(int imageID, boolean pressed) {
            super(imageID);
            this.pressed = pressed;
        }

        private Test2State(Test2State state) {
            super(state.getImageID());
            this.pressed = state.pressed;
        }

        private Test2State applyLever(String lever) {
            Test2State state = new Test2State(this);

            switch (lever) {
                case "press me":
                    state.pressed = !state.pressed;
                    break;
            }

            return state;
        }

        private boolean mEquals(Test2State state) {
            return this.pressed == state.pressed;
        }
    }

    public LeverGameMap_Test2(GameActivity activity) {
        super(activity);

        ArrayList<String> levers = new ArrayList<>();

        levers.add("press me");

        setLevers(levers.toArray(getLevers()));

        addState(new Test2State(R.drawable.levergame_test2_state_empty, false));
        addState(new Test2State(R.drawable.levergame_test2_state_ok, true));

        getState(1).setWinState(true);
        setCurrentState(0);
    }

    @Override
    public void applyLever(String leverName) {
        Test2State state = ((Test2State) getCurrentState()).applyLever(leverName);
        for (int i = 0; i < getStates().size(); i++) {
            if (((Test2State) getStates().get(i)).mEquals(state)) {
                setCurrentState(i);
                return;
            }
        }
    }
}