package ru.ralsei.whatcanyousee.gameactivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.tasks.OnSuccessListener;

import lombok.AccessLevel;
import lombok.Getter;
import ru.ralsei.whatcanyousee.R;

/**
 * Handles user interface in GameActivity.
 */
class UIHandler {
    /**
     * All the individual screens game has.
     */
    final int[] SCREENS = {
            R.id.main_screen,
            R.id.screen_sign_in,
            R.id.screen_wait,
            R.id.invitation_popup
    };

    /**
     * TAG to use in logs.
     */
    private String TAG = "What can you see: UI handler";

    private GameActivity activity;

    UIHandler(GameActivity activity) {
        this.activity = activity;
    }

    @Getter(AccessLevel.PACKAGE)
    private int lastUsedScreen = -1;

    void switchToScreen(int screenId) {
        for (int id : SCREENS) {
            final View view = activity.findViewById(id);
            if (view != null) {
                view.setVisibility(screenId == id ? View.VISIBLE : View.GONE);
            } else {
                Log.d(TAG, "Somehow view with id " + id + " was not found");
            }
        }
        lastUsedScreen = screenId;

        boolean showInvPopup;
        if (activity.getGooglePlayHandler().getIncomingInvitationId() == null) {
            showInvPopup = false;
        } else {
            showInvPopup = (lastUsedScreen == R.id.main_screen);
        }

        View view = activity.findViewById(R.id.invitation_popup);
        if (view != null) {
            view.setVisibility(showInvPopup ? View.VISIBLE : View.GONE);
        }
    }

    void switchToMainScreen() {
        activity.setState(GameActivity.State.MAIN_MENU);

        if (activity.getGooglePlayHandler().getRealTimeMultiplayerClient() != null) {
            switchToScreen(R.id.main_screen);
        } else {
            activity.getGooglePlayHandler().signInSilently();
        }
    }

    /**
     * Show the waiting room UI to track the progress of other players as they enter the
     * room and get connected.
     */
    void showWaitingRoom(Room room) {
        activity.getGooglePlayHandler().getRealTimeMultiplayerClient().getWaitingRoomIntent(room, GameActivity.NUMBER_OF_PLAYERS)
                .addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        // show waiting room UI
                        activity.startActivityForResult(intent, GameActivity.RC_WAITING_ROOM);
                    }
                })
                .addOnFailureListener(activity.createFailureListener("There was a problem getting the waiting room!"));
    }

    /**
     * Show error message about game being cancelled and return to main screen.
     */
    void showGameError() {
        activity.getGooglePlayHandler().leaveRoom();

        switchToMainScreen();

        new AlertDialog.Builder(activity)
                .setMessage("We are having problems creating or running your game. Please check your internet connection.")
                .setNeutralButton(android.R.string.ok, null).show();
    }

    void askPermissionToRecordVoice() {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.RECORD_AUDIO}, GameActivity.RC_REQUEST_VOICE_RECORD_PERMISSION);
        } else {
            Log.d(TAG, "Permission already granted");
            activity.prepareConnection();
        }
    }

    void keepScreenOn() {
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    void stopKeepingScreenOn() {
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}