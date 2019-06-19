package ru.ralsei.whatcanyousee.logic;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;
import java.util.List;

import ru.ralsei.whatcanyousee.gameactivity.GameActivity;
import ru.ralsei.whatcanyousee.R;

/**
 * Class that runs the lever game.
 */
public class LeverGame {
    /**
     * Activity game was called from.
     */
    private GameActivity activity;

    /**
     * Levers that user may press - they will affect the state on the other player's screen.
     */
    private List<Switch> switches = new ArrayList<>();

    private final LeverGameMap leverGameMap;

    /**
     * Initializes the content of the lever game.
     */
    public LeverGame(final GameActivity activity, final LeverGameMap leverGameMap, final LeverGameMap teammateLeverMap) {
        this.activity = activity;
        this.leverGameMap = leverGameMap;

        ((ImageView) activity.findViewById(R.id.leverImage)).setImageResource(leverGameMap.getCurrentState().getImageID());

        ViewGroup layout = activity.findViewById(R.id.layout_levers);

        for (String lever : teammateLeverMap.getLevers()) {
            Switch mSwitch = new Switch(activity);
            switches.add(mSwitch);
            mSwitch.setText(lever);

            mSwitch.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            layout.addView(mSwitch);
        }
    }

    public void setupListeners() {
        activity.findViewById(R.id.button_giveUp_lever).setOnClickListener(onClickListener);
        for (Switch mSwitch : switches) {
            mSwitch.setOnClickListener(onClickListener);
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.button_giveUp_lever: {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            activity.getGameplayHandler().onLeverGameLost(false, "You gave up.");
                        }
                    });
                    break;
                }

                default: {
                    activity.getSoundPlayer().playTrack(R.raw.ok);

                    String leverName = ((Switch) v).getText().toString();

                    if (!leverGameMap.isPressSelf()) {
                        activity.getGameplayHandler().sendLeverPressedMessage(leverName);
                    } else {
                        leverGameMap.applyLever(leverName);
                    }
                }
            }
        }
    };

}