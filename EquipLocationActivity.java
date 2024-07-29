package com.vthree.rentbaseapplication.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.internal.view.SupportMenu;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.vthree.rentbaseapplication.Adapter.Constants;
import com.vthree.rentbaseapplication.Adapter.DirectionsJSONParser;

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
import java.util.List;
import java.util.Locale;

public class EquipLocationActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

private GoogleMap mMap;
      //  double destlang;
     //   double destlat;
        double lat;
        double lng;
        Marker mCurrLocationMarker;
        GoogleApiClient mGoogleApiClient;
        Location mLastLocation;
        LocationRequest mLocationRequest;
        //GoogleMap mMap;
        MarkerOptions markerOptions1;
        TextView MarkerAddress;

        double x, y;
        double curr_lat,curr_lng,latitude,longitude;
        StringBuilder strReturnedAddress;
        double dist;


private class DownloadTask extends AsyncTask<String, Void, String> {
    private DownloadTask() {
    }

    protected String doInBackground(String... url) {
        String data = "";
        try {
            data = EquipLocationActivity.this.downloadUrl(url[0]);
        } catch (Exception e) {
            Log.d("Background Task", e.toString());
        }
        return data;
    }

    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        new EquipLocationActivity.ParserTask().execute(new String[]{result});
    }
}

private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
    private ParserTask() {
    }

    protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
        List<List<HashMap<String, String>>> routes = null;
        try {
            routes = new DirectionsJSONParser().parse(new JSONObject(jsonData[0]));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return routes;
    }

    protected void onPostExecute(List<List<HashMap<String, String>>> result) {
        PolylineOptions lineOptions = null;
        MarkerOptions markerOptions = new MarkerOptions();
        for (int i = 0; i < result.size(); i++) {
            ArrayList points = new ArrayList();
            lineOptions = new PolylineOptions();
            List<HashMap<String, String>> path = (List) result.get(i);
            for (int j = 0; j < path.size(); j++) {
                HashMap<String, String> point = (HashMap) path.get(j);
                points.add(new LatLng(Double.parseDouble((String) point.get("lat")), Double.parseDouble((String) point.get("lng"))));
            }
            lineOptions.addAll(points);
            lineOptions.width(12.0f);
            lineOptions.color(SupportMenu.CATEGORY_MASK);
            lineOptions.geodesic(true);
        }
        if (lineOptions != null) {
            mMap.addPolyline(lineOptions);
        }
    }
}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equip_location);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        MarkerAddress = (TextView) findViewById(R.id.txt_addresss);


        final Intent intent = this.getIntent();
        latitude=intent.getDoubleExtra("latitude",0);
        longitude=intent.getDoubleExtra("longitude",0);

        if (Build.VERSION.SDK_INT >= 23) {
            checkLocationPermission();
        }
        //((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") == 0) {
            return true;
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, "android.permission.ACCESS_FINE_LOCATION")) {
            ActivityCompat.requestPermissions(this, new String[]{"android.permission.ACCESS_FINE_LOCATION"}, 99);
            return false;
        }
        ActivityCompat.requestPermissions(this, new String[]{"android.permission.ACCESS_FINE_LOCATION"}, 99);
        return false;
    }


    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) new URL(strUrl).openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String str = "";
            while (true) {
                str = br.readLine();
                if (str == null) {
                    break;
                }
                sb.append(str);
            }
            data = sb.toString();
            br.close();
        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
    protected void onPostExecute(List<List<HashMap<String, String>>> result) {
        PolylineOptions lineOptions = null;
        MarkerOptions markerOptions = new MarkerOptions();
        for (int i = 0; i < result.size(); i++) {
            ArrayList points = new ArrayList();
            lineOptions = new PolylineOptions();
            List<HashMap<String, String>> path = (List) result.get(i);
            for (int j = 0; j < path.size(); j++) {
                HashMap<String, String> point = (HashMap) path.get(j);
                points.add(new LatLng(Double.parseDouble((String) point.get("lat")), Double.parseDouble((String) point.get("lng"))));
            }
            lineOptions.addAll(points);
            lineOptions.width(12.0f);
            lineOptions.color(SupportMenu.CATEGORY_MASK);
            lineOptions.geodesic(true);
        }
        if (lineOptions != null) {
            mMap.addPolyline(lineOptions);
        }
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;
        this.mMap.setMapType(1);
        if (Build.VERSION.SDK_INT < 23) {
            buildGoogleApiClient();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            this.mMap.setMyLocationEnabled(true);
        } else if (ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") == 0) {
            buildGoogleApiClient();
            this.mMap.setMyLocationEnabled(true);
        }
    }

    protected synchronized void buildGoogleApiClient() {
        this.mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        this.mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        this.mLocationRequest = new LocationRequest();
        this.mLocationRequest.setInterval(1000);
        this.mLocationRequest.setFastestInterval(1000);
        this.mLocationRequest.setPriority(102);
        if (ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") == 0) {
            LocationServices.FusedLocationApi.requestLocationUpdates(this.mGoogleApiClient, this.mLocationRequest, (LocationListener) this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        this.mLastLocation = location;
        if (this.mCurrLocationMarker != null) {
            this.mCurrLocationMarker.remove();
        }
      //  this.lat = location.getLatitude();
       // this.lng = location.getLongitude();
        this.lat = latitude;
         this.lng = longitude;
        LatLng latLng = new LatLng(latitude, longitude);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.markerfour));

        // Toast.makeText(MapCurrentActivity.this,"LatLong"+latLng,Toast.LENGTH_SHORT).show();
        // String provider = ((LocationManager) getSystemService("location")).getBestProvider(new Criteria(), true);
        if (ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") == 0 || ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_COARSE_LOCATION") == 0) {
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(300.0f));
            this.mCurrLocationMarker = this.mMap.addMarker(markerOptions);
            this.mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            this.mMap.animateCamera(CameraUpdateFactory.zoomTo(10.0f));
            if (this.mGoogleApiClient != null) {
                LocationServices.FusedLocationApi.removeLocationUpdates(this.mGoogleApiClient, (LocationListener) this);
            }
            String url = getDirectionsUrl();
            new EquipLocationActivity.DownloadTask().execute(new String[]{url});

            curr_lat=this.lat;
            curr_lng=this.lng;
            drawMarker(new LatLng(this.lat, this.lng), new LatLng(this.lat, this.lng));


            MarkerAddress.setText("latitude" + latitude + " " + longitude);
            Constants.Lat=latitude;
            Constants.Long= longitude;
            getCompleteAddressString(latitude, longitude);
        }
    }

    private String getDirectionsUrl() {

        String str_origin = "origin=" + this.lat + "," + this.lng;
        return "https://maps.googleapis.com/maps/api/directions/" + "json" + "?" + (str_origin + "&" + ("destination=" +this.lat  + "," + this.lng) + "&" + "sensor=false" + "&" + "mode=driving") + "&key=AIzaSyAolmn5QHW4BY2Et-0Qz5DhAgiOOjzFE3o";

    }

    public void drawMarker(final LatLng source_point, LatLng destination_point) {
        markerOptions1 = new MarkerOptions();
        markerOptions1.title("Source");
        markerOptions1.snippet("Current Location" + source_point);
        markerOptions1.icon(BitmapDescriptorFactory.fromResource(R.drawable.markerfour));
        markerOptions1.position(source_point);
        markerOptions1.draggable(true);
        this.mMap.addMarker(markerOptions1);
        this.mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                marker.setSnippet("latitude" + latitude + " " + longitude);
                MarkerAddress.setText("latitude" + latitude + " " + longitude);
                      /*  x=marker.getPosition().latitude;
                        y=marker.getPosition().longitude;*/
                Constants.Lat=latitude;
                Constants.Long= longitude;
                mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                getCompleteAddressString(latitude, longitude);
            }

            @Override
            public void onMarkerDragEnd(final Marker marker) {
               /* marker.setSnippet("latitude" + marker.getPosition().latitude + " " + marker.getPosition().longitude);
                MarkerAddress.setText("latitude" + marker.getPosition().latitude + " " + marker.getPosition().longitude);
                      *//*  x=marker.getPosition().latitude;
                        y=marker.getPosition().longitude;*//*
                Constants.Lat=marker.getPosition().latitude;
                Constants.Long= marker.getPosition().longitude;
                mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                getCompleteAddressString(marker.getPosition().latitude, marker.getPosition().longitude);
                distance(curr_lat,curr_lng,marker.getPosition().latitude, marker.getPosition().longitude);*/
                // cancelOrder(String.valueOf(marker.getPosition().latitude),String.valueOf(marker.getPosition().longitude));
               /* button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.e("distanace param", marker.getPosition().latitude+"//"+marker.getPosition().longitude +"//"+ curr_lat+"//"+curr_lng);

                        distance(curr_lat,curr_lng,marker.getPosition().latitude, marker.getPosition().longitude);

                        if(type.equalsIgnoreCase("regi")){
                            Intent intent = new Intent(EquipLocationActivity.this, RegisterActivity.class);
                            String value = strReturnedAddress.toString();
                            intent.putExtra("value", value);
                            intent.putExtra("x", x);
                            intent.putExtra("y", y);
                            intent.putExtra("latitude", marker.getPosition().latitude);
                            intent.putExtra("longitude", marker.getPosition().longitude);
                            intent.putExtra("dist", dist);
                            // intent.putExtra("curr_longitude", curr_lng);

                            startActivity(intent);
                            finish();
                        }else{
                            Intent intent = new Intent(EquipLocationActivity.this, EquipmentRegisterActivity.class);
                            String value = strReturnedAddress.toString();
                            intent.putExtra("value", value);
                            intent.putExtra("x", x);
                            intent.putExtra("y", y);
                            intent.putExtra("latitude", marker.getPosition().latitude);
                            intent.putExtra("longitude", marker.getPosition().longitude);
                            intent.putExtra("dist", dist);
                            // intent.putExtra("curr_longitude", curr_lng);

                            startActivity(intent);
                            finish();
                        }


                    }
                });*/
            }

            @Override
            public void onMarkerDrag(Marker marker) {

                marker.setSnippet("latitude" + latitude + " " + longitude);
                MarkerAddress.setText("latitude" + latitude + " " + longitude);
                      /*  x=marker.getPosition().latitude;
                        y=marker.getPosition().longitude;*/
                Constants.Lat=latitude;
                Constants.Long= longitude;
                mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                getCompleteAddressString(latitude, longitude);
            }
        });
    }

    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                Log.w("My Current loction ", strReturnedAddress.toString());
                MarkerAddress.setText(strReturnedAddress.toString());
                Log.e("curr location",strReturnedAddress.toString());

            } else {
                Log.w("My Current loction ", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("My Current loction ", "Canont get Address!");
        }
        return strAdd;
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        dist=(dist/0.62137);//convert miles to kilometer
        Log.e("dist", String.valueOf(dist));
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }
}
