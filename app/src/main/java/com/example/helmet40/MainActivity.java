package com.example.helmet40;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.usage.UsageEvents;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.PersistableBundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.SyncStateContract;
import android.renderscript.ScriptGroup;
import android.service.autofill.FillEventHistory;
import android.speech.RecognitionService;
import android.speech.RecognizerIntent;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.ValueEventListener;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;


import com.mapbox.android.telemetry.Event;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.core.constants.Constants;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.LineString;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.ui.PlaceAutocompleteFragment;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.ui.PlaceSelectionListener;
import com.mapbox.mapboxsdk.plugins.places.common.PlaceConstants;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.light.Position;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.squareup.picasso.Picasso;


import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static android.Manifest.permission.SEND_SMS;
import static com.mapbox.core.constants.Constants.PRECISION_6;
import static com.mapbox.geojson.Point.fromLngLat;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;
import static java.lang.String.*;

public class MainActivity extends AppCompatActivity implements MapboxMap.OnMapClickListener, OnMapReadyCallback, PermissionsListener {

    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 1;
    private ArrayList<String> arrayList;
    private MapView mapView;
    private MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;
    public static LocationManager locationManager;
    private FusedLocationProviderClient client;
    LocationManager lm;
    TextView lt, ln;
    String provider;
    float speed;
    Context context;
    Location l;
    private static final int REQUEST_SMS = 0;
    private BroadcastReceiver sentStatusReceiver, deliveredStatusReceiver;
    private LocationComponent locationComponent;
    private static final int REQUEST_CODE_SPEECH_INPUT = 1000;

    private int PERMISSION_REQUEST_READ_PHONE_STATE = 1;
    public final static long APP_START_TIME = System.currentTimeMillis();
    //private Location originLocation;
    private static com.mapbox.geojson.Point destinationPoint;
    private com.mapbox.geojson.Point originPoint;
    PlacesClient placesClient;
    private Button startButton;
    private NavigationMapRoute navigationMapRoute;
    private static final String TAG = "MainActivity";
    private TextView wifi, music, myprofile, weather,search,bluetooth,phonecall,cameratxt;
    private DirectionsRoute currentRoute;
    private DatabaseReference databaseReference;
    // int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE};
    private Double lattitude, longitude, latitudes, longitudes;


    // @android.support.annotation.RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         Mapbox.getInstance(this, "pk.eyJ1IjoiYW1hcmVuZHJhMTk5OCIsImEiOiJjazN4NjlvYWIxMzRnM2tvcHU1MzZweXE2In0.eUex3xeCziZF-zetIQ6HiQ");
        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.mapview);
         mapView.onCreate(savedInstanceState);
         mapView.getMapAsync(this);
        startButton = (Button) findViewById(R.id.startButton);
        databaseReference = FirebaseDatabase.getInstance().getReference("MySmartHelmet");

        client = LocationServices.getFusedLocationProviderClient(this);
         wifi = (TextView) findViewById(R.id.textView);
         music = (TextView) findViewById(R.id.textView1);
        weather = (TextView) findViewById(R.id.textView4);
        search = (TextView) findViewById(R.id.textView5);
        bluetooth = (TextView) findViewById(R.id.textViews);
        phonecall = (TextView) findViewById(R.id.textViewed);
        cameratxt = (TextView) findViewById(R.id.cameratext);

        cameratxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,CameraActivity.class);
                startActivity(intent);
            }
        });
        phonecall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,PhoneCallActivity.class);
                startActivity(intent);
            }
        });
        bluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,BluetoothChat.class);
                startActivity(intent);
            }
        });
        wifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,WifiActivity.class);
                startActivity(intent);
            }
        });
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });
            weather.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, WeatherActivity.class);
                    startActivity(intent);
                }
            });
            arrayList = new ArrayList<>();

            music.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this,Tunes.class);
                    startActivity(intent);
                }
            });

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CALL_PHONE}, 1);
                }
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
                }
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ANSWER_PHONE_CALLS}, 1);
                }
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALL_LOG}, 1);
                }
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_NUMBERS}, 1);
                }
            }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int hasSMSPermission = checkSelfPermission(Manifest.permission.SEND_SMS);
            if (hasSMSPermission != PackageManager.PERMISSION_GRANTED) {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.SEND_SMS)) {
                    showMessageOKCancel("You need to allow access to Send SMS",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestPermissions(new String[] {Manifest.permission.SEND_SMS},
                                                REQUEST_SMS);
                                    }
                                }
                            });
                    return;
                }
                requestPermissions(new String[] {Manifest.permission.SEND_SMS},
                        REQUEST_SMS);
                return;
            }
           // sendMySMS();
        }
//


//
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                if (getApplicationContext().checkSelfPermission(Manifest.permission.PROCESS_OUTGOING_CALLS)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Permission has not been granted, therefore prompt the user to grant permission
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.PROCESS_OUTGOING_CALLS},
                            1);
                }


            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                if (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 1);
                } else {
                    getContacts();
                }
            }
            long launchTime = System.currentTimeMillis() - MainActivity.APP_START_TIME;
            databaseReference.child("Application_Flag").setValue(1);
            databaseReference.child("Application_Time").setValue(launchTime);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                   // String userImage = dataSnapshot.child(Trigger_Phone).getValue().toString();
                    if (Objects.requireNonNull(dataSnapshot.child("Trigger_Phone").getValue()).toString().equals("1")){
                        speak();
                    }else {
                        Toast.makeText(MainActivity.this, "Trigger_Phone is 0", Toast.LENGTH_SHORT).show();

                    }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void speak() {
       Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
       intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
       intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,Locale.getDefault());
       intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Hi Speak Something");
       startActivityForResult(intent,REQUEST_CODE_SPEECH_INPUT);


    }


    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {

    }

    private void getContacts() {
        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null,null);
        while (cursor.moveToNext()){
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String mobile = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            arrayList.add(name + " "+ mobile);
            Map<String,Object> profilemap =new HashMap<>();
            profilemap.put("Contacts",arrayList.toString());
           databaseReference.child("Contacts").setValue(profilemap);
        }
    }

    public void startNavigationbtnclick(View view) {
        boolean simulateRoute = true;
        //while (currentRoute != null) {
            NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                    .directionsRoute(currentRoute)
                    .shouldSimulateRoute(simulateRoute)
                    .build();

            NavigationLauncher.startNavigation(MainActivity.this, options);
        //}
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            enableLocationComponent(mapboxMap.getStyle());
        } else {
            Toast.makeText(getApplicationContext(), "Permission not granted", Toast.LENGTH_SHORT).show();
            finish();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        //
        locationManager = (LocationManager)  this.getSystemService(Context.LOCATION_SERVICE);
        destinationPoint = com.mapbox.geojson.Point.fromLngLat(point.getLongitude(), point.getLatitude());
        longitude = point.getLongitude();
        lattitude = point.getLatitude();
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
           //
        }
        assert locationComponent.getLastKnownLocation() != null;

        longitudes = locationComponent.getLastKnownLocation().getLongitude();
        latitudes = locationComponent.getLastKnownLocation().getLatitude();
        LatLng latLng = new LatLng(latitudes,longitudes);
        //originPoint = com.mapbox.geojson.Point.fromLngLat(lattitude,longitude);
         originPoint = com.mapbox.geojson.Point.fromLngLat(locationComponent.getLastKnownLocation().getLongitude(), locationComponent.getLastKnownLocation().getLatitude());
          speed = locationComponent.getLastKnownLocation().getSpeed();
         GeoJsonSource source = mapboxMap.getStyle().getSourceAs("destination-source-id");
         if (source!=null){
            source.setGeoJson(Feature.fromGeometry((Geometry) destinationPoint));
        }

        Map<String, Object> profiled = new HashMap<>();
        profiled.put("CurrentAddress", latLng);
        profiled.put("State", "C");
        profiled.put("TempTrigger", "0");
        profiled.put("Speed", speed);
         databaseReference.child("Navigation").setValue(profiled);
         startButton.setEnabled(true);
         startButton.setBackgroundResource(R.color.colorPrimary);
         //getAddresses(lattitude,longitude);
         //getAddress(latitudes,longitudes);
         //AutocompleteSupportFragment.newInstance();
         getRoute(originPoint,destinationPoint);
         return true;
    }

    private void getRoute(com.mapbox.geojson.Point originPoint, com.mapbox.geojson.Point destinationPoint) {
        assert Mapbox.getAccessToken() != null;
        NavigationRoute.builder(this)
                .accessToken(Mapbox.getAccessToken())
                .origin(originPoint)
                .destination(destinationPoint)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(@NotNull Call<DirectionsResponse> call, @NotNull Response<DirectionsResponse> response) {
                        if (response.body()!=null && response.body().routes().size()<1){
                             currentRoute = response.body().routes().get(0);
                            if (navigationMapRoute!=null){
                                navigationMapRoute.removeRoute();
                            }
                            else {
                                navigationMapRoute = new NavigationMapRoute(null,mapView,mapboxMap,R.style.NavigationMapRoute);
                            }
                            navigationMapRoute.addRoute(currentRoute);
                           // startNavigationbtnclick(currentRoute);


                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<DirectionsResponse> call, @NotNull Throwable t) {
                        Log.e(TAG, "Erro: " + t.getMessage());
                        Toast.makeText(MainActivity.this, "Erro: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }


    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        this.mapboxMap.setMinZoomPreference(15);
        mapboxMap.setStyle(getString(R.string.navigation_guidance_day), new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                enableLocationComponent(style);
                addDestinationIconLayer(style);
                mapboxMap.addOnMapClickListener(MainActivity.this);
               // mapboxMap.addOnMapLongClickListener(MainActivity.this);
            }
        });

    }

    public void getAddresses(double lat, double lng) {
        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);
            String add = obj.getAddressLine(0);
            add = add + "\n" + obj.getCountryName();
            add = add + "\n" + obj.getCountryCode();
            add = add + "\n" + obj.getAdminArea();
            add = add + "\n" + obj.getPostalCode();
            add = add + "\n" + obj.getSubAdminArea();
            add = add + "\n" + obj.getLocality();
            add = add + "\n" + obj.getSubThoroughfare();
            Map<String, Object> profiled = new HashMap<>();
            profiled.put("Des_Address", add);
            databaseReference.child("Navigation2").setValue(profiled);
            Log.v("IGA", "Address" + add);
            // Toast.makeText(this, "Address=>" + add,
            // Toast.LENGTH_SHORT).show();

            // TennisAppActivity.showDialog(add);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void getAddress(double lat, double lng) {
        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);
            String add = obj.getAddressLine(0);
            add = add + "\n" + obj.getCountryName();
            add = add + "\n" + obj.getCountryCode();
            add = add + "\n" + obj.getAdminArea();
            add = add + "\n" + obj.getPostalCode();
            add = add + "\n" + obj.getSubAdminArea();
            add = add + "\n" + obj.getLocality();
            add = add + "\n" + obj.getSubThoroughfare();
            Map<String, Object> profiled = new HashMap<>();
            profiled.put("Cur_Address", add);
            databaseReference.child("Navigation1").setValue(profiled);
            Log.v("IGA", "Address" + add);
            // Toast.makeText(this, "Address=>" + add,
            // Toast.LENGTH_SHORT).show();

            // TennisAppActivity.showDialog(add);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private void addDestinationIconLayer(Style style) {
        style.addImage("destination-icon-id", BitmapFactory.decodeResource(this.getResources(),R.drawable.mapbox_marker_icon_default));
        GeoJsonSource geoJsonSource = new GeoJsonSource("destination-source-id");
        style.addSource(geoJsonSource);
        SymbolLayer destinationSymbolLayer = new SymbolLayer("destination-symbol-layer-id","destination-source-id");
        destinationSymbolLayer.withProperties(iconImage("destination-icon-id"),iconAllowOverlap(true),iconIgnorePlacement(true));
        style.addLayer(destinationSymbolLayer);

    }

    @SuppressLint("MissingPermission")
    private void enableLocationComponent(Style loadedMapStyle) {

        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            locationComponent = mapboxMap.getLocationComponent();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            locationComponent.activateLocationComponent(LocationComponentActivationOptions.builder(this, loadedMapStyle).build());
            locationComponent.activateLocationComponent(this, loadedMapStyle);
            locationComponent.setLocationComponentEnabled(true);
            locationComponent.setCameraMode(CameraMode.TRACKING);
            locationComponent.setRenderMode(RenderMode.COMPASS);
       }
        else
        {
           permissionsManager = new PermissionsManager(this);
           permissionsManager.requestLocationPermissions(this);
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case REQUEST_CODE_SPEECH_INPUT:{
                if (resultCode == RESULT_OK && null!=data){
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String name = result.get(0);
                    if (name.equals("call on phone")){
                        Intent intent = new Intent(MainActivity.this,CallReceiver.class);
                        startActivity(intent);
                    }else if (name.equals("start front camera")){
                        //Intent intent = new Intent(MainActivity.this,CameraActivity.class);
                        databaseReference.child("CameraFeature").child("FrontCam").setValue("active");
                        //startActivity(intent);
                    }else if (name.equals("stop front camera")){
                        //Intent intent = new Intent(MainActivity.this,CameraActivity.class);
                        databaseReference.child("CameraFeature").child("FrontCam").setValue("inactive");
                        //startActivity(intent);
                    }
                    else if (name.equals("start back camera")){
                        //Intent intent = new Intent(MainActivity.this,CameraActivity.class);
                        databaseReference.child("CameraFeature").child("RareCam").setValue("active");
                        //startActivity(intent);
                    }else if (name.equals("stop back camera")){
                        //Intent intent = new Intent(MainActivity.this,CameraActivity.class);
                        databaseReference.child("CameraFeature").child("RareCam").setValue("inactive");
                        //startActivity(intent);
                    }
                    else if (name.equals("start music")){
                        Intent intent = new Intent(MainActivity.this,Tunes.class);
                        startActivity(intent);
                    }else if (name.equals("start bluetooth")){
                        Intent intent = new Intent(MainActivity.this,BluetoothChat.class);
                        startActivity(intent);
                    }else if (name.equals("start wifi")){
                        Intent intent = new Intent(MainActivity.this,WifiActivity.class);
                        startActivity(intent);
                    }else if (name.equals("stop recording")){
                        Intent intent = new Intent(MainActivity.this,CameraActivity.class);
                        databaseReference.child("FrontCamera").child("State").setValue("inactive");
                        startActivity(intent);
                    }else if (name.equals("accept the call")){
                        //Intent intent = new Intent(MainActivity.this,CameraActivity.class);
                        databaseReference.child("IncomingCall").child("AcceptOrDecline").setValue("A");
                        //startActivity(intent);
                    }else if (name.equals("cut the call")){
                        //Intent intent = new Intent(MainActivity.this,CameraActivity.class);
                         databaseReference.child("IncomingCall").child("AcceptOrDecline").setValue("D");
                        //startActivity(intent);
                    }
                    Toast.makeText(this, name, Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getContacts();
            }
        }
       // switch (requestCode){
           // case 0:
        if (grantResults.length > 0 &&  grantResults[0] == PackageManager.PERMISSION_GRANTED){
            Toast.makeText(getApplicationContext(), "Permission Granted, Now you can access sms", Toast.LENGTH_SHORT).show();
           // sendMySMS();

        }else {
            Toast.makeText(getApplicationContext(), "Permission Denied, You cannot access and sms", Toast.LENGTH_SHORT).show();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(SEND_SMS)) {
                    showMessageOKCancel("You need to allow access to both the permissions",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestPermissions(new String[]{SEND_SMS},
                                                REQUEST_SMS);
                                    }
                                }
                            });
                    return;
                }
            }
        }

                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if (ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(getApplicationContext(), "No Permission Granted", Toast.LENGTH_SHORT).show();
                }
       // }
        permissionsManager.onRequestPermissionsResult(requestCode,permissions,grantResults);

    }

    private boolean checkPermission() {
        return ( ContextCompat.checkSelfPermission(getApplicationContext(), SEND_SMS ) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{SEND_SMS}, REQUEST_SMS);
    }
    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
/*
    public static void sendMySMS() {

        String phone = CallReceiver.number;
        String message = "Call me later,busy now";

        //Check if the phoneNumber is empty
        if (phone.isEmpty()) {
            Toast.makeText(this, "Please Enter a Valid Phone Number", Toast.LENGTH_SHORT).show();
        } else {

            SmsManager sms = SmsManager.getDefault();
            // if message length is too long messages are divided
            List<String> messages = sms.divideMessage(message);
            for (String msg : messages) {

                PendingIntent sentIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_SENT"), 0);
                PendingIntent deliveredIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_DELIVERED"), 0);
                sms.sendTextMessage(phone, null, msg, sentIntent, deliveredIntent);

            }
        }
    }
    */
    @SuppressLint("MissingPermission")
    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();

        sentStatusReceiver=new BroadcastReceiver() {

            @Override
            public void onReceive(Context arg0, Intent arg1) {
                String s = "Unknown Error";
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        s = "Message Sent Successfully !!";
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        s = "Generic Failure Error";
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        s = "Error : No Service Available";
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        s = "Error : Null PDU";
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        s = "Error : Radio is off";
                        break;
                    default:
                        break;
                }
                //sendStatusTextView.setText(s);

            }
        };
        deliveredStatusReceiver=new BroadcastReceiver() {

            @Override
            public void onReceive(Context arg0, Intent arg1) {
                String s = "Message Not Delivered";
                switch(getResultCode()) {
                    case Activity.RESULT_OK:
                        s = "Message Delivered Successfully";
                        break;
                    case Activity.RESULT_CANCELED:
                        break;
                }
              //  deliveryStatusTextView.setText(s);
                //phoneEditText.setText("");
                //messageEditText.setText("");
            }
        };
        registerReceiver(sentStatusReceiver, new IntentFilter("SMS_SENT"));
        registerReceiver(deliveredStatusReceiver, new IntentFilter("SMS_DELIVERED"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        unregisterReceiver(sentStatusReceiver);
        unregisterReceiver(deliveredStatusReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        databaseReference.child("Application_Flag").setValue(0);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
/*
    @Override
    public boolean onMapLongClick(@NonNull LatLng point) {
        speak();
        return true;
    }
*/

/*
    @Override
    public void onPlaceSelected(CarmenFeature carmenFeature) {
        String json = carmenFeature.toJson();
        Intent returningIntent = new Intent();
        returningIntent.putExtra(PlaceConstants.RETURNING_CARMEN_FEATURE, json);
        setResult(AppCompatActivity.RESULT_OK, returningIntent);
        finish();
    }

    @Override
    public void onCancel() {
        setResult(AppCompatActivity.RESULT_CANCELED);
        finish();
    }*/
}
