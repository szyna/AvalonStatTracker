package az.avalonstattracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    public String TAG = "MainActivity";
    Utilities utils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        utils = new Utilities(this);
    }

    public void addGame(View view){
        AddGameActivity.utils = utils;
        Intent intent = new Intent(this, AddGameActivity.class);
        startActivity(intent);
    }

    public void showGameHistory(View view){
        GameHistoryActivity.utils = utils;
        Intent intent = new Intent(this, GameHistoryActivity.class);
        startActivity(intent);
    }

    public void showStatistics(View view){
        StatisticsActivity.utils = utils;
        Intent intent = new Intent(this, StatisticsActivity.class);
        startActivity(intent);
    }

    public void showRankings(View view){
        RankingsActivity.utils = utils;
        Intent intent = new Intent(this, RankingsActivity.class);
        startActivity(intent);
    }
}
