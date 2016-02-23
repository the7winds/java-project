package com.the7winds.verbumSecretum.server.network;

import android.util.Log;
import android.util.Pair;

import com.the7winds.verbumSecretum.utils.Connection;
import com.the7winds.verbumSecretum.utils.Message;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by the7winds on 05.12.15.
 */
public class ConnectionHandler {

    private static final String TAG ="ConnectionHandler";
    private final static int TIMEOUT = 100;
    private final String id;
    private final Connection connection;

    private final Queue<Message> sendMessageQueue = new ConcurrentLinkedQueue<>();
    private static final Queue<Pair<String, String>> receivedMessageQueue = new ConcurrentLinkedQueue<>();

    private final ExecutorService executorService =
            new ThreadPoolExecutor(2, 2, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>()) {
                private static final String TAG = "ThreadPoolExecutor";
                @Override
                protected void afterExecute(Runnable r, Throwable t) {
                    super.afterExecute(r, t);
                    if (t != null) {
                        Log.e(TAG, t.getMessage());
                    }
                }
            }; // Executors.newFixedThreadPool(2);

    public ConnectionHandler(String id, Connection connection) {
        this.id = id;
        this.connection = connection;
    }

    public void open() {
        executorService.execute(new SendTask());
        executorService.execute(new ReceiveTask());
    }

    public void close() throws IOException {
        try {
            executorService.shutdownNow();
            executorService.awaitTermination(TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            connection.close();
        }
    }

    public static Pair<String, String> popMessage() {
        return receivedMessageQueue.poll();
    }

    public void send(Message message) {
        sendMessageQueue.add(message);
    }

    public String getId() {
        return id;
    }

    private class SendTask implements Runnable {
        @Override
        public void run() {
            while (!connection.isClosed() && !Thread.interrupted()) {
                Message message = sendMessageQueue.poll();

                if (message != null) {
                    connection.send(message.serialize());
                }
            }

            if (!connection.isClosed()) {
                while (!sendMessageQueue.isEmpty()) {
                    connection.send(sendMessageQueue.remove().serialize());
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
                    receivedMessageQueue.add(new Pair<>(id, msg));
                }
            }
        }
    }
}
