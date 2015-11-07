package de.ifgi.wayto_prototype.map;

/**
 * Class to store information about heading directions
 *
 * @author Marius Runde
 */
public abstract class Heading {

    /**
     * North direction ending angle in degree
     */
    public static final double TOP = 45;
    /**
     * East direction ending angle in degree
     */
    public static final double RIGHT = 135;
    /**
     * South direction ending angle in degree
     */
    public static final double BOTTOM = -135;
    /**
     * West direction ending angle in degree
     */
    public static final double LEFT = -45;
    /**
     * ID of the North direction
     */
    public static final int TOP_ID = 0;
    /**
     * ID of the East direction
     */
    public static final int RIGHT_ID = 1;
    /**
     * ID of the South direction
     */
    public static final int BOTTOM_ID = 2;
    /**
     * ID of the West direction
     */
    public static final int LEFT_ID = 3;
}
