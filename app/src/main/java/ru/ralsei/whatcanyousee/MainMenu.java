package ru.ralsei.whatcanyousee;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;


import ru.ralsei.whatcanyousee.gameactivity.GameActivity;

/**
 * Main menu of the game.
 */
public class MainMenu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        final Activity activity = this;
        ((Button)findViewById(R.id.debugButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, ChooseGameActivity.class);
                startActivity(intent);
            }
        });

        ((Button)findViewById(R.id.createRoom)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, GameActivity.class);
                startActivity(intent);
            }
        });
    }
}
