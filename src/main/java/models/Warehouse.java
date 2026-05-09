package models;

import datastructures.MyArrayList;
import datastructures.MyQueue;
import datastructures.QueueNode;
import simulation.Simulation;


/**
 * Warehouse class simulates warehouse behavior. Each Warehouse object has a unique ID variable 
 * (which starts from 1) in one simulation run (resets using resetNextId() after the simulation 
 * is done). ID var is also used to compare Warehouse objects in equals() method. Has variables
 * that keep track of its position (posX and posY in double format), number of loading docks it 
 * has, inventory of shipments (+vars to keep it sorted), and queue of trucks that are waiting 
 * to be processed at Warehouse's loading docks. All vars are private and some have appropriate 
 * getter/setter methods. 
 * 
 * Implements Schedule and thus overrides methods action() which checks if there are trucks to 
 * be processed at the trucks queue and processes them accordingly and logStatus() which logs info 
 * about the Warehouse object in this format: 
 * "Hour,WarehouseID,PosX,PosY,LoadingDocks,TrucksQueueSize,TrucksInQueue,InventorySize,ShipmentsInInventory"
 * to a log file "WarehousesCSV.txt" csv file. 
 * 
 * Warehouse's action method handles truck's pickups and unloads using Truck class's pickup() 
 * and unload() methods. It can only unload once and load the truck once per one action() call 
 * (unload once OR load once OR unload once and load once). It also updates Shipment objects' 
 * locations accordingly using a setter method. 
 * 
 * Additionally, it has methods like:
 * addToInventory() to add shipments to the inventory, removeFromInventory() to remove shipments from the inventory,
 * addTruckToQueue() which adds the Truck object to Warehouse's queue (this method is used by the Truck objects), etc.
 *
 * @author Nuris Abdyldaev
 * @version 05/02/2026
 */
public class Warehouse implements Schedule
{
    // instance variables
    private static int nextId = 1;
    private int id;
    private double posX;
    private double posY;
    private int loadingDocks;
    private MyArrayList<Shipment> inventory;
    private int inventorySortedUntil;
    private MyQueue<Truck> trucks;
    
    /**
     * Constructor for objects of class Warehouse. Checks if the params are within the
     * specified bounds.
     * 
     * @param  x      position x of the warehouse
     * @param  y      position y of the warehouse
     * @param  docks  number of loading docks of the warehouse (int between 1-3)
     */
    public Warehouse(double x, double y, int docks)
    {
        if (docks > 3 || docks < 1) throw new IllegalArgumentException("Number of loading docks should be between 1-3");
        if (x < 0 || x > Map.getMapX()) throw new IllegalArgumentException("x position is out of specified bounds");
        if (y < 0 || y > Map.getMapY()) throw new IllegalArgumentException("y position is out of specified bounds");
        
        // initialise instance variables
        id   = nextId++;
        posX = x;
        posY = y;
        loadingDocks = docks;
        inventory    = new MyArrayList<Shipment>();
        trucks       = new MyQueue<Truck>();
        inventorySortedUntil = -1;
    }
    
    /**
     * If trucks.size() == 0, does nothing. Otherwise, polls trucks from the queue (equal to
     * the number of loading docks; breaks if less) and processes them one by one. It checks
     * if their shipment in load.peek() is to be delivered to this warehouse and if yes,
     * calls truck's unload() and adds the shipment to the inventory. Then it checks truck's 
     * priority manifest to see if the shipment is in the inventory of this warehouse. If yes
     * calls truck's pickup() and removes the shipment from the inventory.
     */
    @Override
    public void action()
    {
        // if no trucks in queue, do nothing; otherwise process the ones in loading docks
        if (trucks.size() != 0) {
            for (int i = 0; i < loadingDocks; i++) {
                if (trucks.peek() == null) break;
                
                Truck truck = trucks.poll(); 
                
                // unload if shipment from peekLoad() is to be delivered to this warehouse and add it to inventory
                if (truck.peekLoad() != null && this.equals(truck.peekLoad().getDestination())) {
                    this.addToInventory(truck.unload());
                }
                
                // check if truck can pick up its next shipment
                if (truck.getPriorityManifest() != null && truck.getPriorityManifest().size() <= truck.spaceInLoad()) {
                    if (this.equals(truck.getPriorityManifest().getSource())) {
                        if(!this.removeFromInventory(truck.getPriorityManifest())) {
                            System.err.println("The shipment was not in the inventory");
                        }
                        truck.pickup();
                    }   
                }
            }
        }
    }
    
    /**
     * Logs info about the Warehouse object in this format: 
     * "Hour,WarehouseID,PosX,PosY,LoadingDocks,TrucksQueueSize,TrucksInQueue,InventorySize,ShipmentsInInventory"
     * to a log file "WarehousesCSV.txt" csv file. 
     */
    @Override
    public void logStatus()
    {
        try {
            // Hour,WarehouseID,PosX,PosY,LoadingDocks,TrucksQueueSize,TrucksInQueue,InventorySize,ShipmentsInInventory
            Simulation.getWarehouseBuffer().append(Map.getCurrHour() + "," + id + "," + posX + "," + posY + "," + loadingDocks + ",");
            Simulation.getWarehouseBuffer().append(trucks.size() + "," + "[ ");
            QueueNode<Truck> node = trucks.getHead();
            while (node != null) {
                Simulation.getWarehouseBuffer().append(node.data.getId() + " ");
                node = node.next;
            }
            Simulation.getWarehouseBuffer().append("]," + inventory.size() + ",[ ");
            for (int i = 0; i < inventory.size(); i++) Simulation.getWarehouseBuffer().append(inventory.get(i).getId()+ " ");
            Simulation.getWarehouseBuffer().append("]");
            Simulation.getWarehouseBuffer().newLine();
        } catch (java.io.IOException e) {
            System.err.println("Failed to open/find the file WarehousesCSV.txt");
        }
    }

    /**
     * Returns id of the Warehouse object
     * 
     * @return  id of the Warehouse object
     */
    public int getId()
    {
        return id;
    }
    
    /**
     * Returns posX of the Warehouse object
     * 
     * @return  posX of the Warehouse object
     */
    public double getX()
    {
        return posX;
    }
    
    /**
     * Returns posY of the Warehouse object
     * 
     * @return  posY of the Warehouse object
     */
    public double getY()
    {
        return posY;
    }
    
    /**
     * Returns loadingDocks of the Warehouse object (for unit-testing)
     * 
     * @return  loadingDocks of the Warehouse object
     */
    public int getLoadingDocks()
    {
        return loadingDocks;
    }
    
    /**
     * Returns trucks's head of the Warehouse object (for unit-testing)
     * 
     * @return  trucks's head of the Warehouse object
     */
    public QueueNode getTrucksHead()
    {
        return trucks.getHead();
    }
    
    /**
     * Returns inventory of the Warehouse object (for unit-testing)
     * 
     * @return  inventory of the Warehouse object
     */
    public MyArrayList getInventory()
    {
        return inventory;
    }
    
    /**
     * Adds a shipment to the inventory list and updates shipment's location to this warehouse
     *
     * @param  shipment  shipment to be added to the inventory list
     */
    public void addToInventory(Shipment shipment)
    {
        // add, set, and sort
        inventory.add(shipment);
        shipment.setLocation(this);
        insertionSortOfInventory();
    }
    
    /**
     * Insertion sort of the inventory list with inventorySortedUntil which keeps the index 
     * of up to where the list is sorted.
     */
    private void insertionSortOfInventory()
    {
        // insertion sort by id
        int length = inventory.size();
        int j;
        
        // insertion Sort Algorithm 
        for (int i = inventorySortedUntil + 1; i < length; ++i) {
            Shipment temp = inventory.get(i);
            j = i;
            
            while (j > 0 && inventory.get(j - 1).getId() > temp.getId()) {
                inventory.set(j, inventory.get(j - 1));
                --j;
            }
            
            inventory.set(j, temp);
        }
        
        inventorySortedUntil = inventory.size() - 1;
    }
    
    /**
     * Removes a shipment from the inventory list and set shipment's location to null (truck).
     *
     * @param   shipment  shipment to be removed from the inventory list
     * @return  true if removed shipment
     */
    public boolean removeFromInventory(Shipment shipment)
    {
        if (inventory.size() == 0) return false;
        
        // initialize variables
        int high = inventory.size() - 1;
        int low  = 0;
        int mid;
        
        // binary search 
        while (high >= low) {
            mid  = (high + low) / 2;

            if (shipment.getId() > inventory.get(mid).getId()) {
                low = mid + 1;
            } else if (shipment.getId() < inventory.get(mid).getId()) {
                high = mid - 1;
            } else {
                inventory.remove(mid);
                shipment.setLocation(null);
                inventorySortedUntil--;
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Adds the truck that arrived to the warehouse to the trucks queue.
     *
     * @param  truck  the truck that arrived to the warehouse and to be added to the trucks queue
     */
    public void addTruckToQueue(Truck truck)
    {
        trucks.add(truck);
    }
    
    @Override
    public boolean equals(Object object) 
    {
        // compare using IDs
        if (object instanceof Warehouse) {
            Warehouse warehouse = (Warehouse) object;
            return this.id == warehouse.getId();
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
}