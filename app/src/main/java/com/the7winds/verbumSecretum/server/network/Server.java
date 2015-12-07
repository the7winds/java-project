package com.the7winds.verbumSecretum.server.network;

import android.app.IntentService;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;

import com.the7winds.verbumSecretum.client.other.Events;
import com.the7winds.verbumSecretum.other.Message;
import com.the7winds.verbumSecretum.server.game.Player;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import de.greenrobot.event.EventBus;

/**
 * Created by the7winds on 25.10.15.
 */
public class Server extends IntentService {

    private static final int SERVER_ACCEPT_TIMEOUT = 100;
    public static final String SERVICE_NAME = "VERBUM_SECRETUM_SERVER";

    private NsdServiceInfo nsdServiceInfo;
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

    private Map<String, ConnectionHandler> allConnections = new Hashtable<>();
    private Set<String> allId;

    public Server() {
        super(SERVICE_NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            EventBus.getDefault().register(this);

            serverSocket = new ServerSocket(0);
            serverSocket.setSoTimeout(SERVER_ACCEPT_TIMEOUT);

            registerService();

            WaitingPlayersHandler waitingPlayersHandler = new WaitingPlayersHandler(this, allConnections);
            Map<String, Player> players = waitingPlayersHandler.getPlayers();

            GameHandler gameHandler = new GameHandler(this, players);
            gameHandler.startGame();

            while (true) {
                int a = 1;
            }

            // stopSelf();
         /* startGame();
            playGame();
            finishGame();
            Terminate*/
        }
        catch (IOException e) {
            unregisterNsdManager();
            EventBus.getDefault().unregister(this);
            e.printStackTrace();
        }
    }

    private void registerService() {
        nsdServiceInfo = new NsdServiceInfo();

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

    public void onEvent(Events.StopServer stopServer) {
        teminate();
    }

    public void unregisterNsdManager() {
        nsdManager.unregisterService(registrationListener);
    }

    public synchronized void addConnection(String id, ConnectionHandler connectionHandler) {
        allConnections.put(id, connectionHandler);
    }

    public synchronized void disconnect(String id) {
        sendTo(id, new ServerMessages.Disconnected());
        ConnectionHandler handler = allConnections.remove(id);
        handler.close();
    }

    public void teminate() {
        for (String id : allConnections.keySet()) {
            disconnect(id);
        }

        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        EventBus.getDefault().unregister(this);
        stopSelf();
    }

    public Set<String> getAllId() {
        return allConnections.keySet();
    }

    public synchronized Player createPlayer(String id, String name) {
        return new Player(allConnections.get(id), name);
    }
}

