package com.the7winds.verbumSecretum.server.game;

import com.the7winds.verbumSecretum.server.network.ConnectionHandler;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Created by the7winds on 28.10.15.
 */
public class Player {

    private String name;
    private ConnectionHandler connection;

    private Collection<Card> handCards = new LinkedList<>();
    private LinkedList<Card> playedCards = new LinkedList<>();

    public Player(ConnectionHandler connection, String name) {
        this.connection = connection;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public LinkedList<Card> getPlayedCards() {
        return playedCards;
    }

    public Collection<Card> getHandCards() {
        return handCards;
    }

    public Card getHandCard() {
        return (handCards.isEmpty() ? null : (Card) handCards.toArray()[0]);
    }

    public void addCard(Card card) {
        handCards.add(card);
    }

    public String getId() {
        return connection.getId();
    }
}
