package com.the7winds.verbumSecretum.server.network;

import android.util.Pair;

import com.the7winds.verbumSecretum.other.Connection;
import com.the7winds.verbumSecretum.other.Message;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by the7winds on 05.12.15.
 */
public class ConnectionHandler {

    private final int TIMEOUT = 100;
    private final String id;
    private final Connection connection;

    private final Queue<Message> sendMessageQueue = new LinkedList<>();
    private static final Queue<Pair<String, String>> receivedMessageQueue = new LinkedList<>();

    ExecutorService executor = Executors.newFixedThreadPool(2);

    public ConnectionHandler(String id, Connection connection) {
        this.id = id;
        this.connection = connection;
        send(new ServerMessages.Connected(id));
    }

    public void open() {
        executor.execute(new SendTask());
        executor.execute(new ReceiveTask());
    }

    public void close() {
        executor.shutdownNow();
        try {
            connection.close();
        } catch (IOException ignored) {
            // TODO
        }
    }

    public static Pair<String, String> popMessage() {
        synchronized (receivedMessageQueue) {
            if (!receivedMessageQueue.isEmpty()) {
                return receivedMessageQueue.remove();
            }
        }

        return null;
    }

    public void send(Message message) {
        synchronized (sendMessageQueue) {
            sendMessageQueue.add(message);
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public String getId() {
        return id;
    }

    private class SendTask implements Runnable {
        @Override
        public void run() {
            while (!connection.isClosed() && !Thread.interrupted()) {
                if (!sendMessageQueue.isEmpty()) {
                    Message message;
                    synchronized (sendMessageQueue) {
                        message = sendMessageQueue.remove();
                    }
                    connection.send(message.serialize());
                }
            }
        }
    }

    private class ReceiveTask implements Runnable {
        @Override
        public void run() {
            while (!connection.isClosed() && !Thread.interrupted()) {
                String msg = connection.receive(TIMEOUT);
                if (msg != null) {
                    synchronized (receivedMessageQueue) {
                        receivedMessageQueue.add(new Pair<>(id, msg));
                    }
                }
            }
        }
    }
}
