package com.example.oftrees.ui.records;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.example.oftrees.MyDatabaseHelper;
import com.example.oftrees.R;
import com.example.oftrees.ui.home.HomeFragment;

import java.util.ArrayList;

public class RecordsFragment extends Fragment {
    
    private MyDatabaseHelper dbHelper;
    private static final String TAG = "RecordsFragment";

    private StringBuilder record;
    private ArrayList<String>records=new ArrayList<>();
    
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_record, container, false);


        //查询操作记录
        dbHelper=new MyDatabaseHelper(getActivity().getApplicationContext(),"records.db",null,1);
        dbHelper.getWritableDatabase();
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from clt_records", null);
        while (cursor.moveToNext()) {
            String record_id = cursor.getString(0); //获取第一列的值,第一列的索引从0开始
            String time = cursor.getString(1);//获取第二列的值
            String operator = cursor.getString(2);//获取第三列的值
            String location = cursor.getString(3);
            String positionlat = cursor.getString(4);//
            String positionlng = cursor.getString(5);//
            String tree_id = cursor.getString(6);//

            record=new StringBuilder();
            record.append("记录编号:        ").append(record_id).append("\n")
                    .append("操作时间:      ").append(time).append("\n")
                    .append("操作人员:      ").append(operator).append("\n")
                    .append("操作地点:      ").append(location).append("   ")
                    .append("(").append(positionlat).append(",").append(positionlng).append(")").append("\n")
                    .append("树木编号:      ").append(tree_id);

            records.add(record.toString());
        }
        cursor.close();
        db.close();

        if(records.size()<1){
            String []a={"暂无记录"};
            ArrayAdapter<String> adapter= new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, a);
            ListView listView=root.findViewById(R.id.record_list);
            listView.setAdapter(adapter);
        }else {
            ArrayAdapter<String> adapter= new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, records);
            ListView listView=root.findViewById(R.id.record_list);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Bundle bundle = new Bundle();
                    ArrayList<String> positions=new ArrayList<>();

                    String t=records.get(position);
                    t=t.substring(t.indexOf("(")+1,t.indexOf(")"));
                    positions.add(t);

                    bundle.putStringArrayList("positions",positions);
                    NavHostFragment.findNavController(RecordsFragment.this).navigate(R.id.action_nav_records_to_nav_home,bundle);

//                    Bundle bundle = new Bundle();
//                    String t=records.get(position);
//                    t=t.substring(t.indexOf("(")+1,t.indexOf(")"));
////                    Log.d(TAG, "onItemClick: "+t);
//                    bundle.putString("positionlat",t.substring(0,t.indexOf(",")));
//                    bundle.putString("positionlng",t.substring(t.indexOf(",")+1));
//
//                    NavHostFragment.findNavController(RecordsFragment.this).navigate(R.id.action_nav_records_to_nav_home,bundle);
                }
            });

        }

        //显示菜单
        setHasOptionsMenu(true);

        return root;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.records_menu,menu);
        menu.findItem(R.id.menu_showRecords).setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()){
            case R.id.menu_showRecords:
                Log.d(TAG, "onOptionsItemSelected: !!!!");
                Bundle bundle = new Bundle();
                ArrayList<String> positions=new ArrayList<>();
                for(int i=0;i<records.size();i++)
                {
                    String t=records.get(i);
                    t=t.substring(t.indexOf("(")+1,t.indexOf(")"));
                    positions.add(t);
                }
                bundle.putStringArrayList("positions",positions);
                NavHostFragment.findNavController(RecordsFragment.this).navigate(R.id.action_nav_records_to_nav_home,bundle);

                break;
        }

        return true;
    }
}