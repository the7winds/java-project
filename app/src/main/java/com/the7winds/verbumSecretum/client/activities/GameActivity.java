package com.the7winds.verbumSecretum.client.activities;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.the7winds.verbumSecretum.R;
import com.the7winds.verbumSecretum.client.network.PlayerMessages;
import com.the7winds.verbumSecretum.client.other.ClientData;
import com.the7winds.verbumSecretum.client.other.Events;
import com.the7winds.verbumSecretum.server.game.Game;
import com.the7winds.verbumSecretum.server.network.ServerMessages;

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import de.greenrobot.event.EventBus;

public class GameActivity extends Activity {

    private AvaView myAva;

    private enum State {WAITING_UPD, CARD_SELECT, SUBJECT_SELECT, ROLE_SELECT, FINISH}
    private State state = State.WAITING_UPD;
    private final Game.Move move = new Game.Move();

    private ViewGroup handView;
    private TextView infoTextView;
    private Map<String, AvaView> playersAvas = new Hashtable<>();
    private Map<String, LinearLayout> playedCards = new Hashtable<>();

    private FrameLayout frameLayout;
    private TableLayout tableLayout;
    private HorizontalScrollView selector;
    private FrameLayout showCardFrame;

    private final static int edge = 90;
    private AvaView current;
    private CardView selectedCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        EventBus.getDefault().register(this);
        initSelector();

        showCardFrame = (FrameLayout) getLayoutInflater().inflate(R.layout.game_show_card_frame, null);

        frameLayout  = (FrameLayout) findViewById(R.id.game_frame);
        tableLayout = (TableLayout) findViewById(R.id.game_table);
        infoTextView = (TextView) findViewById(R.id.game_info_text);

        EventBus.getDefault().post(new Events.GameActivityInited());
    }

    private void initSelector() {
        LayoutInflater inflater = getLayoutInflater();
        selector = (HorizontalScrollView) inflater.inflate(R.layout.game_card_selector, null);
        LinearLayout tableRow = (LinearLayout) selector.findViewWithTag(getString(R.string.game_choose_row));

        for (int i = 1; i < Game.Card.values().length; i++) {
            CardView cardView = new CardView(Game.Card.values()[i]);
            tableRow.addView(cardView);
        }
    }

    private class CardView extends FrameLayout {

        private final Game.Card card;
        private ImageView imageView;

        public CardView(final Game.Card card) {
            super(GameActivity.this);

            setPadding(10, 10, 10, 10);
            this.card = card;
            Drawable image = getCardDrawable(card);

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

                    if (card == Game.Card.GUARD_CARD
                            || card == Game.Card.PRIST_CARD
                            || card == Game.Card.LORD_CARD
                            || card == Game.Card.PRINCE_CARD
                            || card == Game.Card.KING_CARD) {
                        for (AvaView ava : playersAvas.values()) {
                            ava.setActive();
                            ava.setClickable(true);
                        }

                        if (card == Game.Card.PRINCE_CARD) {
                            myAva.setActive();
                            myAva.setClickable(true);
                        }

                        state = State.SUBJECT_SELECT;
                    } else {
                        EventBus.getDefault().post(new Events.SendToServerEvent(new PlayerMessages.Move(move)));
                        state = State.WAITING_UPD;
                    }
                } else if (state == State.ROLE_SELECT) {
                    move.role = card;
                    frameLayout.removeView(selector);
                    EventBus.getDefault().post(new Events.SendToServerEvent(new PlayerMessages.Move(move)));
                    state = State.WAITING_UPD;
                }
            }
        }
    }

    private Drawable getCardDrawable(Game.Card card) {
        switch (card) {
            case GUARD_CARD:
                return getResources().getDrawable(R.drawable.c1);
            case PRIST_CARD:
                return getResources().getDrawable(R.drawable.c2);
            case LORD_CARD:
                return getResources().getDrawable(R.drawable.c3);
            case STAFF_CARD:
                return getResources().getDrawable(R.drawable.c4);
            case PRINCE_CARD:
                return getResources().getDrawable(R.drawable.c5);
            case KING_CARD:
                return getResources().getDrawable(R.drawable.c6);
            case COUNTESS_CARD:
                return getResources().getDrawable(R.drawable.c7);
            case PRINCESS_CARD:
                return getResources().getDrawable(R.drawable.c8);
            default:
                return null;
        }
    }

    private class AvaView extends FrameLayout {

        private final Drawable defaultImage = getResources().getDrawable(R.drawable.normal);
        private final ImageView imageView = new ImageView(GameActivity.this);

        public AvaView(final String id) {
            super(GameActivity.this);
            setNormal();

            imageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    resetAvas();
                    move.subjectId = id;

                    if (move.card == Game.Card.GUARD_CARD) {
                        state = State.ROLE_SELECT;
                        frameLayout.addView(selector);
                    } else {
                        EventBus.getDefault().post(new Events.SendToServerEvent(new PlayerMessages.Move(move)));
                        state = State.WAITING_UPD;
                    }
                }
            });

            addView(imageView);

            imageView.setLayoutParams(new LayoutParams(edge, edge));

            setClickable(false);
        }

        @Override
        public void setClickable(boolean clickable) {
            super.setClickable(clickable);
            imageView.setClickable(clickable);
        }

        public void setNormal() {
            // defaultImage.setAlpha(150);
            // defaultImage.clearColorFilter();
        }
        
        public void setActive() {
            defaultImage.setColorFilter(Color.YELLOW, PorterDuff.Mode.SCREEN);
        }

        public void setHighlight() {
            //defaultImage.setAlpha(255);
        }

        public void setInactive() {
            defaultImage.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SCREEN);
        }
    }

    public void onEventMainThread(ServerMessages.GameStart gameStart) {
        LayoutInflater layoutInflater = getLayoutInflater();

        Set<String> ids = new TreeSet<>(ClientData.playersNames.keySet());
        ids.remove(ClientData.id);

        int counter = 0;
        for (String id : ids) {
            View playerView = layoutInflater.inflate(R.layout.game_player, null);

            AvaView avaView = new AvaView(id);
            playersAvas.put(id, avaView);
            ((ViewGroup) playerView
                    .findViewWithTag(getString(R.string.game_ava_layout)))
                    .addView(avaView);

            ((TextView) playerView
                    .findViewWithTag(getString(R.string.game_player_name)))
                    .setText(ClientData.playersNames.get(id));

            playedCards.put(id, (LinearLayout) playerView.findViewWithTag(getString(R.string.game_played_cards)));

            tableLayout.addView(playerView, counter++);
        }

        ((ViewGroup) findViewById(R.id.game_my_ava))
                .addView(myAva = new AvaView(ClientData.id));

        ((TextView) findViewById(R.id.game_my_name))
                .setText(ClientData.name);

        (handView = ((ViewGroup) findViewById(R.id.game_my_hand)))
                .addView(new CardView((Game.Card) ClientData.hand.toArray()[0]));

        current = findAvaFromAll(gameStart.getFirst());
        current.setHighlight();

        playedCards.put(ClientData.id, (LinearLayout) findViewById(R.id.game_my_played_cards));
    }

    public void onEventMainThread(ServerMessages.GameState gameState) {
        resetAvas();

        current = findAvaFromAll(gameState.getCurrent());
        current.setHighlight();

        for (String id : ClientData.playersNames.keySet()) {
            if (gameState.getActivePlayersIdHandCard().containsKey(id)) {
                findAvaFromAll(id).setNormal();
            } else {
                findAvaFromAll(id).setInactive();
            }
        }

        Pair<String, Game.Card> newPlayedCard = gameState.getNewPlayedCard();

        Map<String, Game.Card> cardsThatShouldbeShowed = gameState.getCardsThatShouldBeShowed();

        if (cardsThatShouldbeShowed.containsKey(ClientData.id)) {
            CardView cardView = new CardView(cardsThatShouldbeShowed.get(ClientData.id));
            cardView.setClickable(false);
            showCardFrame.addView(cardView);
            frameLayout.addView(showCardFrame);
        }

        handView.removeAllViews();

        CardView cardView = new CardView(gameState.getActivePlayersIdHandCard().get(ClientData.id));
        handView.addView(cardView);

        String prevId = gameState.getNewPlayedCard().first;
        CardView prevCardView = new CardView(gameState.getNewPlayedCard().second);
        prevCardView.setClickable(false);
        playedCards.get(prevId).addView(prevCardView);
    }

    public void onEventMainThread(ServerMessages.YourTurn yourTurn) {
        move.objectId = ClientData.id;

        Game.Card card = yourTurn.getCard();
        CardView cardView = new CardView(card);
        handView.addView(cardView);

        state = State.CARD_SELECT;
    }

    private void resetAvas() {
        myAva.setNormal();
        myAva.setClickable(false);

        for (AvaView ava : playersAvas.values()) {
            ava.setNormal();
            ava.setClickable(false);
        }

        current.setHighlight();
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
        frameLayout.removeView(showCardFrame);
    }
}
