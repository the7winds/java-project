package com.the7winds.verbumSecretum.client.activities.menuFragments;

import android.app.Fragment;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TextView;

import com.the7winds.verbumSecretum.R;
import com.the7winds.verbumSecretum.client.other.ClientUtils;
import com.the7winds.verbumSecretum.databinding.MenuLoginExistedPlayerBinding;

import java.util.List;

/**
 * Created by the7winds on 20.10.15.
 */
public class Login extends Fragment {

    private TableLayout existedPlayersList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View loginLayout = inflater.inflate(R.layout.menu_login, container, false);
        existedPlayersList = (TableLayout) loginLayout.findViewById(R.id.login_list_view);
        updateExistedPlayersList();
        return loginLayout;
    }

    public void updateExistedPlayersList() {
        List<ClientUtils.PlayerStatisticsData> playerStatisticsDatas = ClientUtils.DB.getAllPlayers();
        existedPlayersList.removeAllViews();
        for (ClientUtils.PlayerStatisticsData playerStatisticsData : playerStatisticsDatas) {
            addPlayerToTable(playerStatisticsData);
        }
    }

    private void addPlayerToTable(ClientUtils.PlayerStatisticsData playerStatisticsData) {
        MenuLoginExistedPlayerBinding binding =
                MenuLoginExistedPlayerBinding.inflate(getActivity().getLayoutInflater(), existedPlayersList, true);
        binding.setPlayerStatisticsData(playerStatisticsData);
    }

    public interface LoginListener {
        void onClickConfirm(View view);
        void onClickChooseExistedPlayer(View view);
        void onClickDeleteExistedPlayer(View view);
    }

    public String getNameByView(View view) {
        MenuLoginExistedPlayerBinding binding = DataBindingUtil.getBinding(view);
        return  binding.getPlayerStatisticsData().name;
    }
}
