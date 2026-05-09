package test;


import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;
import java.util.Scanner;
import models.*;
import simulation.Simulation;

/**
 * The test class ShipmentTest tests the Shipment class 
 * and all of its methods using various unit tests. 
 *
 * @author  Nuris Abdyldaev
 * @version 04/26/2026
 */
public class ShipmentTest
{
    /**
     * Default constructor for test class ShipmentTest
     */
    public ShipmentTest()
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
     * Tests the constructor (and getters) of Shipment class for the correct
     *      initialization of the internal variables.
     */
    @Test
    @DisplayName("Test of constructor")
    public void shipmentTest() {
        // Declaration and initialization of test variables
        Warehouse source = new Warehouse(0, 0, 1);
        Warehouse destination = new Warehouse(0, 0, 1);
        Shipment shipment1 = new Shipment(source, destination, 1);
        Shipment shipment2 = new Shipment(source, destination, 1);
        
        // Assertion statements - check for correct initialization of vars
        assertEquals(source, shipment1.getSource()); 
        assertEquals(destination, shipment1.getDestination()); 
        assertEquals(source, shipment1.getLocation());         
        assertEquals(1, shipment1.getId());
        assertTrue(1 == shipment1.size());
        assertEquals(source, shipment2.getSource()); 
        assertEquals(destination, shipment2.getDestination()); 
        assertEquals(source, shipment2.getLocation());         
        assertEquals(2, shipment2.getId());
        assertTrue(1 == shipment2.size());
        
        assertThrows(IllegalArgumentException.class, () -> new Shipment(source, destination, 0));
        assertThrows(IllegalArgumentException.class, () -> new Shipment(source, destination, 4));
        assertThrows(IllegalArgumentException.class, () -> new Shipment(source, destination, -4));
        
        Map.reset();
    }
    
    /**
     * Tests the equals() method (and hashCode()) of Shipment class for the correct
     *      comparison of the Shipment objects.
     */
    @Test
    @DisplayName("Test of equals() method")
    public void equalsTest() {
        // Declaration and initialization of test variables
        Warehouse source = new Warehouse(0, 0, 1);
        Warehouse destination = new Warehouse(0, 0, 1);
        Shipment shipment1 = new Shipment(source, destination, 1);
        Shipment shipment2 = new Shipment(source, destination, 1);
        Shipment shipment3 = shipment1;
        
        // Assertion statements  - test various cases: comparison with same/different Shipments, other objects, nulls etc.
        assertEquals(shipment1.hashCode(), shipment3.hashCode());
        assertNotEquals(shipment1.hashCode(), shipment2.hashCode());
        assertTrue(shipment1.equals(shipment3));
        assertTrue(shipment3.equals(shipment1));
        assertFalse(shipment1.equals(shipment2));
        assertFalse(shipment2.equals(shipment1));
        assertFalse(shipment1.equals(source));
        assertFalse(shipment2.equals(destination));
        assertFalse(shipment1.equals(null));
        assertFalse(shipment2.equals(null));
        assertFalse(shipment3.equals(null));
        
        Map.reset();
    }
    
    /**
     * Tests the setLocation() and delivered() (and getters) of Shipment class for the correct
     *      set of internal location var,
     *      comparison of the internal vars.
     */
    @Test
    @DisplayName("Test of setLocation() and delivered()")
    public void setLocationAndDeliveredTest() {
        // Declaration and initialization of test variables
        Warehouse source = new Warehouse(0, 0, 1);
        Warehouse destination = new Warehouse(0, 0, 1);
        Warehouse warehouse = new Warehouse(0, 0, 1);
        Shipment shipment1 = new Shipment(source, destination, 1);
        
        // Assertion statements - make sure the values are correctly assigned
        assertEquals(source, shipment1.getLocation());
        assertFalse(shipment1.delivered());
        shipment1.setLocation(warehouse);
        assertEquals(warehouse, shipment1.getLocation());
        assertFalse(shipment1.delivered());
        shipment1.setLocation(null);
        assertEquals(null, shipment1.getLocation());
        assertFalse(shipment1.delivered());
        shipment1.setLocation(destination);
        assertEquals(destination, shipment1.getLocation());
        assertTrue(shipment1.delivered());
        
        Map.reset();
    }
    
    /**
     * Tests the logStatus() of Shipment class for the correct
     *      log of object's info.
     */
    @Test
    @DisplayName("Test of logStatus()")
    public void logStatusTest() throws java.io.IOException {
        // Declaration and initialization of test variables\
        Simulation.initializeBuffers();
        Warehouse source = new Warehouse(0, 0, 1);
        Warehouse w1 = new Warehouse(0, 0, 1);
        Warehouse w2 = new Warehouse(0, 0, 1);
        Warehouse w3 = new Warehouse(0, 0, 1);
        Shipment shipment1 = new Shipment(w1, w2, 1);
        Shipment shipment2 = new Shipment(w2, w3, 1);
        Shipment shipment3 = new Shipment(w3, w3, 1);
        String[] line = {};
        shipment2.setLocation(null);
        
        shipment1.logStatus();
        shipment2.logStatus();
        shipment3.logStatus();
        Simulation.closeBuffers();
        
        Scanner scFile = new Scanner(new File("ShipmentsCSV.txt"));
        
        // Assertion statements - make sure the data written is correct and in a correct format
        line = scFile.nextLine().split(",");
        assertEquals(Map.getCurrHour(), Integer.parseInt(line[0]));
        assertEquals(shipment1.getId(), Integer.parseInt(line[1]));
        assertEquals(1, Integer.parseInt(line[2]));
        assertEquals(w1.getId(), Integer.parseInt(line[3]));
        assertEquals(w2.getId(), Integer.parseInt(line[4]));
        assertEquals("Source Warehouse", line[5]);
        assertEquals("Awaiting pick up", line[6]);
        
        line = scFile.nextLine().split(",");
        assertEquals(Map.getCurrHour(), Integer.parseInt(line[0]));
        assertEquals(shipment2.getId(), Integer.parseInt(line[1]));
        assertEquals(1, Integer.parseInt(line[2]));
        assertEquals(w2.getId(), Integer.parseInt(line[3]));
        assertEquals(w3.getId(), Integer.parseInt(line[4]));
        assertEquals("Truck", line[5]);
        assertEquals("Picked up", line[6]);
        
        line = scFile.nextLine().split(",");
        assertEquals(Map.getCurrHour(), Integer.parseInt(line[0]));
        assertEquals(shipment3.getId(), Integer.parseInt(line[1]));
        assertEquals(1, Integer.parseInt(line[2]));
        assertEquals(w3.getId(), Integer.parseInt(line[3]));
        assertEquals(w3.getId(), Integer.parseInt(line[4]));
        assertEquals("Destination Warehouse", line[5]);
        assertEquals("Delivered", line[6]);
        
        BufferedWriter buffer = new BufferedWriter(new FileWriter("ShipmentsCSV.txt", false));
        
        Map.reset();
        buffer.close();
        scFile.close();
    }
    
}