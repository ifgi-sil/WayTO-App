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
    public static final double NORTH = 45;
    /**
     * East direction ending angle in degree
     */
    public static final double EAST = 135;
    /**
     * South direction ending angle in degree
     */
    public static final double SOUTH = -135;
    /**
     * West direction ending angle in degree
     */
    public static final double WEST = -45;
    /**
     * ID of the North direction
     */
    public static final int NORTH_ID = 0;
    /**
     * ID of the East direction
     */
    public static final int EAST_ID = 1;
    /**
     * ID of the South direction
     */
    public static final int SOUTH_ID = 2;
    /**
     * ID of the West direction
     */
    public static final int WEST_ID = 3;
}
