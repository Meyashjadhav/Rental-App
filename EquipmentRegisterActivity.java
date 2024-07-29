package com.vthree.rentbaseapplication.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
/*import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;*/
import com.vthree.rentbaseapplication.MapsActivity;
import com.vthree.rentbaseapplication.ModelClass.EquipmentModel;
import com.vthree.rentbaseapplication.ModelClass.UserModel;
import com.vthree.rentbaseapplication.R;
import com.vthree.rentbaseapplication.preferences.PrefManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class EquipmentRegisterActivity extends AppCompatActivity {

    private EditText editName,editContact,editDescription,editAddress,editcity,editTaluka,editPrizePerhour,editTextDeposite;
    Button btn_chooseImage,btn_upload,btn_addEquipment;
    ImageView currentLocationImage,imgview_image;
    //defining firebaseauth object
    private FirebaseAuth firebaseAuth;
    //firebase auth object
    DatabaseReference databaseReference;
    String equipment_id ="";
    private Uri filePath;
    private final int PICK_IMAGE_REQUEST = 71;
    String imageString;
    //firebase auth object
    Bitmap bitmap1;
  /*  FirebaseStorage storage;
    StorageReference storageReference;*/
    String value;

    PrefManager prefManager;
    String user_id;
    double latitude,longitude,dist;//curr_latitude,curr_longitude;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equipment_register);
        getSupportActionBar().hide();

        prefManager=new PrefManager(this);
        user_id=prefManager.getString("user_id");


        editName=(EditText)findViewById(R.id.editTractorName);
        editContact=(EditText)findViewById(R.id.editTextContact);
        editDescription=(EditText)findViewById(R.id.editTextDescription);
        editAddress=(EditText)findViewById(R.id.editTextAddressdetail);
        editcity=(EditText)findViewById(R.id.editTextcityone);
        editTaluka=(EditText)findViewById(R.id.editTexttalukaone);
        editPrizePerhour=(EditText)findViewById(R.id.editTextPrize);
        editTextDeposite=(EditText)findViewById(R.id.editTextDeposite);
        btn_chooseImage=(Button)findViewById(R.id.btn_ChooseImage);
        btn_upload=(Button)findViewById(R.id.btn_uploadImage);
        btn_addEquipment=(Button)findViewById(R.id.buttonRegister);
        currentLocationImage=(ImageView)findViewById(R.id.currentLocationImage);
        imgview_image=(ImageView)findViewById(R.id.imgview_image);
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
        String description= sharedPreferences.getString("description",null);
        editName.setText(name);
        editContact.setText(contact);
        editDescription.setText(description);


        // storageReference = FirebaseStorage.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("EquipmentDetail").child("image");

        btn_addEquipment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Addequipments();
                sharedPreferences = getApplicationContext().getSharedPreferences("Mydata", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove("name");
                editor.remove("contact");
                editor.remove("description");
                editor.commit();
            }
        });

        btn_chooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });

        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // uploadImage();
            }
        });

        currentLocationImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sharedPreferences = getApplicationContext().getSharedPreferences("Mydata", MODE_PRIVATE);
                editor = sharedPreferences.edit();
                editor.putString("name", editName.getText().toString());
                editor.putString("contact", editContact.getText().toString());
                editor.putString("description", editDescription.getText().toString());
                editor.commit();

                Intent map=new Intent(EquipmentRegisterActivity.this, MapsActivity.class);
                map.putExtra("type","equip");
                startActivity(map);
                finish();
            }
        });


    }





    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_pic)), PICK_IMAGE_REQUEST);
    }

    public String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            try {
                bitmap1 = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
           // Bitmap bitmap = BitmapFactory.decodeFile(bitmap1);
            bitmap1.compress(Bitmap.CompressFormat.JPEG, 60, baos);
            byte[] imageBytes = baos.toByteArray();
             imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            Log.d("Filepath",""+filePath.getUserInfo()+"   i "+filePath.toString()+"  image: "+imageString);

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imgview_image.setImageBitmap(bitmap1);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    void Addequipments()
    {
        Intent intent=new Intent(EquipmentRegisterActivity.this,MainActivity.class);
        startActivity(intent);
    }

   /* public LatLng getLocationFromAddress(Context context, String strAddress) {

        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;

        try {
            // May throw an IOException
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }

            Address location = address.get(0);
            p1 = new LatLng(location.getLatitude(), location.getLongitude() );

        } catch (IOException ex) {

            ex.printStackTrace();
        }

        return p1;
    }*/
}
