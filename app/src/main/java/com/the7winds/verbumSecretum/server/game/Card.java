package com.the7winds.verbumSecretum.server.game;

import android.content.Context;
import android.graphics.drawable.Drawable;

import static com.the7winds.verbumSecretum.server.game.CardUtils.CantMoveException;

/**
 * Created by the7winds on 16.02.16.
 */
public interface Card extends Comparable<Card> {
    int getWeight();

    void applyMove(Player player, Player opponent, Move move, Game game) throws CantMoveException;

    boolean checkMove(Player player, Player opponent, Move move, Game game);

    String getMoveDescription(Player player, Player opponent, Move move);

    Drawable getDrawable(Context context);

    String name();
}
