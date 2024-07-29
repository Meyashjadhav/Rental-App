package com.vthree.rentbaseapplication.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vthree.rentbaseapplication.ModelClass.UserModel;
import com.vthree.rentbaseapplication.R;
import com.vthree.rentbaseapplication.preferences.PrefManager;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private EditText editName,editContact,editAddress,editcity,editTaluka;
    private ProgressDialog progressDialog;

    //defining firebaseauth object
    private FirebaseAuth firebaseAuth;
    //firebase auth object
    DatabaseReference databaseReference;
    String user_id;
    List<UserModel> data;
    int flag = 0;
    PrefManager prefManager;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        prefManager=new PrefManager(this);
        user_id = prefManager.getString("user_id");

        editName=(EditText)findViewById(R.id.editTextEmail);
        editContact=(EditText)findViewById(R.id.editTextmobile);
        editAddress=(EditText)findViewById(R.id.editTextAddress);
        editcity=(EditText)findViewById(R.id.editTextcity);
        editTaluka=(EditText)findViewById(R.id.editTexttaluka);
        data = new ArrayList<>();


        databaseReference = FirebaseDatabase.getInstance().getReference("RentBase").child("user");

        progressDialog = new ProgressDialog(this);


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

                        user_id = prefManager.getString("user_id");
                        for (int i = 0; i < data.size(); i++) {
                            if (user_id.equals(data.get(i).getUser_id())) {
                                Log.d("da11", data.get(i).getMobile());
                                user_id = data.get(i).getUser_id();
                                flag = 1;

                                editName.setText(data.get(i).getUser_name());
                                editContact.setText(data.get(i).getMobile());
                                editAddress.setText(data.get(i).getAddress());
                                editcity.setText(data.get(i).getCity());
                                editTaluka.setText(data.get(i).getTaluka());
                            }
                        }
                        Log.d("da11", String.valueOf(flag));
                        /*if (flag == 1) {
                            flag = 0;

                        }*/
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
