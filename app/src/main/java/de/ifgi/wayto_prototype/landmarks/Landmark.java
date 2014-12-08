package de.ifgi.wayto_prototype.landmarks;

import com.google.android.gms.maps.model.LatLng;

/**
 * Super class for landmarks
 *
 * @author Marius Runde
 */
public abstract class Landmark implements Comparable<Landmark> {

    /**
     * Tag for the logger
     */
    private static final String TAG = Landmark.class.toString();

    /**
     * Title (or name)
     */
    private String title;
    /**
     * Radius of reference
     */
    private double referenceRadius;
    /**
     * Distance to the map center (dynamic value)
     */
    private double distance = 0;
    /**
     * Category's drawable for the symbol on the map
     */
    private int categoryDrawable;

    /**
     * (Super) Constructor of the Landmark class
     *
     * @param title            Title (or name)
     * @param referenceRadius  Radius of reference
     * @param categoryDrawable Category's drawable for the symbol on the  map
     */
    public Landmark(String title, double referenceRadius, int categoryDrawable) {
        this.title = title;
        this.referenceRadius = referenceRadius;
        this.categoryDrawable = categoryDrawable;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getReferenceRadius() {
        return referenceRadius;
    }

    public void setReferenceRadius(double referenceRadius) {
        this.referenceRadius = referenceRadius;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getCategoryDrawable() {
        return categoryDrawable;
    }

    public void setCategoryDrawable(int categoryDrawable) {
        this.categoryDrawable = categoryDrawable;
    }

    public LatLng getPosition() {
        // MUST BE OVERRIDDEN BY SUB-LANDMARK CLASSES !!!
        return null;
    }

    public int compareTo(Landmark compareLandmark) {
        // Ascending order
        return (int) (getDistance() - compareLandmark.getDistance());
    }
}