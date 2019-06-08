package ru.ralsei.whatcanyousee;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesCallbackStatusCodes;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.GamesClientStatusCodes;
import com.google.android.gms.games.InvitationsClient;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.PlayersClient;
import com.google.android.gms.games.RealTimeMultiplayerClient;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.InvitationCallback;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.OnRealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateCallback;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Objects;

import ru.ralsei.whatcanyousee.internalLogic.CodeGame;
import ru.ralsei.whatcanyousee.internalLogic.CodeGameMap;
import ru.ralsei.whatcanyousee.internalLogic.LeverGame;
import ru.ralsei.whatcanyousee.internalLogic.LeverGameMap;
import ru.ralsei.whatcanyousee.internalLogic.MazeGame;
import ru.ralsei.whatcanyousee.internalLogic.MazeGameMap;
import ru.ralsei.whatcanyousee.maps.codegame.CodeGameMap_Test1;
import ru.ralsei.whatcanyousee.maps.codegame.CodeGameMap_Test2;
import ru.ralsei.whatcanyousee.maps.codegame.CodeGameMap_Test3;
import ru.ralsei.whatcanyousee.maps.codegame.CodeGameMap_Test4;
import ru.ralsei.whatcanyousee.maps.levergame.LeverGameMap_Test1;
import ru.ralsei.whatcanyousee.maps.levergame.LeverGameMap_Test2;
import ru.ralsei.whatcanyousee.maps.levergame.LeverGameMap_Test3;
import ru.ralsei.whatcanyousee.maps.mazegame.MazeGameMap_Test;
import ru.ralsei.whatcanyousee.maps.mazegame.MazeGameMap_Test2;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main (and the only) app activity.
 */
public class GameActivity extends AppCompatActivity implements View.OnClickListener {
    /**
     * Tag to use in logs.
     */
    private final static String TAG = "What can you see?";

    /**
     * Random used to choose exact maps for players.
     */
    private final Random random = new Random();

    /**
     * Request codes for the UIs showed with startActivityForResult.
     */
    private final static int RC_SELECT_PLAYERS = 10000;
    private final static int RC_INVITATION_INBOX = 10001;
    private final static int RC_WAITING_ROOM = 10002;

    /**
     * Request code to invoke sign-in UI.
     */
    private static final int RC_SIGN_IN = 9001;

    /**
     * Request code to invoke activities with no special result handling.
     */
    private static final int RC_UNUSED = 5001;

    /**
     * Request code to ask permission to record player's voice.
     */
    private static final int RC_REQUEST_VOICE_RECORD_PERMISSION = 8001;

    /**
     * Number of players in game. Always 2.
     */
    private static final int NUMBER_OF_PLAYERS = 2;

    /**
     * Stores settings of a created game (maps etc).
     */
    @Nullable
    private GameSettings gameSettings = null;

    /**
     * onCreate with debugMode = true runs single player instance of some games
     * (debugMode and additional argument passes with extraData), without logging into google account.
     */
    //TODO fails.
    private boolean debugMode = false;

    /**
     * Handles micro connection between players.
     */
    //TODO test request micro
    @NonNull
    private AudioConnector audioConnector = new AudioConnector();

    /**
     * Handles google play features (creation etc).
     */
    @NonNull
    private GooglePlayHandler googlePlayHandler = new GooglePlayHandler();

    /**
     * Handles game-messaging between players.
     */
    @NonNull
    private InternetConnector internetConnector = new InternetConnector();

    /**
     * Handles UI changes (switching between screen's etc).
     */
    @NonNull
    private UIHandler uiHandler = new UIHandler();

    /**
     * Handles gameplay stage of the game (switching between levels etc).
     */
    @NonNull
    private GameplayHandler gameplayHandler = new GameplayHandler();

    /**
     * Class for storing statistic and achievements in the game.
     */
    @NonNull
    private GameStatistic gameStatistic = new GameStatistic();

    /**
     * Class for playing several sounds in parallel.
     */
    @NonNull
    private SoundPlayer soundPlayer = new SoundPlayer();

    @NonNull
    public SoundPlayer getSoundPlayer() {
        return soundPlayer;
    }

    private enum State {
        MAIN_MENU, MAZE_GAME, CODE_GAME, LEVER_GAME;
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

        googlePlayHandler.mGoogleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);

        setupListeners();

        for (int screen : uiHandler.SCREENS) {
            findViewById(screen).setVisibility(View.GONE);
        }

        uiHandler.switchToMainScreen();
    }

    private void setupListeners() {
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
                if (gameplayHandler.codeGame != null) {
                    gameplayHandler.codeGame.setupListeners();
                }
                break;
            case MAZE_GAME:
                if (gameplayHandler.maze != null) {
                    gameplayHandler.maze.setupListeners();
                }
                break;
            case LEVER_GAME:
                if (gameplayHandler.leverGame != null) {
                    gameplayHandler.leverGame.setupListeners();
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
        if (googlePlayHandler.mInvitationsClient != null) {
            googlePlayHandler.mInvitationsClient.unregisterInvitationCallback(googlePlayHandler.mInvitationCallback);
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

                googlePlayHandler.mRealTimeMultiplayerClient.getSelectOpponentsIntent(1, 1, false).addOnSuccessListener(
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

                googlePlayHandler.mInvitationsClient.getInvitationInboxIntent().addOnSuccessListener(
                        new OnSuccessListener<Intent>() {
                            @Override
                            public void onSuccess(Intent intent) {
                                startActivityForResult(intent, RC_INVITATION_INBOX);
                            }
                        }
                ).addOnFailureListener(createFailureListener("There was a problem getting the inbox."));
                break;
            case R.id.button_accept_popup_invitation:
                googlePlayHandler.acceptInviteToRoom(googlePlayHandler.mIncomingInvitationId);
                googlePlayHandler.mIncomingInvitationId = null;
                break;
            case R.id.button_decline_popup_invitation:
                googlePlayHandler.mIncomingInvitationId = null;
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
    private void handleException(@Nullable Exception exception, @Nullable String details) {
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
        if (requestCode == RC_SIGN_IN) {
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
        } else if (requestCode == RC_SELECT_PLAYERS) {
            // got the result from the "select players" UI -- ready to create the room
            googlePlayHandler.handleSelectPlayersResult(resultCode, intent);
        } else if (requestCode == RC_INVITATION_INBOX) {
            // got the result from the "select invitation" UI. ready to accept the selected invitation:
            googlePlayHandler.handleInvitationInboxResult(resultCode, intent);
        } else if (requestCode == RC_WAITING_ROOM) {
            // got the result from the "waiting room" UI.
            if (resultCode == Activity.RESULT_OK) {
                // ready to start playing
                Log.d(TAG, "Starting game (waiting room returned OK).");
                uiHandler.askPermission();
            } else if (resultCode == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
                // player indicated that they want to leave the room
                googlePlayHandler.leaveRoom();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                googlePlayHandler.leaveRoom();
            }
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
        soundPlayer.executor.shutdown();

        super.onDestroy();
    }

    @NonNull
    private OnFailureListener createFailureListener(final String string) {
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
    private void prepareConnection() {
        audioConnector.prepareBroadcastAudio();
        audioConnector.prepareReceiveAudio();
        internetConnector.sendReadyMessage();

        internetConnector.prepared = true;

        if (internetConnector.otherPlayerIsReady) {
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
    private void clearAllResources() {
        gameplayHandler.clearResources();
        audioConnector.clearResources();
        internetConnector.clearResources();
        soundPlayer.clearResources();
        gameStatistic.clear();
        gameSettings = null;
    }

    /**
     * Settings of the created game (basically maps of all levels in game), created by the one who
     * created the game, who also sends this setting to the other player.
     *
     * Owner's maps and teammate's maps are being swapped before sending to the other player.
     */
    private static class GameSettings implements Serializable {
        /**
         * Maze game map of this player (class name).
         */
        private String myMazeMap;

        /**
         * Maze game map of the teammate player (class name).
         */
        private String teammateMazeMap;

        /**
         * Code game map of this player.
         */
        private String myCodeGameMap;

        /**
         * Teammate code game map.
         */
        private String myTeammateCodeGameMap;

        /**
         * Lever game map of this player.
         */
        private String myLeverGameMap;

        /**
         * Teammate lever game map.
         */
        private String myTeammateLeverGameMap;


        /**
         * Writes this game settings to out stream in order to send it to the other player.
         */
        private void writeObject(java.io.ObjectOutputStream out)
                throws IOException {
            out.writeUTF(myMazeMap);
            out.writeUTF(teammateMazeMap);

            out.writeUTF(myCodeGameMap);
            out.writeUTF(myTeammateCodeGameMap);

            out.writeUTF(myLeverGameMap);
            out.writeUTF(myTeammateLeverGameMap);
        }

        /**
         * Reads game settings from in after receiving them from the internet.
         */
        private void readObject(java.io.ObjectInputStream in)
                throws IOException, ClassNotFoundException {
            myMazeMap = in.readUTF();
            teammateMazeMap = in.readUTF();

            myCodeGameMap = in.readUTF();
            myTeammateCodeGameMap = in.readUTF();

            myLeverGameMap = in.readUTF();
            myTeammateLeverGameMap = in.readUTF();
        }

        /**
         * Turn this settings into teammate's settings in order to send it to him.
         * Swaps owner's map and teammate's maps.
         */
        private void flipSettings() {
            String temp = myMazeMap;
            myMazeMap = teammateMazeMap;
            teammateMazeMap = temp;

            temp = myCodeGameMap;
            myCodeGameMap = myTeammateCodeGameMap;
            myTeammateCodeGameMap = temp;

            temp = myLeverGameMap;
            myLeverGameMap = myTeammateLeverGameMap;
            myTeammateLeverGameMap = temp;
        }

        private String getMyMazeMap() {
            return myMazeMap;
        }

        private void setMyMazeMap(String myMazeMap) {
            this.myMazeMap = myMazeMap;
        }

        private String getTeammateMazeMap() {
            return teammateMazeMap;
        }

        private void setTeammateMazeMap(String teammateMazeMap) {
            this.teammateMazeMap = teammateMazeMap;
        }

        private String getMyCodeGameMap() {
            return myCodeGameMap;
        }

        private void setMyCodeGameMap(String myCodeGameMap) {
            this.myCodeGameMap = myCodeGameMap;
        }

        @SuppressWarnings("unused")
        private String getMyTeammateCodeGameMap() {
            return myTeammateCodeGameMap;
        }

        private void setMyTeammateCodeGameMap(String myTeammateCodeGameMap) {
            this.myTeammateCodeGameMap = myTeammateCodeGameMap;
        }

        private void setMyLeverGameMap(String myLeverGameMap) {
            this.myLeverGameMap = myLeverGameMap;
        }

        private void setMyTeammateLeverGameMap(String myTeammateLeverGameMap) {
            this.myTeammateLeverGameMap = myTeammateLeverGameMap;
        }

    }

    /**
     * Class for playing sounds in game. Support playing several sounds in parallel.
     */
    public class SoundPlayer {
        /**
         * Players used to play sounds.
         */
        private final MediaPlayer[] players = new MediaPlayer[8];

        /**
         * Executor for setting players.
         */
        private ExecutorService executor = Executors.newSingleThreadExecutor();

        /**
         * Used to support choosing volume from 1 to 10 linearly.
         */
        private final int MAX_VOLUME = 11;

        /**
         * Finds free player and plays given track.
         */
        public void playTrackWithVolume(final int trackId, final int volume) {
            if (volume <= 0 || volume >= MAX_VOLUME) {
                return;
            }

            executor.submit(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < players.length; i++) {
                        if (players[i] == null) {
                            players[i] = MediaPlayer.create(GameActivity.this, trackId);
                        }

                        MediaPlayer mediaPlayer = players[i];

                        if (!mediaPlayer.isPlaying()) {
                            float actualVolume = (float) (Math.log(volume) / Math.log(MAX_VOLUME));
                            mediaPlayer.setVolume(actualVolume, actualVolume);

                            mediaPlayer.selectTrack(trackId);
                            mediaPlayer.start();
                            break;
                        }
                    }
                }
            });
        }

        /**
         * Plays track with maximum volume.
         */
        public void playTrack(int trackId) {
            playTrackWithVolume(trackId, 1);
        }

        /**
         * Releasing all players.
         */
        private void clearResources() {
            for (int i = 0; i < players.length; i++) {
                if (players[i] != null) {
                    players[i].release();
                    players[i] = null;
                }
            }
        }
    }

    /**
     * Class for handling google play connection (disconnection), managing game room.
     */
    private class GooglePlayHandler {
        /**
         * Maximum size of the buffer in google play reliable messaging.
         */
        private static final int MAX_RELIABLE_BUFFER_SIZE = 1400;

        /**
         * Current account player is signed in.
         */
        private GoogleSignInAccount mSignedInAccount;

        /**
         * Client used to sign in with Google APIs.
         */
        private GoogleSignInClient mGoogleSignInClient;

        /**
         * Client used to interact with the real time multiplayer system.
         */
        private RealTimeMultiplayerClient mRealTimeMultiplayerClient;

        /**
         * Client used to interact with game achievements.
         */
        private AchievementsClient mAchievementsClient;

        /**
         * Client used to interact with game statistic.
         */
        private LeaderboardsClient mLeaderboardsClient;

        /**
         * Client used to interact with the invitation system.
         */
        private InvitationsClient mInvitationsClient;

        /**
         * Room ID where the currently active game is taking place.
         */
        private String mRoomId;

        /**
         * Holds the configuration of the current room.
         */
        private RoomConfig mRoomConfig;

        /**
         * Player's account in the currently active game.
         */
        @SuppressWarnings("unused")
        private Participant myParticipant;

        /**
         * Account of player's teammate.
         */
        private Participant teammateParticipant;

        /**
         * Player's participant ID in the currently active game.
         */
        private String mMyId;

        /**
         * Id of the invitation received via the
         * invitation listener.
         */
        private String mIncomingInvitationId;

        /**
         * Player's id.
         */
        private String mPlayerId;

        /**
         * Start a sign in activity.
         */
        private void startSignInIntent() {
            assert mGoogleSignInClient != null;
            startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);
        }

        /**
         * Try to sign in without displaying dialogs to the user.
         * If the user has already signed in previously, it will not show dialog.
         */
        private void signInSilently() {
            Log.d(TAG, "signInSilently()");

            assert mGoogleSignInClient != null;
            mGoogleSignInClient.silentSignIn().addOnCompleteListener(GameActivity.this,
                    new OnCompleteListener<GoogleSignInAccount>() {
                        @Override
                        public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "signInSilently(): success");
                                onConnected(task.getResult());
                            } else {
                                Log.d(TAG, "signInSilently(): failure", task.getException());
                                onDisconnected();
                            }
                        }
                    });
        }

        /**
         * Signs out from current google play account.
         */
        private void signOut() {
            Log.d(TAG, "signOut()");

            assert mGoogleSignInClient != null;
            mGoogleSignInClient.signOut().addOnCompleteListener(GameActivity.this,
                    new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "signOut(): success");
                            } else {
                                handleException(task.getException(), "signOut() failed!");
                            }

                            onDisconnected();
                        }
                    });
        }

        /**
         * Handle the result of the "Select players UI" launched when the user clicked the
         * "Invite friend" button, creates a room with these player.
         */
        private void handleSelectPlayersResult(int response, Intent data) {
            if (response != Activity.RESULT_OK) {
                Log.w(TAG, "*** select players UI cancelled, " + response);
                uiHandler.switchToMainScreen();
                return;
            }

            Log.d(TAG, "Select players UI succeeded.");

            final String invitee = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS).get(0);
            Log.d(TAG, "Invited a player");

            Log.d(TAG, "Creating room...");
            uiHandler.switchToScreen(R.id.screen_wait);
            uiHandler.keepScreenOn();

            mRoomConfig = RoomConfig.builder(mRoomUpdateCallback)
                    .addPlayersToInvite(invitee)
                    .setOnMessageReceivedListener(internetConnector.mOnRealTimeMessageReceivedListener)
                    .setRoomStatusUpdateCallback(mRoomStatusUpdateCallback)
                    .build();
            mRealTimeMultiplayerClient.create(mRoomConfig);

            gameplayHandler.createGameSettings();

            Log.d(TAG, "Room created, waiting for it to be ready...");
        }

        /**
         * Handle the result of the invitation inbox UI, where the player can pick an invitation
         * to accept. React by accepting the selected invitation.
         */
        private void handleInvitationInboxResult(int response, Intent data) {
            if (response != Activity.RESULT_OK) {
                Log.w(TAG, "*** invitation inbox UI cancelled, " + response);
                uiHandler.switchToMainScreen();
                return;
            }

            Log.d(TAG, "Invitation inbox UI succeeded.");
            Invitation invitation = Objects.requireNonNull(data.getExtras()).getParcelable(Multiplayer.EXTRA_INVITATION);

            if (invitation != null) {
                acceptInviteToRoom(invitation.getInvitationId());
            }
        }

        /**
         * Accept the given invitation.
         */
        private void acceptInviteToRoom(String invitationId) {
            Log.d(TAG, "Accepting invitation: " + invitationId);

            mRoomConfig = RoomConfig.builder(mRoomUpdateCallback)
                    .setInvitationIdToAccept(invitationId)
                    .setOnMessageReceivedListener(internetConnector.mOnRealTimeMessageReceivedListener)
                    .setRoomStatusUpdateCallback(mRoomStatusUpdateCallback)
                    .build();

            uiHandler.switchToScreen(R.id.screen_wait);
            uiHandler.keepScreenOn();

            mRealTimeMultiplayerClient.join(mRoomConfig)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "Room Joined Successfully!");
                        }
                    });
        }

        /**
         * Leaves current room.
         */
        private void leaveRoom() {
            state = State.MAIN_MENU;

            setContentView(R.layout.activity_game);
            GameActivity.this.setupListeners();

            clearAllResources();

            Log.d(TAG, "Leaving room.");
            uiHandler.stopKeepingScreenOn();
            if (mRoomId != null) {
                assert mRoomConfig != null;
                mRealTimeMultiplayerClient.leave(mRoomConfig, mRoomId)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                mRoomId = null;
                                mRoomConfig = null;
                                uiHandler.switchToMainScreen();
                            }
                        });
            } else {
                uiHandler.switchToMainScreen();
            }
        }

        /**
         * Calls after connecting to the google play account.
         */
        private void onConnected(GoogleSignInAccount googleSignInAccount) {
            Log.d(TAG, "onConnected(): connected to Google APIs");
            if (mSignedInAccount != googleSignInAccount) {
                mSignedInAccount = googleSignInAccount;

                mRealTimeMultiplayerClient = Games.getRealTimeMultiplayerClient(GameActivity.this, googleSignInAccount);
                mInvitationsClient = Games.getInvitationsClient(GameActivity.this, googleSignInAccount);

                mAchievementsClient = Games.getAchievementsClient(GameActivity.this, googleSignInAccount);
                mLeaderboardsClient = Games.getLeaderboardsClient(GameActivity.this, googleSignInAccount);

                PlayersClient playersClient = Games.getPlayersClient(GameActivity.this, googleSignInAccount);
                playersClient.getCurrentPlayer()
                        .addOnSuccessListener(new OnSuccessListener<Player>() {
                            @Override
                            public void onSuccess(Player player) {
                                mPlayerId = player.getPlayerId();

                                uiHandler.switchToMainScreen();
                            }
                        })
                        .addOnFailureListener(createFailureListener("There was a problem getting the player id!"));
            }

            mInvitationsClient.registerInvitationCallback(mInvitationCallback);

            GamesClient gamesClient = Games.getGamesClient(GameActivity.this, googleSignInAccount);
            gamesClient.getActivationHint()
                    .addOnSuccessListener(new OnSuccessListener<Bundle>() {
                        @Override
                        public void onSuccess(Bundle hint) {
                            if (hint != null) {
                                Invitation invitation =
                                        hint.getParcelable(Multiplayer.EXTRA_INVITATION);

                                if (invitation != null && invitation.getInvitationId() != null) {
                                    // retrieve and cache the invitation ID
                                    Log.d(TAG, "onConnected: connection hint has a room invite!");
                                    acceptInviteToRoom(invitation.getInvitationId());
                                }
                            }
                        }
                    })
                    .addOnFailureListener(createFailureListener("There was a problem getting the activation hint!"));
        }

        /**
         * Calls after disconnecting from the google play account (etc after internet connection lost).
         */
        private void onDisconnected() {
            Log.d(TAG, "onDisconnected()");

            mRealTimeMultiplayerClient = null;
            mInvitationsClient = null;

            mAchievementsClient = null;
            mLeaderboardsClient = null;

            clearAllResources();
            setContentView(R.layout.activity_game);
            GameActivity.this.setupListeners();
            uiHandler.switchToScreen(R.id.screen_sign_in);
        }

        /**
         * Called when connected to the room.
         */
        private RoomStatusUpdateCallback mRoomStatusUpdateCallback = new RoomStatusUpdateCallback() {
            @Override
            public void onConnectedToRoom(Room room) {
                Log.d(TAG, "onConnectedToRoom.");

                mMyId = room.getParticipantId(mPlayerId);

                if (mRoomId == null) {
                    mRoomId = room.getRoomId();
                }

                Log.d(TAG, "Room ID: " + mRoomId);
                Log.d(TAG, "My ID " + mMyId);
                Log.d(TAG, "<< CONNECTED TO ROOM>>");
            }

            @Override
            public void onDisconnectedFromRoom(Room room) {
                mRoomId = null;
                mRoomConfig = null;
                uiHandler.showGameError();
            }

            @Override
            public void onPeerDeclined(Room room, @NonNull List<String> arg1) {
                updateRoom(room);
            }

            @Override
            public void onPeerInvitedToRoom(Room room, @NonNull List<String> arg1) {
                updateRoom(room);
            }

            @Override
            public void onP2PDisconnected(@NonNull String participant) {
            }

            @Override
            public void onP2PConnected(@NonNull String participant) {
            }

            @Override
            public void onPeerJoined(Room room, @NonNull List<String> arg1) {
                updateRoom(room);
            }

            @Override
            public void onPeerLeft(Room room, @NonNull List<String> peersWhoLeft) {
                updateRoom(room);
            }

            @Override
            public void onRoomAutoMatching(Room room) {
                updateRoom(room);
            }

            @Override
            public void onRoomConnecting(Room room) {
                updateRoom(room);
            }

            @Override
            public void onPeersConnected(Room room, @NonNull List<String> peers) {
                updateRoom(room);
            }

            @Override
            public void onPeersDisconnected(Room room, @NonNull List<String> peers) {
                updateRoom(room);
            }
        };

        /**
         * Called when player get an invitation to play a game, reacts by showing invitation to the user.
         */
        private InvitationCallback mInvitationCallback = new InvitationCallback() {
            @Override
            public void onInvitationReceived(@NonNull Invitation invitation) {
                mIncomingInvitationId = invitation.getInvitationId();
                ((TextView) findViewById(R.id.incoming_invitation_text)).setText(
                        String.format("%s %s", invitation.getInviter().getDisplayName(), getString(R.string.is_inviting_you)));
                uiHandler.switchToScreen(uiHandler.mCurScreen);
            }

            @Override
            public void onInvitationRemoved(@NonNull String invitationId) {
                if (mIncomingInvitationId != null && mIncomingInvitationId.equals(invitationId)) {
                    mIncomingInvitationId = null;
                    uiHandler.switchToScreen(uiHandler.mCurScreen);
                }
            }
        };

        /**
         * Handles updating of the current room.
         */
        private RoomUpdateCallback mRoomUpdateCallback = new RoomUpdateCallback() {
            @Override
            public void onRoomCreated(int statusCode, Room room) {
                Log.d(TAG, "onRoomCreated(" + statusCode + ", " + room + ")");
                if (statusCode != GamesCallbackStatusCodes.OK) {
                    Log.e(TAG, "*** Error: onRoomCreated, status " + statusCode);
                    uiHandler.showGameError();
                    return;
                }

                mRoomId = room.getRoomId();

                uiHandler.showWaitingRoom(room);
            }

            @Override
            public void onRoomConnected(int statusCode, Room room) {
                Log.d(TAG, "onRoomConnected(" + statusCode + ", " + room + ")");
                if (statusCode != GamesCallbackStatusCodes.OK) {
                    Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
                    uiHandler.showGameError();
                    return;
                }
                updateRoom(room);
            }

            @Override
            public void onJoinedRoom(int statusCode, Room room) {
                Log.d(TAG, "onJoinedRoom(" + statusCode + ", " + room + ")");
                if (statusCode != GamesCallbackStatusCodes.OK) {
                    Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
                    uiHandler.showGameError();
                    return;
                }

                uiHandler.showWaitingRoom(room);
            }

            @Override
            public void onLeftRoom(int statusCode, @NonNull String roomId) {
                Log.d(TAG, "onLeftRoom, code " + statusCode);
                clearAllResources();
                setContentView(R.layout.activity_game);
                GameActivity.this.setupListeners();
                uiHandler.switchToMainScreen();
            }
        };

        /**
         * Shows player's achievements screen.
         */
        private void onShowAchievementsRequested() {
            mAchievementsClient.getAchievementsIntent()
                    .addOnSuccessListener(new OnSuccessListener<Intent>() {
                        @Override
                        public void onSuccess(Intent intent) {
                            startActivityForResult(intent, RC_UNUSED);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            handleException(e, getString(R.string.achievements_exception));
                        }
                    });
        }

        private void onShowLeaderboardsRequested() {
            mLeaderboardsClient.getAllLeaderboardsIntent()
                    .addOnSuccessListener(new OnSuccessListener<Intent>() {
                        @Override
                        public void onSuccess(Intent intent) {
                            startActivityForResult(intent, RC_UNUSED);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            handleException(e, getString(R.string.leaderboards_exception));
                        }
                    });
        }

        /**
         * Saves player's achievements and statistic to the cloud.
         */
        private void pushAccomplishments() {
            if (GoogleSignIn.getLastSignedInAccount(GameActivity.this) == null) {
                return;
            }

            if (gameStatistic.getMazeGameTime() != -1) {
                mLeaderboardsClient.submitScore(getString(R.string.leaderboard_maze_game_best_time), gameStatistic.getMazeGameTime());
            }

            if (gameStatistic.getCodeGameTime() != -1) {
                mLeaderboardsClient.submitScore(getString(R.string.leaderboard_code_game_best_time), gameStatistic.getCodeGameTime());
            }

            if (gameStatistic.getLeverGameTime() != -1) {
                mLeaderboardsClient.submitScore(getString(R.string.leaderboard_lever_game_best_time), gameStatistic.getLeverGameTime());
            }

            if (gameStatistic.getCodeGameMistakeTaken() != -1) {
                mLeaderboardsClient.submitScore(getString(R.string.leaderboard_code_game_least_mistake_taken), gameStatistic.getCodeGameMistakeTaken());
            }

        }

        /**
         * Get (or remove) teammate's Participant account.
         */
        private void updateRoom(Room room) {
            if (room == null) {
                return;
            }

            Participant[] participants = new Participant[2];
            participants = room.getParticipants().toArray(participants);

            myParticipant = null;
            teammateParticipant = null;

            for (Participant participant : participants) {
                if (participant.getParticipantId().equals(mMyId)) {
                    myParticipant = participant;
                } else {
                    teammateParticipant = participant;
                }
            }
        }
    }

    /**
     * Class for managing internet messaging between player's.
     */
    private class InternetConnector {
        /**
         * True if got message from teammate that he is ready to get a voice connection.
         */
        private boolean otherPlayerIsReady = false;

        /**
         * True if ready to send and receive voice.
         */
        private boolean prepared = false;

        /**
         * Reset prepared status for the next created game.
         */
        private void clearResources() {
            otherPlayerIsReady = false;
            prepared = false;
        }

        /**
         * Send to other player that we are ready to receive his voice.
         * Also sends game settings if we are the game host.
         */
        private void sendReadyMessage() {
            byte[] message;

            if (gameSettings != null) {
                try {
                    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                    ObjectOutputStream writeStream;

                    writeStream = new ObjectOutputStream(byteStream);

                    gameSettings.flipSettings();
                    writeStream.writeObject(gameSettings);
                    writeStream.flush();
                    gameSettings.flipSettings();

                    message = byteStream.toByteArray();
                    writeStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    uiHandler.showGameError();
                    googlePlayHandler.leaveRoom();
                    return;
                }
            } else {
                message = new byte[1];
                message[0] = 'R';
            }

            if (message.length > Multiplayer.MAX_RELIABLE_MESSAGE_LEN) {
                Log.d(TAG, "Ready message: message is too long.");
            }

            sendReliableMessage(message);
        }

        private void reactOnReceivedMessage(byte[] receivedData) {
            if (!otherPlayerIsReady) {
                if (gameSettings == null) {
                    try {
                        ObjectInputStream stream = new ObjectInputStream(new ByteArrayInputStream(receivedData));
                        try {
                            gameSettings = (GameSettings) stream.readObject();

                            stream.close();
                        } catch (ClassNotFoundException e) {
                            handleException(new IOException(), "Error reading from object input stream");
                        }
                    } catch (IOException e) {
                        handleException(new IOException(), "Error reading from object input stream");
                    }
                }

                otherPlayerIsReady = true;

                if (prepared) {
                    gameplayHandler.startGame();
                }
            } else if (receivedData[0] == 'L') {
                //Other player lost on his map.
                if (receivedData[1] == 'M') {
                    gameplayHandler.gameOver(true);
                } else if (receivedData[1] == 'C') {
                    gameplayHandler.gameOver(true);
                } else if (receivedData[1] == 'L') {
                    gameStatistic.setKillYourFriend(true);
                    gameplayHandler.gameOver(true);
                } else {
                    Log.d(TAG, "wrong game code in message");
                }
            } else if (receivedData[0] == 'W') {
                //Other player won on his map.
                if (receivedData[1] == 'M') {
                    gameplayHandler.otherMazeGameWon = true;

                    if (gameplayHandler.myMazeGameWon) {
                        gameplayHandler.startCodeGame();
                    }
                } else if (receivedData[1] == 'C') {
                    gameplayHandler.otherCodeGameWon = true;

                    if (gameplayHandler.myCodeGameWon) {
                        gameplayHandler.startLeverGame();
                    }
                } else if (receivedData[1] == 'L') {
                    gameplayHandler.otherLeverGameWon = true;

                    if (gameplayHandler.myLeverGameWon) {
                        gameplayHandler.gameWin();
                    }
                } else {
                    Log.d(TAG, "wrong game code");
                }
            } else if (receivedData[0] == 'S') {
                if (gameplayHandler.leverGameMap == null) {
                    return;
                }

                byte[] leverName = new byte[receivedData.length - 1];
                System.arraycopy(receivedData, 1, leverName, 0, leverName.length);

                String lever = new String(leverName);
                Log.d(TAG, "Received pressed lever: " + lever);

                gameplayHandler.leverGameMap.applyLever(lever);
            }
        }

        /**
         * Message receiver. The very first message is signal of readiness.
         */
        private OnRealTimeMessageReceivedListener mOnRealTimeMessageReceivedListener = new OnRealTimeMessageReceivedListener() {
            @Override
            public void onRealTimeMessageReceived(@NonNull RealTimeMessage realTimeMessage) {
                final byte[] receivedData = realTimeMessage.getMessageData();

                if (receivedData.length > 0 && receivedData[0] == 'P') {
                    //Received voice audio.
                    if (audioConnector.track != null) {
                        audioConnector.track.write(receivedData, 1, receivedData.length - 1);
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            reactOnReceivedMessage(receivedData);
                        }
                    });
                }
            }
        };

        /**
         * Sends reliable message without message callback.
         */
        private void sendReliableMessage(byte[] message) {
            googlePlayHandler.mRealTimeMultiplayerClient.sendReliableMessage(message, googlePlayHandler.mRoomId, googlePlayHandler.teammateParticipant.getParticipantId(), null);
        }

        /**
         * Send to other player that we have lost the maze game.
         */
        private void sendMazeLostMessage() {
            byte[] message = new byte[2];
            message[0] = 'L';
            message[1] = 'M';
            sendReliableMessage(message);
        }

        /**
         * Send to other player that we have won the maze game.
         */
        private void sendMazeWonMessage() {
            byte[] message = new byte[2];
            message[0] = 'W';
            message[1] = 'M';
            sendReliableMessage(message);
        }

        /**
         * Send to other player that we have lost the code game.
         */
        private void sendCodeLostMessage() {
            byte[] message = new byte[2];
            message[0] = 'L';
            message[1] = 'C';
            sendReliableMessage(message);
        }

        /**
         * Send to other player that we have won the code game.
         */
        private void sendCodeWonMessage() {
            byte[] message = new byte[2];
            message[0] = 'W';
            message[1] = 'C';
            sendReliableMessage(message);
        }

        /**
         * Send to other player that we have lost the lever game.
         */
        private void sendLeverLostMessage() {
            byte[] message = new byte[2];
            message[0] = 'L';
            message[1] = 'L';
            sendReliableMessage(message);
        }

        /**
         * Send to other player that we have won the lever game.
         */
        private void sendLeverWonMessage() {
            byte[] message = new byte[2];
            message[0] = 'W';
            message[1] = 'L';
            sendReliableMessage(message);
        }

        /**
         * Send the name of the lever that we have pressed on our screen.
         */
        private void sendLeverPressedMessage(String lever) {
            sendReliableMessage(("S" + lever).getBytes());
        }
    }

    /**
     * Class for managing voice connection between players.
     */
    private class AudioConnector {
        /**
         * Minimum buffer size to record audio.
         */
        private final int MIN_BUFFER_SIZE = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

        /**
         * Object for streaming audio received as byte buffer.
         */
        private AudioTrack track;

        /**
         * True if player broadcasts his voice, if false --- voice recording pauses.
         */
        private volatile boolean broadcastAudio = false;

        /**
         * Thread for broadcasting audio.
         */
        private Thread broadcastThread;

        /**
         * Voice recorder that translate input voice into byte buffer.
         */
        private AudioRecord recorder;

        /**
         * Prepares AudioTrack.
         */
        private void prepareReceiveAudio() {
            track = new AudioTrack(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build(),
                    new AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(8000)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build(), MIN_BUFFER_SIZE *10, AudioTrack.MODE_STREAM, AudioManager.AUDIO_SESSION_ID_GENERATE);
            track.play();

            Log.d(TAG, "Audio reception prepared");
        }

        /**
         * Initialise audio recorder and starts thread that sends recorded audio to other player infinitely.
         */
        private void prepareBroadcastAudio () {
            if (googlePlayHandler.teammateParticipant == null || googlePlayHandler.teammateParticipant.getStatus() != Participant.STATUS_JOINED) {
                uiHandler.showGameError();
                googlePlayHandler.leaveRoom();
            }

            recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, MIN_BUFFER_SIZE * 10);

            broadcastThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    byte[] buffer = new byte[googlePlayHandler.MAX_RELIABLE_BUFFER_SIZE];

                    recorder.startRecording();
                    while (!Thread.interrupted()) {
                        if (broadcastAudio) {
                            int n = recorder.read(buffer, 1, buffer.length - 1);
                            buffer[0] = 'P';

                            byte[] toSend = new byte[n];
                            System.arraycopy(buffer, 0, toSend, 0, n);

                            internetConnector.sendReliableMessage(toSend);
                        }
                    }
                }
            });

            broadcastThread.start();

            Log.d(TAG, "Voice broadcast started");
        }

        /**
         * Set broadcastAudio to true, after that thread that record voice starts to send recorded audio to
         * the player.
         */
        private void startBroadcastAudio() {
            broadcastAudio = true;
            recorder.startRecording();
        }

        /**
         * Stops recording voice and sending it to other player.
         */
        @SuppressWarnings("unused")
        private void stopBroadcastAudio() {
            broadcastAudio = false;
            recorder.stop();
        }

        /**
         * Stops threads for playing voice,
         */
        private void clearResources() {
            if (broadcastThread != null) {
                broadcastThread.interrupt();
            }

            broadcastAudio = false;

            if (recorder != null) {
                recorder.release();
            }

            if (track != null) {
                track.release();
                track = null;
            }
        }
    }

    /**
     * Handles user interface.
     */
    private class UIHandler {
        /**
         * All the individual screens game has.
         */
        private final int[] SCREENS = {
                R.id.main_screen,
                R.id.screen_sign_in,
                R.id.screen_wait,
                R.id.invitation_popup
        };

        /**
         * Last used screen.
         */
        private int mCurScreen = -1;

        /**
         * Switches to the given screen.
         */
        private void switchToScreen(int screenId) {
            for (int id : SCREENS) {
                final View view = findViewById(id);
                if (view != null) {
                    view.setVisibility(screenId == id ? View.VISIBLE : View.GONE);
                } else {
                    Log.d(TAG, "Somehow view with id " + id + " was not found");
                }
            }
            mCurScreen = screenId;

            boolean showInvPopup;
            if (googlePlayHandler.mIncomingInvitationId == null) {
                showInvPopup = false;
            } else {
                showInvPopup = (mCurScreen == R.id.main_screen);
            }

            View view = findViewById(R.id.invitation_popup);
            if (view != null) {
                view.setVisibility(showInvPopup ? View.VISIBLE : View.GONE);
            }
        }

        private void switchToMainScreen() {
            state = State.MAIN_MENU;

            if (googlePlayHandler.mRealTimeMultiplayerClient != null) {
                switchToScreen(R.id.main_screen);
            } else {
                googlePlayHandler.signInSilently();
            }
        }

        /**
         * Show the waiting room UI to track the progress of other players as they enter the
         * room and get connected.
         */
        private void showWaitingRoom(Room room) {
            googlePlayHandler.mRealTimeMultiplayerClient.getWaitingRoomIntent(room, NUMBER_OF_PLAYERS)
                    .addOnSuccessListener(new OnSuccessListener<Intent>() {
                        @Override
                        public void onSuccess(Intent intent) {
                            // show waiting room UI
                            startActivityForResult(intent, RC_WAITING_ROOM);
                        }
                    })
                    .addOnFailureListener(createFailureListener("There was a problem getting the waiting room!"));
        }

        /**
         * Show error message about game being cancelled and return to main screen.
         */
        void showGameError() {
            googlePlayHandler.leaveRoom();

            switchToMainScreen();

            new AlertDialog.Builder(GameActivity.this)
                    .setMessage("We are having problems creating or running your game. Please check your internet connection.")
                    .setNeutralButton(android.R.string.ok, null).show();
        }

        /**
         * Ask permission to record voice, if it wasn't given yet.
         * TODO test
         */
        private void askPermission() {
            if (ActivityCompat.checkSelfPermission(GameActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(GameActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, RC_REQUEST_VOICE_RECORD_PERMISSION);
            } else {
                Log.d(TAG, "Permission already granted");
                prepareConnection();
            }
        }


        /**
         * Sets the flag to keep this screen on.
         */
        private void keepScreenOn() {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        /**
         * Clears the flag that keeps the screen on.
         */
        private void stopKeepingScreenOn() {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    /**
     * Return class for interacting with the gameplay stage of the game.
     */
    @NonNull
    public GameplayHandler getGameplayHandler() {
        return gameplayHandler;
    }

    /**
     * Class for interacting with the gameplay stage of the game.
     */
    public class GameplayHandler {
        /**
         * True if won the current level but other player may lose it).
         */
        private boolean myMazeGameWon = false;

        /**
         * True if received message that other player won his current level.
         */
        private boolean otherMazeGameWon = false;

        /**
         * True if won the current level but other player may lose it).
         */
        private boolean myCodeGameWon = false;

        /**
         * True if received message that other player won his current level.
         */
        private boolean otherCodeGameWon = false;

        /**
         * True if won the current level but other player may lose it).
         */
        private boolean myLeverGameWon = false;

        /**
         * True if received message that other player won his current level.
         */
        private boolean otherLeverGameWon = false;


        /**
         * Class for handling gameplay stage of the maze game.
         */
        private MazeGame maze;

        /**
         * Instance of the player's game map.
         */
        private MazeGameMap mazeGameMap;

        /**
         * Class for handling gameplay stage of the code game.
         */
        private CodeGame codeGame = null;

        /**
         * Instance of the player's game map.
         */
        private CodeGameMap codeGameMap;

        /**
         * Class for handling gameplay stage of the lever game.
         */
        private LeverGame leverGame = null;

        /**
         * Instance of the player's game map.
         */
        private LeverGameMap leverGameMap;

        /**
         * Clears all handlers and they resources.
         */
        private void clearResources() {
            clearMazeResources();

            clearCodeGameResources();

            clearLeverGameResources();
        }

        /**
         * Clears maze game resources.
         */
        private void clearMazeResources() {
            if (maze != null) {
                maze.onClose();
                maze = null;
            }

            myMazeGameWon = false;
            otherMazeGameWon = false;
        }

        /**
         * Clears code game resources.
         */
        private void clearCodeGameResources() {
            if (codeGame != null) {
                codeGame = null;
            }

            myCodeGameWon = false;
            otherCodeGameWon = false;
        }

        /**
         * Clears lever game resources.
         */
        private void clearLeverGameResources() {
            if (leverGame != null) {
                leverGame = null;
            }

            myLeverGameWon = false;
            otherLeverGameWon = false;
        }

        /**
         * Calls when game's creator successfully started the game. Assigns maps of all levels in game.
         */
        private void createGameSettings() {
            gameSettings = new GameSettings();

            /*
            There could be more smarter selection, but since I haven't made enough amount of levels in the
            game, there is not much to select from.
             */

            if (random.nextBoolean()) {
                gameSettings.setMyMazeMap(MazeGameMap_Test.class.getName());
                gameSettings.setTeammateMazeMap(MazeGameMap_Test2.class.getName());
            } else {
                gameSettings.setMyMazeMap(MazeGameMap_Test2.class.getName());
                gameSettings.setTeammateMazeMap(MazeGameMap_Test.class.getName());
            }

            //For debugging.
            /*
            gameSettings.setMyMazeMap(MazeGameMap_Simple.class.getName());
            gameSettings.setTeammateMazeMap(MazeGameMap_Simple.class.getName());
            */

            String[] codeGames = new String[] {CodeGameMap_Test1.class.getName(), CodeGameMap_Test2.class.getName(), CodeGameMap_Test3.class.getName(), CodeGameMap_Test4.class.getName()};
            int myCodeGameId = (Math.abs(random.nextInt())) % 4;
            gameSettings.setMyCodeGameMap(codeGames[myCodeGameId]);
            int teammateCodeGameId = (Math.abs(random.nextInt())) % 4;
            while (teammateCodeGameId == myCodeGameId) {
                teammateCodeGameId = (Math.abs(random.nextInt())) % 4;
            }
            gameSettings.setMyTeammateCodeGameMap(codeGames[teammateCodeGameId]); //TODO smart selection


            String[] leverGames = new String[] {LeverGameMap_Test1.class.getName(), LeverGameMap_Test2.class.getName(), LeverGameMap_Test3.class.getName()};
            int myLeverGameId = (Math.abs(random.nextInt())) % 3;
            gameSettings.setMyLeverGameMap(leverGames[myLeverGameId]);
            int teammateLeverGameId = (Math.abs(random.nextInt())) % 3;
            while (teammateLeverGameId == myLeverGameId) {
                teammateLeverGameId = (Math.abs(random.nextInt())) % 3;
            }
            gameSettings.setMyTeammateLeverGameMap(leverGames[teammateLeverGameId]); //TODO smart selection
        }

        /**
         * Start gameplay stage of the game.
         */
        private void startGame() {
            audioConnector.startBroadcastAudio();
            startMazeGame();
        }

        /**
         * Starts maze game gameplay stage.
         */
        private void startMazeGame() {
            state = State.MAZE_GAME;

            setContentView(R.layout.content_maze_game);

            assert gameSettings != null;
            Log.d(TAG, "Loaded maps are " + gameSettings.getMyMazeMap() + " " + gameSettings.getTeammateMazeMap());

            MazeGameMap teammateMap = null;
            try {
                mazeGameMap = (MazeGameMap) getClassLoader().loadClass(gameSettings.getMyMazeMap()).getDeclaredConstructor(GameActivity.class).newInstance(GameActivity.this);
                teammateMap = (MazeGameMap) getClassLoader().loadClass(gameSettings.getTeammateMazeMap()).getDeclaredConstructor(GameActivity.class).newInstance(GameActivity.this);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            if (mazeGameMap == null || teammateMap == null) {
                Log.d(TAG, "failed to create the map");
                handleException(new RuntimeException(), "Couldn't create maze game");
                return;
            }

            ((ImageView) findViewById(R.id.image_maze_map)).setImageResource(teammateMap.getImageID());

            maze = new MazeGame(mazeGameMap, GameActivity.this);
            mazeGameMap.draw();
            maze.setupListeners();

            gameStatistic.setMazeGameTime(System.currentTimeMillis());
            Log.d(TAG, "Switched to maze game");
        }



        /**
         * Called then we won the maze game. Either starts the next game or hides everything but
         * the other player's map.
         */
        public void onMazeGameWon() {
            gameStatistic.setMazeGameTime(System.currentTimeMillis() - gameStatistic.getMazeGameTime());

            for (int i = 0; i < MazeGameMap.HEIGHT_VIEW; i++) {
                for (int j = 0; j < MazeGameMap.WIDTH_VIEW; j++) {
                    ImageView imageView = findViewById(mazeGameMap.getImageIds()[i][j]);

                    if (imageView != null) {
                        imageView.setVisibility(View.GONE);
                    }
                }
            }

            Button button = findViewById(R.id.downButton);
            if (button != null) {
                findViewById(R.id.downButton).setVisibility(View.GONE);
                findViewById(R.id.upButton).setVisibility(View.GONE);
                findViewById(R.id.leftButton).setVisibility(View.GONE);
                findViewById(R.id.rightButton).setVisibility(View.GONE);
                findViewById(R.id.useButton).setVisibility(View.GONE);
            }

            myMazeGameWon = true;

            internetConnector.sendMazeWonMessage();

            if (otherMazeGameWon) {
                Log.d(TAG, "maze game won");
                startCodeGame();
            }
        }

        /**
         * Called when we lost the maze game (and, therefore, we and our teammate lost an entire game).
         */
        public void onMazeGameLost() {
            gameStatistic.setMazeGameTime(-1);

            internetConnector.sendMazeLostMessage();
            gameOver(false);
            Log.d(TAG, "maze game lost");
        }

        /**
         * Called when we have won the code game. Hides everything but the image with hint to the other
         * player's game.
         */
        public void onCodeGameWon() {
            gameStatistic.setCodeGameTime(System.currentTimeMillis() - gameStatistic.getCodeGameTime());

            findViewById(R.id.text_code).setVisibility(View.GONE);
            findViewById(R.id.button_giveUp).setVisibility(View.GONE);
            findViewById(R.id.button_submitCode).setVisibility(View.GONE);

            myCodeGameWon = true;

            internetConnector.sendCodeWonMessage();

            if (otherCodeGameWon) {
                Log.d(TAG, "code game won");
                startLeverGame();
            }
        }

        /**
         * Starts the gameplay stage of the code game.
         */
        private void startCodeGame() {
            state = State.CODE_GAME;

            Log.d(TAG, "Code game started");

            clearMazeResources();

            setContentView(R.layout.content_code_game);

            CodeGameMap teammateCodeGameMap = null;
            try {
                assert gameSettings != null;
                codeGameMap = (CodeGameMap) getClassLoader().loadClass(gameSettings.getMyCodeGameMap()).getDeclaredConstructor().newInstance();
                teammateCodeGameMap = (CodeGameMap) getClassLoader().loadClass(gameSettings.getMyTeammateCodeGameMap()).getDeclaredConstructor().newInstance();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            Log.d(TAG, "Switched to code game");

            if (codeGameMap == null || teammateCodeGameMap == null) {
                Log.d(TAG, "failed to load code game map");
                return;
            }

            codeGame = new CodeGame(GameActivity.this, codeGameMap, teammateCodeGameMap);
            codeGame.setupListeners();
            gameStatistic.setCodeGameTime(System.currentTimeMillis());
        }

        /**
         * Called when we lost the code game (and, therefore, we and our teammate lost an entire game).
         */
        public void onCodeGameLost() {
            internetConnector.sendCodeLostMessage();
            gameOver(false);
            Log.d(TAG, "code game lost");
        }

        /**
         * Called on loosing the game.
         */
        private void gameOver(boolean friendDied) {
            if (gameStatistic.isDeadByMonster()) {
                googlePlayHandler.mAchievementsClient.unlock(getString(R.string.achievement_get_dunked_on));
            }

            if (gameStatistic.isKillYourFriend()) {
                googlePlayHandler.mAchievementsClient.unlock(getString(R.string.achievement_how_could_you_do_this));
            }

            clearAllResources();

            String message = "You lost the game because you was killed. Better luck next time!";
            if (friendDied) {
                message = "You lost the game because your friend has been killed. Better luck next time!";
            }

            Toast.makeText(GameActivity.this, message, Toast.LENGTH_LONG).show();
            googlePlayHandler.leaveRoom();
        }

        /**
         * Starts the lever game gameplay stage.
         */
        private void startLeverGame() {
            state = State.LEVER_GAME;

            Log.d(TAG, "Lever game started");

            clearCodeGameResources();

            myLeverGameWon = false;
            otherLeverGameWon = false;

            setContentView(R.layout.content_lever_game);

            leverGameMap = null;
            LeverGameMap teammateLeverGameMap = null;

            assert gameSettings != null;
            Log.d(TAG, "loaded lever maps are: " + gameSettings.myLeverGameMap + " " + gameSettings.myTeammateLeverGameMap);

            try {
                leverGameMap = (LeverGameMap) getClassLoader().loadClass(gameSettings.myLeverGameMap).getDeclaredConstructor(GameActivity.class).newInstance(GameActivity.this);
                teammateLeverGameMap = (LeverGameMap) getClassLoader().loadClass(gameSettings.myTeammateLeverGameMap).getDeclaredConstructor(GameActivity.class).newInstance(GameActivity.this);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            Log.d(TAG, "Switched to lever game");

            if (leverGameMap == null || teammateLeverGameMap == null) {
                Log.d(TAG, "failed to load lever game map");
                return;
            }

            leverGame = new LeverGame(GameActivity.this, leverGameMap, teammateLeverGameMap);
            leverGame.setupListeners();
            gameStatistic.setLeverGameTime(System.currentTimeMillis());
        }

        /**
         * Sends the pressed lever (public for using in internal logic).
         */
        public void sendLeverPressedMessage(String lever) {
            internetConnector.sendLeverPressedMessage(lever);
        }

        /**
         * Handles the loosing the lever game.
         */
        public void onLeverGameLost(boolean wasKilled) {
            internetConnector.sendLeverLostMessage();
            Log.d(TAG, "lever game lost");
            gameOver(false);

            if (!wasKilled) {
                gameStatistic.setKillYourFriend(true);
            }
        }

        /**
         * Handles the winning of the lever game.
         */
        public void onLeverGameWon() {
            gameStatistic.setLeverGameTime(System.currentTimeMillis() - gameStatistic.getLeverGameTime());

            leverGameMap = null;
            findViewById(R.id.button_giveUp_lever).setVisibility(View.GONE);

            myLeverGameWon = true;

            internetConnector.sendLeverWonMessage();

            if (otherLeverGameWon) {
                Log.d(TAG, "lever game won");
                gameWin();
            }
        }

        /**
         * Handles winning an entire game.
         */
        private void gameWin() {
            googlePlayHandler.pushAccomplishments();
            clearAllResources();
            Toast.makeText(GameActivity.this, "VICTORY! Congrats ;) !!!", Toast.LENGTH_LONG).show();
            googlePlayHandler.leaveRoom();
        }
    }

    @NonNull
    public GameStatistic getGameStatistic() {
        return gameStatistic;
    }

    /**
     * Class for storing statistic (and achievements) of the game.
     */
    public static class GameStatistic {
        /**
         * Time for winning the maze game.
         */
        private long mazeGameTime = -1;

        /**
         * Time for winning the code game.
         */
        private long codeGameTime = -1;

        /**
         * Time for winning the lever game.
         */
        private long leverGameTime = -1;

        /**
         * How many mistakes were taking when playing the code game.
         */
        private int codeGameMistakeTaken = -1;

        /**
         * True if player have died by the monster during the maze game.
         */
        private boolean deadByMonster = false;

        /**
         * True if player have killed his friend during the code game.
         */
        private boolean killYourFriend = false;

        /**
         * Resets all statistic to default.
         */
        private void clear() {
            mazeGameTime = -1;
            codeGameTime = -1;
            leverGameTime = -1;
            codeGameMistakeTaken = -1;

            deadByMonster = false;
            killYourFriend = false;
        }

        private long getMazeGameTime() {
            return mazeGameTime;
        }

        private void setMazeGameTime(long mazeGameTime) {
            this.mazeGameTime = mazeGameTime;
        }

        private long getCodeGameTime() {
            return codeGameTime;
        }

        private void setCodeGameTime(long codeGameTime) {
            this.codeGameTime = codeGameTime;
        }

        private long getLeverGameTime() {
            return leverGameTime;
        }

        private void setLeverGameTime(long leverGameTime) {
            this.leverGameTime = leverGameTime;
        }

        private int getCodeGameMistakeTaken() {
            return codeGameMistakeTaken;
        }

        public void setCodeGameMistakeTaken(int codeGameMistakeTaken) {
            this.codeGameMistakeTaken = codeGameMistakeTaken;
        }

        public void incrementCodeGameMistakeTaken() {
            this.codeGameMistakeTaken++;
        }

        private boolean isDeadByMonster() {
            return deadByMonster;
        }

        public void setDeadByMonster(boolean deadByMonster) {
            this.deadByMonster = deadByMonster;
        }

        private boolean isKillYourFriend() {
            return killYourFriend;
        }

        private void setKillYourFriend(boolean killYourFriend) {
            this.killYourFriend = killYourFriend;
        }
    }
}