package de.ifgi.wayto_prototype.activities;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.maps.android.SphericalUtil;

/*import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;*/
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.ifgi.wayto_prototype.R;
import de.ifgi.wayto_prototype.demo.LandmarkCollection;
import de.ifgi.wayto_prototype.demo.PathSegmentCollection;
import de.ifgi.wayto_prototype.demo.Segment;
import de.ifgi.wayto_prototype.demo.Waypoint;
import de.ifgi.wayto_prototype.demo.WaypointCollection;
import de.ifgi.wayto_prototype.directions.DirectionsJSONParser;
import de.ifgi.wayto_prototype.landmarks.Landmark;
import de.ifgi.wayto_prototype.landmarks.PointLandmark;
import de.ifgi.wayto_prototype.landmarks.RegionalLandmark;
import de.ifgi.wayto_prototype.map.Heading;


/**
 * Main activity that displays the map
 *
 * @author Marius Runde, Heinrich Löwen
 */
public class MainActivity extends FragmentActivity implements GoogleMap.OnCameraChangeListener,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {

    /**
     * Tag for the logger
     */
    private final String TAG = MainActivity.class.toString();

    /**
     * Logger
     */
    private String logger = "";

    /**
     * Coordinates of the starting Point
     * eg. town hall LatLng(51.961563, 7.628187)
     * e.g. Geiststr_Peterstr LatLng(51.947197, 7.622430)
     */
    private final LatLng StartingPoint = new LatLng(51.947197, 7.622430);

    /**
     * Navigation running
     */
    private Boolean navigation_running = false;

    // --- Marker variables ---

    /**
     * Size of the markers
     */
    private final int SIZE_MARKER = 50;
    /**
     * Size of the circles underlying the markers
     */
    private final int SIZE_CIRCLE = 45;
    /**
     * Size of the arrows of the off-screen landmarks
     */
    private final int SIZE_ARROW = 50;
    /**
     * Distance of the arrows to their corresponding markers in pixels
     */
    private final int DISTANCE_ARROW = (SIZE_MARKER + SIZE_ARROW) / 2 + 10;
    /**
     * ID for on-screen markers
     */
    private final int MARKER_ON_SCREEN = 0;
    /**
     * ID for near off-screen markers
     */
    private final int MARKER_OFF_SCREEN_NEAR = 1;
    /**
     * ID for far off-screen markers
     */
    private final int MARKER_OFF_SCREEN_FAR = 2;

    private final int OFF_SCREEN_LANDMARKS_TOP = 1;
    private final int OFF_SCREEN_LANDMARKS_RIGHT = 3;
    private final int OFF_SCREEN_LANDMARKS_BOTTOM = 1;
    private final int OFF_SCREEN_LANDMARKS_LEFT = 3;
    // --- End of marker variables ---

    // --- Method variables ---

    // --- Off-screen landmarks ---
    /**
     * ID for the "normal arrow" method (inside, towards map center)
     */
    private final int METHOD_ARROW_INSIDE = 0;
    /**
     * ID for the "normal arrow" method (outside, away from map center)
     */
    private final int METHOD_ARROW_OUTSIDE = 1;
    /**
     * ID for the "not-distance-based arrow" method (inside, towards map center)
     */
    private final int METHOD_ARROW_NOT_DISTANCE_INSIDE = 2;
    /**
     * ID for the "not-distance-based arrow" method (outside, away from map center)
     */
    private final int METHOD_ARROW_NOT_DISTANCE_OUTSIDE = 3;
    /**
     * ID for the "distance-based pointer" method
     */
    private final int METHOD_POINTER = 4;
    /**
     * ID for the "wedge" method
     */
    private final int METHOD_WEDGE = 5;

    // --- Regional landmarks ---
    /**
     * ID for the "bounding box" method
     */
    private final int METHOD_BBOX = 0;
    /**
     * ID for the "polygon" method
     */
    private final int METHOD_POLYGON = 1;

    // --- End of method variables ---

    // --- Preferences variables ---

    /**
     * Shared preferences instance
     */
    private SharedPreferences preferences;
    /**
     * Preference value for following the user's position
     */
    private boolean prefMapFollow;
    /**
     * Preference value for enabling the compass
     */
    private boolean prefMapCompass;
    /**
     *
     */
    private boolean prefCompassTop;
    /**
     * Preference value for the map type (e.g. normal, hybrid, or satellite)
     */
    private int prefMapType;
    /**
     * Preference value for the usage of online landmarks
     */
    private boolean prefDownload;
    /**
     * Indicates whether the online landmarks already have been downloaded
     */
    private boolean notDownloadedYet = true;
    /**
     * Preference value for the URL where the online landmarks are stored
     */
    private String prefURL;
    /**
     * Preference value for the landmark whether they shall be displayed in colours or black
     */
    private boolean prefColoured;
    /**
     * Preference value indicating whether off-screen landmarks shall be displayed
     */
    private boolean prefMethod;
    /**
     * Preference value for the method for displaying off-screen landmarks (e.g. wedge)
     */
    private int prefMethodType;
    /**
     * Preference value for the method for displaying regional landmarks
     */
    private int prefMethodRegional;

    // --- End of preferences variables ---

    // --- Landmark and map variables ---

    /**
     * Vertical map ratio
     */
    private final double VERTICAL_MAP_RATIO = 16.44;
    /**
     * Horizontal map ratio
     */
    private final double HORIZONTAL_MAP_RATIO = 10;
    /**
     * Offset for the wayfinding instructions at the bottom
     */
    private int INSTRUCTIONS_OFFSET = 0;
    /**
     * Default instructions offset
     */
    private final int DEFAULT_INSTRUCTIONS_OFFSET = 200;
    /**
     * Pointer to the current progress in the navigation
     */
    private int navigationProgressPointer = 0;
    /**
     * List of pre-defined landmarks
     */
    private final ArrayList<Landmark> PRE_DEFINED_LANDMARKS = LandmarkCollection.initLandmarks();
    /**
     * List of pre-defined waypoints
     */
    private final ArrayList<Waypoint> PRE_DEFINED_WAYPOINTS = WaypointCollection.initWaypoints();
    /**
     * List of pre-defined path segments
     */
    private final ArrayList<Segment> PRE_DEFINED_PATHSEGMENTS = PathSegmentCollection.initSegmentPoints();
    /**
     * List of downloaded landmarks
     */
    private ArrayList<Landmark> landmarks = null;
    /**
     * Google Maps object
     */
    private GoogleMap map;
    /**
     * View on top of Google Map
     */
    private View mapTouchLayer;
    /**
     * Map/screen ratio
     */
    private double mapScreenRatio;
    /**
     * Marker whose info window is open
     */
    private Marker lastOpened;
    /**
     * Area which is covered by the markers and underlying circles
     */
    private ArrayList<LatLng> coveredArea = new ArrayList<LatLng>();
    /**
     * current camera position
     */
    private CameraPosition currentCameraPosition = new CameraPosition(StartingPoint, 14, 0, 0);
    /**
     * previous camera position
     */
    private CameraPosition previousCameraPosition;
    /**
     * Initial Zoom to Position and Loading of Landmarks
     */
    private boolean initialCameraChange = true;

    /**
     *
     */
    private final Double POSITION_CHANGED_THRESHOLD = 0.00001;
    private final Double BEARING_CHANGED_THRESHOLD = 1.0;

    // --- End of landmark and map variables ---

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Read the preferences
        loadPreferences();

        // Setup the map
        setupMap();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check for updates of the application preferences
        checkPreferences();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Funktion to handle the options menu
     *
     * @param item Gets the menu item that was selected by the user
     * @return boolean
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_item_settings:
                // Open the settings activity
                Intent intentSettings = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intentSettings);
                return true;
            case R.id.menu_item_start_navigation:
                if (navigation_running) {
                    navigation_running = false;
                    item.setTitle("Start navigation");
                    stopNavigationMode();
                } else {
                    navigation_running = true;
                    item.setTitle("Stop navigation");
                    startNavigationMode();
                }
                return true;
            case R.id.menu_item_export:
                // Export the log as email
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("message/rfc822");
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                intent.putExtra(Intent.EXTRA_TEXT, logger);
                Intent mailer = Intent.createChooser(intent, null);
                startActivity(mailer);
                // Reset the log
                logger = "";
                Toast.makeText(getApplicationContext(), getString(R.string.log_reset),
                        Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Starts the navigation mode
     */
    private void startNavigationMode() {
        Toast.makeText(getApplicationContext(), "Navigation started", Toast.LENGTH_SHORT).show();
        ViewGroup.LayoutParams params = findViewById(R.id.instructionsText).getLayoutParams();
        INSTRUCTIONS_OFFSET = DEFAULT_INSTRUCTIONS_OFFSET;
        params.height = DEFAULT_INSTRUCTIONS_OFFSET;
        findViewById(R.id.instructionsText).setLayoutParams(params);
        recalculateLandmarks(false);

        showNextRouteSegment(navigationProgressPointer);
        showNextRouteSegment(navigationProgressPointer + 1);
        showNextNavigationInstruction(navigationProgressPointer);
        navigationProgressPointer ++;

        findViewById(R.id.instructionsText).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (navigationProgressPointer<PRE_DEFINED_PATHSEGMENTS.size()){
                    removePreviousRouteSegment(navigationProgressPointer-1);
                    showNextRouteSegment(navigationProgressPointer + 1);
                    showNextNavigationInstruction(navigationProgressPointer);
                    navigationProgressPointer++;
                } else {
                    Toast.makeText(getApplicationContext(), "Navigation finished. Please stop navigation mode.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Stops the navigation mode
     */
    private void stopNavigationMode() {
        Toast.makeText(getApplicationContext(), "Navigation stopped", Toast.LENGTH_SHORT).show();
        ViewGroup.LayoutParams params = findViewById(R.id.instructionsText).getLayoutParams();
        INSTRUCTIONS_OFFSET = 0;
        params.height = 0;
        findViewById(R.id.instructionsText).setLayoutParams(params);
        recalculateLandmarks(false);

        navigationProgressPointer = 0;

        findViewById(R.id.instructionsText).setOnClickListener(null);

        for (int i = 0; i < PRE_DEFINED_PATHSEGMENTS.size(); i++) {
            Segment s = PRE_DEFINED_PATHSEGMENTS.get(i);
            if (s.getSegmentPolyline() != null) s.getSegmentPolyline().remove();
        }
    }

    private void showNextRouteSegment(int segment) {
        ArrayList<LatLng> points = null;
        PolylineOptions lineOptions = null;
        MarkerOptions markerOptions = new MarkerOptions();

        if (segment < PRE_DEFINED_PATHSEGMENTS.size()) {
            points = PRE_DEFINED_PATHSEGMENTS.get(segment).getSegmentPoints();
            lineOptions = new PolylineOptions();

            // Adding all the points in the route to LineOptions
            lineOptions.addAll(points);
            lineOptions.width(5);
            lineOptions.color(Color.RED);

            // Drawing polyline in the Google Map for the i-th route
            Polyline polyline = map.addPolyline(lineOptions);
            PRE_DEFINED_PATHSEGMENTS.get(segment).setSegmentPolyline(polyline);
        }
    }

    private void removePreviousRouteSegment(int segment) {
        if (segment >= 0) {
            Segment s = PRE_DEFINED_PATHSEGMENTS.get(segment);
            if (s.getSegmentPolyline() != null) s.getSegmentPolyline().remove();
        }
    }

    private void showNextNavigationInstruction(int instruction) {
        TextView tv = (TextView) findViewById(R.id.instructionsText);
        tv.setText(getResources().getStringArray(R.array.routeInstructions)[instruction]);
    }

    /**
     * Function that is called when the camera is changed.
     *
     * @param cameraPosition
     */
    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        logger += "Map moved to position: " + cameraPosition.target.toString() +
                " at bearing: " + cameraPosition.bearing +
                " at zoom level: " + cameraPosition.zoom +
                " at time: " + getCurrentTime() + "\n";
        Log.i(TAG, "Map moved to position: " + cameraPosition.target.toString() +
                " at bearing: " + cameraPosition.bearing +
                " at zoom level: " + cameraPosition.zoom +
                " at time: " + getCurrentTime());
        if (initialCameraChange) {
            Log.i(TAG, "Initial camera change");
            initialCameraChange = false;
            updateMap();
        }
        if (cameraChangedSignificantly(cameraPosition)) {
            if (currentCameraPosition != null) {
                previousCameraPosition = currentCameraPosition;
            }
            currentCameraPosition = cameraPosition;
            updateMap();
        }
    }

    /**
     *
     * @return
     */
    public CameraPosition getCurrentCameraPosition () {
        return this.currentCameraPosition;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        // Check if there is an open info window
        if (lastOpened != null) {
            // Close the info window
            lastOpened.hideInfoWindow();

            // Is the marker the same marker that was already open
            if (lastOpened.equals(marker)) {
                // Nullify the lastOpened object
                lastOpened = null;
                // Return so that the info window isn't opened again
                return true;
            }
        }

        logger += "Clicked on marker: " + marker.getTitle() + " at zoom level: " +
                map.getCameraPosition().zoom + " at time: " + getCurrentTime() +
                "\n";
        // Open the info window for the marker
        marker.showInfoWindow();
        // Re-assign the last opened such that we can close it later
        lastOpened = marker;

        // Event was handled by our code do not launch default behaviour.
        return true;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        logger += "Clicked on map at time: " + getCurrentTime() + "\n";
        // Set the lasted clicked marker to null, so the info window will be shown again
        lastOpened = null;
    }

    /**
     * Check if the cameraPosition changed significantly.
     * @param cameraPosition new camera position
     * @return boolean
     */
    private boolean cameraChangedSignificantly(CameraPosition cameraPosition) {
        if (currentCameraPosition == null) {
            return true;
        } else {
            if (Math.abs(currentCameraPosition.target.latitude - cameraPosition.target.latitude) > POSITION_CHANGED_THRESHOLD) {
                return true;
            } else if (Math.abs(currentCameraPosition.target.longitude - cameraPosition.target.longitude) > POSITION_CHANGED_THRESHOLD) {
                return true;
            } else if (Math.abs(currentCameraPosition.bearing - cameraPosition.bearing) > BEARING_CHANGED_THRESHOLD) {
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * Function to get the current time for the logger
     *
     * @return Current time as plain text
     */
    private String getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        Date currentTime = calendar.getTime();
        return currentTime.toString();
    }

    /**
     * Load the shared preferences
     */
    private void loadPreferences() {
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefMapFollow = preferences.getBoolean(SettingsActivity.PREF_KEY_MAP_FOLLOW, false);
        prefMapCompass = preferences.getBoolean(SettingsActivity.PREF_KEY_MAP_COMPASS, false);
        prefCompassTop = preferences.getBoolean(SettingsActivity.PREF_KEY_COMPASS_TOP, false);
        prefMapType = Integer.valueOf(
                preferences.getString(SettingsActivity.PREF_KEY_MAP_TYPE,
                        getString(R.string.map_type_normal)));
        prefDownload = preferences.getBoolean(SettingsActivity.PREF_KEY_DOWNLOAD, false);
        prefURL = preferences.getString(SettingsActivity.PREF_KEY_URL, null);
        prefColoured = preferences.getBoolean(SettingsActivity.PREF_KEY_COLOURED, false);
        prefMethod = preferences.getBoolean(SettingsActivity.PREF_KEY_METHOD, true);
        prefMethodType = Integer.valueOf(
                preferences.getString(SettingsActivity.PREF_KEY_METHOD_TYPE, "2"));
        prefMethodRegional = Integer.valueOf(
                preferences.getString(SettingsActivity.PREF_KEY_METHOD_REGIONAL, "1"));
    }

    /**
     * Function to check whether a preferences has been changed in which case the required actions
     * will be started
     */
    private void checkPreferences() {
        updateMapRotatingAndFollowing();
        if (prefMapCompass) {
            map.getUiSettings().setCompassEnabled(true);
        } else {
            map.getUiSettings().setCompassEnabled(false);
        }
        int mapType = Integer.valueOf(
                preferences.getString(SettingsActivity.PREF_KEY_MAP_TYPE,
                        getString(R.string.map_type_normal)));
        if (prefMapType != mapType) {
            prefMapType = mapType;
            updateMapType();
        }
        boolean download = preferences.getBoolean(SettingsActivity.PREF_KEY_DOWNLOAD, false);
        if (prefDownload != download) {
            prefDownload = download;
            prefURL = preferences.getString(SettingsActivity.PREF_KEY_URL, null);
            if (prefURL == null && prefDownload) {
                Toast.makeText(getApplicationContext(), getString(R.string.log_url_missing_error),
                        Toast.LENGTH_SHORT).show();
                prefDownload = false;
            } else {
                notDownloadedYet = true;
                updateMap();
            }
        }
        boolean coloured = preferences.getBoolean(SettingsActivity.PREF_KEY_COLOURED, false);
        if (prefColoured != coloured) {
            prefColoured = coloured;
            updateMap();
        }
        boolean method = preferences.getBoolean(SettingsActivity.PREF_KEY_METHOD, true);
        if (prefMethod != method) {
            prefMethod = method;
            updateMap();
        }
        int methodType = Integer.valueOf(
                preferences.getString(SettingsActivity.PREF_KEY_METHOD_TYPE, "2"));
        if (prefMethodType != methodType) {
            prefMethodType = methodType;
            updateMap();
        }
        int methodRegional = Integer.valueOf(
                preferences.getString(SettingsActivity.PREF_KEY_METHOD_REGIONAL, "1"));
        if (prefMethodRegional != methodRegional) {
            prefMethodRegional = methodRegional;
            updateMap();
        }
    }

    /**
     * Set up the map
     */
    private void setupMap() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (map == null) {
            // Try to obtain the map from the SupportMapFragment.
            map = ((SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map)).getMap();
            // Check if we were successful in obtaining the map and then set it up
            // Please note: Some setup is already done in the layout XML file!
            if (map != null) {
                // Set map type
                setMapType();
                // Disable buildings
                map.setBuildingsEnabled(false);
                // Disable indoor maps
                map.setIndoorEnabled(false);
                // Disable the zoom buttons
                map.getUiSettings().setZoomControlsEnabled(false);
                // Enable MyLocation but disable the corresponding button
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                    @Override
                    public boolean onMyLocationButtonClick() {
                        setMapFollowingListenerEnabled(true, prefCompassTop);
                        return false;
                    }
                });
                // Animate to starting point
                animateTo(StartingPoint, 0, 14);
                // Set OnCameraChangeListener
                map.setOnCameraChangeListener(this);
                // Set OnMarkerClickListener
                map.setOnMarkerClickListener(this);
                // Set OnMapClickListener
                map.setOnMapClickListener(this);
                // Angela set rotation false
                map.getUiSettings().setRotateGesturesEnabled(true);
                /*Polyline line = map.addPolyline(new PolylineOptions()
                        .add(new LatLng(51.954611, 7.624338),
                                new LatLng(51.951483, 7.627567))
                        .geodesic(true));*/
            } else {
                // Cannot create map
                String message = getString(R.string.log_map_cannot_create_map);
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                Log.e(TAG, message);
            }
        }

        if (mapTouchLayer == null) {
            mapTouchLayer = findViewById(R.id.map_touch_layer);
        } else {
            // Cannot create View Layer
            String message = getString(R.string.log_map_cannot_create_view_layer);
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            Log.e(TAG, message);
        }

        showRoute();
    }

    private void showRoute() {
/*
        LatLng origin = new LatLng(51.954611,7.624338);
        LatLng dest = new LatLng(51.951483,7.627567);
*/

/*
 * use to show route from origin to destination
        //Use the google directions api to request a route. Here we access a route for walking form the specified origin to the destination
        String url = getDirectionsUrl(origin, dest);
        Log.d(TAG, "Route url" + url);
        DownloadRoute routeFile = new DownloadRoute();
        routeFile.execute(url);
*/

/*
 * use to show all routes for segments
        ArrayList<Waypoint> waypoints = (ArrayList<Waypoint>) PRE_DEFINED_WAYPOINTS.clone();
        String url;

        for (int i = 1; i<waypoints.size(); i++) {
            if (i==6 || i==7) {
                url = getDirectionsUrl(waypoints.get(i).getPosition(), waypoints.get(i-1).getPosition());
                Log.d(TAG, "Route url" + url);
                DownloadRoute routeFile = new DownloadRoute();
                routeFile.execute(url);
            } else {
                url = getDirectionsUrl(waypoints.get(i - 1).getPosition(), waypoints.get(i).getPosition());
                Log.d(TAG, "Route url" + url);
                DownloadRoute routeFile = new DownloadRoute();
                routeFile.execute(url);
            }
        }
*/

/*
 * use to show the whole route
        ArrayList<Segment> pathSegments = (ArrayList<Segment>) PRE_DEFINED_PATHSEGMENTS.clone();

        // Traversing through all the routes
        for(int i=0;i<pathSegments.size();i++){
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            points = pathSegments.get(i).getSegmentPoints();
            lineOptions = new PolylineOptions();

            // Adding all the points in the route to LineOptions
            lineOptions.addAll(points);
            lineOptions.width(5);
            lineOptions.color(Color.RED);

            // Drawing polyline in the Google Map for the i-th route
            map.addPolyline(lineOptions);
        }
*/

    }

    private String getDirectionsUrl(LatLng origin,LatLng dest){
        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Parameter for the mode of travelling
        String travellingMode = "mode=walking";

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor+"&"+travellingMode;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

        return url;
    }


    private void computeMapScreenRatio() {
        // Get the map bounds
        VisibleRegion vr = map.getProjection().getVisibleRegion();
        // upperLeft and upperRight
        double mapWidth = SphericalUtil.computeDistanceBetween(vr.farLeft, vr.farRight);
        // upperLeft and lowerLeft
        double mapHeight = SphericalUtil.computeDistanceBetween(vr.farLeft, vr.nearLeft);

        // Get the screen bounds
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenWidth = size.x;
        int screenHeight = size.y;
        Log.i(TAG, "display size: " + screenWidth + "," + screenHeight);

        // Compute the horizontal and vertical ratios
        double horizontalRatio = mapWidth / screenWidth;
        double verticalRatio = mapHeight / screenHeight;

        // Use the maximum ratio for the map/screen ratio
        this.mapScreenRatio = Math.max(horizontalRatio, verticalRatio);
    }

    /**
     * Move the map
     *
     * @param destination Destination to move to
     * @param zoom        Zoom level
     */
    private void animateTo(LatLng destination, float bearing, float zoom) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(destination)
                .zoom(zoom).bearing(bearing)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    /**
     * Display the landmarks on the map
     * This method is deprecated. Use recalculateLandmarks() instead.
     *
     * @param useOnlineLandmarks Indicates whether the pre-defined or downloaded landmarks shall be
     *                           used
     */
    private void displayLandmarks(boolean useOnlineLandmarks) {
        // Clear the covered area
        coveredArea = new ArrayList<LatLng>();

        // Copy the list of landmarks
        ArrayList<Landmark> onScreenLandmarks = new ArrayList<Landmark>();
        if (landmarks == null || !useOnlineLandmarks) {
            // Store all pre-defined landmarks temporarily in another list
            onScreenLandmarks = (ArrayList<Landmark>) PRE_DEFINED_LANDMARKS.clone();
        } else {
            onScreenLandmarks = (ArrayList<Landmark>) landmarks.clone();
        }

        // Get the bounding box of the displayed map and the map center
        LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
        LatLng mapCenter = new LatLng(bounds.getCenter().latitude, bounds.getCenter().longitude);

        // Get all on-screen candidate landmarks and display the regional ones
        ArrayList<Landmark> onScreenCandidates = new ArrayList<Landmark>();
        for (Landmark l : onScreenLandmarks) {
            if (bounds.contains(l.getPosition())) {
                //if (markerBounds.contains(l.getPosition())) {
                if (((Object) l).getClass() == PointLandmark.class) {
                    // Check if the landmark's position is already covered on the map
                    if (isAreaFree(l.getPosition())) {
                        coveredArea.add(l.getPosition());
                        // Add point landmark to list of on-screen candidates
                        onScreenCandidates.add(l);
                    }
                } else {
                    // Display this regional landmark
                    addLandmarkToMap(l, MARKER_ON_SCREEN);
                }
            }
        }

        for (Landmark l : onScreenCandidates) {
            // Remove this landmark from the list of all landmarks
            onScreenLandmarks.remove(l);
        }

        // Display the on-screen candidate point landmarks, so that they lay above the regional ones
        for (Landmark l : onScreenCandidates) {
            addLandmarkToMap(l, MARKER_ON_SCREEN);
        }

        // Get all off-screen candidate landmarks
        ArrayList<Landmark> offScreenCandidates = new ArrayList<Landmark>();
        for (Landmark l : onScreenLandmarks) {
            if (getDistance(l) <= l.getReferenceRadius()) {
                // Map center is covered by reference radius of landmark
                offScreenCandidates.add(l);
            }
        }

        if (offScreenCandidates.size() > 0) {
            // Remove redundant landmarks based on their headings and distances
            ArrayList<Landmark> filteredCandidates = filterLandmarks(offScreenCandidates);

            // Display the filtered off-screen landmarks
            for (int i = 0; i < filteredCandidates.size(); i++) {
                Landmark l = filteredCandidates.get(i);
                if (map.getClass() == GoogleMap.class) {
                    l.setOffScreenPosition(computeOffScreenPosition(l));
                }
                // Check if the landmark's position is already covered on the map
                if (isAreaFree(l.getOffScreenPosition())) {
                    coveredArea.add(l.getPosition());
                    if (i <= filteredCandidates.size() / 2) {
                        addLandmarkToMap(l, MARKER_OFF_SCREEN_NEAR);
                    } else {
                        addLandmarkToMap(l, MARKER_OFF_SCREEN_FAR);
                    }
                } else {
                    //TODO implemente funktion to shift landmark, if area is already covered

                }
            }
        }
    }

    /**
     * This is a class to get the JSON file asynchronously from the given URL.
     *
     * @author Marius Runde
     */
    private class GetJsonTask extends AsyncTask<String, Void, JSONArray> {

        /**
         * Class name for the logger
         */
        private final String LOG = GetJsonTask.class.toString();

        /**
         * Progress dialog to inform the user about the download
         */
        private ProgressDialog progressDialog = new ProgressDialog(
                MainActivity.this);

        /**
         * Count the time needed for the data download
         */
        private int downloadTimer;

        @Override
        protected void onPreExecute() {
            // Display progress dialog
            progressDialog.setMessage("Downloading landmarks...");
            progressDialog.show();
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    // Cancel the download when the "Cancel" button has been
                    // clicked
                    GetJsonTask.this.cancel(true);
                }
            });

            // Set timer to current time
            downloadTimer = Calendar.getInstance().get(Calendar.SECOND);
        }

        @Override
        protected JSONArray doInBackground(String... url) {
            // Get the data from the URL
            String output = "";
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response;
            try {
                response = httpclient.execute(new HttpGet(url[0]));
                StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    out.close();
                    output = out.toString();
                } else {
                    // Close the connection
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }
            } catch (Exception e) {
                Log.e(LOG, "Could not get the data. This is the error message: " + e.getMessage());
                return null;
            }

            // Convert the output to a JSONObject
            try {
                JSONArray result = new JSONArray(output);
                return result;
            } catch (JSONException e) {
                Log.e(LOG, "Could not convert output to JSONObject. This is the error message: "
                        + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONArray result) {
            // Dismiss progress dialog
            progressDialog.dismiss();

            // Write the time needed for the download into the log
            downloadTimer = Calendar.getInstance().get(Calendar.SECOND)
                    - downloadTimer;
            Log.i(LOG, "Completed landmark download in " + downloadTimer + " seconds");

            // Check if the download was successful
            if (result == null) {
                // Could not receive the JSON
                Toast.makeText(MainActivity.this,
                        getResources().getString(R.string.log_landmark_download_error),
                        Toast.LENGTH_SHORT).show();
                displayLandmarks(false);
            } else {
                // Transform the JSONObject into an ArrayList of landmarks
                ArrayList<Landmark> downloadedLandmarks = new ArrayList<Landmark>();

                try {
                    // Get the point landmarks
                    JSONArray pointLandmarks = result.getJSONObject(0)
                            .getJSONArray("point_landmarks");
                    for (int i = 0; i < pointLandmarks.length(); i++) {
                        JSONObject o = pointLandmarks.getJSONObject(i);
                        int drawableBlack = getResources().getIdentifier(
                                "landmark_" + o.getString("category"), "drawable",
                                getPackageName());
                        int drawableColoured = getResources().getIdentifier(
                                "landmark_coloured_" + o.getString("category"), "drawable",
                                getPackageName());
                        PointLandmark pl = new PointLandmark(
                                o.getString("title"),
                                o.getDouble("radius"),
                                drawableBlack,
                                drawableColoured,
                                new LatLng(o.getDouble("lat"), o.getDouble("lng"))
                        );
                        downloadedLandmarks.add(pl);
                    }

                    // Get the regional landmarks
                    JSONArray regionalLandmarks = result.getJSONObject(1)
                            .getJSONArray("regional_landmarks");
                    for (int i = 0; i < regionalLandmarks.length(); i++) {
                        JSONObject o = regionalLandmarks.getJSONObject(i);
                        int drawableBlack = getResources().getIdentifier(
                                "landmark_" + o.getString("category"), "drawable",
                                getPackageName());
                        int drawableColoured = getResources().getIdentifier(
                                "landmark_coloured_" + o.getString("category"), "drawable",
                                getPackageName());
                        JSONArray points = o.getJSONArray("points");
                        LatLng[] shapePoints = new LatLng[points.length()];
                        for (int j = 0; j < points.length(); j++) {
                            shapePoints[j] = new LatLng(points.getJSONObject(j).getDouble("lat"),
                                    points.getJSONObject(j).getDouble("lng"));
                        }
                        LatLng[] shapePointsArray = shapePoints.clone();
                        LatLng centroid = new LatLng(o.getDouble("centroid_lat"),
                                o.getDouble("centroid_lng"));
                        RegionalLandmark rl = new RegionalLandmark(
                                o.getString("title"),
                                o.getDouble("radius"),
                                drawableBlack,
                                drawableColoured,
                                shapePointsArray,
                                centroid
                        );

                        downloadedLandmarks.add(rl);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    // Copy the list into the class-wide list
                    landmarks = downloadedLandmarks;

                    // Display the downloaded landmarks
                    notDownloadedYet = false;
                    displayLandmarks(true);
                }
            }
        }
    }

    /**
     * A class to download a specified route asynchronous and display it on the map.
     */
    private class DownloadRoute extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... url) {
            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }


        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }

        private String downloadUrl(String strUrl) throws IOException{
            String data = "";
            InputStream iStream = null;
            HttpURLConnection urlConnection = null;
            try{
                URL url = new URL(strUrl);

                // Creating an http connection to communicate with url
                urlConnection = (HttpURLConnection) url.openConnection();

                // Connecting to url
                urlConnection.connect();

                // Reading data from url
                iStream = urlConnection.getInputStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

                StringBuffer sb = new StringBuffer();

                String line = "";
                while( ( line = br.readLine()) != null){
                    sb.append(line);
                }

                data = sb.toString();

                br.close();

            }catch(Exception e){
                Log.d(TAG, "Exception while downloading url" + e.toString());
            }finally{
                iStream.close();
                urlConnection.disconnect();
            }
            return data;
        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            Log.i(TAG, "JSON Routes" + routes);
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            // Traversing through all the routes
            for(int i=0;i<result.size();i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(5);
                lineOptions.color(Color.RED);
            }

            // Drawing polyline in the Google Map for the i-th route
            Log.i(TAG, "JSON Routes points" + points);
            map.addPolyline(lineOptions);
        }
    }

    /**
     * Function to check whether the area is already covered by another landmark
     *
     * @param landmarkPosition Landmark's position to be checked
     * @return <code>TRUE</code>: area is not covered
     * <code>FALSE</code>: area is already covered
     */
    private boolean isAreaFree(LatLng landmarkPosition) {
        for (LatLng point : coveredArea) {
            if (SphericalUtil.computeDistanceBetween(point, landmarkPosition) <
                    (SIZE_CIRCLE * 2 * this.mapScreenRatio)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Add landmark to the map
     *
     * @param landmark Landmark to be displayed
     * @param distance Level of distance:
     *                 0 = on-screen
     *                 1 = off-screen (near)
     *                 2 = off-screen (far)
     */
    private void addLandmarkToMap(Landmark landmark, int distance) {
        // at a zoomlevel < 14 transform regional landmark into point landmark
        if (((Object) landmark).getClass() == PointLandmark.class ||
                map.getCameraPosition().zoom < 14) {
            // --- Add PointLandmark ---
            PointLandmark tempLandmark = null;
            if (((Object) landmark).getClass() == RegionalLandmark.class) {
                tempLandmark =
                        ((RegionalLandmark) landmark).transformIntoPointLandmark();
            } else {
                tempLandmark = (PointLandmark) landmark;
            }

            // Modify the drawable to adjust the size of it
            BitmapDrawable bitmapDrawable;
            if (prefColoured) {
                bitmapDrawable = (BitmapDrawable) getResources()
                        .getDrawable(tempLandmark.getCategoryDrawableColoured());
            } else {
                bitmapDrawable = (BitmapDrawable) getResources()
                        .getDrawable(tempLandmark.getCategoryDrawableBlack());
            }
            Bitmap bitmap = bitmapDrawable.getBitmap();
            Bitmap icon = Bitmap.createScaledBitmap(bitmap, SIZE_MARKER, SIZE_MARKER,
                    false);

            // Get the correct position where the landmark shall be displayed
            LatLng displayedPosition = tempLandmark.getPosition();
            if (distance != MARKER_ON_SCREEN) {
                if (prefMethod) {
                    displayedPosition = tempLandmark.getOffScreenPosition();
                } else {
                    return;
                }
            }

            // Add the underlying circle to the map
            addCircleToMap(landmark, displayedPosition);

            // Add the landmark as a marker to the map
            Marker marker = map.addMarker(new MarkerOptions()
                            .anchor(0.5f, 0.5f)
                            .draggable(false)
                            .icon(BitmapDescriptorFactory.fromBitmap(icon))
                            .position(displayedPosition)
                            .title(tempLandmark.getTitle())
                            .visible(true)
            );
            landmark.setLandmarkMarker(marker);
            Log.d(TAG, getString(R.string.log_map_point_landmark_added) +
                    displayedPosition.toString());

            // Check if the landmark is an off-screen landmark and shall be displayed
            if (distance != MARKER_ON_SCREEN && prefMethod) {
                // Use preferred method for off-screen methods
                switch (prefMethodType) {
                    case METHOD_ARROW_INSIDE:
                        // Use the "normal arrow" method (inside)
                        addArrowToMap(tempLandmark, distance, true);
                        break;
                    case METHOD_ARROW_OUTSIDE:
                        // Use the "normal arrow" method (outside)
                        addArrowToMap(tempLandmark, distance, false);
                        break;
                    case METHOD_ARROW_NOT_DISTANCE_INSIDE:
                        // Use the "not-distance-based arrow" method (inside)
                        addArrowToMap(tempLandmark, MARKER_OFF_SCREEN_NEAR, true);
                        break;
                    case METHOD_ARROW_NOT_DISTANCE_OUTSIDE:
                        // Use the "not-distance-based arrow" method (outside)
                        addArrowToMap(tempLandmark, MARKER_OFF_SCREEN_NEAR, false);
                        break;
                    case METHOD_POINTER:
                        // Use the "distance-based pointer" method
                        Toast.makeText(getApplicationContext(), "Work in progress",
                                Toast.LENGTH_SHORT).show();
                        addPointerToMap(tempLandmark);
                        break;
                    case METHOD_WEDGE:
                        // Use the "wedge" method
                        addWedgeToMap(tempLandmark);
                        break;
                }
            }
        } else {
            // --- Add RegionalLandmark ---

            // Use preference method for regional landmarks
            switch (prefMethodRegional) {
                case METHOD_BBOX:
                    LatLngBounds bounds = ((RegionalLandmark) landmark).getBounds();
                    map.addPolygon(new PolygonOptions()
                                    .add(bounds.southwest)
                                    .add(new LatLng(bounds.southwest.latitude,
                                            bounds.northeast.longitude))
                                    .add(bounds.northeast)
                                    .add(new LatLng(bounds.northeast.latitude,
                                            bounds.southwest.longitude))
                                    .visible(true)
                    );
                    // TODO add to landmark class
                    Log.d(TAG, getString(R.string.log_map_bbox_added) + landmark.getPosition());
                    break;
                case METHOD_POLYGON:
                    ArrayList<LatLng> shapePoints = new ArrayList<>();
                    Collections.addAll(shapePoints, ((RegionalLandmark) landmark).getShapePoints());
                    map.addPolygon(new PolygonOptions()
                                    .addAll(shapePoints)
                                    .visible(true)
                    );
                    // TODO add to landmark class
                    Log.d(TAG, getString(R.string.log_map_regional_landmark_added) +
                            shapePoints.toString());
                    break;
            }
        }
    }

    /**
     * Removes the landmark from the map.
     *
     * @param l Specified Landmark
     */
    private void removeLandmarkFromMap(Landmark l) {
        // Remove Landmark
        if (l.getLandmarkMarker() != null) l.getLandmarkMarker().remove();
        if (l.getLandmarkMarkerCircle() != null) l.getLandmarkMarkerCircle().remove();
        if (l.getLandmarkMarkerArrow() != null) l.getLandmarkMarkerArrow().remove();
        if (l.getLandmarkMarkerPolygon() != null) l.getLandmarkMarkerPolygon().remove();
        if (l.getLandmarkMarkerWedge() != null) l.getLandmarkMarkerWedge().remove();

        // Remove covered Area entry
        if (coveredArea.contains(l.getPosition())) {
            coveredArea.remove(coveredArea.indexOf(l.getPosition()));
        }
        if (coveredArea.contains(l.getOffScreenPosition())) {
            coveredArea.remove(coveredArea.indexOf(l.getOffScreenPosition()));
        }
    }

    /**
     * Function to draw a circle below the markers
     *
     * @param position Position of the circle
     */
    private void addCircleToMap(Landmark l, LatLng position) {
        // Compute the radius
        int radius = (int) (SIZE_CIRCLE * this.mapScreenRatio);

        // Draw the circle
        int d = 500; // diameter
        Bitmap bitmap = Bitmap.createBitmap(d, d, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        Paint p = new Paint();
        p.setColor(Color.WHITE);
        c.drawCircle(d / 2, d / 2, d / 2, p);

        // mapView is the GoogleMap
        GroundOverlay groundOverlay = map.addGroundOverlay(new GroundOverlayOptions()
                        .image(BitmapDescriptorFactory.fromBitmap(bitmap))
                        .position(position, radius * 2, radius * 2)
                        .transparency(0.4f)
        );
        l.setLandmarkMarkerCircle(groundOverlay);
    }

    /**
     * Function to display the "normal arrow" method for the off-screen landmark
     *
     * @param landmark Landmark this function refers to
     * @param distance Indicates whether the off-screen landmark is near or far away
     * @param inside   Indicates whether the arrow is shown "inside" or "outside" of the landmark
     *                 in relation to the map center
     */
    private void addArrowToMap(Landmark landmark, int distance, boolean inside) {
        // Get the heading (from the map center to the landmark)
        double heading = getHeading(landmark);

        //rotation of arrow according to bearing
        heading = heading -(360-currentCameraPosition.bearing);

        // Compute the reverse heading (from the landmark to the map center)
        double reverseHeading = heading;
        int rotation = (int) (reverseHeading + 360) % 360;
        if (reverseHeading > 0) {
            reverseHeading -= 180;
        } else {
            reverseHeading += 180;
        }

        Log.i("HeadingBug", "Reverse Heading " + reverseHeading + " rotation " + rotation);

        // Get the correct drawable depending on the current map type
        BitmapDrawable bitmapDrawable;
        if (prefMapType == GoogleMap.MAP_TYPE_HYBRID ||
                prefMapType == GoogleMap.MAP_TYPE_SATELLITE) {
            if (distance == MARKER_OFF_SCREEN_NEAR) {
                bitmapDrawable = (BitmapDrawable) getResources()
                        .getDrawable(R.drawable.arrow_near_white);
            } else {
                bitmapDrawable = (BitmapDrawable) getResources()
                        .getDrawable(R.drawable.arrow_far_white);
            }
        } else {
            if (distance == MARKER_OFF_SCREEN_NEAR) {
                bitmapDrawable = (BitmapDrawable) getResources()
                        .getDrawable(R.drawable.arrow_near_black);
            } else {
                bitmapDrawable = (BitmapDrawable) getResources()
                        .getDrawable(R.drawable.arrow_far_black);
            }
        }
        Bitmap arrow = bitmapDrawable.getBitmap();
        // Rescale the arrow
        Bitmap scaledArrow = Bitmap.createScaledBitmap(arrow, SIZE_ARROW, SIZE_ARROW, false);

        // Rotate the arrow
        Matrix matrix = new Matrix();
        matrix.postRotate(rotation);
        Bitmap rotatedArrow = Bitmap.createBitmap(scaledArrow, 0, 0, SIZE_ARROW, SIZE_ARROW,
                matrix, false);

        // Compute the offset position of the arrow
        LatLng arrowPositionNear;
        if (inside) {
            arrowPositionNear = SphericalUtil.computeOffset(landmark.getOffScreenPosition(),
                    DISTANCE_ARROW * this.mapScreenRatio, reverseHeading);
        } else {
            arrowPositionNear = SphericalUtil.computeOffset(landmark.getOffScreenPosition(),
                    DISTANCE_ARROW * this.mapScreenRatio, heading);
        }
        // Display the arrow
        Marker marker = map.addMarker(new MarkerOptions()
                        .anchor(0.5f, 0.5f).flat(true)
                        .draggable(false)
                        .icon(BitmapDescriptorFactory.fromBitmap(rotatedArrow))
                        .position(arrowPositionNear)
                        .visible(true)
        );
        landmark.setLandmarkMarkerArrow(marker);
    }

    /**
     * Function to display the "distance-based pointer" method for the off-screen landmark
     *
     * @param landmark Landmark this function refers to
     */
    private void addPointerToMap(Landmark landmark) {
        // TODO implement
    }

    /**
     * Function to display the "wedge" method for the off-screen landmark
     *
     * @param landmark Landmark this function refers to
     */
    private void addWedgeToMap(Landmark landmark) {
        // Get a point at the closest edge of the screen
        LatLng pointAtScreenEdge = null;
        double lat, lng;
        if (landmark.getPosition().longitude <
                map.getProjection().getVisibleRegion().farLeft.longitude) {
            lng = map.getProjection().getVisibleRegion().farLeft.longitude;
        } else if (landmark.getPosition().longitude >
                map.getProjection().getVisibleRegion().farRight.longitude) {
            lng = map.getProjection().getVisibleRegion().farRight.longitude;
        } else {
            lng = landmark.getPosition().longitude;
        }
        if (landmark.getPosition().latitude <
                map.getProjection().getVisibleRegion().nearLeft.latitude) {
            lat = map.getProjection().getVisibleRegion().nearLeft.latitude;
        } else if (landmark.getPosition().latitude >
                map.getProjection().getVisibleRegion().farRight.latitude) {
            lat = map.getProjection().getVisibleRegion().farRight.latitude;
        } else {
            lat = landmark.getPosition().latitude;
        }
        switch (getHeadingID(landmark)) {
            case Heading.TOP_ID:
                pointAtScreenEdge = new LatLng(
                        map.getProjection().getVisibleRegion().farRight.latitude,
                        lng
                );
                break;
            case Heading.RIGHT_ID:
                pointAtScreenEdge = new LatLng(
                        lat,
                        map.getProjection().getVisibleRegion().farRight.longitude
                );
                break;
            case Heading.BOTTOM_ID:
                pointAtScreenEdge = new LatLng(
                        map.getProjection().getVisibleRegion().nearLeft.latitude,
                        lng
                );
                break;
            case Heading.LEFT_ID:
                pointAtScreenEdge = new LatLng(
                        lat,
                        map.getProjection().getVisibleRegion().nearLeft.longitude
                );
                break;
        }
        // Calculate the distance from the landmark to the edge of the screen (in pixels)
        double distanceToScreen = SphericalUtil.computeDistanceBetween(landmark.getPosition(),
                pointAtScreenEdge) / mapScreenRatio;

        // Calculate the length of the legs (in pixels)
        double leg = distanceToScreen + Math.log((distanceToScreen + 20) / 12) * 10;
        // Calculate the length of half of the base (in pixels)
        double halfBase = ((5 + getDistance(landmark) * 0.3) / leg) / 2 * 100;
        // Calculate the distance from the landmark to the base (Pythagorean theorem) (in meters)
        double distanceToBase =
                Math.sqrt(Math.pow(leg, 2) - Math.pow(halfBase, 2)) * mapScreenRatio;
        // Calculate the point lying on the base line
        LatLng pointOnBase = SphericalUtil.computeOffsetOrigin(landmark.getPosition(),
                distanceToBase, getHeading(landmark));

        // Calculate the shape points of the wedge
        LatLng wedgePoint1 = SphericalUtil.computeOffset(pointOnBase, halfBase,
                (getHeading(landmark) + 90) % 360);
        LatLng wedgePoint2 = SphericalUtil.computeOffset(pointOnBase, halfBase,
                (getHeading(landmark) - 90) % 360);

        // Display the wedge
        if (prefMapType == GoogleMap.MAP_TYPE_HYBRID ||
                prefMapType == GoogleMap.MAP_TYPE_SATELLITE) {
            Polygon polygon = map.addPolygon(new PolygonOptions()
                            .add(landmark.getPosition(), wedgePoint1, wedgePoint2)
                            .strokeColor(Color.WHITE)
            );
            landmark.setLandmarkMarkerPolygon(polygon);
        } else {
            Polygon polygon = map.addPolygon(new PolygonOptions()
                            .add(landmark.getPosition(), wedgePoint1, wedgePoint2)
                            .strokeColor(Color.BLACK)
            );
            landmark.setLandmarkMarkerPolygon(polygon);
        }
        Log.v(TAG, getString(R.string.log_map_wedge_added) + landmark.toString() + " " +
                wedgePoint1.toString() + " " + wedgePoint2.toString());
    }

    /**
     * Get the distance from the map's center to the landmark
     *
     * @param landmark Landmark to calculate the distance to
     * @return Distance to landmark
     */
    private double getDistance(Landmark landmark) {
        // Get the current center of the map
        LatLng mapCenter = map.getCameraPosition().target;

        // Calculate and return the distance
        return SphericalUtil.computeDistanceBetween(mapCenter, landmark.getPosition());
    }

    /**
     * Get the heading from the map's center to the landmark
     *
     * @param landmark Landmark to get the heading to
     * @return Heading to the landmark
     */
    private double getHeading(Landmark landmark) {
        // Get the current center of the map
        LatLng mapCenter = map.getCameraPosition().target;

        // Calculate and return the heading
        double heading =  (SphericalUtil.computeHeading(mapCenter, landmark.getPosition()) - currentCameraPosition.bearing) % 360;
        //return SphericalUtil.computeHeading(mapCenter, landmark.getPosition());

        if (heading < -180) {
            return heading + 360;
        } else if (heading > 180) {
            return heading - 360;
        } else {
            return heading;
        }
    }

    /**
     * Get the heading ID from the map's center to the landmark
     *
     * @param landmark Landmark to get the heading to
     * @return Heading ID to the landmark based on the <code>Heading</code> class IDs
     */
    private int getHeadingID(Landmark landmark) {
        // Get the heading value
        double heading = getHeading(landmark);

        // Return the corresponding heading ID
        if (heading <= Heading.BOTTOM || heading > Heading.RIGHT) {
            return Heading.BOTTOM_ID;
        } else if (heading <= Heading.LEFT) {
            return Heading.LEFT_ID;
        } else if (heading <= Heading.TOP) {
            return Heading.TOP_ID;
        } else {
            return Heading.RIGHT_ID;
        }
    }

/*    *//**
     * Function to compute the shifted position of an off-screen landmark
     *
     * @param landmark Landmark to be shifted
     * @return Shifted position
     *//*
    private LatLng computeOffScreenPositionOld(Landmark landmark) {

        // Get the landmark's heading ID - south, east, north, west
        int headingID = getHeadingID(landmark);

        // Get the bounding box of the displayed map and the map center
        LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
        LatLng mapCenter = new LatLng(bounds.getCenter().latitude, bounds.getCenter().longitude);

        // Calculate the latitude and longitude spans
        double spanLat = bounds.northeast.latitude - bounds.southwest.latitude;
        double spanLng = bounds.northeast.longitude - bounds.southwest.longitude;

        // Get the min/max latitude values
        double boundingLatMin = bounds.southwest.latitude + spanLat / VERTICAL_MAP_RATIO;
        double boundingLatMax = bounds.northeast.latitude - spanLat / VERTICAL_MAP_RATIO;

        // Get the min/max longitude values
        double boundingLngMin = bounds.southwest.longitude + spanLng / HORIZONTAL_MAP_RATIO;
        double boundingLngMax = bounds.northeast.longitude - spanLng / HORIZONTAL_MAP_RATIO;

        // Simplify the names of the variables
        double latMapCenter = mapCenter.latitude;
        double lngMapCenter = mapCenter.longitude;
        double latLM = landmark.getPosition().latitude;
        double lngLM = landmark.getPosition().longitude;

        // Start the computation
        double shiftedLat = 0;
        double shiftedLng = 0;
        switch (headingID) {
            case Heading.BOTTOM_ID:
                // Compute the landmark's shifted position for the Southern heading
                shiftedLat = boundingLatMin;
                shiftedLng = lngMapCenter - (lngLM - lngMapCenter) / (latLM - latMapCenter) *
                        (latMapCenter - boundingLatMin);
                break;
            case Heading.LEFT_ID:
                // Compute the landmark's shifted position for the Western heading
                shiftedLat = latMapCenter - (latLM - latMapCenter) / (lngLM - lngMapCenter) *
                        (lngMapCenter - boundingLngMin);
                shiftedLng = boundingLngMin;
                break;
            case Heading.TOP_ID:
                // Compute the landmark's shifted position for the Northern heading
                shiftedLat = boundingLatMax;
                shiftedLng = lngMapCenter + (lngLM - lngMapCenter) / (latLM - latMapCenter) *
                        (boundingLatMax - latMapCenter);
                break;
            case Heading.RIGHT_ID:
                // Compute the landmark's shifted position for the Eastern heading
                shiftedLat = latMapCenter + (latLM - latMapCenter) / (lngLM - lngMapCenter) *
                        (boundingLngMax - lngMapCenter);
                shiftedLng = boundingLngMax;
                break;
        }

        // Move the position to the allowed bounding box if necessary
        if (shiftedLat < boundingLatMin) shiftedLat = boundingLatMin;
        if (shiftedLat > boundingLatMax) shiftedLat = boundingLatMax;
        if (shiftedLng < boundingLngMin) shiftedLng = boundingLngMin;
        if (shiftedLng > boundingLngMax) shiftedLng = boundingLngMax;

        // Return the landmark's shifted position
        LatLng shiftedPosition = new LatLng(shiftedLat, shiftedLng);
        Log.d(TAG, landmark.getTitle() + getString(R.string.log_landmark_shifted_location) +
                shiftedPosition.toString());
        return shiftedPosition;
    }*/

    /**
     * Function to compute the shifted position of an off-screen landmark
     *
     * @param landmark Landmark to be shifted
     * @return Shifted position
     */
    private LatLng computeOffScreenPosition(Landmark landmark) {

        // Get the landmark's heading ID - south, east, north, west
        int headingID = getHeadingID(landmark);

        // Get the bounding box of the displayed map and the map center
        int mapPixelWidth = map.getProjection().toScreenLocation(map.getProjection().getVisibleRegion().nearRight).x; // Nexus5: 1080; width = x
        int mapPixelHeight = map.getProjection().toScreenLocation(map.getProjection().getVisibleRegion().nearRight).y; // Nexus5: 1536: height = y

        // Have an offset for the instructions
        mapPixelHeight -= INSTRUCTIONS_OFFSET;

        double span = 100;

        // Get the min/max latitude values
        double boundingXMin = span;
        double boundingXMax = mapPixelWidth - span;

        // Get the min/max longitude values
        double boundingYMin = span;
        double boundingYMax = mapPixelHeight - span;

        // Simplify the names of the variables
        double xMapCenter = mapPixelWidth / 2;
        double yMapCenter = mapPixelHeight / 2;
        double xLM = map.getProjection().toScreenLocation(landmark.getPosition()).x;
        double yLM = map.getProjection().toScreenLocation(landmark.getPosition()).y;

        // Start the computation
        double shiftedX = 0;
        double shiftedY = 0;
        switch (headingID) {
            case Heading.BOTTOM_ID:
                // Compute the landmark's shifted position for the Southern heading
                //Log.i("OffScreenComputation", "south");
                shiftedY = boundingYMax;
                shiftedX = xMapCenter - (xLM - xMapCenter) / (yLM - yMapCenter) *
                        (yMapCenter - boundingYMax);
                break;
            case Heading.LEFT_ID:
                // Compute the landmark's shifted position for the Western heading
                //Log.i("OffScreenComputation", "west");
                shiftedY = yMapCenter + (yLM - yMapCenter) / (xLM - xMapCenter) *
                        (boundingXMin - xMapCenter);
                shiftedX = boundingXMin;
                break;
            case Heading.TOP_ID:
                // Compute the landmark's shifted position for the Northern heading
                //Log.i("OffScreenComputation", "north");
                shiftedY = boundingYMin;
                shiftedX = xMapCenter + (xLM - xMapCenter) / (yLM - yMapCenter) *
                        (boundingYMin - yMapCenter);
                break;
            case Heading.RIGHT_ID:
                // Compute the landmark's shifted position for the Eastern heading
                //Log.i("OffScreenComputation", "east");
                shiftedY = yMapCenter - (yLM - yMapCenter) / (xLM - xMapCenter) *
                        (xMapCenter - boundingXMax);
                shiftedX = boundingXMax;
                break;
        }

        // Move the position to the allowed bounding box if necessary
        if (shiftedX < boundingXMin) shiftedX = boundingXMin;
        if (shiftedX > boundingXMax) shiftedX = boundingXMax;
        if (shiftedY < boundingYMin) shiftedY = boundingYMin;
        if (shiftedY > boundingYMax) shiftedY = boundingYMax;

        // Return the landmark's shifted position
        LatLng shiftedPosition = map.getProjection().fromScreenLocation(new Point((int) shiftedX, (int) shiftedY));
        Log.d(TAG, landmark.getTitle() + getString(R.string.log_landmark_shifted_location) +
                shiftedPosition.toString());
        return shiftedPosition;
    }

    /**
     * Called when the overlays of the map have to be updated
     */
    private void updateMap() {
        // Compute the map/screen ratio
        computeMapScreenRatio();
        // Then redraw the landmarks
        if (prefDownload && notDownloadedYet) {
            Log.i(TAG, "Update map if true");
            GetJsonTask getJsonTask = new GetJsonTask();
            getJsonTask.execute(prefURL);
        } else {
            Log.i(TAG, "Update map if false");
            recalculateLandmarks(prefDownload);
        }
    }

    /**
     * Recalculate how and where the landmarks are displayed.
     * Case 1: previous = landmark empty > now = landmark on-screen > display
     * Case 2: previous = landmark empty > now = landmark off-screen > display
     * Case 3: previous = landmark on-screen > now = landmark on-screen > do nothing
     * Case 4: previous = landmark off-screen > now = landmark on-screen > display landmark on screen
     * Case 5: previous = landmark on-screen > now = landmark off-screen > make landmark a off-screen candidate
     * Case 6: previous = landmark off-screen > now = landmark off-screen > make landmark a off-screen candidate
     *
     * @param useOnlineLandmarks
     */
    private void recalculateLandmarks(boolean useOnlineLandmarks) {
        // Copy the list of landmarks
        ArrayList<Landmark> allLandmarks = new ArrayList<Landmark>();
        if (landmarks == null || !useOnlineLandmarks) {
            // Store all pre-defined landmarks temporarily in another list
            allLandmarks = (ArrayList<Landmark>) PRE_DEFINED_LANDMARKS.clone();
        } else {
            allLandmarks = (ArrayList<Landmark>) landmarks.clone();
        }

        ArrayList<Landmark> offScreenCandidates = new ArrayList<Landmark>();

        for (Landmark l : allLandmarks) {
            //Log.i(TAG, l.getTitle() + "Screen locations: " + map.getProjection().toScreenLocation(l.getPosition()).toString());
            mapContainsPoint(l);
            // Check whether landmark is on- or off-screen at new camera position
            switch (l.getCategoryStatusLandmark()) {
                case R.integer.landmark_status_empty:
                    if (mapContainsPoint(l)) {
                        // Case 1: empty > on-screen
                        // display on-screen landmarks
                        if (((Object) l).getClass() == PointLandmark.class) {
                            // Check if the landmark's position is already covered on the map
                            if (isAreaFree(l.getPosition())) {
                                coveredArea.add(l.getPosition());
                                // Add point landmark map
                                l.setCategoryStatusLandmark(R.integer.landmark_status_on_screen);
                                addLandmarkToMap(l, MARKER_ON_SCREEN);
                            }
                        } else {
                            // Display this regional landmark
                            addLandmarkToMap(l, MARKER_ON_SCREEN);
                        }
                    } else {
                        // Case 2: empty > off-screen
                        // add to off-screen candidate
                        if (getDistance(l) <= l.getReferenceRadius()) {
                            // Map center is covered by reference radius of landmark
                            offScreenCandidates.add(l);
                        }
                    }
                    break;
                case R.integer.landmark_status_on_screen:
                    if (mapContainsPoint(l)) {
                        // Case 3: on-screen > on-screen
                        // check if zoomed
                        if (previousCameraPosition != null && currentCameraPosition.zoom != previousCameraPosition.zoom) {
                            if (l.getLandmarkMarkerCircle() != null)
                                l.getLandmarkMarkerCircle().remove();
                            addCircleToMap(l, l.getPosition());
                        }
                    } else {
                        // Case 4: on-screen > off-screen
                        // remove on-screen landmark and add to off-screen candidate
                        removeLandmarkFromMap(l);
                        l.setCategoryStatusLandmark(R.integer.landmark_status_off_screen);
                        if (getDistance(l) <= l.getReferenceRadius()) {
                            // Map center is covered by reference radius of landmark
                            offScreenCandidates.add(l);
                        }
                    }
                    break;
                case R.integer.landmark_status_off_screen:
                    if (mapContainsPoint(l)) {
                        // Case 5: off-screen > on-screen
                        // remove off-screen
                        removeLandmarkFromMap(l);
                        // display on-screen
                        if (((Object) l).getClass() == PointLandmark.class) {
                            // Check if the landmark's position is already covered on the map
                            if (isAreaFree(l.getPosition())) {
                                coveredArea.add(l.getPosition());
                                // Add point landmark map
                                l.setCategoryStatusLandmark(R.integer.landmark_status_on_screen);
                                addLandmarkToMap(l, MARKER_ON_SCREEN);
                            }
                        } else {
                            // Display this regional landmark
                            addLandmarkToMap(l, MARKER_ON_SCREEN);
                        }
                    } else {
                        // Case 6: off-screen > off-screen
                        // remove landmark and add to off-screen candidate
                        removeLandmarkFromMap(l);
                        if (getDistance(l) <= l.getReferenceRadius()) {
                            // Map center is covered by reference radius of landmark
                            offScreenCandidates.add(l);
                        }
                    }
                    break;
            }
        }

        if (offScreenCandidates.size() > 0) {
            // Remove redundant landmarks based on their headings and distances
            ArrayList<Landmark> filteredCandidates = filterLandmarks(offScreenCandidates);

            // Display the filtered off-screen landmarks
            for (int i = 0; i < filteredCandidates.size(); i++) {
                Landmark l = filteredCandidates.get(i);
                if (map.getClass() == GoogleMap.class) {
                    l.setOffScreenPosition(computeOffScreenPosition(l));
                }
                // Check if the landmark's position is already covered on the map
                if (isAreaFree(l.getOffScreenPosition())) {
                    coveredArea.add(l.getOffScreenPosition());
                    if (i <= filteredCandidates.size() / 2) {
                        l.setCategoryStatusLandmark(R.integer.landmark_status_off_screen);
                        addLandmarkToMap(l, MARKER_OFF_SCREEN_NEAR);
                    } else {
                        l.setCategoryStatusLandmark(R.integer.landmark_status_off_screen);
                        addLandmarkToMap(l, MARKER_OFF_SCREEN_FAR);
                    }
                } else {
                    //TODO implemente funktion to shift landmark, if area is already covered
                }
            }
        }
    }

    private boolean mapContainsPoint(Landmark l) {
        int mapPixelWidth = map.getProjection().toScreenLocation(map.getProjection().getVisibleRegion().nearRight).x; // Nexus5: 1080
        int mapPixelHeight = map.getProjection().toScreenLocation(map.getProjection().getVisibleRegion().nearRight).y; // Nexus5: 1536

        // Have an offset for the instructions
        mapPixelHeight -= INSTRUCTIONS_OFFSET;

        Point lPos = map.getProjection().toScreenLocation(l.getPosition());
        if (lPos.x >= 0 && lPos.x <= mapPixelWidth && lPos.y >= 0 && lPos.y <= mapPixelHeight) {
            return true;
        }
        return false;
    }

    /**
     * Set the map type respectively to the settings
     */
    private void setMapType() {
        //if (prefMapGoogle) {
        switch (prefMapType) {
            case GoogleMap.MAP_TYPE_NORMAL:
                // Normal
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case GoogleMap.MAP_TYPE_HYBRID:
                // Hybrid
                map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case GoogleMap.MAP_TYPE_SATELLITE:
                // Satellite
                map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
        }
    }

    /**
     * Called when the map type has been changed in the settings
     */
    private void updateMapType() {
        // Set the map type new
        setMapType();
        // Update the displayed overlays
        updateMap();
    }

    /**
     * Called when the map following preference has been changed
     */
    private void updateMapRotatingAndFollowing() {
        if (prefMapFollow && prefCompassTop) {
            if (!((LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                //Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                //startActivity(intent);
                Toast.makeText(getApplicationContext(), "Please enable GPS to get this function working.",
                        Toast.LENGTH_LONG).show();
            }

            setMapFollowingListenerEnabled(true, true);
            setOnTouchListenerEnabled(true);
        } else if (prefMapFollow && !prefCompassTop) {
            if (!((LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                //Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                //startActivity(intent);
                Toast.makeText(getApplicationContext(), "Please enable GPS to get this function working.",
                        Toast.LENGTH_LONG).show();
            }

            setMapFollowingListenerEnabled(true, false);
            setOnTouchListenerEnabled(true);
        } else if (!prefMapFollow && prefCompassTop) {
            setMapFollowingListenerEnabled(false, true);
            setOnTouchListenerEnabled(false);
        } else {
            setMapFollowingListenerEnabled(false, false);
            setOnTouchListenerEnabled(false);
        }
    }

    private void setMapFollowingListenerEnabled(boolean mapFollowing, boolean compassTop) {
        if (mapFollowing && compassTop) {
            map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                @Override
                public void onMyLocationChange(Location location) {
                    if (location.getBearing()==0.0) {
                        //TODO do something
                        //animateTo(new LatLng(location.getLatitude(), location.getLongitude()), currentCameraPosition.bearing, currentCameraPosition.zoom);
                    } else {
                        animateTo(new LatLng(location.getLatitude(), location.getLongitude()), location.getBearing(), currentCameraPosition.zoom);
                    }
                    Log.d(TAG, "Map_Follow_MyLocationChanged: " + location.getLatitude() + ", " + location.getLongitude() + ", " + location.getBearing());
                }
            });
            map.getUiSettings().setMyLocationButtonEnabled(false);
        } else if(mapFollowing && !compassTop) {
            map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                @Override
                public void onMyLocationChange(Location location) {
                    animateTo(new LatLng(location.getLatitude(), location.getLongitude()), 0, currentCameraPosition.zoom);
                    Log.d(TAG, "Map_Follow_MyLocationChanged: " + location.getLatitude() + ", " + location.getLongitude());
                }
            });
            map.getUiSettings().setMyLocationButtonEnabled(false);
        } else if (!mapFollowing && compassTop) {
            map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                public void onMyLocationChange(Location location) {
                    if (location.getBearing()==0.0) {
                        //TODO do something
                    } else {
                        animateTo(currentCameraPosition.target, location.getBearing(), currentCameraPosition.zoom);
                    }
                    Log.d(TAG, "Map_Follow_MyLocationChanged: " + location.getLatitude() + ", " + location.getLongitude() + ", " + location.getBearing());
                }
            });
        } else if (!mapFollowing && !compassTop) {
            map.setOnMyLocationChangeListener(null);
        }
    }

    private void setOnTouchListenerEnabled(boolean b) {
        if (b) {
            mapTouchLayer.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    setMapFollowingListenerEnabled(false, prefCompassTop);
                    map.getUiSettings().setMyLocationButtonEnabled(true);
                    return false; // Pass on the touch to the map or shadow layer.
                }
            });
        } else {
            mapTouchLayer.setOnTouchListener(null);
        }
    }

    /**
     * Function to filter landmarks by their heading and distances.
     * <p/>
     * Only one landmark per heading is allowed. If multiple landmarks appear for the same heading,
     * the one with the greater reference radius will be used.
     * <p/>
     * The first half of the returned landmarks is supposed to be "near" and the other half "far".
     *
     * @param unfilteredLandmarks List of unfiltered landmarks
     * @return List of filtered landmarks
     */
    private ArrayList<Landmark> filterLandmarks(ArrayList<Landmark> unfilteredLandmarks) {
        // Store the landmarks for each heading
        ArrayList<Landmark> top = new ArrayList<Landmark>();
        ArrayList<Landmark> right = new ArrayList<Landmark>();
        ArrayList<Landmark> bottom = new ArrayList<Landmark>();
        ArrayList<Landmark> left = new ArrayList<Landmark>();

        // Iterate through all landmarks
        for (Landmark l : unfilteredLandmarks) {
            int headingID = getHeadingID(l);
            if (headingID == Heading.RIGHT_ID) {
                right.add(l);
            } else if (headingID == Heading.BOTTOM_ID) {
                bottom.add(l);
            } else if (headingID == Heading.LEFT_ID) {
                left.add(l);
            } else if (headingID == Heading.TOP_ID) {
                top.add(l);
            }
        }

        //Sorting Arraylists in descending order
        Collections.sort(top, new Comparator<Landmark>() {
            @Override
            public int compare(Landmark lhs, Landmark rhs) {
                return (int) (rhs.getReferenceRadius() - lhs.getReferenceRadius());
            }
        });

        Collections.sort(right, new Comparator<Landmark>() {
            @Override
            public int compare(Landmark lhs, Landmark rhs) {
                return (int) (rhs.getReferenceRadius() - lhs.getReferenceRadius());
            }
        });

        Collections.sort(bottom, new Comparator<Landmark>() {
            @Override
            public int compare(Landmark lhs, Landmark rhs) {
                return (int) (rhs.getReferenceRadius() - lhs.getReferenceRadius());
            }
        });

        Collections.sort(left, new Comparator<Landmark>() {
            @Override
            public int compare(Landmark lhs, Landmark rhs) {
                return (int) (rhs.getReferenceRadius() - lhs.getReferenceRadius());
            }
        });

        ArrayList<Landmark> filteredLandmarks = new ArrayList<Landmark>();

        Log.i(TAG, "Number of Landmarks per edge top, right, bottom, left: " + top.size() + ", " + right.size() + ", " + bottom.size() + ", " + left.size());

        for (int i = 0; i<OFF_SCREEN_LANDMARKS_TOP; i++) {
            if (i < top.size()) {
                top.get(i).setDistance(getDistance(top.get(i)));
                filteredLandmarks.add(top.get(i));
            }
        }
        for (int i = 0; i<OFF_SCREEN_LANDMARKS_RIGHT; i++) {
            if (i < right.size()) {
                right.get(i).setDistance(getDistance(right.get(i)));
                filteredLandmarks.add(right.get(i));
            }
        }
        for (int i = 0; i<OFF_SCREEN_LANDMARKS_BOTTOM; i++) {
            if (i < bottom.size()) {
                bottom.get(i).setDistance(getDistance(bottom.get(i)));
                filteredLandmarks.add(bottom.get(i));
            }
        }
        for (int i = 0; i<OFF_SCREEN_LANDMARKS_LEFT; i++) {
            if (i < left.size()) {
                left.get(i).setDistance(getDistance(left.get(i)));
                filteredLandmarks.add(left.get(i));
            }
        }

        if (filteredLandmarks.size() == 0) {
            return null;
        } else {
            // Order landmarks by distances
            Collections.sort(filteredLandmarks);
            return filteredLandmarks;
        }
    }
}
