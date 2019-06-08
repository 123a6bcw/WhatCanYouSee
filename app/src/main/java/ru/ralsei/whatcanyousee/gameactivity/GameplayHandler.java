package ru.ralsei.whatcanyousee.gameactivity;

import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Random;

import ru.ralsei.whatcanyousee.R;
import ru.ralsei.whatcanyousee.logic.CodeGame;
import ru.ralsei.whatcanyousee.logic.CodeGameMap;
import ru.ralsei.whatcanyousee.logic.LeverGame;
import ru.ralsei.whatcanyousee.logic.LeverGameMap;
import ru.ralsei.whatcanyousee.logic.MazeGame;
import ru.ralsei.whatcanyousee.logic.MazeGameMap;
import ru.ralsei.whatcanyousee.maps.codegame.CodeGameMap_Test1;
import ru.ralsei.whatcanyousee.maps.codegame.CodeGameMap_Test2;
import ru.ralsei.whatcanyousee.maps.codegame.CodeGameMap_Test3;
import ru.ralsei.whatcanyousee.maps.codegame.CodeGameMap_Test4;
import ru.ralsei.whatcanyousee.maps.levergame.LeverGameKingAndQueen_firstPlayer;
import ru.ralsei.whatcanyousee.maps.levergame.LeverGameKingAndQueen_secondPlayer;
import ru.ralsei.whatcanyousee.maps.levergame.LeverGameMap_Test1;
import ru.ralsei.whatcanyousee.maps.levergame.LeverGameMap_Test2;
import ru.ralsei.whatcanyousee.maps.levergame.LeverGameMap_Test3;
import ru.ralsei.whatcanyousee.maps.mazegame.MazeGameMap_Test;
import ru.ralsei.whatcanyousee.maps.mazegame.MazeGameMap_Test2;

/**
 * Class for interacting with the gameplay stage of the game.
 */
public class GameplayHandler {
    private String TAG = "What can you see: GameplayHandler:";
    private GameActivity activity;

    GameplayHandler(GameActivity activity) {
        this.activity = activity;
    }

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
     * Random used to choose exact maps for players.
     */
    private final Random random = new Random();

    /**
     * Class for storing settings of the current game.
     */
    private GameSettings gameSettings;

    GameSettings getGameSettings() {
        return gameSettings;
    }

    void setGameSettings(GameSettings gameSettings) {
        this.gameSettings = gameSettings;
    }

    /**
     * Clears all handlers and they resources.
     */
    void clearResources() {
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
    void createGameSettings() {
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


        if (Math.abs(random.nextInt()) % 2 == 0) {
            String[] leverGames = new String[]{LeverGameMap_Test1.class.getName(), LeverGameMap_Test2.class.getName(), LeverGameMap_Test3.class.getName()};
            int myLeverGameId = (Math.abs(random.nextInt())) % 3;
            gameSettings.setMyLeverGameMap(leverGames[myLeverGameId]);
            int teammateLeverGameId = (Math.abs(random.nextInt())) % 3;
            while (teammateLeverGameId == myLeverGameId) {
                teammateLeverGameId = (Math.abs(random.nextInt())) % 3;
            }
            gameSettings.setMyTeammateLeverGameMap(leverGames[teammateLeverGameId]); //TODO smart selection
        } else {
            gameSettings.setMyLeverGameMap(LeverGameKingAndQueen_firstPlayer.class.getName());
            gameSettings.setMyTeammateLeverGameMap(LeverGameKingAndQueen_secondPlayer.class.getName());
        }
    }

    /**
     * Start gameplay stage of the game.
     */
    void startGame() {
        activity.getAudioConnector().startBroadcastAudio();
        startMazeGame();
    }

    /**
     * Starts maze game gameplay stage.
     */
    void startMazeGame() {
        activity.setState(GameActivity.State.MAZE_GAME);

        activity.setContentView(R.layout.content_maze_game);

        assert gameSettings != null;
        Log.d(TAG, "Loaded maps are " + gameSettings.getMyMazeMap() + " " + gameSettings.getTeammateMazeMap());

        MazeGameMap teammateMap = null;
        try {
            mazeGameMap = (MazeGameMap) activity.getClassLoader().loadClass(gameSettings.getMyMazeMap()).getDeclaredConstructor(GameActivity.class).newInstance(activity);
            teammateMap = (MazeGameMap) activity.getClassLoader().loadClass(gameSettings.getTeammateMazeMap()).getDeclaredConstructor(GameActivity.class).newInstance(activity);
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
            activity.handleException(new RuntimeException(), "Couldn't create maze game");
            return;
        }

        ((ImageView) activity.findViewById(R.id.image_maze_map)).setImageResource(teammateMap.getImageID());

        maze = new MazeGame(mazeGameMap, activity);
        mazeGameMap.draw();
        maze.setupListeners();

        activity.getGameStatistic().setMazeGameTime(System.currentTimeMillis());
        Log.d(TAG, "Switched to maze game");
    }



    /**
     * Called then we won the maze game. Either starts the next game or hides everything but
     * the other player's map.
     */
    public void onMazeGameWon() {
        activity.getGameStatistic().setMazeGameTime(System.currentTimeMillis() - activity.getGameStatistic().getMazeGameTime());

        for (int i = 0; i < MazeGameMap.HEIGHT_VIEW; i++) {
            for (int j = 0; j < MazeGameMap.WIDTH_VIEW; j++) {
                ImageView imageView = activity.findViewById(mazeGameMap.getImageIds()[i][j]);

                if (imageView != null) {
                    imageView.setVisibility(View.GONE);
                }
            }
        }

        Button button = activity.findViewById(R.id.downButton);
        if (button != null) {
            activity.findViewById(R.id.downButton).setVisibility(View.GONE);
            activity.findViewById(R.id.upButton).setVisibility(View.GONE);
            activity.findViewById(R.id.leftButton).setVisibility(View.GONE);
            activity.findViewById(R.id.rightButton).setVisibility(View.GONE);
            activity.findViewById(R.id.useButton).setVisibility(View.GONE);
        }

        myMazeGameWon = true;

        activity.getInternetConnector().sendMazeWonMessage();

        if (otherMazeGameWon) {
            Log.d(TAG, "maze game won");
            startCodeGame();
        }
    }

    /**
     * Called when we lost the maze game (and, therefore, we and our teammate lost an entire game).
     */
    public void onMazeGameLost(String message) {
        activity.getGameStatistic().setMazeGameTime(-1);

        activity.getInternetConnector().sendMazeLostMessage();
        gameOver(false, message);
        Log.d(TAG, "maze game lost");
    }

    /**
     * Called when we have won the code game. Hides everything but the image with hint to the other
     * player's game.
     */
    public void onCodeGameWon() {
        activity.getGameStatistic().setCodeGameTime(System.currentTimeMillis() - activity.getGameStatistic().getCodeGameTime());

        activity.findViewById(R.id.text_code).setVisibility(View.GONE);
        activity.findViewById(R.id.button_giveUp).setVisibility(View.GONE);
        activity.findViewById(R.id.button_submitCode).setVisibility(View.GONE);

        myCodeGameWon = true;

        activity.getInternetConnector().sendCodeWonMessage();

        if (otherCodeGameWon) {
            Log.d(TAG, "code game won");
            startLeverGame();
        }
    }

    /**
     * Starts the gameplay stage of the code game.
     */
    void startCodeGame() {
        activity.setState(GameActivity.State.CODE_GAME);

        Log.d(TAG, "Code game started");

        clearMazeResources();

        activity.setContentView(R.layout.content_code_game);

        CodeGameMap teammateCodeGameMap = null;
        try {
            assert gameSettings != null;
            codeGameMap = (CodeGameMap) activity.getClassLoader().loadClass(gameSettings.getMyCodeGameMap()).getDeclaredConstructor().newInstance();
            teammateCodeGameMap = (CodeGameMap) activity.getClassLoader().loadClass(gameSettings.getMyTeammateCodeGameMap()).getDeclaredConstructor().newInstance();
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

        codeGame = new CodeGame(activity, codeGameMap, teammateCodeGameMap);
        codeGame.setupListeners();
        activity.getGameStatistic().setCodeGameTime(System.currentTimeMillis());
    }

    /**
     * Called when we lost the code game (and, therefore, we and our teammate lost an entire game).
     */
    public void onCodeGameLost(String message) {
        activity.getInternetConnector().sendCodeLostMessage();
        gameOver(false, message);
        Log.d(TAG, "code game lost");
    }

    /**
     * Called on loosing the game.
     */
    void gameOver(boolean friendDied, String message) {
        if (activity.getGameStatistic().isDeadByMonster()) {
            activity.getGooglePlayHandler().getAchievementsClient().unlock(activity.getString(R.string.achievement_get_dunked_on));
        }

        if (activity.getGameStatistic().isKillYourFriend()) {
            activity.getGooglePlayHandler().getAchievementsClient().unlock(activity.getString(R.string.achievement_how_could_you_do_this));
        }

        activity.clearAllResources();

        if (friendDied) {
            message = message + "\n\n" + "You lost the game because your friend has been killed. Better luck next time!";
        } else {
            message = message + "\n\n" + "You lost the game because you was killed. Better luck next time!";
        }

        new AlertDialog.Builder(activity)
                .setMessage(message)
                .setNeutralButton(android.R.string.ok, null).show();
        activity.getGooglePlayHandler().leaveRoom();
    }

    /**
     * Starts the lever game gameplay stage.
     */
    void startLeverGame() {
        activity.setState(GameActivity.State.LEVER_GAME);

        Log.d(TAG, "Lever game started");

        clearCodeGameResources();

        myLeverGameWon = false;
        otherLeverGameWon = false;

        activity.setContentView(R.layout.content_lever_game);

        leverGameMap = null;
        LeverGameMap teammateLeverGameMap = null;

        assert gameSettings != null;
        Log.d(TAG, "loaded lever maps are: " + gameSettings.myLeverGameMap + " " + gameSettings.myTeammateLeverGameMap);

        try {
            leverGameMap = (LeverGameMap) activity.getClassLoader().loadClass(gameSettings.myLeverGameMap).getDeclaredConstructor(GameActivity.class).newInstance(activity);
            teammateLeverGameMap = (LeverGameMap) activity.getClassLoader().loadClass(gameSettings.myTeammateLeverGameMap).getDeclaredConstructor(GameActivity.class).newInstance(activity);
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

        leverGame = new LeverGame(activity, leverGameMap, teammateLeverGameMap);
        leverGame.setupListeners();
        activity.getGameStatistic().setLeverGameTime(System.currentTimeMillis());
    }

    /**
     * Sends the pressed lever (public for using in internal logic).
     */
    public void sendLeverPressedMessage(String lever) {
        activity.getInternetConnector().sendLeverPressedMessage(lever);
    }

    /**
     * Handles the loosing the lever game.
     */
    public void onLeverGameLost(boolean wasKilled, String message) {
        activity.getInternetConnector().sendLeverLostMessage();
        Log.d(TAG, "lever game lost");
        gameOver(false, message);

        if (!wasKilled) {
            activity.getGameStatistic().setKillYourFriend();
        }
    }

    /**
     * Handles the winning of the lever game.
     */
    public void onLeverGameWon() {
        activity.getGameStatistic().setLeverGameTime(System.currentTimeMillis() - activity.getGameStatistic().getLeverGameTime());

        leverGameMap = null;
        activity.findViewById(R.id.button_giveUp_lever).setVisibility(View.GONE);

        myLeverGameWon = true;

        activity.getInternetConnector().sendLeverWonMessage();

        if (otherLeverGameWon) {
            Log.d(TAG, "lever game won");
            gameWin();
        }
    }

    /**
     * Handles winning an entire game.
     */
    void gameWin() {
        activity.getGooglePlayHandler().pushAccomplishments();
        activity.clearAllResources();
        activity.getGooglePlayHandler().leaveRoom();

        new AlertDialog.Builder(activity)
                .setMessage("VICTORY! Congrats ;) !!!")
                .setNeutralButton(android.R.string.ok, null).show();
    }

    CodeGame getCodeGame() {
        return codeGame;
    }

    MazeGame getMaze() {
        return maze;
    }

    LeverGame getLeverGame() {
        return leverGame;
    }

    void setOtherMazeGameWon() {
        otherMazeGameWon = true;
    }

    void setOtherCodeGameWon() {
        otherCodeGameWon = true;
    }

    void setOtherLeverGameWon() {
        otherLeverGameWon = true;
    }

    boolean getMyMazeGameWon() {
        return myMazeGameWon;
    }

    boolean getMyCodeGameWon() {
        return myCodeGameWon;
    }

    boolean getMyLeverGameWon() {
        return myLeverGameWon;
    }

    LeverGameMap getLeverGameMap() {
        return leverGameMap;
    }

    /**
     * Settings of the created game (basically maps of all levels in game), created by the one who
     * created the game, who also sends this setting to the other player.
     *
     * Owner's maps and teammate's maps are being swapped before sending to the other player.
     */
    static class GameSettings implements Serializable {
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
        void flipSettings() {
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
}
