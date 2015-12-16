package com.the7winds.verbumSecretum.client.activities;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.the7winds.verbumSecretum.R;
import com.the7winds.verbumSecretum.client.activities.menuFragments.Login;
import com.the7winds.verbumSecretum.client.activities.menuFragments.Menu;
import com.the7winds.verbumSecretum.client.activities.menuFragments.Rules;
import com.the7winds.verbumSecretum.client.activities.menuFragments.Statistics;
import com.the7winds.verbumSecretum.client.other.ClientUtils;


public class MenuActivity extends Activity
                            implements Menu.MenuListener,
                                       Login.LoginListener,
                                       Statistics.StatisticsListener,
                                       Rules.RulesListener {

    private FragmentManager fragmentManager = getFragmentManager();
    private Menu menu = new Menu();
    private Login login = new Login();
    private Statistics statistics = new Statistics();
    private Rules rules = new Rules();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ClientUtils.DB.openDB(this);

        fragmentManager.beginTransaction()
                .add(R.id.main, login)
                .show(login)
                .commit();
    }

    @Override
    public void onClickStatistics(View view) {
        fragmentManager.beginTransaction()
                .replace(R.id.main, statistics)
                .show(statistics)
                .commit();
    }

    @Override
    public void onClickStart(View view) {
        startActivity(new Intent().setClass(this, RoomActivity.class));
    }

    @Override
    public void onClickChoose(View view) {
        fragmentManager.beginTransaction()
                .replace(R.id.main, login)
                .show(login)
                .commit();
    }

    @Override
    public void onClickConfirm(View view) {
        EditText editText = (EditText) findViewById(R.id.login_edit_text);
        String newLogin = editText.getText().toString();

        ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        if (ClientUtils.validateLogin(newLogin)) {
            ClientUtils.DB.addNewPlayerDB(newLogin);
            ClientUtils.authoriseAs(newLogin);

            fragmentManager.beginTransaction()
                    .replace(R.id.main, menu)
                    .show(menu)
                    .commit();
        } else {
            Toast.makeText(this, getString(R.string.login_incorrect_login), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClickChooseExistedPlayer(View view) {
        ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        ClientUtils.authoriseAs(login.getNameByView(view));
        fragmentManager.beginTransaction()
                .replace(R.id.main, menu)
                .show(menu)
                .commit();
    }

    @Override
    public void onClickDeleteExistedPlayer(View view) {
        String name = login.getNameByView(view);
        ClientUtils.DB.deletePlayer(name);
        login.updateExistedPlayersList();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onClickBack(View view) {
        fragmentManager.beginTransaction()
                .replace(R.id.main, menu)
                .show(menu)
                .commit();
    }

    public void onClickRules(View view) {
        fragmentManager.beginTransaction()
                .replace(R.id.main, rules)
                .show(rules)
                .commit();
    }

    @Override
    public void onClickRulesBack(View view) {
        fragmentManager.beginTransaction()
                .replace(R.id.main, menu)
                .show(menu)
                .commit();
    }
}
