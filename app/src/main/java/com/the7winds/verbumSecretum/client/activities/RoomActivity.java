package com.the7winds.verbumSecretum.client.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.the7winds.verbumSecretum.R;
import com.the7winds.verbumSecretum.client.network.ClientNetworkService;
import com.the7winds.verbumSecretum.client.network.PlayerMessages;
import com.the7winds.verbumSecretum.client.other.ClientData;
import com.the7winds.verbumSecretum.client.other.Events;
import com.the7winds.verbumSecretum.server.game.Player;
import com.the7winds.verbumSecretum.server.network.Server;
import com.the7winds.verbumSecretum.server.network.ServerMessages;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Hashtable;
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

        ClientData.readyState = ClientData.ReadyState.NOT_READY;

        setContentView(R.layout.activity_room);

        ViewGroup roomView = (ViewGroup) findViewById(R.id.room_layout);

        playersNames.add((TextView) roomView.findViewById(R.id.room_player_1));
        playersNames.add((TextView) roomView.findViewById(R.id.room_player_2));
        playersNames.add((TextView) roomView.findViewById(R.id.room_player_3));
        playersNames.add((TextView) roomView.findViewById(R.id.room_player_4));

        readyButton = (ToggleButton) roomView.findViewById(R.id.ready);
        readyButton.setClickable(false);

        //
        EventBus.getDefault().register(this);

        // setting up view
        EventBus.getDefault().post(new ServerMessages.WaitingPlayersStatus(new Hashtable<String, Player>()));

        // starting server
        if (amIHotspot()) {
            startService(new Intent(this, Server.class));
        }

        // getting client service
        startService(new Intent(this, ClientNetworkService.class));
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (ClientData.readyState == ClientData.ReadyState.READY) {
            EventBus.getDefault().post(new Events.SendToServerEvent(new PlayerMessages.Leave()));
        }

        if (amIHotspot()) {
            EventBus.getDefault().post(new Events.StopServer());
        }

        EventBus.getDefault().post(new Events.StopClientService());

        finish();
    }

    private boolean amIHotspot() {
        try {
            WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
            Method method = wifiManager.getClass().getDeclaredMethod("getWifiApState");
            method.setAccessible(true);

            int actualState = (Integer) method.invoke(wifiManager, (Object[]) null);
            Field fField = wifiManager.getClass().getField("WIFI_AP_STATE_ENABLED");
            int hotspotEnabled = (Integer) fField.get(fField);

            if (actualState == hotspotEnabled) {
                return true;
            }

        } catch (InvocationTargetException | NoSuchMethodException
                | IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }

        return false;
    }

    // subscribing

    public void onEventMainThread(ServerMessages.WaitingPlayersStatus waitingPlayersStatus) {
        Map<String, String> playersNames = waitingPlayersStatus.getPlayersNames();

        Iterator<TextView> iterator = this.playersNames.iterator();

        for (String id : playersNames.keySet()) {
            TextView playerName = iterator.next();
            playerName.setText(playersNames.get(id));
        }

        while (iterator.hasNext()) {
            TextView playerName = iterator.next();
            playerName.setText(R.string.empty_name);
        }

        readyButton.setChecked(playersNames.containsKey(ClientData.id));
        readyButton.setClickable(true);
    }

    public void onEventMainThread(ServerMessages.Connected connected) {
        readyButton.setClickable(true);
        Toast.makeText(this, R.string.room_connected, Toast.LENGTH_SHORT).show();
    }

    public void onEventMainThread(ServerMessages.GameStarting gameStarting) {
        startActivity(new Intent().setClass(this, GameActivity.class));
        finish();
    }

    public void onEventMainThread(ServerMessages.Disconnected disconnected) {
        finish();
    }

    public void onEventMainThread(Events.ClientServiceError clientServiceError) {
        Toast.makeText(this, R.string.common_error_message, Toast.LENGTH_SHORT).show();
        EventBus.getDefault().unregister(this);

        if (amIHotspot()) {
            EventBus.getDefault().post(new Events.StopServer());
        }

        finish();
    }

    public void onEventMainThread(Events.ServerNotFoundError serverNotFoundError) {
        Toast.makeText(this, R.string.server_not_found_error_message, Toast.LENGTH_SHORT).show();
        EventBus.getDefault().unregister(this);

        if (amIHotspot()) {
            EventBus.getDefault().post(new Events.StopServer());
        }

        finish();
    }

    // posting

    public void onClickReady(View view) {
        readyButton.setClickable(false);
        if (readyButton.isChecked()) {
            EventBus.getDefault().post(new Events.SendToServerEvent(new PlayerMessages.Ready(ClientData.name)));
        } else {
            EventBus.getDefault().post(new Events.SendToServerEvent(new PlayerMessages.NotReady()));
        }
    }
}