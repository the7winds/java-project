package com.the7winds.verbumSecretum.client.other;

import android.content.Context;
import android.database.Cursor;

import java.util.LinkedList;
import java.util.List;


/**
 * Created by the7winds on 20.10.15.
 */

public class ClientUtils {

    public static class Player {
        public String name = "";
        public int all = 0;
        public int won = 0;

        public Player() {
        }

        public Player(String name, int all, int won) {
            this.name = name;
            this.all = all;
            this.won = won;
        }

    }

    private static boolean authorised = false;

    public static Player player;

    public static boolean validateLogin(String name) {
        return (name.length() > 2 && !DB.hasPlayer(name));
    }

    public static class DB {

        private static List<Player> playersList;

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

        public static List<Player> getAllPlayers() {
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

                        playersList.add(new Player(name, all, won));
                    }
                }

                return playersList;
            }
        }

        public static Player getPlayer(String name) {
            Cursor cursor = playersDBHelper.getPlayer(name);
            Player player = null;

            if (cursor != null && cursor.moveToNext()) {
                int all = Integer.parseInt(cursor.getString(1));
                int won = Integer.parseInt(cursor.getString(2));
                player = new Player(name, all, won);
            }

            return player;
        }
    }

    public static void authoriseAs(String name) {
        authorised = true;
        player = DB.getPlayer(name);
        ClientData.name = name;
    }

    public static  boolean isAuthorised() {
        return authorised;
    }

}
