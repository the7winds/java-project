package com.the7winds.verbumSecretum.server.game;

import com.the7winds.verbumSecretum.server.network.ConnectionHandler;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by the7winds on 28.10.15.
 */
public class Player {

    private String name;
    private ConnectionHandler connection;

    private Collection<Game.Card> handCards = new LinkedList<>();
    private LinkedList<Game.Card> playedCards = new LinkedList<>();

    public Player(ConnectionHandler connection, String name) {
        this.connection = connection;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public LinkedList<Game.Card> getPlayedCards() {
        return playedCards;
    }

    public Collection<Game.Card> getHandCards() {
        return handCards;
    }

    public Game.Card getHandCard() {
        return (handCards.isEmpty() ? null : (Game.Card) handCards.toArray()[0]);
    }

    public void addCard(Game.Card card) {
        handCards.add(card);
    }

    public String getId() {
        return connection.getId();
    }
}
