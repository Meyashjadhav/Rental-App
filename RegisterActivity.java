package com.vthree.rentbaseapplication.Activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.vthree.rentbaseapplication.MapsActivity;
import com.vthree.rentbaseapplication.R;
import com.vthree.rentbaseapplication.ModelClass.UserModel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class RegisterActivity extends AppCompatActivity {
    private String mVerificationId;
    //The edittext to input the code
    private Button btnRegister;
    private EditText editName,editContact,editAddress,editcity,editTaluka,editTextusertype;
    private ProgressDialog progressDialog;

    //defining firebaseauth object
    private FirebaseAuth firebaseAuth;
    //firebase auth object
    DatabaseReference databaseReference;
    String user_id ="";
    String token;
    List<UserModel> data;
    int flag = 0;

    double latitude,longitude,dist;//curr_latitude,curr_longitude;
    String value;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        btnRegister=(Button) findViewById(R.id.buttonSignup);
        editName=(EditText)findViewById(R.id.editTextEmail);
        editContact=(EditText)findViewById(R.id.editTextmobile);
        editAddress=(EditText)findViewById(R.id.editTextAddress);
        editcity=(EditText)findViewById(R.id.editTextcity);
        editTaluka=(EditText)findViewById(R.id.editTexttaluka);
        editTextusertype=(EditText)findViewById(R.id.editTextusertype);
        data = new ArrayList<>();


        final Intent intent = this.getIntent();
        value=intent.getStringExtra("value");
        String data = intent.getStringExtra("x");
        String d=intent.getStringExtra("y");
        latitude=intent.getDoubleExtra("latitude",0);
        longitude=intent.getDoubleExtra("longitude",0);
        dist = intent.getDoubleExtra("dist",0);


        String data1=(data+d);
        editAddress.setText(value);

        sharedPreferences=getSharedPreferences("Mydata",MODE_PRIVATE);
        sharedPreferences.edit();
        String name= sharedPreferences.getString("name",null);
        String contact= sharedPreferences.getString("contact",null);
        editName.setText(name);
        editContact.setText(contact);
       // editDescription.setText(description);

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("notification", "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                         token = task.getResult().getToken();

                        // Log and toast

                        Log.d("notification11", token);
                       // Toast.makeText(RegisterActivity.this, token, Toast.LENGTH_SHORT).show();
                    }
                });

        //initializing firebase auth object
        firebaseAuth = FirebaseAuth.getInstance();

        databaseReference = FirebaseDatabase.getInstance().getReference("RentBase").child("user");

        progressDialog = new ProgressDialog(this);


        editAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sharedPreferences = getApplicationContext().getSharedPreferences("Mydata", MODE_PRIVATE);
                editor = sharedPreferences.edit();
                editor.putString("name", editName.getText().toString());
                editor.putString("contact", editContact.getText().toString());
                editor.commit();

                Intent map=new Intent(RegisterActivity.this, MapsActivity.class);
                map.putExtra("type","regi");
                startActivity(map);
                finish();
            }
        });
        //attaching listener to button
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                registerUser();
                sharedPreferences = getApplicationContext().getSharedPreferences("Mydata", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove("name");
                editor.remove("contact");
                editor.commit();
            }
        });
        editTextusertype.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(v);
            }
        });
    }
    private void showDialog(final View v) {
        final String[] array = new String[]{
                "Farmer", "Consumer"
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("User Type");
        builder.setSingleChoiceItems(array, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ((EditText) v).setText(array[i]);
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }
    private void registerUser(){

        //getting email and password from edit texts
        String name = editName.getText().toString().trim();
        String contact  = editContact.getText().toString().trim();
        String address  = editAddress.getText().toString().trim();
        String city  = editcity.getText().toString().trim();
        String taluka  = editTaluka.getText().toString().trim();
        String usertype  = editTextusertype.getText().toString().trim();
        //checking if email and passwords are empty
        if(TextUtils.isEmpty(name)){
            Toast.makeText(this,getString(R.string.enter_name),Toast.LENGTH_SHORT).show();
            return;
        }else if(TextUtils.isEmpty(contact)){
            Toast.makeText(this,getString(R.string.enter_contact),Toast.LENGTH_SHORT).show();
            return;
        }else if(TextUtils.isEmpty(address)){
            Toast.makeText(this,getString(R.string.enter_add),Toast.LENGTH_SHORT).show();
            return;
        }else if(TextUtils.isEmpty(city)){
            Toast.makeText(this,getString(R.string.enter_city),Toast.LENGTH_SHORT).show();
            return;
        }else if(TextUtils.isEmpty(taluka)){
            Toast.makeText(this,getString(R.string.enter_taluka),Toast.LENGTH_SHORT).show();
            return;
        }else if(TextUtils.isEmpty(usertype)){
            Toast.makeText(this,getString(R.string.select_user),Toast.LENGTH_SHORT).show();
            return;
        }else {
            user_id = databaseReference.push().getKey();

            for (int i = 0; i < data.size(); i++) {
                if (contact.equals(data.get(i).getMobile())) {
                    Log.d("da11", data.get(i).getMobile());
                    user_id = data.get(i).getUser_id();
                    flag = 1;
                }
            }
            Log.d("da11", String.valueOf(flag));
            if (flag == 1) {
                flag = 0;
                Toast.makeText(RegisterActivity.this, getString(R.string.mobile_not_registered), Toast.LENGTH_SHORT).show();

            } else {
                databaseReference.child(user_id).setValue(new UserModel(user_id,name,address,contact,city,taluka,token,usertype))
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                              //  Toast.makeText(RegisterActivity.this, "User Added Successful", Toast.LENGTH_SHORT).show();
                                AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                                builder.setMessage(getString(R.string.user_added))
                                        .setCancelable(false)
                                        .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                //do things
                                              //  dialog.dismiss();
                                                Intent i = new Intent(RegisterActivity.this,LoginActivity.class);
                                                startActivity(i);
                                                finish();
                                            }
                                        });
                                AlertDialog alert = builder.create();
                                alert.show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                       // Toast.makeText(RegisterActivity.this, "User Not Added Successful", Toast.LENGTH_SHORT).show();
                        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                        builder.setMessage(getString(R.string.enter_user_not_added))
                                .setCancelable(false)
                                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        //do things
                                        dialog.dismiss();
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                });
            }
        }

        //if the email and password are not empty
        //displaying a progress dialog

        /*progressDialog.setMessage("Registering Please Wait...");
        progressDialog.show();*/





    }

    @Override
    protected void onResume() {
        super.onResume();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        UserModel model = snapshot.getValue(UserModel.class);
                        data.add(model);

                        Log.d("da", model.getMobile().toString());
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
