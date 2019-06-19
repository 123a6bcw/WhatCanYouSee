package ru.ralsei.whatcanyousee.gameactivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesCallbackStatusCodes;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.InvitationsClient;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.PlayersClient;
import com.google.android.gms.games.RealTimeMultiplayerClient;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.InvitationCallback;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateCallback;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.List;
import java.util.Objects;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import ru.ralsei.whatcanyousee.R;

/**
 * Class for handling google play connection (disconnection), managing game room.
 */
class GooglePlayHandler {
    private String TAG = "What can you see: googleplay handler";

    private GameActivity activity;

    GooglePlayHandler(GameActivity activity) {
        this.activity = activity;
    }

    static final int MAX_RELIABLE_BUFFER_SIZE = 1400;

    /**
     * Current account player is signed in.
     */
    private GoogleSignInAccount signedInAccount;

    @Getter(AccessLevel.PACKAGE) @Setter(AccessLevel.PACKAGE)
    private GoogleSignInClient googleSignInClient;

    @Getter(AccessLevel.PACKAGE)
    private RealTimeMultiplayerClient realTimeMultiplayerClient;

    @Getter(AccessLevel.PACKAGE)
    private AchievementsClient achievementsClient;

    private LeaderboardsClient leaderboardsClient;

    @Getter(AccessLevel.PACKAGE)
    private InvitationsClient invitationsClient;

    /**
     * Room ID where the currently active game is taking place.
     */
    @Getter(AccessLevel.PACKAGE)
    private String roomId;

    private RoomConfig roomConfig;

    @Getter(AccessLevel.PACKAGE)
    private Participant teammateParticipant;

    /**
     * Player's participant ID in the currently active game.
     */
    private String myId;

    /**
     * Id of the invitation received via the
     * invitation listener.
     */
    @Getter(AccessLevel.PACKAGE) @Setter(AccessLevel.PACKAGE)
    private String incomingInvitationId;

    private String playerId;

    void startSignInIntent() {
        activity.startActivityForResult(googleSignInClient.getSignInIntent(), GameActivity.RC_SIGN_IN);
    }

    /**
     * Try to sign in without displaying dialogs to the user.
     * If the user has already signed in previously, it will not show dialog.
     */
    void signInSilently() {
        Log.d(TAG, "signInSilently()");

        googleSignInClient.silentSignIn().addOnCompleteListener(activity,
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

    void signOut() {
        Log.d(TAG, "signOut()");

        googleSignInClient.signOut().addOnCompleteListener(activity,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signOut(): success");
                        } else {
                            activity.handleException(task.getException(), "signOut() failed!");
                        }

                        onDisconnected();
                    }
                });
    }

    /**
     * Handle the result of the "Select players UI" launched when the user clicked the
     * "Invite friend" button, creates a room with these player.
     */
    void handleSelectPlayersResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
            Log.w(TAG, "*** select players UI cancelled, " + response);
            activity.getUiHandler().switchToMainScreen();
            return;
        }

        Log.d(TAG, "Select players UI succeeded.");

        final String invitee = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS).get(0);
        Log.d(TAG, "Invited a player");

        Log.d(TAG, "Creating room...");
        activity.getUiHandler().switchToScreen(R.id.screen_wait);
        activity.getUiHandler().keepScreenOn();

        roomConfig = RoomConfig.builder(roomUpdateCallback)
                .addPlayersToInvite(invitee)
                .setOnMessageReceivedListener(activity.getInternetConnector().getOnRealTimeMessageReceivedListener())
                .setRoomStatusUpdateCallback(roomStatusUpdateCallback)
                .build();
        realTimeMultiplayerClient.create(roomConfig);

        activity.getGameplayHandler().createGameSettings();

        Log.d(TAG, "Room created, waiting for it to be ready...");
    }

    /**
     * Handle the result of the invitation inbox UI, where the player can pick an invitation
     * to accept. React by accepting the selected invitation.
     */
    void handleInvitationInboxResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
            Log.w(TAG, "*** invitation inbox UI cancelled, " + response);
            activity.getUiHandler().switchToMainScreen();
            return;
        }

        Log.d(TAG, "Invitation inbox UI succeeded.");
        Invitation invitation = Objects.requireNonNull(data.getExtras()).getParcelable(Multiplayer.EXTRA_INVITATION);

        if (invitation != null) {
            acceptInviteToRoom(invitation.getInvitationId());
        }
    }

    void acceptInviteToRoom(String invitationId) {
        Log.d(TAG, "Accepting invitation: " + invitationId);

        roomConfig = RoomConfig.builder(roomUpdateCallback)
                .setInvitationIdToAccept(invitationId)
                .setOnMessageReceivedListener(activity.getInternetConnector().getOnRealTimeMessageReceivedListener())
                .setRoomStatusUpdateCallback(roomStatusUpdateCallback)
                .build();

        activity.getUiHandler().switchToScreen(R.id.screen_wait);
        activity.getUiHandler().keepScreenOn();

        realTimeMultiplayerClient.join(roomConfig)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Room Joined Successfully!");
                    }
                });
    }

    void leaveRoom() {
        activity.setState(GameActivity.State.MAIN_MENU);

        activity.setContentView(R.layout.activity_game);
        activity.setupListeners();

        activity.clearAllResources();

        Log.d(TAG, "Leaving room.");
        activity.getUiHandler().stopKeepingScreenOn();
        if (roomId != null) {
            realTimeMultiplayerClient.leave(roomConfig, roomId)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            roomId = null;
                            roomConfig = null;
                            activity.getUiHandler().switchToMainScreen();
                        }
                    });
        } else {
            activity.getUiHandler().switchToMainScreen();
        }
    }

    /**
     * Calls after connecting to the google play account.
     */
    void onConnected(GoogleSignInAccount googleSignInAccount) {
        Log.d(TAG, "onConnected(): connected to Google APIs");
        if (signedInAccount != googleSignInAccount) {
            signedInAccount = googleSignInAccount;

            realTimeMultiplayerClient = Games.getRealTimeMultiplayerClient(activity, googleSignInAccount);
            invitationsClient = Games.getInvitationsClient(activity, googleSignInAccount);

            achievementsClient = Games.getAchievementsClient(activity, googleSignInAccount);
            leaderboardsClient = Games.getLeaderboardsClient(activity, googleSignInAccount);

            PlayersClient playersClient = Games.getPlayersClient(activity, googleSignInAccount);
            playersClient.getCurrentPlayer()
                    .addOnSuccessListener(new OnSuccessListener<Player>() {
                        @Override
                        public void onSuccess(Player player) {
                            playerId = player.getPlayerId();

                            activity.getUiHandler().switchToMainScreen();
                        }
                    })
                    .addOnFailureListener(activity.createFailureListener("There was a problem getting the player id!"));
        }

        invitationsClient.registerInvitationCallback(invitationCallback);

        GamesClient gamesClient = Games.getGamesClient(activity, googleSignInAccount);
        gamesClient.getActivationHint()
                .addOnSuccessListener(new OnSuccessListener<Bundle>() {
                    @Override
                    public void onSuccess(Bundle hint) {
                        if (hint == null) {
                            return;
                        }

                        Invitation invitation =
                                hint.getParcelable(Multiplayer.EXTRA_INVITATION);

                        if (invitation == null || invitation.getInvitationId() == null) {
                            return;
                        }

                        // retrieve and cache the invitation ID
                        Log.d(TAG, "onConnected: connection hint has a room invite!");
                        acceptInviteToRoom(invitation.getInvitationId());
                    }
                })
                .addOnFailureListener(activity.createFailureListener("There was a problem getting the activation hint!"));
    }

    /**
     * Calls after disconnecting from the google play account (etc after internet connection lost).
     */
    void onDisconnected() {
        Log.d(TAG, "onDisconnected()");

        realTimeMultiplayerClient = null;
        invitationsClient = null;

        achievementsClient = null;
        leaderboardsClient = null;

        activity.clearAllResources();
        activity.setContentView(R.layout.activity_game);
        activity.setupListeners();
        activity.getUiHandler().switchToScreen(R.id.screen_sign_in);
    }

    private RoomStatusUpdateCallback roomStatusUpdateCallback = new RoomStatusUpdateCallback() {
        @Override
        public void onConnectedToRoom(Room room) {
            Log.d(TAG, "onConnectedToRoom.");

            myId = room.getParticipantId(playerId);

            if (roomId == null) {
                roomId = room.getRoomId();
            }

            Log.d(TAG, "Room ID: " + roomId);
            Log.d(TAG, "My ID " + myId);
            Log.d(TAG, "<< CONNECTED TO ROOM>>");
        }

        @Override
        public void onDisconnectedFromRoom(Room room) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    roomId = null;
                    roomConfig = null;
                    activity.getUiHandler().showGameError();
                }
            });
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
            Log.d(TAG, "P2P disconnected");
        }

        @Override
        public void onP2PConnected(@NonNull String participant) {
            Log.d(TAG, "P2P disconnected");
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
    @Getter(AccessLevel.PACKAGE)
    private InvitationCallback invitationCallback = new InvitationCallback() {
        @Override
        public void onInvitationReceived(@NonNull Invitation invitation) {
            incomingInvitationId = invitation.getInvitationId();
            ((TextView) activity.findViewById(R.id.incoming_invitation_text)).setText(
                    String.format("%s %s", invitation.getInviter().getDisplayName(), activity.getString(R.string.is_inviting_you)));
            activity.getUiHandler().switchToScreen(activity.getUiHandler().getLastUsedScreen());
        }

        @Override
        public void onInvitationRemoved(@NonNull String invitationId) {
            if (incomingInvitationId != null && incomingInvitationId.equals(invitationId)) {
                incomingInvitationId = null;
                activity.getUiHandler().switchToScreen(activity.getUiHandler().getLastUsedScreen());
            }
        }
    };

    private RoomUpdateCallback roomUpdateCallback = new RoomUpdateCallback() {
        @Override
        public void onRoomCreated(int statusCode, final Room room) {
            Log.d(TAG, "onRoomCreated(" + statusCode + ", " + room + ")");
            if (statusCode != GamesCallbackStatusCodes.OK) {
                Log.e(TAG, "*** Error: onRoomCreated, status " + statusCode);
                activity.getUiHandler().showGameError();
                return;
            }

            roomId = room.getRoomId();

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.getUiHandler().showWaitingRoom(room);
                }
            });
        }

        @Override
        public void onRoomConnected(int statusCode, Room room) {
            Log.d(TAG, "onRoomConnected(" + statusCode + ", " + room + ")");
            if (statusCode != GamesCallbackStatusCodes.OK) {
                Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
                activity.getUiHandler().showGameError();
                return;
            }
            updateRoom(room);
        }

        @Override
        public void onJoinedRoom(int statusCode, final Room room) {
            Log.d(TAG, "onJoinedRoom(" + statusCode + ", " + room + ")");
            if (statusCode != GamesCallbackStatusCodes.OK) {
                Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
                activity.getUiHandler().showGameError();
                return;
            }

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.getUiHandler().showWaitingRoom(room);
                }
            });
        }

        @Override
        public void onLeftRoom(int statusCode, @NonNull String roomId) {
            Log.d(TAG, "onLeftRoom, code " + statusCode);

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.clearAllResources();
                    activity.setContentView(R.layout.activity_game);
                    activity.setupListeners();
                    activity.getUiHandler().switchToMainScreen();
                }
            });
        }
    };

    void onShowAchievementsRequested() {
        achievementsClient.getAchievementsIntent()
                .addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        activity.startActivityForResult(intent, GameActivity.RC_UNUSED);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        activity.handleException(e, activity.getString(R.string.achievements_exception));
                    }
                });
    }

    void onShowLeaderboardsRequested() {
        leaderboardsClient.getAllLeaderboardsIntent()
                .addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        activity.startActivityForResult(intent, GameActivity.RC_UNUSED);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        activity.handleException(e, activity.getString(R.string.leaderboards_exception));
                    }
                });
    }

    void pushStatisticToCloud() {
        if (GoogleSignIn.getLastSignedInAccount(activity) == null) {
            return;
        }

        if (activity.getGameStatistic().getMazeGameTime() != -1) {
            leaderboardsClient.submitScore(activity.getString(R.string.leaderboard_maze_game_best_time), activity.getGameStatistic().getMazeGameTime());
        }

        if (activity.getGameStatistic().getCodeGameTime() != -1) {
            leaderboardsClient.submitScore(activity.getString(R.string.leaderboard_code_game_best_time), activity.getGameStatistic().getCodeGameTime());
        }

        if (activity.getGameStatistic().getLeverGameTime() != -1) {
            leaderboardsClient.submitScore(activity.getString(R.string.leaderboard_lever_game_best_time), activity.getGameStatistic().getLeverGameTime());
        }

        if (activity.getGameStatistic().getCodeGameMistakeTaken() != -1) {
            leaderboardsClient.submitScore(activity.getString(R.string.leaderboard_code_game_least_mistake_taken), activity.getGameStatistic().getCodeGameMistakeTaken());
        }

    }

    /**
     * Get (or remove) teammate's Participant account.
     */
    private void updateRoom(Room room) {
        if (room == null) {
            return;
        }

        List<Participant> participants = room.getParticipants();

        teammateParticipant = null;

        for (Participant participant : participants) {
            if (!participant.getParticipantId().equals(myId)) {
                teammateParticipant = participant;
            }
        }
    }
}