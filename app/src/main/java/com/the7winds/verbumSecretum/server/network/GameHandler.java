package com.the7winds.verbumSecretum.server.network;

import android.util.Pair;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.the7winds.verbumSecretum.client.network.PlayerMessages;
import com.the7winds.verbumSecretum.server.game.Game;
import com.the7winds.verbumSecretum.server.game.Player;

import java.io.IOException;
import java.util.Map;

/**
 * Created by the7winds on 05.12.15.
 */
public class GameHandler {

    private final Server server;
    private final Map<String, Player> players;
    private Game game;

    public GameHandler(Server server, Map<String, Player> players) {
        this.server = server;
        this.players = players;
    }

    public void startGame() {
        server.broadcast(new ServerMessages.GameStarting());

        game = new Game(players);

        server.broadcast(new ServerMessages.GameStart(game.getActivePlayers(), game.getCurrentPlayerId()));

        playGame();

        finishGame();

        server.teminate();
    }

    private void playGame() {
        server.sendTo(game.getCurrentPlayerId(), game.genYourTurnMessage());

        while (!game.isFinished()) {
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

    private void finishGame() {
        server.broadcast(new ServerMessages.GameOver(game.getResults(), game.getActivePlayers()));
    }

    private void onMoveMessage(String id, PlayerMessages.Move message) {
        Game.Move move = message.getMove();

        if (game.isValidMove(move)) {
            game.applyMove(move);
            server.sendTo(id, new ServerMessages.Correct());
            if (game.nextPlayer()) {
                server.broadcast(game.genGameStateMessage());
                server.sendTo(game.getCurrentPlayerId(), game.genYourTurnMessage());
            }
        } else {
            server.sendTo(id, new ServerMessages.InvalidMove());
        }
    }

    private void onLeave(String id, PlayerMessages.Leave message) {
        if (game.isActivePlayer(id)) {
            server.teminate();
        } else {
            try {
                server.disconnect(id);
            } catch (IOException ignored) {

            }
        }
    }
}
