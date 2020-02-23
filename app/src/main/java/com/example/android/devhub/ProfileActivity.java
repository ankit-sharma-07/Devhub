package com.example.android.devhub;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Layout;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.example.android.devhub.db.EditProfileActivity;
import com.example.android.devhub.db.Userinformation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;


import android.content.DialogInterface;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.regex.Pattern;

public class ProfileActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private TextView profileNameTextView, profilePhonenoTextView;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private ImageView profilePicImageView;
    private FirebaseStorage firebaseStorage;
    private TextView textViewemailname, desc, skill, linkedin, github;
    private EditText editTextName;
    Button pdf;
    ProgressDialog p;
    Uri imagePath;
    private static int PICK_IMAGE = 123;
    ImageView editDesc, editSkills, editMail, editPhone, editLinkedin, editGithub;

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data.getData() != null) {
            imagePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagePath);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                profilePicImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);

        if (imagePath != null) {
            p = new ProgressDialog(this);
            p.setMessage("uploading....");
            p.show();
            sendUserData();
        }
    }

    private void sendUserData() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

        // Get "User UID" from Firebase > Authentification > Users.
        profilePicImageView.setDrawingCacheEnabled(true);
        profilePicImageView.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) profilePicImageView.getDrawable()).getBitmap();
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
                Toast.makeText(getApplicationContext(), "Error: Uploading profile picture", Toast.LENGTH_SHORT).show();

            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                p.dismiss();
                Toast.makeText(getApplicationContext(), "Profile uploaded", Toast.LENGTH_SHORT).show();
            }
        });

    }


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        final ProgressDialog dialog;
        dialog = new ProgressDialog(this);
        dialog.show();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        editTextName = (EditText) findViewById(R.id.et_username);
        profilePicImageView = findViewById(R.id.profile_pic_imageView);
        profileNameTextView = findViewById(R.id.profile_name_textView);
        profilePhonenoTextView = findViewById(R.id.profile_phoneno_textView);
        editDesc = (ImageView) findViewById(R.id.edit_desc);
        editSkills = (ImageView) findViewById(R.id.edit_skills);
        editPhone = (ImageView) findViewById(R.id.editPhone);
        editLinkedin = (ImageView) findViewById(R.id.editLinkedin);
        editGithub = (ImageView) findViewById(R.id.editGithub);
        desc = findViewById(R.id.profile_desc);
        skill = findViewById(R.id.profile_skills);
        linkedin = findViewById(R.id.profile_linkedin);
        github = findViewById(R.id.profile_github);
        //pdf=(Button)findViewById(R.id.btn_pdf);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        databaseReference = firebaseDatabase.getReference(firebaseAuth.getUid());
        storageReference = firebaseStorage.getReference();
        // Get the image stored on Firebase via "User id/Images/Profile Pic.jpg".
        storageReference.child(firebaseAuth.getUid()).child("Images").child("Profile Pic").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Using "Picasso" (http://square.github.io/picasso/) after adding the dependency in the Gradle.
                // ".fit().centerInside()" fits the entire image into the specified area.
                // Finally, add "READ" and "WRITE" external storage permissions in the Manifest.
                Picasso.get().load(uri).fit().centerInside().into(profilePicImageView);
            }
        });
        if (firebaseAuth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        }

        final FirebaseUser user = firebaseAuth.getCurrentUser();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Userinformation userProfile = dataSnapshot.getValue(Userinformation.class);
                if (userProfile != null) {
                    profileNameTextView.setText(userProfile.getUserName());
                    profilePhonenoTextView.setText(userProfile.getUserPhoneno());
                    textViewemailname = (TextView) findViewById(R.id.textViewEmailAdress);
                    textViewemailname.setText(user.getEmail());
                    desc.setText(userProfile.getDesc());
                    skill.setText(userProfile.getSkills());
                    linkedin.setText(userProfile.getLinkedIn());
                    github.setText(userProfile.getGithub());
                }
                dialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ProfileActivity.this, databaseError.getCode(), Toast.LENGTH_SHORT).show();
            }
        });
        editDesc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonClickedEditDescription(view);
            }
        });
        editSkills.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonClickedEditSkills(view);
            }
        });
        editPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonClickedEditPhoneNo(view);
            }
        });
        editLinkedin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonClickedEditLinkedin(view);
            }
        });
        editGithub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonClickedEditGithub(view);
            }
        });

        profileNameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonClickedEditName(view);
            }
        });
        linkedin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.linkedin.com/in/" + linkedin.getText().toString())));
            }
        });
        github.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.github.com/" + github.getText().toString())));
            }
        });
        profilePicImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent profileIntent = new Intent();
                profileIntent.setType("image/*");
                profileIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(profileIntent, "Select Image."), PICK_IMAGE);
            }
        });
        profilePhonenoTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + profilePhonenoTextView.getText().toString())));
            }
        });

    }

    public void buttonClickedEditName(View view) {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.layout_custom_dialog_edit_name, null);
        final EditText etUsername = alertLayout.findViewById(R.id.et_username);
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Edit Name");
        // this is set the view from XML inside AlertDialog
        alert.setView(alertLayout);
        // disallow cancel of AlertDialog on click of back button and outside touch
        alert.setCancelable(false);
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = etUsername.getText().toString();
                FirebaseUser user = firebaseAuth.getCurrentUser();
                databaseReference.child("name").setValue(name);
                etUsername.onEditorAction(EditorInfo.IME_ACTION_DONE);
            }
        });
        AlertDialog dialog = alert.create();
        dialog.show();
    }

    public void buttonClickedEditDescription(View view) {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.layout_custom_dialog_edit_name, null);
        final EditText etUserSurname = alertLayout.findViewById(R.id.et_username);
        etUserSurname.setText(desc.getText().toString());
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Edit Description");
        // this is set the view from XML inside AlertDialog
        alert.setView(alertLayout);
        // disallow cancel of AlertDialog on click of back button and outside touch
        alert.setCancelable(false);
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String description = etUserSurname.getText().toString();
                databaseReference.child("desc").setValue(description);
                etUserSurname.onEditorAction(EditorInfo.IME_ACTION_DONE);
            }
        });
        AlertDialog dialog = alert.create();
        dialog.show();
    }

    public void buttonClickedEditSkills(View view) {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.layout_custom_dialog_edit_name, null);
        final EditText etUserSurname = alertLayout.findViewById(R.id.et_username);
        etUserSurname.setText(skill.getText().toString());
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Edit Skills");
        // this is set the view from XML inside AlertDialog
        alert.setView(alertLayout);
        // disallow cancel of AlertDialog on click of back button and outside touch
        alert.setCancelable(false);
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String skill = etUserSurname.getText().toString();
                databaseReference.child("skills").setValue(skill);
                etUserSurname.onEditorAction(EditorInfo.IME_ACTION_DONE);
            }
        });
        AlertDialog dialog = alert.create();
        dialog.show();
    }

    public void buttonClickedEditPhoneNo(View view) {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.layout_custom_dialog_edit_name, null);
        final EditText etUserPhoneno = alertLayout.findViewById(R.id.et_username);
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Phone No Edit");
        // this is set the view from XML inside AlertDialog
        alert.setView(alertLayout);
        // disallow cancel of AlertDialog on click of back button and outside touch
        alert.setCancelable(false);
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String phoneno = etUserPhoneno.getText().toString();
                databaseReference.child("phoneno").setValue(phoneno);
                etUserPhoneno.onEditorAction(EditorInfo.IME_ACTION_DONE);
            }
        });
        AlertDialog dialog = alert.create();
        dialog.show();
    }

    public void buttonClickedEditLinkedin(View view) {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.layout_custom_dialog_edit_name, null);
        final EditText etUsername = alertLayout.findViewById(R.id.et_username);
        etUsername.setText(linkedin.getText().toString());
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Edit Linkedin username");
        // this is set the view from XML inside AlertDialog
        alert.setView(alertLayout);
        // disallow cancel of AlertDialog on click of back button and outside touch
        alert.setCancelable(false);
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = etUsername.getText().toString();
                boolean l = Pattern.matches("[A-z0-9_-]+", name);
                if (!l && name.length() != 0) {
                    Toast.makeText(getApplicationContext(), "Enter valid linkedin username", Toast.LENGTH_SHORT).show();
                } else {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    databaseReference.child("linkedIn").setValue(name);
                    etUsername.onEditorAction(EditorInfo.IME_ACTION_DONE);
                }
            }
        });
        AlertDialog dialog = alert.create();
        dialog.show();
    }

    public void buttonClickedEditGithub(View view) {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.layout_custom_dialog_edit_name, null);
        final EditText etUsername = alertLayout.findViewById(R.id.et_username);
        etUsername.setText(github.getText().toString());
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Edit Github username");
        // this is set the view from XML inside AlertDialog
        alert.setView(alertLayout);
        // disallow cancel of AlertDialog on click of back button and outside touch
        alert.setCancelable(false);
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = etUsername.getText().toString();
                boolean l = Pattern.matches("[A-z0-9_-]+", name);
                if (!l && name.length() != 0) {
                    Toast.makeText(getApplicationContext(), "Enter valid Github username", Toast.LENGTH_SHORT).show();
                } else {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    databaseReference.child("github").setValue(name);
                    etUsername.onEditorAction(EditorInfo.IME_ACTION_DONE);
                }
            }
        });
        AlertDialog dialog = alert.create();
        dialog.show();
    }


    public void navigateLogOut(View v) {
        FirebaseAuth.getInstance().signOut();
        Intent inent = new Intent(this, MainActivity.class);
        startActivity(inent);
    }
}