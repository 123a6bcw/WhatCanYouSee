package ru.ralsei.whatcanyousee.maps;

import java.util.ArrayList;
import java.util.List;

import ru.ralsei.whatcanyousee.R;
import ru.ralsei.whatcanyousee.internalLogic.LeverGameMap;

/**
 * TODO
 */
public class LeverGameMap_Test2 extends LeverGameMap {
    /**
     * TODO
     */
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

        private boolean equals(Test2State state) {
            return this.pressed == state.pressed;
        }
    }

    public LeverGameMap_Test2() {
        super();

        List<State> states = getStates();
        ArrayList<String> levers = new ArrayList<>();

        levers.add("press me");

        setLevers(levers.toArray(getLevers()));

        states.add(new Test2State(R.drawable.levergame_test2_state_empty, false));
        states.add(new Test2State(R.drawable.levergame_test2_state_ok, true));

        states.get(1).setWinState(true);
        setCurrentState(0);
    }

    @Override
    public void applyLever(String leverName) {
        Test2State state = ((Test2State) getCurrentState()).applyLever(leverName);
        for (int i = 0; i < getStates().size(); i++) {
            if (getStates().get(i).equals(state)) {
                setCurrentState(i);
                return;
            }
        }
    }
}