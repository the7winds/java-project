package com.the7winds.verbumSecretum.client.network;

import com.the7winds.verbumSecretum.client.other.Events;
import com.the7winds.verbumSecretum.other.Connection;
import com.the7winds.verbumSecretum.other.Message;
import com.the7winds.verbumSecretum.server.network.ServerMessages;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import de.greenrobot.event.EventBus;

/**
 * Created by the7winds on 27.11.15.
 */
public class ConnectionHandler implements Runnable {

    private final Connection connection;
    private final int TIMEOUT = 100;
    private final Queue<Message> sendMessageQueue = new LinkedList<>();

    public ConnectionHandler(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        while (!connection.isClosed() && !Thread.interrupted()) {
            onReceiveTask();
            onSendTask();
        }

        if (Thread.interrupted()) try {
                connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onReceiveTask() {
        if (!connection.isClosed()) {
            String msg = connection.receive(TIMEOUT);
            if (msg != null) {
                EventBus.getDefault().post(new Events.ReceivedMessage(msg));
            }
        }
    }

    private void onSendTask() {
        synchronized (sendMessageQueue) {
            if (!sendMessageQueue.isEmpty()) {
                Message message = sendMessageQueue.remove();
                connection.send(message.serialize());
            }
        }
    }

    public void send(Message message) {
        synchronized (sendMessageQueue) {
            sendMessageQueue.add(message);
        }
    }
}