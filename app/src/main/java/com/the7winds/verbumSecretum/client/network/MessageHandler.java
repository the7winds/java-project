package com.the7winds.verbumSecretum.client.network;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.the7winds.verbumSecretum.client.other.ClientData;
import com.the7winds.verbumSecretum.client.other.Events;
import com.the7winds.verbumSecretum.other.Message;
import com.the7winds.verbumSecretum.server.network.ServerMessages;

import de.greenrobot.event.EventBus;

/**
 * Created by the7winds on 05.12.15.
 */
public class MessageHandler {

    // fields
    private enum State {RECEIVE_ID, WAITING_PLAYERS, PLAYING}
    private State state = State.RECEIVE_ID;

    // handler
    public void onEvent(Events.ReceivedMessage receivedMessage) {
        String msg = receivedMessage.msg;
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = jsonParser.parse(msg).getAsJsonObject();

        switch (jsonObject.get("HEAD").getAsString()) {
            case ServerMessages.GameOver.HEAD:
                // res = new ServerMessages.GameOver().deserialize(s);
                break;
            case ServerMessages.InvalidMove.HEAD:
                // res = new ServerMessages.InvalidMove().deserialize(s);
                break;
            case ServerMessages.YourTurn.HEAD:
                // res = new ServerMessages.YourTurn().deserialize(s);
                break;
            case ServerMessages.GameState.HEAD:
                // res = new ServerMessages.GameState().deserialize(s);
                break;
            case ServerMessages.GameStart.HEAD:
                // res = new ServerMessages.GameStart().deserialize(s);
                break;
            case ServerMessages.GameStarting.HEAD:
                // onGameStarting();
                break;
            case ServerMessages.Disconnected.HEAD:
                onDisconnected();
                break;
            case ServerMessages.WaitingPlayersStatus.HEAD:
                onWaitingStatusMessage(msg);
                break;
            case ServerMessages.Connected.HEAD:
                onConnectedMessage(msg);
                break;
        }
    }

    private void onWaitingStatusMessage(String msg) {
        ServerMessages.WaitingPlayersStatus message = new ServerMessages.WaitingPlayersStatus();
        message.deserialize(msg);

        ClientData.playersNames = message.getPlayersNames();
        EventBus.getDefault().post(new Events.UpdateRoomEvent(ClientData.playersNames));
    }

    private void onDisconnected() {
        // TODO
    }

    private void onConnectedMessage(String msg) {
        ServerMessages.Connected message = new ServerMessages.Connected();
        message.deserialize(msg);

        ClientData.id = message.getId();
        state = State.WAITING_PLAYERS;

        EventBus.getDefault().post(new Events.Connected());
    }
}
