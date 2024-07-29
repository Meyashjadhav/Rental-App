package com.vthree.rentbaseapplication.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
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
import com.vthree.rentbaseapplication.Adapter.EquipmentListAdapter;
import com.vthree.rentbaseapplication.Adapter.ProductFarmerAdapter;
import com.vthree.rentbaseapplication.Fragment.HomeEquipment;
import com.vthree.rentbaseapplication.ModelClass.ProductModel;
import com.vthree.rentbaseapplication.R;
import com.vthree.rentbaseapplication.preferences.PrefManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ViewFarmerProducts extends AppCompatActivity {
    Toolbar toolbar;
    ProgressDialog progressDialog;

    List<ProductModel> list = new ArrayList<>();

    RecyclerView recyclerView;
    int flag = 0;
    ProductFarmerAdapter adapter;
    SearchView editsearch;
    PrefManager prefManager;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String user_id,farmer_mobile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_farmer_products);
        prefManager=new PrefManager(this);

        user_id=prefManager.getString("user_id");
        farmer_mobile=prefManager.getString("mobile");
        editsearch=(SearchView)findViewById(R.id.searchView);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading_firebase));
        progressDialog.show();


        adapter = new ProductFarmerAdapter(list, this);
        adapter.OnItemClickArrayElement(new OnItemClickListenerArray());
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

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        Query key = databaseReference.child("Products").child("image").orderByChild("user_id").equalTo(user_id);

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
    private class OnItemClickListenerArray implements ProductFarmerAdapter.OnArrayItemClick {
        @Override
        public void setOnArrayItemClickListener(final int position, final String sellerID) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
            //   Query key = ref.child("EquipmentDetail").child("image").orderByChild("equipment_id").equalTo(sellerID).orderByChild("equipment_id");
            Query key = ref.child("Products").child("image").orderByChild("product_id").equalTo(sellerID);
            key.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot appleSnapshot : dataSnapshot.getChildren()) {
                        appleSnapshot.getRef().removeValue();
                        for (int i = 0; i < list.size(); i++) {
                            if (list.get(i).getProduct_id() == sellerID) {
                                list.remove(i);
                                adapter.notifyDataSetChanged();
                            }
                        }
                        Log.d("position", "" + appleSnapshot.getKey());

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("position", "onCancelled", databaseError.toException());
                }
            });
            Log.d("position11", "" + position + "   " + key + "  " + sellerID);
            //databaseReference.child("seller").child(key).removeValue();

        }
    }
}