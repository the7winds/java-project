package com.the7winds.verbumSecretum.server.game;

import android.util.Log;
import android.util.Pair;

import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;

/**
 * Created by the7winds on 28.10.15.
 */
public class Game {

    private static final String TAG = "Game";
    private static final Game INSTANCE = new Game();

    private boolean finished = false;

    private Map<String, Player> activePlayers;
    private Player[] allPlayers;
    private int currentIdx;

    static class CardsDeck {
        public static CardsDeck getInstance() {
            return INSTANCE;
        }

        private static final CardsDeck INSTANCE = new CardsDeck();

        private Queue<Card> deck = new LinkedList<>();
        private Card lastTakenCard;

        private CardsDeck() {
            List<Integer> nums = Arrays.asList(new Integer[] { 1, 1, 1, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 7, 8 });

            Collections.shuffle(nums, new Random());

            for (Integer n : nums) {
                switch (n) {
                    case 1: deck.add(Card.GUARD_CARD);      break;
                    case 2: deck.add(Card.PRIEST_CARD);     break;
                    case 3: deck.add(Card.LORD_CARD);       break;
                    case 4: deck.add(Card.STAFF_CARD);      break;
                    case 5: deck.add(Card.PRINCE_CARD);     break;
                    case 6: deck.add(Card.KING_CARD);       break;
                    case 7: deck.add(Card.COUNTESS_CARD);   break;
                    case 8: deck.add(Card.PRINCESS_CARD);   break;
                }
            }
        }

        public Card getTopCard() {
            lastTakenCard = deck.remove();
            return lastTakenCard;
        }

        public int size() {
            return deck.size();
        }

        public Card getLastTakenCard() {
            return lastTakenCard;
        }
    }

    private Map<String, Card> cardsThatShouldBeShowed = new Hashtable<>();
    private Pair<String, Card> lastChange;
    private String description;

    private Game() {}

    public static Game getInstance() {
        return INSTANCE;
    }

    String getDescription() {
        return description;
    }

    Pair<String, Card> getLastChange() {
        return lastChange;
    }

    Map<String, Card> getCardsThatShouldBeShowed() {
        return cardsThatShouldBeShowed;
    }

    public String getCurrentPlayerId() {
        return allPlayers[currentIdx].getId();
    }

    public boolean isActivePlayer(String id) {
        return activePlayers.containsKey(id);
    }

    public Map<String,Player> getActivePlayers() {
        return activePlayers;
    }

    void finishGame() {
        finished = true;
    }

    public void reset(Map<String, Player> allPlayers) {
        activePlayers = allPlayers;
        this.allPlayers = allPlayers.values().toArray(new Player[allPlayers.size()]);

        currentIdx = Math.abs(new Random().nextInt()) % this.allPlayers.length;

        giveCards();
    }

    private void giveCards() {
        for (Player player : activePlayers.values()) {
            player.addCard(CardsDeck.getInstance().getTopCard());
        }
    }

    public boolean checkMove(Move move) {
        Player object = activePlayers.get(move.objectId);
        Player subject = activePlayers.get(move.subjectId);

        return move.card.getMoveChecker().checkMove(subject, object, move, this);
    }

    public void applyMove(Move move) {
        Player object = activePlayers.get(move.objectId);
        Player subject = activePlayers.get(move.subjectId);

        object.getHandCards().remove(move.card);
        object.getPlayedCards().add(move.card);
        lastChange = new Pair<>(move.objectId, move.card);

        try {
            cardsThatShouldBeShowed = move.card.getMoveApplier().applyMove(subject, object, move, this);
            description = move.card.getMoveDescription().getMoveDescription(subject, object, move);
        } catch (Cards.MoveApplier.CantMoveException e) {
            Log.e(TAG, e.getMessage());
        }

        if (CardsDeck.getInstance().size() < 2 || activePlayers.size() == 1) {
            finished = true;
        }
    }

    public boolean nextPlayer() {
        if (finished) {
            return false;
        }

        allPlayers[currentIdx].addCard(CardsDeck.getInstance().getTopCard());

        do {
            currentIdx = (currentIdx + 1) % allPlayers.length;
        } while (!isActivePlayer(allPlayers[currentIdx].getId()));

        return true;
    }

    public boolean isFinished() {
        return finished;
    }

}
