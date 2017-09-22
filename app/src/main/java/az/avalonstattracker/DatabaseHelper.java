package az.avalonstattracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.ArraySet;
import android.util.Log;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class DatabaseHelper extends SQLiteOpenHelper {
    static final public String DB_NAME = "avalon_stats";
    static final public int version =1;
    private String TAG = "DataBaseHelper";
    private GameConfiguration config;

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, GameConfiguration config) {
        super(context, name, null, version);
        this.config = config;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        List<String> cmds = Arrays.asList(
                "CREATE TABLE IF NOT EXISTS Roles ( id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT)",
                "CREATE TABLE IF NOT EXISTS Players ( id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT)",
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
        roles.addAll(config.badRoles);
        roles.addAll(config.goodRoles);
        roles.add("good");
        roles.add("evil");

        for(String role : roles){
            insertValues = new ContentValues();
            insertValues.put("name", role);
            sqLiteDatabase.insert("Roles", null, insertValues);
        }

        List<String> results = new ArrayList<>(config.results);

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
        availableRoles.addAll(config.goodRoles);
        availableRoles.addAll(config.badRoles);
        availableRoles.add("good");
        availableRoles.add("evil");
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

            boolean wasEvil = config.badRoles.contains(e.getValue());
            boolean didWin = result.contains("Evil") == wasEvil;
            String updateQuery = "UPDATE RoleStats SET {0} = {0} + 1 WHERE player_id = {1} and role_id = {2}";
            Log.d(TAG, MessageFormat.format(updateQuery, "games", player_id, role_id));
            // TODO update not working at all for some reason
            Cursor c = db.rawQuery(MessageFormat.format("SELECT role_id FROM RoleStats WHERE player_id = {0} and role_id = {1}", player_id, role_id), null);
            c.moveToFirst();
            Log.d(TAG, c.getString(0));
            Log.d(TAG, db.rawQuery(MessageFormat.format(updateQuery, "games", player_id, role_id), null).toString());

            String genericRole;
            if (wasEvil){
                genericRole = "good";
            }else{
                genericRole = "evil";
            }
            String generic_role_id = getFirstRowElement("SELECT id FROM Roles WHERE name = \"" + genericRole + "\"");
            db.rawQuery(MessageFormat.format(updateQuery, "games", player_id, generic_role_id), null);

            if (didWin){
                db.rawQuery(MessageFormat.format(updateQuery, "win", player_id, role_id), null);
                db.rawQuery(MessageFormat.format(updateQuery, "win", player_id, generic_role_id), null);
            }else{
                db.rawQuery(MessageFormat.format(updateQuery, "lose", player_id, role_id), null);
                db.rawQuery(MessageFormat.format(updateQuery, "lose", player_id, generic_role_id), null);
            }

            if (e.getValue().equals("Merlin") || e.getValue().equals("Assassin")){
                if (result.contains("assassinate")){
                    db.rawQuery(MessageFormat.format(updateQuery, "kills", player_id, role_id), null);
                    db.rawQuery(MessageFormat.format(updateQuery, "attempts", player_id, role_id), null);
                }else if (result.contains("Good")){
                    db.rawQuery(MessageFormat.format(updateQuery, "attempts", player_id, role_id), null);
                }
            }
        }
    }


    public List<String> getPlayers(){
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

    String getTableAsString(String tableName) {
        Log.d(TAG, "getTableAsString called");
        String tableString = String.format("Table %s:\n", tableName);
        Cursor allRows  = getReadableDatabase().rawQuery("SELECT * FROM " + tableName, null);
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
