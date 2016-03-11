package com.the7winds.verbumSecretum.server.game;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.the7winds.verbumSecretum.R;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import static com.the7winds.verbumSecretum.server.game.CardUtils.CantMoveException;
import static com.the7winds.verbumSecretum.server.game.CardUtils.cantMove;
import static com.the7winds.verbumSecretum.server.game.CardUtils.genDescription;

/**
 * Created by the7winds on 11.03.16.
 */
public class Cards {
    private static final Map<String, Card> NAME_TO_CARD = new Hashtable();

    public static final Card GUARD_CARD = new Card() {
        private static final int WEIGHT = 1;
        private static final String NAME = "GUARD";

        {
            Cards.NAME_TO_CARD.put(NAME, this);
        }

        @Override
        public int getWeight() {
            return WEIGHT;
        }

        @Override
        public void applyMove(Player player, Player opponent, Move move, Game game) throws CantMoveException {
            if (cantMove(player.getId(), game)) {
                throw new CantMoveException();
            }

            if (opponent.containsHand(move.role)) {
                game.removeActivePlayer(opponent.getId());
            }

            game.setCardsThatShouldBeShowed(new HashMap<String, Card>());
        }

        @Override
        public boolean checkMove(Player player, Player opponent, Move move, Game game) {
            return cantMove(player.getId(), game) || (move.role != GUARD_CARD &&
                    (opponent.getLastPlayedCard() == null || opponent.getLastPlayedCard() != STAFF_CARD));
        }

        @Override
        public String getMoveDescription(Player player, Player opponent, Move move) {
            return genDescription(player.getName(), move.card, opponent.getName(), move.role);
        }

        @Override
        public Drawable getDrawable(Context context) {
            return context.getResources().getDrawable(R.drawable.c1);
        }

        @Override
        public String name() {
            return NAME;
        }

        @Override
        public int compareTo(Card another) {
            return WEIGHT - another.getWeight();
        }
    };

    public static final Card PRIEST_CARD = new Card() {
        private static final int WEIGHT = 2;
        private static final String NAME = "PRIEST";

        {
            Cards.NAME_TO_CARD.put(NAME, this);
        }

        @Override
        public int getWeight() {
            return WEIGHT;
        }

        @Override
        public void applyMove(Player player, Player opponent, Move move, Game game) throws CantMoveException {
            if (cantMove(player.getId(), game)) {
                throw new CantMoveException();
            }

            Map<String, Card> cardsThatShouldBeShowed = new HashMap<>();
            cardsThatShouldBeShowed.put(player.getId(), opponent.getHandCard());

            game.setCardsThatShouldBeShowed(cardsThatShouldBeShowed);
        }

        @Override
        public boolean checkMove(Player player, Player opponent, Move move, Game game) {
            return cantMove(player.getId(), game) ||
                    (opponent.getLastPlayedCard() == null || opponent.getLastPlayedCard() != STAFF_CARD);
        }

        @Override
        public String getMoveDescription(Player player, Player opponent, Move move) {
            return genDescription(player.getName(), move.card, opponent.getName());
        }

        @Override
        public Drawable getDrawable(Context context) {
            return context.getResources().getDrawable(R.drawable.c2);
        }

        @Override
        public String name() {
            return NAME;
        }

        @Override
        public int compareTo(Card another) {
            return WEIGHT - another.getWeight();
        }
    };

    public static final Card LORD_CARD = new Card() {
        private static final int WEIGHT = 3;
        private static final String NAME = "LORD";

        {
            Cards.NAME_TO_CARD.put(NAME, this);
        }

        @Override
        public int getWeight() {
            return WEIGHT;
        }

        @Override
        public void applyMove(Player player, Player opponent, Move move, Game game) throws CantMoveException {
            if (cantMove(player.getId(), game)) {
                throw new CantMoveException();
            }

            Card playerCard = player.getHandCard();
            Card opponentCard = opponent.getHandCard();

            if (playerCard.getWeight() > opponentCard.getWeight()) {
                game.removeActivePlayer(opponent.getId());
            }

            if (playerCard.getWeight() < opponentCard.getWeight()) {
                game.removeActivePlayer(player.getId());
            }

            Map<String, Card> cardsThatShouldBeShowed = new HashMap<>();
            cardsThatShouldBeShowed.put(opponent.getId(), playerCard);
            cardsThatShouldBeShowed.put(player.getId(), opponentCard);

            game.setCardsThatShouldBeShowed(cardsThatShouldBeShowed);
        }

        @Override
        public boolean checkMove(Player player, Player opponent, Move move, Game game) {
            return cantMove(player.getId(), game) ||
                    (opponent.getLastPlayedCard() == null || opponent.getLastPlayedCard() != STAFF_CARD);
        }

        @Override
        public String getMoveDescription(Player player, Player opponent, Move move) {
            return genDescription(player.getName(), move.card, opponent.getName());
        }

        @Override
        public Drawable getDrawable(Context context) {
            return context.getResources().getDrawable(R.drawable.c3);
        }

        @Override
        public String name() {
            return NAME;
        }

        @Override
        public int compareTo(Card another) {
            return WEIGHT - another.getWeight();
        }
    };

    public static final Card STAFF_CARD = new Card() {
        private static final int WEIGHT = 4;
        private static final String NAME = "STAFF";

        {
            Cards.NAME_TO_CARD.put(NAME, this);
        }

        @Override
        public int getWeight() {
            return WEIGHT;
        }

        @Override
        public void applyMove(Player player, Player opponent, Move move, Game game) throws CantMoveException {
            game.setCardsThatShouldBeShowed(new HashMap<String, Card>());
        }

        @Override
        public boolean checkMove(Player player, Player opponent, Move move, Game game) {
            return true;
        }

        @Override
        public String getMoveDescription(Player player, Player opponent, Move move) {
            return genDescription(player.getName(), move.card);
        }

        @Override
        public Drawable getDrawable(Context context) {
            return context.getResources().getDrawable(R.drawable.c4);
        }

        @Override
        public String name() {
            return NAME;
        }

        @Override
        public int compareTo(Card another) {
            return WEIGHT - another.getWeight();
        }
    };

    public static final Card PRINCE_CARD = new Card() {
        private static final int WEIGHT = 5;
        private static final String NAME = "PRINCE";

        {
            Cards.NAME_TO_CARD.put(NAME, this);
        }

        @Override
        public int getWeight() {
            return WEIGHT;
        }

        @Override
        public void applyMove(Player player, Player opponent, Move move, Game game) throws CantMoveException {
            opponent.clearHandCards();
            opponent.addHandCard(Game.CardsDeck.getInstance().getTopCard());
            game.setCardsThatShouldBeShowed(new HashMap<String, Card>());
        }

        @Override
        public boolean checkMove(Player player, Player opponent, Move move, Game game) {
            return !player.containsHand(COUNTESS_CARD) &&
                    (opponent.getLastPlayedCard() == null || opponent.getLastPlayedCard() != STAFF_CARD);
        }

        @Override
        public String getMoveDescription(Player player, Player opponent, Move move) {
            return genDescription(player.getName(), move.card, opponent.getName());
        }

        @Override
        public Drawable getDrawable(Context context) {
            return context.getResources().getDrawable(R.drawable.c5);
        }

        @Override
        public String name() {
            return NAME;
        }

        @Override
        public int compareTo(Card another) {
            return WEIGHT - another.getWeight();
        }
    };

    public static final Card KING_CARD = new Card() {
        public static final int WEIGHT = 6;
        public static final String NAME = "KING";

        {
            Cards.NAME_TO_CARD.put(NAME, this);
        }

        @Override
        public int getWeight() {
            return WEIGHT;
        }

        @Override
        public void applyMove(Player player, Player opponent, Move move, Game game) throws CantMoveException {
            if (cantMove(player.getId(), game)) {
                throw new CantMoveException();
            }

            Card tmp = opponent.getHandCard();
            opponent.clearHandCards();
            opponent.addHandCard(player.getHandCard());
            player.clearHandCards();
            player.addHandCard(tmp);

            game.setCardsThatShouldBeShowed(new HashMap<String, Card>());
        }

        @Override
        public boolean checkMove(Player player, Player opponent, Move move, Game game) {
            return cantMove(player.getId(), game) || (!opponent.containsHand(COUNTESS_CARD) &&
                    (opponent.getLastPlayedCard() == null || opponent.getLastPlayedCard() != STAFF_CARD));
        }

        @Override
        public String getMoveDescription(Player player, Player opponent, Move move) {
            return genDescription(player.getName(), move.card, opponent.getName());
        }

        @Override
        public Drawable getDrawable(Context context) {
            return context.getResources().getDrawable(R.drawable.c6);
        }

        @Override
        public String name() {
            return NAME;
        }

        @Override
        public int compareTo(Card another) {
            return WEIGHT - another.getWeight();
        }
    };

    public static final Card COUNTESS_CARD = new Card() {
        private static final int WEIGHT = 7;
        private static final String NAME = "COUNTESS";

        {
            Cards.NAME_TO_CARD.put(NAME, this);
        }

        @Override
        public int getWeight() {
            return WEIGHT;
        }

        @Override
        public void applyMove(Player player, Player opponent, Move move, Game game) throws CantMoveException {
            game.setCardsThatShouldBeShowed(new HashMap<String, Card>());
        }

        @Override
        public boolean checkMove(Player player, Player opponent, Move move, Game game) {
            return true;
        }

        @Override
        public String getMoveDescription(Player player, Player opponent, Move move) {
            return genDescription(player.getName(), move.card);
        }

        @Override
        public Drawable getDrawable(Context context) {
            return context.getResources().getDrawable(R.drawable.c7);
        }

        @Override
        public String name() {
            return NAME;
        }

        @Override
        public int compareTo(Card another) {
            return WEIGHT - another.getWeight();
        }
    };

    public static final Card PRINCESS_CARD = new Card() {
        public static final int WEIGHT = 8;
        public static final String NAME = "PRINCESS";

        {
            Cards.NAME_TO_CARD.put(NAME, this);
        }

        @Override
        public int getWeight() {
            return WEIGHT;
        }

        @Override
        public void applyMove(Player player, Player opponent, Move move, Game game) throws CantMoveException {
            game.removeActivePlayer(player.getId());
            game.finishGame();
            game.setCardsThatShouldBeShowed(new HashMap<String, Card>());
        }

        @Override
        public boolean checkMove(Player player, Player opponent, Move move, Game game) {
            return true;
        }

        @Override
        public String getMoveDescription(Player player, Player opponent, Move move) {
            return genDescription(player.getName(), move.card);
        }

        @Override
        public Drawable getDrawable(Context context) {
            return context.getResources().getDrawable(R.drawable.c8);
        }

        @Override
        public String name() {
            return NAME;
        }

        @Override
        public int compareTo(Card another) {
            return WEIGHT - another.getWeight();
        }
    };

    private static Card[] values;

    public static Card valueOf(String name) {
        return NAME_TO_CARD.get(name);
    }

    public static Card[] values() {
        if (values == null) {
            values = NAME_TO_CARD.values().toArray(new Card[NAME_TO_CARD.size()]);
            Arrays.sort(values);
        }

        return values;
    }
}