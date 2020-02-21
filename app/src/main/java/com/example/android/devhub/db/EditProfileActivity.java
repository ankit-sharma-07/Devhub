package com.example.android.devhub.db;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.devhub.HomeActivity;
import com.example.android.devhub.LoginActivity;
import com.example.android.devhub.ProfileActivity;
import com.example.android.devhub.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iceteck.silicompressorr.SiliCompressor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.internal.Util;

public class EditProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = EditProfileActivity.class.getSimpleName();
    Button btnsave;
    private FirebaseAuth firebaseAuth;
    private TextView textViewemailname;
    private DatabaseReference databaseReference;
    private EditText editTextName;
    private EditText editTextPhoneNo, editTextDesc, editTextSkill, editTextLinkedin, editTextGithub;
    private ImageView profileImageView;
    private FirebaseStorage firebaseStorage;
    private FirebaseUser user;
    private static int PICK_IMAGE = 123;
    Uri imagePath;
    ProgressDialog p;
    private StorageReference storageReference;


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data.getData() != null) {
            imagePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagePath);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                profileImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        }

        editTextName = (EditText) findViewById(R.id.EditTextName);
        editTextPhoneNo = (EditText) findViewById(R.id.EditTextPhoneNo);
        editTextDesc = (EditText) findViewById(R.id.EditTextDesc);
        editTextSkill = (EditText) findViewById(R.id.EditTextSkill);
        editTextLinkedin = (EditText) findViewById(R.id.EditTextlinkedIn);
        editTextGithub = (EditText) findViewById(R.id.EditTextGithub);
        btnsave = (Button) findViewById(R.id.btnSaveButton);
        user = firebaseAuth.getCurrentUser();
        btnsave.setOnClickListener(this);
        textViewemailname = (TextView) findViewById(R.id.textViewEmailAdress);
        textViewemailname.setText(user.getEmail());
        profileImageView = findViewById(R.id.update_imageView);
        firebaseStorage = FirebaseStorage.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = firebaseStorage.getReference();
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent();
                profileIntent.setType("image/*");
                profileIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(profileIntent, "Select Image."), PICK_IMAGE);
            }
        });
    }

    private void userInformation() {
        String name = editTextName.getText().toString().trim();
        String phoneno = editTextPhoneNo.getText().toString().trim();
        String desc = editTextDesc.getText().toString().trim();
        String skill = editTextSkill.getText().toString().trim();
        String link = editTextLinkedin.getText().toString().trim();
        String github = editTextGithub.getText().toString().trim();
        Userinformation userinformation = new Userinformation(name, phoneno, desc, skill, link, github);
        FirebaseUser user = firebaseAuth.getCurrentUser();
        databaseReference.child(user.getUid()).setValue(userinformation);

    }


    @Override
    public void onClick(View view) {
        if (view == btnsave) {
            boolean l= Pattern.matches("http(s)?:\\/\\/([\\w]+\\.)?linkedin\\.com\\/in\\/[A-z0-9_-]+\\/?",editTextLinkedin.getText().toString());
            boolean g=Pattern.matches("http(s)?:\\/\\/(www\\.)?github\\.com\\/[A-z0-9_-]+\\/?",editTextGithub.getText().toString());
            if(editTextName.getText().toString().length()==0)
            {
                Toast.makeText(getApplicationContext(),"Please enter name",Toast.LENGTH_SHORT).show();
            }
            else if(!l && editTextLinkedin.getText().toString().length()!=0)
            {
                Toast.makeText(getApplicationContext(),"Enter valid linkedin profile url",Toast.LENGTH_SHORT).show();
            }
            else if(!g && editTextGithub.getText().toString().length()!=0)
            {
                Toast.makeText(getApplicationContext(),"Enter valid Github profile url",Toast.LENGTH_SHORT).show();
            }
            else{
            p=new ProgressDialog(this);
            p.setMessage("uploading....");
            p.show();
            if (imagePath == null) {

                Drawable drawable = this.getResources().getDrawable(R.drawable.defavatar);
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.defavatar);
                userInformation();
                sendUserData();
                finish();
                startActivity(new Intent(EditProfileActivity.this, ProfileActivity.class));
            } else {
                userInformation();
                sendUserData();

            }
        }}
    }

    private void sendUserData() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

        // Get "User UID" from Firebase > Authentification > Users.
        profileImageView.setDrawingCacheEnabled(true);
        profileImageView.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) profileImageView.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        DatabaseReference databaseReference = firebaseDatabase.getReference(firebaseAuth.getUid());
        StorageReference imageReference = storageReference.child(firebaseAuth.getUid()).child("Images").child("Profile Pic"); //User id/Images/Profile Pic.jpg
        UploadTask uploadTask = imageReference.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                p.dismiss();
                Toast.makeText(EditProfileActivity.this, "Error: Uploading profile picture", Toast.LENGTH_SHORT).show();

            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                p.dismiss();
                Toast.makeText(EditProfileActivity.this, "Profile uploaded", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(EditProfileActivity.this, ProfileActivity.class));
            }
        });

    }

}
