package com.the7winds.verbumSecretum.client.activities.menuFragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.the7winds.verbumSecretum.R;
import com.the7winds.verbumSecretum.client.other.ClientUtils;

/**
 * Created by the7winds on 20.10.15.
 */
public class Statistics extends Fragment {
    private TextView allGamesText;

    private TextView wonGamesText;

    private static final String ALL_GAMES_TEXT_TAG = "all_games_tag";

    private static final String WON_GAMES_TEXT_TAG = "won_games_tag";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View statisticsLayout = inflater.inflate(R.layout.menu_statistics, container, false);

        allGamesText = (TextView) statisticsLayout.findViewWithTag(ALL_GAMES_TEXT_TAG);
        wonGamesText = (TextView) statisticsLayout.findViewWithTag(WON_GAMES_TEXT_TAG);

        updateLayout();

        return statisticsLayout;
    }

    public void updateLayout() {
        if (ClientUtils.isAuthorised()
                && allGamesText != null
                && wonGamesText != null) {
            allGamesText.setText(Integer.valueOf(ClientUtils.player.all).toString());
            wonGamesText.setText(Integer.valueOf(ClientUtils.player.won).toString());
        }
    }

    public interface StatisticsListener {
        void onClickBack(View view);
    }
}
