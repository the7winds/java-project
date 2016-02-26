package com.the7winds.verbumSecretum.server.game;

import android.content.Context;
import android.graphics.drawable.Drawable;

import java.util.Map;

/**
 * Created by the7winds on 16.02.16.
 */
public class Cards {

    private static abstract class Moves {
        protected static boolean cantMove(String playerId, Game game) {
            for (Map.Entry<String, Player> entry : game.getActivePlayers().entrySet()) {
                if (!entry.getKey().equals(playerId) && (entry.getValue().getLastPlayedCard() == null
                        || entry.getValue().getLastPlayedCard() != Card.STAFF_CARD)) {
                    return false;
                }
            }
            return true;
        }
    }

    static abstract class MoveChecker extends Moves {
        public abstract boolean checkMove(Player player, Player opponent, Move move, Game game);
    }

    static abstract class MoveApplier extends Moves{
        public abstract Map<String, Card> applyMove(Player player, Player opponent, Move move, Game game) throws CantMoveException;

        protected static void makeInactive(Player player, Game game) {
            game.removeActivePlayer(player.getId());
        }

        public static class CantMoveException extends Exception {

        }
    }

    static abstract class MoveDescription {
        public abstract String getMoveDescription(Player player, Player opponent, Move move);

        protected static String genDescription(String player, Card card) {
            return player + " played " + Integer.toString(card.getWeight());
        }

        protected static String genDescription(String player, Card card, String opponent) {
            return player + " played " + Integer.toString(card.getWeight()) + " on " + opponent;
        }

        protected static String genDescription(String player, Card card, String opponent, Card role) {
            return player + " suppose that " + opponent + "`s role is " + Integer.toString(role.getWeight());
        }
    }

    interface DrawableGetter {
        Drawable getDrawble(Context context);
    }
}
