package com.the7winds.verbumSecretum.server.game;

import android.util.Log;
import android.util.Pair;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
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

    private boolean finished;

    private Map<String, Player> activePlayers;
    private Player[] allPlayers;
    private int currentIdx;

    static class CardsDeck {
        public static CardsDeck getInstance() {
            return INSTANCE;
        }

        private static final CardsDeck INSTANCE = new CardsDeck();

        private Queue<Card> deck;

        private CardsDeck() {
            reset();
        }

        public void reset() {
            deck = new LinkedList<>();

            final List<Integer> nums = Arrays.asList(new Integer[] { 1, 1, 1, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 7, 8 });
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
            return deck.remove();
        }

        public int size() {
            return deck.size();
        }
    }

    private Map<String, Card> cardsThatShouldBeShowed;
    private Pair<String, Card> lastPlayed;
    private String description;

    private Game() {}

    public static Game getInstance() {
        return INSTANCE;
    }

    String getDescription() {
        return description;
    }

    Pair<String, Card> getLastPlayed() {
        return lastPlayed;
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

    public Player removeActivePlayer(String id) {
        return activePlayers.remove(id);
    }

    public Map<String,Player> getActivePlayers() {
        return new HashMap<>(activePlayers);
    }

    void finishGame() {
        finished = true;
    }

    public void reset(Map<String, Player> allPlayers) {
        finished = false;

        activePlayers = allPlayers;
        this.allPlayers = allPlayers.values().toArray(new Player[allPlayers.size()]);

        currentIdx = Math.abs(new Random().nextInt()) % this.allPlayers.length;
        CardsDeck.getInstance().reset();

        cardsThatShouldBeShowed = new Hashtable<>();

        giveCards();
    }

    private void giveCards() {
        for (Player player : activePlayers.values()) {
            player.addHandCard(CardsDeck.getInstance().getTopCard());
        }
    }

    public boolean checkMove(Move move) {
        Player player = activePlayers.get(move.playerId);
        Player opponent = activePlayers.get(move.opponentId);

        return move.card.checkMove(player, opponent, move, this);
    }

    public void applyMove(Move move) {
        Card card = move.card;
        Player player = activePlayers.get(move.playerId);
        Player opponent = activePlayers.get(move.opponentId);

        player.removeHandCard(card);
        player.setLastPlayedCard(card);
        lastPlayed = new Pair<>(player.getId(), card);

        try {
            cardsThatShouldBeShowed = card.applyMove(player, opponent, move, this);
            description = card.getMoveDescription(player, opponent, move);
        } catch (Cards.MoveApplier.CantMoveException e) {
            Log.e(TAG, e.toString());
        }

        if (CardsDeck.getInstance().size() < 2 || activePlayers.size() == 1) {
            finished = true;
        }
    }

    public boolean nextPlayer() {
        if (finished) {
            return false;
        }

        do {
            currentIdx = (currentIdx + 1) % allPlayers.length;
        } while (!isActivePlayer(allPlayers[currentIdx].getId()));

        return true;
    }

    public boolean isFinished() {
        return finished;
    }

}
