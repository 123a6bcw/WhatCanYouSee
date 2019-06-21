package ru.ralsei.whatcanyousee.gameactivity;

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
    private static final int SAMPLE_RATE_IN_HZ = 8000;
    private static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT; //The only encoding with compression that can be used on api lever 24.

    private String TAG = "What can you see: audio connector";

    private GameActivity activity;

    AudioConnector(GameActivity activity) {
        this.activity = activity;
    }

    /**
     * Minimum buffer size to record audio.
     */
    private final int MIN_BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_MONO, ENCODING);

    /**
     * Object for streaming audio received as byte buffer.
     */
    private AudioTrack track;

    /**
     * True if player broadcasts his voice, if false --- voice recording pauses.
     */
    private volatile boolean broadcastAudio = false;

    private Thread broadcastThread;

    /**
     * Voice recorder that translate input voice into byte buffer.
     */
    private AudioRecord recorder;

    void prepareReceiveAudio() {
        track = new AudioTrack(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build(),
                new AudioFormat.Builder()
                        .setEncoding(ENCODING)
                        .setSampleRate(SAMPLE_RATE_IN_HZ)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build(), MIN_BUFFER_SIZE * 10, AudioTrack.MODE_STREAM, AudioManager.AUDIO_SESSION_ID_GENERATE);
        track.play();

        Log.d(TAG, "Audio reception prepared");
    }

    /**
     * Initialise audio recorder and starts thread that sends recorded audio to other player infinitely.
     */
    void prepareBroadcastAudio() {
        if (activity.getGooglePlayHandler().getTeammateParticipant() == null || activity.getGooglePlayHandler().getTeammateParticipant().getStatus() != Participant.STATUS_JOINED) {
            activity.getUiHandler().showGameError();
            activity.getGooglePlayHandler().leaveRoom();
        }

        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_MONO, ENCODING, MIN_BUFFER_SIZE * 10);

        broadcastThread = new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] buffer = new byte[GooglePlayHandler.MAX_RELIABLE_BUFFER_SIZE];

                while (!Thread.interrupted()) {
                    synchronized (AudioConnector.this) {
                        while (!broadcastAudio) {
                            try {
                                if (recorder.getState() == AudioRecord.RECORDSTATE_RECORDING) {
                                    recorder.stop();
                                }

                                AudioConnector.this.wait();
                            } catch (InterruptedException e) {
                                return;
                            }
                        }

                        if (recorder.getState() == AudioRecord.RECORDSTATE_STOPPED) {
                            recorder.startRecording();
                        }

                        int n = recorder.read(buffer, 1, buffer.length - 1);
                        buffer[0] = 'P';

                        byte[] toSend = new byte[n];
                        System.arraycopy(buffer, 0, toSend, 0, n);

                        activity.getInternetConnector().sendVoiceMessageToTeammate(toSend);
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
    synchronized void startBroadcastAudio() {
        broadcastAudio = true;
        recorder.startRecording();
        notifyAll();
    }

    /**
     * Unused feature to stop recording voice (i.e. may be button with "start/stop recording voice").
     */
    @SuppressWarnings("unused")
    private synchronized void stopBroadcastAudio() {
        broadcastAudio = false;
    }

    void clearResources() {
        stopBroadcastAudio();

        if (broadcastThread != null) {
            broadcastThread.interrupt();
            try {
                broadcastThread.join();
            } catch (InterruptedException ignored) {
            }
        }

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
