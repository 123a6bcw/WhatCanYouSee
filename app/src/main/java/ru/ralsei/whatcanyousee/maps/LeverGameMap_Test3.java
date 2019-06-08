package ru.ralsei.whatcanyousee.maps;

import com.google.android.gms.games.Game;

import java.util.ArrayList;
import java.util.List;

import ru.ralsei.whatcanyousee.GameActivity;
import ru.ralsei.whatcanyousee.R;
import ru.ralsei.whatcanyousee.internalLogic.LeverGameMap;

/**
 * Another simple lever game map. To win, other player should press "open the door", to lose --
 * press the "release snakes".
 */
public class LeverGameMap_Test3 extends LeverGameMap {
    private class Test3State extends State {
        private boolean pressed;
        private boolean snakeReleased;

        private Test3State(int imageID, boolean pressed, boolean snakeReleased) {
            super(imageID);
            this.pressed = pressed;
            this.snakeReleased = snakeReleased;
        }

        private Test3State(Test3State state) {
            super(state.getImageID());
            this.pressed = state.pressed;
            this.snakeReleased = state.snakeReleased;
        }

        private Test3State applyLever(String lever) {
            Test3State state = new Test3State(this);

            switch (lever) {
                case "open the door":
                    state.pressed = !state.pressed;
                    break;
                case "do nothing":
                    break;
                case "release snakes!!1":
                    state.snakeReleased = true;
                    break;
            }

            return state;
        }

        private boolean mEquals(Test3State state) {
            return this.pressed == state.pressed && this.snakeReleased == state.snakeReleased;
        }
    }

    public LeverGameMap_Test3(GameActivity activity) {
        super(activity);

        List<State> states = getStates();
        ArrayList<String> levers = new ArrayList<>();

        levers.add("release snakes!!1");
        levers.add("do nothing");
        levers.add("open the door");

        setLevers(levers.toArray(getLevers()));

        states.add(new Test3State(R.drawable.levergame_emptyimage, false, false));
        states.add(new Test3State(R.drawable.levergame_emptyimage, true, false));
        states.add(new Test3State(R.drawable.levergame_test3_state_snake, false, true));

        states.get(1).setWinState(true);
        states.get(2).setLoseState(true);
        setCurrentState(0);
    }

    @Override
    public void applyLever(String leverName) {
        Test3State state = ((Test3State) getCurrentState()).applyLever(leverName);
        for (int i = 0; i < getStates().size(); i++) {
            if (((Test3State) getStates().get(i)).mEquals(state)) {
                setCurrentState(i);
                return;
            }
        }
    }
}