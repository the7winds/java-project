package com.the7winds.verbumSecretum.client.activities;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.the7winds.verbumSecretum.R;
import com.the7winds.verbumSecretum.client.network.PlayerMessages;
import com.the7winds.verbumSecretum.client.other.ClientData;
import com.the7winds.verbumSecretum.client.other.Events;
import com.the7winds.verbumSecretum.server.game.Game;
import com.the7winds.verbumSecretum.server.network.ServerMessages;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import de.greenrobot.event.EventBus;

public class GameActivity extends Activity {

    private AvaView myAva;

    private enum State {WAITING_UPD, CARD_SELECT, SUBJECT_SELECT, ROLE_SELECT, FINISH}
    private State state = State.WAITING_UPD;
    private Game.Move move;

    private ViewGroup handView;
    private TextView infoTextView;
    private Map<String, AvaView> playersAvas = new Hashtable<>();
    private Map<Game.Card, List<CardView>> handCardViews = new Hashtable<>();

    private FrameLayout frameLayout;
    private HorizontalScrollView selector;

    // consts
    /*private DisplayMetrics displayMetrics = new DisplayMetrics();
    private static int screenWidth;
    private static int wTertia;*/
    private static int wQuinta = 90;

    /*private static int screenHeight;
    private static int hTertia;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        // TODO finishing parent Activity

        /*getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        wTertia = screenWidth / 3;
        wQuinta = screenWidth / 5;
        screenHeight = displayMetrics.heightPixels;
        hTertia = screenHeight / 3; */

        EventBus.getDefault().register(this);
        initSelector();
        frameLayout  = (FrameLayout) findViewById(R.id.game_frame);
        infoTextView = (TextView) findViewById(R.id.game_info_text);

        EventBus.getDefault().post(new Events.GameActivityInited());
    }

    private void initSelector() {
        LayoutInflater inflater = getLayoutInflater();
        selector = (HorizontalScrollView) inflater.inflate(R.layout.choose_smth, null);
        LinearLayout tableRow = (LinearLayout) selector.findViewWithTag(getString(R.string.game_choose_row));

        for (int i = 1; i < Game.Card.values().length; i++) {
            CardView cardView = new CardView(Game.Card.values()[i]);
            cardView.setPadding(10, 10, 10, 10);
            tableRow.addView(cardView);
        }
    }

    private class CardView extends FrameLayout {

        private final Game.Card card;
        private ImageView imageView;

        public CardView(final Game.Card card) {
            super(GameActivity.this);

            this.card = card;
            Drawable image = null;
            switch (card) {
                case GUARD_CARD:
                    image = getResources().getDrawable(R.drawable.c1);
                    break;
                case PRIST_CARD:
                    image = getResources().getDrawable(R.drawable.c2);
                    break;
                case LORD_CARD:
                    image = getResources().getDrawable(R.drawable.c3);
                    break;
                case STAFF_CARD:
                    image = getResources().getDrawable(R.drawable.c4);
                    break;
                case PRINCE_CARD:
                    image = getResources().getDrawable(R.drawable.c5);
                    break;
                case KING_CARD:
                    image = getResources().getDrawable(R.drawable.c6);
                    break;
                case COUNTESS_CARD:
                    image = getResources().getDrawable(R.drawable.c7);
                    break;
                case PRINCESS_CARD:
                    image = getResources().getDrawable(R.drawable.c8);
                    break;
            }

            imageView = new ImageView(GameActivity.this);
            imageView.setImageDrawable(image);
            addView(imageView);

            imageView.setLayoutParams(new LayoutParams(wQuinta, wQuinta));
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
                    resetClickableHand();
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

    private class AvaView extends FrameLayout {

        private Drawable normal = getResources().getDrawable(R.drawable.normal);
        private Drawable active = getResources().getDrawable(R.drawable.active);
        private Drawable highlighted = getResources().getDrawable(R.drawable.highlighted);
        private Drawable inactive = getResources().getDrawable(R.drawable.inactive);
        private ImageView imageView = new ImageView(GameActivity.this);

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
            imageView.setLayoutParams(new LayoutParams(wQuinta, wQuinta));
            setClickable(false);
        }

        @Override
        public void setClickable(boolean clickable) {
            super.setClickable(clickable);
            imageView.setClickable(clickable);
        }

        public void setNormal() {
            imageView.setImageDrawable(normal);
        }

        public void setActive() {
            imageView.setImageDrawable(active);
        }

        public void setHighlight() {
            imageView.setImageDrawable(highlighted);
        }

        public void setInactive() {
            imageView.setImageDrawable(inactive);
        }
    }

    public void onEventMainThread(ServerMessages.GameState gameState) {
        if (gameState.getCurrent().equals(ClientData.id)) {
            myAva.setActive();
        } else {
            playersAvas.get(gameState.getCurrent()).setActive();
        }

        for (String id : ClientData.playersNames.keySet()) {
            if (gameState.getActivePlayersId().contains(id)) {
                if (ClientData.id.equals(id)) {
                    myAva.setNormal();
                } else {
                    playersAvas.get(id).setNormal();
                }
            } else {
                if (ClientData.id.equals(id)) {
                    myAva.setInactive();
                } else {
                    playersAvas.get(id).setInactive();
                }
            }
        }

        if (ClientData.id.equals(gameState.getCurrent())) {
            myAva.setActive();
        } else {
            playersAvas.get(gameState.getCurrent()).setActive();
        }
    }

    public void onEventMainThread(ServerMessages.YourTurn yourTurn) {
        move = new Game.Move();
        move.objectId = ClientData.id;

        Game.Card card = yourTurn.getCard();
        CardView cardView = new CardView(card);
        handView.addView(cardView);

        if (handCardViews.containsKey(card)) {
            handCardViews.get(card).add(cardView);
        } else {
            List<CardView> list = new LinkedList<>();
            list.add(cardView);
            handCardViews.put(card, list);
        }

        state = State.CARD_SELECT;
    }

    public void onEventMainThread(ServerMessages.GameStart gameStart) {
        LayoutInflater layoutInflater = getLayoutInflater();
        ViewGroup gamePlayersRow = (ViewGroup) findViewById(R.id.game_players_row);

        Set<String> ids = new TreeSet<>(ClientData.playersNames.keySet());
        ids.remove(ClientData.id);

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

            gamePlayersRow.addView(playerView);
        }

        ((ViewGroup) findViewById(R.id.game_my_ava))
                .addView(myAva = new AvaView(ClientData.id));

        ((TextView) findViewById(R.id.game_my_name))
                .setText(ClientData.name);

        (handView = ((ViewGroup) findViewById(R.id.game_my_hand)))
                .addView(new CardView((Game.Card) ClientData.hand.toArray()[0]));
    }

    public void onEventMainThread(ServerMessages.InvalidMove event) {
        resetAvas();
        infoTextView.setText(getString(R.string.game_invalid_move_message));
    }

    public void onEventMainThread(ServerMessages.Correct event) {
        resetAvas();
        handView.removeView(handCardViews.get(move.card).get(0));
    }

    private void resetAvas() {
        myAva.setNormal();
        for (AvaView ava : playersAvas.values()) {
            ava.setNormal();
        }
    }

    private void resetClickableAvas() {
        myAva.setClickable(false);
        for (AvaView ava : playersAvas.values()) {
            ava.setClickable(false);
        }
    }

    private void resetClickableHand() {
        for (List<CardView> list : handCardViews.values()) {
            for (CardView cardView : list) {
                cardView.setClickable(false);
            }
        }
    }
}
