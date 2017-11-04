package az.avalonstattracker;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;

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

    public void addPlayer(View view){
        Context context = this;
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.add_player_alert, null);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = promptsView.findViewById(R.id.add_player_alert_btn);

        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Add player",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String player_name = userInput.getText().toString();
                                String text;
                                boolean success;
                                switch (utils.dbHelper.addPlayer(player_name)) {
                                    case 1: text = "Player name cannot be empty!";
                                        success = false;
                                        break;
                                    case 2: text = "Player name already exists!";
                                        success = false;
                                        break;
                                    default: text = "Player added successfully";
                                        success = true;
                                }

                                Animations.addFadeOutTextAnimation(
                                        getApplicationContext(),
                                        (RelativeLayout) findViewById(R.id.const_layout),
                                        text,
                                        success
                                );
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();
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
