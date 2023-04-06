package com.rohit.samples;

import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;

public class MainActivity extends AppCompatActivity {

    private AppUpdateManager appUpdateManager;
    private static final int RC_APP_UPDATE = 16007;
    private TextView tvVersionName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvVersionName = findViewById(R.id.tvVersionName);
        try {
            tvVersionName.setText("Hello Rohit..!\nApp version : ".concat(getPackageManager().getPackageInfo(getPackageName(),0).versionName));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        checkForUpdate();
    }

    private void checkForUpdate(){
        appUpdateManager = AppUpdateManagerFactory.create(this);
        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {

            String data = "\npackageName :" + appUpdateInfo.packageName() + ", " +

                    "availableVersionCode :" + appUpdateInfo.availableVersionCode() + ", " +

                    "updateAvailability :" + appUpdateInfo.updateAvailability() + ", " +

                    "installStatus :" + appUpdateInfo.installStatus()+ ", ";

            tvVersionName.setText(tvVersionName.getText().toString().concat(data));

            if(appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)){
                try {
                    appUpdateManager.startUpdateFlowForResult(appUpdateInfo,AppUpdateType.FLEXIBLE,
                            MainActivity.this,RC_APP_UPDATE);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }else if(appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)){
                try {
                    appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE,
                            MainActivity.this, RC_APP_UPDATE);

                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }

            }else{
                Toast.makeText(MainActivity.this,"No Update",Toast.LENGTH_LONG).show();
            }
        });

        appUpdateManager.registerListener(listener);
    }

    private final InstallStateUpdatedListener listener = installState -> {
        if(installState.installStatus() == InstallStatus.DOWNLOADED){
            showCompleteUpdate();
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == RC_APP_UPDATE && resultCode != RESULT_OK){
            Toast.makeText(MainActivity.this, "Cancel", Toast.LENGTH_SHORT).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showCompleteUpdate(){
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"New App is Ready!",Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("Install", view -> appUpdateManager.completeUpdate());
        snackbar.show();
    }

    @Override
    protected void onStop() {
        if(appUpdateManager!=null)appUpdateManager.unregisterListener(listener);
        super.onStop();
    }
}