package com.the7winds.verbumSecretum.client.other;

import com.the7winds.verbumSecretum.other.Message;

/**
 * Created by the7winds on 05.12.15.
 */
public class Events {

    public static class GameActivityInited {
    }

    public static class ReceivedMessage {
        public ReceivedMessage(String message) {
            this.msg = message;
        }

        public String msg;
    }

    public static class ClientServiceError {
    }

    public static class SendToServerEvent {

        public SendToServerEvent(Message message) {
            this.message = message;
        }

        public Message message;
    }

    public static class StopServer {
    }

    public static class ServerNotFoundError {
    }

    public static class StopClientService {
    }
}
