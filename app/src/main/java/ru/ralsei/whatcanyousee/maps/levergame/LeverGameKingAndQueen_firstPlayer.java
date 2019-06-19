package ru.ralsei.whatcanyousee.maps.levergame;

import java.util.ArrayList;
import java.util.List;

import ru.ralsei.whatcanyousee.gameactivity.GameActivity;
import ru.ralsei.whatcanyousee.R;
import ru.ralsei.whatcanyousee.logic.LeverGameMap;

/**
 * Level with story about the king and the queen shared between players.
 */
public class LeverGameKingAndQueen_firstPlayer extends LeverGameMap {
    private int pressed = 0;

    private class KAQ1State extends LeverGameMap.State {
        private int number;

        private boolean king = false;
        private boolean queen = false;
        private boolean son = false;
        private boolean stranger = false;
        private boolean sword = false;

        private KAQ1State(int imageID, int number) {
            super(imageID);
            this.number = number;
        }

        private KAQ1State(KAQ1State state) {
            super(state.getImageID());
            this.number = state.number;

            this.king = state.king;
            this.queen = state.queen;
            this.son = state.son;
            this.stranger = state.stranger;
            this.sword = state.sword;

        }

        private boolean nextStageFullfield() {
            switch (number) {
                case 1:
                    return king && queen && !son && !stranger && !sword;
                case 2:
                    return king && queen && son && !sword && !stranger;
                case 3:
                    return stranger && !king && !queen && !sword && !son;
                case 4:
                    return stranger && son && sword && !queen && !king;
            }

            return false;
        }

        private void applyLever(String lever) {
            switch (lever) {
                case "King": {
                    king = !king;
                    break;
                }
                case "Queen": {
                    queen = !queen;
                    break;
                }
                case "Son": {
                    son = !son;
                    break;
                }
                case "Stranger": {
                    stranger = !stranger;
                    break;
                }
                case "Sword": {
                    sword = !sword;
                    break;
                }
            }

            if (nextStageFullfield()) {
                ((KAQ1State) getState(number)).king = this.king;
                ((KAQ1State) getState(number)).queen = this.queen;
                ((KAQ1State) getState(number)).son = this.son;
                ((KAQ1State) getState(number)).stranger = this.stranger;
                ((KAQ1State) getState(number)).sword = this.sword;

                setCurrentState(number);
            }
        }

        private boolean mEquals(KAQ1State state) {
            return number == state.number;
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

        states.add(new KAQ1State(R.drawable.levergame_king_and_queen_firstplayer_stage1, 1));
        states.add(new KAQ1State(R.drawable.levergame_king_and_queen_firstplayer_stage2, 2));
        states.add(new KAQ1State(R.drawable.levergame_king_and_queen_firstplayer_stage3, 3));
        states.add(new KAQ1State(R.drawable.levergame_king_and_queen_firstplayer_stage4, 4));
        states.add(new KAQ1State(R.drawable.levergame_king_and_queen_firstplayer_stage5, 5));

        setCurrentState(0);
        states.get(4).setWinState(true);
        setPressSelf(true);
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

        ((KAQ1State) getCurrentState()).applyLever(leverName);
    }
}
