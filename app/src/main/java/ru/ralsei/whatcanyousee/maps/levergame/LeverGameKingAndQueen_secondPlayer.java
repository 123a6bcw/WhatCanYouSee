package ru.ralsei.whatcanyousee.maps.levergame;

import java.util.ArrayList;
import java.util.List;

import ru.ralsei.whatcanyousee.gameactivity.GameActivity;
import ru.ralsei.whatcanyousee.R;
import ru.ralsei.whatcanyousee.logic.LeverGameMap;

/**
 * Level with story about the king and the queen shared between players.
 */
public class LeverGameKingAndQueen_secondPlayer extends LeverGameMap {
    private int pressed = 0;

    private class KAQ2State extends LeverGameMap.State {
        private int number;

        private boolean king = false;
        private boolean queen = false;
        private boolean son = false;
        private boolean stranger = false;
        private boolean sword = false;

        private KAQ2State(int imageID, int number) {
            super(imageID);
            this.number = number;
        }

        private KAQ2State(KAQ2State state) {
            super(state.getImageID());
            this.number = state.number;
        }

        private boolean nextStageFullfield() {
            switch (number) {
                case 1:
                    return king && !queen && !son && !stranger && !sword;
                case 2:
                    return son && !king && !queen && !stranger && !sword;
                case 3:
                    return stranger && son && !king && !queen && !sword;
                case 4:
                    return king && queen && !son && !sword && !stranger;
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
                setCurrentState(number);

                ((KAQ2State) getState(number)).king = this.king;
                ((KAQ2State) getState(number)).queen = this.queen;
                ((KAQ2State) getState(number)).son = this.son;
                ((KAQ2State) getState(number)).stranger = this.stranger;
                ((KAQ2State) getState(number)).sword = this.sword;
            }
        }

        private boolean mEquals(KAQ2State state) {
            return number == state.number;
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

        states.add(new KAQ2State(R.drawable.levergame_king_and_queen_secondplayer_stage1, 1));
        states.add(new KAQ2State(R.drawable.levergame_king_and_queen_secondplayer_stage2, 2));
        states.add(new KAQ2State(R.drawable.levergame_king_and_queen_secondplayer_stage3, 3));
        states.add(new KAQ2State(R.drawable.levergame_king_and_queen_secondplayer_stage4, 4));
        states.add(new KAQ2State(R.drawable.levergame_king_and_queen_secondplayer_stage5, 5));

        setCurrentState(0);
        states.get(4).setWinState(true);
        setPressSelf();
    }

    @Override
    public void applyLever(String leverName) {
        pressed++;

        if (pressed > 20) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LeverGameKingAndQueen_secondPlayer.this.getActivity().getGameplayHandler().onLeverGameLost(true, "Your friend pressed too much levers...");
                }
            });
        }

        ((KAQ2State) getCurrentState()).applyLever(leverName);
    }
}
