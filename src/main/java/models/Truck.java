package models;

import datastructures.MyArrayList;
import datastructures.ArrayBasedStack;
import simulation.Simulation;


/**
 * Truck class simulates trucks behavior. Each Truck object has a unique ID variable 
 * (which starts from 1) in one simulation run (resets using resetNextId() after the simulation 
 * is done). ID var is also used to compare Truck objects in equals() method. Has variables
 * that keep track of its position (posX and posY in double format), size and speed of the truck, 
 * its status, current destination, shipments in load, shipments in manifest, and if action was
 * done by the warehouse. All vars are private and some have appropriate getter/setter methods. 
 * Implements Schedule and thus overrides methods action(), which checks if the action was done by 
 * the warehouse (unload() and/or pickup()) and acts accordingly, and logStatus() which logs info 
 * about the Warehouse object in this format: 
 * "Hour,TruckID,PosX,PosY,LoadSize,Speed,Status,CurrLoad,SpaceInLoad,DestinationWarehouseID,PriorityManifestID,Manifest"
 * to a log file "TrucksCSV.txt" csv file. 
 * 
 * Truck's order of pickup is determined by the distance to the shipment and their IDs. The shorter
 * the distance to the shipment, the more it is prioritized and if distance is equal, newer shipments
 * are prioritized. 
 * 
 * Truck's load works as a stack (LIFO). Only last shipment loaded can be unloaded. If the load is full
 * or there is not enough space for the shipment that was decided to be picked up, Truck will first 
 * deliver the last shipment loaded (if there will be enough space for the new shipment that is to be
 * picked up, it will pick up first). 
 * 
 * Thus, Truck's next destination is determined by the space in load and the next shipment to be picked 
 * up (which might change as the truck moves). Truck will always pickup a shipment first if it has
 * appropriate space for it in the load.
 * 
 * Truck's action method checks if the action was done by the warehouse (unload() and/or pickup())
 * and if done, sets vars accordingly and checks if its next destination is the same warehouse where
 * it is now. If yes, adds the truck to the warehouse's queue and returns. If the action was not 
 * done by the warehouses, it checks if the truck is waiting in the queue and sets status accordingly
 * if yes. If is not waiting, it didn't reach its destination so it moves the truck towards its 
 * destination and sets status with appropriate value.
 * 
 * Trucks can only unload once and/or pick up once per action() (warehouse's action). Can't pick up 
 * twice or unload twice at a time. Thus trucks either move towards their destination, pickup and/or
 * unload at the warehouse, or wait in the queue of the warehouse.
 * 
 * Additionally, it has methods like:
 * addManifest() to add shipments to the manifest, peekLoad() to get last loaded shipment, finished() 
 * which indicates if the truck is done with the shipments, spaceInLoad() which returns the available 
 * space in load, getPriorityManifest() which returns the prioritized shipment to be picked up, etc.
 * 
 * 
 * @author Nuris Abdyldaev
 * @version 05/02/2026
 */
public class Truck implements Schedule
{
    // instance variables
    private static int nextId = 1;
    private int id;
    private double posX;
    private double posY;
    private boolean actionDone;
    private String status;
    private Warehouse currDestination;
    private int loadSize;
    private int speed;
    private int inLoad;
    private ArrayBasedStack<Shipment> load;
    private MyArrayList<Shipment> manifest;
    
    /**
     * Constructor for objects of class Truck. Checks if the params are within the
     * specified bounds.
     * 
     * @param  x     position x of the Truck
     * @param  y     position y of the Truck
     * @param  size  size of the load of the Truck (int between 2-5)
     */
    public Truck(double x, double y, int size)
    {
        if (size > 5 || size < 2) throw new IllegalArgumentException("Size of the Truck should be between 2-5");
        if (x < 0 || x > Map.getMapX()) throw new IllegalArgumentException("x position is out of specified bounds");
        if (y < 0 || y > Map.getMapY()) throw new IllegalArgumentException("y position is out of specified bounds");
        
        // initialise instance variables
        id   = nextId++;
        posX = x;
        posY = y;
        actionDone = false;
        status = "Done";
        currDestination = null;
        loadSize = size;
        speed = 6 - loadSize;
        inLoad = 0;
        load = new ArrayBasedStack<Shipment>();
        manifest = new MyArrayList<Shipment>();
    }
    
    /**
     * Returns id of the Truck object
     * 
     * @return  id of the Truck object
     */
    public int getId()
    {
        return id;
    }

    /**
     * Checks if the truck is done with all the shipments. If yes, sets status with "Done" and returns.
     * Checks the actionDone variable which keeps track of the actions done by the Truck object in Warehouses. 
     * If unload() and/or pickup() were called, actionDone would be true and this method would set actionDone
     * to false and set status to "Being processed at loading dock" and also checks if the next destination is
     * the same warehouse and puts it into the warehouse's trucks queue.
     * If actionDone is false, updates destination and checks if it reached its destination. If yes, sets
     * status to "Waiting for the available loading dock", otherwise moves towards its destination and sets
     * status with approptiate value.
     */
    @Override
    public void action()
    {
        // if destination is null and no shipments to be made, truck is done
        if (!actionDone && currDestination == null && manifest.size() == 0 && load.size() == 0) {
            status = "Done";
            return;
        }
        
        // first check if the action was done by the warehouse
        if (actionDone) {
            actionDone = false;
            status = "Being processed at loading dock";
            // if the destination is the same get into warehouse queue
            if (currDestination != null && currDestination.getX() == posX && currDestination.getX() == posX) currDestination.addTruckToQueue(this);
            return;
        }
    
        updateDestination();
        
        // if didn't reach the destination yet, move towards it; wait otherwise
        if (currDestination.getX() == posX && currDestination.getX() == posX) {
            status = "Waiting for the available loading dock";
            return;
        } else move();
    }
    
    /**
     * Logs info about the Truck object in this format: 
     * "Hour,TruckID,PosX,PosY,LoadSize,Speed,Status,CurrLoad,SpaceInLoad,DestinationWarehouseID,PriorityManifestID,Manifest"
     * to a log file "TrucksCSV.txt" csv file. 
     */
    @Override
    public void logStatus()
    {
        try {
            // Hour,TruckID,PosX,PosY,LoadSize,Speed,Status,CurrLoad,SpaceInLoad,DestinationWarehouseID,PriorityManifestID,Manifest
            Simulation.getTruckBuffer().append(Map.getCurrHour() + "," + id + "," + posX + "," + posY + "," + loadSize + "," + speed + "," + status + "," + load.toString() + "," + spaceInLoad() + ",");
            if (currDestination != null) Simulation.getTruckBuffer().append(currDestination.getId() + ",");
            else Simulation.getTruckBuffer().append(null + ",");
            if (getPriorityManifest() != null) Simulation.getTruckBuffer().append(getPriorityManifest().toString() + ",");
            else Simulation.getTruckBuffer().append(null + ",");
            Simulation.getTruckBuffer().append(manifest.toString());
            Simulation.getTruckBuffer().newLine();
        } catch (java.io.IOException e) {
            System.err.println("Failed to open/find the file TrucksCSV.txt");
        }
    }
    
    /**
     * Change the coordinates of this truck (moves it) and if reached warehouse, 
     * joins its queue & action history var updated
     */
    private void move()
    {
        if (currDestination == null) return;
        
        double distance = Math.hypot(currDestination.getX() - posX, currDestination.getY() - posY);
        double factor;
        
        // update coordinates; if reached warehouse add this truck to warehouse queue
        if (distance < speed) {
            posX = currDestination.getX();
            posY = currDestination.getY();
            currDestination.addTruckToQueue(this);
        } else {
            factor = speed / distance;
            posX = posX + factor * (currDestination.getX() - posX);
            posY = posY + factor * (currDestination.getY() - posY);
        }
    }
    
    /**
     * If there is space in load for the shipment that is prioritized in manifest, 
     * truck will first pick up that shipment regardless of the shipments in its load.
     * Updates destination based on distances to warehouses and shipment id; 
     * Is called after a shipment is added.
     */
    private void updateDestination() 
    {
        // sort manifest so its always up to date
        insertionSortOfManifest(); 
        
        // if no space for priority manifest, deliver first
        if (load.peek() != null && (spaceInLoad() == 0 || getPriorityManifest() != null && spaceInLoad() < getPriorityManifest().size())) {
            currDestination = load.peek().getDestination();
            status = "Driving to deliver";
            return;
        }
        
        // if there is space get priority manifest; if no manifest, deliver load
        if (getPriorityManifest() != null) {
            currDestination = getPriorityManifest().getSource();
            status = "Driving to pick up";
            return;
        } else if (load.peek() != null) {
            currDestination = load.peek().getDestination();
            status = "Driving to deliver";
            return;
        }
        
        // no manifest, no load, no next destination
        currDestination = null;
        status = "Done";
    }
    
    /**
     * Returns true if all shipments are delivered.
     *
     * @return    true if all shipments are delivered
     */
    public boolean finished()
    {
        return manifest.size() == 0 && load.size() == 0;
    }
    
    /**
     * Resets nextId to 1.
     */
    public static void resetNextId()
    {
        nextId = 1;
    }
    
    /**
     * Adds a shipment to the manifest list; calls updateDestination(). Doesn't allow 
     * shipments that are greater than truck's load size 
     *
     * @param  shipment  shipment to be added into manifest list
     * @return false if load size is less than the size of the shipment
     */
    public boolean addManifest(Shipment shipment)
    {
        if (loadSize < shipment.size()) return false;
        
        // add and update destination
        manifest.add(shipment);
        updateDestination();
        return true;
    }
    
    /**
     * Insertion sort of the manifest list.
     *
     * @param  shipment  shipment to be added into manifest list
     */
    private void insertionSortOfManifest()
    {
        // insertion sort with distance in descending order and id in ascending (so that the priority is at the end of the array)
        int length = manifest.size();
        int j;
        
        // insertion sort algorithm 
        for (int i = 1; i < length; ++i) {
            Shipment temp = manifest.get(i);
            j = i;            
            double distanceComparison = this.compareDistances(temp, manifest.get(j - 1));

            while (j > 0 && (distanceComparison < 0 || distanceComparison == 0 && manifest.get(j - 1).getId() > temp.getId())) {
                manifest.set(j, manifest.get(j - 1));
                --j;
            }
            
            manifest.set(j, temp);
        }
    }
    
    /**
     * Compares distances of the 2 param shipments
     *
     * @param   s1  shipment 1 to be compared
     * @param   s2  shipment 2 to be compared
     * @return  an positive int if s2 is farther away, negative int if s2 is closer and a 0 if the distance is equal 
     */
    private double compareDistances(Shipment s1, Shipment s2)
    {
        return this.getDistance(s2) - this.getDistance(s1);
    }
    
    /**
     * Returns the distance to a shipment (warehouse it is in)
     *
     * @param   shipment the distance to which is to be returned
     * @return  the distance to a shipment 
     */
    private double getDistance(Shipment shipment)
    {
        return Math.hypot(shipment.getSource().getX() - posX, shipment.getSource().getY() - posY);
    }
    
    /**
     * Adds a shipment to the load stack and removes the last (priority) manifest;
     * sets actionDone to true and status with appropriate values.
     */
    public void pickup()
    {
        // add to load, update actionDone, inLoad, and destination
        actionDone = true;
        load.push(manifest.remove(manifest.size() - 1));
        inLoad += load.peek().size();
        updateDestination();
    }
    
    /**
     * Pops a shipment from the load stack if at the warehouse which is the 
     * destination location for one of the shipments in the truck’s load stack; 
     * sets actionDone to true and status with appropriate values.
     *
     * @return    a shipment popped from the load stack 
     */
    public Shipment unload()
    {
        // remove from load; update actionDone, inLoad, destination; and return removed shipment
        actionDone = true;
        inLoad -= load.peek().size();
        Shipment shipment = load.pop();
        updateDestination();
        return shipment;
    }
    
    /**
     * Returns a peek of load.
     *
     * @return    a peek of load
     */
    public Shipment peekLoad()
    {
        if (load.isEmpty()) return null;
        return load.peek();
    }
    
    /**
     * Returns the last shipment in manifest (the one that has the highest priority)
     *
     * @return    the last shipment in manifest
     */
    public Shipment getPriorityManifest()
    {
        if (manifest.isEmpty()) return null;
        return manifest.get(manifest.size() - 1);
    }
    
    /**
     * Returns the loadSize (for unit-tests)
     *
     * @return    the loadSize
     */
    public int getLoadSize()
    {
        return loadSize;
    }
    
    /**
     * Returns posX of the Truck object (for unit-tests)
     * 
     * @return  posX of the Truck object
     */
    public double getX()
    {
        return posX;
    }
    
    /**
     * Returns posY of the Truck object (for unit-tests)
     * 
     * @return  posY of the Truck object
     */
    public double getY()
    {
        return posY;
    }
    
    /**
     * Returns status of the Truck object (for unit-tests)
     * 
     * @return  status of the Truck object
     */
    public String getStatus()
    {
        return status;
    }
    
    /**
     * Returns currDestination of the Truck object (for unit-tests)
     * 
     * @return  currDestination of the Truck object
     */
    public Warehouse getCurrDestination()
    {
        return currDestination;
    }
    
    /**
     * Returns an int of the space available in the load
     *
     * @return    an int of the space available in the load
     */
    public int spaceInLoad()
    {
        return loadSize - inLoad;
    }
    
    @Override
    public boolean equals(Object object) 
    {
        // compare using IDs
        if (object instanceof Truck) {
            Truck truck = (Truck) object;
            return this.id == truck.getId();
        }
        
        return false;
    }
    
    @Override
    public int hashCode()
    {
        return Integer.hashCode(id);
    }
}