package test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import models.Map;
import simulation.Main;
import simulation.Simulation;

/**
 * The test class SimulationTest tests the Simulation class 
 * and its methods using various unit tests. 
 *
 * @author  Nuris Abdyldaev
 * @version 05/01/2026
 */
public class SimulationTest
{
    /**
     * Default constructor for test class SimulationTest
     */
    public SimulationTest()
    {
    }

    /**
     * Sets up the test fixture.
     *
     * Called before every test case method.
     */
    @BeforeEach
    public void setUp()
    {
    }

    /**
     * Tears down the test fixture.
     *
     * Called after every test case method.
     */
    @AfterEach
    public void tearDown()
    {
    }
    
    /**
     * Tests the constructor and initialize() of Simulation class for the correct
     *      initialization of simulation vars,
     *      initialization of files.
     */
    @Test
    @DisplayName("Test of constructor and initialize()")
    public void constructorAndInitializeTest() throws java.io.IOException {
        // declaration and initialization of test variables
        Main.configure(new File("config.txt"), 1, 1, 1, 2, 1);
        Simulation simulation = new Simulation(new File("config.txt"));
        simulation.closeBuffers();
        Scanner scTrucks = new Scanner(new File("TrucksCSV.txt"));
        Scanner scWarehouses = new Scanner(new File("WarehousesCSV.txt"));
        Scanner scShipments = new Scanner(new File("ShipmentsCSV.txt"));
        Scanner scFile = new Scanner(new File("config.txt"));
        String[] lineTrucks = {};
        String[] lineWarehouses = {};
        String[] lineShipments = {};
        String[] line = {};
        
        // assertion statements
        assertFalse(simulation.lastTruckDelivered);
        assertEquals("Hour,TruckID,PosX,PosY,LoadSize,Speed,Status,CurrLoad,SpaceInLoad,DestinationWarehouseID,PriorityManifestID,Manifest", scTrucks.nextLine());
        assertEquals("Hour,WarehouseID,PosX,PosY,LoadingDocks,TrucksQueueSize,TrucksInQueue,InventorySize,ShipmentsInInventory", scWarehouses.nextLine());
        assertEquals("Hour,ShipmentID,Size,SourceID,DestinationID,CurrentWarehouse,Status", scShipments.nextLine());
        
        // compare with config.txt
        line = scFile.nextLine().split(",");
        assertEquals(Integer.parseInt(line[0]), Map.getMapX());
        assertEquals(Integer.parseInt(line[1]), Map.getMapY());
        line = scFile.nextLine().split(",");
        assertEquals(Integer.parseInt(line[0]), simulation.warehouses);
        assertEquals(Integer.parseInt(line[1]), simulation.shipments);
        assertEquals(Integer.parseInt(line[2]), simulation.trucks);
        
        // check warehouses config
        lineWarehouses = scWarehouses.nextLine().split(",");
        line = scFile.nextLine().split(",");
        assertEquals(0, Integer.parseInt(lineWarehouses[0]));
        assertEquals(1, Integer.parseInt(lineWarehouses[1]));
        assertEquals(line[1], lineWarehouses[2]);
        assertEquals(line[2], lineWarehouses[3]);
        assertEquals(line[3], lineWarehouses[4]);
        assertEquals(0, Integer.parseInt(lineWarehouses[5]));
        assertEquals("[ ]", lineWarehouses[6]);
        assertTrue(0 == Integer.parseInt(lineWarehouses[7]) || 1 == Integer.parseInt(lineWarehouses[7]));
        assertTrue(lineWarehouses[8].equals("[ ]") || lineWarehouses[8].equals("[ 1 ]"));
        lineWarehouses = scWarehouses.nextLine().split(",");
        line = scFile.nextLine().split(",");
        assertEquals(0, Integer.parseInt(lineWarehouses[0]));
        assertEquals(2, Integer.parseInt(lineWarehouses[1]));
        assertEquals(line[1], lineWarehouses[2]);
        assertEquals(line[2], lineWarehouses[3]);
        assertEquals(line[3], lineWarehouses[4]);
        assertEquals(0, Integer.parseInt(lineWarehouses[5]));
        assertEquals("[ ]", lineWarehouses[6]);
        assertTrue(0 == Integer.parseInt(lineWarehouses[7]) || 1 == Integer.parseInt(lineWarehouses[7]));
        assertTrue(lineWarehouses[8].equals("[ ]") || lineWarehouses[8].equals("[ 1 ]"));
        
        
        // check shipment config
        lineShipments = scShipments.nextLine().split(",");
        line = scFile.nextLine().split(",");
        assertEquals(0, Integer.parseInt(lineShipments[0]));
        assertEquals(1, Integer.parseInt(lineShipments[1]));
        assertEquals(line[3], lineShipments[2]);
        assertEquals(line[1], lineShipments[3]);
        assertEquals(line[2], lineShipments[4]);
        assertEquals("Source Warehouse", lineShipments[5]);
        assertEquals("Awaiting pick up", lineShipments[6]);
        
        
        // check truck config
        lineTrucks = scTrucks.nextLine().split(",");
        line = scFile.nextLine().split(",");
        assertEquals(0, Integer.parseInt(lineTrucks[0]));
        assertEquals(1, Integer.parseInt(lineTrucks[1]));
        assertEquals(line[1], lineTrucks[2]);
        assertEquals(line[2], lineTrucks[3]);
        assertEquals(line[3], lineTrucks[4]);
        assertEquals(6 - Integer.parseInt(line[3]), Integer.parseInt(lineTrucks[5]));
        assertEquals("Driving to pick up", lineTrucks[6]);
        assertEquals("[ ]", lineTrucks[7]);
        assertEquals(Integer.parseInt(line[3]), Integer.parseInt(lineTrucks[8]));
        assertEquals(lineShipments[3], lineTrucks[9]);
        assertEquals("1", lineTrucks[10]);
        assertEquals("[ 1 ]", lineTrucks[11]); 
        
        
        scTrucks.close();
        scWarehouses.close();
        scShipments.close();
        Map.reset();
        BufferedWriter buffer = new BufferedWriter(new FileWriter("WarehousesCSV.txt", false));
        buffer = new BufferedWriter(new FileWriter("TrucksCSV.txt", false));
        buffer = new BufferedWriter(new FileWriter("ShipmentsCSV.txt", false));
        buffer = new BufferedWriter(new FileWriter("config.txt", false));
        buffer.close();
    }
    
    /**
     * Tests the simulate() method of Simulation class for the correct
     *      lastTruckDelivered == True.
     */
    @Test
    @DisplayName("Test of simulate()")
    public void simulateTest() throws java.io.IOException {
        // declaration and initialization of test variables
        Main.configure(new File("config.txt"), 1, 1, 1, 2, 1);
        Simulation simulation = new Simulation(new File("config.txt"));
        simulation.simulate();
        
        assertTrue(simulation.lastTruckDelivered);
        
        Map.reset();
        BufferedWriter buffer = new BufferedWriter(new FileWriter("WarehousesCSV.txt", false));
        buffer = new BufferedWriter(new FileWriter("TrucksCSV.txt", false));
        buffer = new BufferedWriter(new FileWriter("ShipmentsCSV.txt", false));
        buffer = new BufferedWriter(new FileWriter("config.txt", false));
        buffer.close();
    }
}