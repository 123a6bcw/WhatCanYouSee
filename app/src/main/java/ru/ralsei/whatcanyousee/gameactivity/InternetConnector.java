package ru.ralsei.whatcanyousee.gameactivity;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.realtime.OnRealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * Class for managing internet messaging between player's.
 */
class InternetConnector {
    private String TAG = "What can you see: Internet connector";

    private GameActivity activity;

    InternetConnector(GameActivity activity) {
        this.activity = activity;
    }

    /**
     * True if got message from teammate that he is ready to get a voice connection.
     */
    @Getter(AccessLevel.PACKAGE)
    private boolean otherPlayerIsReady = false;

    /**
     * True if ready to send and receive voice.
     */
    @Setter(AccessLevel.PACKAGE)
    private boolean prepared = false;

    /**
     * Reset prepared status for the next created game.
     */
    void clearResources() {
        otherPlayerIsReady = false;
        prepared = false;
    }

    /**
     * Send to other player that we are ready to receive his voice.
     * Also sends game settings if we are the game host.
     */
    void sendReadyMessage() {
        byte[] message;

        GameplayHandler.GameSettings gameSettings = activity.getGameplayHandler().getGameSettings();
        if (gameSettings != null) {
            try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                 ObjectOutputStream writeStream = new ObjectOutputStream(byteStream)) {

                gameSettings.flipSettings();
                writeStream.writeObject(gameSettings);
                writeStream.flush();
                gameSettings.flipSettings();

                message = byteStream.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
                activity.getUiHandler().showGameError();
                activity.getGooglePlayHandler().leaveRoom();
                return;
            }
        } else {
            message = new byte[1];
            message[0] = 'R';
        }

        if (message.length > Multiplayer.MAX_RELIABLE_MESSAGE_LEN) {
            Log.d(TAG, "Ready message: message is too long.");
        }

        sendReliableMessageToTeammate(message);
    }

    private void reactOnReceivedMessage(byte[] receivedData) {
        if (!otherPlayerIsReady) {
            if (activity.getGameplayHandler().getGameSettings() == null) {
                try (ObjectInputStream stream = new ObjectInputStream(new ByteArrayInputStream(receivedData))) {
                    activity.getGameplayHandler().setGameSettings((GameplayHandler.GameSettings) stream.readObject());
                } catch (IOException | ClassNotFoundException e) {
                    activity.handleException(new IOException(), "Error reading from object input stream");
                }
            }

            otherPlayerIsReady = true;

            if (prepared) {
                activity.getGameplayHandler().startGame();
            }
        } else if (receivedData[0] == 'L') {
            //Other player lost on his map.
            if (receivedData[1] == 'M') {
                activity.getGameplayHandler().gameOver(true, "You can not survive alone...");
            } else if (receivedData[1] == 'C') {
                activity.getGameplayHandler().gameOver(true, "You can not survive alone...");
            } else if (receivedData[1] == 'L') {
                activity.getGameStatistic().setTeammateKilledInCodeGame(true);
                activity.getGameplayHandler().gameOver(true, "You can not survive alone...");
            } else {
                Log.d(TAG, "wrong game code in message");
            }
        } else if (receivedData[0] == 'W') {
            //Other player won on his map.
            if (receivedData[1] == 'M') {
                activity.getGameplayHandler().setOtherMazeGameWon(true);

                if (activity.getGameplayHandler().isMyMazeGameWon()) {
                    activity.getGameplayHandler().startCodeGame();
                }
            } else if (receivedData[1] == 'C') {
                activity.getGameplayHandler().setOtherCodeGameWon(true);

                if (activity.getGameplayHandler().isMyCodeGameWon()) {
                    activity.getGameplayHandler().startLeverGame();
                }
            } else if (receivedData[1] == 'L') {
                activity.getGameplayHandler().setOtherLeverGameWon(true);

                if (activity.getGameplayHandler().isMyLeverGameWon()) {
                    activity.getGameplayHandler().onWinningEntireGame();
                }
            } else {
                Log.d(TAG, "wrong game code");
            }
        } else if (receivedData[0] == 'S') {
            if (activity.getGameplayHandler().getLeverGameMap() == null) {
                return;
            }

            byte[] leverName = new byte[receivedData.length - 1];
            System.arraycopy(receivedData, 1, leverName, 0, leverName.length);

            String lever = new String(leverName);
            Log.d(TAG, "Received pressed lever: " + lever);

            activity.getGameplayHandler().getLeverGameMap().applyLever(lever);
        }
    }

    @Getter(AccessLevel.PACKAGE)
    private OnRealTimeMessageReceivedListener onRealTimeMessageReceivedListener = new OnRealTimeMessageReceivedListener() {
        @Override
        public void onRealTimeMessageReceived(@NonNull RealTimeMessage realTimeMessage) {
            final byte[] receivedData = realTimeMessage.getMessageData();

            if (receivedData.length > 0 && receivedData[0] == 'P') {
                //Received voice audio.
                if (activity.getAudioConnector().getTrack() != null) {
                    activity.getAudioConnector().getTrack().write(receivedData, 1, receivedData.length - 1);
                }
            } else {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        reactOnReceivedMessage(receivedData);
                    }
                });
            }
        }
    };

    void sendReliableMessageToTeammate(byte[] message) {
        activity.getGooglePlayHandler().getRealTimeMultiplayerClient().sendReliableMessage(message, activity.getGooglePlayHandler().getRoomId(), activity.getGooglePlayHandler().getTeammateParticipant().getParticipantId(), null);
    }

    void sendMazeLostMessage() {
        byte[] message = new byte[2];
        message[0] = 'L';
        message[1] = 'M';
        sendReliableMessageToTeammate(message);
    }

    void sendMazeWonMessage() {
        byte[] message = new byte[2];
        message[0] = 'W';
        message[1] = 'M';
        sendReliableMessageToTeammate(message);
    }

    void sendCodeLostMessage() {
        byte[] message = new byte[2];
        message[0] = 'L';
        message[1] = 'C';
        sendReliableMessageToTeammate(message);
    }

    void sendCodeWonMessage() {
        byte[] message = new byte[2];
        message[0] = 'W';
        message[1] = 'C';
        sendReliableMessageToTeammate(message);
    }

    void sendLeverLostMessage() {
        byte[] message = new byte[2];
        message[0] = 'L';
        message[1] = 'L';
        sendReliableMessageToTeammate(message);
    }

    void sendLeverWonMessage() {
        byte[] message = new byte[2];
        message[0] = 'W';
        message[1] = 'L';
        sendReliableMessageToTeammate(message);
    }

    /**
     * Send the name of the lever that we have pressed on our screen.
     */
    void sendLeverPressedMessage(String lever) {
        sendReliableMessageToTeammate(("S" + lever).getBytes());
    }
}