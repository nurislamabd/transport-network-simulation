package models;

/**
 * Map class contains static vars which keep track of hours passed in the simulation and 
 * map boundaries. Map's x-axis and y-axis  (in miles) can be set using an apporpriate Map 
 * constructor. All the variables are private and can be accessed through getter methods.
 * Also, has methods like: clockIncrement() to increment currHour and reset() to reset all
 * values and IDs of Truck, Warehouse, and Shipment classes.
 *
 * @author Nuris Abdyldaev
 * @version 05/02/2026
 */
public class Map
{
    // instance variables
    private static int currHour;
    private static double mapX;
    private static double mapY;

    /**
     * Empty constructor for objects of class Map (to be used by Truck, Warehouse, 
     */
    public Map()
    {
    }
    
    /**
     * Constructor for objects of class Map
     */
    public Map(double x, double y)
    {
        // initialise instance variables
        currHour = 0;
        mapX = x;
        mapY = y;
    }

    /**
     * Getter for currHour
     * 
     * @return    the value of currHour
     */
    public static int getCurrHour()
    {
        return currHour;
    }

    /**
     * Getter for mapX 
     *
     * @return    the value of mapX
     */
    public static double getMapX()
    {
        return mapX;
    }

    /**
     * Getter for mapY
     *
     * @return    the value of mapY
     */
    public static double getMapY()
    {
        return mapY;
    }

    /**
     * Incrementer for currHour
     */
    public static void clockIncrement()
    {
        currHour++;
    }

    /**
     * Resetter for everything
     */
    public static void reset()
    {
        // reset everything
        currHour = 0;
        mapX = 0;
        mapY = 0;
        Shipment.resetNextId();
        Warehouse.resetNextId();
        Truck.resetNextId();
    }
}