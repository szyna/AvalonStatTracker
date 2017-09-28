package az.avalonstattracker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TableLayout;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GameHistoryActivity extends AppCompatActivity {

    static Utilities utils = null;
    private List<String> headerData;
    private HashMap<String, List<Map.Entry<String, String>>> childData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_history);
    }

    public void onStart(){
        super.onStart();

        prepareGameHistoryData();

        ExpandableListView lv = (ExpandableListView) findViewById(R.id.gameHistoryElv);
        GameHistoryList adapter = new GameHistoryList(this, headerData, childData, utils);
        lv.setAdapter(adapter);
    }

    private void prepareGameHistoryData(){
        headerData = new LinkedList<>();
        childData = new HashMap<>();

        List<GameHistoryEntry> gamesHistory = utils.dbHelper.getGamesHistory();
        for (GameHistoryEntry e : gamesHistory){
            String headline = "[" + e.gameId + "] " + e.date + " " + e.result;
            headerData.add(headline);
            childData.put(headline, e.playerRoles);
        }
    }
}
