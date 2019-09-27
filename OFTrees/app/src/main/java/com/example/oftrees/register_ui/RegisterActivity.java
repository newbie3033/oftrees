package com.example.oftrees.register_ui;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.oftrees.R;
import com.example.oftrees.ui.login.LoginActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import static com.example.oftrees.R.string.invalid_username;

public class RegisterActivity extends AppCompatActivity {
    private EditText mUsername;
    private EditText mPassword;
    private EditText mRepassword;
    private EditText mMobile;
    private EditText mEmail;
    private LinearLayout playout;
    private OkHttpClient okHttpClient=new OkHttpClient();

    private JSONObject _Response;
    private JSONObject _ResponseData;
    private boolean _ResponseSuccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("RegisterActivity", "onCreate: ");
        setContentView(R.layout.activity_register);
        Button button_1 = (Button) findViewById(R.id.relogin);
        button_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
//                startActivity(intent);
                finish();
            }
        });
        Button button_2 = (Button) findViewById(R.id.register);
        playout=findViewById(R.id.register_layoutPBar);
        button_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(register_check()){
                    register();
                    playout.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public boolean register_check() {
        mUsername = (EditText) findViewById(R.id.username);
        mPassword = (EditText) findViewById(R.id.password);
        mRepassword = (EditText) findViewById(R.id.repassword);
        mEmail=(EditText)findViewById(R.id.email);
        mMobile=(EditText)findViewById(R.id.pnumber);//注册按钮的监听事件
        if (isUserNameAndPwdValid()) {
            String userName = mUsername.getText().toString().trim();
            String userPwd = mPassword.getText().toString().trim();
            String userPwdCheck = mRepassword.getText().toString().trim();
            //检查用户名是否为空
            //检查密码长度
            //检查两次输入的密码是否一样
            if (userPwd.equals(userPwdCheck) == false) {
                Toast.makeText(RegisterActivity.this,getString(R.string.rp_not_same_p),Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        }
        return false;
    }

    public boolean isUserNameAndPwdValid() {
        if (mUsername.getText().toString().trim().equals("")) {
            Toast.makeText(RegisterActivity.this, getString(R.string.invalid_username), Toast.LENGTH_SHORT).show();
            return false;
        } else if (mPassword.getText().toString().trim().equals("")) {
            Toast.makeText(RegisterActivity.this, getString(R.string.invalid_password), Toast.LENGTH_SHORT).show();
            return false;
        }else if(mRepassword.getText().toString().trim().equals("")) {
            Toast.makeText(RegisterActivity.this, getString(R.string.pwd_check_empty), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void register(){

//        JSONArray array =new JSONArray();
//        JSONObject object =new JSONObject();
//        JSONObject object1 =new JSONObject();
//        JSONObject obj= new JSONObject();
//        try {
//            object.put("item1","value1");
//            object.put("age",12);
//            object.put("name","tom");
//            object1.put("item2","value2");
//            object1.put("age",12232);
//            object1.put("name","tom");
//            array.put(object);
//            array.put(object1);
//            obj.put("name",array);
//            System.out.println(obj.toString());
//        }catch (Exception e){
//            e.printStackTrace();
//        }

        String params="{\"username\":\""+mUsername.getText().toString()+
                "\",\"password\":\"" +mPassword.getText().toString()+
                "\",\"email\":\"" +mEmail.getText().toString()+
                "\",\"mobile\":\""+mMobile.getText().toString()+"\"}";
        Log.d("RegisterActivity", "register: "+params);

        MediaType json = MediaType.parse("application/json; charset=utf-8");//数据类型为json格式，
        String jsonStr = "{\"type\":\"USER_REGISTER\",\"params\":"+params+"}";//json数据
        Log.d("RegisterActivity", "register: "+jsonStr);

        RequestBody body = RequestBody.create(json,jsonStr);
        Request.Builder request = new Request.Builder()
                .url("http://39.106.89.21/Server/MyMgisServer.php")
                .post(body);
        execute(request);


    }


    private void execute(Request.Builder builder){
        Call call=okHttpClient.newCall(builder.build());
        call.enqueue(callback);
    }

    private Callback callback=new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            Log.d("RegisterActivity", "onFailure: ");
            e.printStackTrace();
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            //从response获得服务器返回的数据，转换成字符串处理
            String str=new String(response.body().bytes(),"utf-8");
            Log.d("RegisterActivity", "onResponse: "+str);
            try {
                _Response=new JSONObject(str);
                _ResponseSuccess=_Response.getBoolean("success");
//                _ResponseData=_Response.getJSONObject("data");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //通过Handler更新UI
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
                case 0:
                    //注册失败
                    playout.setVisibility(View.GONE);
                    AlertDialog.Builder dialog=new AlertDialog.Builder(RegisterActivity.this)
                            .setTitle("提示")
                            .setMessage("注册失败");
                    break;
                case 1:
                    //注册成功
                    playout.setVisibility(View.GONE);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(RegisterActivity.this,"注册成功！",Toast.LENGTH_LONG).show();
                        }
                    });
                    finish();
                    break;
            }
        }
    };

}
