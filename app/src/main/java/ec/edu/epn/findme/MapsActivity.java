package ec.edu.epn.findme;

import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import ec.edu.epn.findme.AuxiliaryClasses.TimeToColor;
import ec.edu.epn.findme.entity.LastLocation;
import ec.edu.epn.findme.entity.RutaRecorrida;
import ec.edu.epn.findme.entity.TrackObject;

import static ec.edu.epn.findme.R.drawable.ic_stop_navigation;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener, GoogleMap.OnPolylineClickListener {

    private FloatingActionButton fab;
    private static final int DEFAULT_ZOOM = 10;
    private static final int NEAR_ZOOM = 16;
    private static final int NORMAL_LOCATION_INTERVAL = 15000;
    private static final int FASTEST_PERMITED_LOCATION_INTERVAL = 5000;
    private static final String TAG = MapsActivity.class.getSimpleName();

    private GoogleMap mMap;
    private boolean mLocationPermissionGranted = false;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationRequest mLocationRequest;

    private LocationCallback mLocationCallback;
    private boolean isTracking = false;
    private Polyline firstPolyline;
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);

    private Location mLastKnownLocation;
    private int[] coloursForPolyline = {R.color.zeroToOneHour,R.color.oneToThreeHours,R.color.threeToTwelveHours,R.color.twelveToTwoDays,R.color.twoToFiveDayS};

    /*
    first [] declares the number of polyline, starting at 0
    second [] declares the number of point, starting at 0. The highest point count of all polylines is
                determined in the startLocationUpdates() callback
    third [] will be 0 or 1 because 0 will be latitude and "1" will allocate longitude
     */
    //private double[][][] polylineArray;
    private int localPointCounter;
    private int globalMaxPointCounter=0;
    private int currentNumberOfTracksOnFirebase;

    private ArrayList<RutaRecorrida> rutasRecorridas;
    //UserName will go Here
    private String username = "ItsMeLuigi";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference usuarioInvitado2Ref = db.collection("LocationData").document("Quito").collection("usuarios");
    CollectionReference userLastLocations = db.collection("LocationData").document("Quito").collection("LastLocations");
    CollectionReference usuarios = db.collection("LocationData").document("Quito").collection("usuarios");
    private long timeDiff = 432000000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main_activity_map);
        currentNumberOfTracksOnFirebase = 0;
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        //fab.getBackgroundTintList(ColorStateList.valueOf(R.color.colorGreenStartNavigation));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTrackingPosition();
                Snackbar.make(view, isTracking?"Ha dejado de rastrear su posicion":"Comienza a rastrear su posición", Snackbar.LENGTH_LONG)
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
        savedInstanceState.putDouble("LastKnownLocationLatitude",mLastKnownLocation.getLatitude());
        savedInstanceState.putDouble("LastKnownLocationLongitude",mLastKnownLocation.getLongitude());
        //TODO get the Vector of polylines
        //savedInstanceState.putDoubleArray("LastPolyline",firstPolyline.getPoints());
//        if(firstPolyline.getPoints().size()>0){
//            rutasRecorridas.add(new RutaRecorrida(firstPolyline,FieldValue.serverTimestamp()));
//            Log.d(TAG, "Se añadio polyline: " + rutasRecorridas.get(0).getPolyline().getPoints().get(0));
//            savedInstanceState.putParcelableArrayList("PolylineArrayList",rutasRecorridas );
//        }


    }
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "Hola: " );
        if(savedInstanceState.getDouble("LastKnownLocationLatitude")!= mDefaultLocation.latitude){
            mLastKnownLocation.setLatitude(savedInstanceState.getDouble("LastKnownLocationLatitude"));
            mLastKnownLocation.setLongitude(savedInstanceState.getDouble("LastKnownLocationLongitude"));
            LatLng actualPosition = new LatLng(mLastKnownLocation.getLatitude(),
                    mLastKnownLocation.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(actualPosition, NEAR_ZOOM);
            mMap.animateCamera(cameraUpdate);
        }
        rutasRecorridas = savedInstanceState.getParcelableArrayList("PolylineArrayList");
        List<LatLng> points = rutasRecorridas.get(rutasRecorridas.size()-1).getPolyline().getPoints();
        Log.d(TAG, "Lineas: " + points.get(0) + " " + points.get(1));
        firstPolyline.setPoints(points);
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
            // Handle the camera action
        } else if (id == R.id.mis_alertas) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_main);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
        getLocationPermission();
        updateLocationUI();
        getDeviceLocation();
        //startTrackingPosition();
        getPointsAndDrawOtherUsersPoints();
        mMap.setOnPolylineClickListener(this);
        getUsersLastLocationsAndAddMarkers();
        // Add a marker in Sydney and move the camera
        LatLng homeLocation = new LatLng(-0.196, -78.511);
        mMap.addMarker(new MarkerOptions().position(homeLocation).title("Marker in Quito"));
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(homeLocation, 10);
        mMap.animateCamera(cameraUpdate);
        if(rutasRecorridas.size()>0){
            List<LatLng> points = rutasRecorridas.get(rutasRecorridas.size()-1).getPolyline().getPoints();
            Log.d(TAG, "Lineas: " + points.get(0) + " " + points.get(1));
            firstPolyline.setPoints(points);
        }
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
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
                if(isTracking == false){
                    startLocationUpdates();
                    fab.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),ic_stop_navigation));
                    isTracking = true;
                }else if (isTracking != false) {
                    stopLocationUpdates();
                    fab.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.ic_navigation_start));
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
            firstPolyline = mMap.addPolyline(new PolylineOptions().clickable(true));
            localPointCounter = 0;
            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult( LocationResult locationResult) {
                    final double MIN_DISTANCE_TO_ACCEPT_LATLNG = 15;
                    if (locationResult == null) {
                        return;
                    }
                    Log.d(TAG, "GetPoints at start: " + firstPolyline.getPoints().toString());
                    if (firstPolyline.getPoints().isEmpty()) {
                        Log.d(TAG, "Esta vacia la polyline+: " );
                        List<LatLng> primerPunto = firstPolyline.getPoints();
                        primerPunto.add(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()));
                        firstPolyline.setPoints(primerPunto);
                        localPointCounter++;
                    }
                    for (Location location : locationResult.getLocations()) {
                        //Here's where the magic happens and we start tracking
                        String textoLatLng = String.valueOf(location.getLatitude()) + String.valueOf(location.getLongitude());
                        Log.d(TAG, "Location Results: " + textoLatLng);
                        double distanceBetweenLast2Points=MIN_DISTANCE_TO_ACCEPT_LATLNG;
                        if (mLastKnownLocation!=null){
                            distanceBetweenLast2Points = calculateDistanceInMeters(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude(),location.getLatitude(),location.getLongitude());
                            Log.d(TAG, "Location Results: " + textoLatLng+"Distance Between 2 lastPoints"+distanceBetweenLast2Points);
                        }
                        //Esto luego se podra comentar, es solo para ver cuantas actualizaciones se dieron
                        mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(),
                                location.getLongitude()))).setTitle("Aqui estoy");
                        //firstPolyline.getPoints().add(new LatLng(location.getLatitude(),location.getLongitude()));
                        if (distanceBetweenLast2Points>=MIN_DISTANCE_TO_ACCEPT_LATLNG) {
                            List<LatLng> points = firstPolyline.getPoints();

                            points.add(new LatLng(location.getLatitude(), location.getLongitude()));
                            firstPolyline.setPoints(points);
                            mLastKnownLocation = location;
                            localPointCounter++;
                        }
                    }
                }
            };
            mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);


    }


    private void stopLocationUpdates(){
        if (firstPolyline!=null){
            Map<String,Object> trackData= new HashMap<>();
            trackData.put("lastTraveled", FieldValue.serverTimestamp());
            //polylineVector.add(firstPolyline);
            TrackObject trackObjectToAdd = new TrackObject();
            trackObjectToAdd.setLatLngPoints(firstPolyline.getPoints());
            trackObjectToAdd.setLastTraveledFieldValue(FieldValue.serverTimestamp());
            rutasRecorridas.add(new RutaRecorrida(firstPolyline,FieldValue.serverTimestamp()));

            if(localPointCounter>globalMaxPointCounter){
                globalMaxPointCounter= localPointCounter;
                Log.d(TAG, "Número de puntos máximos: "+globalMaxPointCounter );
            }
            SetPointsIntoFirebase(trackObjectToAdd,rutasRecorridas.size()-1);
            mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
        }


        //Log.d(TAG, "Antes de añadir: "+trackObjectToAdd.getPoints().get(0));

    }

    private void SetPointsIntoFirebase(final TrackObject trackObjectToAdd, int i ) {
        final Map<String,Object> lastSeenTrackData = new HashMap<>();
        lastSeenTrackData.put("lastSeen",trackObjectToAdd.getLastTraveledFieldValue());
        lastSeenTrackData.put("lastUbication",trackObjectToAdd.getPoints().get(trackObjectToAdd.getPoints().size()-1));
        lastSeenTrackData.put("lastSeenMillis",trackObjectToAdd.getLastTraveled().getTime());

        Map<String,Object> trackData= new HashMap<>();
        trackData.put("lastTraveled", trackObjectToAdd.getLastTraveledFieldValue());
        trackData.put("points",trackObjectToAdd.getPoints());


        usuarioInvitado2Ref.document(username).collection("tracks").document("track"+(i+1+currentNumberOfTracksOnFirebase)).set(trackData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Entro en la base de datos: ");
                //Log.d(TAG, "Siguientes datos: "+trackObjectToAdd.getPoints().get(0));

            }
        });
        if(currentNumberOfTracksOnFirebase==0){
            Map<String,Object> testData = new HashMap<>();
            testData.put("TestField","Hola, porfavor arreglate");
            usuarioInvitado2Ref.document(username).set(testData).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "Entro el dummy field: ");
                    //Log.d(TAG, "Siguientes datos: "+trackObjectToAdd.getPoints().get(0));

                }
            });
            Map<String,Object> toEraseTestData = new HashMap<>();
            toEraseTestData.put("TestField",FieldValue.delete());
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
    }

    private void getPointsAndDrawOtherUsersPoints (){
        usuarios.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        Log.d(TAG, document.getId() + " => " + document.getData() );

                        getTrackInformation(document.getId());



                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }

    private void getTrackInformation(final String id) {
        Date timeNow = new Date(System.currentTimeMillis());
        Date fromDate = new Date(timeNow.getTime()-timeDiff);
        usuarios.document(id).collection("tracks").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>(){
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(DocumentSnapshot document : task.getResult()){
                        Log.d(TAG, id + " and "+username);
                        if(id.equals(username) ){
                            currentNumberOfTracksOnFirebase+=1;
                            Log.d(TAG, document.getId() + "Current tracks: "+currentNumberOfTracksOnFirebase);
                        }
                        Log.d(TAG, document.getId() + " => " +  document.getDate("lastTraveled"));
                        drawLineToMap(document.toObject(TrackObject.class));

                    }
                }else {

                }
            }
        });
    }

    private void drawLineToMap(TrackObject trackObject) {
        //FieldValue timeNow = FieldValue.serverTimestamp();

        long diffInMilliseconds = System.currentTimeMillis() - trackObject.getLastTraveled().getTime();

        String friendlyTimePastLastTraveled = getTimePastLastTraveled(diffInMilliseconds);
        TimeToColor timeToColor = new TimeToColor();
        int polylineColor = timeToColor.getTimeToColor(diffInMilliseconds);
        Polyline trackedPolyline = mMap.addPolyline(new PolylineOptions().clickable(true).color(ContextCompat.getColor(this,polylineColor)));
        trackedPolyline.setTag(friendlyTimePastLastTraveled);

        List<LatLng> pointsToDraw= new ArrayList<>();
        for(int i = 0;i<trackObject.getPoints().size();i++){
            pointsToDraw.add(new LatLng(trackObject.getPoints().get(i).getLatitude(),trackObject.getPoints().get(i).getLongitude()));
        }
        trackedPolyline.setPoints(pointsToDraw);




    }

    public void getUsersLastLocationsAndAddMarkers(){
        long timeDiff = 432000000;//5 days
        long longtimeToSearch = System.currentTimeMillis()-timeDiff;
        //Date timeToSearch = new Date(System.currentTimeMillis()-timeDiff);
        userLastLocations.whereGreaterThan("lastSeenMillis",longtimeToSearch).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        Log.d(TAG, "LastLocations: "+document.getId() + " => " + document.getData());
                        drawLastSeenUsersMarkers(document.getId(),document.toObject(LastLocation.class));
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }

    private void drawLastSeenUsersMarkers(String id, LastLocation lastLocation) {
        mMap.addMarker(new MarkerOptions().alpha(0.4f).position(lastLocation.getLastUbicationLatLng()).title(id));
    }

    @Override
    public void onPolylineClick(Polyline polyline) {
        Toast.makeText(this,"Tiempo desde la ultima pasada "+ polyline.getTag().toString(),Toast.LENGTH_LONG).show();
    }


    //Method got from stackoverflow
    private String getTimePastLastTraveled(long diffInMilliseconds) {

        //This units gets implemented into an ArrayList with an enumeration of all TimeUnits and gets reordered to DAYS, HOURS....
        List<TimeUnit> units = new ArrayList<TimeUnit>(EnumSet.allOf(TimeUnit.class));
        Collections.reverse(units);

        Map<TimeUnit,Long> timePastLastTraveled = new LinkedHashMap<TimeUnit,Long>();
        long milliesRest = diffInMilliseconds;
        for ( TimeUnit unit : units ) {
            long diff = unit.convert(milliesRest,TimeUnit.MILLISECONDS);
            long diffInMilliesForUnit = unit.toMillis(diff);
            milliesRest = milliesRest - diffInMilliesForUnit;
            timePastLastTraveled.put(unit,diff);
        }
        StringBuilder friendlyTime = new StringBuilder();
        for(Map.Entry<TimeUnit,Long> entry : timePastLastTraveled.entrySet()){
            if(entry.getValue()>0){
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
