package com.the7winds.verbumSecretum.server.game;

import com.the7winds.verbumSecretum.server.network.ConnectionHandler;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by the7winds on 28.10.15.
 */
public class Player {

    private String name;
    private ConnectionHandler connection;

    private List<Card> handCards = new LinkedList<>();
    private Card lastPlayedCard = null;

    public Player(ConnectionHandler connection, String name) {
        this.connection = connection;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Card getLastPlayedCard() {
        return lastPlayedCard;
    }

    public boolean containsHand(Card card) {
        return handCards.contains(card);
    }

    public void clearHandCards() {
        handCards.clear();
    }

    public Card getHandCard() {
        return (handCards.isEmpty() ? null : handCards.get(0));
    }

    public boolean removeHandCard(Card card) {
        return handCards.remove(card);
    }

    public void addHandCard(Card card) {
        handCards.add(card);
    }

    public String getId() {
        return connection.getId();
    }

    public void setLastPlayedCard(Card card) {
        lastPlayedCard = card;
    }
}
