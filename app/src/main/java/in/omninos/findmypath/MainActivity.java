package in.omninos.findmypath;


import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends ActionBarActivity implements LocationListener {

    private static final String TAG = "LocationActivity";
    private static final long INTERVAL = 1000 * 60 * 1; //1 minute
    private static final long FASTEST_INTERVAL = 1000 * 60 * 1; // 1 minute

    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    String mapStartTime;
    private LocationManager locationManager;
    private Handler handler;
    private Location location;
    private GoogleMap googleMap;

    ImageView menu;
    NavigationView navigationView;
    DrawerLayout drawerLayout;
    ImageView close_drawer, logout;
    private TextView startTime, timer, distance;
    private long myStartTime, countUp;
    private double dist;

    private double latitude;
    private double longitude;

    private ToggleButton start;
    private String asText;

    private ArrayList<ArrayListModel> locationArrayList = new ArrayList<>();

    double firstLatitude, firstLongitude, startLatitude, startLongitude, currentLatitude, currentLongitude;

    ArrayListModel firstModel, startModel, currentModel;
    private MarkerOptions marker;
    private Polyline myPolyline;
    private Chronometer stopWatch;
    private final int MY_PERMISSIONS_REQUEST_LOCATION = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        init();

        try {
            initilizeMap();
        } catch (Exception e) {
            e.printStackTrace();
        }

        startMyLocation();
        drawMarker();
        start = (ToggleButton) findViewById(R.id.toggleButton);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (start.isChecked()) {
                    setTimer();
                    startLocationUpdates();
                } else if (!start.isChecked()) {
                    stopWatch.stop();
                    stopWatch.clearComposingText();
                }
            }
        });
    }

    private void init() {
        menu = (ImageView) findViewById(R.id.map_menu);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerlayout);
        navigationView = (NavigationView) findViewById(R.id.navigation);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(navigationView);

            }
        });

        View headerLayout = navigationView.getHeaderView(0);
        close_drawer = (ImageView) headerLayout.findViewById(R.id.close_drawer);
        logout = (ImageView) headerLayout.findViewById(R.id.logout);

        close_drawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawer(navigationView);
            }
        });

        startTime = (TextView) findViewById(R.id.start_time);
        timer = (TextView) findViewById(R.id.timer);
        distance = (TextView) findViewById(R.id.distance);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                if (item.getItemId() == R.id.home) {

                    Toast.makeText(MainActivity.this, "welcome", Toast.LENGTH_SHORT).show();

                }
                if (item.getItemId() == R.id.inbox) {

                    Toast.makeText(MainActivity.this, "welcome", Toast.LENGTH_SHORT).show();

                }
                if (item.getItemId() == R.id.notification) {

                    Toast.makeText(MainActivity.this, "welcome", Toast.LENGTH_SHORT).show();


                }
                if (item.getItemId() == R.id.favrioute) {
                    Toast.makeText(MainActivity.this, "welcome", Toast.LENGTH_SHORT).show();


                }
                if (item.getItemId() == R.id.postJob) {
                    Toast.makeText(MainActivity.this, "welcome", Toast.LENGTH_SHORT).show();


                }
                if (item.getItemId() == R.id.aboutApp) {
                    Toast.makeText(MainActivity.this, "welcome", Toast.LENGTH_SHORT).show();


                }
                if (item.getItemId() == R.id.profile) {
                    Toast.makeText(MainActivity.this, "welcome", Toast.LENGTH_SHORT).show();

                }
                if (item.getItemId() == R.id.closeApp) {
                    finish();

                }
                if (item.getItemId() == R.id.leavefeedback) {
                    Toast.makeText(MainActivity.this, "welcome", Toast.LENGTH_SHORT).show();

                }


                return true;

            }
        });
    }

    private void startLocationUpdates() {
        mapStartTime = DateFormat.getTimeInstance().format(new Date());
        startTime.setText(mapStartTime.toString());
        Log.d(TAG, "Location update started ..............: ");
    }


    private void setTimer() {
        stopWatch = (Chronometer) findViewById(R.id.chrono);
        myStartTime = SystemClock.elapsedRealtime();

        stopWatch.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer arg0) {
                countUp = (SystemClock.elapsedRealtime() - arg0.getBase()) / 1000;
                asText = (countUp / 60) + ":" + (countUp % 60);
                timer.setText(asText);
            }
        });

        stopWatch.start();

    }


    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515 * 1.609344 * 1000;
        Log.e("Distance", "" + dist);
        distance.setText(String.valueOf(dist));
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    public boolean checkLocationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                    // Show an expanation to the user asynchronously -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                    //Prompt the user once explanation has been shown
                    ActivityCompat.requestPermissions(this,
                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                            MY_PERMISSIONS_REQUEST_LOCATION);


                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(this,
                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                            MY_PERMISSIONS_REQUEST_LOCATION);
                }
                return false;
            }
        } else {
            return true;
        }

        return true;
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {


                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void startMyLocation() {
        handler = new Handler();


        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
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
        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }

        handler.postDelayed(runLocation, 1000);
    }

    public Runnable runLocation = new Runnable() {
        @Override
        public void run() {
            MainActivity.this.handler.postDelayed(MainActivity.this.runLocation, 10000);

            addLocation();

        }
    };

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        distance(firstLatitude, firstLongitude, currentLatitude, currentLongitude);

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void initilizeMap() {
        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                    R.id.map)).getMap();

            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(getApplicationContext(),
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initilizeMap();
    }

    private void drawMarker() {
        // create marker
        marker = new MarkerOptions().position(new LatLng(latitude, longitude)).title("Hello Maps ");
        googleMap.addMarker(marker);

        CameraPosition cameraPosition = new CameraPosition.Builder().target(
                new LatLng(latitude, longitude)).zoom(18).build();

        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void addLocation() {

        ArrayListModel arrayListModel = new ArrayListModel();
        arrayListModel.latitude = latitude;
        arrayListModel.longitude = longitude;

        locationArrayList.add(arrayListModel);

        if (locationArrayList.size() == 1) {
            firstModel = locationArrayList.get(locationArrayList.size() - 1);
            firstLatitude = firstModel.getLatitude();
            firstLongitude = firstModel.getLongitude();
        }

        startModel = locationArrayList.get(locationArrayList.size() - 1);
        startLatitude = startModel.getLatitude();
        startLongitude = startModel.getLongitude();


        if (locationArrayList.size() > 1) {
            currentModel = locationArrayList.get(locationArrayList.size() - 2);

            currentLatitude = currentModel.getLatitude();
            currentLongitude = currentModel.getLongitude();
        }
        drawPolyline();
    }

    private void drawPolyline() {
        if (locationArrayList.size() > 1) {

            Log.e("LatLong", startLatitude + " " + startLongitude + " " + currentLatitude + " " + currentLongitude);
            myPolyline = googleMap.addPolyline(new PolylineOptions()
                    .add(new LatLng(startLatitude, startLongitude),
                            new LatLng(currentLatitude, currentLongitude))
                    .width(5).color(Color.BLUE).geodesic(true));

        }
    }
}