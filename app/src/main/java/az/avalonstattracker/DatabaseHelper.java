package az.avalonstattracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.ArraySet;
import android.util.Log;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


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

            insertValues.put("role_id", getFirstRowElement("SELECT id FROM Roles WHERE name = \"" + role + "\""));
            getWritableDatabase().insert("RoleStats", null, insertValues);
        }
    }

    private String getFirstRowElement(String query){
        String result = "";
        Cursor row = getWritableDatabase().rawQuery(query, null);
        if (row != null){
            if (row.moveToFirst()){
                result = row.getString(0);
            }
            row.close();
        }

        return result;
    }

    void addGame(Map<String, String> playerRoles, String result){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues insertValues = new ContentValues();
        insertValues.put("result_id", getFirstRowElement("SELECT id FROM Results WHERE result = \"" + result + "\""));

        String date = DateFormat.getInstance().format(System.currentTimeMillis());
        insertValues.put("date", date);

        long game_id = db.insert("Games", null, insertValues);

        for (Map.Entry<String, String> e : playerRoles.entrySet()){
            insertValues = new ContentValues();
            String player_id = getFirstRowElement("SELECT id FROM Players WHERE name = \"" + e.getKey() + "\"");
            String role_id = getFirstRowElement("SELECT id FROM Roles WHERE name = \"" + e.getValue() + "\"");

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


    List<String> getPlayers(){
        List<String> players = new LinkedList<>();
        Cursor allRows  = getReadableDatabase().rawQuery("SELECT name FROM Players", null);
        if (allRows.moveToFirst() ){
            String[] columnNames = allRows.getColumnNames();
            do {
                players.add(allRows.getString(allRows.getColumnIndex("name")));
            } while (allRows.moveToNext());
        }
        allRows.close();
        return players;
    }

    void removeHistoryGame(String gameId){
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
                for (int i=0; i<columnCount; i++) {
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
                }

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

    String getTableAsString(String tableName) {
        Log.d(TAG, "getTableAsString called");
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
