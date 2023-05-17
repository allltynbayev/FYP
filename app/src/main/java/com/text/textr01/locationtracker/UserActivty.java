package com.text.textr01.locationtracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class UserActivty extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private double currentLatitude;
    private double currentLongitude;

    private int permissionCode = 1;

    private GoogleMap map;
    private SupportMapFragment supportMapFragment;

    String phone;
    String busid;
    RelativeLayout relativeLayout;
    TextView trackModetv, Taptv;
    String date;
    private FusedLocationProviderClient fusedLocationClient;

    ProgressDialog loadingBar;
    String mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_activty);

        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);
        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;

                // Coordinates for Bishkek
                LatLng Bishkek = new LatLng(42.8746, 74.5698);

                map.moveCamera(CameraUpdateFactory.newLatLngZoom(Bishkek, 13));
                if (ActivityCompat.checkSelfPermission(UserActivty.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(UserActivty.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.

                    return;
                }
                map.setMyLocationEnabled(true);
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(UserActivty.this);

                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(UserActivty.this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                // Got last known location. In some rare situations, this can be null.
                                if (location != null) {
                                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                                }
                            }
                        });

                final DatabaseReference driversRef = FirebaseDatabase.getInstance().getReference().child("Drivers").child(busid);
                driversRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {

                            map.clear(); // Clear previous markers
                            List<LatLng> points = new ArrayList<>();
                            points.add(new LatLng(42.7477, 74.2344)); // San Francisco
                            points.add(new LatLng(41.5477, 76.2344)); // Los Angeles
                            points.add(new LatLng(43.5477, 73.2344));
                            points.add(new LatLng(42.7477, 74.2344));
                            // Add the polyline to the map
                            PolylineOptions polylineOptions = new PolylineOptions().addAll(points).width(10).color(Color.BLUE);
                            map.addPolyline(polylineOptions);

                            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

                            int counter = 0;
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String locationName = snapshot.child("locationname").getValue(String.class);
                                String driverName = snapshot.child("name").getValue(String.class);

                                if (locationName != null) {
                                    double latD = Double.parseDouble(locationName.split(" ")[0]);
                                    double longtD = Double.parseDouble(locationName.split(" ")[1]);

                                    MarkerOptions marker = new MarkerOptions().position(new LatLng(latD, longtD)).title("Bus Driver " + driverName);
                                    map.addMarker(marker);

                                    counter++;


                                    boundsBuilder.include(new LatLng(latD, longtD));
                                }
                            }
                            // bounds is not empty
                            LatLngBounds bounds = boundsBuilder.build();

                            int padding = 100; // Adjust as needed
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                            map.animateCamera(cameraUpdate);

                        } else {

                            Toast.makeText(UserActivty.this, "Could not find BUS ID", Toast.LENGTH_SHORT).show();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle error
                    }
                });
            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                // The next two lines tell the new client that “this” current class will handle connection stuff
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                //fourth line adds the LocationServices API endpoint from GooglePlayServices
                .addApi(LocationServices.API)
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds


        Intent i = getIntent();
        phone = i.getStringExtra("phone");
        busid = i.getStringExtra("busid");

        loadingBar = new ProgressDialog(this);

        loadingBar.setTitle("Openning..");
        loadingBar.setMessage("Please wait...");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        trackModetv = (TextView) findViewById(R.id.trackmodetext);
        Taptv = (TextView) findViewById(R.id.tap);

        relativeLayout = (RelativeLayout) findViewById(R.id.userlayout);

        final DatabaseReference RootRef;
        RootRef = FirebaseDatabase.getInstance().getReference();

        RootRef.child("Users").child(phone).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mode = dataSnapshot.child("mode").getValue().toString();

                if (mode.equals("on")) {
                    loadingBar.dismiss();
                    relativeLayout.setBackgroundResource(R.drawable.green);
                    trackModetv.setText("Track Mode is On");
                    Taptv.setText("Tap On Screen to Turn Off");

                } else if (mode.equals("off")) {
                    loadingBar.dismiss();
                    relativeLayout.setBackgroundResource(R.drawable.red);
                    trackModetv.setText("Track Mode is off");
                    Taptv.setText("Tap On Screen to Turn On");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        final LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }

                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        currentLatitude = location.getLatitude();
                        currentLongitude = location.getLongitude();
                        RootRef.child("Drivers").child(busid).child(phone).child("mode").setValue("off");

                        Toast.makeText(UserActivty.this, currentLatitude + " WORKS " + currentLongitude + "", Toast.LENGTH_LONG).show();
                        saveLocationToDB(currentLatitude, currentLongitude);
                    }
                }
            }
        };
        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mode.equals("on")) {
                    if (ActivityCompat.checkSelfPermission(UserActivty.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(UserActivty.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    RootRef.child("Drivers").child(busid).child(phone).child("mode").setValue("off");
                    RootRef.child("Users").child(phone).child("mode").setValue("off");
                    fusedLocationClient.removeLocationUpdates(locationCallback);

//                    RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//
//                            RootRef.child("Users").child(phone).child("mode").setValue("off");
//                            RootRef.child("Drivers").child(busid).child(phone).child("mode").setValue("off");
//
//                            String currentDateTimeString = java.text.DateFormat.getDateTimeInstance().format(new Date());
//
//                            RootRef.child("Users").child(phone).child(date).child("modeoffdatetime").setValue(currentDateTimeString);
//                            RootRef.child("Drivers").child(busid).child(phone).child("modeoffdatetime").setValue(currentDateTimeString);
//
//
//                            if (ActivityCompat.checkSelfPermission(UserActivty.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(UserActivty.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//
//                                return;
//                            }
//                            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//
//                            if (location == null) {
//                                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, UserActivty.this);
//
//                            } else {
//                                currentLatitude = location.getLatitude();
//                                currentLongitude = location.getLongitude();
//
//                                Toast.makeText(UserActivty.this, currentLatitude + " WORKS " + currentLongitude + "", Toast.LENGTH_LONG).show();
//
//                                saveLocationToDB(currentLatitude, currentLongitude);
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                        }
//                    });
                } else if (mode.equals("off")) {

                    RootRef.child("Users").child(phone).child("mode").setValue("on");
                    RootRef.child("Drivers").child(busid).child(phone).child("mode").setValue("on");
                    // Request location updates
                    LocationRequest locationRequest = LocationRequest.create();
                    locationRequest.setInterval(5000);  // Set desired interval for active location updates, in milliseconds.
                    locationRequest.setFastestInterval(5000);  // Set the fastest rate for active location updates, in milliseconds.
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());


//                    RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//
//                            RootRef.child("Users").child(phone).child("mode").setValue("on");
//                            RootRef.child("Drivers").child(busid).child(phone).child("mode").setValue("on");
//
//                            date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
//                            String currentDateTimeString = java.text.DateFormat.getDateTimeInstance().format(new Date());
//
//                            HashMap<String, Object> userDataMap = new HashMap<>();
//                            userDataMap.put("modeondatetime", currentDateTimeString);
//                            userDataMap.put("locationname", " ");
//                            userDataMap.put("modeoffdatetime", "");
//
//                            RootRef.child("Users").child(phone).child(date).updateChildren(userDataMap);
//                            RootRef.child("Drivers").child(busid).child(phone).updateChildren(userDataMap);
//                            if (ActivityCompat.checkSelfPermission(UserActivty.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(UserActivty.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//
//                                return;
//                            }
//                            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//
//                            if (location == null) {
//                                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, UserActivty.this);
//
//                            } else {
//                                currentLatitude = location.getLatitude();
//                                currentLongitude = location.getLongitude();
//
//                                Toast.makeText(UserActivty.this, currentLatitude + " WORKS " + currentLongitude + "", Toast.LENGTH_LONG).show();
//
//                                saveLocationToDB(currentLatitude, currentLongitude);
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                        }
//                    });
                }

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.logout2:

                SharedPreferences sharedPreferences
                        = getSharedPreferences("MySharedPref",
                        MODE_PRIVATE);

                SharedPreferences.Editor myEdit
                        = sharedPreferences.edit();

                myEdit.putString(
                        "active",
                        "newuser");
                myEdit.commit();
                Intent intent = new Intent(UserActivty.this, MainActivity.class);
                startActivity(intent);
                return true;

            case R.id.updatepassword2:
                Intent intent2 = new Intent(UserActivty.this, UserUpdatePassword.class);
                intent2.putExtra("phone", phone);
                startActivity(intent2);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        //Now lets connect to the API
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(this.getClass().getSimpleName(), "onPause()");

        //Disconnect from API onPause()
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }


    }

    /**
     * If connected get lat and long
     *
     */
    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            AlertDialog.Builder builder = new AlertDialog.Builder(UserActivty.this);
            builder.setTitle("Grant permissions");
            builder.setMessage("please give ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION permission");
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ActivityCompat.requestPermissions(
                            UserActivty.this,
                            new String[]{
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                            },
                            permissionCode
                    );
                }
            })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {


                            Toast.makeText(UserActivty.this, "Please give permission to use this application..", Toast.LENGTH_SHORT).show();

                            ActivityCompat.requestPermissions(
                                    UserActivty.this,
                                    new String[]{
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                    },
                                    permissionCode
                            );
                        }
                    })
                    .create();
            builder.show();

        } else {

            final DatabaseReference RootRef;
            RootRef = FirebaseDatabase.getInstance().getReference();

            RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child("Users").child(phone).child("mode").getValue().equals("on")) {

                        if (ActivityCompat.checkSelfPermission(UserActivty.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(UserActivty.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                        if (location == null) {
                            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, UserActivty.this);

                        } else {
                            //If everything went fine lets get latitude and longitude
                            currentLatitude = location.getLatitude();
                            currentLongitude = location.getLongitude();

                            Toast.makeText(UserActivty.this, currentLatitude + " WORKS " + currentLongitude + "", Toast.LENGTH_LONG).show();

                            saveLocationToDB(currentLatitude,currentLongitude);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });



        }

    }

    private void saveLocationToDB(double currentLatitude, double currentLongitude) {
        final DatabaseReference RootRef;
        RootRef = FirebaseDatabase.getInstance().getReference();

        date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
        String currentDateTimeString = java.text.DateFormat.getDateTimeInstance().format(new Date());

        HashMap<String, Object> userDataMap = new HashMap<>();
        userDataMap.put("locationname", String.valueOf(currentLatitude)+" "+String.valueOf(currentLongitude));

        RootRef.child("Users").child(phone).child(date).updateChildren(userDataMap);
        RootRef.child("Drivers").child(busid).child(phone).updateChildren(userDataMap);

    }


    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            Log.e("Error", "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    /**
     * If locationChanges change lat and long
     *
     *
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();

        Toast.makeText(this, currentLatitude + " WORKS " + currentLongitude + "", Toast.LENGTH_LONG).show();

        saveLocationToDB(currentLatitude,currentLongitude);
    }

}
