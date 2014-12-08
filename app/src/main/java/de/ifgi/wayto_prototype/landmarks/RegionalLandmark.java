package de.ifgi.wayto_prototype.landmarks;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

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
     * Constructor of the RegionalLandmark class
     *
     * @param title            Title (or name)
     * @param referenceRadius  Radius of reference
     * @param categoryDrawable Category's drawable for the symbol on the  map
     * @param shapePoints      Shape points that define the region of the landmark
     */
    public RegionalLandmark(String title, double referenceRadius, int categoryDrawable,
                            LatLng[] shapePoints) {
        super(title, referenceRadius, categoryDrawable);
        this.shapePoints = shapePoints;
    }

    /**
     * Transform the regional into a point landmark
     *
     * @return Point landmark with the centroid position of the regional landmark
     */
    public PointLandmark transformIntoPointLandmark() {
        return new PointLandmark(getTitle(), getReferenceRadius(), getCategoryDrawable(),
                getPosition());
    }

    public LatLng[] getShapePoints() {
        return shapePoints;
    }

    public void setShapePoints(LatLng[] shapePoints) {
        this.shapePoints = shapePoints;
    }

    public LatLng getPosition() {
        ArrayList<LatLng> points = new ArrayList<LatLng>();
        for (LatLng point : shapePoints) {
            points.add(point);
        }

        // Add the first point of the list of points to the end for the calculations
        points.add(points.get(0));

        // First calculate the acreage
        double a = 0;
        for (int i = 0; i < points.size() - 1; i++) {
            LatLng p0 = points.get(i);
            LatLng p1 = points.get(i + 1);
            a += p0.latitude * p1.longitude - p1.latitude * p0.longitude;
        }
        a /= 2;

        // Then calculate the latitude of the centroid
        double latitude = 0;
        for (int i = 0; i < points.size() - 1; i++) {
            LatLng p0 = points.get(i);
            LatLng p1 = points.get(i + 1);
            latitude = (p0.latitude + p1.latitude) * (p0.latitude * p1.longitude - p1.latitude *
                    p0.longitude);
        }
        latitude /= (6 * a);

        // Then calculate the longitude of the centroid
        double longitude = 0;
        for (int i = 0; i < points.size() - 1; i++) {
            LatLng p0 = points.get(i);
            LatLng p1 = points.get(i + 1);
            longitude = (p0.longitude + p1.longitude) * (p0.latitude * p1.longitude - p1.latitude *
                    p0.longitude);
        }
        longitude /= (6 * a);

        // Return the calculated centroid
        return new LatLng(latitude, longitude);
    }
}