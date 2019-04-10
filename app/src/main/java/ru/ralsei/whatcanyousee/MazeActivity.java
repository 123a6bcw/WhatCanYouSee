package ru.ralsei.whatcanyousee;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import ru.ralsei.whatcanyousee.R;

public class MazeActivity extends AppCompatActivity implements View.OnClickListener {

    MazeExplorer mazeExplorer = null;
    MazeMap map = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        runCycle();

        ((Button)findViewById(R.id.upButton)).setOnClickListener(this);
        ((Button)findViewById(R.id.leftButton)).setOnClickListener(this);
        ((Button)findViewById(R.id.rightButton)).setOnClickListener(this);
        ((Button)findViewById(R.id.downButton)).setOnClickListener(this);
        ((Button)findViewById(R.id.useButton)).setOnClickListener(this);

    }

    void runCycle() {
        map = new TestMap(this);
        mazeExplorer = new MazeExplorer(map, this);
        map.draw();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.upButton:
                mazeExplorer.cycle(MazeExplorer.Command.UP);
                break;
            case R.id.downButton:
                mazeExplorer.cycle(MazeExplorer.Command.DOWN);
                break;
            case R.id.leftButton:
                mazeExplorer.cycle(MazeExplorer.Command.LEFT);
                break;
            case R.id.rightButton:
                mazeExplorer.cycle(MazeExplorer.Command.RIGHT);
                break;
            case R.id.useButton:
                mazeExplorer.cycle(MazeExplorer.Command.USE);
                break;
            default:
                break;
        }
    }
}
