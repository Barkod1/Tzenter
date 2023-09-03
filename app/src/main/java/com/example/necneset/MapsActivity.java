package com.example.necneset;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, View.OnClickListener {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 122;
    private GoogleMap mMap;
    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 123;

    private FusedLocationProviderClient fusedLocationClient;
    static double latitude;
    static double longitude;
    private Button addLocation;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private ArrayList<BeitCneset> locations;
    private String key;
    private boolean isClicked = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestLocationPermission();
        setContentView(R.layout.activity_maps);
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("/locations");
        firebaseAuth = FirebaseAuth.getInstance();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        addLocation = (Button) findViewById(R.id.btnAddLocation);
        addLocation.setOnClickListener(this);
        addLocation.setClickable(false);
        DatabaseReference userRef = firebaseDatabase.getReference("/locations");
        isClicked = false;
        userRef.addValueEventListener(new ValueEventListener() {


            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (ds.child("/admin").exists()) {
                        if(ds.child("/admin").getValue(String.class).equals(firebaseAuth.getCurrentUser().getUid())){
                            // change photo
                            addLocation.setText("ערוך את המנייו שהעלתי");
                            key = ds.getKey();
                            addLocation.setClickable(true);
                            addLocation.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                                    if (connectivityManager != null) {
                                        Network network = null;
                                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                            network = connectivityManager.getActiveNetwork();
                                        }
                                        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);

                                        if (networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                                            // The device has an internet connection.
                                            Intent intent = new Intent(getApplicationContext(), EditLocation.class);
                                            intent.putExtra("key",key);
                                            startActivity(intent);
                                        } else {
                                            // The device does not have an internet connection.
                                            Toast.makeText(getApplicationContext(),"נראה שאין לך חיבור לאינטרנט",Toast.LENGTH_LONG).show();
                                        }
                                    }

                                }
                            });


                        }
                    }
                }
                if(!addLocation.getText().toString().equals("ערוך את המנייו שהעלתי")){
                    addLocation.setClickable(true);
                }
                userRef.removeEventListener(this);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });
        if(isClicked)
            addLocation.setOnClickListener(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        try {
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.map_style_json));

            if (!success) {
                Log.e("MapActivity", "Styling parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("MapActivity", "Style resource not found.");
        }
        this.locations = new ArrayList<>();
        ArrayList<Marker> markers = new ArrayList<>();
        ArrayList<String> keys = new ArrayList<>();
        Map<Marker, List<Prayer>> prayersMap = new HashMap<>();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            Log.d("location", location.getLongitude() + " " + location.getLatitude());
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 16));
                        }
                    }
                });
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {

                        if (!ds.child("/admin").exists()) {
                            databaseReference.child(ds.getKey()).removeValue();
                        } else {
                            ArrayList<Prayer> prayers = (ArrayList<Prayer>) ds.child("prayers").getValue(new GenericTypeIndicator<List<Prayer>>() {
                            });
                            LatLng loct = new LatLng(ds.child("latitude").getValue(double.class), ds.child("longitude").getValue(double.class));
                            String details = ds.child("details").getValue(String.class);
                            Log.d("checkcheck", details + " " + loct.toString());
                            if ((int) distance(latitude, longitude, loct.latitude, loct.longitude) > 3000) {
                                return;
                            }
                            Marker marker = mMap.addMarker(new MarkerOptions().position(loct).title(ds.child("name").getValue(String.class)).snippet(details));
                            markers.add(marker);
                            String key = ds.getKey();
                            keys.add(key);
                            prayersMap.put(marker, prayers);
                            googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                                @Override
                                public View getInfoWindow(Marker marker) {
                                    // Return null to use default info window
                                    return null;
                                }

                                @SuppressLint("SetTextI18n")
                                @Override
                                public View getInfoContents(Marker marker) {
                                    // Inflate layout with details
                                    View infoWindow = getLayoutInflater().inflate(R.layout.info_window, null);

                                    @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView titleTextView = infoWindow.findViewById(R.id.title_text_view1);
                                    @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView detailsTextView = infoWindow.findViewById(R.id.details_text_view1);
                                    titleTextView.setText(marker.getTitle());
                                    detailsTextView.setText(marker.getSnippet());
                                    double distanceInMeters = distance(latitude, longitude, marker.getPosition().latitude, marker.getPosition().longitude);
                                    TextView tvMeters = infoWindow.findViewById(R.id.tvDistance);
                                    tvMeters.setText("מרחק: " + distanceInMeters + " מטרים"
                                            + "\n" + getClosestTime((ArrayList<Prayer>) Objects.requireNonNull(prayersMap.get(marker))));
                                    return infoWindow;
                                }
                            });
                        }
                databaseReference.removeEventListener(this);


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Intent intent = new Intent(MapsActivity.this, InfoBeitCneset.class);
                intent.putExtra("name", marker.getTitle());
                intent.putExtra("key", keys.get(markers.indexOf(marker)));
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {


        return false;
    }


    @Override
    public void onClick(View view) {
        if (view == addLocation) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

                return;
            }
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                Network network = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    network = connectivityManager.getActiveNetwork();
                }
                NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);

                if (networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                    fusedLocationClient.getLastLocation()
                            .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    // Got last known location. In some rare situations this can be null.
                                    if (location != null) {
                                        Log.d("location", location.getLongitude() + " " + location.getLatitude());
                                        latitude = location.getLatitude();
                                        longitude = location.getLongitude();
                                    }
                                }
                            });
                    Intent intent = new Intent(this, AddLocation.class);
                    startActivity(intent);
                } else {
                    // The device does not have an internet connection.
                    Toast.makeText(this,"נראה שאין לך חיבור לאינטרנט",Toast.LENGTH_LONG).show();
                }
            }

        }
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissions granted, you can access the location
            } else {
                // Permissions denied, show a message to the user explaining why the permission is required
                Toast.makeText(this, "Location permission is required to use this feature", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public static int distance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371000; // Earth radius in meters
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return (int) (R * c);
    }

    public static String getClosestTime(ArrayList<Prayer> hours) {
        ArrayList<String> hours1 = new ArrayList<>();
        ArrayList<String> names = new ArrayList<>();
        for(Prayer prayer : hours){
            hours1.add(prayer.hour);
            names.add(prayer.name);
        }
        LocalTime now = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            now = LocalTime.now();
        }
        String currentTime = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            currentTime = now.format(DateTimeFormatter.ofPattern("HH:mm"));
        }
        String[] timesWithCurrentTime = new String[hours1.size() + 1];
        hours1.toArray(timesWithCurrentTime);
        timesWithCurrentTime[timesWithCurrentTime.length - 1] = currentTime;


// sort the array from earliest to latest
        Arrays.sort(timesWithCurrentTime, (a, b) -> {
            LocalTime timeA = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                timeA = LocalTime.parse(a, DateTimeFormatter.ofPattern("HH:mm"));
            }
            LocalTime timeB = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                timeB = LocalTime.parse(b, DateTimeFormatter.ofPattern("HH:mm"));
            }
            return timeA.compareTo(timeB);
        });
        for(String hour: timesWithCurrentTime){
            Log.d("time",hour);
        }
// find the index of the hour that comes after the current time
        int index = Arrays.binarySearch(timesWithCurrentTime, currentTime) + 1;
        if (index >= timesWithCurrentTime.length) {
            // current time was not found in the array, get the index of the next hour
            index = 0;
        }

        String name = "";
       for(Prayer prayer: hours){
           if(prayer.hour.equals(timesWithCurrentTime[index])){
               name = prayer.name;
           }
       }
        return "התפילה הקרובה: " + "\n" + name+ ": "+ timesWithCurrentTime[index];
    }
}