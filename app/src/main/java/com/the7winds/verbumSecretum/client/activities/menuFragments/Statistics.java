package com.the7winds.verbumSecretum.client.activities.menuFragments;

import android.app.Fragment;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.the7winds.verbumSecretum.R;
import com.the7winds.verbumSecretum.client.other.ClientUtils;
import com.the7winds.verbumSecretum.databinding.MenuStatisticsBinding;

/**
 * Created by the7winds on 20.10.15.
 */
public class Statistics extends Fragment {
    private View statisticsLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        statisticsLayout = inflater.inflate(R.layout.menu_statistics, container, false);
        updateLayout();
        return statisticsLayout;
    }

    public void updateLayout() {
        MenuStatisticsBinding binding = MenuStatisticsBinding.bind(statisticsLayout);
        binding.setPlayerStatisticsData(ClientUtils.Data.playerStatisticsData);
    }

    public interface StatisticsListener {
        void onClickBack(View view);
    }
}
