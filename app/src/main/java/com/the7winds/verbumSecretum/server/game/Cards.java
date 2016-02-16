package com.the7winds.verbumSecretum.server.game;

import java.util.Map;

/**
 * Created by the7winds on 16.02.16.
 */
public class Cards {

    private static abstract class Moves {
        protected static boolean cantMove(String objectId, Game game) {
            for (Map.Entry<String, Player> entry : game.getActivePlayers().entrySet()) {
                if (!entry.getKey().equals(objectId) && (entry.getValue().getPlayedCards().isEmpty()
                        || entry.getValue().getPlayedCards().getLast() != Card.STAFF_CARD)) {
                    return false;
                }
            }
            return true;
        }
    }

    static abstract class MoveChecker extends Moves {
        public abstract boolean checkMove(Player subject, Player object, Move move, Game game);
    }

    static abstract class MoveApplier extends Moves{
        public abstract Map<String, Card> applyMove(Player subject, Player object, Move move, Game game) throws CantMoveException;

        protected static void makeInactive(Player player, Game game) {
            game.getActivePlayers().remove(player.getId());
        }

        public static class CantMoveException extends Exception {

        }
    }

    static abstract class MoveDescription {
        public abstract String getMoveDescription(Player subject, Player object, Move move);

        protected static String genDescription(String object, Card card) {
            return object + " played " + Integer.toString(card.getWeight());
        }

        protected static String genDescription(String object, Card card, String subject) {
            return object + " played " + Integer.toString(card.getWeight()) + " on " + subject;
        }

        protected static String genDescription(String object, Card card, String subject, Card role) {
            return object + " suppose that " + subject + "`s role is " + Integer.toString(role.getWeight());
        }
    }

}
