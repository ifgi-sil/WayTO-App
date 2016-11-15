package de.ifgi.wayto_prototype.demo;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by Heinrich on 11.12.15.
 */
public abstract class PathSegmentCollection {

    private static ArrayList<Segment> segmentPath;

    /**
     * Initialize the segment points
     * @return
     */
    public static ArrayList<Segment> initSegmentPoints() {
        // Initialise the list of landmarks
        segmentPath = new ArrayList<Segment>();

        // Initialise the landmarks of the different cities
        initMuensterSegmentPoints();

        // Return the landmarks
        return segmentPath;
    }

    private static void initMuensterSegmentPoints() {
        segmentPath.add(new Segment(1, new ArrayList<LatLng>() {{
            add(new LatLng(51.94724,7.62219));
            add(new LatLng(51.94718,7.6222));
            add(new LatLng(51.94695,7.62225));
            add(new LatLng(51.94695,7.62225));
            add(new LatLng(51.94695,7.62216));
        }}));

        segmentPath.add(new Segment(2, new ArrayList<LatLng>() {{
            add(new LatLng(51.94695,7.62216));
            add(new LatLng(51.94693,7.62138));
            add(new LatLng(51.94691,7.62058));
            add(new LatLng(51.94691,7.62058));
            add(new LatLng(51.94697,7.62058));
        }}));

        segmentPath.add(new Segment(3, new ArrayList<LatLng>() {{
            add(new LatLng(51.94697,7.62058));
            add(new LatLng(51.94775,7.62049));
            add(new LatLng(51.94775,7.62049));
            add(new LatLng(51.94774,7.62039));
        }}));

        segmentPath.add(new Segment(4, new ArrayList<LatLng>() {{
            add(new LatLng(51.94774,7.62039));
            add(new LatLng(51.94774,7.62019));
            add(new LatLng(51.94771,7.61977));
            add(new LatLng(51.9477,7.61953));
            add(new LatLng(51.94768,7.61902));
            add(new LatLng(51.94767,7.61841));
            add(new LatLng(51.94767,7.61792));
            add(new LatLng(51.94767,7.61773));
            add(new LatLng(51.94765,7.61756));
            add(new LatLng(51.94765,7.61756));
            add(new LatLng(51.9477,7.61752));
        }}));

        segmentPath.add(new Segment(5, new ArrayList<LatLng>() {{
            add(new LatLng(51.9477,7.61752));
            add(new LatLng(51.94773,7.6175));
            add(new LatLng(51.94778,7.61745));
            add(new LatLng(51.94793,7.6173));
            add(new LatLng(51.94833,7.61727));
            add(new LatLng(51.94849,7.61726));
            add(new LatLng(51.94863,7.61726));
            add(new LatLng(51.94899,7.61726));
            add(new LatLng(51.94899,7.61726));
            add(new LatLng(51.94899,7.61713));
        }}));

        segmentPath.add(new Segment(6, new ArrayList<LatLng>() {{
            add(new LatLng(51.94899,7.61713));
            add(new LatLng(51.949,7.61705));
            add(new LatLng(51.94901,7.61692));
            add(new LatLng(51.94902,7.61684));
            add(new LatLng(51.94905,7.61673));
            add(new LatLng(51.94908,7.61659));
            add(new LatLng(51.94913,7.61645));
            add(new LatLng(51.94913,7.61645));
            add(new LatLng(51.94907,7.6164));
        }}));

        segmentPath.add(new Segment(7, new ArrayList<LatLng>() {{
            add(new LatLng(51.94907,7.6164));
            add(new LatLng(51.949,7.61634));
            add(new LatLng(51.94883,7.61619));
            add(new LatLng(51.94871,7.61609));
            add(new LatLng(51.94863,7.616));
            add(new LatLng(51.94857,7.61595));
            add(new LatLng(51.94844,7.61581));
            add(new LatLng(51.94822,7.61562));
            add(new LatLng(51.94751,7.61501));
            add(new LatLng(51.94676,7.6144));
            add(new LatLng(51.94668,7.61434));
            add(new LatLng(51.94619,7.61393));
            add(new LatLng(51.94619,7.61393));
            add(new LatLng(51.94612,7.61401));
        }}));

        segmentPath.add(new Segment(8, new ArrayList<LatLng>() {{
            add(new LatLng(51.94612,7.61401));
            add(new LatLng(51.94567,7.61456));
            add(new LatLng(51.94567,7.61456));
            add(new LatLng(51.94566,7.61454));
            add(new LatLng(51.94563,7.61451));
        }}));

        segmentPath.add(new Segment(9, new ArrayList<LatLng>() {{
            add(new LatLng(51.94563,7.61451));
            add(new LatLng(51.94547,7.61432));
            add(new LatLng(51.94517,7.6142));
            add(new LatLng(51.9447,7.61465));
            add(new LatLng(51.94423,7.61465));
            add(new LatLng(51.94338,7.6144));
            add(new LatLng(51.94338,7.6144));
            add(new LatLng(51.94338,7.61448));
        }}));

        segmentPath.add(new Segment(10, new ArrayList<LatLng>() {{
            add(new LatLng(51.94338,7.61448));
            add(new LatLng(51.94336,7.61463));
            add(new LatLng(51.94335,7.61481));
            add(new LatLng(51.94334,7.61493));
            add(new LatLng(51.94334,7.61508));
            add(new LatLng(51.94333,7.6153));
            add(new LatLng(51.94334,7.61559));
            add(new LatLng(51.94334,7.61567));
            add(new LatLng(51.94334,7.61567));
            add(new LatLng(51.9434,7.61569));
        }}));

        segmentPath.add(new Segment(11, new ArrayList<LatLng>() {{
            add(new LatLng(51.9434,7.61569));
            add(new LatLng(51.94356,7.61576));
            add(new LatLng(51.94371,7.61586));
            add(new LatLng(51.94381,7.61594));
            add(new LatLng(51.94387,7.61599));
            add(new LatLng(51.94391,7.61603));
            add(new LatLng(51.94394,7.61606));
            add(new LatLng(51.94395,7.61608));
            add(new LatLng(51.94397,7.61612));
            add(new LatLng(51.944,7.61617));
            add(new LatLng(51.94402,7.61624));
            add(new LatLng(51.94405,7.61632));
            add(new LatLng(51.9442,7.61681));
            add(new LatLng(51.94423,7.61692));
            add(new LatLng(51.94433,7.61731));
            add(new LatLng(51.94451,7.61805));
            add(new LatLng(51.94469,7.61895));
            add(new LatLng(51.94469,7.61895));
            add(new LatLng(51.94462,7.61897));
        }}));

        segmentPath.add(new Segment(12, new ArrayList<LatLng>() {{
            add(new LatLng(51.94462,7.61897));
            add(new LatLng(51.94438,7.61903));
            add(new LatLng(51.94422,7.61907));
            add(new LatLng(51.94407,7.61911));
            add(new LatLng(51.94389,7.61917));
            add(new LatLng(51.94377,7.61922));
            add(new LatLng(51.94329,7.61942));
            add(new LatLng(51.94329,7.61942));
            add(new LatLng(51.94332,7.61953));
        }}));

        segmentPath.add(new Segment(13, new ArrayList<LatLng>() {{
            add(new LatLng(51.94332,7.61953));
            add(new LatLng(51.94343,7.61991));
            add(new LatLng(51.94356,7.62025));
            add(new LatLng(51.94362,7.62042));
            add(new LatLng(51.94364,7.62048));
            add(new LatLng(51.94364,7.6205));
            add(new LatLng(51.94368,7.62061));
            add(new LatLng(51.94373,7.62079));
            add(new LatLng(51.9438,7.62103));
            add(new LatLng(51.94385,7.62121));
            add(new LatLng(51.94386,7.62128));
            add(new LatLng(51.94386,7.62128));
            add(new LatLng(51.94393,7.62125));
        }}));

        segmentPath.add(new Segment(14, new ArrayList<LatLng>() {{
            add(new LatLng(51.94393,7.62125));
            add(new LatLng(51.94439,7.62101));
            add(new LatLng(51.9444,7.62101));
            add(new LatLng(51.944412, 7.621071));
        }}));

        segmentPath.add(new Segment(15, new ArrayList<LatLng>() {{
            //add(new LatLng(51.9444,7.62101));
            add(new LatLng(51.944412, 7.621071));
            add(new LatLng(51.944581, 7.621798));
            add(new LatLng(51.944649, 7.622314));
        }}));
    }
}
