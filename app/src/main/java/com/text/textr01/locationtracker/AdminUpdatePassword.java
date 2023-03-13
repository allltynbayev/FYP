package com.text.textr01.locationtracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class AdminUpdatePassword extends AppCompatActivity {

    EditText AdminUsername,AdminPassword;
    Button AdminBtn;
    ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_update_password);

        AdminUsername=(EditText)findViewById(R.id.adminusername);
        AdminPassword=(EditText)findViewById(R.id.adminpassword);

        AdminBtn=(Button)findViewById(R.id.adminupdatepasswordbtn);
        loadingBar=new ProgressDialog(this);

        AdminBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String adminnametxt=AdminUsername.getText().toString();
                String adminpasstxt=AdminPassword.getText().toString();

                if(TextUtils.isEmpty(adminnametxt))
                {
                    Toast.makeText(AdminUpdatePassword.this, "Please enter username...", Toast.LENGTH_SHORT).show();
                }
                else if (TextUtils.isEmpty(adminpasstxt))
                {
                    Toast.makeText(AdminUpdatePassword.this, "Please enter password...", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    loadingBar.setTitle("Update Password");
                    loadingBar.setMessage("Please wait...");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    ChangePassword(adminnametxt,adminpasstxt);

                }
            }
        });
    }

    private void ChangePassword(final String adminnametxt, final String adminpasstxt) {
        final DatabaseReference RootRef;
        RootRef= FirebaseDatabase.getInstance().getReference();

        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String, Object> userDataMap = new HashMap<>();
                userDataMap.put("username", adminnametxt);
                userDataMap.put("password", adminpasstxt);

                RootRef.child("Admin").updateChildren(userDataMap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful())
                                {
                                    loadingBar.dismiss();

                                    SharedPreferences sharedPreferences
                                            = getSharedPreferences("MySharedPref",
                                            MODE_PRIVATE);

                                    SharedPreferences.Editor myEdit
                                            = sharedPreferences.edit();

                                    myEdit.putString(
                                            "active",
                                            "newadmin");
                                    myEdit.commit();

                                    Toast.makeText(AdminUpdatePassword.this, "Password update successful", Toast.LENGTH_SHORT).show();
                                    Intent i=new Intent(AdminUpdatePassword.this,MainActivity.class);
                                    startActivity(i);
                                }
                            }
                        });


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
