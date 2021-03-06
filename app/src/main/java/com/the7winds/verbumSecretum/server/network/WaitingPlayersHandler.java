package com.the7winds.verbumSecretum.server.network;

import android.os.SystemClock;
import android.util.Pair;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.the7winds.verbumSecretum.client.network.PlayerMessages;
import com.the7winds.verbumSecretum.client.other.ClientUtils;
import com.the7winds.verbumSecretum.server.game.Player;
import com.the7winds.verbumSecretum.utils.Connection;

import java.io.IOException;
import java.net.Socket;
import java.sql.Time;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by the7winds on 05.12.15.
 */
public class WaitingPlayersHandler {

    private static final int MAX_PLAYERS_NUM = 4;
    private static final int MIN_PLAYERS_NUM = 2;

    private Server server;

    private ExecutorService acceptorExecutorService = Executors.newSingleThreadExecutor();

    private Map<String, Player> readyPlayers;


    public WaitingPlayersHandler(Server server, Map<String, ConnectionHandler> allConnections) {
        this.readyPlayers = new Hashtable<>();
        this.server = server;
    }

    public Map<String, Player> getPlayers() throws ServerExceptions.ServerDeviceDisconnected {
        acceptorExecutorService.execute(new NewConnectionsAcceptor());
        waitPlayers();
        acceptorExecutorService.shutdownNow();
        disconnectNotPlaying();

        return readyPlayers;
    }

    private void disconnectNotPlaying() {
        Set<String> ids = new TreeSet<>(server.getAllId());
        for (String id : ids) {
            if (!readyPlayers.containsKey(id)) {
                server.disconnect(id);
            }
        }
    }

    private void waitPlayers() throws ServerExceptions.ServerDeviceDisconnected {
        final int TIMEOUT = 3000; // MILLISECONDS
        long checkpoint = System.currentTimeMillis();

        while (true) {
            boolean broadcastFlag = false;
            boolean timeFlag = false;

            broadcastFlag = onMessageReceived();
            timeFlag = broadcastFlag | (readyPlayers.size() < MIN_PLAYERS_NUM);
            if (timeFlag) {
                checkpoint = System.currentTimeMillis();
            }
            if (broadcastFlag) {
                server.broadcast(new ServerMessages.WaitingPlayersStatus(readyPlayers));
            }

            if (System.currentTimeMillis() - checkpoint >= TIMEOUT) {
                break;
            }
        }
    }

    private boolean onMessageReceived() throws ServerExceptions.ServerDeviceDisconnected {
        Pair<String, String> idMsg = ConnectionHandler.popMessage();

        if (idMsg != null) {
            String id = idMsg.first;
            String strMsg = idMsg.second;

            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = jsonParser.parse(strMsg).getAsJsonObject();

            switch (jsonObject.get("HEAD").getAsString()) {

                case PlayerMessages.Leave.HEAD: {
                    return onMessageLeaveReceived(id);
                }

                case PlayerMessages.NotReady.HEAD: {
                    return onMessageNotReadyReceived(id);
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

    private boolean onMessageNotReadyReceived(String id) {
        readyPlayers.remove(id);
        return true;
    }

    private boolean onMessageLeaveReceived(String id) throws ServerExceptions.ServerDeviceDisconnected {
        if (id.equals(ClientUtils.Data.id)) {
            throw new ServerExceptions.ServerDeviceDisconnected();
        }

        server.removeConnection(id);

        if (readyPlayers.containsKey(id)) {
            readyPlayers.remove(id);
            return true;
        }

        return false;
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
            server.sendTo(id, new ServerMessages.Connected(id));
            server.sendTo(id, new ServerMessages.WaitingPlayersStatus(readyPlayers));

            connectionHandler.open();
        }
    }

}
