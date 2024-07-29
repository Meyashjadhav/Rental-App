package com.vthree.rentbaseapplication.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.vthree.rentbaseapplication.Adapter.BookOrderAdapter;
import com.vthree.rentbaseapplication.Adapter.MyOrderAdapter;
import com.vthree.rentbaseapplication.Adapter.ProductOrderAdapterCustomer;
import com.vthree.rentbaseapplication.ModelClass.BookingModel;
import com.vthree.rentbaseapplication.ModelClass.ProductOrderModel;
import com.vthree.rentbaseapplication.R;
import com.vthree.rentbaseapplication.preferences.PrefManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class BookedOrderActivity extends AppCompatActivity {
    Toolbar toolbar;
    ProgressDialog progressDialog;

    List<ProductOrderModel> list = new ArrayList<>();

    RecyclerView recyclerView;
    int flag = 0;
    ProductOrderAdapterCustomer adapter;
    SearchView editsearch;
    PrefManager prefManager;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String cust_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booked_order);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.bookedorder));
        prefManager = new PrefManager(this);
        cust_id=prefManager.getString("user_id");
        editsearch=(SearchView)findViewById(R.id.searchView);


        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading_firebase));
        progressDialog.show();


        adapter = new ProductOrderAdapterCustomer(list, this);
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
    public void onResume() {
        super.onResume();
        // DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        Log.e("cust_id",cust_id);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        Query key = databaseReference.child("ProductsOrders").child("data").orderByChild("customer_id").equalTo(cust_id);

        key.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    try {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            ProductOrderModel model = snapshot.getValue(ProductOrderModel.class);

                            list.add(model);
                        }

                    } catch (Exception e) {
                        Log.d("dataqq", e.getMessage());
                    }

                    HashSet<ProductOrderModel> hashSet = new HashSet<ProductOrderModel>();
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(BookedOrderActivity.this,ViewProducts.class);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finishAffinity();
    }
}
