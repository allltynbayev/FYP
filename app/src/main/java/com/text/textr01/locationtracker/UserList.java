package com.text.textr01.locationtracker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UserList extends AppCompatActivity {

    ListView listView;
    ArrayList<String> list;
    ArrayAdapter<String> arrayAdapter;
    User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        user =new User();

        listView=(ListView)findViewById(R.id.listview);
        final DatabaseReference RootRef;
        RootRef= FirebaseDatabase.getInstance().getReference();

        list=new ArrayList<>();
        arrayAdapter=new ArrayAdapter<String>(this,R.layout.row,R.id.namelistitem,list);

        RootRef.child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot ds:dataSnapshot.getChildren())
                {
                    user=ds.getValue(User.class);
                    list.add("Phone : "+user.getPhone()+"\n"+"EmpID : "+user.getEmpid()+"\n"+"Address : "+user.getAddress()+
                            "\n"+"Name : "+user.getName()+"\n"+"Email : "+user.getEmail()
                            +"\n"+"Password : "+user.getPassword()+"\n"+"Status : "+user.getStatus());
                }
                listView.setAdapter(arrayAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent =new Intent(UserList.this,EditUser.class);
                intent.putExtra("phone",list.get(i).substring(8,18));
                startActivity(intent);
            }
        });


    }
}
