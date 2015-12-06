package com.the7winds.verbumSecretum.server.network;

import android.util.Pair;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.the7winds.verbumSecretum.other.Message;
import com.the7winds.verbumSecretum.server.game.Game;
import com.the7winds.verbumSecretum.server.game.Player;

import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

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

        public Map<String, String> getIdToNames() {
            return idToNames;
        }

        public Map<String, Game.Card> getIdToCard() {
            return idToCard;
        }

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

            return new Gson().toJson(gameStart);
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
        private static final String NEW_PLAYED_CARD_ID_FIELD = "new_played_card_id";
        private static final String NEW_PLAYED_CARD_FIELD = "new_played_card";
        private static final String CARDS_THAT_SHOULD_BE_SHOWED_ID_FIELD = "cards_that_should_be_showed_id";
        private static final String CARDS_THAT_SHOULD_BE_SHOWED_CARDS_FIELD = "cards_that_should_be_showed_cards";
        private static final String CURRENT_FIELD = "current";
        private static final String ACTIVE_PLAYERS_ID_FIELD = "active_players_id";


        // fields
        private Pair<String, Game.Card> newPlayedCard;
        private Map<String, Game.Card> cardsThatShouldBeShowed = new Hashtable<>();
        private String current;
        private Set<String> activePlayersId = new LinkedHashSet<>();

        public Pair<String, Game.Card> getNewPlayedCard() {
            return newPlayedCard;
        }

        public Map<String, Game.Card> getCardsThatShouldBeShowed() {
            return cardsThatShouldBeShowed;
        }

        public String getCurrent() {
            return current;
        }

        public Set<String> getActivePlayersId() {
            return activePlayersId;
        }

        public GameState() {
            super(HEAD);
        }

        public GameState(String current
                         , Pair<String, Game.Card> newPlayedCard
                         , Map<String, Game.Card> cardsThatShouldBeShowed
                         , Set<String> activePlayersId) {
            super(HEAD);

            this.current = current;
            this.newPlayedCard = newPlayedCard;
            this.cardsThatShouldBeShowed = cardsThatShouldBeShowed;
            this.activePlayersId = activePlayersId;
        }

        @Override
        public String serialize() {
            JsonObject gameData = getBaseJsonObject();

            gameData.addProperty(CURRENT_FIELD, current);

            gameData.addProperty(NEW_PLAYED_CARD_ID_FIELD, newPlayedCard.first);
            gameData.addProperty(NEW_PLAYED_CARD_FIELD, newPlayedCard.second.toString());

            JsonArray ids = new JsonArray();
            JsonArray cards = new JsonArray();

            for (String id : cardsThatShouldBeShowed.keySet()) {
                ids.add(id);
                cards.add(cardsThatShouldBeShowed.get(id).name());
            }

            gameData.add(CARDS_THAT_SHOULD_BE_SHOWED_ID_FIELD, ids);
            gameData.add(CARDS_THAT_SHOULD_BE_SHOWED_CARDS_FIELD, cards);

            JsonArray activeIds = new JsonArray();
            for (String id : activePlayersId) {
                activeIds.add(id);
            }

            gameData.add(ACTIVE_PLAYERS_ID_FIELD, activeIds);

            return new Gson().toJson(gameData);
        }

        @Override
        public Message deserialize(String str) {
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = jsonParser.parse(str).getAsJsonObject();

            current = jsonObject.get(CURRENT_FIELD).getAsString();

            newPlayedCard = new Pair<>(jsonObject.get(NEW_PLAYED_CARD_ID_FIELD).getAsString(),
                    Game.Card.valueOf(jsonObject.get(NEW_PLAYED_CARD_FIELD).getAsString()));

            JsonArray ids = jsonObject.getAsJsonArray(CARDS_THAT_SHOULD_BE_SHOWED_ID_FIELD);
            JsonArray cards = jsonObject.getAsJsonArray(CARDS_THAT_SHOULD_BE_SHOWED_CARDS_FIELD);

            for (int i = 0; i < ids.size(); i++) {
                String id = ids.get(i).getAsString();
                Game.Card card = Game.Card.valueOf(cards.get(i).getAsString());
                cardsThatShouldBeShowed.put(id, card);
            }

            JsonArray activeIds = jsonObject.get(ACTIVE_PLAYERS_ID_FIELD).getAsJsonArray();

            for (int i = 0; i < activeIds.size(); i++) {
                activePlayersId.add(activeIds.get(i).getAsString());
            }

            return this;
        }
    }

    public static class YourTurn extends Message {
        public final static String HEAD = "YourTurn";
        // json structure
        private final static String CARD_FIELD = "card";

        public Game.Card getCard() {
            return card;
        }

        // fields
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
            JsonObject yourTurn = getBaseJsonObject();

            yourTurn.addProperty(CARD_FIELD, card.name());

            return new Gson().toJson(yourTurn);
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

        public InvalidMove() {
            super(HEAD);
        }
    }

    public static class GameOver extends Message {
        public static final String HEAD = "GameOver";
        // json structure fields
        private static final String WINNERS_FIELD = "winners";
        private static final String HAND_CARDS_IDS_FIELD = "ids";
        private static final String HAND_CARDS_CARDS_FIELD = "cards";

        // fields
        private Map<String, Game.Card> handCards;
        private String[] winners;

        public GameOver() {
            super(HEAD);
        }

        public GameOver(String[] result, Map<String, Player> players) {
            super(HEAD);

            winners = result;

            for (String id : players.keySet()) {
                handCards.put(id, players.get(id).getHandCard());
            }
        }

        @Override
        public String serialize() {
            JsonObject gameOver = getBaseJsonObject();

            JsonArray winners = new JsonArray();

            for (String winner : this.winners) {
                winners.add(winner);
            }

            gameOver.add(WINNERS_FIELD, winners);

            JsonArray ids = new JsonArray();
            JsonArray cards = new JsonArray();

            for (String id : handCards.keySet()) {
                ids.add(id);
                cards.add(handCards.get(id).toString());
            }

            gameOver.add(HAND_CARDS_IDS_FIELD, ids);
            gameOver.add(HAND_CARDS_CARDS_FIELD, cards);

            return new Gson().toJson(gameOver);
        }

        @Override
        public Message deserialize(String str) {
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = jsonParser.parse(str).getAsJsonObject();

            JsonArray jWinners = jsonObject.getAsJsonArray(WINNERS_FIELD);
            JsonArray jIds = jsonObject.getAsJsonArray(HAND_CARDS_IDS_FIELD);
            JsonArray jCards = jsonObject.getAsJsonArray(HAND_CARDS_CARDS_FIELD);

            winners = new String[jWinners.size()];
            for (int i = 0; i < winners.length; i++) {
                winners[i] = jWinners.get(i).getAsString();
            }

            for (int i = 0; i < jIds.size(); i++) {
                String id = jIds.get(i).getAsString();
                Game.Card card = Game.Card.valueOf(jCards.get(i).getAsString());
                handCards.put(id, card);
            }

            return this;
        }
    }
}
