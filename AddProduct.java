package com.vthree.rentbaseapplication.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.vthree.rentbaseapplication.MapsActivity;
import com.vthree.rentbaseapplication.ModelClass.EquipmentModel;
import com.vthree.rentbaseapplication.ModelClass.ProductModel;
import com.vthree.rentbaseapplication.R;
import com.vthree.rentbaseapplication.preferences.PrefManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AddProduct extends AppCompatActivity {

    private EditText editName,editDescription,editTextQuantity,editcity,editTextPincode,editTextPrize;
    Button btn_chooseImage,btn_upload,btn_addProduct,BtnViewProduct;
    ImageView imgview_image;
    //defining firebaseauth object
    private FirebaseAuth firebaseAuth;
    //firebase auth object
    DatabaseReference databaseReference;
    String product_id ="";
    private Uri filePath;
    private final int PICK_IMAGE_REQUEST = 71;
    String imageString;
    //firebase auth object
    Bitmap bitmap1;


    PrefManager prefManager;
    String user_id,farmer_mobile;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);
        getSupportActionBar().hide();

        prefManager=new PrefManager(this);
        user_id=prefManager.getString("user_id");
        farmer_mobile=prefManager.getString("mobile");


        editName=(EditText)findViewById(R.id.editName);
        editDescription=(EditText)findViewById(R.id.editTextDescription);
        editTextQuantity=(EditText)findViewById(R.id.editTextQuantity);
        editcity=(EditText)findViewById(R.id.editTextcityone);
        editTextPincode=(EditText)findViewById(R.id.editTextPincode);
        editTextPrize=(EditText)findViewById(R.id.editTextPrize);


        btn_chooseImage=(Button)findViewById(R.id.btn_ChooseImage);
        btn_upload=(Button)findViewById(R.id.btn_uploadImage);
        btn_addProduct=(Button)findViewById(R.id.buttonRegister);
        BtnViewProduct=(Button)findViewById(R.id.BtnViewProduct);

        imgview_image=(ImageView)findViewById(R.id.imgview_image);



        // storageReference = FirebaseStorage.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Products").child("image");

        btn_addProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Addproduct();

            }
        });
        BtnViewProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               startActivity(new Intent(AddProduct.this, ViewFarmerProducts.class));

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

    void Addproduct()
    {
        //getting email and password from edit texts
        String name = editName.getText().toString().trim();
        String quantity  = editTextQuantity.getText().toString().trim();
        String city  = editcity.getText().toString().trim();
        String description=editDescription.getText().toString().trim();
        String pincode  = editTextPincode.getText().toString().trim();
        String price  =editTextPrize.getText().toString().trim();
        //checking if email and passwords are empty
        if(TextUtils.isEmpty(name)){
            Toast.makeText(this,getString(R.string.enter_prodname),Toast.LENGTH_SHORT).show();
            return;
        }

        if(TextUtils.isEmpty(quantity)){
            Toast.makeText(this,getString(R.string.enter_qty),Toast.LENGTH_SHORT).show();
            return;
        }


        if(TextUtils.isEmpty(city)){
            Toast.makeText(this,getString(R.string.enter_city),Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(description)){
            Toast.makeText(this,getString(R.string.enetr_produdetails),Toast.LENGTH_SHORT).show();
            return;
        }

        if(TextUtils.isEmpty(price)){
            Toast.makeText(this,getString(R.string.enter_price),Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(pincode)){
            Toast.makeText(this,getString(R.string.enter_pincode),Toast.LENGTH_SHORT).show();
            return;
        }
        //if the email and password are not empty
        //displaying a progress dialog

        /*progressDialog.setMessage("Registering Please Wait...");
        progressDialog.show();*/


        product_id = databaseReference.push().getKey();

        databaseReference.child(product_id).setValue(new ProductModel(product_id,name,quantity,user_id,farmer_mobile,description,imageString,price,city,pincode))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(AddProduct.this, getString(R.string.produ_added_sucess), Toast.LENGTH_SHORT).show();
                        Intent intent=new Intent(AddProduct.this,MainActivity.class);
                        startActivity(intent);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AddProduct.this, getString(R.string.produ_added_fail), Toast.LENGTH_SHORT).show();
            }
        });
    }
}