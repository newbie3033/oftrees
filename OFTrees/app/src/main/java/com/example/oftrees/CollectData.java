package com.example.oftrees;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import pub.devrel.easypermissions.EasyPermissions;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.oftrees.lib.getPhotoFromPhotoAlbum;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class CollectData extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private static final String TAG = "CollectData";
    private static final Integer UPLOAD_FAILED=0;
    private static final Integer UPLOAD_SUCCEED=1;

    private OkHttpClient client=new OkHttpClient();

    private String positionLat="000";
    private String positionLng="000";

    private  EditText editText1,editText2,editText3,editText4,editText5,editText6,editText7,editText8,
            editText9,editText11,editText12,editText13,editText14,editText15,editText16,
            editText17,editText18,editText19,editText21,editText24;

    private Spinner editText10,editText20,editText22,editText23;
    private TextView mtvTitle;

    private ImageView pic1,pic2,pic3;
    private int imageNums=0;
    private File cameraSavePath;//拍照照片路径
    private ArrayList<File> cameraSavePathlist=new ArrayList<>();


    private Dialog dialogPic;
    private Button btn_camera,btn_pic;

    private Uri uri;
    private String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    LinearLayout pblayout;
    //已完成的上传任务数量
    private int tasknum=0;

    private boolean checkIfUpdate;

    private JSONObject _Response;
    private boolean _ResponseSuccess=false;

    private MyDatabaseHelper dbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect);

        positionLat=String.valueOf(getIntent().getDoubleExtra("positionLat",0));
        positionLng=String.valueOf(getIntent().getDoubleExtra("positionLng",0));

        Log.d(TAG, "onCreate: "+positionLat+","+positionLng);

        Button button= findViewById(R.id.collect_submit);

        editText1=findViewById(R.id.address);
        editText1.setText(getIntent().getStringExtra("infos"));

        editText2=findViewById(R.id.xaddress);
        editText3=findViewById(R.id.nuber);
        editText4=findViewById(R.id.branch);
        editText5=findViewById(R.id.sort);
        editText6=findViewById(R.id.cname);
        editText7=findViewById(R.id.alias);
        editText8=findViewById(R.id.latin_name);
        editText9=findViewById(R.id.feature);
        editText10=findViewById(R.id.type);
        editText11=findViewById(R.id.e_age);
        editText12=findViewById(R.id.t_height);
        editText13=findViewById(R.id.diameter);
        editText14=findViewById(R.id.average_crown);
        editText15=findViewById(R.id.se_crown);
        editText16=findViewById(R.id.sn_crown);
        editText17=findViewById(R.id.altitude);
        editText18=findViewById(R.id.slope_d);
        editText19=findViewById(R.id.slope_a);
        editText20=findViewById(R.id.soil_category);
        editText21=findViewById(R.id.soil_density);
        editText22=findViewById(R.id.growth_potential);
        editText23=findViewById(R.id.growth_environment);
        editText24=findViewById(R.id.histroy);

        Toolbar toolbar_collect =  findViewById(R.id.toolbar_collect);
        setSupportActionBar(toolbar_collect);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar_collect.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();//返回
            }
        });

        Button btnGetPicFromCamera = findViewById(R.id.take_photo);
        Button btnGetPicFromPhotoAlbum = findViewById(R.id.album);
        Button btnReChoose=findViewById(R.id.reChoose);

        pic1=findViewById(R.id.pic1);
        pic2=findViewById(R.id.pic2);
        pic3=findViewById(R.id.pic3);

        pblayout=findViewById(R.id.layoutPBar);


        getPermission();
        btnGetPicFromCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imageNums<3){
                    if(editText3.getText().toString().equals(""))
                        Toast.makeText(CollectData.this,"先填写古树编号！",Toast.LENGTH_LONG).show();
                    else
                        goCamera();
                }else {
                    Toast.makeText(CollectData.this,"最多上传三张",Toast.LENGTH_LONG).show();
                }
            }
        });
        btnGetPicFromPhotoAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imageNums<3){
                    if(editText3.getText().toString().equals(""))
                        Toast.makeText(CollectData.this,"先填写古树编号！",Toast.LENGTH_LONG).show();
                    else
                        goPhotoAlbum();
                }else {
                    Toast.makeText(CollectData.this,"最多上传三张",Toast.LENGTH_LONG).show();
                }

            }
        });
        btnReChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //清空照片
                pic1.setImageResource(R.drawable.add);
                pic1.setBackgroundResource(R.drawable.text_sharp);
                pic2.setImageResource(0);
                pic2.setBackgroundResource(0);
                pic3.setImageResource(0);
                pic3.setImageResource(0);
                imageNums=0;
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onJudge())
                {
                    //提示用户正在上传数据
                    AlertDialog.Builder builder=new AlertDialog.Builder(CollectData.this);
                    builder.setTitle("提示")
                            .setMessage("确认要上传这些数据吗？")
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //检查数据上传类型:更新还是新建
                                    checkData();
                                    pblayout.setVisibility(View.VISIBLE);
                                }
                            })
                            .create();
                    builder.show();

                }
            }
        });



    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    //检查数据上传方式
    private void checkData(){
        JSONObject obj=new JSONObject();
        JSONObject object1=new JSONObject();
        try {
            obj.put("type","DATA_CHECK");
            object1.put("positionLat",positionLat);
            object1.put("positionLng",positionLng);
            object1.put("id",editText3.getText().toString());
            obj.put("params",object1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String jsonStr=obj.toString();

        RequestBody body=RequestBody.create(MediaType.parse("application/json;charset=ust-8"),jsonStr);
        Request.Builder request=new Request.Builder()
                .url("http://39.106.89.21/Server/MyMgisServer.php")
                .post(body);
        execute(request,2);

    }


    //上传数据到服务器
    private void submitData(){

        JSONObject obj=new JSONObject();
        JSONObject object1=new JSONObject();


        try{
            obj.put("type","DATA_INPUTINFO");

            object1.put("t_positionlat",positionLat);
            object1.put("t_positionlng",positionLng);
            object1.put("t_location",editText1.getText().toString());
            object1.put("t_location_detail",editText2.getText().toString());
            object1.put("t_id",editText3.getText().toString());
            object1.put("t_family",editText4.getText().toString());
            object1.put("t_category",editText5.getText().toString());
            object1.put("t_chsname",editText6.getText().toString());
            object1.put("t_alias",editText7.getText().toString());
            object1.put("t_latinname",editText8.getText().toString());
            object1.put("t_trait",editText9.getText().toString());
            object1.put("t_type",editText10.getSelectedItem().toString());
            object1.put("t_age",editText11.getText().toString());
            object1.put("t_rounds",editText12.getText().toString());
            object1.put("t_crown_ave",editText13.getText().toString());
            object1.put("t_crown_ew",editText14.getText().toString());
            object1.put("t_crown_sn",editText15.getText().toString());
            object1.put("t_elevation",editText16.getText().toString());
            object1.put("t_aspect",editText17.getText().toString());
            object1.put("t_gradient",editText18.getText().toString());
            object1.put("t_slope",editText19.getText().toString());
            object1.put("t_soil_name",editText20.getSelectedItem().toString());
            object1.put("t_soil_density",editText21.getText().toString());
            object1.put("t_growth_potential",editText22.getSelectedItem().toString());
            object1.put("t_growth_environment",editText23.getSelectedItem().toString());
            object1.put("t_history_detail",editText24.getText().toString());


            obj.put("params",object1);

            Log.d(TAG, "onClick: "+obj.toString());
        }catch (Exception e){
            e.printStackTrace();
        }

        String jsonStr=obj.toString();
        RequestBody body=RequestBody.create(MediaType.parse("application/json;charset=ust-8"),jsonStr);
        Request.Builder request=new Request.Builder()
                .url("http://39.106.89.21/Server/MyMgisServer.php")
                .post(body);
        execute(request,0);


    }

    //上传图片到服务器
    //单张图片的大小应该小于8M
    private void upLoadPic() {
        File file;
        for (int i = 0; i < imageNums; i++) {
            file = cameraSavePathlist.get(i);
            MultipartBody.Builder builder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("img", file.getName(), RequestBody.create(MediaType.parse("image/jpeg"), file));
            RequestBody requestBody = builder.build();
            Request.Builder request = new Request.Builder()
                    .url("http://39.106.89.21/Server/MyMgisServer.php")
                    .post(requestBody);
            execute(request,1);
        }
    }


    private void execute(Request.Builder builder,int code){
        Call call=client.newCall(builder.build());
        //判断上传的是啥
        //0:json ; 1:jpg
        switch (code){
            case 0:
                call.enqueue(callback0);
                break;
            case 1:
                call.enqueue(callback1);
                break;
            case 2:
                call.enqueue(callback2);
                break;
            case 3:
                call.enqueue(callback3);
                break;
        }
    }

    private Callback callback3=new Callback() {
        @Override
        public void onFailure(@NotNull Call call, @NotNull IOException e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(CollectData.this, "未知错误！", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
            //从response获得服务器返回的数据，转换成字符串处理
            String str=new String(response.body().bytes(),"utf-8");
            Log.d("CollectData", "onResponse:333 "+str);

            try {
                _Response=new JSONObject(str);
                _ResponseSuccess=_Response.getBoolean("success");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if(_ResponseSuccess){
                //通知handle上传数据
                Message message=new Message();
                message.arg1=3;
                handler.sendMessage(message);
            }
            else{
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CollectData.this,"图片更新失败！",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    };

    private Callback callback2=new Callback() {
        @Override
        public void onFailure(@NotNull Call call, @NotNull IOException e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(CollectData.this, "未知错误！", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
            //从response获得服务器返回的数据，转换成字符串处理
            String str=new String(response.body().bytes(),"utf-8");
            Log.d("CollectData", "onResponse: "+str);

            try {
                _Response=new JSONObject(str);
                _ResponseSuccess=_Response.getBoolean("success");
                checkIfUpdate=_ResponseSuccess;
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Message message=new Message();
            message.arg1=2;
            handler.sendMessage(message);
        }
    };

    private Callback callback0=new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            Log.d("CollectData", "onFailure: ");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(CollectData.this, "信息上传失败", Toast.LENGTH_SHORT).show();
                }
            });
            e.printStackTrace();
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            //从response获得服务器返回的数据，转换成字符串处理
            String str=new String(response.body().bytes(),"utf-8");
            Log.d("CollectData", "onResponse: "+str);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(CollectData.this, "信息上传成功", Toast.LENGTH_SHORT).show();
                    tasknum++;
                }
            });

            try {
                _Response=new JSONObject(str);
                _ResponseSuccess=_Response.getBoolean("success");
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    };

    private Callback callback1=new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            Log.e(TAG, "onFailure: 上传图片" + e);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(CollectData.this, "图片上传失败", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            String str=new String(response.body().bytes(),"utf-8");
            Log.e(TAG, "成功:" + str);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(CollectData.this, "图片上传成功", Toast.LENGTH_SHORT).show();
                }
            });
            tasknum++;

            Message message=new Message();
            message.arg1=UPLOAD_SUCCEED;
            handler.sendMessage(message);
        }
    };

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
//            String result=(String)msg.obj;
//            Log.d("CollectData", "handleMessage:result: "+result);
            switch (msg.arg1){
               //失败
               case 0:
                   pblayout.setVisibility(View.GONE);
                   _tips(false);
                   break;
                   //成功
               case 1:
                   if(tasknum==imageNums+1)
                   {
                       pblayout.setVisibility(View.GONE);
                       _tips(true);

                   }
                   break;
                case 2:
                    //更新
                    if(checkIfUpdate)
                    {
                        AlertDialog.Builder builder=new AlertDialog.Builder(CollectData.this);
                        builder.setTitle("提示")
                                .setMessage("此树木已存在，是否将原数据覆盖？")
                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        pblayout.setVisibility(View.GONE);
                                    }
                                })
                                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //删除照片的文件夹,，删除成功后再调用上传功能
                                        _deletePics();
                                    }
                                })
                                .create();
                        builder.show();

                    }else {
                        //插入
                        submitData();
                        upLoadPic();
                    }
                    break;
                case 3:
                    submitData();
                    upLoadPic();
           }

        }
    };

    //删除服务端的图片
    private void _deletePics(){
        JSONObject obj=new JSONObject();
        JSONObject object=new JSONObject();
        try {
            obj.put("type","DATA_DELETE");
            object.put("t_id",editText3.getText().toString());
            obj.put("params",object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String jsonStr=obj.toString();
        RequestBody body=RequestBody.create(MediaType.parse("application/json;charset=ust-8"),jsonStr);
        Request.Builder request=new Request.Builder()
                .url("http://39.106.89.21/Server/MyMgisServer.php")
                .post(body);
        execute(request,3);

    }

    //上传后的提示
    private void _tips(boolean flag){
        AlertDialog.Builder builder=new AlertDialog.Builder(CollectData.this);
        builder.setTitle("提示")
                .create();
        if(flag)
        {
            builder.setMessage("上传成功");
            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
        }else
            builder.setMessage("上传失败");
        builder.show();
    }


    //成功打开权限
    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Toast.makeText(this, "相关权限获取成功", Toast.LENGTH_SHORT).show();
    }
    //用户未同意权限
    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Toast.makeText(this, "请同意相关权限，否则功能无法使用", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String photoPath;
        if (requestCode == 1 && resultCode == RESULT_OK) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                photoPath = String.valueOf(cameraSavePath);
            } else {
                photoPath = uri.getEncodedPath();
            }
            Log.d(TAG, "onActivityResult: 拍照返回图片路径:"+photoPath);
            _refreshPic(photoPath);

        } else if (requestCode == 2 && resultCode == RESULT_OK) {
            photoPath = getPhotoFromPhotoAlbum.getRealPathFromUri(this, data.getData());
            Log.d(TAG, "onActivityResult: 相册返回图片路径:"+photoPath);
            //将路径下的图片复制到指定文件夹并重命名
//            File becopyfile=new File(photoPath);
            //创建文件夹
            File file2 = new File(Environment.getExternalStorageDirectory().getPath()+"/oftrees/uploadPics");
            if (!file2.exists())
            {
                file2.mkdirs();
            }
            int s=(int) System.currentTimeMillis()/60-16200000;
            String copypath=Environment.getExternalStorageDirectory().getPath()+"/oftrees/uploadPics/"+
                    editText3.getText()+"_"+s
                    + ".jpg";
            Log.d(TAG, "onActivityResult: "+copypath);
//            File copyfile=new File();
            try {

                FileInputStream fileInputStream = new FileInputStream(photoPath);
                FileOutputStream fileOutputStream = new FileOutputStream(copypath);
                byte[] buffer = new byte[1024];
                int byteRead;
                while (-1 != (byteRead = fileInputStream.read(buffer))) {
                    fileOutputStream.write(buffer, 0, byteRead);
                }
                fileInputStream.close();
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            photoPath=copypath;

            //需要将得到的照片存放入列表中
            cameraSavePathlist.add(new File(photoPath));

            _refreshPic(photoPath);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


        //将采集记录保存下来
        if(_ResponseSuccess){

            dbHelper=new MyDatabaseHelper(this,"records.db",null,1);
            SQLiteDatabase db=dbHelper.getWritableDatabase();
            ContentValues values=new ContentValues();
            //开始组装第一条数据
            values.put("operator","Guest");
            values.put("location",editText1.getText().toString()+editText2.getText().toString());
            values.put("positionlat",positionLat);
            values.put("positionlng",positionLng);
            values.put("tree_id",editText3.getText().toString());
            db.insert("clt_records",null,values);
            values.clear();

        }
    }

    //更新显示的图片
    private void _refreshPic(String photoPath){
        switch (imageNums){
            case 0:
                Glide.with(CollectData.this).load(photoPath).into(pic1);
                pic2.setImageResource(R.drawable.add);
                pic2.setBackgroundResource(R.drawable.text_sharp);
                pic1.setBackgroundResource(0);

                imageNums++;
                break;
            case 1:
                Glide.with(CollectData.this).load(photoPath).into(pic2);
                pic3.setImageResource(R.drawable.add);
                pic3.setBackgroundResource(R.drawable.text_sharp);
                pic2.setBackgroundResource(0);
                imageNums++;
                break;
            case 2:
                Glide.with(CollectData.this).load(photoPath).into(pic3);
                pic3.setBackgroundResource(0);
                imageNums++;
                break;
            default:
                break;
        }
    }

    //激活相册操作
    private void goPhotoAlbum() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 2);
    }

    //激活相机操作
    private void goCamera() {
        int s=(int)System.currentTimeMillis()/60-16200000;
        //创建文件夹
        File file2 = new File(Environment.getExternalStorageDirectory().getPath()+"/oftrees/uploadPics");
        if (!file2.exists())
        {
            file2.mkdirs();
        }
        cameraSavePath = new File(
                Environment.getExternalStorageDirectory().getPath()+"/oftrees/uploadPics"
                        + "/" + editText3.getText()+"_"+s
                        + ".jpg");
        cameraSavePathlist.add(cameraSavePath);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(CollectData.this, "com.example.oftrees.fileprovider", cameraSavePath);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(cameraSavePath);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        CollectData.this.startActivityForResult(intent, 1);
    }


    //获取权限
    private void getPermission() {
        if (EasyPermissions.hasPermissions(CollectData.this, permissions)) {
            //已经打开权限
            Toast.makeText(this, "已经申请相关权限", Toast.LENGTH_SHORT).show();
        } else {
            //没有打开相关权限、申请权限
            EasyPermissions.requestPermissions(CollectData.this, "需要获取您的相册、照相使用权限", 1, permissions);
        }

    }


//提交前判断
    private boolean onJudge() {
        if (editText1.getText().toString().equals("")) {
            Toast.makeText(CollectData.this, "请填写古树位置", Toast.LENGTH_SHORT).show();
            return false;
        } else if (editText2.getText().toString().equals("")) {
            Toast.makeText(CollectData.this, "请填写古树详细位置", Toast.LENGTH_SHORT).show();
            return false;
        } else if (editText3.getText().toString().equals("")) {
            Toast.makeText(CollectData.this, "请填写古树编号", Toast.LENGTH_SHORT).show();
            return false;
        } else if (editText4.getText().toString().equals("")) {
            Toast.makeText(CollectData.this, "请填写古树所属科", Toast.LENGTH_SHORT).show();
            return false;
        } else if (editText5.getText().toString().equals("")) {
            Toast.makeText(CollectData.this, "请填写古树所属属", Toast.LENGTH_SHORT).show();
            return false;
        } else if (editText6.getText().toString().equals("")) {
            Toast.makeText(CollectData.this, "请填写古树中文名称", Toast.LENGTH_SHORT).show();
            return false;
        } else if (editText9.getText().toString().equals("")) {
            Toast.makeText(CollectData.this, "请填写古树特点", Toast.LENGTH_SHORT).show();
            return false;
        } else if (editText11.getText().toString().equals("")) {
            Toast.makeText(CollectData.this, "请填写古树树龄", Toast.LENGTH_SHORT).show();
            return false;
        } else if (editText12.getText().toString().equals("")) {
            Toast.makeText(CollectData.this, "请填写古树高度", Toast.LENGTH_SHORT).show();
            return false;
        } else if (editText13.getText().toString().equals("")) {
            Toast.makeText(CollectData.this, "请填写古树胸围", Toast.LENGTH_SHORT).show();
            return false;
        } else if (editText14.getText().toString().equals("")) {
            Toast.makeText(CollectData.this, "请填写古树平均冠幅", Toast.LENGTH_SHORT).show();
            return false;
        } else if (editText15.getText().toString().equals("")) {
            Toast.makeText(CollectData.this, "请填写古树东西冠幅", Toast.LENGTH_SHORT).show();
            return false;
        } else if (editText16.getText().toString().equals("")) {
            Toast.makeText(CollectData.this, "请填写古树南北冠幅", Toast.LENGTH_SHORT).show();
            return false;
        } else if (editText17.getText().toString().equals("")) {
            Toast.makeText(CollectData.this, "请填写古树所在位置海拔", Toast.LENGTH_SHORT).show();
            return false;
        } else if (editText18.getText().toString().equals("")) {
            Toast.makeText(CollectData.this, "请填写古树所在位置坡向", Toast.LENGTH_SHORT).show();
            return false;
        } else if (editText19.getText().toString().equals("")) {
            Toast.makeText(CollectData.this, "请填写古树所在位置坡度", Toast.LENGTH_SHORT).show();
            return false;
        } else if (editText21.getText().toString().equals("")) {
            Toast.makeText(CollectData.this, "请填写古树所在位置土壤密度", Toast.LENGTH_SHORT).show();
            return false;
        } else if (editText24.getText().toString().equals("")) {
            Toast.makeText(CollectData.this, "请填写您所了解的关于古树的历史或详情", Toast.LENGTH_SHORT).show();
            return false;
        }else if (imageNums==0){
            Toast.makeText(CollectData.this,"请上传照片!!",Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.collect_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }
}
