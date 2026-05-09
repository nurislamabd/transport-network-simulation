package models;

import simulation.Simulation;

/**
 * Shipment class simulates Shipment behavior. Each Shipment object has a unique ID variable 
 * (which starts from 1) in one simulation run (resets using resetNextId() after the simulation 
 * is done). ID var is also used to compare Shipment objects in equals() method. Has variables that 
 * contain its source and desitnation warehouses, a size variable which is an int between 1-3, 
 * and a location variable which contains the warehouse it is currently in and is null if it 
 * is on the truck. Each shipment starts with a location of its source warehouse. All vars are 
 * private and some have appropriate getter/setter methods. 
 * 
 * Additionally, it has methods like:
 * logStatus() to log info about the object to the log file, toString() which prints its id,
 * and delivered() which inidicates if the shipment was delivered to its destination, etc. 
 * 
 * logStatus() logs info about the Shipment object in this format: 
 * "Hour,ShipmentID,Size,SourceID,DestinationID,CurrentWarehouse,Status" to a log file 
 * "ShipmentsCSV.txt" csv file.
 * 
 * @author Nuris Abdyldaev
 * @version 05/02/2026
 */
public class Shipment
{
    // instance variables
    private static int nextId = 1;
    private int id;
    private Warehouse source;
    private Warehouse destination;
    private int size;    
    private Warehouse location;

    /**
     * Constructor for objects of class Shipment. Checks if the size is within the
     * specified bounds (1-3).
     * 
     * @param  source       source warehouse
     * @param  destination  destination warehouse
     * @param  size         size of the shipment (int between 1-3)
     */
    public Shipment(Warehouse source, Warehouse destination, int size)
    {
        if (size > 3 || size < 1) throw new IllegalArgumentException("Size of the shipment should be between 1-3");
        
        // initialise instance variables
        this.source      = source;
        this.destination = destination;
        this.size = size;
        location = source;
        id   = nextId++;
    }
    
    /**
     * Logs info about the Shipment object in this format: 
     * "Hour,ShipmentID,Size,SourceID,DestinationID,CurrentWarehouse,Status"
     * to a log file "ShipmentsCSV.txt" csv file. 
     */
    public void logStatus()
    {
        try {
            // Hour,ShipmentID,Size,SourceID,DestinationID,CurrentWarehouse,Status
            Simulation.getShipmentBuffer().append(Map.getCurrHour() + "," + id + "," + size + "," + source.getId() + "," + destination.getId() + ",");
            if (delivered()) Simulation.getShipmentBuffer().append("Destination Warehouse," + "Delivered");
            else if (location != null) Simulation.getShipmentBuffer().append("Source Warehouse," + "Awaiting pick up");
            else Simulation.getShipmentBuffer().append("Truck," + "Picked up");
            Simulation.getShipmentBuffer().newLine();
        } catch (java.io.IOException e) {
            System.err.println("Failed to open/find the file ShipmentsCSV.txt");
        }
    }
    
    @Override
    public boolean equals(Object object) 
    {
        // compare using id
        if (object instanceof Shipment) {
            Shipment shipment = (Shipment) object;
            return this.id == shipment.getId();
        }
        
        return false;
    }
    
    @Override
    public int hashCode()
    {
        return Integer.hashCode(id);
    }
    
    /**
     * Resets nextId to 1.
     */
    public static void resetNextId()
    {
        nextId = 1;
    }
    
    /**
     * Returns id of the Shipment object.
     * 
     * @return  id of the Shipment object
     */
    public int getId()
    {
        return id;
    }
    
    /**
     * Returns source of the Shipment object.
     * 
     * @return  source of the Shipment object
     */
    public Warehouse getSource()
    {
        return source;
    }
    
    /**
     * Returns destination of the Shipment object.
     * 
     * @return  destination of the Shipment object
     */
    public Warehouse getDestination()
    {
        return destination;
    }
    
    /**
     * Returns location of the Shipment object; null if truck.
     * 
     * @return  location of the Shipment object
     */
    public Warehouse getLocation()
    {
        return location;
    }
    
    /**
     * Returns size of the Shipment object
     * 
     * @return  size of the Shipment object
     */
    public int size()
    {
        return size;
    }
    
    /**
     * Sets the location of the Shipment object with its curr location; null for truck
     */
    public void setLocation(Warehouse currLocation)
    {
        location = currLocation;
    }
    
    /**
     * Returns true if delivered to its destination
     *
     * @return    true if delivered to its destination
     */
    public boolean delivered()
    {
        return destination.equals(location);
    }
    
    @Override
    public String toString()
    {
        return id + "";
    }
}