package com.the7winds.verbumSecretum.server.game;

import android.util.Pair;

import java.util.Arrays;
import java.util.Collection;
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

    private boolean finished = false;

    private Map<String, Player> activePlayers;

    private Player[] players;

    private int currentIdx;

    private Queue<Card> deck = new LinkedList<>();

    private Map<String, Card> cardsThatShouldBeShowed = new Hashtable<>();

    Random random = new Random();

    public Pair<String, Card> getLastChange() {
        return lastChange;
    }

    private Pair<String, Card> lastChange;

    public Map<String, Player> getActivePlayers() {
        return activePlayers;
    }

    public String getCurrent() {
        return players[currentIdx].getId();
    }

    public boolean isActivePlayer(String id) {
        return activePlayers.containsKey(id);
    }

    public String[] getResults() {
        if (activePlayers.size() == 1) {
            return new String[] {((Player[]) activePlayers.values().toArray())[0].getName()};
        } else {
            Collection<String> winners = new LinkedList<>();
            Card max = Card.GUARD_CARD;
            for (Player player : activePlayers.values()) {
                if (player.getHandCard() == max) {
                    winners.add(player.getName());
                } else if (player.getHandCard().ordinal() > max.ordinal()) {
                    max = player.getHandCard();
                    winners = new LinkedList<>();
                    winners.add(player.getName());
                }
            }
            return (String[]) winners.toArray();
        }
    }

    public Map<String,Card> getCardsThatShouldBeShowed() {
        return cardsThatShouldBeShowed;
    }

    public Card getTopCard() {
        return deck.remove();
    }

    public enum Card {
        GUARD_CARD,
        PRIST_CARD,
        LORD_CARD,
        STAFF_CARD,
        PRINCE_CARD,
        KING_CARD,
        COUNTESS_CARD,
        PRINCESS_CARD
    }

    public static class Move {
        public String subjectId;
        public Card card;
        public String objectId;
        public Card role;
    }

    public Game(Map<String, Player> allPlayers) {
        activePlayers = allPlayers;
        players = (Player[]) allPlayers.values().toArray();
        currentIdx = random.nextInt();

        initDec();
        giveCards();
    }

    public void initDec() {
        final int TIMES = 20;

        List<Integer> nums = Arrays.asList(new Integer[] { 1, 1, 1, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 7, 8 });

        Collections.shuffle(nums, random);

        for (Integer n : nums) {
            switch (n) {
                case 1: deck.add(Card.GUARD_CARD);      break;
                case 2: deck.add(Card.PRIST_CARD);      break;
                case 3: deck.add(Card.LORD_CARD);       break;
                case 4: deck.add(Card.STAFF_CARD);      break;
                case 5: deck.add(Card.PRINCE_CARD);     break;
                case 6: deck.add(Card.KING_CARD);       break;
                case 7: deck.add(Card.COUNTESS_CARD);   break;
                case 8: deck.add(Card.PRINCESS_CARD);   break;
            }
        }
    }

    private void giveCards() {
        for (Player player : activePlayers.values()) {
            player.addCard(deck.remove());
        }
    }

    public boolean isValidMove(Move move) {

        Player object = activePlayers.get(move.objectId);
        Player subject = activePlayers.get(move.subjectId);

        switch (move.card) {
            case GUARD_CARD:
                return (move.role != Card.GUARD_CARD
                        && object.getPlayedCards().element() != Card.STAFF_CARD);
            case PRIST_CARD:
                return (object.getPlayedCards().element() != Card.STAFF_CARD);
            case LORD_CARD:
                return (object.getPlayedCards().element() != Card.STAFF_CARD);
            case STAFF_CARD:
                return true;
            case PRINCE_CARD:
                return (!object.getHandCards().contains(Card.COUNTESS_CARD) &&
                        (object == subject || object.getPlayedCards().element() != Card.STAFF_CARD));
            case KING_CARD:
                return (!object.getHandCards().contains(Card.COUNTESS_CARD) &&
                        object.getPlayedCards().element() != Card.STAFF_CARD);
            case COUNTESS_CARD:
                return true;
            case PRINCESS_CARD:
                return true;
        }

        return false;
    }

    public void applyMove(Move move) {

        cardsThatShouldBeShowed.clear();

        Player object = activePlayers.get(move.objectId);
        Player subject = activePlayers.get(move.subjectId);


        object.getHandCards().remove(move.card);
        object.getPlayedCards().add(move.card);
        lastChange = new Pair<>(move.objectId, move.card);

        switch (move.card) {

            case GUARD_CARD: // try
                if (object.getHandCards().contains(move.role)) {
                    makeInactive(object);
                }

                break;

            case PRIST_CARD: // show
                cardsThatShouldBeShowed.put(move.subjectId, object.getHandCard());

                break;

            case LORD_CARD: // compare cards
                Card subjectsCard = subject.getHandCard();
                Card objectsCard = object.getHandCard();

                if (subjectsCard.compareTo(objectsCard) > 0) {
                    makeInactive(object);
                }

                if (subjectsCard.compareTo(objectsCard) < 0) {
                    makeInactive(subject);
                }

                cardsThatShouldBeShowed.put(move.objectId, subjectsCard);
                cardsThatShouldBeShowed.put(move.subjectId, objectsCard);

                break;

            case STAFF_CARD: // protect
                break;

            case PRINCE_CARD: // change
                subject.getHandCards().clear();
                subject.getHandCards().add(deck.remove());

                break;

            case KING_CARD: // swap
                Collection<Card> tmp = object.getHandCards();
                object.getHandCards().addAll(subject.getHandCards());
                subject.getHandCards().clear();
                subject.getHandCards().addAll(tmp);

                break;

            case COUNTESS_CARD: // nothing
                break;

            case PRINCESS_CARD: // game over
                makeInactive(subject);
                finished = true;

                break;
        }

        if (deck.size() < 2 || activePlayers.size() == 1) {
            finished = true;
        }
    }

    public void giveCard(String id) {
        activePlayers.get(id).addCard(deck.poll());
    }

    public boolean nextPlayer() {
        if (deck.size() < 2 || activePlayers.size() == 1) {
            return false;
        }

        do {
            currentIdx = (currentIdx + 1) % players.length;
        }
        while (!isActivePlayer(players[currentIdx].getId()));

        return true;
    }

    public boolean isFinished() {
        return finished;
    }

    private void makeInactive(Player player) {
        activePlayers.remove(player);
    }
}
