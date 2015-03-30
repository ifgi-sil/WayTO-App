package de.ifgi.wayto_prototype.landmarks;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

/**
 * Landmark that can be located at a specific region
 *
 * @author Marius Runde
 */
public class RegionalLandmark extends Landmark {

    /**
     * Tag for the logger
     */
    private static final String TAG = RegionalLandmark.class.toString();

    /**
     * Shape points that define the region of the landmark
     */
    private LatLng[] shapePoints;

    /**
     * Centroid (not exactly but fair enough for displaying purpose)
     */
    private LatLng centroid;

    /**
     * Constructor of the RegionalLandmark class
     *
     * @param title                    Title (or name)
     * @param referenceRadius          Radius of reference
     * @param categoryDrawableBlack    Category's drawable for the symbol on the map (black)
     * @param categoryDrawableColoured Category's drawable for the symbol on the map (coloured)
     * @param shapePoints              Shape points that define the region of the landmark
     * @param centroid                 Centroid (not exactly but fair enough for displaying purpose)
     */
    public RegionalLandmark(String title, double referenceRadius, int categoryDrawableBlack,
                            int categoryDrawableColoured, LatLng[] shapePoints, LatLng centroid) {
        super(title, referenceRadius, categoryDrawableBlack, categoryDrawableColoured);
        this.shapePoints = shapePoints;
        this.centroid = centroid;
    }

    /**
     * Transform the regional into a point landmark
     *
     * @return Point landmark with the centroid position of the regional landmark
     */
    public PointLandmark transformIntoPointLandmark() {
        return new PointLandmark(getTitle(), getReferenceRadius(), getCategoryDrawableBlack(),
                getCategoryDrawableColoured(), getPosition());
    }

    public LatLng[] getShapePoints() {
        return shapePoints;
    }

    public void setShapePoints(LatLng[] shapePoints) {
        this.shapePoints = shapePoints;
    }

    public LatLng getPosition() {
        return centroid;
    }

    public LatLngBounds getBounds() {
        double south = 91, west = 181, north = -91, east = -181;
        for (LatLng point : shapePoints) {
            if (point.latitude < south) south = point.latitude;
            if (point.latitude > north) north = point.latitude;
            if (point.longitude < west) west = point.longitude;
            if (point.longitude > east) east = point.longitude;
        }
        LatLng southwest = new LatLng(south, west), northeast = new LatLng(north, east);
        LatLngBounds bounds = new LatLngBounds(southwest, northeast);
        return bounds;
    }
}