package com.the7winds.verbumSecretum.client.activities;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.the7winds.verbumSecretum.R;
import com.the7winds.verbumSecretum.client.network.PlayerMessages;
import com.the7winds.verbumSecretum.client.other.ClientUtils;
import com.the7winds.verbumSecretum.client.other.Events;
import com.the7winds.verbumSecretum.server.game.Card;
import com.the7winds.verbumSecretum.server.game.Move;
import com.the7winds.verbumSecretum.server.network.ServerMessages;

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import de.greenrobot.event.EventBus;

import static com.the7winds.verbumSecretum.server.game.Cards.GUARD_CARD;
import static com.the7winds.verbumSecretum.server.game.Cards.KING_CARD;
import static com.the7winds.verbumSecretum.server.game.Cards.LORD_CARD;
import static com.the7winds.verbumSecretum.server.game.Cards.PRIEST_CARD;
import static com.the7winds.verbumSecretum.server.game.Cards.PRINCE_CARD;
import static com.the7winds.verbumSecretum.server.game.Cards.values;

public class GameActivity extends Activity {

    private AvaView myAva;

    private enum State {WAITING_UPD, CARD_SELECT, OPPONENT_SELECT, ROLE_SELECT, FINISH}
    private State state = State.WAITING_UPD;
    private final Move move = new Move();

    private ViewGroup handView;
    private TextView infoTextView;
    private Map<String, AvaView> playersAvas = new Hashtable<>();
    private Map<String, LinearLayout> playedCards = new Hashtable<>();

    private ViewGroup gameLayout;
    private TableLayout tableLayout;
    private View selector;
    private ViewGroup showCardFrame;
    private TextView cardsCounter;

    private int edge;
    private AvaView current;
    private CardView selectedCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        EventBus.getDefault().register(this);

        edge = getResources().getDisplayMetrics().widthPixels / 5;

        initSelector();

        showCardFrame = (ViewGroup) getLayoutInflater().inflate(R.layout.game_show_card, null);

        gameLayout = (ViewGroup) findViewById(R.id.game_frame);
        tableLayout = (TableLayout) findViewById(R.id.game_table);
        infoTextView = (TextView) findViewById(R.id.game_info_text);
        cardsCounter = (TextView) findViewById(R.id.cards_counter);

        ClientUtils.Data.gameActivityInited.set(true);
    }

    private void initSelector() {
        LayoutInflater inflater = getLayoutInflater();
        selector = inflater.inflate(R.layout.game_card_selector, null);
        ViewGroup tableRow = (ViewGroup) selector.findViewWithTag(getString(R.string.game_choose_row));

        for (int i = 1; i < values().length; i++) {
            CardView cardView = new CardView(values()[i]);
            tableRow.addView(cardView);
        }
    }

    private class CardView extends FrameLayout {

        private static final int padding = 3;
        private final Card card;
        private ImageView imageView;

        public CardView(final Card card) {
            super(GameActivity.this);

            this.card = card;

            setPadding(padding, padding, padding, padding);
            Drawable image = card.getDrawable(GameActivity.this);

            imageView = new ImageView(GameActivity.this);
            imageView.setImageDrawable(image);
            addView(imageView);

            imageView.setLayoutParams(new LayoutParams(edge, edge));
            imageView.setOnClickListener(new OnClickHandler());
        }

        @Override
        public void setClickable(boolean clickable) {
            super.setClickable(clickable);
            imageView.setClickable(clickable);
        }

        private class OnClickHandler implements OnClickListener {
            @Override
            public void onClick(View v) {
                if (state == State.CARD_SELECT) {
                    selectedCard = CardView.this;
                    move.card = card;

                    if ((card == GUARD_CARD)
                            || (card == PRIEST_CARD)
                            || (card == LORD_CARD)
                            || (card == PRINCE_CARD)
                            || (card == KING_CARD)) {
                        for (AvaView ava : playersAvas.values()) {
                            ava.setHighlightClickable();
                        }

                        if (card == PRINCE_CARD) {
                            myAva.setHighlightClickable();
                        }

                        state = State.OPPONENT_SELECT;
                    } else {
                        EventBus.getDefault().post(new Events.SendToServerEvent(new PlayerMessages.Move(move)));
                        state = State.WAITING_UPD;
                    }
                } else if (state == State.ROLE_SELECT) {
                    move.role = card;
                    gameLayout.removeView(selector);
                    EventBus.getDefault().post(new Events.SendToServerEvent(new PlayerMessages.Move(move)));
                    state = State.WAITING_UPD;
                }
            }
        }
    }

    private class AvaView extends FrameLayout {

        private final ImageView imageView = new ImageView(GameActivity.this);
        private boolean inactiveFlag = false;

        public AvaView(final String id) {
            super(GameActivity.this);

            imageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    resetAvas();
                    move.opponentId = id;

                    if (move.card == GUARD_CARD) {
                        state = State.ROLE_SELECT;
                        gameLayout.addView(selector);
                    } else {
                        EventBus.getDefault().post(new Events.SendToServerEvent(new PlayerMessages.Move(move)));
                        state = State.WAITING_UPD;
                    }
                }
            });

            addView(imageView);

            imageView.setLayoutParams(new LayoutParams(edge, edge));

            setNormal();
        }

        @Override
        public void setClickable(boolean clickable) {
            super.setClickable(clickable);
            imageView.setClickable(clickable);
        }

        public void setNormal() {
            if (!inactiveFlag) {
                imageView.setImageResource(R.drawable.normal);
                imageView.setClickable(false);
            }
        }

        public void setHighlightClickable() {
            if (!inactiveFlag) {
                imageView.setImageResource(R.drawable.clickable);
                imageView.setClickable(true);
            }
        }

        public void setHighlightCurrent() {
            if (!inactiveFlag) {
                imageView.setImageResource(R.drawable.current);
                imageView.setClickable(false);
            }
        }

        public void setHighlightInactive() {
            imageView.setImageResource(R.drawable.inactive);
            imageView.setClickable(false);
            inactiveFlag = true;
        }
    }

    public void onEventMainThread(ServerMessages.GameStart gameStart) {
        LayoutInflater layoutInflater = getLayoutInflater();

        Set<String> ids = new TreeSet<>(ClientUtils.Data.playersNames.keySet());
        ids.remove(ClientUtils.Data.id);

        int counter = 1;
        for (String id : ids) {
            View playerView = layoutInflater.inflate(R.layout.game_player, null);

            AvaView avaView = new AvaView(id);
            playersAvas.put(id, avaView);
            ((ViewGroup) playerView
                    .findViewWithTag(getString(R.string.game_ava_layout)))
                    .addView(avaView);

            ((TextView) playerView
                    .findViewWithTag(getString(R.string.game_player_name)))
                    .setText(ClientUtils.Data.playersNames.get(id));

            playedCards.put(id, (LinearLayout) playerView.findViewWithTag(getString(R.string.game_played_cards)));

            tableLayout.addView(playerView, counter++);
        }

        ((ViewGroup) findViewById(R.id.game_my_ava))
                .addView(myAva = new AvaView(ClientUtils.Data.id));

        ((TextView) findViewById(R.id.game_my_name))
                .setText(ClientUtils.Data.playerStatisticsData.name);

        (handView = ((ViewGroup) findViewById(R.id.game_my_hand)))
                .addView(new CardView(gameStart.getIdToCard().get(ClientUtils.Data.id)));

        (current = findAvaFromAll(gameStart.getFirst())).setHighlightCurrent();

        playedCards.put(ClientUtils.Data.id, (LinearLayout) findViewById(R.id.game_my_played_cards));

        cardsCounter.setText(genDeckSizeMessage(gameStart.getDeckSize()));
    }

    public void onEventMainThread(ServerMessages.GameState gameState) {
        resetAvas();

        for (String id : ClientUtils.Data.playersNames.keySet()) {
            if (gameState.getActivePlayersIdHandCard().containsKey(id)) {
                findAvaFromAll(id).setNormal();
            } else {
                findAvaFromAll(id).setHighlightInactive();
            }
        }

        Map<String, Card> cardsThatShouldBeShowed = gameState.getCardsThatShouldBeShowed();

        if (cardsThatShouldBeShowed.containsKey(ClientUtils.Data.id)) {
            CardView cardView = new CardView(cardsThatShouldBeShowed.get(ClientUtils.Data.id));
            cardView.setClickable(false);
            showCardFrame.addView(cardView);
            gameLayout.addView(showCardFrame);
        }

        (current = findAvaFromAll(gameState.getCurrent())).setHighlightCurrent();

        handView.removeAllViews();

        CardView cardView = new CardView(gameState.getActivePlayersIdHandCard().get(ClientUtils.Data.id));
        handView.addView(cardView);

        String prevId = gameState.getNewPlayedCard().first;
        CardView prevCardView = new CardView(gameState.getNewPlayedCard().second);
        prevCardView.setClickable(false);
        playedCards.get(prevId).addView(prevCardView);

        infoTextView.setText(gameState.getDescription());
        cardsCounter.setText(genDeckSizeMessage(gameState.getDeckSize()));
    }

    private String genDeckSizeMessage(int deckSize) {
        return getString(R.string.game_cards_counter)
                + " " + Integer.toString(deckSize);
    }

    public void onEventMainThread(ServerMessages.YourTurn yourTurn) {
        move.playerId = ClientUtils.Data.id;

        Card card = yourTurn.getCard();
        CardView cardView = new CardView(card);
        handView.addView(cardView);

        resetAvas();
        state = State.CARD_SELECT;
    }

    public void onEventMainThread(ServerMessages.GameOver gameOver) {
        state = State.FINISH;

        LayoutInflater layoutInflater = getLayoutInflater();
        ViewGroup gameOverFrame = (ViewGroup) layoutInflater.inflate(R.layout.game_game_over_frame, null);

        TableLayout winners = (TableLayout) gameOverFrame.findViewWithTag(getString(R.string.game_winners));

        for (String name : gameOver.getWinners().values()) {
            TableRow tableRow = new TableRow(this);
            TextView nameView = new TextView(this);

            nameView.setText(name);
            tableRow.addView(nameView);

            winners.addView(tableRow);
        }

        TableLayout last = (TableLayout) gameOverFrame.findViewWithTag(getString(R.string.game_last_players));

        for (Pair<String, Card> nameCard : gameOver.getLastNamesCards().values()) {
            TableRow tableRow = new TableRow(this);
            TextView nameView = new TextView(this);
            CardView cardView = new CardView(nameCard.second);

            cardView.setClickable(false);
            cardView.setLayoutParams(new TableRow.LayoutParams(0
                    , ViewGroup.LayoutParams.WRAP_CONTENT
                    , 1));

            nameView.setText(nameCard.first);
            nameView.setLayoutParams(new TableRow.LayoutParams(0
                    , ViewGroup.LayoutParams.WRAP_CONTENT
                    , 1));

            tableRow.addView(nameView);
            tableRow.addView(cardView);

            last.addView(tableRow);
        }

        gameLayout.addView(gameOverFrame);
    }

    private void resetAvas() {
        myAva.setNormal();
        myAva.setClickable(false);

        for (AvaView ava : playersAvas.values()) {
            ava.setNormal();
            ava.setClickable(false);
        }

        current.setHighlightCurrent();
    }

    public void onEventMainThread(ServerMessages.InvalidMove event) {
        resetAvas();
        infoTextView.setText(getString(R.string.game_invalid_move_message));
        state = State.CARD_SELECT;
    }

    public void onEventMainThread(ServerMessages.Correct event) {
        resetAvas();
        handView.removeView(selectedCard);
    }

    private AvaView findAvaFromAll(String id) {
        return playersAvas.containsKey(id) ? playersAvas.get(id)
                : myAva;
    }

    public void onClickShowCardFrame(View view) {
        showCardFrame.removeAllViews();
        gameLayout.removeView(showCardFrame);
    }

    public void onClickGameOverFrameOk(View view) {
        freeResources();
        finish();
    }

    public void onEvent(ServerMessages.Disconnected event) {
        if (state != State.FINISH) {
            freeResources();
            finish();
        }
    }

    private void freeResources() {
        ClientUtils.Data.gameActivityInited.set(false);
        ClientUtils.Data.gameActivityStarted.set(false);

        if (state != State.FINISH) {
            EventBus.getDefault().post(new Events.SendToServerEvent(new PlayerMessages.Leave()));
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    protected void onPause() {
        freeResources();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        freeResources();
        finish();

        super.onBackPressed();
    }
}
