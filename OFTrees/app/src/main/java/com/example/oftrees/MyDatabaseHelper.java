package com.example.oftrees;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class MyDatabaseHelper extends SQLiteOpenHelper {

   private static String create_db=
           "create table clt_records("
            + "id integer not null primary key autoincrement,"
            + "time timestamp not null default current_timestamp,"
            +"operator varchar(32) not null,"
                   +"location varchar(256) not null,"
            +"positionlat varchar(32) not null,"
            +"positionlng varchar(32) not null,"
            +"tree_id varchar(32) not null)";

   private Context mContext;


    public MyDatabaseHelper(Context context,String name,SQLiteDatabase.CursorFactory factory,int version){
        super(context,name,factory,version);
        mContext=context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(create_db);
        Toast.makeText(mContext,"create succeeded",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
