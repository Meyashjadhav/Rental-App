package com.vthree.rentbaseapplication.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import com.vthree.rentbaseapplication.Adapter.EquipmentListAdapter;
import com.vthree.rentbaseapplication.DirectionsJSONParser;
import com.vthree.rentbaseapplication.ModelClass.EquipmentModel;
import com.vthree.rentbaseapplication.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class MapNearByActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback {

    RecyclerView recyclerView;
    int flag = 0;
    EquipmentListAdapter adapter;
    EditText editsearch;
    Location mLastLocation;
    LocationManager mLocationManager;
    GoogleApiClient mGoogleApiClient;
    LatLng latLng;
    LocationRequest mLocationRequest;
    GoogleMap mGoogleMap;
    SupportMapFragment mapFrag;
    Marker mCurrLocationMarker;
    ImageView btn_search;
    DatabaseReference databaseReference;
    List<EquipmentModel> list = new ArrayList<>();
    ProgressDialog progressDialog;
String searchequip;
    Polyline line;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_near_by);
        databaseReference = FirebaseDatabase.getInstance().getReference("EquipmentDetail").child("image");
        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.serachmap);
        mapFrag.getMapAsync(this);


        editsearch=(EditText)findViewById(R.id.searchView);
        btn_search=(ImageView) findViewById(R.id.btn_search);

        final Intent intent = this.getIntent();
        searchequip=intent.getStringExtra("searchequip");

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading_firebase));
        progressDialog.show();

        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                searchequip= editsearch.getText().toString();

                Intent i = new Intent(MapNearByActivity.this, MapNearByActivity.class);  //your class
                i.putExtra("searchequip", searchequip);
                startActivity(i);
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
        }

        buildGoogleApiClient();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        try {
            mLocationRequest = new LocationRequest();
          //  mLocationRequest.setInterval(10000);
           // mLocationRequest.setFastestInterval(1000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                        mLocationRequest, this);
            }
        }catch (Exception e){}
    }

    public LatLng getLocationFromAddress(String strAddress) {

        Geocoder coder = new Geocoder(this);
        List<Address> address;
        LatLng p1 = null;

        try {
            // May throw an IOException
            address = coder.getFromLocationName(strAddress, 50);
            if (address == null) {
                return null;
            }

            Address location = address.get(0);
            p1 = new LatLng(location.getLatitude(), location.getLongitude() );

        } catch (IOException ex) {

            ex.printStackTrace();
        }

        return p1;
    }

    boolean checkRadius(
            int radius,
            double centerLatitude ,
            double centerLongitud,
            double testLatitude,
            double testLongitude
    ){
        float[] results =new  float[1];
        Location.distanceBetween(centerLatitude, centerLongitud, testLatitude, testLongitude, results);
        float distanceInMeters = results[0];
        return distanceInMeters < radius;
    }

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            mLastLocation=location;
            Log.d("location",mLastLocation.getLatitude()+"  :   "+mLastLocation.getLongitude());
        }
    };

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        Log.d("location",mLastLocation.getLatitude()+"  :   "+mLastLocation.getLongitude());
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,10));

        CircleOptions circleOptions = new CircleOptions()
                .center(latLng)
                .radius(7000)
                .strokeWidth(2)
                .strokeColor(Color.BLUE)
                .fillColor(Color.parseColor("#500084d3"));
        mGoogleMap.addCircle(circleOptions);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap=googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);


        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                buildGoogleApiClient();
                mGoogleMap.setMyLocationEnabled(true);
            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        }
        else {
            buildGoogleApiClient();
            mGoogleMap.setMyLocationEnabled(true);
        }


    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    public void EquipSearchList(){

    }
    @Override
    protected void onResume() {
        super.onResume();
        if (searchequip != null && !searchequip.isEmpty() && !searchequip.equals("null")){

            Query qry= databaseReference.orderByChild("equipment_name").equalTo(searchequip);
            qry.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        try {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                EquipmentModel model = snapshot.getValue(EquipmentModel.class);
                                latLng=getLocationFromAddress(model.getAddress());
                                //   boolean fenc= checkRadius(10000,mLastLocation.getLatitude(),mLastLocation.getLongitude(),latLng.latitude,latLng.longitude);
                                if (list.size() > 0) {
                                    for (int i = 0; i < list.size(); i++) {
                                        if (model.getEquipment_id().equals(list.get(i).getEquipment_id())) {
                                            flag = 1;
                                            Log.d("data2", model.getEquipment_name().toString());
                                        }
                                    }
                                    if (flag == 1) {
                                    } else {
                                        flag = 0;
                                        // if (fenc==true) {
                                        list.add(model);
                                        Log.d("data", model.getEquipment_name().toString());
                                        Log.d("data", model.getContact().toString());
                                        MarkerOptions markerOptions = new MarkerOptions();
                                        markerOptions.position(latLng);
                                        markerOptions.title(model.getEquipment_name());

                                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                                        mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);
                                        //   }
                                    }
                                } else {
                                    //   if (fenc==true) {
                                    list.add(model);
                                    Log.d("data11", model.getEquipment_name().toString());
                                    Log.d("data11", model.getContact().toString());
                                    MarkerOptions markerOptions = new MarkerOptions();
                                    markerOptions.position(latLng);
                                    markerOptions.title(model.getEquipment_name());

                                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                                    mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);
                                    //  }
                                }
                                // Log.d("datas", model.getAddress().toString()+"  lat: "+mLastLocation.getLatitude()+"long: "+mLastLocation.getLongitude()+"  val: "+fenc);
                            }

                        } catch (Exception e) {
                            Log.d("data", e.getMessage());
                        }

                        HashSet<EquipmentModel> hashSet = new HashSet<EquipmentModel>();
                        hashSet.addAll(list);
                        list.clear();
                        list.addAll(hashSet);
                        Log.d("size1", "" + list.size());
                        if(progressDialog!=null){
                            progressDialog.dismiss();
                        }

                        // adapter.notifyDataSetChanged();

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d("data", databaseError.getMessage());
                }
            });


        }
        else{
            Query qry= databaseReference.orderByChild("equipment_name");//.equalTo(searchequip);
            qry.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        try {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                EquipmentModel model = snapshot.getValue(EquipmentModel.class);
                                latLng=getLocationFromAddress(model.getAddress());
                                //   boolean fenc= checkRadius(10000,mLastLocation.getLatitude(),mLastLocation.getLongitude(),latLng.latitude,latLng.longitude);
                                if (list.size() > 0) {
                                    for (int i = 0; i < list.size(); i++) {
                                        if (model.getEquipment_id().equals(list.get(i).getEquipment_id())) {
                                            flag = 1;
                                            Log.d("data2", model.getEquipment_name().toString());
                                        }
                                    }
                                    if (flag == 1) {
                                    } else {
                                        flag = 0;
                                        // if (fenc==true) {
                                        list.add(model);
                                        Log.d("data", model.getEquipment_name().toString());
                                        Log.d("data", model.getContact().toString());
                                        MarkerOptions markerOptions = new MarkerOptions();
                                        markerOptions.position(latLng);
                                        markerOptions.title(model.getEquipment_name());

                                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                                        mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);
                                        //   }
                                    }
                                } else {
                                    //   if (fenc==true) {
                                    list.add(model);
                                    Log.d("data11", model.getEquipment_name().toString());
                                    Log.d("data11", model.getContact().toString());
                                    MarkerOptions markerOptions = new MarkerOptions();
                                    markerOptions.position(latLng);
                                    markerOptions.title(model.getEquipment_name());

                                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                                    mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);
                                    //  }
                                }
                                // Log.d("datas", model.getAddress().toString()+"  lat: "+mLastLocation.getLatitude()+"long: "+mLastLocation.getLongitude()+"  val: "+fenc);
                            }

                        } catch (Exception e) {
                            Log.d("data", e.getMessage());
                        }

                        HashSet<EquipmentModel> hashSet = new HashSet<EquipmentModel>();
                        hashSet.addAll(list);
                        list.clear();
                        list.addAll(hashSet);
                        Log.d("size1", "" + list.size());
                        if(progressDialog!=null){
                            progressDialog.dismiss();
                        }

                        // adapter.notifyDataSetChanged();

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d("data", databaseError.getMessage());
                }
            });

        }


    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.permission_needed))
                        .setMessage(getString(R.string.permi_msg))
                        .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MapNearByActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION );
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mGoogleMap.setMyLocationEnabled(true);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, getString(R.string.permi_denied), Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

   /* @Override
    public void onBackPressed() {
        startActivity(new Intent(MapNearByActivity.this,BookEquipementActivity.class));
        finish();
    }*/
}
