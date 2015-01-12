package de.ifgi.wayto_prototype.landmarks;

import com.google.android.gms.maps.model.LatLng;

/**
 * Landmark that can be located at a specific point
 *
 * @author Marius Runde
 */
public class PointLandmark extends Landmark {

    /**
     * Tag for the logger
     */
    private static final String TAG = PointLandmark.class.toString();

    /**
     * Position as geolocated point
     */
    private LatLng position;

    /**
     * Constructor of the PointLandmark class
     *
     * @param title                    Title (or name)
     * @param referenceRadius          Radius of reference
     * @param categoryDrawableBlack    Category's drawable for the symbol on the map (black)
     * @param categoryDrawableColoured Category's drawable for the symbol on the map (coloured)
     * @param position                 Position as geolocated point
     */
    public PointLandmark(String title, double referenceRadius, int categoryDrawableBlack,
                         int categoryDrawableColoured, LatLng position) {
        super(title, referenceRadius, categoryDrawableBlack, categoryDrawableColoured);
        this.position = position;
    }

    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }
}