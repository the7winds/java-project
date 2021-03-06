package com.the7winds.verbumSecretum.server.network;

import android.app.IntentService;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import com.the7winds.verbumSecretum.server.game.Player;
import com.the7winds.verbumSecretum.utils.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

/**
 * Created by the7winds on 25.10.15.
 */
public class Server extends IntentService {

    private static final String TAG = "SERVER";

    private static final int SERVER_ACCEPT_TIMEOUT = 100;
    public static final String SERVICE_NAME = "VERBUM_SECRETUM_SERVER";

    private NsdManager nsdManager;
    private NsdManager.RegistrationListener registrationListener = new NsdManager.RegistrationListener() {

        @Override
        public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
            // Save the service name.  Android may have changed it in order to
            // resolve a conflict, so update the name you initially requested
            // with the name Android actually used.
        }

        @Override
        public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            // Registration failed!  Put debugging code here to determine why.
        }

        @Override
        public void onServiceUnregistered(NsdServiceInfo arg0) {
            // Service has been unregistered.  This only happens when you call
            // NsdManager.unregisterService() and pass in this listener.
        }

        @Override
        public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            // Unregistration failed.  Put debugging code here to determine why.
        }
    };

    private ServerSocket serverSocket;

    private Map<String, ConnectionHandler> allConnections =
            Collections.synchronizedMap(new Hashtable<String, ConnectionHandler>());

    public Server() {
        super(SERVICE_NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            serverSocket = new ServerSocket(0);
            serverSocket.setSoTimeout(SERVER_ACCEPT_TIMEOUT);

            Log.d(TAG, "register service");
            registerService();

            Log.d(TAG, "waits players");
            WaitingPlayersHandler waitingPlayersHandler = new WaitingPlayersHandler(this, allConnections);
            Map<String, Player> players = waitingPlayersHandler.getPlayers();

            Log.d(TAG, "game generated");
            GameHandler gameHandler = new GameHandler(this, players);

            Log.d(TAG, "game start");
            gameHandler.startGame();

            Log.d(TAG, "game play");
            gameHandler.playGame();

            Log.d(TAG, "game finish");
            gameHandler.finishGame();

            terminate();
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            nsdManager.unregisterService(registrationListener);
        } catch (ServerExceptions.ServerDeviceDisconnected
                | ServerExceptions.ActivePlayerDisconnected serverException) {
            Log.e(TAG, serverException.toString());
            terminate();
        }
    }

    private void registerService() {
        NsdServiceInfo nsdServiceInfo = new NsdServiceInfo();

        nsdServiceInfo.setServiceName(SERVICE_NAME);
        nsdServiceInfo.setServiceType("_http._tcp");
        nsdServiceInfo.setPort(serverSocket.getLocalPort());

        nsdManager = (NsdManager) getSystemService(NSD_SERVICE);

        nsdManager.registerService(nsdServiceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener);
    }

    public synchronized void broadcast(Message message) {
        for (String id : allConnections.keySet()) {
            sendTo(id, message);
        }
    }

    public synchronized void sendTo(String id, Message message) {
        ConnectionHandler connectionHandler = allConnections.get(id);
        connectionHandler.send(message);
    }

    public Socket acceptSocket() throws IOException {
        return serverSocket.accept();
    }

    public void terminate() {
        Log.d(TAG, "start terminate");
        nsdManager.unregisterService(registrationListener);

        try {
            for (ConnectionHandler handler : allConnections.values()) {
                handler.send(new ServerMessages.Disconnected());
                handler.close();
            }

            serverSocket.close();
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        } finally {
            Log.d(TAG, "stopped");
            stopSelf();
        }
    }

    public synchronized void addConnection(String id, ConnectionHandler connectionHandler) {
        Log.d(TAG, "player connected");
        allConnections.put(id, connectionHandler);
    }

    public synchronized void removeConnection(String id) {
        try {
            ConnectionHandler handler = allConnections.remove(id);
            if (handler != null) {
                handler.close();
            }
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
    }

    public synchronized void disconnect(String id) {
        try {
            sendTo(id, new ServerMessages.Disconnected());
            ConnectionHandler handler = allConnections.remove(id);
            if (handler != null) {
                handler.close();
            }
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
    }

    public Set<String> getAllId() {
        return allConnections.keySet();
    }

    public synchronized Player createPlayer(String id, String name) {
        return new Player(allConnections.get(id), name);
    }
}

