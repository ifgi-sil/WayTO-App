package de.ifgi.wayto_prototype.landmarks;

import com.google.android.gms.maps.model.LatLng;

/**
 * Super class for landmarks
 *
 * @author Marius Runde
 */
public abstract class Landmark implements Comparable<Landmark> {

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
     * Shifted position if the landmark is off-screen
     */
    private LatLng offScreenPosition = null;
    /**
     * Category's drawable for the symbol on the map (black)
     */
    private int categoryDrawableBlack;
    /**
     * Category's drawable for the symbol on the map (coloured)
     */
    private int categoryDrawableColoured;

    /**
     * (Super) Constructor of the Landmark class
     *
     * @param title                    Title (or name)
     * @param referenceRadius          Radius of reference
     * @param categoryDrawableBlack    Category's drawable for the symbol on the map (black)
     * @param categoryDrawableColoured Category's drawable for the symbol on the map (coloured)
     */
    public Landmark(String title, double referenceRadius, int categoryDrawableBlack,
                    int categoryDrawableColoured) {
        this.title = title;
        this.referenceRadius = referenceRadius;
        this.categoryDrawableBlack = categoryDrawableBlack;
        this.categoryDrawableColoured = categoryDrawableColoured;
    }

    @Override
    public String toString() {
        return title + ": " + getPosition().toString() + ", reference radius: " + referenceRadius;
    }

    public String getTitle() {
        return title;
    }

    public double getReferenceRadius() {
        return referenceRadius;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public LatLng getOffScreenPosition() {
        return offScreenPosition;
    }

    public void setOffScreenPosition(LatLng offScreenPosition) {
        this.offScreenPosition = offScreenPosition;
    }

    public int getCategoryDrawableBlack() {
        return categoryDrawableBlack;
    }

    public int getCategoryDrawableColoured() {
        return categoryDrawableColoured;
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