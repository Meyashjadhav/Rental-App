package com.vthree.rentbaseapplication.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.vthree.rentbaseapplication.Adapter.Constants;
import com.vthree.rentbaseapplication.ModelClass.BookingModel;
import com.vthree.rentbaseapplication.ModelClass.EquipmentModel;
import com.vthree.rentbaseapplication.ModelClass.UserModel;
import com.vthree.rentbaseapplication.R;
import com.vthree.rentbaseapplication.preferences.PrefManager;
import com.vthree.rentbaseapplication.service.MySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BookEquipementActivity extends AppCompatActivity {
    EquipmentModel equipmentModel;
    ImageView book_image;
    TextView book_equipment_name, book_equipment_price, book_equipment_dipost, book_equipment_address, book_equipment_contact, select_fromdate,select_todate,
            select_to_time, select_from_time, total_houre;

    Calendar myCalendar;
    byte[] imageAsBytes;
    private int mYear, mMonth, mDay, mHour, mMinute;
    Button place_order;
    String todate,fromdate,todate1,fromdate1, to_time, from_time = "";
    long hours_difference,amount;
    String booking_id;
    DatabaseReference databaseReference;
    PrefManager prefManager;
    String user_id, address, taluka, city, mobile, user_name = null;
    String token;
    FloatingActionButton show_map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_equipement);
        myCalendar = Calendar.getInstance();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        prefManager = new PrefManager(this);
        //  imageAsBytes=intent.getByteArrayExtra("imageAsBytes");
        equipmentModel = (EquipmentModel) intent.getSerializableExtra("equipmentData");
        getSupportActionBar().setTitle(equipmentModel.getEquipment_name());
        book_image = findViewById(R.id.book_image);
        book_equipment_name = findViewById(R.id.book_equipment_name);
        book_equipment_price = findViewById(R.id.book_equipment_price);
        book_equipment_dipost = findViewById(R.id.book_equipment_dipost);
        book_equipment_address = findViewById(R.id.book_equipment_address);
        book_equipment_contact = findViewById(R.id.book_equipment_contact);

        select_fromdate = findViewById(R.id.select_fromdate);
        select_todate = findViewById(R.id.select_todate);

        select_to_time = findViewById(R.id.select_to_time);
        select_from_time = findViewById(R.id.select_from_time);
        total_houre = findViewById(R.id.total_houre);
        place_order = findViewById(R.id.place_order);
        show_map= findViewById(R.id.show_map);

        user_id = prefManager.getString("user_id");
        address = prefManager.getString("address");
        taluka = prefManager.getString("taluka");
        city = prefManager.getString("city");
        mobile = prefManager.getString("mobile");
        user_name = prefManager.getString("user_name");
        databaseReference = FirebaseDatabase.getInstance().getReference("equipmentBooking").child("data");

        if(equipmentModel.getImage()!=null){
            byte[] imageAsBytes = Base64.decode(equipmentModel.getImage().getBytes(), Base64.DEFAULT);

            book_image.setImageBitmap(BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length));

        }else{
            book_image.setImageResource(R.drawable.notavailble);

        }
        show_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getApplicationContext(), EquipLocationActivity.class);
                intent.putExtra("latitude",equipmentModel.getLatitude());
                intent.putExtra("longitude",equipmentModel.getLongitude());
                startActivity(intent);
                finish();
                Log.d("click","click");
            }
        });
        book_equipment_name.setText(equipmentModel.getEquipment_name());
        book_equipment_price.setText(getString(R.string.rs) + equipmentModel.getPriseinhr() + "/-");
        book_equipment_dipost.setText(getString(R.string.deposite_rs)+ equipmentModel.getDeposite() + "/-");
        book_equipment_address.setText(getString(R.string.add) + equipmentModel.getAddress() + ", " + equipmentModel.getCity() + ", " + equipmentModel.getTaluka());
        book_equipment_contact.setText(getString(R.string.cont) + equipmentModel.getContact());
        select_fromdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mYear = myCalendar.get(Calendar.YEAR);
                mMonth = myCalendar.get(Calendar.MONTH);
                mDay = myCalendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(BookEquipementActivity.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {

                                select_fromdate.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                                fromdate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                               // yyyy-MM-dd
                                fromdate1 = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;

                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                datePickerDialog.show();
            }
        });
        select_todate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mYear = myCalendar.get(Calendar.YEAR);
                mMonth = myCalendar.get(Calendar.MONTH);
                mDay = myCalendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(BookEquipementActivity.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {

                                select_todate.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                                todate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                                todate1 = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;

                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                datePickerDialog.show();
            }
        });
        select_from_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int hour = myCalendar.get(Calendar.HOUR_OF_DAY);
                int minute = myCalendar.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(BookEquipementActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        select_from_time.setText(selectedHour + ":" + selectedMinute);
                        from_time = selectedHour + ":" + selectedMinute;
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle(getString(R.string.selecttime));
                mTimePicker.show();
            }
        });
        select_to_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int hour = myCalendar.get(Calendar.HOUR_OF_DAY);
                int minute = myCalendar.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(BookEquipementActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        select_to_time.setText(selectedHour + ":" + selectedMinute);
                        to_time = selectedHour + ":" + selectedMinute;
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle(getString(R.string.selecttime));
                mTimePicker.show();
            }
        });

        place_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BookEquipementActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        Log.d("datass", equipmentModel.getEquipment_name());
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else {
            Toast.makeText(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
    private void sendNotification(JSONObject notification) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Constants.FCM_API, notification,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("responce", "onResponse: " + response.toString());

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(BookEquipementActivity.this, getString(R.string.requesterror), Toast.LENGTH_LONG).show();
                        Log.i("error", "onErrorResponse: Didn't work  " + error.getMessage());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", Constants.serverkey);
                params.put("Content-Type", Constants.contentType);
                return params;
            }
        };
        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
    }


}
