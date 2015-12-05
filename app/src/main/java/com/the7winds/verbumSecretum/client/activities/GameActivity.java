package com.the7winds.verbumSecretum.client.activities;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.the7winds.verbumSecretum.R;
import com.the7winds.verbumSecretum.client.other.ClientData;
import com.the7winds.verbumSecretum.client.network.ClientNetworkService;
import com.the7winds.verbumSecretum.server.game.Game;

import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;

public class GameActivity extends Activity {

    private enum State {WAITING_UPD, CARD_SELECT, SUBJECT_SELECT, ROLE_SELECT, FINISH}
    private State state = State.WAITING_UPD;

    private ViewGroup gameLayout;
    private Map<String, AvaView> playersViews = new Hashtable<>();
    private ViewGroup handView;
    private Collection<CardView> hand = new LinkedList<>();

    private Game.Move move;
    private ClientNetworkService cnsService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        gameLayout = (ViewGroup) findViewById(R.id.game_layout);
        initGameLayout();
        //bindService(new Intent(this, ClientNetworkService.class), serviceConnection, BIND_AUTO_CREATE);
    }

    private void initGameLayout() {
        LayoutInflater layoutInflater = getLayoutInflater();
        ViewGroup gamePlayersRow = (ViewGroup) gameLayout.findViewById(R.id.game_players);

        for (String id : ClientData.playersNames.keySet()) {
            if (!id.equals(ClientData.id)) {
                View playerView = layoutInflater.inflate(R.layout.game_player, null);

                int width = getWindowManager().getDefaultDisplay().getWidth() / 3;
                playerView.setMinimumWidth(width);

                AvaView avaView = new AvaView(this);
                playersViews.put(id, avaView);
                ((ViewGroup) playerView.findViewWithTag("ava_layout")).addView(avaView);

                TextView name = (TextView) playerView.findViewWithTag("name");
                name.setText(ClientData.playersNames.get(id));

                gamePlayersRow.addView(playerView);
            }
        }

        ((ViewGroup) gameLayout.findViewWithTag("my_ava_layout")).addView(new AvaView(this));


        TextView name = (TextView) gameLayout.findViewWithTag("my_name");
        name.setText(ClientData.playersNames.get(ClientData.id));

        handView = (ViewGroup) gameLayout.findViewWithTag("hand");
        /* TODO for (Game.Card card : ClientData.hand) {
            handView.addView(new CardView(this, card));
        }*/
    }

    public void onYourTurn(Game.Card card) {
        state = State.CARD_SELECT;
        move = new Game.Move();
        move.objectId = ClientData.id;

        handView.addView(new CardView(this, card));
        hand.add(new CardView(this, card));

        for (CardView cardView : hand) {
            cardView.setClickable(true);
        }
    }

    private class CardView extends ImageView {

        private Game.Card card;
        private Drawable image;

        public CardView(Context context, final Game.Card card) {
            super(context);
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
            setClickable(false);

            setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (CardView cardView : hand) {
                        cardView.setClickable(false);
                    }

                    if (state.equals(State.CARD_SELECT)) {
                        move.card = card;

                        if (card.equals(Game.Card.GUARD_CARD)
                                || card.equals(Game.Card.PRIST_CARD)
                                || card.equals(Game.Card.LORD_CARD)
                                || card.equals(Game.Card.PRINCE_CARD)
                                || card.equals(Game.Card.KING_CARD)) {
                            for (String id : playersViews.keySet()) {
                                if (!(card.equals(Game.Card.PRINCE_CARD) && id.equals(ClientData.id))) {
                                    AvaView player = playersViews.get(id);
                                    player.setClickable(true);
                                    player.setHighlight();
                                }
                            }

                            state = State.SUBJECT_SELECT;
                        } else {
            // TODO                            cnsService.send(new PlayerMessages.Move(move));
                        }
                    } else if (state.equals(State.ROLE_SELECT)) {
                        // show Select Role
                    }
                }
            });
        }
    }


    private class AvaView extends ImageView {

        private Drawable normal = getResources().getDrawable(R.drawable.normal);
        private Drawable active = getResources().getDrawable(R.drawable.active);
        private Drawable activeHighlighted1 = getResources().getDrawable(R.drawable.highlighted);
        private Drawable inactive = getResources().getDrawable(R.drawable.inactive);

        public AvaView(Context context) {
            super(context);
            setNormal();
        }

        public void setNormal() {
            this.setImageDrawable(normal);
        }

        public void setActive() {
            this.setImageDrawable(active);
        }

        public void setHighlight() {
            this.setImageDrawable(activeHighlighted1);
        }

        public void setIncative() {
            this.setImageDrawable(inactive);
        }
    }


    public static class SelectRole extends DialogFragment {

        public SelectRole() {
            super();
        }

        public SelectRole newInstance() {

            Bundle args = new Bundle();

            SelectRole fragment = new SelectRole();
            fragment.setArguments(args);
            return fragment;
        }
    }
}
