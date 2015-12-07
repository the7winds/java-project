package com.the7winds.verbumSecretum.server.network;

import android.util.Pair;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.the7winds.verbumSecretum.client.network.PlayerMessages;
import com.the7winds.verbumSecretum.other.Connection;
import com.the7winds.verbumSecretum.server.game.Player;

import java.io.IOException;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by the7winds on 05.12.15.
 */
public class WaitingPlayersHandler {

    private static final int MAX_PLAYERS_NUM = 4;
    private static final int MIN_PLAYERS_NUM = 2;//TODO 2

    private Server server;

    private ExecutorService acceptorExecutorService = Executors.newSingleThreadExecutor();
    private TimeChanges timeChanges;

    private Map<String, Player> readyPlayers;


    public WaitingPlayersHandler(Server server, Map<String, ConnectionHandler> allConnections) {
        this.readyPlayers = new Hashtable<>();
        this.server = server;
    }

    public Map<String, Player> getPlayers() {
        acceptorExecutorService.execute(new NewConnectionsAcceptor());
        waitPlayers();
        acceptorExecutorService.shutdownNow();
        disconnectNotPlaying();

        return readyPlayers;
    }

    private void disconnectNotPlaying() {
        // server.unregisterNsdManager();

        for (String id : server.getAllId()) {
            if (!readyPlayers.containsKey(id)) {
                server.disconnect(id);
            }
        }
    }

    private void waitPlayers() {
        timeChanges = new TimeChanges();
        timeChanges.start();

        while (timeChanges.isAlive()) {
            boolean broadcastFlag = false;
            boolean timeFlag = false;

            broadcastFlag = onMessageReceived();
            timeFlag = broadcastFlag | (readyPlayers.size() < MIN_PLAYERS_NUM);
            if (timeFlag) {
                timeChanges.timeFlagUp();
            }
            if (broadcastFlag) {
                server.broadcast(new ServerMessages.WaitingPlayersStatus(readyPlayers));
            }
        }
    }

    private boolean onMessageReceived() {
        Pair<String, String> idMsg = ConnectionHandler.popMessage();

        if (idMsg != null) {
            String id = idMsg.first;
            String strMsg = idMsg.second;

            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = jsonParser.parse(strMsg).getAsJsonObject();

            switch (jsonObject.get("HEAD").getAsString()) {

                case PlayerMessages.Leave.HEAD: {
                    return onMessageLeaveReceived(id, null);
                }

                case PlayerMessages.NotReady.HEAD: {
                    return onMessageNotReadyReceived(id, null);
                }

                case PlayerMessages.Ready.HEAD: {
                    PlayerMessages.Ready msg = new PlayerMessages.Ready();
                    msg.deserialize(strMsg);
                    return onMessageReadyReceived(id, msg);
                }
            }
        }

        return false;
    }

    private boolean onMessageReadyReceived(String id, PlayerMessages.Ready msg) {
        if (readyPlayers.size() < MAX_PLAYERS_NUM && !readyPlayers.containsKey(id)) {
            readyPlayers.put(id, server.createPlayer(id, msg.getName()));
            return true;
        } else {
            return false;
        }
    }

    private boolean onMessageNotReadyReceived(String id, PlayerMessages.NotReady msg) {
        readyPlayers.remove(id);

        return true;
    }

    private boolean onMessageLeaveReceived(String id, PlayerMessages.Leave msg) {
        boolean res = false;

        if (readyPlayers.containsKey(id)) {
            readyPlayers.remove(id);
            res = true;
        }

        server.disconnect(id);

        return res;
    }

    private class NewConnectionsAcceptor implements Runnable {

        private int idsCounter = 0;

        @Override
        public void run() {
            while (!Thread.interrupted()) try {
                Socket socket = server.acceptSocket();
                onPlayerConnected(socket);
            } catch (IOException ignored) {
            }
        }

        private void onPlayerConnected(Socket socket) throws IOException {
            String id = Integer.valueOf(idsCounter++).toString();

            Connection connection = new Connection(socket);
            ConnectionHandler connectionHandler = new ConnectionHandler(id, connection);

            server.addConnection(id, connectionHandler);

            connectionHandler.open();
        }
    }

    private class TimeChanges extends Thread {
        private static final int delay = 3000;
        private AtomicBoolean timeFlag = new AtomicBoolean(true);

        @Override
        public void run() {
            try {
                while (timeFlag.get()) {
                    timeFlag.set(false);
                    Thread.sleep(delay);
                }
            } catch (InterruptedException ignored) {
            }
        }

        public void timeFlagUp() {
            timeFlag.set(true);
        }
    }
}
