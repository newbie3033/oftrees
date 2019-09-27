package com.example.oftrees.start_ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.oftrees.R;
import com.example.oftrees.ui.login.LoginActivity;

public class StartLoadingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("StartLoadingActivity", "onCreate: ");

        new Thread( new Runnable( ) {
            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 这里可以睡几秒钟，如果要放广告的话
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Intent intent = new Intent(StartLoadingActivity.this,LoginActivity.class);
                        startActivity(intent);
                        StartLoadingActivity.this.finish();
                    }
                });
            }
        } ).start();

    }
}
