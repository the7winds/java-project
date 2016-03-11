package com.the7winds.verbumSecretum.server.game;

import java.util.Map;

import static com.the7winds.verbumSecretum.server.game.Cards.STAFF_CARD;

/**
 * Created by the7winds on 16.02.16.
 */
public class CardUtils {

    public static boolean cantMove(String playerId, Game game) {
        for (Map.Entry<String, Player> entry : game.getActivePlayers().entrySet()) {
            if (!entry.getKey().equals(playerId) && (entry.getValue().getLastPlayedCard() == null
                    || entry.getValue().getLastPlayedCard() != STAFF_CARD)) {
                return false;
            }
        }
        return true;
    }

    public static class CantMoveException extends Exception {
    }

    public static String genDescription(String player, Card card) {
        return player + " played " + Integer.toString(card.getWeight());
    }

    public static String genDescription(String player, Card card, String opponent) {
        return player + " played " + Integer.toString(card.getWeight()) + " on " + opponent;
    }

    public static String genDescription(String player, Card card, String opponent, Card role) {
        return player + " suppose that " + opponent + "`s role is " + Integer.toString(role.getWeight());
    }
}
