package de.ifgi.wayto_prototype.demo;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

/**
 * Created by Heinrich on 10.12.15.
 */
public class Waypoint {

    /**
     * Unique ID of the Waypoint
     */
    private final int ID;

    /**
     * Reference radius which is relevant to check whether user is inside or outside the vicinity of the waypoint.
     */
    private int referenceRadius = 10;

    /**
     * Position of the waypoint
     */
    private LatLng position;

    /**
     * Constructor of the waypoint class
     *
     * @param ID
     * @param position
     */
    public Waypoint (int ID, LatLng position) {
        this.ID = ID;
        this.position = position;
    }

    @Override
    public String toString() {
        return ID + ": " + position.toString() + ", reference radius: " + referenceRadius;
    }

    public int getID() {
        return this.ID;
    }

    public int getReferenceRadius() {
        return this.referenceRadius;
    }

    public LatLng getPosition() {
        return this.position;
    }

    public boolean withinReferenceRadius (LatLng position) {
        if (SphericalUtil.computeDistanceBetween(position, this.position) < referenceRadius) {
            return true;
        } else {
            return false;
        }
    }
}
