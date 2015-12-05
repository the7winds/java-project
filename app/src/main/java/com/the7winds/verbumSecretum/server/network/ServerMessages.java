package com.the7winds.verbumSecretum.server.network;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.the7winds.verbumSecretum.other.Message;
import com.the7winds.verbumSecretum.server.game.Game;
import com.the7winds.verbumSecretum.server.game.Player;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Created by the7winds on 21.11.15.
 */
public class ServerMessages {

    public static class Connected extends Message {
        public static final String HEAD = "Connected";
        // json structure fields
        private static final String CLIENT_ID_FIELD = "id";

        private String id;

        public Connected() {
            super(HEAD);
        }

        public Connected(String id) {
            super(HEAD);
            this.id = id;
        }

        public String serialize() {
            JsonObject connected = getBaseJsonObject();
            connected.addProperty(CLIENT_ID_FIELD, id);

            return new Gson().toJson(connected);
        }

        @Override
        public Message deserialize(String str) {
            JsonParser jsonParser = new JsonParser();

            id = jsonParser.parse(str)
                    .getAsJsonObject()
                    .get(CLIENT_ID_FIELD)
                    .getAsString();

            return this;
        }

        public String getId() {
            return id;
        }
    }

    public static class WaitingPlayersStatus extends Message {
        public static final String HEAD = "WaitingPlayersStatus";
        // JSON structure fields
        private static final String PLAYERS_ID_TO_NAMES_FIELD = "playersNames";
        private static final String PLAYER_ID_FIELD = "id";
        private static final String PLAYER_NAME_FIELD = "name";

        private Map<String, String> playersNames = new Hashtable<>();

        public WaitingPlayersStatus() {
            super(HEAD);
        }

        public WaitingPlayersStatus(Map<String, Player> playersMap) {
            super(HEAD);

            for (String id : playersMap.keySet()) {
                Player player = playersMap.get(id);
                playersNames.put(id, player.getName());
            }
        }

        public String serialize() {
            JsonObject waitingPlayersStatus = getBaseJsonObject();
            JsonArray idToNames = new JsonArray();

            for (String id : this.playersNames.keySet()) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty(PLAYER_ID_FIELD, id);
                jsonObject.addProperty(PLAYER_NAME_FIELD, this.playersNames.get(id));
                idToNames.add(jsonObject);
            }

            waitingPlayersStatus.add(PLAYERS_ID_TO_NAMES_FIELD, idToNames);

            return new Gson().toJson(waitingPlayersStatus);
        }

        @Override
        public Message deserialize(String str) {
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = jsonParser.parse(str).getAsJsonObject();

            JsonArray playersNames = jsonObject.getAsJsonArray(PLAYERS_ID_TO_NAMES_FIELD);

            this.playersNames = new Hashtable<>();

            for (int i = 0; i < playersNames.size(); i++) {
                JsonObject idName = playersNames.get(i).getAsJsonObject();
                String id = idName.get(PLAYER_ID_FIELD).getAsString();
                String name = idName.get(PLAYER_NAME_FIELD).getAsString();
                this.playersNames.put(id, name);
            }

            return this;
        }

        public Map<String, String> getPlayersNames() {
            return playersNames;
        }
    }

    public static class Disconnected extends Message {
        public static final String HEAD = "Disconnected";

        public Disconnected() {
            super(HEAD);
        }
    }

    public static class GameStart extends Message {
        public static final String HEAD = "GameStart";
        // JSON structure
        private static final String IDS_FIELD = "ids";
        private static final String NAMES_FIELD = "names";
        private static final String CARDS_FIELD = "cards";

        private Map<String, String> idToNames = new Hashtable<>();
        private Map<String, Game.Card> idToCard = new Hashtable<>();

        public GameStart() {
            super(HEAD);
        }

        public GameStart(Map<String, Player> players) {
            super(HEAD);
            for (String id : players.keySet()) {
                idToNames.put(id, players.get(id).getName());
                idToCard.put(id, players.get(id).getHandCard());
            }
        }

        @Override
        public String serialize() {
            Gson gson = new Gson();

            JsonObject gameStart = getBaseJsonObject();

            JsonArray ids = new JsonArray();
            JsonArray names = new JsonArray();
            JsonArray cards = new JsonArray();

            for (String id : idToNames.keySet()) {
                ids.add(id);
                names.add(idToNames.get(id));
                cards.add(idToCard.get(id).name());
            }

            gameStart.add(IDS_FIELD, ids);
            gameStart.add(NAMES_FIELD, names);
            gameStart.add(CARDS_FIELD, cards);

            return gson.toJson(gameStart);
        }

        @Override
        public Message deserialize(String str) {
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = jsonParser.parse(str).getAsJsonObject();

            JsonArray ids = jsonObject.getAsJsonArray(IDS_FIELD);
            JsonArray names = jsonObject.getAsJsonArray(NAMES_FIELD);
            JsonArray cards = jsonObject.getAsJsonArray(CARDS_FIELD);

            for (int i = 0; i < ids.size(); ++i) {
                String id = ids.get(i).getAsString();
                String name = names.get(i).getAsString();
                Game.Card card = Game.Card.valueOf(cards.get(i).getAsString());
                idToNames.put(id, name);
                idToCard.put(id, card);
            }

            return this;
        }
    }

    public static class GameStarting extends Message {
        public static final String HEAD = "GameStarting";

        public GameStarting() {
            super(HEAD);
        }
    }

    public static class GameState extends Message {
        public static final String HEAD = "GameState";
        // JSON structure
        // TODO


        private Map<String, Queue<Game.Card>> playedCards = new Hashtable<>();
        private Map<String, Game.Card> cardsThatShouldBeShowed = new Hashtable<>();
        private String current;

        public GameState() {
            super(HEAD);
        }

        public GameState(String current,
                         Map<String, Player> players, Map<String, Game.Card> cardsThatShouldBeShowed) {
            super(HEAD);
            for (String id : players.keySet()) {
                playedCards.put(id, players.get(id).getPlayedCards());
            }
            this.cardsThatShouldBeShowed = cardsThatShouldBeShowed;
            this.current = current;
        }

        @Override
        public String serialize() { // TODO
            Gson gson = new Gson();

            JsonObject gameData = new JsonObject();

            gameData.addProperty("HEAD", HEAD);

            JsonArray playersData = new JsonArray();

            for (String id : playedCards.keySet()) {
                JsonObject playerData = new JsonObject();

                playerData.addProperty("id", id);

                JsonArray playedCards = new JsonArray();

                for (Game.Card card : this.playedCards.get(id)) {
                    playedCards.add(card.name());
                }

                playerData.add("playedCards", playedCards);
            }

            gameData.addProperty("current", current);

            JsonArray ids = new JsonArray();
            JsonArray cards = new JsonArray();

            for (String id : cardsThatShouldBeShowed.keySet()) {
                ids.add(id);
                cards.add(cardsThatShouldBeShowed.get(id).name());
            }

            JsonObject cardsThatShouldBeShowed = new JsonObject();
            cardsThatShouldBeShowed.add("ids", ids);
            cardsThatShouldBeShowed.add("cards", cards);

            gameData.add("cardsThatShouldBeShowed", cardsThatShouldBeShowed);


            return gson.toJson(gameData);
        }

        @Override
        public Message deserialize(String str) {
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = jsonParser.parse(str).getAsJsonObject();

            this.current = jsonObject.get("current").getAsString();

            JsonObject cardsThatShouldBeShowed = jsonObject.getAsJsonObject("cardsThatShouldBeShowed");
            JsonArray ids = cardsThatShouldBeShowed.getAsJsonArray("ids");
            JsonArray cards = cardsThatShouldBeShowed.getAsJsonArray("cards");

            for (int i = 0; i < ids.size(); i++) {
                String id = ids.get(i).getAsString();
                Game.Card card = Game.Card.valueOf(cards.get(i).getAsString());
                this.cardsThatShouldBeShowed.put(id, card);
            }

            JsonArray playedCards = jsonObject.getAsJsonArray("playedCards");

            for (int i = 0; i < playedCards.size(); i++) {
                JsonObject playerData = playedCards.get(i).getAsJsonObject();
                String id = playerData.get("id").getAsString();
                cards = playerData.getAsJsonArray("cards");
                Queue<Game.Card> cardQueue = new LinkedList<>();

                for (int j = 0; j < cards.size(); j++) {
                    cardQueue.add(Game.Card.valueOf(cards.get(j).getAsString()));
                }

                this.playedCards.put(id, cardQueue);
            }

            return this;
        }
    }

    public static class YourTurn extends Message {
        public final static String HEAD = "YourTurn";
        private Game.Card card;

        public YourTurn() {
            super(HEAD);
        }

        public YourTurn(Game.Card card) {
            super(HEAD);
            this.card = card;
        }

        @Override
        public String serialize() {
            Gson gson = new Gson();

            JsonObject yourTurn = getBaseJsonObject();

            yourTurn.addProperty("card", card.name());

            return gson.toJson(yourTurn);
        }

        @Override
        public Message deserialize(String str) {
            JsonParser jsonParser = new JsonParser();
            card = Game.Card.valueOf(jsonParser.parse(str)
                    .getAsJsonObject()
                    .get("card")
                    .getAsString());

            return this;
        }
    }

    public static class InvalidMove extends Message {
        public static final String HEAD = "HEAD";

        InvalidMove() {
            super(HEAD);
        }
    }

    public static class GameOver extends Message { // TODO
        public static final String HEAD = "GameOver";
        private String[] winners;

        public GameOver() {
            super(HEAD);
        }

        public GameOver(String[] result) {
            super(HEAD);
            winners = result;
        }

        @Override
        public String serialize() {
            Gson gson = new Gson();

            JsonObject gameOver = new JsonObject();

            gameOver.addProperty("HEAD", HEAD);

            JsonArray winners = new JsonArray();

            for (String winner : this.winners) {
                winners.add(winner);
            }

            gameOver.add("winners", winners);

            return gson.toJson(gameOver);
        }

        @Override
        public Message deserialize(String str) {
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = new JsonObject().getAsJsonObject();

            JsonArray jsonArray = jsonObject.getAsJsonArray("winners");
            winners = new String[jsonArray.size()];
            for (int i = 0; i < winners.length; i++) {
                winners[i] = jsonArray.get(i).getAsString();
            }

            return this;
        }
    }
}
