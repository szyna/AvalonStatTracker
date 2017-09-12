package az.avalonstattracker;

import android.content.Context;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class GameConfiguration {

    Context context;
    final List<String> goodRoles;
    final List<String> badRoles;
    final Map<Integer, Map<String, Integer>> playerConfig;
    List<String> availableRoles;
    List<String> availablePlayers;
    Integer goodRolesNr;
    Integer badRolesNr;
    Integer playerNr;
    List<ViewListRow> data;

    GameConfiguration(Context context, List<String> availablePlayers, Integer playerNr){
        this.goodRolesNr = new Integer(0);
        this.badRolesNr = new Integer(0);
        this.context = context;
        this.availablePlayers = availablePlayers;
        this.playerNr = playerNr;
        this.goodRoles = Arrays.asList(context.getResources().getStringArray(R.array.good_roles));
        this.badRoles = Arrays.asList(context.getResources().getStringArray(R.array.bad_roles));
        this.availableRoles = new LinkedList<>();
        this.availableRoles.addAll(this.goodRoles);
        this.availableRoles.addAll(this.badRoles);

        this.playerConfig = new HashMap<>();
        List<Integer> good_boyes = Arrays.asList(3, 4, 4 ,5 ,6, 6);
        List<Integer> bad_boyes = Arrays.asList(2, 2, 3, 3, 3, 4);
        int offset = 5;
        for(int i=offset; i<=10; i++){
            HashMap<String, Integer> mp = new HashMap<>();
            mp.put("good", good_boyes.get(i - offset));
            mp.put("bad", bad_boyes.get(i - offset));
            playerConfig.put(i, mp);
        }
    }

    void setData(List<ViewListRow> data){
        this.data = data;
    }
}
