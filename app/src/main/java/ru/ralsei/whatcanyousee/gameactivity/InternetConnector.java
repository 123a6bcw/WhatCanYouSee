package ru.ralsei.whatcanyousee.gameactivity;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.realtime.OnRealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

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
        byte[] messageData;

        GameplayHandler.GameSettings gameSettings = activity.getGameplayHandler().getGameSettings();
        if (gameSettings != null) {
            try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                 ObjectOutputStream writeStream = new ObjectOutputStream(byteStream)) {

                gameSettings.flipSettings();
                writeStream.writeObject(gameSettings);
                writeStream.flush();
                gameSettings.flipSettings();

                messageData = byteStream.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
                activity.getUiHandler().showGameError();
                activity.getGooglePlayHandler().leaveRoom();
                return;
            }
        } else {
            messageData = new byte[0];
        }

        if (messageData.length > Multiplayer.MAX_RELIABLE_MESSAGE_LEN) {
            Log.d(TAG, "Ready message: message is too long.");
        }

        sendReliableMessageToTeammate(new Message(Message.MessageType.READY, messageData));
    }

    /**
     * @param receivedMessage is an instance of Message.
     */
    private void reactOnReceivedMessage(byte[] receivedMessage) {
        Message message;
        try (ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(receivedMessage))) {
            message = (Message) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }

        switch (message.messageType) {
            case READY:
                if (activity.getGameplayHandler().getGameSettings() == null) {
                    try (ObjectInputStream stream = new ObjectInputStream(new ByteArrayInputStream(message.data))) {
                        activity.getGameplayHandler().setGameSettings((GameplayHandler.GameSettings) stream.readObject());
                    } catch (IOException | ClassNotFoundException e) {
                        activity.handleException(new IOException(), "Error reading from object input stream");
                    }
                }

                otherPlayerIsReady = true;

                if (prepared) {
                    activity.getGameplayHandler().startGame();
                }

                break;

            case WON_MAZE:
                activity.getGameplayHandler().setOtherMazeGameWon(true);

                if (activity.getGameplayHandler().isMyMazeGameWon()) {
                    activity.getGameplayHandler().startCodeGame();
                }
                break;

            case WON_CODE:
                activity.getGameplayHandler().setOtherCodeGameWon(true);

                if (activity.getGameplayHandler().isMyCodeGameWon()) {
                    activity.getGameplayHandler().startLeverGame();
                }
                break;

            case WON_LEVER:
                activity.getGameplayHandler().setOtherLeverGameWon(true);

                if (activity.getGameplayHandler().isMyLeverGameWon()) {
                    activity.getGameplayHandler().onWinningEntireGame();
                }
                break;

            case LOST_MAZE:
                activity.getGameplayHandler().gameOver(true, "You can not survive alone...");
                break;

            case LOST_CODE:
                activity.getGameplayHandler().gameOver(true, "You can not survive alone...");
                break;

            case LOST_LEVER:
                activity.getGameStatistic().setTeammateKilledInCodeGame(true);
                activity.getGameplayHandler().gameOver(true, "You can not survive alone...");
                break;

            case LEVER_PRESSED:
                String lever = new String(message.data);
                Log.d(TAG, "Received pressed lever: " + lever);

                activity.getGameplayHandler().getLeverGameMap().applyLever(lever);
                break;
        }
    }

    @Getter(AccessLevel.PACKAGE)
    private OnRealTimeMessageReceivedListener onRealTimeMessageReceivedListener = new OnRealTimeMessageReceivedListener() {
        @Override
        public void onRealTimeMessageReceived(@NonNull RealTimeMessage realTimeMessage) {
            final byte[] receivedData = realTimeMessage.getMessageData();

            if (receivedData.length > 0 && receivedData[0] == 'P') {
                /*
                Received data is either voice audio data or serialized Message object. Since
                Serialized object always starts with AC ED bytes, it's safe to code voice audio to begin with 'P'.
                 */
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

    private void sendReliableMessageToTeammate(Message message) {
        byte[] data;

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(message);
            data = byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        sendReliableMessageToTeammate(data);
    }

    private void sendReliableMessageToTeammate(byte[] data) {
        activity.getGooglePlayHandler().getRealTimeMultiplayerClient().sendReliableMessage(data, activity.getGooglePlayHandler().getRoomId(), activity.getGooglePlayHandler().getTeammateParticipant().getParticipantId(), null);
    }

    void sendMazeLostMessage() {
        sendReliableMessageToTeammate(new Message(Message.MessageType.LOST_MAZE));
    }

    void sendMazeWonMessage() {
        sendReliableMessageToTeammate(new Message(Message.MessageType.WON_MAZE));
    }

    void sendCodeLostMessage() {
        sendReliableMessageToTeammate(new Message(Message.MessageType.LOST_CODE));
    }

    void sendCodeWonMessage() {
        sendReliableMessageToTeammate(new Message(Message.MessageType.WON_CODE));
    }

    void sendLeverLostMessage() {
        sendReliableMessageToTeammate(new Message(Message.MessageType.LOST_LEVER));
    }

    void sendLeverWonMessage() {
        sendReliableMessageToTeammate(new Message(Message.MessageType.WON_LEVER));
    }

    /**
     * Send the name of the lever that we have pressed on our screen.
     */
    void sendLeverPressedMessage(String lever) {
        sendReliableMessageToTeammate(new Message(Message.MessageType.LEVER_PRESSED, lever.getBytes()));
    }

    void sendVoiceMessageToTeammate(byte[] voice) {
        sendReliableMessageToTeammate(voice);
    }

    private static class Message implements Serializable {
        private enum MessageType {
            LEVER_PRESSED, READY,
            WON_MAZE, WON_CODE, WON_LEVER,
            LOST_MAZE, LOST_CODE, LOST_LEVER,
        }

        private MessageType messageType;
        private byte[] data;

        private Message(MessageType messageType) {
            this.messageType = messageType;
            this.data = new byte[0];
        }

        private Message(MessageType messageType, byte[] data) {
            this.messageType = messageType;
            this.data = data;
        }
    }
}