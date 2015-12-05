package com.the7winds.verbumSecretum.client.other;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by the7winds on 21.10.15.
 */
public class PlayersDBHelper extends SQLiteOpenHelper {
    public static final int VERSION = 1;

    private static final String DB_NAME = "P_DB";

    private static final String P_TABLE = "P_DBT";
    public static final String P_NAME = "p_name";
    public static final String P_ALL = "p_all";
    public static final String P_WON = "p_won";

    private static final String CREATE_TABLE = "CREATE TABLE " + P_TABLE +
                                        " (" + P_NAME + " TEXT NOT NULL, " +
                                               P_ALL + " TEXT NOT NULL, " +
                                               P_WON + " TEXT NOT NULL" + ");";
    private static final String DROP_TABLE = "DROP TABLE IF EXISTS " + P_TABLE + ";";

    private static final String ADD_PLAYER = "INSERT INTO " + P_TABLE + " (" + P_NAME + ", "
                                                                             + P_ALL + ", "
                                                                             + P_WON + ") "
                                                                      + "VALUES (?, ?, ?);";

    private static final String GET_PLAYER = "SELECT " + P_NAME + ", " + P_ALL + ", " + P_WON +
                                             " FROM " + P_TABLE + " WHERE " + P_NAME + " = ?;";

    private static final String GET_ALL = "SELECT * FROM " + P_TABLE;

    private static final String DELETE_PLAYER = "DELETE FROM " + P_TABLE + " WHERE " + P_NAME + " = ?;";

    public PlayersDBHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE);
        onCreate(db);
    }

    public void addPlayer(String name, Integer won, Integer all) {
        try {
            getWritableDatabase().execSQL(ADD_PLAYER, new String[]{name, won.toString(), all.toString()});
        }
        catch (SQLException e) {
            Log.d("E", e.toString());
        }
    }

    public Cursor getPlayer(String name) {
        return getReadableDatabase().rawQuery(GET_PLAYER, new String[] {name});
    }

    public void getChangePlayerData(String name, String field, Object data) {
        ContentValues cv = new ContentValues();
        cv.put(field, data.toString());
        getWritableDatabase().update(P_TABLE, cv, P_NAME + " = ?", new String[] {name});
    }

    public Cursor getAll() {
        return getReadableDatabase().rawQuery(GET_ALL, new String[] {});
    }

    public void deletePlayer(String name) {
        getWritableDatabase().execSQL(DELETE_PLAYER, new String[] {name});
    }
}
