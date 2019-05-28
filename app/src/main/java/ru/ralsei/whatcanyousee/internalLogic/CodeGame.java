package ru.ralsei.whatcanyousee.internalLogic;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import ru.ralsei.whatcanyousee.GameActivity;
import ru.ralsei.whatcanyousee.R;

/**
 * TODO
 */
public class CodeGame {
    /**
     * TODO
     */
    private static final int MAX_CODE_LENGTH = 6;

    /**
     * TODO
     */
    private static final String TAG = "What can you see, Code game:";

    /**
     * TODO
     */
    private GameActivity activity;

    /**
     * TODO
     */
    private CodeGameMap codeGameMap;

    public CodeGame(final GameActivity activity, final CodeGameMap codeGameMap, final CodeGameMap teammateCodeGameMap) {
        this.activity = activity;
        this.codeGameMap = codeGameMap;

        ((ImageView) activity.findViewById(R.id.codeImage)).setImageResource(teammateCodeGameMap.getImageId());

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.button_submitCode: {
                        String code = ((EditText) activity.findViewById(R.id.text_code)).getText().toString();
                        Log.d(TAG, "Submitted code " + code);

                        if (code.length() > MAX_CODE_LENGTH) {
                            onWrongCode();
                            return;
                        }

                        for (int i = 0; i < code.length(); i++) {
                            if (code.charAt(i) < '0' || code.charAt(i) > '9') {
                                onWrongCode();
                                return;
                            }
                        }

                        if (codeGameMap.checkCode(code)) {
                            //TODO GameActivity.SoundPlayer.playTrack(CORRECT);
                            activity.getGameplayHandler().onCodeGameWon();
                        } else {
                            onWrongCode();
                        }

                        break;
                    }

                    case R.id.button_giveUp: {
                        activity.getGameplayHandler().onCodeGameLost();
                        break;
                    }
                }
            }

            /**
             * TODO
             */
            private void onWrongCode() {
                //TODO GameActivity.SoundPlayer.playTrack(NOT CORRECT :( );
            }
        };

        activity.findViewById(R.id.button_submitCode).setOnClickListener(onClickListener);
        activity.findViewById(R.id.button_giveUp).setOnClickListener(onClickListener);
    }
}
