package com.the7winds.verbumSecretum.client.network;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.the7winds.verbumSecretum.client.other.ClientData;
import com.the7winds.verbumSecretum.client.other.Events;
import com.the7winds.verbumSecretum.other.Message;
import com.the7winds.verbumSecretum.server.network.ServerMessages;

import java.util.Hashtable;

import de.greenrobot.event.EventBus;

/**
 * Created by the7winds on 05.12.15.
 */
public class MessageHandler {

    private boolean gameActivityInited = false;

    // handler
    public void onEvent(Events.ReceivedMessage receivedMessage) {
        String msg = receivedMessage.msg;
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = jsonParser.parse(msg).getAsJsonObject();

        switch (jsonObject.get("HEAD").getAsString()) {
            case ServerMessages.GameOver.HEAD:
                onGameOver(msg);
                break;
            case ServerMessages.Correct.HEAD:
                onCorrect();
                break;
            case ServerMessages.InvalidMove.HEAD:
                onInvalidMoveMessage();
                break;
            case ServerMessages.YourTurn.HEAD:
                onYourTurnMessage(msg);
                break;
            case ServerMessages.GameState.HEAD:
                onGameStateMessage(msg);
                break;
            case ServerMessages.GameStart.HEAD:
                onStartMessage(msg);
                break;
            case ServerMessages.GameStarting.HEAD:
                onStartingMessage();
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

    private void onCorrect() {
        EventBus.getDefault().post(new ServerMessages.Correct());
    }

    public void onEvent(Events.GameActivityInited event) {
        gameActivityInited = true;
    }

    // Check
    private void onGameOver(String msg) {
        ServerMessages.GameOver message = new ServerMessages.GameOver();
        message.deserialize(msg);

        EventBus.getDefault().post(message);
    }

    private void onInvalidMoveMessage() {
        EventBus.getDefault().post(new ServerMessages.InvalidMove());
    }

    private void onYourTurnMessage(String msg) {
        ServerMessages.YourTurn message = new ServerMessages.YourTurn();
        message.deserialize(msg);

        while (!gameActivityInited);

        EventBus.getDefault().post(message);
    }

    private void onGameStateMessage(String msg) {
        ServerMessages.GameState message = new ServerMessages.GameState();
        message.deserialize(msg);

        while (!gameActivityInited);

        EventBus.getDefault().post(message);
    }

    // +--OK-----------------------------------------
    // |
    // V
    private void onStartMessage(String msg) {
        ServerMessages.GameStart message = new ServerMessages.GameStart();
        message.deserialize(msg);

        ClientData.hand.add(message.getIdToCard().get(ClientData.id));
        ClientData.playersNames = new Hashtable<>(message.getIdToNames());
        ClientData.activePlayersNames = new Hashtable<>(message.getIdToNames());

        while (!gameActivityInited);

        EventBus.getDefault().post(message);
    }

    private void onStartingMessage() {
        EventBus.getDefault().post(new ServerMessages.GameStarting());
    }

    private void onWaitingStatusMessage(String msg) {
        ServerMessages.WaitingPlayersStatus message = new ServerMessages.WaitingPlayersStatus();
        message.deserialize(msg);

        ClientData.playersNames = message.getPlayersNames();
        EventBus.getDefault().post(message);
    }

    private void onDisconnected() {
        // TODO
    }

    private void onConnectedMessage(String msg) {
        ServerMessages.Connected message = new ServerMessages.Connected();
        message.deserialize(msg);

        ClientData.id = message.getId();

        EventBus.getDefault().post(message);
    }
}
