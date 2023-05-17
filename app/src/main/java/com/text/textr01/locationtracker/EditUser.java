package com.text.textr01.locationtracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class EditUser extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    EditText EditName,EditEmpId,EditAddress,EditEmailId,EditPassword;
    Button EditCreateUserButton;
    String EditNametxt,EditEmpIdtxt,EditAddresstxt,EditEmailIdtxt,EditPasswordtxt,EditStatustxt,currentStatus;
    ProgressDialog loadingBar;

    String phonenumber;


    Spinner EditStatusSpinner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user);

        loadingBar=new ProgressDialog(this);
        EditStatusSpinner=(Spinner)findViewById(R.id.editstatusspinner);
        EditStatusSpinner.setOnItemSelectedListener(this);


        final ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this, R.array.Spinner_items1, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        final ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array.Spinner_items2, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);



        Intent i=getIntent();
        phonenumber=i.getStringExtra("phone");



        final DatabaseReference RootRef;
        RootRef= FirebaseDatabase.getInstance().getReference();



        EditName=(EditText)findViewById(R.id.editname);
        EditEmpId=(EditText)findViewById(R.id.editempid);
        EditAddress=(EditText)findViewById(R.id.editaddress);
        EditEmailId=(EditText)findViewById(R.id.editemailid);
        EditPassword=(EditText)findViewById(R.id.editcreateuserpassword);

        RootRef.child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                EditName.setText(dataSnapshot.child(phonenumber).child("name").getValue().toString());
                EditEmpId.setText(dataSnapshot.child(phonenumber).child("empid").getValue().toString());
                EditAddress.setText(dataSnapshot.child(phonenumber).child("address").getValue().toString());
                EditEmailId.setText(dataSnapshot.child(phonenumber).child("email").getValue().toString());
                EditPassword.setText(dataSnapshot.child(phonenumber).child("password").getValue().toString());
                EditStatustxt=dataSnapshot.child(phonenumber).child("status").getValue().toString();

                //Toast.makeText(EditUser.this,EditStatustxt , Toast.LENGTH_SHORT).show();
                if(EditStatustxt.equals("Active"))
                {
                    EditStatusSpinner.setAdapter(adapter1);
                }

                else if(EditStatustxt.equals("Inactive"))
                {
                    EditStatusSpinner.setAdapter(adapter2);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        EditCreateUserButton=(Button)findViewById(R.id.editcreateuserbtn);

        EditCreateUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditNametxt=EditName.getText().toString();
                EditEmpIdtxt=EditEmpId.getText().toString();
                EditAddresstxt=EditAddress.getText().toString();
                EditEmailIdtxt=EditEmailId.getText().toString();
                EditPasswordtxt=EditPassword.getText().toString();

                if(TextUtils.isEmpty(EditNametxt))
                {
                    Toast.makeText(EditUser.this, "Please enter name...", Toast.LENGTH_SHORT).show();
                }
                else if (TextUtils.isEmpty(EditEmpIdtxt))
                {
                    Toast.makeText(EditUser.this, "Please enter empid...", Toast.LENGTH_SHORT).show();
                }
                else if (TextUtils.isEmpty(EditAddresstxt))
                {
                    Toast.makeText(EditUser.this, "Please enter address...", Toast.LENGTH_SHORT).show();
                }
                else if (TextUtils.isEmpty(EditEmailIdtxt))
                {
                    Toast.makeText(EditUser.this, "Please enter bus id...", Toast.LENGTH_SHORT).show();
                }
                else if (TextUtils.isEmpty(EditPasswordtxt))
                {
                    Toast.makeText(EditUser.this, "Please enter password...", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    loadingBar.setTitle("Edit Details");
                    loadingBar.setMessage("Please wait, while we are checking credentials");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    ValidatePhoneNumber(EditNametxt,EditEmpIdtxt,EditAddresstxt,EditEmailIdtxt,EditPasswordtxt);

                }

            }
        });

    }

    private void ValidatePhoneNumber(final String editNametxt, final String editEmpIdtxt, final String editAddresstxt, final String editEmailIdtxt, final String editPasswordtxt) {
        final DatabaseReference RootRef;
        RootRef= FirebaseDatabase.getInstance().getReference();

        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("Users").child(phonenumber).exists())
                {
                    HashMap<String,Object> userDataMap=new HashMap<>();
                    userDataMap.put("name",editNametxt);
                    userDataMap.put("empid",editEmpIdtxt);
                    userDataMap.put("address",editAddresstxt);
                    userDataMap.put("busid",editEmailIdtxt);
                    userDataMap.put("password",editPasswordtxt);
                    userDataMap.put("status",currentStatus);

                    RootRef.child("Users").child(phonenumber).updateChildren(userDataMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        Toast.makeText(EditUser.this, "update successful", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                        Intent i =new Intent(EditUser.this,HomeActicity.class);
                                        startActivity(i);
                                    }
                                }
                            });
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        currentStatus=adapterView.getSelectedItem().toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
