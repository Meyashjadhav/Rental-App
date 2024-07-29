package com.vthree.rentbaseapplication.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import com.vthree.rentbaseapplication.Adapter.ProductAdapter;
import com.vthree.rentbaseapplication.ModelClass.ProductModel;
import com.vthree.rentbaseapplication.R;
import com.vthree.rentbaseapplication.preferences.PrefManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class ViewProducts extends AppCompatActivity {

    Toolbar toolbar;
    ProgressDialog progressDialog;

    List<ProductModel> list = new ArrayList<>();

    RecyclerView recyclerView;
    int flag = 0;
    ProductAdapter adapter;
    SearchView editsearch;
    PrefManager prefManager;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_products);

        prefManager=new PrefManager(this);


        editsearch=(SearchView)findViewById(R.id.searchView);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading_firebase));
        progressDialog.show();


        adapter = new ProductAdapter(list, this);
        recyclerView.setAdapter(adapter);

        editsearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editsearch.setIconified(false);
            }
        });
        editsearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return true;
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.consumer_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_language) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ViewProducts.this);
            builder.setTitle(R.string.action_language);
            // Add the buttons
            builder.setPositiveButton(R.string.english, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    String languageToLoad = "en"; // your language
                    Locale locale = new Locale(languageToLoad);
                    Locale.setDefault(locale);
                    Configuration config = new Configuration();
                    config.locale = locale;
                    getBaseContext().getResources().updateConfiguration(config,
                            getBaseContext().getResources().getDisplayMetrics());

                    dialog.dismiss();

                    sharedPreferences = getApplicationContext().getSharedPreferences("Mydata", MODE_PRIVATE);
                    editor = sharedPreferences.edit();
                    editor.putString("language", languageToLoad);
                    editor.commit();

                    finish();
                    startActivity(getIntent());

                }
            });
            builder.setNegativeButton(R.string.marathi, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog

                    String languageToLoad = "mr"; // your language
                    Locale locale = new Locale(languageToLoad);
                    Locale.setDefault(locale);
                    Configuration config = new Configuration();
                    config.locale = locale;
                    getBaseContext().getResources().updateConfiguration(config,
                            getBaseContext().getResources().getDisplayMetrics());

                    dialog.dismiss();
                    sharedPreferences = getApplicationContext().getSharedPreferences("Mydata", MODE_PRIVATE);
                    editor = sharedPreferences.edit();
                    editor.putString("language", languageToLoad);
                    editor.commit();

                    finish();
                    startActivity(getIntent());

                }
            });

            builder.create().show();
        }
        if (item.getItemId() == R.id.action_logout) {

            final AlertDialog builder=new AlertDialog.Builder(this).create();
            View view= LayoutInflater.from(this).inflate(R.layout.row_alert_logout,null);
            Button logout_btn_cancel=(Button)view.findViewById(R.id.logout_btn_cancel);
            Button logout_btn_ok=(Button)view.findViewById(R.id.logout_btn_ok);
            logout_btn_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    builder.dismiss();
                }
            });
            logout_btn_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ViewProducts.this, LoginActivity.class);
                    prefManager.setLogin(false);
                    startActivity(intent);

                    builder.dismiss();

                }
            });
            builder.setView(view);
            builder.setCanceledOnTouchOutside(true);
            builder.show();

        }
        if (item.getItemId() == R.id.action_order) {
            Intent intent=new Intent(ViewProducts.this,BookedOrderActivity.class);
            startActivity(intent);
        }
        else {
            Toast.makeText(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        // DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        Query key = databaseReference.child("Products").child("image").orderByChild("product_id");

        key.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    try {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            ProductModel model = snapshot.getValue(ProductModel.class);
                            //  latLng=getLocationFromAddress(model.getAddress());
                            //   Log.d("data2",mLastLocation.getLatitude()+" : "+mLastLocation.getLongitude()+" : "+latLng.latitude+" : "+latLng.longitude);
                            // boolean fenc= checkRadius(10000,mLastLocation.getLatitude(),mLastLocation.getLongitude(),latLng.latitude,latLng.longitude);
                            if (list.size() > 0) {
                                for (int i = 0; i < list.size(); i++) {
                                    if (model.getProduct_id().equals(list.get(i).getProduct_id())) {
                                        flag = 1;
                                        Log.d("data2", model.getProduct_name().toString());
                                    }
                                }
                                if (flag == 1) {
                                } else {
                                    flag = 0;
                                    //if (fenc==true) {
                                    list.add(model);
                                    Log.d("data", model.getProduct_name().toString());
                                    Log.d("data", model.getFarmer_mobile().toString());
                                       /* MarkerOptions markerOptions = new MarkerOptions();
                                        markerOptions.position(latLng);
                                        markerOptions.title(model.getEquipment_name());

                                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                                        mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);*/
                                    // }
                                }
                            } else {
                                // if (fenc==true) {
                                list.add(model);
                                Log.d("data11", model.getProduct_name().toString());
                                Log.d("data11", model.getQuantity().toString());
                                //  progressDialog.dismiss();
                                 /*   MarkerOptions markerOptions = new MarkerOptions();
                                    markerOptions.position(latLng);
                                    markerOptions.title(model.getEquipment_name());

                                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                                    mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);*/
                                // }
                            }
                            //  Log.d("datas", model.getAddress().toString()+"  lat: "+mLastLocation.getLatitude()+"long: "+mLastLocation.getLongitude()+"  val: "+fenc);
                        }

                    } catch (Exception e) {
                        Log.d("dataqq", e.getMessage());
                    }

                    HashSet<ProductModel> hashSet = new HashSet<ProductModel>();
                    hashSet.addAll(list);
                    list.clear();
                    list.addAll(hashSet);
                    Log.d("size1", "" + list.size());

                    adapter.notifyDataSetChanged();
                }
                if(progressDialog!=null){
                    progressDialog.dismiss();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("data", databaseError.getMessage());
            }
        });


    }
}