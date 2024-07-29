package com.vthree.rentbaseapplication.Activity;

import static android.Manifest.permission.CALL_PHONE;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Address;
import android.location.Geocoder;

import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.vthree.rentbaseapplication.Adapter.EquipmentListAdapter;
import com.vthree.rentbaseapplication.Adapter.MyListAdapter;
import com.vthree.rentbaseapplication.Fragment.BookedOrder;
import com.vthree.rentbaseapplication.Fragment.HomeEquipment;
import com.vthree.rentbaseapplication.Fragment.MyEquipment;
import com.vthree.rentbaseapplication.Fragment.MyOrder;
import com.vthree.rentbaseapplication.MapsActivity;
import com.vthree.rentbaseapplication.ModelClass.EquipmentModel;
import com.vthree.rentbaseapplication.ModelClass.MyListData;
import com.vthree.rentbaseapplication.ModelClass.UserModel;
import com.vthree.rentbaseapplication.R;
import com.vthree.rentbaseapplication.preferences.PrefManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

// NavigationView.OnNavigationItemSelectedListener,
public class MainActivity extends AppCompatActivity
         {
             PrefManager prefManager;
    long back_pressed;
    Toolbar toolbar;
    private TabLayout tab_layout;
    private ViewPager view_pager;
    private SectionsPagerAdapter viewPagerAdapter;
             SharedPreferences sharedPreferences;
             SharedPreferences.Editor editor;
             private static final int PERMISSION_REQUEST_CODE = 200;

             @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkLocationPermission();
        prefManager=new PrefManager(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(this.getResources().getColor( R.color.colorPrimaryDark));
        }
        initComponent();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
        }




       /* DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_person_white);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);*/



    }

    private void initComponent() {

        view_pager = (ViewPager) findViewById(R.id.view_pager);
        tab_layout = (TabLayout) findViewById(R.id.tab_layout);
        setupViewPager(view_pager);
        tab_layout.setupWithViewPager(view_pager);


        tab_layout.getTabAt(0).setIcon(R.drawable.ic_home);
        tab_layout.getTabAt(1).setIcon(R.drawable.equip);
        tab_layout.getTabAt(2).setIcon(R.drawable.order);
        tab_layout.getTabAt(3).setIcon(R.drawable.booking);

      /*  tab_layout.addTab(tab_layout.newTab().setIcon(R.drawable.ic_home), 0);
        tab_layout.addTab(tab_layout.newTab().setIcon(R.drawable.equip), 1);
        tab_layout.addTab(tab_layout.newTab().setIcon(R.drawable.order), 2);
        tab_layout.addTab(tab_layout.newTab().setIcon(R.drawable.booking), 3);*/

        // set icon color pre-selected
        tab_layout.getTabAt(0).getIcon().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
        tab_layout.getTabAt(1).getIcon().setColorFilter(getResources().getColor(R.color.grey_60), PorterDuff.Mode.SRC_IN);
        tab_layout.getTabAt(2).getIcon().setColorFilter(getResources().getColor(R.color.grey_60), PorterDuff.Mode.SRC_IN);
        tab_layout.getTabAt(3).getIcon().setColorFilter(getResources().getColor(R.color.grey_60), PorterDuff.Mode.SRC_IN);
      //  tab_layout.getTabAt(4).getIcon().setColorFilter(getResources().getColor(R.color.grey_60), PorterDuff.Mode.SRC_IN);

        tab_layout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tab.getIcon().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
               /* switch (tab.getPosition()) {
                    case 0:
                        toolbar.setTitle("Home");
                        break;
                    case 1:
                        toolbar.setTitle("Explore");
                        break;
                    case 2:
                        toolbar.setTitle("Story");
                        break;
                    case 3:
                        toolbar.setTitle("Activity");
                        break;
                   *//* case 4:
                        toolbar.setTitle("Profile");
                        break;*//*
                }*/

               // ViewAnimation.fadeOutIn(nested_scroll_view);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tab.getIcon().setColorFilter(getResources().getColor(R.color.grey_60), PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


    }
    private void setupViewPager(ViewPager viewPager) {
        viewPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragment(HomeEquipment.newInstance(), "");    // index 0
        viewPagerAdapter.addFragment( MyEquipment.newInstance(), "");   // index 1
        viewPagerAdapter.addFragment( MyOrder.newInstance(), "");    // index 2
        viewPagerAdapter.addFragment( BookedOrder.newInstance(), "");    // index 3
        viewPager.setAdapter(viewPagerAdapter);
    }
    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public SectionsPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        public String getTitle(int position) {
            return mFragmentTitleList.get(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            //return null;
            return mFragmentTitleList.get(position);
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
                                         ActivityCompat.requestPermissions(MainActivity.this,
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
    public void onBackPressed() {
      /*  DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {*/
            if (back_pressed + 2000 > System.currentTimeMillis()) {
                super.onBackPressed();

            } else {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);


                alertDialog.setTitle(R.string.app_name);


                alertDialog.setMessage(getString(R.string.exit_fromapp));


                alertDialog.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                        startActivity(intent);
                        finishAffinity();

                    }
                });


                alertDialog.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.cancel();
                    }
                });

                alertDialog.show();
            }
      //  }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_language) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
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
        if (item.getItemId() == R.id.action_about_us) {
            Intent intent=new Intent(MainActivity.this,AboutUs.class);
            startActivity(intent);
        }
        if (item.getItemId() == R.id.action_profile) {
            Intent intent=new Intent(MainActivity.this,ProfileActivity.class);
            startActivity(intent);
        }

        if (item.getItemId() == R.id.action_share) {
            String shareBody = getString( R.string.app_name ) + getString( R.string.url_app_google_play );
            Intent sharingIntent = new Intent( android.content.Intent.ACTION_SEND );
            sharingIntent.setType( "text/plain" );
            sharingIntent.putExtra( android.content.Intent.EXTRA_TEXT, shareBody );
            sharingIntent.putExtra( Intent.EXTRA_SUBJECT, getString( R.string.app_name ) );
            startActivity( Intent.createChooser( sharingIntent, getResources().getString( R.string.app_name ) ) );
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
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    prefManager.setLogin(false);
                    startActivity(intent);

                    builder.dismiss();

                }
            });
            builder.setView(view);
            builder.setCanceledOnTouchOutside(true);
            builder.show();

        }
        if (item.getItemId() == R.id.action_expertisenumber) {
            final AlertDialog builder=new AlertDialog.Builder(this).create();
            View view= LayoutInflater.from(this).inflate(R.layout.row_expertisenumber,null);
            Button btn_cancel=(Button)view.findViewById(R.id.btn_cancel);
            Button btn_call=(Button)view.findViewById(R.id.btn_call);
            btn_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    builder.dismiss();
                }
            });
            btn_call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (checkPermission()) {

                        Snackbar.make(v, "Permission already granted.", Snackbar.LENGTH_LONG).show();
                        String phone_number = "9874843758";

                        Intent phone_intent = new Intent(Intent.ACTION_DIAL);
                        phone_intent.setData(Uri.parse("tel:" + phone_number));
                        startActivity(phone_intent);

                        builder.dismiss();
                    } else {
                        requestPermission();
                        Snackbar.make(v, "Please request permission.", Snackbar.LENGTH_LONG).show();
                        builder.dismiss();
                    }

                    /*Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:"+phone_number));
                    context.startActivity(intent);*/
                }
            });
            builder.setView(view);
            builder.setCanceledOnTouchOutside(true);
            builder.show();
        }
        if (item.getItemId() == R.id.action_marketprices) {
            final AlertDialog builder=new AlertDialog.Builder(this).create();
            View view= LayoutInflater.from(this).inflate(R.layout.row_marketprice,null);

            AppCompatButton btn_visit=(AppCompatButton) view.findViewById(R.id.btn_visit);

            btn_visit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String url = "https://www.geeksforgeeks.org/";
                    Intent urlIntent = new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(url)
                    );
                    startActivity(urlIntent);

                    builder.dismiss();

                }
            });
            builder.setView(view);
            builder.setCanceledOnTouchOutside(true);
            builder.show();
        }
        if (item.getItemId() == R.id.action_order) {
            Intent intent=new Intent(MainActivity.this,MyOrderActivity.class);
            startActivity(intent);
        }
        else {
            Toast.makeText(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean checkPermission() {
                 int result = ContextCompat.checkSelfPermission(getApplicationContext(), CALL_PHONE);

                 return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
                 ActivityCompat.requestPermissions(this, new String[]{CALL_PHONE}, PERMISSION_REQUEST_CODE);
    }
  /*  @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_equipment) {

            Intent intent=new Intent(MainActivity.this,EquipmentRegisterActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_order) {
            Intent intent=new Intent(MainActivity.this,MyOrderActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_booked_order) {
            Intent intent=new Intent(MainActivity.this,BookedOrderActivity.class);
            startActivity(intent);
        }else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_slideshow) {
            Intent intent=new Intent(MainActivity.this,AboutUs.class);
            startActivity(intent);
        } else  if(id==R.id.nav_logout){
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
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    prefManager.setLogin(false);
                    startActivity(intent);

                    builder.dismiss();

                }
            });
            builder.setView(view);
            builder.setCanceledOnTouchOutside(true);
            builder.show();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
*/

}
