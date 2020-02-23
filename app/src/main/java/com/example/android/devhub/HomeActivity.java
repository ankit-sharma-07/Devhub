package com.example.android.devhub;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {
    private FirebaseStorage firebaseStorage;
    FirebaseAuth firebaseAuth;
    ProgressDialog p;
    private StorageReference storageReference;
    int f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        firebaseAuth = FirebaseAuth.getInstance();
        p = new ProgressDialog(HomeActivity.this);
        if (firebaseAuth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        }
        BottomNavigationView bottomNavigationView = (BottomNavigationView)
                findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {

                            case R.id.action_profile:
                                Intent inentProfile = new Intent(HomeActivity.this, ProfileActivity.class);
                                startActivity(inentProfile);
                                break;
                        }
                        return true;
                    }
                });
        SearchView searchView = (SearchView) findViewById(R.id.search);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String s) {
                DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
                f = 0;
                final String se = s;
                p.setMessage("searching...");
                p.show();
                ValueEventListener eventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            String name = ds.child("email").getValue(String.class);
                            String id = ds.child("id").getValue(String.class);
                            if (name.equals(se)) {
                                f = 1;
                                p.dismiss();
                                //String  id=ds.child("..").getValue(String.class);
                                Toast.makeText(getApplicationContext(), "user found ", Toast.LENGTH_SHORT).show();
                                Intent i = new Intent(getApplicationContext(), GuestProfileActivity.class);
                                i.putExtra("id", id);
                                i.putExtra("name", ds.child("name").getValue(String.class));
                                i.putExtra("email", name);
                                i.putExtra("desc", ds.child("desc").getValue(String.class));
                                i.putExtra("github", ds.child("github").getValue(String.class));
                                i.putExtra("linkedIn", ds.child("linkedIn").getValue(String.class));
                                i.putExtra("phone", ds.child("phoneno").getValue(String.class));
                                i.putExtra("skills", ds.child("skills").getValue(String.class));
                                startActivity(i);
                                break;

                            }
                        }
                        if (f == 0)
                        { p.dismiss();
                            Toast.makeText(getApplicationContext(), "user not found", Toast.LENGTH_SHORT).show();}

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                };
                rootRef.addListenerForSingleValueEvent(eventListener);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

}