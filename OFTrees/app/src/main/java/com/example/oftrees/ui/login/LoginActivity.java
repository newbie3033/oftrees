package com.example.oftrees.ui.login;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;


import com.example.oftrees.MainActivity;
import com.example.oftrees.R;
import com.example.oftrees.register_ui.RegisterActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;
    private EditText username;
    private EditText password;
    private OkHttpClient client=new OkHttpClient();

    private static final String TAG = "LoginActivity";

    private JSONObject _ResponseData;
    private JSONObject _Response;
    private boolean _ResponseSuccess;

    private LinearLayout playout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("LoginActivity", "onCreate: ");
        setContentView(R.layout.activity_login);

        playout=findViewById(R.id.login_layoutPBar);

        Button button_1=(Button)findViewById(R.id.register);
        button_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        //游客身份登录
        Button button2=(Button)findViewById(R.id.guestlogin);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(LoginActivity.this,MainActivity.class);
                intent.putExtra("authority",false);
                startActivity(intent);
                finish();
            }
        });

        //账号密码登录

        Button button3=(Button)findViewById(R.id.login);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
                playout.setVisibility(View.VISIBLE);

            }
        });

        username=findViewById(R.id.username);
        password=findViewById(R.id.password);

    }

    private void login(){
        //创建json字符串
//        JSONArray array =new JSONArray();
        JSONObject object =new JSONObject();
        JSONObject obj=new JSONObject();
        try {
            obj.put("type","USER_LOGIN");
            object.put("username",username.getText());
            object.put("password",password.getText());
            obj.put("params",object);
//            array.put(object);
            Log.d(TAG, "login: "+obj.toString());
        }catch (Exception e){
            e.printStackTrace();
        }

        String jsonStr=obj.toString();
        RequestBody body=RequestBody.create(MediaType.parse("application/json;charset=ust-8"),jsonStr);
        Request.Builder request=new Request.Builder()
                .url("http://39.106.89.21/Server/MyMgisServer.php")
                .post(body);
        execute(request);
        

    }

    private void execute(Request.Builder builder){
        Call call=client.newCall(builder.build());
        call.enqueue(callback);
    }

    private Callback callback=new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            Log.d(TAG, "onFailure: ");
            e.printStackTrace();
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            String str=new String(response.body().bytes(),"utf-8");
            Log.d(TAG, "onResponse: "+str);
            try {
                _Response=new JSONObject(str);
//                _ResponseData=_Response.getJSONObject("data");
                _ResponseSuccess=_Response.getBoolean("success");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Message message=new Message();
            if(_ResponseSuccess){
                message.what=1;
                handler.sendMessage(message);
            }else {
                message.what=0;
                handler.sendMessage(message);
            }

        }
    };

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                //登录失败
                case 0:
                    playout.setVisibility(View.GONE);
                    //提示
                    AlertDialog.Builder builder=new AlertDialog.Builder(LoginActivity.this)
                            .setTitle("提示")
                            .setMessage("用户密码错误!!!")
                            .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //清空输入框密码
                                    password.setText("");
                                }
                            });
                    builder.show();
                    break;
                case 1:
                    //登录成功
                    playout.setVisibility(View.GONE);
                    Intent intent=new Intent(LoginActivity.this,MainActivity.class);
                    intent.putExtra("username",username.getText().toString());
                    intent.putExtra("authority",true);
                    startActivity(intent);
                    finish();
                    break;
            }
        }
    };

}
