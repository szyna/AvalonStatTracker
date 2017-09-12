package az.avalonstattracker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;

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
    }

    public void addPlayer(View view) throws JSONException {
        EditText text = (EditText) findViewById(R.id.nameTextInput);
        String player_json = pref.getString(getString(R.string.players_json), "{}");
        JSONObject json_data = new JSONObject(player_json);
        String player_name = text.getText().toString();

        if (!json_data.has(player_name) && !player_name.equals("")){
            json_data.put(player_name, "");
            SharedPreferences.Editor editor = pref.edit();
            editor.putString(getString(R.string.players_json), json_data.toString());
            editor.apply();

            config.availablePlayers.add(player_name);
        }
    }
}
