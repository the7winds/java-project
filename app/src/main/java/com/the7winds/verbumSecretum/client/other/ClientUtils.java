package com.the7winds.verbumSecretum.client.other;

import android.content.Context;
import android.database.Cursor;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Created by the7winds on 20.10.15.
 */

public class ClientUtils {

    private static final String TAG = "ClientUtils";

    public static class Data {
        public static String id;

        public static Map<String, String> playersNames;

        public static AtomicBoolean gameActivityInited = new AtomicBoolean(false);
        public static AtomicBoolean gameActivityStarted = new AtomicBoolean(false);

        public static PlayerStatisticsData playerStatisticsData;
    }

    public static class PlayerStatisticsData extends BaseObservable {
        public String name = "";
        public int all = 0;
        public int won = 0;

        public PlayerStatisticsData(String name, int all, int won) {
            this.name = name;
            this.all = all;
            this.won = won;
        }

        @Bindable
        public int getAll() {
            return all;
        }

        @Bindable
        public int getWon() {
            return won;
        }
    }

    private static boolean authorised = false;

    public static boolean validateLogin(String name) {
        return (name.length() > 2 && !DB.hasPlayer(name));
    }

    public static class DB {

        private static List<PlayerStatisticsData> playersList;

        private static boolean changed = true;

        private static PlayersDBHelper playersDBHelper;

        public static void openDB(Context context) {
            playersDBHelper = new PlayersDBHelper(context);
        }

        public static boolean hasPlayer(String name) {
            Cursor cursor = playersDBHelper.getPlayer(name);
            return (cursor != null && cursor.moveToNext());
        }

        public static void addNewPlayerDB(String name) {
            if (!hasPlayer(name)) {
                changed = true;
                playersDBHelper.addPlayer(name, 0, 0);
            }
        }

        public static void changePlayersData(String name, String field, Object value) {
            if (hasPlayer(name)) {
                changed = true;
                playersDBHelper.getChangePlayerData(name, field, value);
            }
        }

        public static void deletePlayer(String name) {
            if (hasPlayer(name)) {
                changed = true;
                playersDBHelper.deletePlayer(name);
            }
        }

        public static List<PlayerStatisticsData> getAllPlayers() {
            if (!DB.changed) {
                return playersList;
            } else {
                playersList = new LinkedList<>();
                Cursor cursor = DB.playersDBHelper.getAll();

                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        String name = cursor.getString(0);
                        int all = Integer.parseInt(cursor.getString(1));
                        int won = Integer.parseInt(cursor.getString(2));

                        playersList.add(new PlayerStatisticsData(name, all, won));
                    }
                }

                return playersList;
            }
        }

        public static PlayerStatisticsData getPlayer(String name) {
            Cursor cursor = playersDBHelper.getPlayer(name);
            PlayerStatisticsData playerStatisticsData = null;

            if (cursor != null && cursor.moveToNext()) {
                int all = Integer.parseInt(cursor.getString(1));
                int won = Integer.parseInt(cursor.getString(2));
                playerStatisticsData = new PlayerStatisticsData(name, all, won);
            }

            return playerStatisticsData;
        }
    }

    public static void authoriseAs(String name) {
        authorised = true;
        Data.playerStatisticsData = DB.getPlayer(name);
    }

    public static boolean isAuthorised() {
        return authorised;
    }

    public static boolean amIHotspot(Context context) {
        try {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            Method method = wifiManager.getClass().getDeclaredMethod("getWifiApState");
            method.setAccessible(true);

            int actualState = (Integer) method.invoke(wifiManager, (Object[]) null);
            Field fField = wifiManager.getClass().getField("WIFI_AP_STATE_ENABLED");
            int hotspotEnabled = (Integer) fField.get(fField);

            if (actualState == hotspotEnabled) {
                return true;
            }

        } catch (InvocationTargetException | NoSuchMethodException
                | IllegalAccessException | NoSuchFieldException e) {
            Log.e(TAG, e.toString());
        }

        return false;
    }

    public static void saveStatistics() {
        DB.changePlayersData(Data.playerStatisticsData.name, PlayersDBHelper.P_ALL, Data.playerStatisticsData.all);
        DB.changePlayersData(Data.playerStatisticsData.name, PlayersDBHelper.P_WON, Data.playerStatisticsData.won);
    }
}
