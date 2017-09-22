package az.avalonstattracker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AddGameActivity extends AppCompatActivity {

    RelativeLayout rl;
    String TAG = "AddGameActivity";
    SharedPreferences pref;
    List<Integer> players_entry;
    GameConfiguration config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_game);
        rl = (RelativeLayout) findViewById(R.id.add_game_relative_layout);
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        players_entry = new LinkedList<>();
    }

    public void onStart(){
        super.onStart();
        rl = (RelativeLayout) findViewById(R.id.add_game_relative_layout);

        final String player_json = pref.getString(getString(R.string.players_json), "{}");
        JSONObject json_data;
        try {
            json_data = new JSONObject(player_json);
            List<String> p = new ArrayList<>();
            for (Iterator<String> i = json_data.keys(); i.hasNext();){
                p.add(i.next());
            }

            Integer player_nr = Integer.valueOf(((Spinner) findViewById(R.id.playerNumberSpn)).getSelectedItem().toString());

            config = new GameConfiguration(this, p, player_nr);

            List<ViewListRow> data = new LinkedList<>();
            for(int i=0; i<player_nr; i++){
                data.add(new ViewListRow("", "", config));
            }
            config.setData(data);

            ListView lv = (ListView) findViewById(R.id.playersListView);
            NewGameList adapter = new NewGameList(this, data, lv, config);
            lv.setAdapter(adapter);

        } catch (JSONException e) {
            e.printStackTrace();
        }

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
                    if (!vlr.selectedPlayer.equals("") && !vlr.selectedRole.equals("")) {
                        playerRoles.put(vlr.selectedPlayer, vlr.selectedRole);
                    }
                }
                String winMethod = ((Spinner) findViewById(R.id.gameResultSpinner)).getSelectedItem().toString();

                config.dbHelper.addGame(playerRoles, winMethod);

                config.dbHelper.getTableAsString("Games");
                config.dbHelper.getTableAsString("Players");
                config.dbHelper.getTableAsString("Roles");
                config.dbHelper.getTableAsString("PlayerRoles");
                config.dbHelper.getTableAsString("RoleStats");

                /*try {
                    String games_json = pref.getString(getString(R.string.games_history), "[]");
                    JSONArray json_data = new JSONArray(games_json);
                    Map<String, Object> game_entry = new HashMap<>();
                    String winMethod = ((Spinner) findViewById(R.id.gameResultSpinner)).getSelectedItem().toString();

                    Map<String, String> playerRoles = new HashMap<>();
                    for (ViewListRow vlr : config.data) {
                        if (!vlr.selectedPlayer.equals("") && !vlr.selectedRole.equals("")) {
                            playerRoles.put(vlr.selectedPlayer, vlr.selectedRole);
                        }
                    }

                    game_entry.put("players", playerRoles);
                    game_entry.put("win_method", winMethod);

                    json_data.put(game_entry);

                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString(getString(R.string.games_history), json_data.toString());
                    editor.apply();

                    String players_json = pref.getString(getString(R.string.players_json), "{}");
                    JSONObject json = new JSONObject(players_json);
                    JSONObject playerData, roleEntry, genericEntry;
                    for (ViewListRow vlr : config.data){
                        if (!vlr.selectedPlayer.equals("") && !vlr.selectedRole.equals("")) {
                            Log.d(TAG, json.toString());
                            Log.d(TAG, json.getJSONObject(vlr.selectedPlayer).toString());
                            playerData = json.getJSONObject(vlr.selectedPlayer);
                            boolean wasEvil = config.badRoles.contains(vlr.selectedRole);
                            boolean didWin = winMethod.contains("Evil") && wasEvil;
                            Log.d(TAG, wasEvil + " " + didWin);
                            roleEntry = playerData.getJSONObject(vlr.selectedRole);
                            incrementJsonValue(roleEntry, "played");
                            if (wasEvil){
                                genericEntry = playerData.getJSONObject("evil");
                            }else{
                                genericEntry = playerData.getJSONObject("good");
                            }
                            incrementJsonValue(genericEntry, "played");

                            if (didWin){
                                incrementJsonValue(roleEntry, "wins");
                                incrementJsonValue(genericEntry, "wins");
                            }

                            if(vlr.selectedRole.equals("Merlin")){
                                genericEntry = playerData.getJSONObject("merlin_stat");
                                if (winMethod.contains("assassinate")){
                                    incrementJsonValue(genericEntry, "kill_attempts");
                                    incrementJsonValue(genericEntry, "killed");
                                }else if(winMethod.contains("Good")){
                                    incrementJsonValue(genericEntry, "kill_attempts");
                                }
                            }else if(vlr.selectedRole.equals("Assassin")){
                                genericEntry = playerData.getJSONObject("assassin_stat");
                                if (winMethod.contains("assassinate")){
                                    incrementJsonValue(genericEntry, "kill_attempts");
                                    incrementJsonValue(genericEntry, "kills");
                                }else if(winMethod.contains("Good")){
                                    incrementJsonValue(genericEntry, "kill_attempts");
                                }
                            }
                        }
                    }
                    editor.putString(getString(R.string.players_json), json.toString());
                    editor.apply();

                    Log.d(TAG, json.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }*/
            }
        });
    }

    private void incrementJsonValue(JSONObject entry, String key) throws JSONException {
        entry.put(key, String.valueOf(Integer.parseInt((String) entry.get(key)) + 1));
    }

    public void addPlayer(View view) throws JSONException {
        EditText text = (EditText) findViewById(R.id.nameTextInput);
        String player_json = pref.getString(getString(R.string.players_json), "{}");
        JSONObject json_data = new JSONObject(player_json);
        String player_name = text.getText().toString();
        config.dbHelper.addPlayer(player_name);
        config.dbHelper.getTableAsString("Players");
        config.dbHelper.getTableAsString("RoleStats");
        config.dbHelper.getTableAsString("Roles");

        if (!json_data.has(player_name) && !player_name.equals("")){

            /*JSONObject playerData, entry;

            playerData = new JSONObject();
            List<String> keys = new LinkedList<>();
            keys.addAll(config.badRoles);
            keys.addAll(config.goodRoles);
            keys.add("good");
            keys.add("evil");
            for (String key : keys){
                entry = new JSONObject();
                entry.put("played", "0");
                entry.put("wins", "0");
                playerData.put(key, entry);
            }

            entry = new JSONObject();
            for (String method : getResources().getStringArray(R.array.game_result)){
                entry.put(method, "0");
            }
            playerData.put("win_method", entry);

            entry = new JSONObject();
            entry.put("kills", "0");
            entry.put("kill_attempts", "0");
            playerData.put("assassin_stat", entry);

            entry = new JSONObject();
            entry.put("killed", "0");
            entry.put("kill_attempts", "0");
            playerData.put("merlin_stat", entry);

            json_data.put(player_name, playerData);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString(getString(R.string.players_json), json_data.toString());
            editor.apply();

            config.availablePlayers.add(player_name);*/
        }
    }
}
