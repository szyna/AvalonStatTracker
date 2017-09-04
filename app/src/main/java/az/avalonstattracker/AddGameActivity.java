package az.avalonstattracker;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
    TableRow tr;
    String TAG = "AddGameActivity";
    SharedPreferences pref;
    String player_json;
    JSONObject json_data;
    ArrayAdapter<CharSequence> adapter;
    TableLayout.LayoutParams params;
    List<Integer> players_entry;

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

        String player_json = pref.getString(getString(R.string.players_json), "{}");
        JSONObject json_data;
        try {
            json_data = new JSONObject(player_json);
            List<String> p = new ArrayList<>();
            for (Iterator<String> i = json_data.keys(); i.hasNext();){
                p.add(i.next());
            }

            Integer player_nr = Integer.valueOf(((Spinner) findViewById(R.id.playerNumberSpn)).getSelectedItem().toString());
            List<List<String>> players = new ArrayList<>();
            List<List<String>> characters = new ArrayList<>();
            for (int i=0; i<player_nr; i++){
                players.add(p);
                characters.add(Arrays.asList(getResources().getStringArray(R.array.roles)));
            }

            Log.d(TAG, "xxx");
            NewGameList adapter = new NewGameList(this, players, characters);
            ListView lv = (ListView) findViewById(R.id.playersListView);
            lv.setAdapter(adapter);
            Log.d(TAG, "xxx");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /*public void changePlayerNumber(View view){
        Integer player_nr = Integer.valueOf(((Spinner) findViewById(R.id.playerNumberSpn)).getSelectedItem().toString());
        tl = (TableLayout) findViewById(R.id.topTableLayout);
        if(player_nr > prev_player_nr){
            // add some players rows
        }else if (player_nr < prev_player_nr){
            // remove some players rows
            //TODO remove children - how?
            tl.removeViews(player_nr, tl.getChildCount());
        }
        prev_player_nr = player_nr;
    }*/

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

            for (Integer id : players_entry){
                Spinner s = (Spinner) findViewById(id);
                ArrayAdapter<String> a = (ArrayAdapter<String>) s.getAdapter();
                // TODO - maybe change to something else? it seems hard..
            }
        }
    }
}
