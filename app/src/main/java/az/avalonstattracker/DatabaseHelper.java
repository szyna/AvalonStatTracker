package az.avalonstattracker;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class DatabaseHelper extends SQLiteOpenHelper {
    static final public String DB_NAME = "avalon_stats";
    static final public int version =1;
    private String TAG = "DataBaseHelper";

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        /*
        DROP TABLE IF EXISTS Games;
DROP TABLE IF EXISTS PlayerRoles;
DROP TABLE IF EXISTS PlayerStats;

CREATE TABLE IF NOT EXISTS Games ( id INTEGER PRIMARY KEY, date TEXT);
CREATE TABLE IF NOT EXISTS PlayerRoles ( id INTEGER PRIMARY KEY, name TEXT, role TEXT);
CREATE TABLE IF NOT EXISTS PlayerStats ( name TEXT PRIMARY KEY, ..);

https://kripken.github.io/sql.js/GUI/
         */
        //sqLiteDatabase.execSQL("CREATE TABLE Games (  )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public String getTableAsString(SQLiteDatabase db, String tableName) {
        Log.d(TAG, "getTableAsString called");
        String tableString = String.format("Table %s:\n", tableName);
        Cursor allRows  = db.rawQuery("SELECT * FROM " + tableName, null);
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

        return tableString;
    }
}
