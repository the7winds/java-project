package com.the7winds.verbumSecretum.server.game;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.the7winds.verbumSecretum.R;
import com.the7winds.verbumSecretum.server.game.Cards.DrawableGetter;
import com.the7winds.verbumSecretum.server.game.Cards.MoveApplier;
import com.the7winds.verbumSecretum.server.game.Cards.MoveChecker;
import com.the7winds.verbumSecretum.server.game.Cards.MoveDescription;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * Created by the7winds on 16.02.16.
 */
public enum Card {

    GUARD_CARD(1, new MoveChecker() {
        @Override
        public boolean checkMove(Player player, Player opponent, Move move, Game game) {
            return cantMove(player.getId(), game) || (move.role != GUARD_CARD &&
                    (opponent.getLastPlayedCard() == null || opponent.getLastPlayedCard() != STAFF_CARD));
        }
    }, new MoveApplier() {
        @Override
        public Map<String, Card> applyMove(Player player, Player opponent, Move move,
                                           Game game) throws CantMoveException {
            if (cantMove(player.getId(), game)) {
                throw new CantMoveException();
            }

            if (opponent.containsHand(move.role)) {
                makeInactive(opponent, game);
            }

            return new HashMap<>();
        }
    }, new MoveDescription() {
        @Override
        public String getMoveDescription(Player player, Player opponent, Move move) {
            return genDescription(player.getName(), move.card, opponent.getName(), move.role);
        }
    }, new DrawableGetter() {
        @Override
        public Drawable getDrawble(Context context) {
            return context.getResources().getDrawable(R.drawable.c1);
        }
    }),

    PRIEST_CARD(2, new MoveChecker() {
        @Override
        public boolean checkMove(Player player, Player opponent, Move move, Game game) {
            return cantMove(player.getId(), game) ||
                    (opponent.getLastPlayedCard() == null || opponent.getLastPlayedCard() != STAFF_CARD);
        }
    }, new MoveApplier() {
        @Override
        public Map<String, Card> applyMove(Player player, Player opponent, Move move, Game game) throws CantMoveException {
            if (cantMove(player.getId(), game)) {
                throw new CantMoveException();
            }

            Map<String, Card> cardsThatShouldBeShowed = new HashMap<>();
            cardsThatShouldBeShowed.put(player.getId(), opponent.getHandCard());
            return cardsThatShouldBeShowed;
        }
    }, new MoveDescription() {
        @Override
        public String getMoveDescription(Player player, Player opponent, Move move) {
            return genDescription(player.getName(), move.card, opponent.getName());
        }
    }, new DrawableGetter() {
        @Override
        public Drawable getDrawble(Context context) {
            return context.getResources().getDrawable(R.drawable.c2);
        }
    }),

    LORD_CARD(3, new MoveChecker() {
        @Override
        public boolean checkMove(Player player, Player opponent, Move move, Game game) {
            return cantMove(player.getId(), game) ||
                    (opponent.getLastPlayedCard() == null || opponent.getLastPlayedCard() != STAFF_CARD);
        }
    }, new MoveApplier() {
        @Override
        public Map<String, Card> applyMove(Player player, Player opponent, Move move, Game game) throws CantMoveException {
            if (cantMove(player.getId(), game)) {
                throw new CantMoveException();
            }

            Card playerCard = player.getHandCard();
            Card opponentCard = opponent.getHandCard();

            if (playerCard.getWeight() > opponentCard.getWeight()) {
                makeInactive(opponent, game);
            }

            if (playerCard.getWeight() < opponentCard.getWeight()) {
                makeInactive(player, game);
            }

            Map<String, Card> cardsThatShouldBeShowed = new HashMap<>();
            cardsThatShouldBeShowed.put(opponent.getId(), playerCard);
            cardsThatShouldBeShowed.put(player.getId(), opponentCard);

            return cardsThatShouldBeShowed;

        }
    }, new MoveDescription() {
        @Override
        public String getMoveDescription(Player player, Player opponent, Move move) {
            return genDescription(player.getName(), move.card, opponent.getName());
        }
    }, new DrawableGetter() {
        @Override
        public Drawable getDrawble(Context context) {
            return context.getResources().getDrawable(R.drawable.c3);
        }
    }),

    STAFF_CARD(4, new MoveChecker() {
        @Override
        public boolean checkMove(Player player, Player opponent, Move move, Game game) {
            return true;
        }
    }, new MoveApplier() {
        @Override
        public Map<String, Card> applyMove(Player player, Player opponent, Move move, Game game) throws CantMoveException {
            return new HashMap<>();
        }
    }, new MoveDescription() {
        @Override
        public String getMoveDescription(Player player, Player opponent, Move move) {
            return genDescription(player.getName(), move.card);
        }
    }, new DrawableGetter() {
        @Override
        public Drawable getDrawble(Context context) {
            return context.getResources().getDrawable(R.drawable.c4);
        }
    }),

    PRINCE_CARD(5, new MoveChecker() {
        @Override
        public boolean checkMove(Player player, Player opponent, Move move, Game game) {
            return !player.containsHand(COUNTESS_CARD) &&
                    (opponent.getLastPlayedCard() == null || opponent.getLastPlayedCard() != STAFF_CARD);
        }
    }, new MoveApplier() {
        @Override
        public Map<String, Card> applyMove(Player player, Player opponent, Move move, Game game) throws CantMoveException {
            opponent.clearHandCards();
            opponent.addHandCard(Game.CardsDeck.getInstance().getTopCard());
            return new Hashtable<>();
        }
    }, new MoveDescription() {
        @Override
        public String getMoveDescription(Player player, Player opponent, Move move) {
            return genDescription(player.getName(), move.card, opponent.getName());
        }
    }, new DrawableGetter() {
        @Override
        public Drawable getDrawble(Context context) {
            return context.getResources().getDrawable(R.drawable.c5);
        }
    }),

    KING_CARD(6, new MoveChecker() {
        @Override
        public boolean checkMove(Player player, Player opponent, Move move, Game game) {
            return cantMove(player.getId(), game) || (!opponent.containsHand(COUNTESS_CARD) &&
                    (opponent.getLastPlayedCard() == null || opponent.getLastPlayedCard() != STAFF_CARD));
        }
    }, new MoveApplier() {
        @Override
        public Map<String, Card> applyMove(Player player, Player opponent, Move move, Game game) throws CantMoveException {
            if (cantMove(player.getId(), game)) {
                throw new CantMoveException();
            }

            Card tmp = opponent.getHandCard();
            opponent.clearHandCards();
            opponent.addHandCard(player.getHandCard());
            player.clearHandCards();
            player.addHandCard(tmp);

            return new HashMap<>();
        }
    }, new MoveDescription() {
        @Override
        public String getMoveDescription(Player player, Player opponent, Move move) {
            return genDescription(player.getName(), move.card, opponent.getName());
        }
    }, new DrawableGetter() {
        @Override
        public Drawable getDrawble(Context context) {
            return context.getResources().getDrawable(R.drawable.c6);
        }
    }),

    COUNTESS_CARD(7, new MoveChecker() {
        @Override
        public boolean checkMove(Player player, Player opponent, Move move, Game game) {
            return true;
        }
    }, new MoveApplier() {
        @Override
        public Map<String, Card> applyMove(Player player, Player opponent, Move move, Game game) throws CantMoveException {
            return new HashMap<>();
        }
    }, new MoveDescription() {
        @Override
        public String getMoveDescription(Player player, Player opponent, Move move) {
            return genDescription(player.getName(), move.card);
        }
    }, new DrawableGetter() {
        @Override
        public Drawable getDrawble(Context context) {
            return context.getResources().getDrawable(R.drawable.c7);
        }
    }),

    PRINCESS_CARD(8, new MoveChecker() {
        @Override
        public boolean checkMove(Player player, Player opponent, Move move, Game game) {
            return true;
        }
    }, new MoveApplier() {
        @Override
        public Map<String, Card> applyMove(Player player, Player opponent, Move move, Game game) throws CantMoveException {
            makeInactive(player, game);
            game.finishGame();
            return new HashMap<>();
        }
    }, new MoveDescription() {
        @Override
        public String getMoveDescription(Player player, Player opponent, Move move) {
            return genDescription(player.getName(), move.card);
        }
    }, new DrawableGetter() {
        @Override
        public Drawable getDrawble(Context context) {
            return context.getResources().getDrawable(R.drawable.c8);
        }
    });

    Card(int weight,
         MoveChecker moveChecker,
         MoveApplier moveApplier,
         MoveDescription moveDescription,
         DrawableGetter drawableGetter) {
        this.weight = weight;
        this.moveChecker = moveChecker;
        this.moveApplier = moveApplier;
        this.moveDescription = moveDescription;
        this.drawableGetter = drawableGetter;
    }

    private final MoveChecker moveChecker;
    private final MoveApplier moveApplier;
    private final MoveDescription moveDescription;
    private final DrawableGetter drawableGetter;
    private final int weight;

    public int getWeight() {
        return weight;
    }

    public Map<String, Card> applyMove(Player player, Player opponent, Move move, Game game) throws MoveApplier.CantMoveException {
        return moveApplier.applyMove(player, opponent, move, game);
    }

    public boolean checkMove(Player player, Player opponent, Move move, Game game) {
        return moveChecker.checkMove(player, opponent, move, game);
    }

    public String getMoveDescription(Player player, Player opponent, Move move) {
        return moveDescription.getMoveDescription(player, opponent, move);
    }

    public Drawable getDrawable(Context context) {
        return drawableGetter.getDrawble(context);
    }
}
