package com.the7winds.verbumSecretum.client.network;

import android.app.IntentService;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;

import com.the7winds.verbumSecretum.client.other.Events;
import com.the7winds.verbumSecretum.other.Connection;
import com.the7winds.verbumSecretum.other.Message;
import com.the7winds.verbumSecretum.server.network.Server;

import java.net.InetAddress;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import de.greenrobot.event.EventBus;

/**
 * Created by the7winds on 23.11.15.
 */
public class ClientNetworkService extends IntentService {

    private InetAddress server = null;
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
    private ExecutorService executor = Executors.newCachedThreadPool();
    private MessageHandler messageHandler = new MessageHandler();


    public ClientNetworkService() {
        super("Client");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            EventBus.getDefault().register(this);
            EventBus.getDefault().register(messageHandler);
            Future<Object> connected = executor.submit(new Connecting());
            connected.get(Connecting.CONNECTING_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            EventBus.getDefault().unregister(this);
            EventBus.getDefault().unregister(messageHandler);

            nsdManager.stopServiceDiscovery(discoveryListener);
            EventBus.getDefault().post(new Events.ServerNotFoundError());
            stopSelf();
        } catch (InterruptedException | ExecutionException e) {
            EventBus.getDefault().unregister(this);
            EventBus.getDefault().unregister(messageHandler);

            nsdManager.stopServiceDiscovery(discoveryListener);
            EventBus.getDefault().post(new Events.ClientServiceError());
            stopSelf();
        }
    }

    private class Connecting implements Callable<Object> {

        public final static int CONNECTING_TIMEOUT = 1000000;

        @Override
        public Object call() throws Exception {
            nsdManager = (NsdManager) getSystemService(NSD_SERVICE);

            while (server == null || Thread.interrupted()) {
                nsdManager.discoverServices("_http._tcp.", NsdManager.PROTOCOL_DNS_SD, discoveryListener);
            }

            Connection connection = new Connection(server, port);
            connectionHandler = new ConnectionHandler(connection);

            nsdManager.stopServiceDiscovery(discoveryListener);

            executor.submit(connectionHandler);

            return null;
        }
    }

    public void onEvent(Events.SendToServerEvent sendToServerEvent) {
        Message message = sendToServerEvent.message;
        connectionHandler.send(message);
    }

    public void onEvent(Events.StopClientService stopClientService) {
        EventBus.getDefault().unregister(messageHandler);
        EventBus.getDefault().unregister(this);

        nsdManager.stopServiceDiscovery(discoveryListener);
        executor.shutdownNow();

        stopSelf();
    }
}
