package de.ifgi.wayto_prototype.activities;

import android.app.ProgressDialog;
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
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.maps.android.SphericalUtil;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import de.ifgi.wayto_prototype.R;
import de.ifgi.wayto_prototype.demo.LandmarkCollection;
import de.ifgi.wayto_prototype.landmarks.Landmark;
import de.ifgi.wayto_prototype.landmarks.PointLandmark;
import de.ifgi.wayto_prototype.landmarks.RegionalLandmark;
import de.ifgi.wayto_prototype.map.Heading;

/* Angela */


/**
 * Main activity that displays the map
 *
 * @author Marius Runde
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
     * List of pre-defined landmarks
     */
    private final ArrayList<Landmark> PRE_DEFINED_LANDMARKS = LandmarkCollection.initLandmarks();
    /**
     * List of downloaded landmarks
     */
    private ArrayList<Landmark> landmarks = null;
    /**
     * Google Maps object
     */
    private GoogleMap map;
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
    private ArrayList<LatLng> coveredArea;

    // --- End of landmark and map variables ---

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
     * Function that is called when the camera is changed.
     * @param cameraPosition
     */
    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        logger += "Map moved to position: " + cameraPosition.target.toString() +
                " at zoom level: " + cameraPosition.zoom + " at time: " + getCurrentTime() + "\n";
        updateMap();
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
        if (prefMapFollow) {
            updateMapFollowing();
        }
        if (prefMapCompass) {
            map.getUiSettings().setCompassEnabled(true);
        } else {
            map.getUiSettings().setCompassEnabled(false);
        }
        Log.d(TAG, "Compass enagled: " + map.getUiSettings().isCompassEnabled());

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
                map.getUiSettings().setMyLocationButtonEnabled(true);
                // Animate to starting point
                animateTo(StartingPoint, 14);
                // Set OnCameraChangeListener
                map.setOnCameraChangeListener(this);
                // Set OnMarkerClickListener
                map.setOnMarkerClickListener(this);
                // Set OnMapClickListener
                map.setOnMapClickListener(this);
                // Angela set rotation false
                map.getUiSettings().setRotateGesturesEnabled(true);
            } else {
                // Cannot create map
                String message = getString(R.string.log_map_cannot_create);
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                Log.e(TAG, message);
            }
        }
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
    private void animateTo(LatLng destination, float zoom) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(destination)
                .zoom(zoom)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    /**
     * Display the landmarks on the map
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
/*
Currently not used, because only relevant for on screen landmarks that lie in the border where off-screen landmarks are displayed.
New solution: use bounds of the map and only display a landmark as off-screen landmark, when is not visible on map any more.

        // Make up a inner frame, where the off-screen landmarks shall be displayed.
        // The center of the off-screen landmarks will be at a tenth of the display/map width

        // Calculate the latitude and longitude spans
        double spanLat = bounds.northeast.latitude - bounds.southwest.latitude;
        double spanLng = bounds.northeast.longitude - bounds.southwest.longitude;

        // Get the min/max latitude values
        double boundingLatMin = bounds.southwest.latitude + spanLat / 10;
        double boundingLatMax = bounds.northeast.latitude - spanLat / 10;

        // Get the min/max longitude values
        double boundingLngMin = bounds.southwest.longitude + spanLng / 10;
        double boundingLngMax = bounds.northeast.longitude - spanLng / 10;

        // Get the bounding box where the markers shall be displayed
        LatLngBounds markerBounds = new LatLngBounds(new LatLng(boundingLatMin, boundingLngMin),
                new LatLng(boundingLatMax, boundingLngMax));
*/

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
            addCircleToMap(displayedPosition);

            // Add the landmark as a marker to the map
            map.addMarker(new MarkerOptions()
                            .anchor(0.5f, 0.5f)
                            .draggable(false)
                            .icon(BitmapDescriptorFactory.fromBitmap(icon))
                            .position(displayedPosition)
                            .title(tempLandmark.getTitle())
                            .visible(true)
            );
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
                    Log.d(TAG, getString(R.string.log_map_bbox_added) + landmark.getPosition());
                    break;
                case METHOD_POLYGON:
                    ArrayList<LatLng> shapePoints = new ArrayList<>();
                    Collections.addAll(shapePoints, ((RegionalLandmark) landmark).getShapePoints());
                    map.addPolygon(new PolygonOptions()
                                    .addAll(shapePoints)
                                    .visible(true)
                    );
                    Log.d(TAG, getString(R.string.log_map_regional_landmark_added) +
                            shapePoints.toString());
                    break;
            }
        }
    }

    /**
     * Function to draw a circle below the markers
     *
     * @param position Position of the circle
     */
    private void addCircleToMap(LatLng position) {
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
        map.addGroundOverlay(new GroundOverlayOptions()
                        .image(BitmapDescriptorFactory.fromBitmap(bitmap))
                        .position(position, radius * 2, radius * 2)
                        .transparency(0.4f)
        );
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

        // Compute the reverse heading (from the landmark to the map center)
        double reverseHeading = heading;
        int rotation = (int) (reverseHeading + 360) % 360;
        if (reverseHeading > 0) {
            reverseHeading -= 180;
        } else {
            reverseHeading += 180;
        }

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
        map.addMarker(new MarkerOptions()
                        .anchor(0.5f, 0.5f)
                        .draggable(false)
                        .icon(BitmapDescriptorFactory.fromBitmap(rotatedArrow))
                        .position(arrowPositionNear)
                        .visible(true)
        );
    }

    /**
     * Function to display the "distance-based pointer" method for the off-screen landmark
     *
     * @param landmark Landmark this function refers to
     */
    private void addPointerToMap(Landmark landmark) {
        // ...
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
            case Heading.NORTH_ID:
                pointAtScreenEdge = new LatLng(
                        map.getProjection().getVisibleRegion().farRight.latitude,
                        lng
                );
                break;
            case Heading.EAST_ID:
                pointAtScreenEdge = new LatLng(
                        lat,
                        map.getProjection().getVisibleRegion().farRight.longitude
                );
                break;
            case Heading.SOUTH_ID:
                pointAtScreenEdge = new LatLng(
                        map.getProjection().getVisibleRegion().nearLeft.latitude,
                        lng
                );
                break;
            case Heading.WEST_ID:
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
            map.addPolygon(new PolygonOptions()
                            .add(landmark.getPosition(), wedgePoint1, wedgePoint2)
                            .strokeColor(Color.WHITE)
            );
        } else {
            map.addPolygon(new PolygonOptions()
                            .add(landmark.getPosition(), wedgePoint1, wedgePoint2)
                            .strokeColor(Color.BLACK)
            );
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
        return SphericalUtil.computeHeading(mapCenter, landmark.getPosition());
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
        if (heading <= Heading.SOUTH || heading > Heading.EAST) {
            return Heading.SOUTH_ID;
        } else if (heading <= Heading.WEST) {
            return Heading.WEST_ID;
        } else if (heading <= Heading.NORTH) {
            return Heading.NORTH_ID;
        } else {
            return Heading.EAST_ID;
        }
    }

    /**
     * Function to compute the shifted position of an off-screen landmark
     *
     * @param landmark Landmark to be shifted
     * @return Shifted position
     */
    private LatLng computeOffScreenPosition(Landmark landmark) {
        // Get the landmark's heading ID
        int headingID = getHeadingID(landmark);

        // Get the bounding box of the displayed map and the map center
        LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
        LatLng mapCenter = new LatLng(bounds.getCenter().latitude, bounds.getCenter().longitude);

        // Calculate the latitude and longitude spans
        double spanLat = bounds.northeast.latitude - bounds.southwest.latitude;
        double spanLng = bounds.northeast.longitude - bounds.southwest.longitude;

        // Get the min/max latitude values
        double boundingLatMin = bounds.southwest.latitude + spanLat / 15;
        double boundingLatMax = bounds.northeast.latitude - spanLat / 15;

        // Get the min/max longitude values
        double boundingLngMin = bounds.southwest.longitude + spanLng / 10;
        double boundingLngMax = bounds.northeast.longitude - spanLng / 10;

        // Simplify the names of the variables
        double latMapCenter = mapCenter.latitude;
        double lngMapCenter = mapCenter.longitude;
        double latLM = landmark.getPosition().latitude;
        double lngLM = landmark.getPosition().longitude;

        // Start the computation
        double shiftedLat = 0;
        double shiftedLng = 0;
        switch (headingID) {
            case Heading.SOUTH_ID:
                // Compute the landmark's shifted position for the Southern heading
                shiftedLat = boundingLatMin;
                shiftedLng = lngMapCenter - (lngLM - lngMapCenter) / (latLM - latMapCenter) *
                        (latMapCenter - boundingLatMin);
                break;
            case Heading.WEST_ID:
                // Compute the landmark's shifted position for the Western heading
                shiftedLat = latMapCenter - (latLM - latMapCenter) / (lngLM - lngMapCenter) *
                        (lngMapCenter - boundingLngMin);
                shiftedLng = boundingLngMin;
                break;
            case Heading.NORTH_ID:
                // Compute the landmark's shifted position for the Northern heading
                shiftedLat = boundingLatMax;
                shiftedLng = lngMapCenter + (lngLM - lngMapCenter) / (latLM - latMapCenter) *
                        (boundingLatMax - latMapCenter);
                break;
            case Heading.EAST_ID:
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
    }

    /**
     * Called when the overlays of the map have to be updated
     */
    private void updateMap() {
        // First clear the map
        map.clear();
        // Compute the map/screen ratio
        computeMapScreenRatio();
        // Then redraw the landmarks
        if (prefDownload && notDownloadedYet) {
            GetJsonTask getJsonTask = new GetJsonTask();
            getJsonTask.execute(prefURL);
        } else {
            displayLandmarks(prefDownload);
        }
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
    private void updateMapFollowing() {
        if (prefMapFollow) {
            map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                @Override
                public void onMyLocationChange(Location location) {
                    animateTo(new LatLng(location.getLatitude(), location.getLongitude()), 14);
                    Log.d(TAG, "Map_Follow_MyLocationChanged: " + location.getLatitude() + ", " + location.getLongitude());
                }
            });
        } else {
            map.setOnMyLocationChangeListener(null);
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
        Landmark north = null;
        Landmark east = null;
        Landmark south = null;
        Landmark west = null;

        // Iterate through all landmarks
        for (Landmark l : unfilteredLandmarks) {
            int headingID = getHeadingID(l);
            if (headingID == Heading.EAST_ID && (east == null || (east != null &&
                    l.getReferenceRadius() > east.getReferenceRadius()))) {
                east = l;
            } else if (headingID == Heading.SOUTH_ID && (south == null || (south != null &&
                    l.getReferenceRadius() > south.getReferenceRadius()))) {
                south = l;
            } else if (headingID == Heading.WEST_ID && (west == null || (west != null &&
                    l.getReferenceRadius() > west.getReferenceRadius()))) {
                west = l;
            } else if (north == null || (north != null &&
                    l.getReferenceRadius() > north.getReferenceRadius())) {
                north = l;
            }
        }

        // Return the filtered landmarks
        ArrayList<Landmark> filteredLandmarks = new ArrayList<Landmark>();
        if (north != null) {
            north.setDistance(getDistance(north));
            filteredLandmarks.add(north);
        }
        if (east != null) {
            east.setDistance(getDistance(east));
            filteredLandmarks.add(east);
        }
        if (south != null) {
            south.setDistance(getDistance(south));
            filteredLandmarks.add(south);
        }
        if (west != null) {
            west.setDistance(getDistance(west));
            filteredLandmarks.add(west);
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
