package de.ifgi.wayto_prototype.demo;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import de.ifgi.wayto_prototype.R;
import de.ifgi.wayto_prototype.landmarks.Landmark;
import de.ifgi.wayto_prototype.landmarks.PointLandmark;

/**
 * Class to initialise the landmarks for demonstration purpose
 *
 * @author Marius Runde
 */
public abstract class LandmarkCollection {

    /**
     * Initialise the landmarks
     *
     * @return List of landmarks
     */
    public static ArrayList<Landmark> initLandmarks() {
        ArrayList<Landmark> landmarkCollection = new ArrayList<Landmark>();

        /*ALL_LANDMARKS.add(new RegionalLandmark("Aasee", 1500, R.drawable.landmark_water_body,
                new LatLng[]{
                        new LatLng(51.95728, 7.61329), new LatLng(51.95688, 7.61368),
                        new LatLng(51.95656, 7.61286), new LatLng(51.95675, 7.6126),
                        new LatLng(51.95596, 7.6111), new LatLng(51.95471, 7.60973),
                        new LatLng(51.95395, 7.60835), new LatLng(51.95339, 7.60638),
                        new LatLng(51.95265, 7.60655), new LatLng(51.95106, 7.60509),
                        new LatLng(51.95064, 7.60471), new LatLng(51.94874, 7.60054),
                        new LatLng(51.94744, 7.59904), new LatLng(51.94683, 7.59681),
                        new LatLng(51.94651, 7.59578), new LatLng(51.94617, 7.59681),
                        new LatLng(51.94516, 7.59702), new LatLng(51.94419, 7.59604),
                        new LatLng(51.94318, 7.59458), new LatLng(51.94244, 7.59501),
                        new LatLng(51.94278, 7.59668), new LatLng(51.94405, 7.59904),
                        new LatLng(51.94625, 7.6005), new LatLng(51.94858, 7.60333),
                        new LatLng(51.94934, 7.60462), new LatLng(51.94998, 7.60569),
                        new LatLng(51.95045, 7.60608), new LatLng(51.95384, 7.61097),
                        new LatLng(51.95458, 7.61376), new LatLng(51.95503, 7.61509),
                        new LatLng(51.95635, 7.61771), new LatLng(51.95712, 7.61754),
                        new LatLng(51.95754, 7.61406)
                })); TODO */

        landmarkCollection.add(new PointLandmark("Innenstadt", 2500, R.drawable.landmark_shopping,
                new LatLng(51.957173, 7.627344)));
        landmarkCollection.add(new PointLandmark("Schloss", 2750, R.drawable.landmark_castle,
                new LatLng(51.963622, 7.613187)));
        landmarkCollection.add(new PointLandmark("UKM", 2250, R.drawable.landmark_hospital,
                new LatLng(51.9603, 7.596262)));
        landmarkCollection.add(new PointLandmark("Halle Münsterland", 1750, R.drawable.landmark_stadium,
                new LatLng(51.949066, 7.63982)));
        landmarkCollection.add(new PointLandmark("Leonardo Campus", 1700, R.drawable.landmark_university,
                new LatLng(51.973388, 7.601604)));
        landmarkCollection.add(new PointLandmark("York Center", 1650, R.drawable.landmark_shopping,
                new LatLng(51.973418, 7.611111)));
        landmarkCollection.add(new PointLandmark("Hbf", 1500, R.drawable.landmark_train_station,
                new LatLng(51.956667, 7.635)));
        landmarkCollection.add(new PointLandmark("Aasee", 1500, R.drawable.landmark_water_body,
                new LatLng(51.949444, 7.603514)));
        landmarkCollection.add(new PointLandmark("Dom", 1450, R.drawable.landmark_church,
                new LatLng(51.962825, 7.625772)));
        landmarkCollection.add(new PointLandmark("Preußenstadion", 1300, R.drawable.landmark_stadium,
                new LatLng(51.92917, 7.624766)));
        landmarkCollection.add(new PointLandmark("Buddenturm", 720, R.drawable.landmark_tower,
                new LatLng(51.96623, 7.623138)));
        landmarkCollection.add(new PointLandmark("Aral Tankstelle", 550, R.drawable.landmark_gas_station,
                new LatLng(51.967284, 7.61386)));
        landmarkCollection.add(new PointLandmark("Picasso Museum", 500, R.drawable.landmark_museum,
                new LatLng(51.959883, 7.62651)));

        return landmarkCollection;
    }
}
