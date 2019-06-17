package ru.ralsei.whatcanyousee.gameactivity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesCallbackStatusCodes;
import com.google.android.gms.games.GamesClientStatusCodes;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import ru.ralsei.whatcanyousee.R;

/**
 * Main (and the only) app activity.
 */
public class GameActivity extends AppCompatActivity implements View.OnClickListener {
    /**
     * Tag to use in logs.
     */
    private final static String TAG = "What can you see?";

    /**
     * Request codes for the UIs showed with startActivityForResult.
     */
    private final static int RC_SELECT_PLAYERS = 10000;
    private final static int RC_INVITATION_INBOX = 10001;
    final static int RC_WAITING_ROOM = 10002;

    /**
     * Request code to invoke sign-in UI.
     */
    static final int RC_SIGN_IN = 9001;

    /**
     * Request code to invoke activities with no special result handling.
     */
    static final int RC_UNUSED = 5001;

    /**
     * Request code to ask permission to record player's voice.
     */
    static final int RC_REQUEST_VOICE_RECORD_PERMISSION = 8001;

    /**
     * Number of players in game. Always 2.
     */
    static final int NUMBER_OF_PLAYERS = 2;

    /**
     * onCreate with debugMode = true runs single player instance of some games
     * (debugMode and additional argument passes with extraData), without logging into google account.
     */
    //TODO fails.
    private boolean debugMode = false;

    /**
     * Handles micro connection between players.
     */
    @NonNull
    private AudioConnector audioConnector = new AudioConnector(this);

    /**
     * Handles google play features (creation etc).
     */
    @NonNull
    private GooglePlayHandler googlePlayHandler = new GooglePlayHandler(this);

    /**
     * Handles game-messaging between players.
     */
    @NonNull
    private InternetConnector internetConnector = new InternetConnector(this);

    /**
     * Handles UI changes (switching between screen's etc).
     */
    @NonNull
    private UIHandler uiHandler = new UIHandler(this);

    UIHandler getUIHandler() {
        return uiHandler;
    }

    /**
     * Handles gameplay stage of the game (switching between levels etc).
     */
    @NonNull
    private GameplayHandler gameplayHandler = new GameplayHandler(this);

    /**
     * Class for storing statistic and achievements in the game.
     */
    @NonNull
    private GameStatistic gameStatistic = new GameStatistic();

    /**
     * Class for playing several sounds in parallel.
     */
    @NonNull
    private SoundPlayer soundPlayer = new SoundPlayer(this);

    @NonNull
    public SoundPlayer getSoundPlayer() {
        return soundPlayer;
    }

    @NonNull
    GooglePlayHandler getGooglePlayHandler() {
        return googlePlayHandler;
    }

    @NonNull
    AudioConnector getAudioConnector() {
        return audioConnector;
    }

    @NonNull
    InternetConnector getInternetConnector() {
        return internetConnector;
    }

    void setState(State state) {
        this.state = state;
    }

    enum State {
        MAIN_MENU, MAZE_GAME, CODE_GAME, LEVER_GAME
    }

    private State state;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        state = State.MAIN_MENU;

        Intent intent = getIntent();
        if (intent.hasExtra("debug")) {
            debugMode = true;
            String debugGame = intent.getStringExtra("debug");
            if (debugGame != null) {
                //noinspection SwitchStatementWithTooFewBranches
                switch (debugGame) {
                    case "maze":
                        gameplayHandler.startMazeGame();
                        break;
                    default:
                        handleException(new IllegalArgumentException(), "Wrong debug name game passed to intent");
                }
            }

            return;
        }

        setContentView(R.layout.activity_game);

        googlePlayHandler.setGoogleSignInClient(GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN));

        setupListeners();

        for (int screen : uiHandler.SCREENS) {
            findViewById(screen).setVisibility(View.GONE);
        }

        uiHandler.switchToMainScreen();
    }

    void setupListeners() {
        findViewById(R.id.button_accept_popup_invitation).setOnClickListener(this);
        findViewById(R.id.button_invite_friend).setOnClickListener(this);
        findViewById(R.id.button_accept_invitation).setOnClickListener(this);
        findViewById(R.id.button_sign_in).setOnClickListener(this);
        findViewById(R.id.button_sign_out).setOnClickListener(this);
        findViewById(R.id.button_show_achievements).setOnClickListener(this);
        findViewById(R.id.button_show_leaderboards).setOnClickListener(this);
        findViewById(R.id.button_decline_popup_invitation).setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");

        switch (state) {
            case MAIN_MENU:
                this.setupListeners();
                break;
            case CODE_GAME:
                if (gameplayHandler.getCodeGame() != null) {
                    gameplayHandler.getCodeGame().setupListeners();
                }
                break;
            case MAZE_GAME:
                if (gameplayHandler.getMaze() != null) {
                    gameplayHandler.getMaze().setupListeners();
                }
                break;
            case LEVER_GAME:
                if (gameplayHandler.getLeverGame() != null) {
                    gameplayHandler.getLeverGame().setupListeners();
                }
                break;
        }

        if (!debugMode) {
            googlePlayHandler.signInSilently();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Unregister listeners.  They will be re-registered via onResume->signInSilently->onConnected.
        if (googlePlayHandler.getInvitationsClient() != null) {
            googlePlayHandler.getInvitationsClient().unregisterInvitationCallback(googlePlayHandler.getInvitationCallback());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_sign_in:
                Log.d(TAG, "Sign-in button clicked");
                googlePlayHandler.startSignInIntent();
                break;
            case R.id.button_sign_out:
                Log.d(TAG, "Sign-out button clicked");
                googlePlayHandler.signOut();
                uiHandler.switchToScreen(R.id.screen_sign_in);
                break;
            case R.id.button_invite_friend:
                uiHandler.switchToScreen(R.id.screen_wait);

                googlePlayHandler.getRealTimeMultiplayerClient().getSelectOpponentsIntent(1, 1, false).addOnSuccessListener(
                        new OnSuccessListener<Intent>() {
                            @Override
                            public void onSuccess(Intent intent) {
                                startActivityForResult(intent, RC_SELECT_PLAYERS);
                            }
                        }
                ).addOnFailureListener(createFailureListener("There was a problem selecting opponents."));
                break;
            case R.id.button_accept_invitation:
                uiHandler.switchToScreen(R.id.screen_wait);

                googlePlayHandler.getInvitationsClient().getInvitationInboxIntent().addOnSuccessListener(
                        new OnSuccessListener<Intent>() {
                            @Override
                            public void onSuccess(Intent intent) {
                                startActivityForResult(intent, RC_INVITATION_INBOX);
                            }
                        }
                ).addOnFailureListener(createFailureListener("There was a problem getting the inbox."));
                break;
            case R.id.button_accept_popup_invitation:
                googlePlayHandler.acceptInviteToRoom(googlePlayHandler.getIncomingInvitationId());
                googlePlayHandler.setIncomingInvitationId(null);
                break;
            case R.id.button_decline_popup_invitation:
                googlePlayHandler.setIncomingInvitationId(null);
                uiHandler.switchToMainScreen();
                break;
            case R.id.button_show_achievements:
                googlePlayHandler.onShowAchievementsRequested();
                break;
            case R.id.button_show_leaderboards:
                googlePlayHandler.onShowLeaderboardsRequested();
                break;
        }
    }

    /**
     * Since a lot of the operations use tasks, we can use a common handler for whenever one fails.
     *
     * @param exception The exception to evaluate.  Will try to display a more descriptive reason for the exception.
     * @param details   Will display alongside the exception if you wish to provide more details for why the exception
     *                  happened.
     */
    void handleException(@Nullable Exception exception, @Nullable String details) {
        int status = 0;

        if (exception instanceof ApiException) {
            ApiException apiException = (ApiException) exception;
            status = apiException.getStatusCode();
        }

        String errorString = null;
        switch (status) {
            case GamesCallbackStatusCodes.OK:
                break;
            case GamesClientStatusCodes.MULTIPLAYER_ERROR_NOT_TRUSTED_TESTER:
                errorString = getString(R.string.status_multiplayer_error_not_trusted_tester);
                break;
            case GamesClientStatusCodes.MATCH_ERROR_ALREADY_REMATCHED:
                errorString = getString(R.string.match_error_already_rematched);
                break;
            case GamesClientStatusCodes.NETWORK_ERROR_OPERATION_FAILED:
                errorString = getString(R.string.network_error_operation_failed);
                break;
            case GamesClientStatusCodes.INTERNAL_ERROR:
                errorString = getString(R.string.internal_error);
                break;
            case GamesClientStatusCodes.MATCH_ERROR_INACTIVE_MATCH:
                errorString = getString(R.string.match_error_inactive_match);
                break;
            case GamesClientStatusCodes.MATCH_ERROR_LOCALLY_MODIFIED:
                errorString = getString(R.string.match_error_locally_modified);
                break;
            default:
                errorString = getString(R.string.unexpected_status, GamesClientStatusCodes.getStatusCodeString(status));
                break;
        }

        if (errorString == null) {
            return;
        }

        String message = getString(R.string.status_exception_error, details, status, exception);

        new AlertDialog.Builder(GameActivity.this)
                .setTitle("Error")
                .setMessage(message + "\n" + errorString)
                .setNeutralButton(android.R.string.ok, null)
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case RC_SIGN_IN:
                Task<GoogleSignInAccount> task =
                        GoogleSignIn.getSignedInAccountFromIntent(intent);
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    googlePlayHandler.onConnected(account);
                } catch (ApiException apiException) {
                    String message = "Seems like you are having problems connecting to google play. " +
                            "Please check your or your friend's internet connection. Game won't be saved :(";

                    googlePlayHandler.onDisconnected();

                    new AlertDialog.Builder(this)
                            .setMessage(message)
                            .setNeutralButton(android.R.string.ok, null)
                            .show();
                }
                break;
            case RC_SELECT_PLAYERS:
                // got the result from the "select players" UI -- ready to create the room
                googlePlayHandler.handleSelectPlayersResult(resultCode, intent);
                break;
            case RC_INVITATION_INBOX:
                // got the result from the "select invitation" UI. ready to accept the selected invitation:
                googlePlayHandler.handleInvitationInboxResult(resultCode, intent);
                break;
            case RC_WAITING_ROOM:
                // got the result from the "waiting room" UI.
                if (resultCode == Activity.RESULT_OK) {
                    // ready to start playing
                    Log.d(TAG, "Starting game (waiting room returned OK).");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            uiHandler.askPermission();
                        }
                    });
                } else if (resultCode == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
                    // player indicated that they want to leave the room
                    googlePlayHandler.leaveRoom();
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    googlePlayHandler.leaveRoom();
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public void onStop() {
        Log.d(TAG, "**** got onStop");

        if (debugMode) {
            super.onStop();
            return;
        }

        googlePlayHandler.leaveRoom();
        clearAllResources();
        uiHandler.stopKeepingScreenOn();
        uiHandler.switchToMainScreen();

        super.onStop();
    }

    @Override
    public void onDestroy() {
        googlePlayHandler.leaveRoom();
        clearAllResources();

        super.onDestroy();
    }

    @NonNull
    OnFailureListener createFailureListener(final String string) {
        return new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                handleException(e, string);
            }
        };
    }

    /**
     * Prepare internet and voice connection (receiving and sending). After that, starts the gameplay phase of the game.
     */
    void prepareConnection() {
        audioConnector.prepareBroadcastAudio();
        audioConnector.prepareReceiveAudio();
        internetConnector.sendReadyMessage();

        internetConnector.setPrepared();

        if (internetConnector.getOtherPlayerIsReady()) {
            gameplayHandler.startGame();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RC_REQUEST_VOICE_RECORD_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    prepareConnection();
                    Log.d(TAG, "permission granted");
                } else {
                    Toast.makeText (this, "I'm sorry, but without voice this game does not make sense. Leaving room." , Toast .LENGTH_LONG).show();
                    Log.d(TAG, "Voice permission wasn't granted");
                    googlePlayHandler.leaveRoom();
                }

            }
        }
    }

    /**
     * Clearing all used resources after game stops (active threads etc) and reset all
     * gameplay variables.
     */
    void clearAllResources() {
        gameplayHandler.clearResources();
        audioConnector.clearResources();
        internetConnector.clearResources();
        soundPlayer.clearResources();
        gameStatistic.clear();
    }

    /**
     * Return class for interacting with the gameplay stage of the game.
     */
    @NonNull
    public GameplayHandler getGameplayHandler() {
        return gameplayHandler;
    }

    @NonNull
    public GameStatistic getGameStatistic() {
        return gameStatistic;
    }
}