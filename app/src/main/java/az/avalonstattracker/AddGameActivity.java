package az.avalonstattracker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AddGameActivity extends AppCompatActivity {

    RelativeLayout rl;
    String TAG = "AddGameActivity";
    List<Integer> players_entry;
    GameConfiguration config;
    static Utilities utils = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_game);
        rl = (RelativeLayout) findViewById(R.id.add_game_relative_layout);
        players_entry = new LinkedList<>();
    }

    public void onStart(){
        super.onStart();
        rl = (RelativeLayout) findViewById(R.id.add_game_relative_layout);

        Integer player_nr = Integer.valueOf(((Spinner) findViewById(R.id.playerNumberSpn)).getSelectedItem().toString());

        config = new GameConfiguration(utils, player_nr);

        List<ViewListRow> data = new LinkedList<>();
        for(int i=0; i<config.playerNr; i++){
            data.add(new ViewListRow(config.utils.EMPTY_FIELD, config.utils.EMPTY_FIELD, config));
        }
        config.setData(data);

        ListView lv = (ListView) findViewById(R.id.playersListView);
        NewGameList adapter = new NewGameList(this, data, lv, config);
        lv.setAdapter(adapter);


        Spinner s = (Spinner) findViewById(R.id.playerNumberSpn);
        s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String playerNr = adapterView.getItemAtPosition(i).toString();
                config.playerNr = Integer.parseInt(playerNr);
                ListView lv = (ListView) findViewById(R.id.playersListView);
                NewGameList adapter = (NewGameList) lv.getAdapter();
                adapter.changeDataSize(Integer.parseInt(playerNr));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        Button winBtn = (Button) findViewById(R.id.addGameBtn);
        winBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                Map<String, String> playerRoles = new HashMap<>();
                for (ViewListRow vlr : config.data) {
                    if (!vlr.selectedPlayer.equals(config.utils.EMPTY_FIELD) && !vlr.selectedRole.equals(config.utils.EMPTY_FIELD)) {
                        playerRoles.put(vlr.selectedPlayer, vlr.selectedRole);
                    }
                }
                String winMethod = ((Spinner) findViewById(R.id.gameResultSpinner)).getSelectedItem().toString();

                config.utils.dbHelper.addGame(playerRoles, winMethod);

                //config.dbHelper.getTableAsString("Games");
                //config.dbHelper.getTableAsString("Players");
                //config.dbHelper.getTableAsString("Roles");
                //config.dbHelper.getTableAsString("PlayerRoles");
//                config.utils.dbHelper.getTableAsString("RoleStats");

            }
        });
    }

    public void addPlayer(View view) {
        EditText text = (EditText) findViewById(R.id.nameTextInput);
        String player_name = text.getText().toString();
        config.utils.dbHelper.addPlayer(player_name);

        /*config.utils.dbHelper.getTableAsString("Players");
        config.utils.dbHelper.getTableAsString("RoleStats");
        config.utils.dbHelper.getTableAsString("Roles");*/
    }
}
