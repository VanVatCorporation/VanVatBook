package com.vanvatcorporation.vanvatsach.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.vanvatcorporation.vanvatsach.R;
import com.vanvatcorporation.vanvatsach.constants.DynamicConstants;
import com.vanvatcorporation.vanvatsach.constants.MethodConstants;
import com.vanvatcorporation.vanvatsach.helper.AppSourceChecker;
import com.vanvatcorporation.vanvatsach.helper.EnumHelper;
import com.vanvatcorporation.vanvatsach.impl.AppCompatActivityImpl;

public class WarningActivity extends AppCompatActivityImpl {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MethodConstants.fullScreenNoTitleTransition(this);

        setContentView(R.layout.layout_warning);
        boolean accountValidate;

        TextView warningDescription = findViewById(R.id.warningDescription);
        if(getIntent() != null)
        {
            if(getIntent().getStringExtra("WarningType") != null)
            {
                if(EnumHelper.stringToEnum(getIntent().getStringExtra("WarningType"), AppSourceChecker.InstallerMethod.class) == AppSourceChecker.InstallerMethod.UNKNOWN)
                {
                    warningDescription.setText(R.string.warning_unknown_installation_method);
                }
            }
            accountValidate = getIntent().getBooleanExtra("AccountValidate", false);
        } else {
            accountValidate = false;
        }


        Button warningButton = findViewById(R.id.warningButton);
        warningButton.setOnClickListener(v -> {
            warningButton.setEnabled(false);
            DynamicConstants.IS_PROCEED_UNKNOWN_OTHER_INSTALLATION = true;
            Intent intent = new Intent(this, accountValidate ? MainActivity.class : LoginActivity.class);
            startActivity(intent);
            finish();
        });

    }
}
