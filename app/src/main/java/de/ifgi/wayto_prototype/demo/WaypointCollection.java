package de.ifgi.wayto_prototype.demo;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by Heinrich on 10.12.15.
 */
public abstract class WaypointCollection {

    private static ArrayList<Waypoint> waypoints;

    /**
     * Initialise the waypoints
     *
     * @return List of waypoints
     */
    public static ArrayList<Waypoint> initWaypoints() {
        // Initialise the list of landmarks
        waypoints = new ArrayList<Waypoint>();

        // Initialise the landmarks of the different cities
        initMuensterWaypoints();

        // Return the landmarks
        return waypoints;
    }

    public static void initMuensterWaypoints() {
        waypoints.add(new Waypoint(1, new LatLng(51.947245, 7.622193)));
        waypoints.add(new Waypoint(2, new LatLng(51.946951, 7.622159)));
        waypoints.add(new Waypoint(3, new LatLng(51.946969, 7.620576)));
        waypoints.add(new Waypoint(4, new LatLng(51.947743, 7.620394)));
        waypoints.add(new Waypoint(5, new LatLng(51.947706, 7.617524)));
        waypoints.add(new Waypoint(6, new LatLng(51.948991, 7.617125)));
        waypoints.add(new Waypoint(7, new LatLng(51.949061, 7.616448)));
        waypoints.add(new Waypoint(8, new LatLng(51.946125, 7.614010)));
        waypoints.add(new Waypoint(9, new LatLng(51.945631, 7.614509)));
        waypoints.add(new Waypoint(10, new LatLng(51.943380, 7.614482)));
        waypoints.add(new Waypoint(11, new LatLng(51.943395, 7.615693)));
        waypoints.add(new Waypoint(12, new LatLng(51.944624, 7.618970)));
        waypoints.add(new Waypoint(13, new LatLng(51.943318, 7.619526)));
        waypoints.add(new Waypoint(14, new LatLng(51.943931, 7.621248)));
        waypoints.add(new Waypoint(15, new LatLng(51.944423, 7.621104)));
    }
}
