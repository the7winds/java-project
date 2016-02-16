package com.the7winds.verbumSecretum.server.network;

import android.util.Pair;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.the7winds.verbumSecretum.client.network.PlayerMessages;
import com.the7winds.verbumSecretum.client.other.ClientUtils;
import com.the7winds.verbumSecretum.server.game.Game;
import com.the7winds.verbumSecretum.server.game.GameMessages;
import com.the7winds.verbumSecretum.server.game.Move;
import com.the7winds.verbumSecretum.server.game.Player;

import java.util.Map;

/**
 * Created by the7winds on 05.12.15.
 */
public class GameHandler {

    private final Server server;
    private final Map<String, Player> players;

    public GameHandler(Server server, Map<String, Player> players) {
        this.server = server;
        this.players = players;
    }

    public void startGame() {
        server.broadcast(new ServerMessages.GameStarting());

        Game.getInstance().reset(players);

        server.broadcast(GameMessages.newStartMessage());
    }

    public void playGame()
            throws ServerExceptions.ServerDeviceDisconnected, ServerExceptions.ActivePlayerDisconnected {
        server.sendTo(Game.getInstance().getCurrentPlayerId(), GameMessages.newYourTurnMessage());

        while (!Game.getInstance().isFinished()) {
            Pair<String, String> idMsg = ConnectionHandler.popMessage();

            if (idMsg != null) {
                String id = idMsg.first;
                String msg = idMsg.second;

                JsonParser jsonParser = new JsonParser();
                JsonObject jsonObject = jsonParser.parse(msg).getAsJsonObject();

                switch (jsonObject.get("HEAD").getAsString()) {

                    case PlayerMessages.Move.HEAD:
                        PlayerMessages.Move message = new PlayerMessages.Move();
                        message.deserialize(msg);
                        onMoveMessage(id, message);
                        break;

                    case PlayerMessages.Leave.HEAD:
                        onLeave(id, null);
                        break;
                }
            }
        }
    }

    public void finishGame() {
        server.broadcast(GameMessages.newGameOverMessage());
    }

    private void onMoveMessage(String id, PlayerMessages.Move message) {
        Move move = message.getMove();

        if (Game.getInstance().checkMove(move)) {
            Game.getInstance().applyMove(move);
            server.sendTo(id, new ServerMessages.Correct());
            if (Game.getInstance().nextPlayer()) {
                server.broadcast(GameMessages.newGameStateMessage());
                server.sendTo(Game.getInstance().getCurrentPlayerId(), GameMessages.newYourTurnMessage());
            }
        } else {
            server.sendTo(id, new ServerMessages.InvalidMove());
        }
    }

    private void onLeave(String id, PlayerMessages.Leave message)
            throws ServerExceptions.ServerDeviceDisconnected, ServerExceptions.ActivePlayerDisconnected {

        if (ClientUtils.Data.id.equals(id)) {
            throw new ServerExceptions.ServerDeviceDisconnected();
        }

        if (Game.getInstance().isActivePlayer(id)) {
            throw new ServerExceptions.ActivePlayerDisconnected();
        } else {
            server.disconnect(id);
        }
    }
}
