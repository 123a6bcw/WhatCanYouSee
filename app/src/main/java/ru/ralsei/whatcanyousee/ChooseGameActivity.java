package ru.ralsei.whatcanyousee;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import ru.ralsei.whatcanyousee.gameactivity.GameActivity;

/**
 * Testing activity: choosing the game and the map to play and test in single player mode.
 *
 * Unfortunately, unfinished.
 */
public class ChooseGameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_game_debug);

        final Activity activity = this;

        ((Button)findViewById(R.id.mazeGame)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, GameActivity.class);
                intent.putExtra("debug", "maze");
                startActivity(intent);
            }
        });
    }
}
