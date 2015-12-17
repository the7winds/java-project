package com.the7winds.verbumSecretum.client.network;

import com.the7winds.verbumSecretum.client.other.Events;
import com.the7winds.verbumSecretum.utils.Connection;
import com.the7winds.verbumSecretum.utils.Message;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
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
    private final CountDownLatch closeLatch = new CountDownLatch(1);

    public ConnectionHandler(Connection connection) {
        this.connection = connection;
    }

    public void open() {
        executorService.execute(new SendTask());
        executorService.execute(new ReceiveTask());
    }

    public boolean isClosed() {
        return connection.isClosed();
    }

    public void close() {
        try {
            executorService.shutdownNow();
            closeLatch.await();
            connection.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
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

            while (connection.inputReady()) {
                String msg = connection.receive(TIMEOUT);
                EventBus.getDefault().post(new Events.ReceivedMessage(msg));
            }

            closeLatch.countDown();
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