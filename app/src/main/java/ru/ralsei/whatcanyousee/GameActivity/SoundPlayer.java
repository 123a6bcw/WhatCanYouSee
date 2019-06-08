package ru.ralsei.whatcanyousee.GameActivity;

import android.media.MediaPlayer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Class for playing sounds in game. Support playing several sounds in parallel.
 */
public class SoundPlayer {
    private GameActivity activity;

    SoundPlayer(GameActivity activity) {
        this.activity = activity;
    }

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
                        players[i] = MediaPlayer.create(activity, trackId);
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
    void clearResources() {
        for (int i = 0; i < players.length; i++) {
            if (players[i] != null) {
                players[i].release();
                players[i] = null;
            }
        }
    }

    void shutdown() {
        executor.shutdown();
    }
}