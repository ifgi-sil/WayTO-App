package de.ifgi.wayto_prototype.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
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
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.android.gms.maps.model.UrlTileProvider;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.maps.android.SphericalUtil;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import de.ifgi.wayto_prototype.R;
import de.ifgi.wayto_prototype.demo.LandmarkCollection;
import de.ifgi.wayto_prototype.landmarks.Landmark;
import de.ifgi.wayto_prototype.landmarks.PointLandmark;
import de.ifgi.wayto_prototype.landmarks.RegionalLandmark;
import de.ifgi.wayto_prototype.map.Heading;

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
     * Coordinates of the town hall as starting location
     */
    private final LatLng TOWN_HALL = new LatLng(51.961563, 7.628187);

    /**
     * Size of the markers
     */
    private final int SIZE_MARKER = 30;
    /**
     * Size of the circles underlying the markers
     */
    private final int SIZE_CIRCLE = 25;
    /**
     * Size of the arrows of the off-screen landmarks
     */
    private final int SIZE_ARROW = 30;
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

    /**
     * ID for the "normal arrow" method
     */
    private final int METHOD_ARROW = 0;
    /**
     * ID for the "distance-based pointer" method
     */
    private final int METHOD_POINTER = 1;
    /**
     * ID for the "wedge" method
     */
    private final int METHOD_WEDGE = 2;

    /**
     * Shared preferences instance
     */
    private SharedPreferences preferences;
    /**
     * Preference value for the usage of Google Maps data
     */
    private boolean prefMapGoogle;
    /**
     * Preference value for the map type (e.g. normal, hybrid, or satellite)
     */
    private int prefMapType;
    /**
     * Preference value indicating whether off-screen landmarks shall be displayed
     */
    private boolean prefMethod;
    /**
     * Preference value for the method for displaying off-screen landmarks (e.g. wedge)
     */
    private int prefMethodType;

    /**
     * List of landmarks
     */
    private final ArrayList<Landmark> ALL_LANDMARKS = LandmarkCollection.initLandmarks();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_item_settings:
                // Open the settings activity
                Intent intentSettings = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intentSettings);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Load the shared preferences
     */
    private void loadPreferences() {
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefMapGoogle = preferences.getBoolean(SettingsActivity.PREF_KEY_MAP_GOOGLE, true);
        prefMapType = Integer.valueOf(
                preferences.getString(SettingsActivity.PREF_KEY_MAP_TYPE, getString(R.string.map_type_normal)));
        prefMethod = preferences.getBoolean(SettingsActivity.PREF_KEY_METHOD, true);
        prefMethodType = Integer.valueOf(
                preferences.getString(SettingsActivity.PREF_KEY_METHOD_TYPE, "2"));
    }

    /**
     * Function to check whether a preferences has been changed in which case the required actions
     * will be started
     */
    private void checkPreferences() {
        boolean mapGoogle = preferences.getBoolean(SettingsActivity.PREF_KEY_MAP_GOOGLE, true);
        if (prefMapGoogle != mapGoogle) {
            prefMapGoogle = mapGoogle;
            updateMapType();
        }
        int mapType = Integer.valueOf(
                preferences.getString(SettingsActivity.PREF_KEY_MAP_TYPE, getString(R.string.map_type_normal)));
        if (prefMapType != mapType) {
            prefMapType = mapType;
            updateMapType();
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
                // Animate to town hall
                animateTo(TOWN_HALL, 14);
                // Set OnCameraChangeListener
                map.setOnCameraChangeListener(this);
                // Set OnMarkerClickListener
                map.setOnMarkerClickListener(this);
                // Set OnMapClickListener
                map.setOnMapClickListener(this);
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
        double mapWidth = SphericalUtil.computeDistanceBetween(vr.farLeft, vr.farRight);
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
     */
    private void displayLandmarks() {
        // Clear the covered area
        coveredArea = new ArrayList<LatLng>();

        // Store all landmarks temporarily in another list
        ArrayList<Landmark> landmarks = (ArrayList<Landmark>) ALL_LANDMARKS.clone();

        // Get the bounding box of the displayed map and the map center
        LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
        LatLng mapCenter = new LatLng(bounds.getCenter().latitude, bounds.getCenter().longitude);

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

        // Get all on-screen candidate landmarks and display the regional ones
        ArrayList<Landmark> onScreenCandidates = new ArrayList<Landmark>();
        for (Landmark l : landmarks) {
            if (markerBounds.contains(l.getPosition())) {
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
            landmarks.remove(l);
        }

        // Display the on-screen candidate point landmarks, so that they lay above the regional ones
        for (Landmark l : onScreenCandidates) {
            addLandmarkToMap(l, MARKER_ON_SCREEN);
        }

        // Get all off-screen candidate landmarks
        ArrayList<Landmark> offScreenCandidates = new ArrayList<Landmark>();
        for (Landmark l : landmarks) {
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
                if (isAreaFree(l.getPosition())) {
                    coveredArea.add(l.getPosition());
                    if (i <= filteredCandidates.size() / 2) {
                        addLandmarkToMap(l, MARKER_OFF_SCREEN_NEAR);
                    } else {
                        addLandmarkToMap(l, MARKER_OFF_SCREEN_FAR);
                    }
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
                map.getProjection().getVisibleRegion().latLngBounds
                        .contains(landmark.getPosition())) {
            // Modify the drawable to adjust the size of it
            BitmapDrawable bitmapDrawable = (BitmapDrawable) getResources()
                    .getDrawable(landmark.getCategoryDrawable());
            Bitmap bitmap = bitmapDrawable.getBitmap();
            Bitmap icon = Bitmap.createScaledBitmap(bitmap, SIZE_MARKER, SIZE_MARKER,
                    false);

            // Get the correct position where the landmark shall be displayed
            LatLng displayedPosition = landmark.getPosition();
            if (distance != MARKER_ON_SCREEN) {
                displayedPosition = landmark.getOffScreenPosition();
            }

            // Add the underlying circle to the map
            addCircleToMap(displayedPosition);

            // Add the landmark as a marker to the map
            map.addMarker(new MarkerOptions()
                            .anchor(0.5f, 0.5f)
                            .draggable(false)
                            .icon(BitmapDescriptorFactory.fromBitmap(icon))
                            .position(displayedPosition)
                            .title(landmark.getTitle())
                            .visible(true)
            );
            Log.d(TAG, getString(R.string.log_map_point_landmark_added));

            // Check if the landmark is an off-screen landmark and shall be displayed
            if (distance != MARKER_ON_SCREEN && prefMethod) {
                // Use preferred method for off-screen methods
                switch (prefMethodType) {
                    case METHOD_ARROW:
                        // Use the "normal arrow" method
                        addArrowToMap(landmark, distance);
                        break;
                    case METHOD_POINTER:
                        // Use the "distance-based pointer" method
                        addArrowToMap(landmark, distance); // TODO remove when addPointer works
                        Toast.makeText(getApplicationContext(), "Work in progress",
                                Toast.LENGTH_SHORT).show(); // TODO remove when addPointer works
                        addPointerToMap(landmark);
                        break;
                    case METHOD_WEDGE:
                        // Use the "wedge" method
                        addWedgeToMap(landmark);
                        break;
                }
            }
        } else {
            // TODO display regional landmark
            ArrayList<LatLng> shapePoints = new ArrayList<LatLng>();
            Collections.addAll(shapePoints, ((RegionalLandmark) landmark).getShapePoints());
            map.addPolygon(new PolygonOptions().addAll(shapePoints));
            Log.d(TAG, getString(R.string.log_map_regional_landmark_added));
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
     */
    private void addArrowToMap(Landmark landmark, int distance) {
        // Compute the reverse heading (from the landmark to the map center)
        double reverseHeading = getHeading(landmark);
        int rotation = (int) (reverseHeading + 360) % 360;
        if (reverseHeading > 0) {
            reverseHeading -= 180;
        } else {
            reverseHeading += 180;
        }

        // Get the correct drawable depending on the current map type
        BitmapDrawable bitmapDrawable;
        if (prefMapGoogle && (prefMapType == GoogleMap.MAP_TYPE_HYBRID ||
                prefMapType == GoogleMap.MAP_TYPE_SATELLITE)) {
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
        LatLng arrowPositionNear = SphericalUtil.computeOffset(landmark.getOffScreenPosition(),
                DISTANCE_ARROW * this.mapScreenRatio, reverseHeading);
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
        // TODO
    }

    /**
     * Function to display the "wedge" method for the off-screen landmark
     *
     * @param landmark Landmark this function refers to
     */
    private void addWedgeToMap(Landmark landmark) {
        // Calculate the distance from the landmark to the edge of the screen
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
        double distanceToScreen = SphericalUtil.computeDistanceBetween(landmark.getPosition(),
                pointAtScreenEdge);
        // Calculate the length of the legs
        double leg = distanceToScreen + Math.log((distanceToScreen + 20) / 12) * 10;
        // Calculate the length of half of the base
        double halfBase = ((5 + getDistance(landmark) * 0.3) / leg) / 2;
        // Calculate the distance from the landmark to the base (Pythagorean theorem)
        double distanceToBase = Math.sqrt(Math.pow(leg, 2) - Math.pow(halfBase, 2));
        // Calculate the point lying on the base line
        LatLng pointOnBase = SphericalUtil.computeOffsetOrigin(landmark.getPosition(),
                distanceToBase, getHeading(landmark));

        // Calculate the shape points of the wedge
        LatLng wedgePoint1 = SphericalUtil.computeOffset(pointOnBase, halfBase,
                (getHeading(landmark) + 90) % 360);
        LatLng wedgePoint2 = SphericalUtil.computeOffset(pointOnBase, halfBase,
                (getHeading(landmark) - 90) % 360);

        // Display the wedge
        map.addPolygon(new PolygonOptions()
                        .add(landmark.getPosition(), wedgePoint1, wedgePoint2)
                        .strokeColor(Color.WHITE)
        );
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
        double boundingLatMin = bounds.southwest.latitude + spanLat / 10;
        double boundingLatMax = bounds.northeast.latitude - spanLat / 10;

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

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
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

        // Open the info window for the marker
        marker.showInfoWindow();
        // Re-assign the last opened such that we can close it later
        lastOpened = marker;

        // Event was handled by our code do not launch default behaviour.
        return true;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        // Set the lasted clicked marker to null, so the info window will be shown again
        lastOpened = null;
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
        displayLandmarks();
    }

    /**
     * Set the map type respectively to the settings
     */
    private void setMapType() {
        if (prefMapGoogle) {
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
        } else {
            map.setMapType(GoogleMap.MAP_TYPE_NONE);

            TileProvider tileProvider = new UrlTileProvider(256, 256) {
                @Override
                public synchronized URL getTileUrl(int x, int y, int zoom) {
                    // The moon tile coordinate system is reversed.  This is not normal.
                    int reversedY = (1 << zoom) - y - 1;
                    String s = String.format(Locale.US, "http://mw1.google.com/mw-planetary/lunar/lunarmaps_v1/clem_bw/%d/%d/%d.jpg", zoom, x, reversedY);
                    URL url = null;
                    try {
                        url = new URL(s);
                    } catch (MalformedURLException e) {
                        throw new AssertionError(e);
                    }
                    return url;
                }
            };
            map.addTileOverlay(new TileOverlayOptions().tileProvider(tileProvider));

            // TODO OSMTileProvider osmTileProvider = new OSMTileProvider();
            // TODO map.addTileOverlay(new TileOverlayOptions().tileProvider(osmTileProvider));
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
