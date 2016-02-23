package com.the7winds.verbumSecretum.server.network;

import android.util.Log;
import android.util.Pair;

import com.the7winds.verbumSecretum.utils.Connection;
import com.the7winds.verbumSecretum.utils.Message;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
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

    private final Queue<Message> sendMessageQueue = new LinkedList<>();
    private static final Queue<Pair<String, String>> receivedMessageQueue = new LinkedList<>();

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

    private final CountDownLatch closeLatch = new CountDownLatch(1);

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
            closeLatch.await();
        } catch (InterruptedException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            connection.close();
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

    public String getId() {
        return id;
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

            if (!connection.isClosed()) {
                synchronized (sendMessageQueue) {
                    while (!sendMessageQueue.isEmpty()) {
                        connection.send(sendMessageQueue.remove().serialize());
                    }
                }
            }

            closeLatch.countDown();
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
