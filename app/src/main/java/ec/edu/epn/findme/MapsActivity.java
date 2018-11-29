package ec.edu.epn.findme;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import ec.edu.epn.findme.AuxiliaryClasses.TimeToColor;
import ec.edu.epn.findme.entity.Alert;
import ec.edu.epn.findme.entity.LastLocation;
import ec.edu.epn.findme.entity.RutaRecorrida;
import ec.edu.epn.findme.entity.TrackObject;
import ec.edu.epn.findme.entity.Usuario;
import ec.edu.epn.findme.enums.AlertTypeEnum;

import static ec.edu.epn.findme.R.drawable.ic_stop_navigation;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener, GoogleMap.OnPolylineClickListener {

    private FloatingActionButton fab;
    private static final int DEFAULT_ZOOM = 10;
    private static final int NEAR_ZOOM = 16;
    private static final int NORMAL_LOCATION_INTERVAL = 15000;
    private static final int FASTEST_PERMITED_LOCATION_INTERVAL = 5000;
    private static final int ALERTS_REQUEST_CODE = 1;
    private static final String TAG = MapsActivity.class.getSimpleName();
    private final int TASK_COMPLETED = 1;
    private final int TASK_FAILED = 0;

    private GoogleMap mMap;
    private boolean mLocationPermissionGranted = false;
    private boolean resultFromOuterActivityObtained = false;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationRequest mLocationRequest;

    private LocationCallback mLocationCallback;
    private boolean isTracking = false;
    private Polyline firstPolyline;
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);

    private Location mLastKnownLocation;
    //private int[] coloursForPolyline = {R.color.zeroToOneHour,R.color.oneToThreeHours,R.color.threeToTwelveHours,R.color.twelveToTwoDays,R.color.twoToFiveDayS};

    /*
    first [] declares the number of polyline, starting at 0
    second [] declares the number of point, starting at 0. The highest point count of all polylines is
                determined in the startLocationUpdates() callback
    third [] will be 0 or 1 because 0 will be latitude and "1" will allocate longitude
     */
    //private double[][][] polylineArray;
    private int localPointCounter;
    private int globalMaxPointCounter = 0;
    private int currentNumberOfTracksOnFirebase;
    private int numberOfAlerts;
    private boolean isUsuarioDinased;

    private ArrayList<RutaRecorrida> rutasRecorridas;
    private ArrayList<String> idsActiveSearches;
    //UserName will go Here
    private String username;
    private Usuario usuario;
    FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().setPersistenceEnabled(true).build();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    CollectionReference usuarioInvitado2Ref = db.collection("LocationData").document("Quito").collection("usuarios");
    CollectionReference userLastLocations = db.collection("LocationData").document("Quito").collection("LastLocations");
    CollectionReference usuarios = db.collection("LocationData").document("Quito").collection("usuarios");
    CollectionReference userPersonalData = db.collection("UserData").document("Quito").collection("usuarios");
    private long timeDiff = 432000000;//5 days
    List<ListenerRegistration> listenerRegistrations;
    List<String> userIdsOnSameSearches;

    TextView searcherNameView;
    TextView searcherTypeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main_activity_map);
        db.setFirestoreSettings(settings);
        currentNumberOfTracksOnFirebase = 0;
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        userIdsOnSameSearches = new ArrayList<>();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        //fab.getBackgroundTintList(ColorStateList.valueOf(R.color.colorGreenStartNavigation));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTrackingPosition();
                Snackbar.make(view, isTracking ? "Ha dejado de rastrear su posicion" : "Comienza a rastrear su posición", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_main);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        rutasRecorridas = new ArrayList<RutaRecorrida>();

        //polylineVector = new Vector<Polyline>(10) ;
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        super.onSaveInstanceState(savedInstanceState);
        getDeviceLocation();
        savedInstanceState.putDouble("LastKnownLocationLatitude", mLastKnownLocation.getLatitude());
        savedInstanceState.putDouble("LastKnownLocationLongitude", mLastKnownLocation.getLongitude());
        //TODO get the Vector of polylines
        //savedInstanceState.putDoubleArray("LastPolyline",firstPolyline.getPoints());
//        if(firstPolyline.getPoints().size()>0){
//            rutasRecorridas.add(new RutaRecorrida(firstPolyline,FieldValue.serverTimestamp()));
//            Log.d(TAG, "Se añadio polyline: " + rutasRecorridas.get(0).getPolyline().getPoints().get(0));
//            savedInstanceState.putParcelableArrayList("PolylineArrayList",rutasRecorridas );
//        }


    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (resultFromOuterActivityObtained == false) {
            if (savedInstanceState.getDouble("LastKnownLocationLatitude") != mDefaultLocation.latitude) {
                mLastKnownLocation.setLatitude(savedInstanceState.getDouble("LastKnownLocationLatitude"));
                mLastKnownLocation.setLongitude(savedInstanceState.getDouble("LastKnownLocationLongitude"));
                LatLng actualPosition = new LatLng(mLastKnownLocation.getLatitude(),
                        mLastKnownLocation.getLongitude());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(actualPosition, NEAR_ZOOM);
                mMap.animateCamera(cameraUpdate);
            }
        }

        rutasRecorridas = savedInstanceState.getParcelableArrayList("PolylineArrayList");
        List<LatLng> points = rutasRecorridas.get(rutasRecorridas.size() - 1).getPolyline().getPoints();
        Log.d(TAG, "Lineas: " + points.get(0) + " " + points.get(1));
        firstPolyline.setPoints(points);
        resultFromOuterActivityObtained = false;
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                //mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            if (mLastKnownLocation != null) {
                                LatLng actualPosition = new LatLng(mLastKnownLocation.getLatitude(),
                                        mLastKnownLocation.getLongitude());
                                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(actualPosition, NEAR_ZOOM);
                                mMap.animateCamera(cameraUpdate);

                                /*mMap.addMarker(new MarkerOptions().position(new LatLng(mLastKnownLocation.getLatitude(),
                                        mLastKnownLocation.getLongitude()))).setTitle("Aqui estoy");
                                Log.d(TAG, "Nuevo Marker!");*/
                            } else {
                                Log.d(TAG, "Current location is null. Using defaults.");
                                Log.e(TAG, "Exception: %s", task.getException());
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                            }


                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_main);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            stopLocationUpdates();
            removeTrackAndLastLocationListeners();
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_activity_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.alertas) {
            getDeviceLocation();
            Bundle bundle = new Bundle();
            bundle.putStringArrayList("selectedActiveSearchIds", idsActiveSearches);
            bundle.putBoolean("usuarioDinased", isUsuarioDinased);
            bundle.putBoolean("showAllAlerts", true);
            bundle.putDouble("alertLatitude", mLastKnownLocation.getLatitude());
            bundle.putDouble("alertLongitude", mLastKnownLocation.getLongitude());
            bundle.putInt("numberOfAlerts", numberOfAlerts);

            Intent intent = new Intent(this, Alerts.class);
            intent.putExtras(bundle);
            //startActivity(intent);
            startActivityForResult(intent, ALERTS_REQUEST_CODE);

        } else if (id == R.id.mis_alertas) {
            getDeviceLocation();
            Bundle bundle = new Bundle();
            bundle.putStringArrayList("selectedActiveSearchIds", idsActiveSearches);
            bundle.putBoolean("usuarioDinased", isUsuarioDinased);
            bundle.putBoolean("showAllAlerts", false);
            bundle.putDouble("alertLatitude", mLastKnownLocation.getLatitude());
            bundle.putDouble("alertLongitude", mLastKnownLocation.getLongitude());
            bundle.putInt("numberOfAlerts", numberOfAlerts);

            Intent intent = new Intent(this, Alerts.class);
            intent.putExtras(bundle);
            startActivityForResult(intent, ALERTS_REQUEST_CODE);
        } else if (id == R.id.add_alerta) {
            getDeviceLocation();
            Bundle bundle = new Bundle();
            bundle.putDouble("alertLatitude", mLastKnownLocation.getLatitude());
            bundle.putDouble("alertLongitude", mLastKnownLocation.getLongitude());
            bundle.putInt("numberOfAlerts", numberOfAlerts);
            bundle.putStringArrayList("selectedActiveSearchIds", idsActiveSearches);
            bundle.putString("Uid", username);
            bundle.putBoolean("usuarioDinased", isUsuarioDinased);

            Intent intent = new Intent(this, NewAlertActivity.class);
            intent.putExtras(bundle);
            startActivityForResult(intent, 1);
        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_log_out) {

            stopLocationUpdates();
            removeTrackAndLastLocationListeners();

            FirebaseAuth.getInstance().signOut();
            Intent i = new Intent(MapsActivity.this, RegistroActivity.class);
            startActivity(i);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_main);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent result) {
        if (requestCode == ALERTS_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Bundle resultData = result.getExtras();
                if (result.hasExtra("alertLatitude") && result.hasExtra("alertLongitude")) {
                    resultFromOuterActivityObtained = true;
                    LatLng alertResultLocation = new LatLng(result.getExtras().getDouble("alertLatitude"), result.getExtras().getDouble("alertLongitude"));
                    Log.d(TAG, "Esta es la ubicacion de la alerta: " + alertResultLocation.toString());
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(alertResultLocation, NEAR_ZOOM);
                    mMap.animateCamera(cameraUpdate);


                }
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        Log.d(TAG, "Entro por aqui!");
        Intent intent = getIntent();
        if (intent.hasExtra("selectedActiveSearchIds")) {
            idsActiveSearches = intent.getExtras().getStringArrayList("selectedActiveSearchIds");
            idsActiveSearches.remove(null);
            Log.d(TAG, "ActiveSearches: " + idsActiveSearches);

        }
        if (intent.hasExtra("Uid")) {
            //idsActiveSearches = intent.getExtras().getStringArrayList("selectedActiveSearchIds");
            Log.d(TAG, "Uid: " + intent.getExtras().getString("Uid"));
            username = intent.getExtras().getString("Uid");
        }
        searcherNameView = (TextView) findViewById(R.id.nav_header_nombres_registro);
        searcherTypeView = (TextView) findViewById(R.id.nav_header_tipo_usuario);
        setNameAndUserType();
        getLocationPermission();
        updateLocationUI();
        getDeviceLocation();
        getCurrentnumberOfTracksAndAlertsInFirebase();

        setActiveSearchesToUserOnFirebase();

        //getPointsAndDrawOtherUsersPoints();
        mMap.setOnPolylineClickListener(this);
        getUsersLastLocationsAndAddMarkers();
        // Add a marker in Sydney and move the camera
        LatLng homeLocation = new LatLng(-0.196, -78.511);
        mMap.addMarker(new MarkerOptions().position(homeLocation).title("Marker in Quito"));
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(homeLocation, 10);
        mMap.animateCamera(cameraUpdate);
        if (rutasRecorridas.size() > 0) {
            List<LatLng> points = rutasRecorridas.get(rutasRecorridas.size() - 1).getPolyline().getPoints();
            Log.d(TAG, "Lineas: " + points.get(0) + " " + points.get(1));
            firstPolyline.setPoints(points);
        }
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                listenToUpdatesFromUsersWithSameSearchIds();
            }
        }, 10000);
        //
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }



    private void getCurrentnumberOfTracksAndAlertsInFirebase() {

        usuarios.document(username).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.getLong("numberOfTracks") != null) {

                        currentNumberOfTracksOnFirebase = document.getLong("numberOfTracks").intValue();
                        Log.d(TAG, document.getId() + "Current tracks: " + currentNumberOfTracksOnFirebase);
                    } else {
                        Log.d(TAG, document.getId() + "There are no current tracks: " + currentNumberOfTracksOnFirebase);
                    }
                    if (document.getLong("numberOfAlerts") != null) {
                        numberOfAlerts = document.getLong("numberOfAlerts").intValue();
                    }


                } else {
                    Log.d(TAG, " No se pueden obtener tracks del mismo usuario " + username);
                }
            }
        });

    }

    private void setNameAndUserType() {
        userPersonalData.document(username).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    Log.d(TAG, "UserData: " + document.getData());
                    usuario = document.toObject(Usuario.class);
                    Log.d(TAG, "UserData: " + usuario.getNombres());
                    searcherNameView.setText(usuario.getNombres() + " " + usuario.getApellidos());
                    isUsuarioDinased = usuario.isUsuarioDinased();
                    if (isUsuarioDinased) {
                        searcherTypeView.setText("DINASED");
                    } else {
                        searcherTypeView.setText("Buscador");
                    }
                }
            }
        });
    }


    private void startTrackingPosition() {
        createLocationRequest();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...
                getDeviceLocation();

                //startLocationUpdates();
                Log.d(TAG, "Muestra posicion actual!");
                if (isTracking == false) {
                    startLocationUpdates();
                    fab.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), ic_stop_navigation));
                    fab.setBackgroundTintList(getResources().getColorStateList(R.color.colorAccent));
                    isTracking = true;
                } else if (isTracking != false) {
                    stopLocationUpdates();
                    fab.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_navigation_start));
                    fab.setBackgroundTintList(getResources().getColorStateList(R.color.colorGreenStartNavigation));
                    isTracking = false;
                }

            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {


            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MapsActivity.this,
                                1);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });

    }


    protected void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(NORMAL_LOCATION_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_PERMITED_LOCATION_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @SuppressWarnings({"MissingPermission"})
    private void startLocationUpdates() {
        getDeviceLocation();
        firstPolyline = mMap.addPolyline(new PolylineOptions().clickable(true).color(ContextCompat.getColor(this, R.color.zeroToOneHour)));
        localPointCounter = 0;
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                final double MIN_DISTANCE_TO_ACCEPT_LATLNG = 15;
                if (locationResult == null) {
                    return;
                }
                if(firstPolyline == null){
                    firstPolyline = mMap.addPolyline(new PolylineOptions().clickable(true).color(ContextCompat.getColor(getApplicationContext(), R.color.zeroToOneHour)));
                }
                Log.d(TAG, "GetPoints at start: " + firstPolyline.getPoints().toString());
                if (firstPolyline.getPoints().isEmpty()) {
                    Log.d(TAG, "Esta vacia la polyline+: ");
                    List<LatLng> primerPunto = firstPolyline.getPoints();
                    primerPunto.add(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()));
                    firstPolyline.setPoints(primerPunto);
                    localPointCounter++;
                }
                for (Location location : locationResult.getLocations()) {
                    //Here's where the magic happens and we start tracking
                    String textoLatLng = String.valueOf(location.getLatitude()) + String.valueOf(location.getLongitude());
                    Log.d(TAG, "Location Results: " + textoLatLng);
                    double distanceBetweenLast2Points = MIN_DISTANCE_TO_ACCEPT_LATLNG;
                    if (mLastKnownLocation != null) {
                        distanceBetweenLast2Points = calculateDistanceInMeters(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude(), location.getLatitude(), location.getLongitude());
                        Log.d(TAG, "Location Results: " + textoLatLng + "Distance Between 2 lastPoints" + distanceBetweenLast2Points);
                    }
                    //Esto luego se podra comentar, es solo para ver cuantas actualizaciones se dieron
                    //mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(),location.getLongitude()))).setTitle("Aqui estoy");
                    //firstPolyline.getPoints().add(new LatLng(location.getLatitude(),location.getLongitude()));
                    if (distanceBetweenLast2Points >= MIN_DISTANCE_TO_ACCEPT_LATLNG) {
                        List<LatLng> points = firstPolyline.getPoints();

                        points.add(new LatLng(location.getLatitude(), location.getLongitude()));
                        firstPolyline.setPoints(points);
                        mLastKnownLocation = location;
                        localPointCounter++;
                        if (localPointCounter % 5 == 0) {
                            GeoPoint userLastLocation = new GeoPoint(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                            setUserLastLocation(FieldValue.serverTimestamp(), userLastLocation, System.currentTimeMillis());
                            prepareTrackDataToStore();
                        }
                    }
                }
            }
        };
        mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);

    }


    private void stopLocationUpdates() {
       prepareTrackDataToStore();
       if(mLocationCallback!= null){
           mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
       }


        //Log.d(TAG, "Antes de añadir: "+trackObjectToAdd.getPoints().get(0));

    }

    private void setActiveSearchesToUserOnFirebase() {
        usuarioInvitado2Ref.document(username).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.get("activeSearches") != null) {
                        ArrayList<String> idSearchesFromFirebase = (ArrayList) document.get("activeSearches");
                        Log.d(TAG, "idSearchesFromFirebase: " + idSearchesFromFirebase);
                        for (String idSearch : idSearchesFromFirebase) {
                            if (!idsActiveSearches.contains(idSearch)) {
                                idsActiveSearches.add(idSearch);
                            }
                        }
                    }
                    Map<String, Object> activeSearches = new HashMap<>();
                    activeSearches.put("activeSearches", idsActiveSearches);
                    usuarioInvitado2Ref.document(username).set(activeSearches, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "Entro en los ActiveSearchesIds: ");
                            //Log.d(TAG, "Siguientes datos: "+trackObjectToAdd.getPoints().get(0));
                            getPointsAndDrawOtherUsersPoints();
                        }
                    });


                } else {

                }
            }
        });


    }



    private void prepareTrackDataToStore(){
        if (firstPolyline != null) {
            Map<String, Object> trackData = new HashMap<>();
            trackData.put("lastTraveled", FieldValue.serverTimestamp());
            //polylineVector.add(firstPolyline);
            TrackObject trackObjectToAdd = new TrackObject();
            trackObjectToAdd.setLatLngPoints(firstPolyline.getPoints());
            trackObjectToAdd.setLastTraveledFieldValue(FieldValue.serverTimestamp());
            trackObjectToAdd.setLastTraveledMillis(System.currentTimeMillis());
            rutasRecorridas.add(new RutaRecorrida(firstPolyline, FieldValue.serverTimestamp()));
            if (localPointCounter > globalMaxPointCounter) {
                globalMaxPointCounter = localPointCounter;
                Log.d(TAG, "Número de puntos máximos: " + globalMaxPointCounter);
            }
            SetPointsIntoFirebase(trackObjectToAdd, rutasRecorridas.size() - 1);

        }
    }

    private void SetPointsIntoFirebase(final TrackObject trackObjectToAdd, int i) {
        String friendlyTimePastLastTraveled = getTimePastLastTraveled(0);
        firstPolyline.setTag(friendlyTimePastLastTraveled + "0 segundos");

        setUserLastLocation(trackObjectToAdd.getLastTraveledFieldValue(),
                trackObjectToAdd.getPoints().get(trackObjectToAdd.getPoints().size() - 1), trackObjectToAdd.getLastTraveledMillis());
        final Map<String, Object> lastSeenTrackData = new HashMap<>();
        lastSeenTrackData.put("lastSeen", trackObjectToAdd.getLastTraveledFieldValue());
        lastSeenTrackData.put("lastUbication", trackObjectToAdd.getPoints().get(trackObjectToAdd.getPoints().size() - 1));
        lastSeenTrackData.put("lastSeenMillis", trackObjectToAdd.getLastTraveledMillis());

        Map<String, Object> trackData = new HashMap<>();
        trackData.put("lastTraveled", trackObjectToAdd.getLastTraveledFieldValue());
        trackData.put("points", trackObjectToAdd.getPoints());
        trackData.put("lastTraveledMillis", trackObjectToAdd.getLastTraveledMillis());

        Map<String, Object> numberOfTracks = new HashMap<>();
        numberOfTracks.put("numberOfTracks", i + 1 + currentNumberOfTracksOnFirebase);

        usuarioInvitado2Ref.document(username).collection("tracks").document("track" + (i + 1 + currentNumberOfTracksOnFirebase)).set(trackData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Entro en la base de datos: ");
                //Log.d(TAG, "Siguientes datos: "+trackObjectToAdd.getPoints().get(0));

            }
        });
        usuarioInvitado2Ref.document(username).set(numberOfTracks, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Entro en los numberOfTracks: ");
                //Log.d(TAG, "Siguientes datos: "+trackObjectToAdd.getPoints().get(0));

            }
        });
        if (currentNumberOfTracksOnFirebase == 0) {
            Map<String, Object> testData = new HashMap<>();
            testData.put("TestField", "Hola, porfavor arreglate");
            usuarioInvitado2Ref.document(username).set(testData).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "Entro el dummy field: ");
                    //Log.d(TAG, "Siguientes datos: "+trackObjectToAdd.getPoints().get(0));

                }
            });
            Map<String, Object> toEraseTestData = new HashMap<>();
            toEraseTestData.put("TestField", FieldValue.delete());
            usuarioInvitado2Ref.document(username).update(toEraseTestData).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Log.d(TAG, "Borro el dummy field: ");
                }
            });
        }

        userLastLocations.document(username).set(lastSeenTrackData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Entro en lastLocations: ");

            }
        });
        firstPolyline = null;
    }

    private void setUserLastLocation(FieldValue lastTraveledFieldValue, GeoPoint userLastLocation, long lastTraveledMillis) {
        final Map<String, Object> lastSeenTrackData = new HashMap<>();
        lastSeenTrackData.put("lastSeen", lastTraveledFieldValue);
        lastSeenTrackData.put("lastUbication", userLastLocation);
        lastSeenTrackData.put("lastSeenMillis", lastTraveledMillis);

        userLastLocations.document(username).set(lastSeenTrackData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Entro en lastLocations: ");

            }
        });
    }

    private void getPointsAndDrawOtherUsersPoints() {

//        for(String idToSearch : idsActiveSearches ){
        usuarios.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {

                    for (DocumentSnapshot document : task.getResult()) {

                        Log.d(TAG, document.getId() + " => " + document.getData());
                        ArrayList<String> searchIdsFromForeignUser = (ArrayList<String>) document.get("activeSearches");
                        Log.d(TAG, "Contenido de este ID: " + searchIdsFromForeignUser);
                        //if(searchIdsFromForeignUser!=null){
                        for (int i = 0; i < searchIdsFromForeignUser.size(); i++) {
                            Log.d(TAG, "idActiveSearches: " + idsActiveSearches + "ForeignUserId" + searchIdsFromForeignUser);
//                                if(Arrays.asList(idsActiveSearches).contains(searchIdsFromForeignUser.get(i).toString())){
                            if (idsActiveSearches.contains(searchIdsFromForeignUser.get(i).toString())) {

                                Log.d(TAG, "Entro para dibujar");
                                i = searchIdsFromForeignUser.size();
                                userIdsOnSameSearches.add(document.getId());

                                getAlertsInformation(document.getId());
                            }

                        }

//                            }
                    }


                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
//        }

    }

    private void listenToUpdatesFromUsersWithSameSearchIds() {
        Log.d(TAG,"Entro a escuchar ");
        listenerRegistrations = new ArrayList<>();
        long milliesNow = System.currentTimeMillis();
        for(String userID : userIdsOnSameSearches){
            Log.d(TAG,"Este es el userID: "+userID);
            ListenerRegistration listenerRegistration = usuarios.document(userID).collection("tracks").whereGreaterThanOrEqualTo("lastTraveledMillis",milliesNow).addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot snapshots, FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w(TAG, "listen:error", e);
                        return;
                    }
                    //for (DocumentSnapshot doc : snapshots.getDocuments()) {
                    for (DocumentChange doc : snapshots.getDocumentChanges()) {
                        switch (doc.getType()) {
                            case ADDED:
                                Log.d(TAG,"ACTUALIZACION Esto fue obtenido: "+doc.getDocument().getId()+"tiempo: "+doc.getDocument().getDate("lastTraveled"));
                                if(doc.getDocument().getId() != null){
                                    drawLineToMap(doc.getDocument().toObject(TrackObject.class));
                                }
                                break;
                            case MODIFIED:
                                Log.d(TAG, "Modified city: " + doc.getDocument().getData());
                                break;
                            case REMOVED:
                                Log.d(TAG, "Removed city: " + doc.getDocument().getData());
                                break;
                        }


                    }
                }
            });
            listenerRegistrations.add(listenerRegistration);
            //TODO funcion que escuche y cambie el LastLocations, se necesita recuperar ese marker
        }
    }

    private void getAlertsInformation(String id) {
        usuarios.document(id).collection("alerts").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        //Log.d(TAG, id + " and "+username);
                        /*if(id.equals(username) ){
                            currentNumberOfTracksOnFirebase+=1;
                            Log.d(TAG, document.getId() + "Current tracks: "+currentNumberOfTracksOnFirebase);
                        }*/
                        Log.d(TAG, document.getId() + " => " + document.get("description"));
                        Alert alert = document.toObject(Alert.class);
                        int alertIcon = 0;
                        if (alert.getAlertType().equals(AlertTypeEnum.PISTA.toString())) {
                            alertIcon = R.drawable.ic_action_name;
                        } else if (alert.getAlertType().equals(AlertTypeEnum.AVISTAMIENTO.toString())) {
                            alertIcon = R.drawable.ic_binocular_avistamiento_background;
                            //Bitmap bm = BitmapFactory.decodeResource(getResources(),R.drawable.ic_binocular_avistamiento_background);
                            //Bitmap bm = BitmapFactory.decodeFile("main/ic_binocular_avistamiento-web.png");
                        }
                        mMap.addMarker(new MarkerOptions().alpha(0.6f).position(alert.getLocationLatLng()).title(alert.getTitle()).icon(BitmapDescriptorFactory.fromResource(alertIcon)));


                    }
                } else {

                }
            }
        });
    }

    private void getTrackInformation(final String id) {
        //long timeDiff = 432000000;//5 days
        long longtimeToSearch = System.currentTimeMillis() - timeDiff;
        //usuarios.document(id).collection("tracks").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>(){


        usuarios.document(id).collection("tracks").whereGreaterThanOrEqualTo("lastTraveledMillis", longtimeToSearch).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        Log.d(TAG, id + " and " + username);
                        /*if(id.equals(username) ){
                            currentNumberOfTracksOnFirebase+=1;
                            Log.d(TAG, document.getId() + "Current tracks: "+currentNumberOfTracksOnFirebase);
                        }*/
                        Log.d(TAG, document.getId() + " => " + document.getDate("lastTraveled"));

                        drawLineToMap(document.toObject(TrackObject.class));

                    }


                }
            }
        });


    }

    private void drawLineToMap(TrackObject trackObject) {
        //FieldValue timeNow = FieldValue.serverTimestamp();

        long diffInMilliseconds = System.currentTimeMillis() - trackObject.getLastTraveled().getTime();
        Log.v(TAG,"DIferencia de tiempo: en millis"+diffInMilliseconds);
        String friendlyTimePastLastTraveled = getTimePastLastTraveled(diffInMilliseconds);
        TimeToColor timeToColor = new TimeToColor();
        int polylineColor = timeToColor.getTimeToColor(diffInMilliseconds);
        Polyline trackedPolyline = mMap.addPolyline(new PolylineOptions().clickable(true).color(ContextCompat.getColor(this, polylineColor)));
        trackedPolyline.setTag(friendlyTimePastLastTraveled);

        List<LatLng> pointsToDraw = new ArrayList<>();
        for (int i = 0; i < trackObject.getPoints().size(); i++) {
            pointsToDraw.add(new LatLng(trackObject.getPoints().get(i).getLatitude(), trackObject.getPoints().get(i).getLongitude()));
        }
        trackedPolyline.setPoints(pointsToDraw);

    }

    public void getUsersLastLocationsAndAddMarkers() {
        long timeDiff = 432000000;//5 days
        long longtimeToSearch = System.currentTimeMillis() - timeDiff;
        //Date timeToSearch = new Date(System.currentTimeMillis()-timeDiff);
        userLastLocations.whereGreaterThan("lastSeenMillis", longtimeToSearch).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        Log.d(TAG, "LastLocations: " + document.getId() + " => " + document.getData());
                        drawLastSeenUsersMarkers(document.getId(), document.toObject(LastLocation.class));
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }

    private void drawLastSeenUsersMarkers(String userUid, final LastLocation lastLocation) {
        userPersonalData.document(userUid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    Log.d(TAG, "UserData: " + document.getData());
                    Usuario userFromLastLocation = document.toObject(Usuario.class);
                    Log.d(TAG, "UserData: " + userFromLastLocation.getNombres());
                    String nameToShow = userFromLastLocation.getNombres() + " " + userFromLastLocation.getApellidos();
                    mMap.addMarker(new MarkerOptions().alpha(0.4f).position(lastLocation.getLastUbicationLatLng()).title(nameToShow).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_seeker)));
                } else {
                    Log.d(TAG, "Error getting documents of that Uid: ", task.getException());
                }
            }
        });
    }

    @Override
    public void onPolylineClick(Polyline polyline) {
        Toast.makeText(this, "Tiempo desde la ultima pasada " + polyline.getTag().toString(), Toast.LENGTH_LONG).show();
    }

    private void removeTrackAndLastLocationListeners() {
        if(listenerRegistrations != null){
            for(ListenerRegistration listener : listenerRegistrations){
                listener.remove();
            }
        }

    }

    public void putLastTraveledMillisIntoOldData() {

    }

    //Method got from stackoverflow
    private String getTimePastLastTraveled(long diffInMilliseconds) {

        //This units gets implemented into an ArrayList with an enumeration of all TimeUnits and gets reordered to DAYS, HOURS....
        List<TimeUnit> units = new ArrayList<TimeUnit>(EnumSet.allOf(TimeUnit.class));
        Collections.reverse(units);

        Map<TimeUnit, Long> timePastLastTraveled = new LinkedHashMap<TimeUnit, Long>();
        long milliesRest = diffInMilliseconds;
        for (TimeUnit unit : units) {
            long diff = unit.convert(milliesRest, TimeUnit.MILLISECONDS);
            long diffInMilliesForUnit = unit.toMillis(diff);
            milliesRest = milliesRest - diffInMilliesForUnit;
            timePastLastTraveled.put(unit, diff);
        }
        StringBuilder friendlyTime = new StringBuilder();
        for (Map.Entry<TimeUnit, Long> entry : timePastLastTraveled.entrySet()) {
            if (entry.getValue() > 0) {
                friendlyTime.append(entry.getValue())
                        .append(entry.getKey().toString().charAt(0))
                        .append(" ");
            }
        }
        friendlyTime.trimToSize();
        return friendlyTime.toString();

    }

    //Method got from StackOverflow :)
    public final static double AVERAGE_RADIUS_OF_EARTH_M = 6371000;

    public int calculateDistanceInMeters(double userLat, double userLng,
                                         double venueLat, double venueLng) {

        double latDistance = Math.toRadians(userLat - venueLat);
        double lngDistance = Math.toRadians(userLng - venueLng);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(userLat)) * Math.cos(Math.toRadians(venueLat))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return (int) (Math.round(AVERAGE_RADIUS_OF_EARTH_M * c));
    }


}
