package de.ifgi.wayto_prototype.demo;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by helo on 11.12.15.
 */
public class Segment {

    private final int ID;

    private ArrayList<LatLng> segmentPoints;

    /**
     * Constructor for the Segment class
     *
     * @param ID
     * @param segmentPoints
     */
    public Segment(int ID, ArrayList<LatLng> segmentPoints) {
        this.ID = ID;
        this.segmentPoints = segmentPoints;
    }

    public int getID() {
        return this.ID;
    }

    public ArrayList<LatLng> getSegmentPoints() { return segmentPoints; }
}
