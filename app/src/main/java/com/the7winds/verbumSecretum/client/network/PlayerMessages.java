package com.the7winds.verbumSecretum.client.network;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.the7winds.verbumSecretum.server.game.Cards;
import com.the7winds.verbumSecretum.utils.Message;

/**
 * Created by the7winds on 28.10.15.
 */
public class PlayerMessages {

    public static class Ready extends Message {
        // JSON fields consts
        public static final String HEAD = "Ready";
        // JSON fields structure
        private static final String NAME_FIELD = "name";

        // fields
        private String name;

        public Ready() {
            super(HEAD);
        }

        public Ready(String name) {
            super(HEAD);
            this.name = name;
        }

        @Override
        public String serialize() {
            JsonObject ready = getBaseJsonObject();
            ready.addProperty(NAME_FIELD, name);

            return new Gson().toJson(ready);
        }

        @Override
        public Message deserialize(String str) {
            JsonParser jsonParser = new JsonParser();

            name = jsonParser.parse(str)
                    .getAsJsonObject()
                    .get(NAME_FIELD)
                    .getAsString();

            return this;
        }

        public String getName() {
            return name;
        }
    }

    public static class NotReady extends Message {
        // JSON fields consts
        public static final String HEAD = "NotReady";

        public NotReady() {
            super(HEAD);
        }
    }

    public static class Leave extends Message {
        // JSON fields consts
        public static final String HEAD = "Leave";

        public Leave() {
            super(HEAD);
        }
    }

    public static class Move extends Message {
        // JSON fields consts
        public static final String HEAD = "Move";
        // JSON fields structure
        private static final String PLAYER_ID_FIELD = "playerId";
        private static final String CARD_FIELD = "card";
        private static final String OPPONENT_ID_FIELD = "opponentId";
        private static final String ROLE_FIELD = "role";

        // fields
        private com.the7winds.verbumSecretum.server.game.Move move;

        public Move() {
            super(HEAD);
        }

        public Move(com.the7winds.verbumSecretum.server.game.Move move) {
            super(HEAD);
            this.move = move;
        }

        @Override
        public String serialize() {
            JsonObject moveMsg = getBaseJsonObject();

            moveMsg.addProperty(PLAYER_ID_FIELD, move.playerId);
            moveMsg.addProperty(CARD_FIELD, move.card.name());
            moveMsg.addProperty(OPPONENT_ID_FIELD, move.opponentId);
            moveMsg.addProperty(ROLE_FIELD, move.role.name());

            return new Gson().toJson(moveMsg);
        }

        public com.the7winds.verbumSecretum.server.game.Move getMove() {
            return move;
        }

        @Override
        public Message deserialize(String str) {
            JsonParser parser = new JsonParser();
            JsonObject jsonObject = parser.parse(str).getAsJsonObject();

            move = new com.the7winds.verbumSecretum.server.game.Move();
            move.playerId = jsonObject.get(PLAYER_ID_FIELD).getAsString();
            move.card = Cards.valueOf(jsonObject.get(CARD_FIELD).getAsString());
            move.opponentId = jsonObject.get(OPPONENT_ID_FIELD).getAsString();
            move.role = Cards.valueOf(jsonObject.get(ROLE_FIELD).getAsString());

            return this;
        }
    }

}
