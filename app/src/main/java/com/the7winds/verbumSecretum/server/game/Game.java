package com.the7winds.verbumSecretum.server.game;

import com.the7winds.verbumSecretum.server.game.Player;

import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Random;

/**
 * Created by the7winds on 28.10.15.
 */
public class Game {

    private boolean finished = false;

    private Map<String, Player> players;

    private int currentIndx = 0;

    private String currentId;

    private String[] ids;

    private Player currentPlayer;

    private Queue<Card> deck;

    private Map<String, Card> cardsThatShouldBeShowed;

    Random random = new Random();

    public Map<String, Player> getActivePlayers() {
        return players;
    }

    public String getCurrent() {
        return currentId;
    }

    public boolean isActive(String id) {
        return players.containsKey(id);
    }

    public String[] result() {
        return new String[0];
    }

    public Map<String,Card> getCardsThatShouldBeShowed() {
        return cardsThatShouldBeShowed;
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

    public Game(Map<String, Player> players) {
        this.players = players;
        cardsThatShouldBeShowed = new Hashtable<>();
        deck = new LinkedList<>();
        ids = players.keySet().toArray(new String[players.size()]);
        currentId = ids[0];
        currentIndx = 0;

        initDec();
        giveCards();
    }

    public void initDec() {
        final int TIMES = 20;

        int[] nums = new int[] { 1, 1, 1, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 7, 8 };

        for (int i = 0; i < TIMES; i++) {
            int idx1 = random.nextInt() % nums.length;
            int idx2 = random.nextInt() % nums.length;

            int tmp = nums[idx1];
            nums[idx1] = nums[idx2];
            nums[idx2] = tmp;
        }

        for (int i = 0; i < nums.length; i++) {
            switch (i) {
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
        for (String id : players.keySet()) {
            Player player = players.get(id);
            player.addCard(deck.remove());
        }
    }

    public boolean testMove(Move move) {

        Player object = players.get(move.objectId);
        Player subject = players.get(move.subjectId);

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

        Player object = players.get(move.objectId);
        Player subject = players.get(move.subjectId);


        object.getHandCards().remove(move.card);
        object.getPlayedCards().add(move.card);

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
                subject.getHandCards().add(deck.poll()); // TODO: check maybe null

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

        if (deck.isEmpty()) {
            finished = true;
        } else {
/*
            if (!currentPlayerI.hasNext()) {
                if (players.isEmpty()) {
                    finished = true;
                } else {
                    currentPlayerI = players.iterator();
                    currentPlayer = currentPlayerI.next();
                }
            } else {
                currentPlayer = currentPlayerI.next();
            }*/
            throw new UnsupportedOperationException();
        }

    }

    public void next() {

    }

    public boolean isFinished() {
        return finished;
    }

    public void makeInactive(Player player) {
        players.remove(player);
    }
}
