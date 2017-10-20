package az.avalonstattracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.ArraySet;
import android.util.Log;
import android.util.Pair;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinTask;


public class DatabaseHelper extends SQLiteOpenHelper {
    static final String DB_NAME = "avalon_stats";
    static final int version = 1;
    private String TAG = "DataBaseHelper";
    private Utilities utils;

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, Utilities utils) {
        super(context, name, null, version);
        this.utils = utils;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        List<String> cmds = Arrays.asList(
                "CREATE TABLE IF NOT EXISTS Roles ( id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT, is_good INTEGER, image BLOB)",
                "CREATE TABLE IF NOT EXISTS Players ( id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT, image BLOB)",
                "CREATE TABLE IF NOT EXISTS Results ( id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, result TEXT)",
                "CREATE TABLE IF NOT EXISTS Games ( id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, date TEXT, result_id INTEGER REFERENCES Results(id))",
                "CREATE TABLE IF NOT EXISTS PlayerRoles ( id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, game_id INTEGER REFERENCES Games(id), player_id TEXT REFERENCES Players(id), role_id TEXT REFERENCES Roles(id))",
                "CREATE TABLE IF NOT EXISTS RoleStats ( id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, player_id INTEGER REFERENCES Players(id), role_id INTEGER REFERENCES Roles(id), win INTEGER, lose INTEGER, games INTEGER, kills INTEGER, attempts INTEGER)"
        );

        for (String cmd : cmds){
            sqLiteDatabase.execSQL(cmd);
        }

        ContentValues insertValues;
        List<String> roles = new ArrayList<>();
        roles.addAll(utils.badRoles);
        roles.addAll(utils.goodRoles);

        for(String role : roles){
            insertValues = new ContentValues();
            insertValues.put("name", role);
            if (utils.goodRoles.contains(role)){
                insertValues.put("is_good", 1);
            } else{
                insertValues.put("is_good", 0);
            }
            sqLiteDatabase.insert("Roles", null, insertValues);
        }

        List<String> results = new ArrayList<>(utils.results);

        for(String result : results){
            insertValues = new ContentValues();
            insertValues.put("result", result);
            sqLiteDatabase.insert("Results", null, insertValues);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        List<String> cmds = Arrays.asList(
                "DROP TABLE IF EXISTS Games",
                "DROP TABLE IF EXISTS Roles",
                "DROP TABLE IF EXISTS Players",
                "DROP TABLE IF EXISTS Results",
                "DROP TABLE IF EXISTS PlayerRoles",
                "DROP TABLE IF EXISTS RoleStats"
        );

        for (String cmd : cmds){
            sqLiteDatabase.execSQL(cmd);
        }
        onCreate(sqLiteDatabase);
    }

    void addPlayer(String playerName){
        /*
        Add player and create empty stats for every possible role for this player
         */
        ContentValues insertValues = new ContentValues();
        insertValues.put("name", playerName);
        long id = getWritableDatabase().insert("Players", null, insertValues);

        List<String> availableRoles = new LinkedList<>();
        availableRoles.addAll(utils.goodRoles);
        availableRoles.addAll(utils.badRoles);
        for(String role : availableRoles){
            insertValues = new ContentValues();
            insertValues.put("player_id", id);
            insertValues.put("win", 0);
            insertValues.put("lose", 0);
            insertValues.put("games", 0);
            insertValues.put("kills", 0);
            insertValues.put("attempts", 0);

            insertValues.put("role_id", getFirstRow("SELECT id FROM Roles WHERE name = \"" + role + "\"").get(0));
            getWritableDatabase().insert("RoleStats", null, insertValues);
        }
    }

    private List<String> getFirstRow(String query){
        List<String> result = new LinkedList<>();
        Cursor row = getWritableDatabase().rawQuery(query, null);
        int columnCount = row.getColumnCount();
        if (row.moveToFirst()){
            for(int column=0; column<columnCount; column++){
                result.add(row.getString(column));
            }
        }
        row.close();

        return result;
    }

    void addGame(Map<String, String> playerRoles, String result){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues insertValues = new ContentValues();
        insertValues.put("result_id", getFirstRow("SELECT id FROM Results WHERE result = \"" + result + "\"").get(0));

        String date = DateFormat.getInstance().format(System.currentTimeMillis());
        insertValues.put("date", date);

        long game_id = db.insert("Games", null, insertValues);

        for (Map.Entry<String, String> e : playerRoles.entrySet()){
            insertValues = new ContentValues();
            String player_id = getFirstRow("SELECT id FROM Players WHERE name = \"" + e.getKey() + "\"").get(0);
            String role_id = getFirstRow("SELECT id FROM Roles WHERE name = \"" + e.getValue() + "\"").get(0);

            insertValues.put("game_id", game_id);
            insertValues.put("player_id", player_id);
            insertValues.put("role_id", role_id);
            db.insert("PlayerRoles", null, insertValues);

            boolean wasEvil = utils.badRoles.contains(e.getValue());
            boolean didWin = result.contains("Evil") == wasEvil;
            String updateQuery = "UPDATE RoleStats SET {0} = {0} + 1 WHERE player_id = {1} and role_id = {2}";

            db.execSQL(MessageFormat.format(updateQuery, "games", player_id, role_id));

            if (didWin){
                db.execSQL(MessageFormat.format(updateQuery, "win", player_id, role_id));
            }else{
                db.execSQL(MessageFormat.format(updateQuery, "lose", player_id, role_id));
            }

            if (e.getValue().equals("Merlin") || e.getValue().equals("Assassin") || e.getValue().equals("Perceval") || e.getValue().equals("Morgana")){
                if (result.contains("assassinate")){
                    db.execSQL(MessageFormat.format(updateQuery, "kills", player_id, role_id));
                    db.execSQL(MessageFormat.format(updateQuery, "attempts", player_id, role_id));
                }else if (result.contains("Good")){
                    db.execSQL(MessageFormat.format(updateQuery, "attempts", player_id, role_id));
                }
            }
        }
    }

    private List<List<String>> getQueryData(String query){
        List<List<String>> result = new LinkedList<>();
        List<String> entry;
        Cursor allRows = getWritableDatabase().rawQuery(query, null);
        int columnCount = allRows.getColumnCount();
        if (allRows.moveToFirst()){
            do {
                entry = new LinkedList<>();
                for(int column=0; column<columnCount; column++){
                    entry.add(allRows.getString(column));
                }
                result.add(entry);
            } while (allRows.moveToNext());
        }

        return result;
    }

    List<String> getPlayers(){
        List<String> players = new LinkedList<>();
        for(List<String> e : getQueryData("SELECT name FROM players")){
            players.add(e.get(0));
        }
        return players;
    }

    private Float getPercent(String numerator, String denominator){
        Float percent;
        if (denominator.equals("0")){
            percent = 0.f;
        }else{
            percent = Float.parseFloat(numerator)/Float.parseFloat(denominator);
        }
        return percent;
    }

    List<String> getPlayerStatistics(String playerName){
        List<String> stats = new LinkedList<>();

        String playerId = getFirstRow(MessageFormat.format("SELECT id FROM Players WHERE name = \"{0}\"", playerName)).get(0);
        // General player stats
        List<String> generalPlayerStats = getFirstRow(
                MessageFormat.format("SELECT sum(games), sum(win) FROM RoleStats WHERE player_id = \"{0}\"", playerId));

        String entry = MessageFormat.format(
                "General :\n\tGames played : {0}\n\tTotal winrate : {1,number,#.##%} ({2}/{0})\n",
                generalPlayerStats.get(0),
                getPercent(generalPlayerStats.get(1), generalPlayerStats.get(0)),
                generalPlayerStats.get(1));

        String[] v = {"0", "1"};
        for (String e : v){
            generalPlayerStats = getFirstRow(
                    MessageFormat.format("SELECT sum(rs.win), sum(rs.games) FROM RoleStats as rs " +
                                    "JOIN Roles as r ON rs.role_id = r.id " +
                                    "WHERE rs.player_id = \"{0}\" AND r.is_good = {1}",
                            playerId, e)
            );
            if ( e.equals("0") ){
                entry += "\tGood winrate : ";
            }else{
                entry += "\tEvil winrate : ";
            }

            entry += MessageFormat.format(
                    "{0,number,#.##%} ({1}/{2})\n",
                    getPercent(generalPlayerStats.get(0), generalPlayerStats.get(1)),
                    generalPlayerStats.get(0),
                    generalPlayerStats.get(1)
            );
        }
        stats.add(entry);

        List<List<String>> rolesStats = getQueryData(
                MessageFormat.format("" +
                        "SELECT r.name, win, games, kills, attempts " +
                        "FROM RoleStats as rs " +
                        "left join Roles as r ON r.id = rs.role_id " +
                        "WHERE player_id = \"{0}\"", playerId)
        );

        Collections.sort(
                rolesStats, new Comparator<List<String>>() {
                    @Override
                    public int compare(List<String> a, List<String> b) {
                        return a.get(0).compareTo(b.get(0));
                    }
                }
        );

        for(List<String> e : rolesStats){
            entry = MessageFormat.format("{0} :\n\twinrate {1,number,#.##%} ({2}/{3}) ",
                    e.get(0),
                    getPercent(e.get(1), e.get(2)),
                    e.get(1),
                    e.get(2));
            stats.add(entry);
            if ( e.get(0).equals("Merlin") || e.get(0).equals("Assassin") || e.get(0).equals("Perceval") || e.get(0).equals("Morgana")){
                entry = MessageFormat.format("\n\tKA ratio: {0,number,#.##%} ({1}/{2})",
                        getPercent(e.get(3), e.get(4)),
                        e.get(3),
                        e.get(4));
                stats.add(entry);
            }
            stats.add("\n");
        }

        return stats;
    }

    List<String> getGeneralStats(){
        List<String> result = new LinkedList<>();
        String gamesPlayed = getFirstRow("SELECT count(*) FROM Games").get(0);
        result.add(MessageFormat.format(
                "Overall Games Played : {0}\nWin method :\n",
                gamesPlayed)
        );

        List<List<String>> rows = getQueryData(
            "SELECT r.result, count(result_id) FROM Games as g JOIN Results as r ON g.result_id = r.id GROUP BY r.result"
        );

        for (List<String> row : rows){
            result.add(
                    MessageFormat.format("\t{0} : {1,number,#.##%} ({2}/{3})\n",
                            row.get(0),
                            getPercent(row.get(1), gamesPlayed),
                            row.get(1),
                            gamesPlayed
                    )
            );
        }

        return result;
    }

    void removeHistoryGame(String gameId){
        SQLiteDatabase db = getWritableDatabase();
        String query = MessageFormat.format("select r.result, pr.player_id, ro.is_good, ro.name, ro.id from Games as g " +
                "join Results as r on r.id = g.result_id " +
                "join PlayerRoles as pr on pr.game_id = g.id " +
                "join Roles as ro on ro.id = pr.role_id " +
                "where g.id = {0}", gameId);

        Boolean game_won;
        String result, player_id, role_id, role_name;
        String updateQuery = "UPDATE RoleStats SET {0} = {0} - 1 WHERE player_id = {1} and role_id = {2}";
        for(List<String> row : getQueryData(query)){
            result = row.get(0).toLowerCase();
            role_name = row.get(3);
            player_id = row.get(1);
            role_id = row.get(4);
            game_won = result.contains("good") == row.get(2).contains("1");

            db.execSQL(MessageFormat.format(updateQuery, "games", player_id, role_id));
            if (game_won){
                db.execSQL(MessageFormat.format(updateQuery, "win", player_id, role_id));
            }else{
                db.execSQL(MessageFormat.format(updateQuery, "lose", player_id, role_id));
            }

            if (role_name.equals("Merlin") || role_name.equals("Assassin") || role_name.equals("Perceval") || role_name.equals("Morgana")){
                if (result.contains("assassinate")){
                    db.execSQL(MessageFormat.format(updateQuery, "kills", player_id, role_id));
                    db.execSQL(MessageFormat.format(updateQuery, "attempts", player_id, role_id));
                }else if (result.contains("good")){
                    db.execSQL(MessageFormat.format(updateQuery, "attempts", player_id, role_id));
                }
            }
        }
        getReadableDatabase().execSQL("DELETE FROM Games WHERE id = " + gameId);
        getReadableDatabase().execSQL("DELETE FROM PlayerRoles WHERE game_id = " + gameId);
    }

    List<GameHistoryEntry> getGamesHistory(){
        List<GameHistoryEntry> history = new LinkedList<>();
        List<Map.Entry<String, String>> playerRoles = new LinkedList<>();

        Cursor allRows = getReadableDatabase().rawQuery(
                "SELECT g.id, g.date, r.result, ro.name, p.name FROM 'Games' as g " +
                "left join 'Results' as r on g.result_id = r.id " +
                "left join 'PlayerRoles' as pr on g.id = pr.game_id " +
                "left join 'Roles' as ro on pr.role_id = ro.id " +
                "left join 'Players' as p on pr.player_id = p.id", null);

        int columnCount = allRows.getColumnCount();
        String prevPlayer = "";
        int gameId = -1;
        GameHistoryEntry game = new GameHistoryEntry();
        if (allRows.moveToFirst()){
            int prevGameId = allRows.getInt(0);
            do {
                //for (int i=0; i<columnCount; i++) {
                    gameId = allRows.getInt(0);
                    String date = allRows.getString(1);
                    String result = allRows.getString(2);
                    String role = allRows.getString(3);
                    String player = allRows.getString(4);
                    if (gameId == prevGameId){
                        if (!prevPlayer.equals(player)) {
                            prevPlayer = player;
                            playerRoles.add(new AbstractMap.SimpleEntry<>(player, role));
                            game.gameId = gameId;
                            game.playerRoles = playerRoles;
                            game.date = date;
                            game.result = result;
                        }
                    } else{
                        prevGameId = gameId;
                        prevPlayer = player;
                        history.add(game);
                        playerRoles = new LinkedList<>();
                        playerRoles.add(new AbstractMap.SimpleEntry<>(player, role));
                        game = new GameHistoryEntry(gameId, date, result, playerRoles);
                    }
               // }

            } while (allRows.moveToNext());

            // this is kinda dumb but I had do while and I didn't feel like changing it
            if (gameId != -1){
                history.add(game);
            }
        }

        allRows.close();

        // newest entries first
        Collections.reverse(history);
        return history;
    }

    List<String> getTopWinrate(String option){
        /*
        Available options "overall", "good", "evil" and every character name (default)
         */
        List<String> result = new LinkedList<>();

        String whereFilter = "";
        if (option.equals("overall")){
            whereFilter = "";
        } else if (option.equals("good")){
            whereFilter = "WHERE r.is_good=1";
        } else if (option.equals("evil")){
            whereFilter = "WHERE r.is_good=0";
        } else {
            whereFilter = MessageFormat.format("WHERE r.name=\"{0}\"", option);
        }

        String query = MessageFormat.format(
                "SELECT p.name, 1.0*sum(rs.win)/sum(rs.games) as \"Win rate\", sum(rs.win), sum(rs.games) as \"Total games\" FROM RoleStats as rs " +
                        "JOIN Players as p ON rs.player_id = p.id " +
                        "JOIN Roles as r ON rs.role_id = r.id {0} " +
                        "GROUP BY rs.player_id " +
                        "HAVING \"Total games\" > 0 " +
                        "ORDER BY \"Win rate\" DESC",
                whereFilter
        );

        List<List<String>> data = getQueryData(query);

        for(List<String> row : data){
            result.add(
                    MessageFormat.format("{0}    {1,number,#.##%}    ({2}/{3})",
                            row.get(0), Float.parseFloat(row.get(1)), row.get(2), row.get(3))
            );
        }

        return result;
    }

    class PairResult implements Comparable {
        Integer games_won;
        Integer games_played;

        PairResult(){
            games_played = 0;
            games_won = 0;
        }

        @Override
        public int compareTo(Object o) {
            PairResult p = (PairResult) o;
            return Float.compare(p.getWinrate(), getWinrate());
        }

        public float getWinrate(){
            return 1.0f * games_won/games_played;
        }

    }

    public static Map<String, PairResult>  sortByValue(Map<String, PairResult>  map) {
        List<Map.Entry<String, PairResult> > list = new LinkedList<>(map.entrySet());
        Collections.sort( list, new Comparator<Map.Entry<String, PairResult> >() {
            @Override
            public int compare(Map.Entry<String, PairResult>  o1, Map.Entry<String, PairResult>  o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        Map<String, PairResult>  result = new LinkedHashMap<>();
        for (Map.Entry<String, PairResult>  entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    List<String> getTopDuoWinrate(String option){
        /*
        Available options "overall", "good", "evil", "M&P"
         */
        List<String> result = new LinkedList<>();
        Map<String, PairResult> pairMap = new HashMap<>();

        String queryCondition;
        if( option.equals("M&P") ){
            queryCondition = "WHERE ro.name = \"Perceval\" or ro.name = \"Merlin\" ";
        }else{
            queryCondition = "WHERE ro.is_good = {0} ";
        }

        String query = MessageFormat.format("SELECT GROUP_CONCAT(DISTINCT(p.name)) as team, r.result FROM Games as g " +
                "join Results as r on g.result_id = r.id " +
                "join PlayerRoles as pr on g.id = pr.game_id " +
                "join Roles as ro on pr.role_id = ro.id " +
                "join Players as p on pr.player_id = p.id " +
                "{0}" +
                "GROUP BY g.id", queryCondition);

        String[] team;
        String key;
        Boolean game_won;
        PairResult pair;

        List<Pair<String, Integer>> options = new LinkedList<>();
        if ( option.equals("M&P") ){
            options.add(new Pair<>("good", 0));
        }else if (option.equals("overall")){
            options.add(new Pair<>("good", 1));
            options.add(new Pair<>("evil", 0));
        }else{
            if (option.equals("good")){
                options.add(new Pair<>("good", 1));
            }else{
                options.add(new Pair<>("evil", 0));
            }
        }

        for(Pair<String, Integer> opt : options){
            for (List<String> row: getQueryData(MessageFormat.format(query, opt.second))){
                team = row.get(0).split(",");
                game_won = row.get(1).toLowerCase().contains(opt.first);
                for(int i=0; i<team.length; i++){
                    for(int j=i+1; j<team.length; j++){
                        key = MessageFormat.format("{0} & {1}", team[i], team[j]);
                        if ( pairMap.containsKey(key) ){
                            pair = pairMap.get(key);
                        }else{
                            pair = new PairResult();
                            pairMap.put(key, pair);
                        }

                        pair.games_played += 1;
                        if ( game_won ){
                            pair.games_won += 1;
                        }
                    }
                }
            }
        }

        for(Map.Entry<String, PairResult> e: sortByValue(pairMap).entrySet()){
            pair = e.getValue();
            result.add(MessageFormat.format(
                    "{0}    winrate : {1,number,#.##%}    ({2}/{3})",
                    e.getKey(), pair.getWinrate(), pair.games_won, pair.games_played
            ));
        }

        return result;
    }

    List<String> getTopKills(String role, boolean descSort){
        List<String> result = new LinkedList<>();

        String descSortOpt = "";
        if (descSort){
            descSortOpt = "DESC";
        }

        String query = "SELECT p.name, 1.0*sum(rs.kills)/sum(rs.attempts) as \"KA ratio\", sum(rs.kills), sum(rs.attempts) " +
                "FROM RoleStats as rs " +
                "JOIN Players as p ON rs.player_id = p.id " +
                "JOIN Roles as r ON rs.role_id = r.id " +
                "WHERE r.name = \"{0}\" " +
                "GROUP BY rs.player_id " +
                "HAVING \"KA ratio\" NOT NULL " +
                "ORDER BY \"KA ratio\" {1}";

        List<List<String>> data = getQueryData(MessageFormat.format(query, role, descSortOpt));

        for(List<String> row : data){
            result.add(
                    MessageFormat.format("{0}    {1,number,#.##%}    ({2}/{3})",
                            row.get(0), Float.parseFloat(row.get(1)), row.get(2), row.get(3))
            );
        }

        return result;
    }

    List<String> getTopSaboteur(){
        List<String> result = new LinkedList<>();

        String query = "SELECT p.name, count(*) as \"sabotages\" FROM Games as g " +
                "JOIN Results as r on g.result_id = r.id " +
                "JOIN PlayerRoles as pr ON g.id = pr.game_id " +
                "JOIN Players as p ON pr.player_id = p.id " +
                "JOIN Roles as ro on ro.id = pr.role_id " +
                "where r.result = 'Evil assassinate' AND ro.is_good = 0 " +
                "group by p.name " +
                "ORDER BY \"sabotages\" DESC";

        for(List<String> row : getQueryData(query)){
            result.add(MessageFormat.format(
                    "{0}     {1}", row.get(0), row.get(1)
            ));
        }

        return result;
    }

    List<String> getLastGamePlayers(){
        List<String> result = getFirstRow("select GROUP_CONCAT(p.name) from Games as g " +
                "join PlayerRoles as pr on g.id = pr.game_id " +
                "join Players as p on pr.player_id = p.id " +
                "group by g.id " +
                "order by g.id desc limit 1");

        if (result.size() != 0){
            result = Arrays.asList(result.get(0).split(","));
        }

        return result;
    }

    String getTableAsString(String tableName) {
        String tableString = String.format("Table %s:\n", tableName);
        Cursor allRows = getReadableDatabase().rawQuery("SELECT * FROM " + tableName, null);
        if (allRows.moveToFirst() ){
            String[] columnNames = allRows.getColumnNames();
            do {
                for (String name: columnNames) {
                    tableString += String.format("%s: %s\n", name,
                            allRows.getString(allRows.getColumnIndex(name)));
                }
                tableString += "\n";

            } while (allRows.moveToNext());
        }

        allRows.close();

        Log.d("XD", tableString);

        return tableString;
    }
}
