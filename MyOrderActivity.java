package com.vthree.rentbaseapplication.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.vthree.rentbaseapplication.Adapter.MyOrderAdapter;
import com.vthree.rentbaseapplication.Adapter.MyProductOrderAdapter;
import com.vthree.rentbaseapplication.ModelClass.ProductOrderModel;
import com.vthree.rentbaseapplication.R;
import com.vthree.rentbaseapplication.preferences.PrefManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class MyOrderActivity extends AppCompatActivity {
    PrefManager prefManager;
    String user_id;
    RecyclerView recyclerView;
    List<ProductOrderModel> list;
    MyProductOrderAdapter myOrderAdapter;
    int flag=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_order);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("My Order");
        prefManager = new PrefManager(this);
        user_id = prefManager.getString("user_id");

        recyclerView=findViewById(R.id.myorder_recycler);
        recyclerView.hasFixedSize();
        recyclerView.setLayoutManager(new LinearLayoutManager(MyOrderActivity.this));
        list=new ArrayList<>();
        myOrderAdapter=new MyProductOrderAdapter(list,this);
        recyclerView.setAdapter(myOrderAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("daa", "onresume");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        Query key = databaseReference.child("ProductsOrders").child("data").orderByChild("user_id").equalTo(user_id);
        key.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ProductOrderModel model = snapshot.getValue(ProductOrderModel.class);

                    list.add(model);
                    // list.add(model);
                    HashSet<ProductOrderModel> hashSet = new HashSet<ProductOrderModel>();
                    hashSet.addAll(list);
                    list.clear();
                    list.addAll(hashSet);
                    Log.d("size1", "" + list.size());

                    myOrderAdapter.notifyDataSetChanged();

                    Log.d("daaa", model.getProduct_name().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
