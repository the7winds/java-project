package com.the7winds.verbumSecretum.client.other;

import com.the7winds.verbumSecretum.server.game.Game;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by the7winds on 27.11.15.
 */
public class ClientData {
    public static String id;
    public static Map<String, String> playersNames;
    public static Map<String, LinkedList<Game.Card>> playerPlayedCards;
    public static Collection<Game.Card> hand;
    public static String name;

    public enum ReadyState {READY, NOT_READY}
    public static ReadyState readyState = ReadyState.NOT_READY;
}
