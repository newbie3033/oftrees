package com.example.oftrees;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import com.example.oftrees.ui.login.LoginActivity;
import com.example.oftrees.ui.person.personInfo;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout drawer;
    //监听网络变化
    private IntentFilter intentFilter;
    private NetworkChangeReceiver networkChangeReceiver;

    private  NavigationView navigationView;
    private NavController navController;
    private View headerView;

    //用户信息
    private String username;

    private MyDatabaseHelper dbHelper;

    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("MainActivity", "onCreate: ");

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        //为悬浮按钮添加事件
//        FloatingActionButton fB_addTree = findViewById(R.id.fButton_addTree);
//        fB_addTree.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });


        drawer = findViewById(R.id.drawer_layout);
//        ActionBar actionBar=getSupportActionBar();
//        if(actionBar!=null){
//            actionBar.setDisplayHomeAsUpEnabled(true);
//        }
        navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_records, R.id.nav_send)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navigationView.bringToFront();
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                //跳转不同菜单项
//                int i=navigationView.getCheckedItem().getItemId();
                navController.popBackStack();
                navController.navigate(menuItem.getItemId());
//                navController.navigateUp();
                navigationView.setCheckedItem(menuItem.getItemId());

                drawer.closeDrawers();
                return true;
            }
        });
        headerView=navigationView.getHeaderView(0);


        //用户简要信息和头像展示
        username=getIntent().getStringExtra("username");
        TextView tv1=headerView.findViewById(R.id.header_username);
        TextView tv2=headerView.findViewById(R.id.header_info);
        if(username==null||username.equals("")||username.isEmpty())
            tv1.setText(R.string.nav_header_title);
        else{
            tv1.setText(username);
            tv2.setText("数据采集员");
        }
        //连接服务端获取用户其他信息
        if(getUserInfo())
        {
            //code here
        }


        headerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: head click");
//                //跳转个人信息界面
//                Intent intent=new Intent(MainActivity.this, personInfo.class);
//                intent.putExtra("username",username);
//                startActivity(intent);
            }
        });

        //创建数据库
        dbHelper=new MyDatabaseHelper(this,"records.db",null,1);
        dbHelper.getWritableDatabase();


    }


    //连接服务器获取用户信息
    private boolean getUserInfo(){

        //code here
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        //这里需要判断当前的fragment，以改变不同菜单项的可见性
//        if(false){
//            MenuItem item=menu.findItem(R.id.nav_share);
//            item.setVisible(false);
//        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){

            case R.id.menu_relogin:
                Intent intent=new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                break;
            case android.R.id.home:
                drawer.openDrawer(GravityCompat.START);
                break;
            default:
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    //网络变化广播接收器
    class NetworkChangeReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager=(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo=connectivityManager.getActiveNetworkInfo();
            if(networkInfo!=null&&networkInfo.isConnected()){
//                Toast.makeText(MainActivity.this,"网络可用",Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(MainActivity.this,"网络不可用！",Toast.LENGTH_SHORT).show();
            }
        }
    }

//    //展示定位信息
//    public  void showPositionInfo(){
//        HomeFragment fragment= (HomeFragment) MainActivity.this.getSupportFragmentManager().findFragmentById(R.id.nav_home);
//
//        if(fragment!=null)
//            fragment.showPositionInfo();
//        else
//            Toast.makeText(this,"没有定位信息",Toast.LENGTH_SHORT).show();
//    }


    @Override
    protected void onStart() {
        super.onStart();
        //监听网络变化
        intentFilter=new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        networkChangeReceiver=new NetworkChangeReceiver();
        registerReceiver(networkChangeReceiver,intentFilter);

    }

    @Override
    protected void onResume() {
        super.onResume();

        //查询操作记录
        dbHelper=new MyDatabaseHelper(this,"records.db",null,1);
        dbHelper.getWritableDatabase();
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from clt_records", null);
        while (cursor.moveToNext()) {
            String record_id = cursor.getString(0); //获取第一列的值,第一列的索引从0开始
            String time = cursor.getString(1);//获取第二列的值
            String operator = cursor.getString(2);//获取第三列的值
            String positionlat = cursor.getString(3);//获取第三列的值
            String positionlng = cursor.getString(4);//获取第三列的值
            String tree_id = cursor.getString(5);//获取第三列的值

            Log.d(TAG, "onStart: "+record_id+","+time+","+operator+","+positionlat+","+positionlng+","+tree_id);
        }

        cursor.close();
        db.close();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(networkChangeReceiver);
    }
}
