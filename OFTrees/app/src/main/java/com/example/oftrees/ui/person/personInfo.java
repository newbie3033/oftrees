package com.example.oftrees.ui.person;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.example.oftrees.*;

public class personInfo extends AppCompatActivity {

    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_info);

        username=getIntent().getStringExtra("username");
        TextView textView=findViewById(R.id.temp1);
        textView.setText(username);

        //连接服务器获取用户信息

    }
}
