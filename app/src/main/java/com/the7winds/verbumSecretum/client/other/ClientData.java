package com.the7winds.verbumSecretum.client.other;

import com.the7winds.verbumSecretum.server.game.Game;

import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by the7winds on 27.11.15.
 */
public class ClientData {
    public static String id;
    public static Map<String, String> playersNames;
    public static Collection<Game.Card> hand = new LinkedList<>();
    public static String name;
    public static Hashtable<String, String> activePlayersNames;

    public enum ReadyState {READY, NOT_READY}
    public static ReadyState readyState = ReadyState.NOT_READY;

    public static AtomicBoolean gameActivityInited = new AtomicBoolean(false);
    public static AtomicBoolean gameActivityStarted = new AtomicBoolean(false);
}
