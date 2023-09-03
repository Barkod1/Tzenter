package com.example.necneset;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;

import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kotlin.jvm.internal.TypeReference;

public class InfoBeitCneset extends AppCompatActivity implements View.OnClickListener {
    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;
    DatabaseReference databaseReference;
    List data;
    BeitCneset beitCneset;
    TextView title, details;
    Button btnCancel, btnEdit;
    static String key;
    static Context context;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_beit_cneset);
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("locations");
        this.context = getApplicationContext();
        this.key = getIntent().getStringExtra("key");
        this.btnCancel = findViewById(R.id.btnCancel);

        this.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users/"+firebaseAuth.getCurrentUser().getUid());
                final String[] keyTocancel = new String[1];
                final int[] index = new int[1];
                btnCancel.setClickable(false);
                userRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren()){
                            keyTocancel[0] = ds.getKey();
                            index[0] = ds.getValue(Integer.class);
                            userRef.setValue(1);
                            DatabaseReference locRef = firebaseDatabase.getReference("locations/"+ keyTocancel[0] +"/prayers/"+ index[0]);
                            locRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    Log.d("location וכו", snapshot.toString() + " ");
                                    if(snapshot.child("/coming").exists()) {
                                        int coming = snapshot.child("/coming").getValue(Integer.class) - 1;
                                        locRef.child("/coming").setValue(coming);
                                    }else{
                                        locRef.child("/coming").setValue(0);
                                    }
                                        locRef.removeEventListener(this);

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                            userRef.removeEventListener(this);
                            Log.d("user וכו",  index[0] + " " + keyTocancel[0]);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                Handler handler = new Handler();
                Runnable task = new Runnable() {
                    @Override
                    public void run() {
                        btnCancel.setClickable(true);
                    };

                };
                handler.postDelayed(task, 2000);


            }
        });
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        title = findViewById(R.id.tvNameBeitCneset);
        details = findViewById(R.id.tvDetailsBeitCneset);
        getDataFromCloud(); // replace with your own data retrieval method
        Handler handler = new Handler();
        Runnable task = new Runnable() {
            @Override
            public void run() {
                mAdapter = new MyAdapter(data);
                mRecyclerView.setAdapter(mAdapter);
            }
        };
        handler.postDelayed(task, 500);
        btnEdit = findViewById(R.id.btnEdit);
        databaseReference = firebaseDatabase.getReference("/locations/"+key);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.child("/admin").exists()){
                    databaseReference.removeValue();
                    Intent intent = new Intent(InfoBeitCneset.this, MapsActivity.class);
                    startActivity(intent);
                    databaseReference.removeEventListener(this);
                }
                else
                if(!snapshot.child("/admin").getValue(String.class).equals(firebaseAuth.getCurrentUser().getUid())){

                    ConstraintLayout constraintLayout = findViewById(R.id.cons);
                    constraintLayout.removeView(btnEdit);
                    databaseReference.removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        if(btnEdit != null)
            btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InfoBeitCneset.this, EditLocation.class);
                intent.putExtra("key", key);
                startActivity(intent);
            }
        });

    }

    private void getDataFromCloud() {
        String key = getIntent().getStringExtra("key");
        Log.d("keyyy", key);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("keyyy", key);
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (key.equals(ds.getKey())) {
                        title.setText(ds.child("name").getValue(String.class));
                        details.setText(ds.child("details").getValue(String.class));
                        data = new ArrayList<>();
                        data = ds.child("prayers").getValue(new GenericTypeIndicator<List<Prayer>>() {});
                        return;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public void onClick(View view) {
        String key = getIntent().getStringExtra("key");

        Log.d("keyyy", key);

    }

}