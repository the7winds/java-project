package com.the7winds.verbumSecretum.server.game;

import android.util.Pair;

import com.the7winds.verbumSecretum.server.network.ServerMessages;

import java.security.Guard;
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

    private Pair<String, Card> lastChange;

    private String description;

    public Map<String, Player> getActivePlayers() {
        return activePlayers;
    }

    public String getCurrentPlayerId() {
        return players[currentIdx].getId();
    }

    public boolean isActivePlayer(String id) {
        return activePlayers.containsKey(id);
    }

    private String[] getResults() {
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

    public ServerMessages.GameOver genGameOverMessage() {
        Map<String, String> winners = new Hashtable<>();
        Card max = Card.GUARD_CARD;

        for (Map.Entry<String, Player> entry : activePlayers.entrySet()) {
            String id = entry.getKey();
            Player player = entry.getValue();

            if (player.getHandCard() == max) {
                winners.put(id, player.getName());
            } else if (player.getHandCard().ordinal() > max.ordinal()) {
                max = player.getHandCard();
                winners = new Hashtable<>();
                winners.put(id, player.getName());
            }
        }

        return new ServerMessages.GameOver(winners, activePlayers);
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
        public String subjectId = "";
        public Card card = Card.GUARD_CARD;
        public String objectId = "";
        public Card role = Card.GUARD_CARD;
    }

    public Game(Map<String, Player> allPlayers) {
        activePlayers = allPlayers;
        players = allPlayers.values().toArray(new Player[allPlayers.size()]);

        Random random = new Random();
        currentIdx = Math.abs(random.nextInt()) % players.length;
        // nextPlayer();

        initDec();
        giveCards();
    }

    private void initDec() {
        List<Integer> nums = Arrays.asList(new Integer[] { 1, 1, 1, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 7, 8 });
        // List<Integer> nums = Arrays.asList(new Integer[] { 1, 7, 8 });
        // List<Integer> nums = Arrays.asList(new Integer[] { 7, 3, 5 });

        Random random = new Random();
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
                return cantMove(move.objectId) || (move.role != Card.GUARD_CARD
                        && (subject.getPlayedCards().isEmpty() || subject.getPlayedCards().element() != Card.STAFF_CARD));
            case PRIST_CARD:
                return cantMove(move.objectId) || (subject.getPlayedCards().isEmpty() || subject.getPlayedCards().element() != Card.STAFF_CARD);
            case LORD_CARD:
                return cantMove(move.objectId) || (subject.getPlayedCards().isEmpty() || subject.getPlayedCards().element() != Card.STAFF_CARD);
            case STAFF_CARD:
                return true;
            case PRINCE_CARD:
                return (!object.getHandCards().contains(Card.COUNTESS_CARD) &&
                        (object == subject || (subject.getPlayedCards().isEmpty() || subject.getPlayedCards().element() != Card.STAFF_CARD)));
            case KING_CARD:
                return cantMove(move.objectId) || (!object.getHandCards().contains(Card.COUNTESS_CARD) &&
                        (subject.getPlayedCards().isEmpty() || subject.getPlayedCards().element() != Card.STAFF_CARD));
            case COUNTESS_CARD:
                return true;
            case PRINCESS_CARD:
                return true;
        }

        return false;
    }

    private boolean cantMove(String objectId) {
        for (Map.Entry<String, Player> entry : activePlayers.entrySet()) {
            if (!entry.getKey().equals(objectId) && !entry.getValue().getPlayedCards().contains(Card.STAFF_CARD)) {
                return false;
            }
        }
        return true;
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
                if (cantMove(move.objectId)) {
                    break;
                }
                description = genDescription(object.getName(), move.card.toString(), subject.getName(), move.role.toString());
                if (subject.getHandCards().contains(move.role)) {
                    makeInactive(subject);
                }
                break;

            case PRIST_CARD: // show
                if (cantMove(move.objectId)) {
                    break;
                }
                description = genDescription(object.getName(), move.card.toString(), subject.getName());
                cardsThatShouldBeShowed.put(move.objectId, subject.getHandCard());
                break;

            case LORD_CARD: // compare cards
                if (cantMove(move.objectId)) {
                    break;
                }

                description = genDescription(object.getName(), move.card.toString(), subject.getName());

                Card subjectsCard = subject.getHandCard();
                Card objectsCard = object.getHandCard();

                if (subjectsCard.ordinal() > objectsCard.ordinal()) {
                    makeInactive(object);
                }

                if (subjectsCard.ordinal() < objectsCard.ordinal()) {
                    makeInactive(subject);
                }

                cardsThatShouldBeShowed.put(move.objectId, subjectsCard);
                cardsThatShouldBeShowed.put(move.subjectId, objectsCard);

                break;

            case STAFF_CARD: // protect
                description = genDescription(object.getName(), move.card.toString());
                break;

            case PRINCE_CARD: // change
                description = genDescription(object.getName(), move.card.toString(), subject.getName());
                subject.getHandCards().clear();
                subject.getHandCards().add(deck.remove());
                break;

            case KING_CARD: // swap
                if (cantMove(move.objectId)) {
                    break;
                }
                description = genDescription(object.getName(), move.card.toString(), subject.getName());
                Card tmp = object.getHandCard();
                object.getHandCards().clear();
                object.getHandCards().add(subject.getHandCard());
                subject.getHandCards().clear();
                subject.getHandCards().add(tmp);
                break;

            case COUNTESS_CARD: // nothing
                description = genDescription(object.getName(), move.card.toString());
                break;

            case PRINCESS_CARD: // game over
                description = genDescription(object.getName(), move.card.toString());
                makeInactive(object);
                finished = true;
                break;
        }

        if (deck.size() < 2 || activePlayers.size() == 1) {
            finished = true;
        }
    }

    public boolean nextPlayer() {
        if (finished) {
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
        activePlayers.remove(player.getId());
    }

    public ServerMessages.GameState genGameStateMessage() {
        return new ServerMessages.GameState(players[currentIdx].getId()
                , description
                , lastChange
                , cardsThatShouldBeShowed
                , activePlayers);
    }

    public ServerMessages.YourTurn genYourTurnMessage() {
        Card card = deck.remove();
        players[currentIdx].addCard(card);
        return new ServerMessages.YourTurn(card);
    }

    private String genDescription(String object, String card) {
        return object + " played " + card;
    }

    private String genDescription(String object, String card, String subject) {
        return object + " played " + card + " on " + subject;
    }

    private String genDescription(String object, String card, String subject, String role) {
        return object + " suppose that " + subject + "`s role is " + role;
    }
}
