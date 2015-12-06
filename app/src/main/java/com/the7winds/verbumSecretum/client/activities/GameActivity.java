package com.the7winds.verbumSecretum.client.activities;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
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
    private Game.Move move;

    private ViewGroup handView;
    private Map<String, AvaView> playersAvas = new Hashtable<>();

    private FrameLayout frameLayout;
    private TableLayout selector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        EventBus.getDefault().register(this);
        initSelector();
        frameLayout  = (FrameLayout) findViewById(R.id.game_frame);
    }

    private void initSelector() {
        LayoutInflater inflater = getLayoutInflater();
        selector = (TableLayout) inflater.inflate(R.layout.choose_smth, null);
        TableRow tableRow = (TableRow) selector.findViewWithTag(R.string.game_choose_row);

        for (int i = 1; i < Game.Card.values().length; i++) {
            tableRow.addView(new CardView(Game.Card.values()[i]));
        }
    }

    private class CardView extends ImageView {

        private Game.Card card;
        private Drawable image;

        public CardView(final Game.Card card) {
            super(GameActivity.this);
            this.card = card;
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

            setImageDrawable(image);

            setOnClickListener(new OnClickHandler());
        }

        private class OnClickHandler implements OnClickListener {
            @Override
            public void onClick(View v) {

                if (state == State.CARD_SELECT) {
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
                    move.card = card;
                    frameLayout.removeView(selector);
                    EventBus.getDefault().post(new Events.SendToServerEvent(new PlayerMessages.Move(move)));
                    state = State.WAITING_UPD;
                }
            }
        }
    }

    private class AvaView extends ImageView {

        private Drawable normal = getResources().getDrawable(R.drawable.normal);
        private Drawable active = getResources().getDrawable(R.drawable.active);
        private Drawable highlighted = getResources().getDrawable(R.drawable.highlighted);
        private Drawable inactive = getResources().getDrawable(R.drawable.inactive);

        public AvaView(final String id) {
            super(GameActivity.this);
            setNormal();

            setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    setClickable(false);
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
        }

        public void setNormal() {
            this.setImageDrawable(normal);
        }

        public void setActive() {
            this.setImageDrawable(active);
        }

        public void setHighlight() {
            this.setImageDrawable(highlighted);
        }

        public void setInactive() {
            this.setImageDrawable(inactive);
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

        handView.addView(new CardView(yourTurn.getCard()));

        state = State.CARD_SELECT;
    }

    public void onEventMainThread(ServerMessages.GameStart startGame) {
        LayoutInflater layoutInflater = getLayoutInflater();
        ViewGroup gamePlayersRow = (ViewGroup) findViewById(R.id.game_players_row);

        Set<String> ids = new TreeSet<>(ClientData.playersNames.keySet());
        ids.remove(ClientData.id);

        for (String id : ids) {
            View playerView = layoutInflater.inflate(R.layout.game_player, null);

            AvaView avaView = new AvaView(id);
            playersAvas.put(id, avaView);
            ((ViewGroup) playerView
                    .findViewWithTag(R.string.game_ava_layout))
                    .addView(avaView);

            ((TextView) playerView
                    .findViewWithTag(R.string.game_player_name))
                    .setText(ClientData.playersNames.get(id));

            gamePlayersRow.addView(playerView);
        }

        ((ViewGroup) findViewById(R.id.game_my_ava))
                .addView(myAva = new AvaView(ClientData.id));

        ((TextView) findViewById(R.id.game_my_name))
                .setText(ClientData.playersNames.get(ClientData.id));

        (handView = ((ViewGroup) findViewById(R.id.game_my_hand)))
                .addView(new CardView((Game.Card) ClientData.hand.toArray()[0]));
    }
}
