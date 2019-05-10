package ru.ralsei.whatcanyousee;

import android.Manifest;
import android.accounts.Account;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesCallbackStatusCodes;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.GamesClientStatusCodes;
import com.google.android.gms.games.InvitationsClient;
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
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Objects;

import ru.ralsei.whatcanyousee.internalLogic.MazeGame;
import ru.ralsei.whatcanyousee.internalLogic.MazeMap;
import ru.ralsei.whatcanyousee.maps.SimpleMap;
import ru.ralsei.whatcanyousee.maps.TestMap;

//TODO @NonNull and stuff

public class GameActivity extends AppCompatActivity implements View.OnClickListener {
    private final static String TAG = "What can you see?";

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
     * Request code to ask permission to record player's voice.
     */
    private static final int RC_REQUEST_VOICE_RECORD_PERMISSION = 8001;

    /**
     * Client used to sign in with Google APIs.
     */
    private GoogleSignInClient mGoogleSignInClient = null;

    /**
     * Client used to interact with the real time multiplayer system.
     */
    private RealTimeMultiplayerClient mRealTimeMultiplayerClient = null;

    /**
     * Client used to interact with the invitation system.
     */
    private InvitationsClient mInvitationsClient = null;

    /**
    * Room ID where the currently active game is taking place.
    */
    private String mRoomId = null;

    /**
     * Holds the configuration of the current room.
     */
    private RoomConfig mRoomConfig;

    /**
     * Player's account in the currently active game.
     */
    private Participant myParticipant = null;

    /**
     * Account of player's teammate.
     */
    private Participant teammateParticipant = null;

    /**
     * Player's participant ID in the currently active game.
     */
    private String mMyId = null;

    /**
    * Id of the invitation received via the
    * invitation listener.
    */
    private String mIncomingInvitationId = null;

    /**
     * Number of players in game. Always 2.
     */
    private final static int NUMBER_OF_PLAYERS = 2;

    /**
     * TODO
     */
    private GameSettings gameSettings = null;

    /**
     * onCreate with debugMode = true runs single player instance of some games
     * (debugMode and additional argument passes with extraData), without logging into google account.
     */
    private boolean debugMode = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent.hasExtra("debug")) {
            debugMode = true;
            String debugGame = intent.getStringExtra("debug");
            if (debugGame != null) {
                //noinspection SwitchStatementWithTooFewBranches
                switch (debugGame) {
                    case "maze":
                        startMazeGame();
                        break;
                    default:
                        handleException(new IllegalArgumentException(), "Wrong debug name game passed to intent");
                }
            }

            return;
        }

        setContentView(R.layout.activity_create_room);

        mGoogleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);

        findViewById(R.id.button_accept_popup_invitation).setOnClickListener(this);
        findViewById(R.id.button_invite_friend).setOnClickListener(this);
        findViewById(R.id.button_accept_invitation).setOnClickListener(this);
        findViewById(R.id.button_sign_in).setOnClickListener(this);
        findViewById(R.id.button_sign_out).setOnClickListener(this);

        for (int screen : SCREENS) {
            findViewById(screen).setVisibility(View.GONE);
        }

        switchToMainScreen();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");

        if (!debugMode) {
            signInSilently();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Unregister listeners.  They will be re-registered via onResume->signInSilently->onConnected.
        if (mInvitationsClient != null) {
            mInvitationsClient.unregisterInvitationCallback(mInvitationCallback);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_sign_in:
                Log.d(TAG, "Sign-in button clicked");
                startSignInIntent();
                break;
            case R.id.button_sign_out:
                Log.d(TAG, "Sign-out button clicked");
                signOut();
                switchToScreen(R.id.screen_sign_in);
                break;
            case R.id.button_invite_friend:
                switchToScreen(R.id.screen_wait);

                mRealTimeMultiplayerClient.getSelectOpponentsIntent(1, 1, false).addOnSuccessListener(
                        new OnSuccessListener<Intent>() {
                            @Override
                            public void onSuccess(Intent intent) {
                                startActivityForResult(intent, RC_SELECT_PLAYERS);
                            }
                        }
                ).addOnFailureListener(createFailureListener("There was a problem selecting opponents."));
                break;
            case R.id.button_accept_invitation:
                switchToScreen(R.id.screen_wait);

                mInvitationsClient.getInvitationInboxIntent().addOnSuccessListener(
                        new OnSuccessListener<Intent>() {
                            @Override
                            public void onSuccess(Intent intent) {
                                startActivityForResult(intent, RC_INVITATION_INBOX);
                            }
                        }
                ).addOnFailureListener(createFailureListener("There was a problem getting the inbox."));
                break;
            case R.id.button_accept_popup_invitation:
                acceptInviteToRoom(mIncomingInvitationId);
                mIncomingInvitationId = null;
                break;
        }
    }

    /**
     * Start a sign in activity.
     */
    private void startSignInIntent() {
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);
    }

    /**
     * Try to sign in without displaying dialogs to the user.
     * If the user has already signed in previously, it will not show dialog.
     */
    private void signInSilently() {
        Log.d(TAG, "signInSilently()");

        final Activity thisActivity = this;

        mGoogleSignInClient.silentSignIn().addOnCompleteListener(this,
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

    private void signOut() {
        Log.d(TAG, "signOut()");

        mGoogleSignInClient.signOut().addOnCompleteListener(this,
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
     * Since a lot of the operations use tasks, we can use a common handler for whenever one fails.
     *
     * @param exception The exception to evaluate.  Will try to display a more descriptive reason for the exception.
     * @param details   Will display alongside the exception if you wish to provide more details for why the exception
     *                  happened.
     */
    private void handleException(Exception exception, String details) {
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
                onConnected(account);
            } catch (ApiException apiException) {
                String message = "Seems like you are having problems connecting to google play. " +
                        "Please check your internet connection.";

                onDisconnected();

                new AlertDialog.Builder(this)
                        .setMessage(message)
                        .setNeutralButton(android.R.string.ok, null)
                        .show();
            }
        } else if (requestCode == RC_SELECT_PLAYERS) {
            // got the result from the "select players" UI -- ready to create the room

            handleSelectPlayersResult(resultCode, intent);
        } else if (requestCode == RC_INVITATION_INBOX) {
            // got the result from the "select invitation" UI. ready to accept the selected invitation:
            handleInvitationInboxResult(resultCode, intent);
        } else if (requestCode == RC_WAITING_ROOM) {
            // got the result from the "waiting room" UI.
            if (resultCode == Activity.RESULT_OK) {
                // ready to start playing
                Log.d(TAG, "Starting game (waiting room returned OK).");
                askPermission();
            } else if (resultCode == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
                // player indicated that they want to leave the room
                leaveRoom();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                //TODO handle somehow else?
                leaveRoom();
            }
        }

        super.onActivityResult(requestCode, resultCode, intent);
    }

    /**
    * Handle the result of the "Select players UI" launched when the user clicked the
    * "Invite friend" button, creates a room with these player.
    */
    private void handleSelectPlayersResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
            Log.w(TAG, "*** select players UI cancelled, " + response);
            switchToMainScreen();
            return;
        }

        Log.d(TAG, "Select players UI succeeded.");

        final String invitee = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS).get(0);
        Log.d(TAG, "Invited a player");

        Log.d(TAG, "Creating room...");
        switchToScreen(R.id.screen_wait);
        keepScreenOn();

        mRoomConfig = RoomConfig.builder(mRoomUpdateCallback)
                .addPlayersToInvite(invitee)
                .setOnMessageReceivedListener(mOnRealTimeMessageReceivedListener)
                .setRoomStatusUpdateCallback(mRoomStatusUpdateCallback)
                .build();
        mRealTimeMultiplayerClient.create(mRoomConfig);

        createGameSettings();

        Log.d(TAG, "Room created, waiting for it to be ready...");
    }

    /**
    * Handle the result of the invitation inbox UI, where the player can pick an invitation
    * to accept. React by accepting the selected invitation.
    */
    private void handleInvitationInboxResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
            Log.w(TAG, "*** invitation inbox UI cancelled, " + response);
            switchToMainScreen();
            return;
        }

        Log.d(TAG, "Invitation inbox UI succeeded.");
        Invitation invitation = Objects.requireNonNull(data.getExtras()).getParcelable(Multiplayer.EXTRA_INVITATION);

        // accept invitation
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
                .setOnMessageReceivedListener(mOnRealTimeMessageReceivedListener)
                .setRoomStatusUpdateCallback(mRoomStatusUpdateCallback)
                .build();

        switchToScreen(R.id.screen_wait);
        keepScreenOn();

        mRealTimeMultiplayerClient.join(mRoomConfig)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Room Joined Successfully!");
                    }
                });
    }

    @Override
    public void onStop() {
        Log.d(TAG, "**** got onStop");

        if (debugMode) {
            super.onStop();
            return;
        }

        leaveRoom();
        stopKeepingScreenOn();
        switchToMainScreen();

        super.onStop();
    }

    /**
     * TODO
     */
    private void leaveRoom() {
        setContentView(R.layout.activity_create_room);

        gameSettings = null;

        Log.d(TAG, "Leaving room.");
        stopKeepingScreenOn();
        if (mRoomId != null) {
            mRealTimeMultiplayerClient.leave(mRoomConfig, mRoomId)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            mRoomId = null;
                            mRoomConfig = null;
                        }
                    });
            switchToScreen(R.id.screen_wait);
        } else {
            switchToMainScreen();
        }
    }

    /**
    * Show the waiting room UI to track the progress of other players as they enter the
    * room and get connected.
    */
    private void showWaitingRoom(Room room) {
        mRealTimeMultiplayerClient.getWaitingRoomIntent(room, NUMBER_OF_PLAYERS)
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
     * Called when player get an invitation to play a game, reacts by showing that to the user.
     */
    private InvitationCallback mInvitationCallback = new InvitationCallback() {
        @Override
        public void onInvitationReceived(@NonNull Invitation invitation) {
            mIncomingInvitationId = invitation.getInvitationId();
            ((TextView) findViewById(R.id.incoming_invitation_text)).setText(
                    String.format("%s %s", invitation.getInviter().getDisplayName(), getString(R.string.is_inviting_you)));
            switchToScreen(mCurScreen);
        }

        @Override
        public void onInvitationRemoved(@NonNull String invitationId) {
            if (mIncomingInvitationId != null && mIncomingInvitationId.equals(invitationId)) {
                mIncomingInvitationId = null;
                switchToScreen(mCurScreen);
            }
        }
    };

    /**
     * Player's id.
     */
    private String mPlayerId;

    /**
     * Current account player is signed in.
     */
    private GoogleSignInAccount mSignedInAccount = null;

    private void onConnected(GoogleSignInAccount googleSignInAccount) {
        Log.d(TAG, "onConnected(): connected to Google APIs");
        if (mSignedInAccount != googleSignInAccount) {
            mSignedInAccount = googleSignInAccount;

            mRealTimeMultiplayerClient = Games.getRealTimeMultiplayerClient(this, googleSignInAccount);
            mInvitationsClient = Games.getInvitationsClient(GameActivity.this, googleSignInAccount);

            PlayersClient playersClient = Games.getPlayersClient(this, googleSignInAccount);
            playersClient.getCurrentPlayer()
                    .addOnSuccessListener(new OnSuccessListener<Player>() {
                        @Override
                        public void onSuccess(Player player) {
                            mPlayerId = player.getPlayerId();

                            switchToMainScreen();
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

    private OnFailureListener createFailureListener(final String string) {
        return new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                handleException(e, string);
            }
        };
    }

    private void onDisconnected() {
        Log.d(TAG, "onDisconnected()");

        mRealTimeMultiplayerClient = null;
        mInvitationsClient = null;

        switchToScreen(R.id.screen_sign_in);
    }

    /**
     * Called when we are connected to the room.
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
            showGameError();
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
     * Show error message about game being cancelled and return to main screen.
     */
    void showGameError() {
        switchToMainScreen();

        new AlertDialog.Builder(this)
                .setMessage("We are having problems creating or running your game. Please check your internet connection.")
                .setNeutralButton(android.R.string.ok, null).show();
    }

    private RoomUpdateCallback mRoomUpdateCallback = new RoomUpdateCallback() {
        @Override
        public void onRoomCreated(int statusCode, Room room) {
            Log.d(TAG, "onRoomCreated(" + statusCode + ", " + room + ")");
            if (statusCode != GamesCallbackStatusCodes.OK) {
                Log.e(TAG, "*** Error: onRoomCreated, status " + statusCode);
                showGameError();
                return;
            }

            mRoomId = room.getRoomId();

            showWaitingRoom(room);
        }

        @Override
        public void onRoomConnected(int statusCode, Room room) {
            Log.d(TAG, "onRoomConnected(" + statusCode + ", " + room + ")");
            if (statusCode != GamesCallbackStatusCodes.OK) {
                Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
                showGameError();
                return;
            }
            updateRoom(room);
        }

        @Override
        public void onJoinedRoom(int statusCode, Room room) {
            Log.d(TAG, "onJoinedRoom(" + statusCode + ", " + room + ")");
            if (statusCode != GamesCallbackStatusCodes.OK) {
                Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
                showGameError();
                return;
            }

            showWaitingRoom(room);
        }

        @Override
        public void onLeftRoom(int statusCode, @NonNull String roomId) {
            Log.d(TAG, "onLeftRoom, code " + statusCode);
            switchToMainScreen();
        }
    };

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


    /*
     * VOICE COMMUNICATIONS SECTION.
     */

    /**
     * Ask permission to record voice, if it wasn't given yet.
     * TODO
     */
    private void askPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RC_REQUEST_VOICE_RECORD_PERMISSION);
        } else {
            Log.d(TAG, "Permission already granted");
            prepareConnection();
        }
    }

    /**
     * Start the gameplay phase of the game.
     */
    private void prepareConnection() {
        prepareBroadcastAudio();
        prepareReceiveAudio();
        sendReadyMessage();

        prepared = true;

        synchronized (otherPlayerReadyLock) {
            if (otherPlayerIsReady) {
                startGame();
            }
        }
    }

    /**
     * True if we got message from teammate that he is read to get a voice connection.
     */
    private boolean otherPlayerIsReady = false;

    /**
     * TODO
     */
    private volatile boolean prepared = false;

    /**
     * TODO Change name
     */
    private final Object otherPlayerReadyLock = new Object();


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
                    leaveRoom();
                }

            }
        }
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
                ObjectOutputStream writeStream = null;

                writeStream = new ObjectOutputStream(byteStream);

                gameSettings.flipSettings();
                writeStream.writeObject(gameSettings);
                writeStream.flush();
                gameSettings.flipSettings();

                message = byteStream.toByteArray();
                writeStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                return; //TODO handle this, by leaving the room or something
            }
        } else {
            message = new byte[0];
        }

        if (message.length > Multiplayer.MAX_RELIABLE_MESSAGE_LEN) {
            Log.d(TAG, "Ready message: message is too long.");
        }

        mRealTimeMultiplayerClient.sendReliableMessage(message, mRoomId, teammateParticipant.getParticipantId(), null);
    }

    /**
     * Minimum buffer size to record audio.
     */
    private static final int MIN_BUFFER_SIZE = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);;

    /**
     * Object for streaming audio received as byte buffer.
     */
    private AudioTrack track;

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
     * Message receiver. The very first message is signal of readiness.
     *
     * TODO codes
     */
    private OnRealTimeMessageReceivedListener mOnRealTimeMessageReceivedListener = new OnRealTimeMessageReceivedListener() {
        @Override
        public void onRealTimeMessageReceived(@NonNull RealTimeMessage realTimeMessage) {
            byte[] receivedData = realTimeMessage.getMessageData();

            if (!otherPlayerIsReady) {
                synchronized (otherPlayerReadyLock) {
                    if (gameSettings == null) {
                        try {
                            ObjectInputStream stream = new ObjectInputStream(new ByteArrayInputStream(receivedData));
                            try {
                                gameSettings = (GameSettings) stream.readObject();

                                stream.close();
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace(); //TODO handle
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    otherPlayerIsReady = true;

                    if (prepared) {
                        startGame();
                    }
                }
            } else if (receivedData[0] == 'P') {
                track.write(receivedData, 1, receivedData.length - 1);
            } else if (receivedData[0] == 'L') {
                gameOver();
            } else if (receivedData[0] == 'W') {
                synchronized (otherPlayerWonLock) {
                    otherMazeGameWon = true;

                    if (myMazeGameWon) {
                        startNextGame();
                    }
                }
            }
        }
    };

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
     * Initialise audio recorder and starts thread that sends recorded audio to other player infinitly.
     */
    private void prepareBroadcastAudio () {
        if (teammateParticipant == null || teammateParticipant.getStatus() != Participant.STATUS_JOINED) {
            showGameError();
            leaveRoom();
        }

        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, MIN_BUFFER_SIZE * 10);

        broadcastThread = new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] buffer = new byte[1400]; //TODO constant

                recorder.startRecording();
                while (!Thread.interrupted()) {
                    if (broadcastAudio) {
                        int n = recorder.read(buffer, 1, buffer.length - 1);
                        buffer[0] = 'P';

                        byte[] toSend = new byte[n];
                        System.arraycopy(buffer, 0, toSend, 0, n);

                        mRealTimeMultiplayerClient.sendReliableMessage(toSend, mRoomId, teammateParticipant.getParticipantId(), null);
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
    private void stopBroadcastAudio() {
        broadcastAudio = false;
        recorder.stop();
    }

    /*
     * UI SECTION. Methods that implement the game's UI.
     */

    /**
     * All the individual screens game has.
     */
    private final static int[] SCREENS = {
            R.id.main_screen,
            R.id.screen_sign_in,
            R.id.screen_wait,
            R.id.invitation_popup
    };

    /**
     * Last used screen.
     */
    private int mCurScreen = -1;

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
        if (mIncomingInvitationId == null) {
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
        if (mRealTimeMultiplayerClient != null) {
            switchToScreen(R.id.main_screen);
        } else {
            signInSilently();
            //switchToScreen(R.id.screen_sign_in);
        }
    }

    /*
     * GAME LOGIC SECTION.
     */

    /**
     * TODO
     */
    private void createGameSettings() {
        gameSettings = new GameSettings();

        gameSettings.setMyMazeMap(TestMap.class.getName());
        gameSettings.setTeammateMazeMap(SimpleMap.class.getName()); //TODO smart selection
    }

    /**
     * TODO
     */
    private void startGame() {
        startBroadcastAudio();
        startMazeGame();
    }

    /*
     * MAZE GAME SECTION.
     */

    private MazeGame maze = null;
    private MazeMap map = null;

    /**
     * TODO
     */
    private void startMazeGame() {
        setContentView(R.layout.activity_maze_game);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.upButton:
                        maze.react(MazeGame.Command.UP);
                        break;
                    case R.id.downButton:
                        maze.react(MazeGame.Command.DOWN);
                        break;
                    case R.id.leftButton:
                        maze.react(MazeGame.Command.LEFT);
                        break;
                    case R.id.rightButton:
                        maze.react(MazeGame.Command.RIGHT);
                        break;
                    case R.id.useButton:
                        maze.react(MazeGame.Command.USE);
                        break;
                    default:
                        break;
                }
            }
        };

        ((Button)findViewById(R.id.upButton)).setOnClickListener(onClickListener);
        ((Button)findViewById(R.id.leftButton)).setOnClickListener(onClickListener);
        ((Button)findViewById(R.id.rightButton)).setOnClickListener(onClickListener);
        ((Button)findViewById(R.id.downButton)).setOnClickListener(onClickListener);
        ((Button)findViewById(R.id.useButton)).setOnClickListener(onClickListener);

        Log.d(TAG, "Loaded maps are " + gameSettings.getMyMazeMap() + " " + gameSettings.getTeammateMazeMap());

        try {
            map = (MazeMap) getClassLoader().loadClass(gameSettings.getMyMazeMap()).getDeclaredConstructor(GameActivity.class).newInstance(this);
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

        if (map == null) {
            Log.d(TAG, "failed to create the map");
        }

        maze = new MazeGame(map, this);
        map.draw();

        Log.d(TAG, "Switched to maze game");
    }

    /**
     * TODO
     */
    private final Object otherPlayerWonLock = new Object();

    /**
     * TODO
     */
    private boolean myMazeGameWon = false;

    /**
     * TODO
     */
    private boolean otherMazeGameWon = false;

    /**
     * TODO
     */
    public void onMazeGameWon() {
        synchronized (otherPlayerWonLock) {
            myMazeGameWon = true;

            sendWonMessage();

            if (otherMazeGameWon) {
                Log.d(TAG, "maze game won");
                startNextGame();
            }
        }
    }

    /**
     * TODO
     */
    public void onMazeGameLost() {
        sendLostMessage();
        gameOver();
        Log.d(TAG, "maze game lost");
    }

    /**
     * TODO
     */
    private void gameOver() {
        //TODO
        leaveRoom();
        maze.onClose();
        broadcastThread.interrupt();
        
        //Intent intent = new Intent(this, GameLostActivity.class);
        //startActivity(intent);
        Toast.makeText(this, "lost", Toast.LENGTH_LONG).show();
    }

    private void sendReliableMessage(byte[] message) {
        mRealTimeMultiplayerClient.sendReliableMessage(message, mRoomId, teammateParticipant.getParticipantId(), null);
    }

    private void sendLostMessage() {
        byte[] message = new byte[1];
        message[0] = 'L';
        sendReliableMessage(message);
    }

    private void sendWonMessage() {
        byte[] message = new byte[1];
        message[0] = 'W';
        sendReliableMessage(message);
    }

    /*
    Next game section.
     */

    private void startNextGame() {

    }

    /* TODO
     * MISC SECTION. Miscellaneous methods.
     */


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

    /**
     * TODO
     */
    private static class GameSettings implements Serializable {
        private String myMazeMap;
        private String teammateMazeMap;

        private void writeObject(java.io.ObjectOutputStream out)
                throws IOException {
            out.writeUTF(myMazeMap);
            out.writeUTF(teammateMazeMap);
        }

        private void readObject(java.io.ObjectInputStream in)
                throws IOException, ClassNotFoundException {
            myMazeMap = in.readUTF();
            teammateMazeMap = in.readUTF();
        }

        public String getMyMazeMap() {
            return myMazeMap;
        }

        public void setMyMazeMap(String myMazeMap) {
            this.myMazeMap = myMazeMap;
        }

        public String getTeammateMazeMap() {
            return teammateMazeMap;
        }

        public void setTeammateMazeMap(String teammateMazeMap) {
            this.teammateMazeMap = teammateMazeMap;
        }

        private void flipSettings() {
            String temp = myMazeMap;
            myMazeMap = teammateMazeMap;
            teammateMazeMap = temp;
        }
    }
}