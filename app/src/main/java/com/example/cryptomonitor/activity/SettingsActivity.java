package com.example.cryptomonitor.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.example.cryptomonitor.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean mTheme = mPreferences.getBoolean("theme", false);
        if (mTheme.equals(true)){
            setTheme(R.style.AppThemeDarkPurple);
        }
        else{
            setTheme(R.style.AppThemeDark);
        }
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        String mLang = mPreferences.getString("lang", "default");
        assert mLang != null;
        if (mLang.equals("default")) {
            mLang = getResources().getConfiguration().locale.getCountry();
        }
        Locale mLocale = new Locale(mLang);
        Locale.setDefault(mLocale);
        Configuration configuration = new Configuration();
        configuration.locale = mLocale;
        getBaseContext().getResources().updateConfiguration(configuration, null);
        FloatingActionButton button =  findViewById(R.id.buttonApply);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, SettingsActivity.class);
                SettingsActivity.this.startActivity(intent);
                SettingsActivity.this.finishAffinity();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
        SettingsActivity.this.startActivity(intent);
        SettingsActivity.this.finishAffinity();
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            if(rootKey.equals("usd")){
                Toast toast = Toast.makeText(SettingsActivity.this,
                        "Пора покормить кота!", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

}