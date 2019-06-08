package ru.ralsei.whatcanyousee.maps.levergame;

import java.util.ArrayList;
import java.util.List;

import ru.ralsei.whatcanyousee.GameActivity.GameActivity;
import ru.ralsei.whatcanyousee.R;
import ru.ralsei.whatcanyousee.internalLogic.LeverGameMap;

/**
 * Level with story about the king and the queen shared between players.
 */
public class LeverGameKingAndQueen_secondPlayer extends LeverGameMap {
    private int pressed;

    private class KAQ2State extends LeverGameMap.State {
        private KAQ2State(int imageID, boolean greenRotated, boolean redRotated, boolean blueRotated) {
            super(imageID);
        }

        private KAQ2State(KAQ2State state) {
            super(state.getImageID());
        }

        private KAQ2State applyLever(String lever) {
            KAQ2State state = new KAQ2State(this);

            switch (lever) {
            }

            return state;
        }

        private boolean mEquals(KAQ2State state) {
            return false;
        }
    }

    public LeverGameKingAndQueen_secondPlayer(GameActivity activity) {
        super(activity);

        List<LeverGameMap.State> states = getStates();
        ArrayList<String> levers = new ArrayList<>();

        levers.add("King");
        levers.add("Queen");
        levers.add("Son");
        levers.add("Stranger");
        levers.add("Sword");

        setLevers(levers.toArray(getLevers()));

        setCurrentState(0);
    }

    @Override
    public void applyLever(String leverName) {
        pressed++;

        if (pressed > 20) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getActivity().getGameplayHandler().onLeverGameLost(true, "You are pressing too much buttons...");
                }
            });
        }

        KAQ2State state = ((KAQ2State) getCurrentState()).applyLever(leverName);
        for (int i = 0; i < getStates().size(); i++) {
            if (((KAQ2State) getStates().get(i)).mEquals(state)) {
                setCurrentState(i);
                return;
            }
        }
    }
}
