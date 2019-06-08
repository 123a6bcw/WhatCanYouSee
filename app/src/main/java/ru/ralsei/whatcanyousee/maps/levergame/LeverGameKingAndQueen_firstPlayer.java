package ru.ralsei.whatcanyousee.maps.levergame;

import java.util.ArrayList;
import java.util.List;

import ru.ralsei.whatcanyousee.GameActivity.GameActivity;
import ru.ralsei.whatcanyousee.R;
import ru.ralsei.whatcanyousee.internalLogic.LeverGameMap;

/**
 * Level with story about the king and the queen shared between players.
 */
public class LeverGameKingAndQueen_firstPlayer extends LeverGameMap {
    private int pressed = 0;

    private class KAQ1State extends LeverGameMap.State {
        private KAQ1State(int imageID, boolean greenRotated, boolean redRotated, boolean blueRotated) {
            super(imageID);
        }

        private KAQ1State(KAQ1State state) {
            super(state.getImageID());
        }

        private KAQ1State applyLever(String lever) {
            KAQ1State state = new KAQ1State(this);

            switch (lever) {
            }

            return state;
        }

        private boolean mEquals(KAQ1State state) {
            return false;
        }
    }

    public LeverGameKingAndQueen_firstPlayer(GameActivity activity) {
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
                    LeverGameKingAndQueen_firstPlayer.this.getActivity().getGameplayHandler().onLeverGameLost(true, "You are pressing too much buttons...");
                }
            });
        }

        KAQ1State state = ((KAQ1State) getCurrentState()).applyLever(leverName);
        for (int i = 0; i < getStates().size(); i++) {
            if (((KAQ1State) getStates().get(i)).mEquals(state)) {
                setCurrentState(i);
                return;
            }
        }
    }
}
