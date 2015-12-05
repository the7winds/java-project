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
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.the7winds.verbumSecretum.server.network.ConnectionHandler.getReceivedMessageQueue;

/**
 * Created by the7winds on 05.12.15.
 */
public class WaitingPlayersHandler {

    private static final int MAX_PLAYERS_NUM = 4;
    private static final int MIN_PLAYERS_NUM = 2;

    private Server server;

    private ExecutorService acceptorExecutorService = Executors.newSingleThreadExecutor();
    private ExecutorService timeChangesExecutorService = Executors.newSingleThreadExecutor();

    private Map<String, ConnectionHandler> allConnections;
    private Map<String, Player> readyPlayers;


    public WaitingPlayersHandler(Server server, Map<String, ConnectionHandler> allConnections) {
        this.allConnections = allConnections;
        this.readyPlayers = new Hashtable<>();
        this.server = server;
    }

    public Map<String, Player> getPlayers() {
        acceptorExecutorService.execute(new NewConnectionsAcceptor());
        waitPlayers();
        acceptorExecutorService.shutdownNow();
        server.unregisterNsd();
        disconnectNotPlaying();

        return readyPlayers;
    }

    private void disconnectNotPlaying() {
        Set<String> notPlaying = allConnections.keySet();
        notPlaying.removeAll(readyPlayers.keySet());

        for (String id : notPlaying) {
            ConnectionHandler connectionHandler = allConnections.get(id);
            server.sendTo(id, new ServerMessages.Disconnected());
            connectionHandler.close();
            allConnections.remove(id);
        }
    }

    private void waitPlayers() {
        timeChangesExecutorService.submit(new TimeChanges());

        //while (!timeChangesExecutorService.isTerminated()) {
        while (true) {
            boolean broadcastFlag = false;
            boolean timeFlag = false;

            broadcastFlag = onMessageReceived();
            timeFlag = broadcastFlag | (readyPlayers.size() < MIN_PLAYERS_NUM);
            if (timeFlag) {
            //    timeChangesExecutorService.shutdownNow();
            //    timeChangesExecutorService = Executors.newSingleThreadExecutor();
            //    timeChangesExecutorService.submit(new TimeChanges());
            }
            if (broadcastFlag) {
                server.broadcast(new ServerMessages.WaitingPlayersStatus(readyPlayers));
            }
        }
    }

    private boolean onMessageReceived() {
        Pair<String, String> idMsg = null;

        synchronized (getReceivedMessageQueue()) {
            if (!getReceivedMessageQueue().isEmpty()) {
                idMsg = getReceivedMessageQueue().remove();
            }
        }

        if (idMsg != null) {
            String id = idMsg.first;
            String strMsg = idMsg.second;

            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = jsonParser.parse(strMsg).getAsJsonObject();

            switch (jsonObject.get("HEAD").getAsString()) {

                case PlayerMessages.Leave.HEAD: {
                    PlayerMessages.Leave msg = new PlayerMessages.Leave();
                    msg.deserialize(strMsg);
                    return onMessageLeaveReceived(id, msg);
                }

                case PlayerMessages.NotReady.HEAD: {
                    PlayerMessages.NotReady msg = new PlayerMessages.NotReady();
                    msg.deserialize(strMsg);
                    return onMessageNotReadyReceived(id, msg);
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
            ConnectionHandler connectionHandler = allConnections.get(id);
            readyPlayers.put(id, new Player(connectionHandler, msg.getName()));
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

        ConnectionHandler connectionHandler = allConnections.remove(id);
        connectionHandler.close();

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

    private class TimeChanges implements Runnable {
        private int delay = 10000;

        @Override
        public void run() {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException ignored) {
            }
        }
    }
}
