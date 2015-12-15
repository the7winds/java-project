package com.the7winds.verbumSecretum.client.activities.menuFragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.the7winds.verbumSecretum.R;

/**
 * Created by the7winds on 15.12.15.
 */
public class Rules extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.menu_rules, container, false);
    }

    public interface RulesListener {
        void onClickRulesBack(View view);
    }
}
