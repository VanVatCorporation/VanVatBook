package com.vanvatcorporation.vanvatsach.activities;

import android.os.Bundle;

import com.vanvatcorporation.vanvatsach.R;
import com.vanvatcorporation.vanvatsach.fragments.SettingsFragment;
import com.vanvatcorporation.vanvatsach.impl.AppCompatActivityImpl;

public class SettingsActivity extends AppCompatActivityImpl {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_settings);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings_container, new SettingsFragment())
                    .commit();
        }
    }
}
