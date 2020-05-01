package com.example.collegeconnect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.example.collegeconnect.adapters.SettingsAdapter;
import com.example.collegeconnect.datamodels.SaveSharedPreference;
import com.example.collegeconnect.settingsactivity.HomeEditActivity;
import com.example.collegeconnect.ui.home.HomeFragment;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    private DatabaseHelper db;
    GoogleSignInClient mgoogleSignInClient;
    private Button logout;
    private RecyclerView recyclerView;
    private SettingsAdapter settingsAdapter;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef;
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference;
    CircleImageView prfileImage;
    TextDrawable drawable;
    TextView nameField;
    Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        storageRef = storage.getReference();
        Toolbar toolbar = findViewById(R.id.settingbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.newBlue), PorterDuff.Mode.SRC_ATOP);
        db = new DatabaseHelper(this);

        prfileImage = findViewById(R.id.settings_dp);
        nameField = findViewById(R.id.textView16);
        String name = SaveSharedPreference.getUser(this);
        nameField.setText(name);
        File file = new File("/storage/emulated/0/Android/data/"+ BuildConfig.APPLICATION_ID+"/files/Display Picture/dp.jpeg");
        if(file.exists()) {
            SettingsActivity.this.uri = Uri.fromFile(file);
            Picasso.get().load(uri).into(prfileImage);
        }
        else {
            try {
                int space = name.indexOf(" ");
                int color = navigation.generatecolor();
                drawable = TextDrawable.builder().beginConfig()
                        .width(150)
                        .height(150)
                        .bold()
                        .endConfig()
                        .buildRound(name.substring(0, 1) + name.substring(space + 1, space + 2), color);
                prfileImage.setImageDrawable(drawable);
            }
            catch (Exception e){

            }

        }
        ArrayList<String> options= new ArrayList<>();
//        options.add("Update Profile");
        options.add("Theme");
        options.add("My Upload List");
        options.add("Contact Us");
        options.add("About");

        recyclerView = findViewById(R.id.settings_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        settingsAdapter = new SettingsAdapter(options, this);
        recyclerView.setAdapter(settingsAdapter);
        DividerItemDecoration decoration = new DividerItemDecoration(this,DividerItemDecoration.VERTICAL_LIST,80,0);
        recyclerView.addItemDecoration(decoration);
        findViewById(R.id.profile_card).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, HomeEditActivity.class));
            }
        });

        logout = findViewById(R.id.logoutButton);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logOutDialog();
            }
        });
//        loadFragment(fragment);
    }


    public void logOutDialog(){
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        // Setting Dialog Title
        builder.setTitle("Confirm SignOut");
        // Setting Dialog Message
        builder.setMessage("Are you sure you want to Signout?\nAll your saved data wil be lost!");

        builder.setPositiveButton("Logout",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here to execute after dialog

                        FirebaseAuth.getInstance().signOut();
                        signOut();
                        Intent i = new Intent(SettingsActivity.this, MainActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        db.deleteall();
                        SaveSharedPreference.clearUserName(SettingsActivity.this);

                        startActivity(i);
                        finish();
                    }
                });

        // Setting Negative "NO" Btn
        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    private void signOut() {
        mgoogleSignInClient.signOut();
    }

    @Override
    public void onStart() {
        super.onStart();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mgoogleSignInClient = GoogleSignIn.getClient(this,gso);

    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

//    private boolean loadFragment(Fragment fragment)
//    {
//        if (fragment!=null)
//        {
//            Log.d("Settings", "loadFragmentsInSettings: Frag is loaded");
//            getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.settings_frag_container,fragment)
//                    .commit();
//
//            return true;
//        }
//        return false;
//    }
}
