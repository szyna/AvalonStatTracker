package az.avalonstattracker;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

class GameHistoryEntry {
    int gameId;
    String date;
    String result;
    List<Map.Entry<String, String>> playerRoles;

    GameHistoryEntry(){}

    GameHistoryEntry(int gameId, String date, String result, List<Map.Entry<String, String>> playerRoles){
        this.gameId = gameId;
        this.date = date;
        this.result = result;
        this.playerRoles = playerRoles;
    }

    @Override
    public String toString(){
        String result = gameId + " " + date + " " + this.result;
        for (Map.Entry<String, String> e : playerRoles){
            result += " \n" + e.getKey() + "-" + e.getValue();
        }

        return result;
    }
}
