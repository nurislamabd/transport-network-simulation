package models;

/**
 * Contains main methods for Truck & Warehouse classes.
 *
 * @author Nuris Abdyldaev
 * @version 04/24/2026
 */
public interface Schedule {
    /**
     * Called each hour, allowing the object to perform an action.
     */
    public void action();
    
    /**
     * Will store the object’s current information into a log file.
     */ 
    public void logStatus();
}