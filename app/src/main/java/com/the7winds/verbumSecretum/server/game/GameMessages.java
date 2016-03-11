package com.the7winds.verbumSecretum.server.game;

import com.the7winds.verbumSecretum.server.network.ServerMessages;
import com.the7winds.verbumSecretum.utils.Message;

import java.util.Hashtable;
import java.util.Map;

/**
 * Created by the7winds on 16.02.16.
 */
public class GameMessages {

    public static Message newStartMessage() {
        return new ServerMessages.GameStart(Game.getInstance().getActivePlayers(), Game.getInstance().getCurrentPlayerId(), Game.CardsDeck.getInstance().size());
    }

    public static ServerMessages.GameState newGameStateMessage() {
        return new ServerMessages.GameState(Game.getInstance().getCurrentPlayerId()
                , Game.getInstance().getDescription()
                , Game.CardsDeck.getInstance().size()
                , Game.getInstance().getLastPlayed()
                , Game.getInstance().getCardsThatShouldBeShowed()
                , Game.getInstance().getActivePlayers());
    }

    public static ServerMessages.YourTurn newYourTurnMessage() {
        Card card = Game.CardsDeck.getInstance().getTopCard();
        String playerId = Game.getInstance().getCurrentPlayerId();
        Game.getInstance().getActivePlayers().get(playerId).addHandCard(card);
        return new ServerMessages.YourTurn(card);
    }

    public static ServerMessages.GameOver newGameOverMessage() {
        Map<String, String> winners = new Hashtable<>();
        Card max = Cards.GUARD_CARD;

        for (Map.Entry<String, Player> entry : Game.getInstance().getActivePlayers().entrySet()) {
            String id = entry.getKey();
            Player player = entry.getValue();

            if (player.getHandCard() == max) {
                winners.put(id, player.getName());
            } else if (player.getHandCard().getWeight() > max.getWeight()) {
                max = player.getHandCard();
                winners = new Hashtable<>();
                winners.put(id, player.getName());
            }
        }

        return new ServerMessages.GameOver(winners, Game.getInstance().getActivePlayers());
    }

}
