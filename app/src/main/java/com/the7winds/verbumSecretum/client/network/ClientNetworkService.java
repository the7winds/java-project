package com.the7winds.verbumSecretum.client.network;

import android.app.IntentService;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import com.the7winds.verbumSecretum.client.other.ClientUtils;
import com.the7winds.verbumSecretum.client.other.Events;
import com.the7winds.verbumSecretum.other.Connection;
import com.the7winds.verbumSecretum.other.Message;
import com.the7winds.verbumSecretum.server.network.Server;

import java.net.InetAddress;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import de.greenrobot.event.EventBus;

/**
 * Created by the7winds on 23.11.15.
 */
public class ClientNetworkService extends IntentService {

    public final static int CONNECTING_TIMEOUT = 20000;

    private InetAddress server;
    private int port;

    private NsdManager nsdManager;
    private NsdManager.DiscoveryListener discoveryListener = new NsdManager.DiscoveryListener() {

        @Override
        public void onStartDiscoveryFailed(String serviceType, int errorCode) {

        }

        @Override
        public void onStopDiscoveryFailed(String serviceType, int errorCode) {

        }

        @Override
        public void onDiscoveryStarted(String serviceType) {

        }

        @Override
        public void onDiscoveryStopped(String serviceType) {

        }

        @Override
        public void onServiceFound(NsdServiceInfo serviceInfo) {
            if (serviceInfo.getServiceName().equals(Server.SERVICE_NAME)) {
                nsdManager.resolveService(serviceInfo, resolveListener);
            }
        }

        @Override
        public void onServiceLost(NsdServiceInfo serviceInfo) {

        }
    };
    private NsdManager.ResolveListener resolveListener = new NsdManager.ResolveListener() {
        @Override
        public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
        }

        @Override
        public void onServiceResolved(NsdServiceInfo serviceInfo) {
            port = serviceInfo.getPort();
            server = serviceInfo.getHost();
        }
    };

    private ConnectionHandler connectionHandler;
    private ExecutorService executorService = Executors.newCachedThreadPool();
    private MessageHandler messageHandler = new MessageHandler();

    public ClientNetworkService() {
        super("Client");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        EventBus.getDefault().register(this);
        EventBus.getDefault().register(messageHandler);

        Log.i("Client", "starting");

        try {
            executorService.submit(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    nsdManager = (NsdManager) getSystemService(NSD_SERVICE);
                    nsdManager.discoverServices("_http._tcp.", NsdManager.PROTOCOL_DNS_SD, discoveryListener);

                    while (server == null || Thread.interrupted()) ;

                    nsdManager.stopServiceDiscovery(discoveryListener);

                    Connection connection = new Connection(server, port);
                    connectionHandler = new ConnectionHandler(connection);
                    connectionHandler.open();

                    return null;
                }
            }).get(CONNECTING_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            e.printStackTrace();
            EventBus.getDefault().post(new Events.ServerNotFoundError());
            if (nsdManager != null) {
                nsdManager.stopServiceDiscovery(discoveryListener);
            }
            errorHandle();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            EventBus.getDefault().post(new Events.ClientServiceError());
            errorHandle();
        }
    }

    private void errorHandle() {
        EventBus.getDefault().unregister(this);
        EventBus.getDefault().unregister(messageHandler);
        stopSelf();
    }

    public void onEvent(Events.SendToServerEvent event) {
        Message message = event.message;
        if (connectionHandler != null) {
            connectionHandler.send(message);
        }
    }

    public void onEvent(Events.StopClientService event) {
        Log.i("Client" + "(" + ClientUtils.Data.id + ")", "event stop");
        EventBus.getDefault().unregister(messageHandler);
        EventBus.getDefault().unregister(this);

        if (connectionHandler != null && connectionHandler.isClosed()) {
            connectionHandler.close();
        }

        Log.i("Client" + "(" + ClientUtils.Data.id + ")", "stopped");
        stopSelf();
    }
}
