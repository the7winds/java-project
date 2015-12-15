package com.the7winds.verbumSecretum.client.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.the7winds.verbumSecretum.R;
import com.the7winds.verbumSecretum.client.network.ClientNetworkService;
import com.the7winds.verbumSecretum.client.network.PlayerMessages;
import com.the7winds.verbumSecretum.client.other.ClientUtils;
import com.the7winds.verbumSecretum.client.other.Events;
import com.the7winds.verbumSecretum.server.network.Server;
import com.the7winds.verbumSecretum.server.network.ServerMessages;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import de.greenrobot.event.EventBus;

public class RoomActivity extends Activity {

    private Collection<TextView> playersNames = new LinkedList<>();
    private ToggleButton readyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_room);

        ViewGroup roomView = (ViewGroup) findViewById(R.id.room_layout);

        playersNames.add((TextView) roomView.findViewById(R.id.room_player_1));
        playersNames.add((TextView) roomView.findViewById(R.id.room_player_2));
        playersNames.add((TextView) roomView.findViewById(R.id.room_player_3));
        playersNames.add((TextView) roomView.findViewById(R.id.room_player_4));

        readyButton = (ToggleButton) roomView.findViewById(R.id.ready);
        readyButton.setClickable(false);

        EventBus.getDefault().register(this);

        // setting up view
        for (TextView playerName : playersNames) {
            playerName.setText(R.string.room_empty_name);
        }

        // starting server
        if (ClientUtils.amIHotspot(this)) {
            startService(new Intent(this, Server.class));
        }

        // getting client service
        startService(new Intent(this, ClientNetworkService.class));
    }

    @Override
    protected void onPause() {
        super.onPause();

        EventBus.getDefault().unregister(this);

        if (!ClientUtils.Data.gameActivityStarted.get()) {
            EventBus.getDefault().post(new Events.SendToServerEvent(new PlayerMessages.Leave()));
            EventBus.getDefault().post(new Events.StopClientService());
        }
    }

    // subscribing

    public void onEventMainThread(ServerMessages.WaitingPlayersStatus event) {
        Map<String, String> playersNames = event.getPlayersNames();

        Iterator<TextView> iterator = this.playersNames.iterator();

        for (String id : playersNames.keySet()) {
            TextView playerName = iterator.next();
            playerName.setText(playersNames.get(id));
        }

        while (iterator.hasNext()) {
            TextView playerName = iterator.next();
            playerName.setText(R.string.room_empty_name);
        }

        readyButton.setChecked(playersNames.containsKey(ClientUtils.Data.id));
        readyButton.setClickable(true);
    }

    public void onEventMainThread(ServerMessages.Connected event) {
        readyButton.setClickable(true);
        Toast.makeText(this, R.string.room_connected, Toast.LENGTH_SHORT).show();
    }

    public void onEventMainThread(ServerMessages.GameStarting event) {
        startActivity(new Intent().setClass(this, GameActivity.class));
        ClientUtils.Data.gameActivityStarted.set(true);
        finish();
    }

    public void onEventMainThread(ServerMessages.Disconnected event) {
        Toast.makeText(this, R.string.game_disconnected_message, Toast.LENGTH_SHORT).show();
        finish();
    }

    public void onEventMainThread(Events.ClientServiceError event) {
        Toast.makeText(this, R.string.room_common_error_message, Toast.LENGTH_SHORT).show();
        finish();
    }

    public void onEventMainThread(Events.ServerNotFoundError event) {
        Toast.makeText(this, R.string.room_server_not_found_error_message, Toast.LENGTH_SHORT).show();
        finish();
    }

    // posting

    public void onClickReady(View view) {
        readyButton.setClickable(false);
        if (readyButton.isChecked()) {
            EventBus.getDefault()
                    .post(new Events.SendToServerEvent(
                            new PlayerMessages.Ready(ClientUtils.Data.playerStatisticsData.name)));
        } else {
            EventBus.getDefault()
                    .post(new Events.SendToServerEvent(
                            new PlayerMessages.NotReady()));
        }
    }
}