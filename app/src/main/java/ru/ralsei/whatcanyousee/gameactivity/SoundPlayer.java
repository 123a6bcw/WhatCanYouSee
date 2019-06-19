package ru.ralsei.whatcanyousee.gameactivity;

import android.media.MediaPlayer;

/**
 * Class for playing sounds in game. Support playing several sounds in parallel.
 */
public class SoundPlayer {
    /**
     * Maximum number of players that can be used in parallel. Should be fixed as total number
     * of MediaPlayers in android is limited.
     */
    private static final int NUMBER_OF_PLAYERS = 8;

    private GameActivity activity;

    SoundPlayer(GameActivity activity) {
        this.activity = activity;
    }

    private final MediaPlayer[] players = new MediaPlayer[NUMBER_OF_PLAYERS];

    /**
     * Used to support choosing volume from 1 to 10 linearly.
     */
    private final int MAX_VOLUME = 11;

    /**
     * False if not in a gameplay stage of the game sounds shouldn't be playing.
     */
    private boolean canPlay = false;

    /**
     * Finds free player and plays given track, there volume should be from 0 to
     * MAX_VOLUME.
     */
    public void playTrackWithVolume(final int trackId, final int volume) {
        if (volume <= 0 || volume >= MAX_VOLUME) {
            return;
        }

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                synchronized (players) {
                    if (!canPlay) {
                        return;
                    }

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
     * Called after entering gameplay stage of the game, so sound can be playing.
     */
    void setCanPlay() {
        synchronized (players) {
            canPlay = true;
        }
    }

    /**
     * Releasing all players and forbids new sounds to play until reentering gameplay stage.
     */
    void clearResources() {
        synchronized (players) {
            canPlay = false;

            for (int i = 0; i < players.length; i++) {
                if (players[i] != null) {
                    players[i].release();
                    players[i] = null;
                }
            }
        }
    }
}