package simulation;

import java.io.File;
import java.util.Scanner;
import datastructures.MyArrayList;
import models.Map;
import models.Truck;
import models.Warehouse;
import models.Shipment;
import java.io.FileNotFoundException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Simulation class is used to initialize and run the simulation. Contains variables
 * to store the numbers of trucks, warehouses, and shipments in the simulation, and
 * stores all objects in appropriate lists. Also contains static BufferedWriter 
 * objects for each of the log files that are to be accessed through getters.
 * 
 * Its constructor initializes the log files and then initializes all simulation 
 * variables and objects. Method simulate() runs cycles of the simulations until the
 * last truck makes its last delivery. During each cycle, it calls each object's 
 * action() and/or logStatus() methods starting from warehouses, then trucks, and 
 * then shipments. While processing Truck objects, checks if all of them are done 
 * and ends the simulation if yes.
 * 
 * Simulation is called from an instance of this class.
 *
 * @author Nuris Abdyldaev
 * @version 05/02/2026
 */
public class Simulation
{
    // instance variables - made first four public for unit-tests
    public boolean lastTruckDelivered;
    public int warehouses;
    public int shipments;
    public int trucks;
    private MyArrayList<Truck> trucksArrayList;
    private MyArrayList<Warehouse> warehousesArrayList;
    private MyArrayList<Shipment> shipmentsArrayList;
    private static BufferedWriter truckBuffer;
    private static BufferedWriter warehouseBuffer;
    private static BufferedWriter shipmentBuffer;
    
    // runtime check vars
    private double trucksMs;
    private double warehousesMs;
    private double shipmentsMs;
    
    /**
     * Constructor for objects of class Simulation.
     * 
     * @param  file  config file which contains the full configuration for the file
     */
    public Simulation(File file)
    {
        // clear and intitialize files
        try {
            // Truck file
            truckBuffer = new BufferedWriter(new FileWriter("TrucksCSV.txt", false));
            truckBuffer.write("Hour,TruckID,PosX,PosY,LoadSize,Speed,Status,CurrLoad,SpaceInLoad,DestinationWarehouseID,PriorityManifestID,Manifest");
            truckBuffer.newLine();
            
            // Warehouse file
            warehouseBuffer = new BufferedWriter(new FileWriter("WarehousesCSV.txt", false));
            warehouseBuffer.write("Hour,WarehouseID,PosX,PosY,LoadingDocks,TrucksQueueSize,TrucksInQueue,InventorySize,ShipmentsInInventory");
            warehouseBuffer.newLine();
            
            // Shipment file
            shipmentBuffer = new BufferedWriter(new FileWriter("ShipmentsCSV.txt", false));
            shipmentBuffer.write("Hour,ShipmentID,Size,SourceID,DestinationID,CurrentWarehouse,Status");
            shipmentBuffer.newLine();
        } catch (IOException e) {
            System.err.println("Failed to open/find the log files");
        }
        
        // initialise instance variables
        initialize(file);
        lastTruckDelivered = false;
        
        // runtime check vars
    }

    /**
     * Runs the simulation until the last truck makes its delivery
     */
    public void simulate()
    {
        // simulate until the lastTruckDelivered is true
        while (!lastTruckDelivered) {
            nextCycle();
        }
        
        System.out.println("Simulation is done!");
        System.out.println("");
        System.out.println("Trucks = " + trucks + "  |  Warehouses = " + warehouses + "  |  Shipments = " + shipments);
        System.out.println("Hours passed in the simulation: " + Map.getCurrHour());
        System.out.println("Truck Runtime: " + trucksMs + "ms" + "  |  Warehouse Runtime: " + warehousesMs + "ms" + "  |  Shipment Runtime: " + shipmentsMs + "ms");
        
        try
        {
            truckBuffer.close();
            warehouseBuffer.close();
            shipmentBuffer.close();
        }
        catch (IOException ioe)
        {
            System.err.println("Failed to close the file");
        }
        Map.reset();
    }

    /**
     * Runs the next cycle where each object is processed starting from warehouses
     */
    private void nextCycle() 
    {
        // runtime check vars
        long startTime;
        long endTime;
        
        // call each object's action() and/or logStatus() methods starting from warehouses
        Map.clockIncrement();
        
        startTime = System.nanoTime();
        warehousesActionLogStatus();
        endTime = System.nanoTime();
        warehousesMs += (endTime - startTime) / 1000000; 
        
        startTime = System.nanoTime();
        trucksActionLogStatus();
        endTime = System.nanoTime();
        trucksMs += (endTime - startTime) / 1000000; 
        
        startTime = System.nanoTime();
        shipmentsLogStatus();
        endTime = System.nanoTime();
        shipmentsMs += (endTime - startTime) / 1000000; 
    }
    
    /**
     * Helper to loop through the array and call each warehouse's action() and logStatus() 
     * methods.
     */
    private void warehousesActionLogStatus() 
    {
        // loop and call action() & logStatus()
        for (int i = 0; i < warehouses; i++) {
            warehousesArrayList.get(i).action();
            warehousesArrayList.get(i).logStatus();
        }
    }
    
    /**
     * Helper to loop through the array and call each truck's action() and logStatus() 
     * methods. Additionally, checks if the last truck has made it's delivery.
     */
    private void trucksActionLogStatus() 
    {
        // loop and call action() & logStatus() + check if all are done
        lastTruckDelivered = true;
        for (int i = 0; i < trucks; i++) {
            trucksArrayList.get(i).action();
            trucksArrayList.get(i).logStatus();
            if (!trucksArrayList.get(i).getStatus().equals("Done")) lastTruckDelivered = false;
        }
    }
    
    /**
     * Helper to loop through the array and call each shipment's logStatus() method. 
     */
    private void shipmentsLogStatus() 
    {
        // loop and call logStatus()
        for (int i = 0; i < shipments; i++) {
            shipmentsArrayList.get(i).logStatus();
        }
    }

    /**
     * Creates all objects and initializes the simulation. 
     *
     * @param  file  config file that contains all the configurations for the simulation
     */
    private void initialize(File file) 
    {
        // read file
        try {
            Scanner scFile = new Scanner(file);
            String[] line = {};
            
            // initialize map
            line = scFile.nextLine().split(",");
            Map map = new Map(Integer.parseInt(line[0]), Integer.parseInt(line[1]));
            
            // initialize arraylists
            line = scFile.nextLine().split(",");
            warehouses = Integer.parseInt(line[0]);
            shipments  = Integer.parseInt(line[1]);
            trucks     = Integer.parseInt(line[2]);
            warehousesArrayList = new MyArrayList<Warehouse>(warehouses);
            shipmentsArrayList  = new MyArrayList<Shipment>(shipments);
            trucksArrayList     = new MyArrayList<Truck>(trucks); 
            
            // create, configure objects, and log their initial status
            for (int i = 0; i < warehouses; i++) {
                line = scFile.nextLine().split(",");
                if (!line[0].equals("Warehouse")) System.err.println("Error in the file: number of warehouses is wrong");
                Warehouse newWarehouse = new Warehouse(Double.parseDouble(line[1]), Double.parseDouble(line[2]), Integer.parseInt(line[3]));
                warehousesArrayList.add(newWarehouse);
            }
            
            for (int i = 0; i < shipments; i++) {
                line = scFile.nextLine().split(",");
                if (!line[0].equals("Shipment")) System.err.println("Error in the file: number of shipments is wrong");
                Shipment newShipment = new Shipment(warehousesArrayList.get(Integer.parseInt(line[1]) - 1), warehousesArrayList.get(Integer.parseInt(line[2]) - 1), Integer.parseInt(line[3]));
                shipmentsArrayList.add(newShipment);
                
                // add shipments to their source warehouses
                warehousesArrayList.get(Integer.parseInt(line[1]) - 1).addToInventory(newShipment);
                
                newShipment.logStatus();
            }
            
            for (int i = 0; i < warehouses; i++) {
                // log warehouse status after adding shipments
                warehousesArrayList.get(i).logStatus();
            }
            
            for (int i = 0; i < trucks; i++) {
                line = scFile.nextLine().split(",");
                if (!line[0].equals("Truck")) System.err.println("Error in the file: number of trucks is wrong");
                Truck newTruck = new Truck(Double.parseDouble(line[1]), Double.parseDouble(line[2]), Integer.parseInt(line[3]));
                trucksArrayList.add(newTruck);
                
                // add shipments to truck's manifest
                for (int j = 4; j < line.length; j++) {
                    if(!newTruck.addManifest(shipmentsArrayList.get(Integer.parseInt(line[j]) - 1))) {
                        System.err.println("Error in the file: shipment of size 3 can't be added to a truck's manifest whose load size is less than 3 (2)");
                    }
                }
                newTruck.logStatus();
            }
            
            
            scFile.close();
        } catch (FileNotFoundException e) {
            System.err.println("Failed to open/find the file");
        }
    }
    
    /**
     * Returns the truckBuffer
     *
     * @return    truckBuffer - BufferedWriter for truck objects to log their status to a log file
     */
    public static BufferedWriter getTruckBuffer()
    {
        return truckBuffer;
    }
    
    /**
     * Returns the warehouseBuffer
     *
     * @return    warehouseBuffer - BufferedWriter for warehouse objects to log their status to a log file
     */
    public static BufferedWriter getWarehouseBuffer()
    {
        return warehouseBuffer;
    }
    
    /**
     * Returns the shipmentBuffer
     *
     * @return    shipmentBuffer - BufferedWriter for shipment objects to log their status to a log file
     */
    public static BufferedWriter getShipmentBuffer()
    {
        return shipmentBuffer;
    }
    
    /**
     * Initializes all the buffers with their appropriate files (for unit-tests of the Truck, Warehouse, Shipment classes)
     */
    public static void initializeBuffers() {
        try {
            // Truck file
            truckBuffer = new BufferedWriter(new FileWriter("TrucksCSV.txt", false));
            
            // Warehouse file
            warehouseBuffer = new BufferedWriter(new FileWriter("WarehousesCSV.txt", false));
            
            // Shipment file
            shipmentBuffer = new BufferedWriter(new FileWriter("ShipmentsCSV.txt", false));
        } catch (IOException e) {
            System.err.println("Failed to open/find the log files");
        }
    }
    
    /**
     * Closes all the buffers (for unit-tests of the Truck, Warehouse, Shipment classes)
     */
    public static void closeBuffers() {
        try {
            // close files
            truckBuffer.close();
            warehouseBuffer.close();
            shipmentBuffer.close();
        } catch (IOException e) {
            System.err.println("Failed to close the log files");
        }
    }
}