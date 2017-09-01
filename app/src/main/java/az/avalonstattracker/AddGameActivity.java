package az.avalonstattracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AddGameActivity extends AppCompatActivity {

    RelativeLayout rl;
    TableLayout tl;
    TableRow tr;
    String TAG = "AddGameActivity";
    SharedPreferences pref;
    String player_json;
    JSONObject json_data;
    ArrayAdapter<CharSequence> adapter;
    TableLayout.LayoutParams params;
    List<Integer> players_entry;
    Integer prev_player_nr;

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
        tl = (TableLayout) findViewById(R.id.topTableLayout);
        prev_player_nr = Integer.valueOf(((Spinner) findViewById(R.id.playerNumberSpn)).getSelectedItem().toString());

        String player_json = pref.getString(getString(R.string.players_json), "{}");
        JSONObject json_data;
        Spinner spinner;
        try {
            json_data = new JSONObject(player_json);
            List<String> players = new ArrayList<>();
            for (Iterator<String> i = json_data.keys(); i.hasNext();){
                players.add(i.next());
            }

            TableRow.LayoutParams lp;

            for(int i=0; i<prev_player_nr; i++){
                tr = new TableRow(this);
                tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
                tr.setWeightSum(2);

                spinner = new Spinner(this);
                ArrayAdapter<String> adapter2 = new ArrayAdapter<>(this, R.layout.spinner_item, players);
                ArrayAdapter.createFromResource(this, R.array.roles, R.layout.spinner_item);
                adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter2);
                lp = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.f);
                spinner.setLayoutParams(lp);
                tr.addView(spinner);
                players_entry.add(spinner.getId());

                spinner = new Spinner(this);
                adapter = ArrayAdapter.createFromResource(this, R.array.roles, R.layout.spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
                spinner.setLayoutParams(lp);
                tr.addView(spinner);

                tl.addView(tr);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void changePlayerNumber(View view){
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

            for (Integer id : players_entry){
                Spinner s = (Spinner) findViewById(id);
                ArrayAdapter<String> a = (ArrayAdapter<String>) s.getAdapter();
                // TODO - maybe change to something else? it seems hard..
            }
        }
    }
}
