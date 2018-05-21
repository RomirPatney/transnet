package whitewire.transnetandroid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.util.Base64;
import android.util.Log;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Info;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.model.Step;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.R.attr.bitmap;
import static android.R.attr.direction;
import static android.R.attr.key;
import static android.R.attr.mode;
import static android.R.id.message;
import static android.support.v7.widget.AppCompatDrawableManager.get;
import static com.google.android.gms.location.places.ui.PlaceAutocomplete.getStatus;
import static java.security.AccessController.getContext;
import static whitewire.transnetandroid.R.id.fromText;
import static whitewire.transnetandroid.R.id.toText;

/**
 * An activity that displays a map showing the place at the device's current location.
 */
public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    private static final String TAG = MapsActivity.class.getSimpleName();
    private GoogleMap mMap;
    private CameraPosition mCameraPosition;
    private Marker currentMarker;

    // The entry points to the Places API.
    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 5;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    int allowedSearches = 0;
    LatLng origin, destination = null;
    int cost;
    String email;
    Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        final TextView fromText = (TextView) findViewById(R.id.fromText);
        final TextView toText = (TextView) findViewById(R.id.toText);
        Button buyButton = (Button) findViewById(R.id.buy);

        buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Getting data from shared preferences
                SharedPreferences settings = PreferenceManager
                        .getDefaultSharedPreferences(getApplicationContext());
                email = settings.getString("email", "email");
                buyTicket(email, cost);
            }
        });

        final PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                if (allowedSearches == 0) {
                    allowedSearches++;
                    fromText.setText(place.getAddress().toString().split(",")[0]);
                    updateMap(place.getAddress().toString());
                } else if (allowedSearches == 1) {
                    allowedSearches++;
                    toText.setText(place.getAddress().toString().split(",")[0]);
                    updateMap(place.getAddress().toString());
                } else {
                    Toast.makeText(getApplicationContext(), "This will restart your search",
                            Toast.LENGTH_SHORT).show();
                    allowedSearches = 0;
                    fromText.setText("");
                    toText.setText("");
                    origin = null;
                    destination = null;
                }
                autocompleteFragment.setText("");
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this, null);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Build the map.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public void buyTicket(final String email, final int cost) {
        // Initializing request and defining URL
        /*RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        String url = "http://api-transnet.azurewebsites.net/api/Values/PurchasePost";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Given the request is successful, display a message
                        Toast.makeText(getApplicationContext(), "Your ride has successfully been purchased",
                                Toast.LENGTH_LONG).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Given there is an error with the request, display it and log it
                Toast.makeText(getApplicationContext(), "Error is: " + error.toString(),
                        Toast.LENGTH_LONG).show();
                //Log.e("VOLLEY", error.toString());
            }
        }) {
            // Sending data to API
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parameters = new HashMap<>();
                parameters.put("email", email);
                parameters.put("cost", String.valueOf(cost));
                return parameters;
            }

            // Method to finalize request
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                //params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };
        requestQueue.add(stringRequest);*/
        Toast.makeText(getApplicationContext(), "Your ride has successfully been purchased",
                Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    private String getRoute(LatLng origin, LatLng destination) {
        // Initializing request and defining URL

        return "";
    }

    private void getDirections(final LatLng origin, final LatLng destination) {
        final TextView durationLabel = (TextView) findViewById(R.id.distanceLabel);
        final TextView distanceLabel = (TextView) findViewById(R.id.durationLabel);
        final TextView durationText = (TextView) findViewById(R.id.durationText);
        final TextView distanceText = (TextView) findViewById(R.id.distanceText);
        final TextView costText = (TextView) findViewById(R.id.costText);
        final LinearLayout layout = (LinearLayout) findViewById(R.id.paymentLayout);

        // Setting up request
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        String url = "http://api-transnet.azurewebsites.net/api/Values/ThirdPartyGet";

        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);
        final int month = calendar.get(Calendar.MONTH);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response == "") {
                            Toast.makeText(getApplicationContext(),
                                    "No route found matching your criteria",
                                    Toast.LENGTH_LONG).show();
                        } else {
//                            String[] results = response.split(",");
//                            String lat1 = results[0];
//                            String lat2 = results[1];
//                            String lon1 = results[2];
//                            String lon2 = results[3];
//                            String mode = results[4];
                            Toast.makeText(getApplicationContext(),
                                    "Great news! We found a route for you!",
                                    Toast.LENGTH_LONG).show();
                        }

                        String serverKey = "AIzaSyCAWJqAAO0eD4138tt9sEjV-YLoMrH4BzI";
                        GoogleDirection.withServerKey(serverKey)
                                .from(origin)
                                .to(destination)
                                .execute(new DirectionCallback() {
                                    @Override
                                    public void onDirectionSuccess(Direction direction, String rawBody) {
                                        Route route = direction.getRouteList().get(0);
                                        Leg leg = route.getLegList().get(0);
        //                          List<Step> stepList= leg.getStepList();
        //                          ArrayList<LatLng> pointList = leg.getDirectionPoint();
        //
        //                          String travelMode = step.getTravelMode();
        //                          ArrayList<LatLng> sectionList = leg.getSectionPoint();
                                    String distance = leg.getDistance().getText();
                                    String duration = leg.getDuration().getText();
                                    durationLabel.setVisibility(View.VISIBLE);
                                    distanceLabel.setVisibility(View.VISIBLE);
                                    durationText.setText(duration);
                                    distanceText.setText(distance);

                                    // TODO set cost based on mode
                                    int mode = 1;
                                    cost = Integer.parseInt(distance.split(" ")[0]) * mode;
                                    costText.setText("$" + cost/10);

                                    ArrayList<LatLng> directionPositionList = leg.getDirectionPoint();
                                    PolylineOptions polylineOptions = DirectionConverter.createPolyline(getApplicationContext(),
                                            directionPositionList, 5, Color.RED);
                                    mMap.addPolyline(polylineOptions);

                                    layout.setVisibility(View.VISIBLE);
                                    layout.setBackgroundColor(Color.parseColor("#ffffff"));
                                }

                                    @Override
                                    public void onDirectionFailure(Throwable t) {
                                        Toast.makeText(getApplicationContext(), t.toString(),
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Given there is an error with the request, display it and log it
                Toast.makeText(getApplicationContext(), "Error is: " + error.toString(),
                        Toast.LENGTH_LONG).show();
                //Log.e("VOLLEY", error.toString());
            }
        }) {
            // Sending data to API
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parameters = new HashMap<>();
                parameters.put("fromMonth", String.valueOf(month));
                parameters.put("fromDay", String.valueOf(day));
                parameters.put("fromHour", String.valueOf(hour));
                parameters.put("lat1", String.valueOf(origin.latitude));
                parameters.put("lon1", String.valueOf(origin.longitude));
                parameters.put("lat2", String.valueOf(destination.latitude));
                parameters.put("lon2", String.valueOf(destination.longitude));
                return parameters;
            }

            // Method to finalize request
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                //params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    private void updateMap (String strAddress) {
        Geocoder gcd = new Geocoder(this);
        LatLng loc = null;
        try {
            List<Address> address = gcd.getFromLocationName(strAddress,5);
            if (address==null) {
                return;
            }
            Address location=address.get(0);
            location.getLatitude();
            location.getLongitude();
            loc = new LatLng(location.getLatitude(),location.getLongitude());
        } catch (IOException e) {
            Log.e(TAG, "Exception is: " + e.toString());
        }
        if (origin == null) {
            origin = loc;
        } else if (destination == null) {
            destination = loc;
            getDirections(origin, destination);
        }
        currentMarker = mMap.addMarker(new MarkerOptions()
                .position(loc)
                .title(strAddress.split(",")[0]));
        currentMarker.setTag(0);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
    }

    /**
     * Saves the state of the map when the activity is paused.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    /**
     * Manipulates the map when it's available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        // Use a custom info window adapter to handle multiple lines of text in the
        // info window contents.
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            // Return null here, so that getInfoContents() is called next.
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Inflate the layouts for the info window, title and snippet.
                View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents,
                        (FrameLayout) findViewById(R.id.map), false);

                TextView title = ((TextView) infoWindow.findViewById(R.id.title));
                title.setText(marker.getTitle());

                TextView snippet = ((TextView) infoWindow.findViewById(R.id.snippet));
                snippet.setText(marker.getSnippet());

                return infoWindow;
            }
        });

        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }


    /**
     * Prompts the user for permission to use the device location.
     */
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

    /**
     * Handles the result of the request for location permissions.
     */
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

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
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
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }
}