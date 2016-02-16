package com.the7winds.verbumSecretum.server.game;

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
        public boolean checkMove(Player subject, Player object, Move move, Game game) {
            return cantMove(move.objectId, game) || (move.role != GUARD_CARD &&
                    (subject.getPlayedCards().isEmpty() ||
                            subject.getPlayedCards().getLast() != STAFF_CARD));
        }
    }, new MoveApplier() {

        @Override
        public Map<String, Card> applyMove(Player subject, Player object, Move move,
                                           Game game) throws CantMoveException {
            if (cantMove(move.objectId, game)) {
                throw new CantMoveException();
            }

            if (subject.getHandCards().contains(move.role)) {
                makeInactive(subject, game);
            }

            return new HashMap<>();
        }
    }, new MoveDescription() {
        @Override
        public String getMoveDescription(Player subject, Player object, Move move) {
            return genDescription(object.getName(), move.card, subject.getName(), move.role);
        }
    }),

    PRIEST_CARD(2, new MoveChecker() {
        @Override
        public boolean checkMove(Player subject, Player object, Move move, Game game) {
            return cantMove(move.objectId, game) ||
                    (subject.getPlayedCards().isEmpty() ||
                            subject.getPlayedCards().getLast() != STAFF_CARD);
        }
    }, new MoveApplier() {
        @Override
        public Map<String, Card> applyMove(Player subject, Player object, Move move, Game game) throws CantMoveException {
            if (cantMove(move.objectId, game)) {
                throw new CantMoveException();
            }

            Map<String, Card> cardsThatShouldBeShowed = new HashMap<>();
            cardsThatShouldBeShowed.put(move.objectId, subject.getHandCard());
            return cardsThatShouldBeShowed;
        }
    }, new MoveDescription() {
        @Override
        public String getMoveDescription(Player subject, Player object, Move move) {
            return genDescription(object.getName(), move.card, subject.getName());
        }
    }),

    LORD_CARD(3, new MoveChecker() {
        @Override
        public boolean checkMove(Player subject, Player object, Move move, Game game) {
            return cantMove(move.objectId, game) ||
                    (subject.getPlayedCards().isEmpty() ||
                            subject.getPlayedCards().getLast() != STAFF_CARD);
        }
    }, new MoveApplier() {
        @Override
        public Map<String, Card> applyMove(Player subject, Player object, Move move, Game game) throws CantMoveException {
            if (cantMove(move.objectId, game)) {
                throw new CantMoveException();
            }

            Card subjectsCard = subject.getHandCard();
            Card objectsCard = object.getHandCard();

            if (subjectsCard.getWeight() > objectsCard.getWeight()) {
                makeInactive(object, game);
            }

            if (subjectsCard.getWeight() < objectsCard.getWeight()) {
                makeInactive(subject, game);
            }

            Map<String, Card> cardsThatShouldBeShowed = new HashMap<>();
            cardsThatShouldBeShowed.put(move.objectId, subjectsCard);
            cardsThatShouldBeShowed.put(move.subjectId, objectsCard);

            return cardsThatShouldBeShowed;

        }
    }, new MoveDescription() {
        @Override
        public String getMoveDescription(Player subject, Player object, Move move) {
            return genDescription(object.getName(), move.card, subject.getName());
        }
    }),

    STAFF_CARD(4, new MoveChecker() {
        @Override
        public boolean checkMove(Player subject, Player object, Move move, Game game) {
            return true;
        }
    }, new MoveApplier() {
        @Override
        public Map<String, Card> applyMove(Player subject, Player object, Move move, Game game) throws CantMoveException {
            return new HashMap<>();
        }
    }, new MoveDescription() {
        @Override
        public String getMoveDescription(Player subject, Player object, Move move) {
            return genDescription(object.getName(), move.card);
        }
    }),

    PRINCE_CARD(5, new MoveChecker() {
        @Override
        public boolean checkMove(Player subject, Player object, Move move, Game game) {
            return !object.getHandCards().contains(COUNTESS_CARD) &&
                    (object == subject || (subject.getPlayedCards().isEmpty() ||
                            subject.getPlayedCards().getLast() != STAFF_CARD));
        }
    }, new MoveApplier() {
        @Override
        public Map<String, Card> applyMove(Player subject, Player object, Move move, Game game) throws CantMoveException {
            subject.getHandCards().clear();
            subject.getHandCards().add(Game.CardsDeck.getInstance().getTopCard());
            return new Hashtable<>();
        }
    }, new MoveDescription() {
        @Override
        public String getMoveDescription(Player subject, Player object, Move move) {
            return genDescription(object.getName(), move.card, subject.getName());
        }
    }),

    KING_CARD(6, new MoveChecker() {
        @Override
        public boolean checkMove(Player subject, Player object, Move move, Game game) {
            return cantMove(move.objectId, game) || (!object.getHandCards().contains(COUNTESS_CARD) &&
                    (subject.getPlayedCards().isEmpty() || subject.getPlayedCards().getLast() != STAFF_CARD));
        }
    }, new MoveApplier() {
        @Override
        public Map<String, Card> applyMove(Player subject, Player object, Move move, Game game) throws CantMoveException {
            if (cantMove(move.objectId, game)) {
                throw new CantMoveException();
            }

            Card tmp = object.getHandCard();
            object.getHandCards().clear();
            object.getHandCards().add(subject.getHandCard());
            subject.getHandCards().clear();
            subject.getHandCards().add(tmp);

            return new HashMap<>();
        }
    }, new MoveDescription() {
        @Override
        public String getMoveDescription(Player subject, Player object, Move move) {
            return genDescription(object.getName(), move.card, subject.getName());
        }
    }),

    COUNTESS_CARD(7, new MoveChecker() {
        @Override
        public boolean checkMove(Player subject, Player object, Move move, Game game) {
            return true;
        }
    }, new MoveApplier() {
        @Override
        public Map<String, Card> applyMove(Player subject, Player object, Move move, Game game) throws CantMoveException {
            return new HashMap<>();
        }
    }, new MoveDescription() {
        @Override
        public String getMoveDescription(Player subject, Player object, Move move) {
            return genDescription(object.getName(), move.card);
        }
    }),

    PRINCESS_CARD(8, new MoveChecker() {
        @Override
        public boolean checkMove(Player subject, Player object, Move move, Game game) {
            return true;
        }
    }, new MoveApplier() {
        @Override
        public Map<String, Card> applyMove(Player subject, Player object, Move move, Game game) throws CantMoveException {
            makeInactive(object, game);
            game.finishGame();
            return new HashMap<>();
        }
    }, new MoveDescription() {
        @Override
        public String getMoveDescription(Player subject, Player object, Move move) {
            return genDescription(object.getName(), move.card);
        }
    });

    Card(int weight, MoveChecker moveChecker, MoveApplier moveApplier, MoveDescription moveDescription) {
        this.weight = weight;
        this.moveChecker = moveChecker;
        this.moveApplier = moveApplier;
        this.moveDescription = moveDescription;
    }

    private final MoveChecker moveChecker;
    private final MoveApplier moveApplier;
    private final MoveDescription moveDescription;
    private final int weight;

    public int getWeight() {
        return weight;
    }

    public MoveApplier getMoveApplier() {
        return moveApplier;
    }

    public MoveChecker getMoveChecker() {
        return moveChecker;
    }

    public MoveDescription getMoveDescription() {
        return moveDescription;
    }

}
