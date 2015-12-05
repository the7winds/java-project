package com.the7winds.verbumSecretum.client.other;

import com.the7winds.verbumSecretum.other.Message;

import java.util.Map;

/**
 * Created by the7winds on 05.12.15.
 */
public class Events {

    private Events() {}

    public static class ReceivedMessage {
        public ReceivedMessage(String message) {
            this.msg = message;
        }

        public String msg;
    }

    public static class ClientServiceError {

    }

    public static class Disconnected {

    }

    public static class StartGame {

    }

    public static class Connected {
    }

    public static class UpdateRoomEvent {

        public UpdateRoomEvent(Map<String, String> playersNames) {
            this.playersNames = playersNames;
        }

        public Map<String, String> playersNames;
    }

    public static class SendToServerEvent {

        public SendToServerEvent(Message message) {
            this.message = message;
        }

        public Message message;
    }

    public static class StopClientService {
    }

    public static class StopServer {
    }

    public static class ServerNotFoundError {
    }
}
