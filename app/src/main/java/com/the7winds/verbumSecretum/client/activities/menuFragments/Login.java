package com.the7winds.verbumSecretum.client.activities.menuFragments;

import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.the7winds.verbumSecretum.R;
import com.the7winds.verbumSecretum.client.other.ClientUtils;

import java.util.List;

/**
 * Created by the7winds on 20.10.15.
 */
public class Login extends Fragment {

    private static final String EXISTED_PLAYER_TAG = "existed_player_text";

    private TableLayout existedPlayersList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View loginLayout = inflater.inflate(R.layout.login, container, false);

        existedPlayersList = (TableLayout) loginLayout.findViewById(R.id.login_list_view);

        updateExistedPlayersList();

        return loginLayout;
    }

    public void updateExistedPlayersList() {
        List<ClientUtils.Player> players = ClientUtils.DB.getAllPlayers();
        existedPlayersList.removeAllViews();
        for (ClientUtils.Player player : players) {
            addPlayerToTable(player);
        }
    }

    private void addPlayerToTable(ClientUtils.Player player) {
        ViewGroup existedPlayerView = (ViewGroup) View.inflate(getActivity(), R.layout.login_existed_player, null);
        TextView text = (TextView) existedPlayerView.findViewWithTag(EXISTED_PLAYER_TAG);
        text.setText(player.name);
        existedPlayersList.addView(existedPlayerView);
    }

    public interface LoginListener {
        void onClickConfirm(View view);
        void onClickChooseExistedPlayer(View view);
        void onClickDeleteExistedPlayer(View view);
    }

    public String getNameByView(View view) {
        TableRow row = (TableRow) view.getParent();
        TextView text = (TextView) row.findViewWithTag(EXISTED_PLAYER_TAG);
        return text.getText().toString();
    }
}
