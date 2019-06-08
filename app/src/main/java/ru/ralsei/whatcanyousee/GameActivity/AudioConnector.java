package ru.ralsei.whatcanyousee.GameActivity;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;

import com.google.android.gms.games.multiplayer.Participant;

/**
 * Class for managing voice connection between players.
 */
class AudioConnector {
    private String TAG = "What can you see: audio connector";

    private GameActivity activity;

    AudioConnector(GameActivity activity) {
        this.activity = activity;
    }

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
    void prepareReceiveAudio() {
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
    void prepareBroadcastAudio() {
        if (activity.getGooglePlayHandler().getTeammateParticipant() == null || activity.getGooglePlayHandler().getTeammateParticipant().getStatus() != Participant.STATUS_JOINED) {
            activity.getUIHandler().showGameError();
            activity.getGooglePlayHandler().leaveRoom();
        }

        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, MIN_BUFFER_SIZE * 10);

        broadcastThread = new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] buffer = new byte[GooglePlayHandler.MAX_RELIABLE_BUFFER_SIZE];

                recorder.startRecording();
                while (!Thread.interrupted()) {
                    if (broadcastAudio) {
                        int n = recorder.read(buffer, 1, buffer.length - 1);
                        buffer[0] = 'P';

                        byte[] toSend = new byte[n];
                        System.arraycopy(buffer, 0, toSend, 0, n);

                        activity.getInternetConnector().sendReliableMessage(toSend);
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
    void startBroadcastAudio() {
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
    void clearResources() {
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

    AudioTrack getTrack() {
        return track;
    }
}
