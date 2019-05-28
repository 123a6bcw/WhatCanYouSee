package ru.ralsei.whatcanyousee.internalLogic;

import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;

import ru.ralsei.whatcanyousee.GameActivity;
import ru.ralsei.whatcanyousee.R;

/**
 * TODO
 */
public class LeverGame {
    /**
     * TODO
     */
    private GameActivity activity;

    public LeverGame(final GameActivity activity, final LeverGameMap myLeverMap, final LeverGameMap teammateLeverMap) {
        this.activity = activity;

        ((ImageView) activity.findViewById(R.id.leverImage)).setImageResource(myLeverMap.getCurrentState().getImageID());

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.button_giveUp: {
                        activity.getGameplayHandler().onLeverGameLost();
                        break;
                    }

                    default: {
                        String leverName = ((Switch) v).getText().toString();
                        //myLeverMap.applyLever(leverName);
                        activity.getGameplayHandler().sendLeverPressedMessage(leverName);
                    }
                }
            }
        };

        ViewGroup layout = activity.findViewById(R.id.layout_levers);

        for (String lever : teammateLeverMap.getLevers()) {
            Switch mSwitch = new Switch(activity);
            mSwitch.setOnClickListener(onClickListener);
            mSwitch.setText(lever);

            mSwitch.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            layout.addView(mSwitch);
        }

        activity.findViewById(R.id.button_giveUp_lever).setOnClickListener(onClickListener);
    }
}