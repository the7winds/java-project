package com.the7winds.verbumSecretum.client.activities.menuFragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.the7winds.verbumSecretum.R;

/**
 * Created by the7winds on 20.10.15.
 */
public class Menu extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.menu_menu, container, false);
    }

    public interface MenuListener {
        void onClickStatistics(View view);
        void onClickStart(View view);
        void onClickChoose(View view);
    }
}
