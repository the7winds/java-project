package com.the7winds.verbumSecretum.client.network;

import com.the7winds.verbumSecretum.client.other.Events;
import com.the7winds.verbumSecretum.other.Connection;
import com.the7winds.verbumSecretum.other.Message;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.greenrobot.event.EventBus;

/**
 * Created by the7winds on 27.11.15.
 */
public class ConnectionHandler {

    private final static int TIMEOUT = 100;
    private final Connection connection;
    private final Queue<Message> sendMessageQueue = new LinkedList<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);

    public ConnectionHandler(Connection connection) {
        this.connection = connection;
    }

    public void open() {
        executorService.execute(new SendTask());
        executorService.execute(new ReceiveTask());
    }

    public void close() throws IOException {
        executorService.shutdownNow();
        connection.close();
    }

    private class ReceiveTask implements Runnable {
        @Override
        public void run() {
            while (!connection.isClosed() && !Thread.interrupted()) {
                String msg = connection.receive(TIMEOUT);
                if (msg != null) {
                    EventBus.getDefault().post(new Events.ReceivedMessage(msg));
                }
            }
        }
    }

    private class SendTask implements Runnable {
        @Override
        public void run() {
            while (!connection.isClosed() && !Thread.interrupted()) {
                Message message = null;

                synchronized (sendMessageQueue) {
                    if (!sendMessageQueue.isEmpty()) {
                        message = sendMessageQueue.remove();
                    }
                }

                if (message != null) {
                    connection.send(message.serialize());
                }
            }
        }
    }

    public void send(Message message) {
        synchronized (sendMessageQueue) {
            sendMessageQueue.add(message);
        }
    }
}