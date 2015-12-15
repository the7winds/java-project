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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View statisticsLayout = inflater.inflate(R.layout.menu_statistics, container, false);

        allGamesText = (TextView) statisticsLayout.findViewWithTag(getString(R.string.statistics_all_games_tag));
        wonGamesText = (TextView) statisticsLayout.findViewWithTag(getString(R.string.statistics_won_games_tag));

        updateLayout();

        return statisticsLayout;
    }

    public void updateLayout() {
        if (ClientUtils.isAuthorised()
                && allGamesText != null
                && wonGamesText != null) {
            allGamesText.setText(Integer.valueOf(ClientUtils.Data.playerStatisticsData.all).toString());
            wonGamesText.setText(Integer.valueOf(ClientUtils.Data.playerStatisticsData.won).toString());
        }
    }

    public interface StatisticsListener {
        void onClickBack(View view);
    }
}
