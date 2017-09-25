package az.avalonstattracker;

import android.content.Context;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Utilities{

    final List<String> goodRoles;
    final List<String> badRoles;
    final Map<Integer, Map<String, Integer>> playerConfig;
    final String EMPTY_FIELD = "";
    List<String> results;
    DatabaseHelper dbHelper;

    Utilities(Context context){
        this.goodRoles = Arrays.asList(context.getResources().getStringArray(R.array.good_roles));
        this.badRoles = Arrays.asList(context.getResources().getStringArray(R.array.bad_roles));
        this.results = Arrays.asList(context.getResources().getStringArray(R.array.game_result));
        this.dbHelper = new DatabaseHelper(context, DatabaseHelper.DB_NAME, null, DatabaseHelper.version, this);

        this.playerConfig = new HashMap<>();
        List<Integer> good_boyes = Arrays.asList(3, 4, 4 ,5 ,6, 6);
        List<Integer> bad_boyes = Arrays.asList(2, 2, 3, 3, 3, 4);
        int offset = 5;
        for(int i=offset; i<=10; i++){
            HashMap<String, Integer> mp = new HashMap<>();
            mp.put("good", good_boyes.get(i - offset));
            mp.put("evil", bad_boyes.get(i - offset));
            playerConfig.put(i, mp);
        }
    }

}
