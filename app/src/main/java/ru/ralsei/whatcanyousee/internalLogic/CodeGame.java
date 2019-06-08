package ru.ralsei.whatcanyousee.internalLogic;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import ru.ralsei.whatcanyousee.GameActivity.GameActivity;
import ru.ralsei.whatcanyousee.R;

/**
 * The code game running class.
 */
public class CodeGame {
    /**
     * Maximum length of the input code.
     */
    private static final int MAX_CODE_LENGTH = 6;

    /**
     * Tag to use in logs.
     */
    private static final String TAG = "What can you see, Code game:";

    /**
     * Activity that owns this game.
     */
    private GameActivity activity;

    /**
     * Code game map of this player.
     */
    private CodeGameMap codeGameMap;

    /**
     * Initializes the content of the code game.
     */
    public CodeGame(final GameActivity activity, final CodeGameMap codeGameMap, final CodeGameMap teammateCodeGameMap) {
        this.activity = activity;
        this.codeGameMap = codeGameMap;

        ((ImageView) activity.findViewById(R.id.codeImage)).setImageResource(teammateCodeGameMap.getImageId());
    }

    public void setupListeners() {
        activity.findViewById(R.id.button_submitCode).setOnClickListener(onClickListener);
        activity.findViewById(R.id.button_giveUp).setOnClickListener(onClickListener);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
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
                        activity.getSoundPlayer().playTrack(R.raw.ok);
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
         * Handling the wrong input code.
         */
        private void onWrongCode() {
            activity.getSoundPlayer().playTrack(R.raw.not_ok);
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.getGameStatistic().incrementCodeGameMistakeTaken();
                }
            });
        }
    };
}