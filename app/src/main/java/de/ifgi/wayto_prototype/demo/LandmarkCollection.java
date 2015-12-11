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

    private static ArrayList<Landmark> landmarks;

    /**
     * Initialise the landmarks
     *
     * @return List of landmarks
     */
    public static ArrayList<Landmark> initLandmarks() {
        // Initialise the list of landmarks
        landmarks = new ArrayList<Landmark>();

        // Initialise the landmarks of the different cities
        initMannheimLandmarks();
        initMuensterLandmarks();

        // Return the landmarks
        return landmarks;
    }

    /**
     * Initialise the landmarks of Mannheim
     */
    private static void initMannheimLandmarks() {
        // TODO wait for landmarks from Stefan Münzer
    }

    /**
     * Initialise the landmarks of Münster
     */
    private static void initMuensterLandmarks() {
        /*landmarks.add(new RegionalLandmark("Aasee", 1500, R.drawable.landmark_water_body,
                R.drawable.landmark_coloured_water_body,
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
                }, new LatLng(51.951133, 7.605916)));
*/
/*        landmarks.add(new PointLandmark("Halle Münsterland", 50000, R.drawable.landmark_stadium,
                R.drawable.landmark_coloured_shopping, new LatLng(51.949066,7.63982)));*/
        landmarks.add(new PointLandmark("HBF", 5000, R.drawable.landmark_train_station,
                R.drawable.landmark_coloured_shopping, new LatLng(51.956667,7.635)));
        landmarks.add(new PointLandmark("Innenstadt", 8000, R.drawable.landmark_city_center,
                R.drawable.landmark_coloured_shopping, new LatLng(51.962825,7.625772)));
        landmarks.add(new PointLandmark("Fußballstadion", 5000, R.drawable.landmark_stadium,
                R.drawable.landmark_coloured_shopping, new LatLng(51.92917,7.624766)));
/*        landmarks.add(new PointLandmark("Schloss", 500000, R.drawable.landmark_castle,
                R.drawable.landmark_coloured_shopping, new LatLng(51.963622,7.613187)));*/
        landmarks.add(new PointLandmark("Wasserturm", 7000, R.drawable.landmark_tower_water,
                R.drawable.landmark_coloured_shopping, new LatLng(51.947076, 7.620066)));
/*        landmarks.add(new PointLandmark("Netto", 50000, R.drawable.landmark_shopping,
                R.drawable.landmark_coloured_shopping, new LatLng(51.949347, 7.616978)));*/
/*        landmarks.add(new PointLandmark("Schrunz bakery", 50000, R.drawable.landmark_bakery,
                R.drawable.landmark_coloured_shopping, new LatLng(51.945213, 7.622094)));*/
        landmarks.add(new PointLandmark("Cineplex", 5000, R.drawable.landmark_cinema,
                R.drawable.landmark_coloured_shopping, new LatLng(51.950336, 7.637018)));
/*        landmarks.add(new PointLandmark("harbour recreation area", 50000, R.drawable.landmark_port,
                R.drawable.landmark_coloured_shopping, new LatLng(51.952093, 7.639746)));*/
        landmarks.add(new PointLandmark("Zoo", 5000, R.drawable.landmark_zoo,
                R.drawable.landmark_coloured_shopping, new LatLng(51.947907, 7.589782)));
        landmarks.add(new PointLandmark("WL Bank", 5000, R.drawable.landmark_bank,
                R.drawable.landmark_coloured_shopping, new LatLng(51.943852, 7.613416)));
        landmarks.add(new PointLandmark("LVM", 5000, R.drawable.landmark_bank,
                R.drawable.landmark_coloured_shopping, new LatLng(51.949313, 7.615925)));
    }
}
