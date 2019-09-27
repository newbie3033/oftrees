package com.example.oftrees.ui.home;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.*;
import okhttp3.RequestBody;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.model.LatLng;
import com.bumptech.glide.Glide;
import com.example.oftrees.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class InfoShowActivity extends AppCompatActivity {

    private static final String TAG = "InfoShowActivity";
    private static final int UPDATE_PROBAR_VISIBLE=0;
    private static final int UPDATE_PROBAR_GONE=1;

    private double positionLat;
    private double positionLng;

    private ImageView pic1,pic2,pic3;
    private ImageView pic_zoom1;
    private Dialog dialog;
    private  TextView editText1,editText2,editText3,editText4,editText5,editText6,editText7,editText8,
            editText9,editText10,editText11,editText12,editText13,editText14,editText15,editText16,
            editText17,editText18,editText19,editText20,editText21,editText22,editText23,editText24;
    private LinearLayout pblayout;

    private JSONObject _Response;
    private boolean _ResponseSuccess=false;
    private boolean client_over=false;
    private JSONObject _ResponseData;
    private String[] picPath;
    private int picNum=0;

    private OkHttpClient okHttpClient=new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_info);


        positionLat=getIntent().getDoubleExtra("positionLat",0);
        positionLng=getIntent().getDoubleExtra("positionLng",0);

        editText1=findViewById(R.id.di_address);
        editText2=findViewById(R.id.di_xaddress);
        editText3=findViewById(R.id.di_nuber);
        editText4=findViewById(R.id.di_branch);
        editText5=findViewById(R.id.di_sort);
        editText6=findViewById(R.id.di_cname);
        editText7=findViewById(R.id.di_alias);
        editText8=findViewById(R.id.di_latin_name);
        editText9=findViewById(R.id.di_feature);
        editText10=findViewById(R.id.di_type);
        editText11=findViewById(R.id.di_age);
        editText12=findViewById(R.id.di_height);
        editText13=findViewById(R.id.di_diameter);
        editText14=findViewById(R.id.di_average_crown);
        editText15=findViewById(R.id.di_se_crown);
        editText16=findViewById(R.id.di_sn_crown);
        editText17=findViewById(R.id.di_altitude);
        editText18=findViewById(R.id.di_slope_d);
        editText19=findViewById(R.id.di_slope);
        editText20=findViewById(R.id.di_soil_category);
        editText21=findViewById(R.id.di_soil_density);
        editText22=findViewById(R.id.di_growth_potential);
        editText23=findViewById(R.id.di_growth_environment);
        editText24=findViewById(R.id.di_histroy);

        pic1=findViewById(R.id.di_pic1);
        pic2=findViewById(R.id.di_pic2);
        pic3=findViewById(R.id.di_pic3);

        dialog=new Dialog(InfoShowActivity.this);
        dialog.setContentView(R.layout.pic_zoom);
        dialog.setCancelable(true);
        pic_zoom1=(ImageView)dialog.findViewById(R.id.pic_zoom1);

//        LayoutInflater factory = LayoutInflater.from(InfoShowActivity.this);
//        View layout = factory.inflate(R.layout.pic_zoom,null);
//        pic_zoom1=layout.findViewById(R.id.pic_zoom1);



        pblayout=findViewById(R.id.di_layoutPBar);

        //连接服务器获取详细信息
        _getInformations();
        //当服务器未响应成功时一直显示进度条
        while(!client_over)
        {
            pblayout.setVisibility(View.VISIBLE);
        }
        if(_ResponseSuccess==false)
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(InfoShowActivity.this,"该点没有树木",Toast.LENGTH_LONG).show();
                }
            });
            finish();
        }
        else {
            //显示结果
            _show();
            //连接服务器获取图片
            _getPic();
        }

        //显示大图
        pic1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pic_zoom1.setImageBitmap(((BitmapDrawable)pic1.getDrawable()).getBitmap());
                dialog.show();
            }
        });
        pic2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pic_zoom1.setImageBitmap(((BitmapDrawable)pic2.getDrawable()).getBitmap());
                dialog.show();
            }
        });
        pic3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pic_zoom1.setImageBitmap(((BitmapDrawable)pic3.getDrawable()).getBitmap());
                dialog.show();
            }
        });


        //绑定toolbar
        Toolbar toolbar_collect =  findViewById(R.id.di_toolbar);
        setSupportActionBar(toolbar_collect);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar_collect.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();//返回
            }
        });

    }

    protected void  _getInformations(){
        JSONObject obj= new JSONObject(),object=new JSONObject();
        try {
            object.put("positionLat",String.valueOf(positionLat));
            object.put("positionLng",String.valueOf(positionLng));
            obj.put("type","DATA_GETINFO");
            obj.put("params",object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String jsonStr=obj.toString();
        RequestBody body=RequestBody.create(MediaType.parse("application/json;charset=uts-8"),jsonStr);
        Request.Builder builder=new Request.Builder()
                .url("http://39.106.89.21/Server/MyMgisServer.php")
                .post(body);
        Call call=okHttpClient.newCall(builder.build());
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
                _ResponseData=_Response.getJSONObject("data");
                _ResponseSuccess=_Response.getBoolean("success");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            client_over=true;
        }
    };

    protected void _show(){
        try {
            editText1.setText(_ResponseData.getString("t_location"));
            editText2.setText(_ResponseData.getString("t_location_detail"));
            editText3.setText(_ResponseData.getString("t_id"));
            editText4.setText(_ResponseData.getString("t_family"));
            editText5.setText(_ResponseData.getString("t_category"));
            editText6.setText(_ResponseData.getString("t_chsname"));
            editText7.setText(_ResponseData.getString("t_alias"));
            editText8.setText(_ResponseData.getString("t_latinname"));
            editText9.setText(_ResponseData.getString("t_trait"));
            editText10.setText(_ResponseData.getString("t_type"));
            editText11.setText(_ResponseData.getString("t_age"));
            editText12.setText(_ResponseData.getString("t_rounds"));
            editText13.setText(_ResponseData.getString("t_crown_ave"));
            editText14.setText(_ResponseData.getString("t_crown_ew"));
            editText15.setText(_ResponseData.getString("t_crown_sn"));
            editText16.setText(_ResponseData.getString("t_elevation"));
            editText17.setText(_ResponseData.getString("t_aspect"));
            editText18.setText(_ResponseData.getString("t_gradient"));
            editText19.setText(_ResponseData.getString("t_slope"));
            editText20.setText(_ResponseData.getString("t_soil_name"));
            editText21.setText(_ResponseData.getString("t_soil_density"));
            editText22.setText(_ResponseData.getString("t_growth_potential"));
            editText23.setText(_ResponseData.getString("t_growth_environment"));
            editText24.setText(_ResponseData.getString("t_history_detail"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //获取图片
    protected void _getPic(){
        JSONObject obj=new JSONObject();
        JSONObject object=new JSONObject();
        try {
            obj.put("type","DATA_DOWNLOADFILE");
            object.put("id",editText3.getText().toString());
            obj.put("params",object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "_getPic: "+obj.toString());
        RequestBody body=RequestBody.create(MediaType.parse("application/json;charset:utf-8"),obj.toString());
        Request request1=new Request.Builder()
                .url("http://39.106.89.21/Server/MyMgisServer.php")
                .post(body)
                .build();
        Call call1=okHttpClient.newCall(request1);
        call1.enqueue(callback2);


    }
    //获取图片路径
    private Callback callback2=new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            e.printStackTrace();
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            String str=new String(response.body().bytes(),"utf-8");
            Log.d(TAG, "onResponse: "+str);
            try {
                _Response=new JSONObject(str);
                _ResponseSuccess=_Response.getBoolean("success");
                JSONArray jsonArray =_Response.getJSONArray("data");
                picPath=new String[jsonArray.length()];
                for(int i=0;i<jsonArray.length();i++)
                    picPath[i]=jsonArray.getString(i);
//                Log.d(TAG, "onResponse: "+jsonArray.toString());

            } catch (JSONException e) {
                e.printStackTrace();
            }

            if(picPath.length<1||picPath==null)
                return;

            Log.d(TAG, "onResponse: 11111"+picPath[0].substring(0,picPath[0].indexOf("_")));

            if(_ResponseSuccess){
                //显示进度
                pblayout.setVisibility(View.VISIBLE);
                for(int i=0;i<picPath.length;i++){
                    //发送获取图片请求
                    Request request2=new Request.Builder()
                            .get()
                            .url("http://39.106.89.21/Server/Uploads/"
                                    +picPath[i].substring(0,picPath[i].indexOf("_"))
                                    +"/"
                                    +picPath[i]+".jpg")
                            .build();
                    Call call2=okHttpClient.newCall(request2);
                    call2.enqueue(callback3);

                }

            }


        }
    };

    //获取图片
    private Callback callback3=new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            Log.d(TAG, "onFailure: ");
            e.printStackTrace();
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            //将响应数据转化为输入流数据
            InputStream inputStream=response.body().byteStream();
            if(inputStream!=null){
                //将输入流数据转化为Bitmap位图数据
                Bitmap bitmap= BitmapFactory.decodeStream(inputStream);
                //创建文件夹
                File file2 = new File(Environment.getExternalStorageDirectory().getPath()+"/oftrees/downloadPics");
                if (!file2.exists())
                {
                    file2.mkdirs();
                }
                //创建文件
                String _name=picPath[picNum];
                File file=new File(Environment.getExternalStorageDirectory().getPath()+"/oftrees/downloadPics/",
                        editText3.getText().toString()
                                +_name.substring(_name.indexOf("_"))
                                +".jpg");
                if(file.exists()){
                    file.delete();
                }
                file.createNewFile();
                //创建文件输出流对象用来向文件中写入数据
                FileOutputStream out=new FileOutputStream(file);
                //将bitmap存储为jpg格式的图片
                if(bitmap!=null){
                    bitmap.compress(Bitmap.CompressFormat.JPEG,100,out);
                    //刷新文件流
                    out.flush();
                    out.close();
                    
                    Message msg=Message.obtain();
                    msg.obj=bitmap;
                    msg.arg1=0;
                    handler.sendMessage(msg);
                }
                else{
                    Log.d(TAG, "onResponse: 没有图片数据");
                }

            }
            else{
                Log.d(TAG, "onResponse: 没有输入流数据");
            }
        }
    };

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.arg1){
                case 0:
                    //更新图片
                    picNum++;
                    if(picNum==picPath.length){
                        pblayout.setVisibility(View.GONE);
                        Toast.makeText(InfoShowActivity.this,"信息加载完成",Toast.LENGTH_SHORT).show();
                    }
                    Log.d(TAG, "handleMessage: ");
                    switch (picNum){
                        case 1:
                            pic1.setImageBitmap((Bitmap)msg.obj);
                            Log.d(TAG, "handleMessage: 1");
                            break;
                        case 2:
                            pic2.setImageBitmap((Bitmap)msg.obj);
                            Log.d(TAG, "handleMessage: 2");
                            break;
                        case 3:
                            pic3.setImageBitmap((Bitmap)msg.obj);
                            Log.d(TAG, "handleMessage: 3");
                            break;
                    }
                    break;
                case 1:
                    //更新view
                    switch (msg.what){
                        case 0:
//                            pblayout.setVisibility(View.VISIBLE);
                            break;
                        case 1:
//                            pblayout.setVisibility(View.GONE);
                            break;
                    }
                    break;
            }

        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.infoshow_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }
}
