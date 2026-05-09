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
 * The test class TruckTest tests the Truck class 
 * and all of its methods using various unit tests. 
 *
 * @author  Nuris Abdyldaev
 * @version 04/29/2026
 */
public class TruckTest
{
    /**
     * Default constructor for test class TruckTest
     */
    public TruckTest()
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
     * Tests the constructor (+ getters, spaceInLoad(), peekLoad() & finished() methods) of Truck class for the correct
     *      initialization of the internal variables.
     */
    @Test
    @DisplayName("Test of constructor")
    public void constructorTest() {
        // Declaration and initialization of test variables
        Truck t1 = new Truck(0, 0, 2);
        Truck t2 = new Truck(0, 0, 5);
        
        // Assertion statements - check for correct initialization of vars
        assertEquals(1, t1.getId()); 
        assertTrue(0 == t1.getX());
        assertTrue(0 == t1.getY());
        assertTrue(2 == t1.getLoadSize());
        assertTrue(2 == t1.spaceInLoad());
        assertTrue(t1.finished());
        assertNull(t1.getCurrDestination());
        assertNull(t1.getPriorityManifest());
        assertNull(t1.peekLoad());
        assertEquals("Done", t1.getStatus()); 
        assertEquals(2, t2.getId()); 
        assertTrue(0 == t2.getX());
        assertTrue(0 == t2.getY());
        assertTrue(5 == t2.getLoadSize());
        assertTrue(5 == t2.spaceInLoad());
        assertTrue(t2.finished());
        assertNull(t2.getCurrDestination());
        assertNull(t2.getPriorityManifest());
        assertNull(t2.peekLoad());
        assertEquals("Done", t2.getStatus()); 
        
        assertThrows(IllegalArgumentException.class, () -> new Truck(-1, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> new Truck(100, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> new Truck(0, -1, 1));
        assertThrows(IllegalArgumentException.class, () -> new Truck(0, 100, 1));
        assertThrows(IllegalArgumentException.class, () -> new Truck(0, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new Truck(0, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> new Truck(0, 0, -4));
        
        Map.reset();
    }
    
    /**
     * Tests the action() of Truck class for the correct
     *      actions to be done by the Truck object.
     */
    @Test
    @DisplayName("Test of action()")
    public void actionTest() {
        // Declaration and initialization of test variables
        Map map = new Map(10,10);
        Truck t1 = new Truck(0, 0, 2);
        Truck t2 = new Truck(0, 0, 3);
        Warehouse w1 = new Warehouse(0, 0, 3);
        Warehouse w2 = new Warehouse(3, 4, 3);
        Warehouse w3 = new Warehouse(6, 8, 3);
        Shipment s1  = new Shipment(w2, w1, 3);
        Shipment s3  = new Shipment(w1, w3, 2);
        Shipment s5  = new Shipment(w2, w1, 1);
        Shipment s6  = new Shipment(w2, w1, 2);
        Shipment s7  = new Shipment(w3, w1, 1);
        w1.addToInventory(s3);
        w2.addToInventory(s1);
        w2.addToInventory(s5);
        w2.addToInventory(s6);
        w3.addToInventory(s7);
        
        // if truck is done - changes status to "Done"
        assertEquals("Done", t1.getStatus());
        t1.action();
        assertEquals("Done", t1.getStatus()); 
        assertEquals("Done", t2.getStatus());
        t2.action();
        assertEquals("Done", t2.getStatus()); 
        
        t2.addManifest(s6);
        t2.addManifest(s7);
        t2.addManifest(s5);
        t2.addManifest(s3);
        
        // if reached destination - changes status to "Waiting for the available loading dock"
        t2.action();
        assertEquals("Waiting for the available loading dock", t2.getStatus());
        
        // if pickup() was called - changes status to "Being processed at loading dock"
        t2.pickup();
        t2.action();
        assertEquals("Being processed at loading dock", t2.getStatus());
        
        // if unload() was called - changes status to "Being processed at loading dock"
        t2.unload();
        t2.action();
        assertEquals("Being processed at loading dock", t2.getStatus());
        
        // if none from above - move towards currDestination
        t2.action();
        assertEquals("Driving to pick up", t2.getStatus());
        assertEquals(0.6 * 3, t2.getX());
        assertEquals(0.6 * 4, t2.getY());
        t2.action();
        assertEquals("Driving to pick up", t2.getStatus());
        assertEquals(4, t2.getY());
        assertEquals(3, t2.getX());
        
        // additional tests - wait; move in a diff direction
        w2.action();
        t2.action();
        assertEquals("Being processed at loading dock", t2.getStatus());
        assertEquals(3, t2.getX());
        assertEquals(4, t2.getY());
        assertEquals(s6, t2.peekLoad());  
        w2.action();
        t2.action();
        assertEquals("Being processed at loading dock", t2.getStatus());
        assertEquals(3, t2.getX()); 
        assertEquals(4, t2.getY());
        assertEquals(s5, t2.peekLoad());
        t2.action();
        assertEquals("Driving to deliver", t2.getStatus());
        assertEquals(3 - 0.6 * 3, t2.getX());
        assertEquals(4 - 0.6 * 4, t2.getY());
        t2.action();
        assertEquals("Driving to deliver", t2.getStatus());
        assertEquals(0, t2.getX());
        assertEquals(0, t2.getY());
        w1.action();
        t2.action();
        assertEquals("Being processed at loading dock", t2.getStatus());
        assertEquals(0, t2.getX());
        assertEquals(0, t2.getY());
        t2.action();
        assertEquals("Driving to pick up", t2.getStatus());
        assertEquals(0 + 0.3 * 6, t2.getX());
        assertEquals(0 + 0.3 * 8, t2.getY()); 
        t2.action();
        assertEquals("Driving to pick up", t2.getStatus());
        assertEquals(2 * 0.3 * 6, t2.getX());
        assertEquals(2 * 0.3 * 8, t2.getY()); 
        t2.action();
        assertEquals("Driving to pick up", t2.getStatus());
        assertEquals(5.4, t2.getX());
        assertEquals(7.2, t2.getY()); 
        t2.action();
        assertEquals("Driving to pick up", t2.getStatus());
        assertEquals(6, t2.getX());
        assertEquals(8, t2.getY()); 
        w3.action();
        t2.action();
        assertEquals("Being processed at loading dock", t2.getStatus());
        assertEquals(6, t2.getX());
        assertEquals(8, t2.getY());
        t2.action();
        assertEquals("Driving to deliver", t2.getStatus());
        assertEquals(6 - 0.3 * 6, t2.getX());
        assertEquals(8 - 0.3 * 8, t2.getY()); 
        t2.action();
        assertEquals("Driving to deliver", t2.getStatus());
        assertEquals(6 - 2 * 0.3 * 6, t2.getX());
        assertEquals(3.1999999999999997, t2.getY()); 
        t2.action();
        assertEquals("Driving to deliver", t2.getStatus());
        assertEquals(0.6000000000000001, t2.getX());
        assertEquals(0.7999999999999998, t2.getY()); 
        t2.action();
        assertEquals("Driving to deliver", t2.getStatus());
        assertEquals(0, t2.getX());
        assertEquals(0, t2.getY());
        w1.action();
        t2.action();
        assertEquals("Being processed at loading dock", t2.getStatus());
        assertEquals(0, t2.getX());
        assertEquals(0, t2.getY());
        w1.action();
        t2.action();
        assertEquals("Being processed at loading dock", t2.getStatus());
        assertEquals(0, t2.getX());
        assertEquals(0, t2.getY());
        t2.action();
        assertEquals("Done", t2.getStatus()); 
        assertEquals(null, t2.getPriorityManifest()); 
        assertEquals(null, t2.getCurrDestination());
        assertTrue(t1.finished());
        assertEquals(3, t2.spaceInLoad());
        
        Map.reset();
    }
    
    /**
     * Tests the logStatus() of Truck class for the correct
     *      log of object's info.
     */
    @Test
    @DisplayName("Test of logStatus() and delivered()")
    public void logStatusTest() throws java.io.IOException {
        // Declaration and initialization of test variables\
        Simulation.initializeBuffers();
        Map map = new Map(10,10);
        String[] line = {};
        Truck t1 = new Truck(0, 0, 2);
        Truck t2 = new Truck(0, 0, 3);
        Warehouse w1 = new Warehouse(0, 0, 3);
        Warehouse w2 = new Warehouse(3, 4, 3);
        Warehouse w3 = new Warehouse(6, 8, 3);
        Shipment s1  = new Shipment(w2, w1, 3);
        Shipment s2  = new Shipment(w1, w3, 2);
        Shipment s3  = new Shipment(w2, w1, 1);
        Shipment s4  = new Shipment(w2, w1, 2);
        Shipment s5  = new Shipment(w3, w1, 1);
        
        t1.action();
        t1.logStatus();
        
        t2.action();
        t2.logStatus();
        
        t2.addManifest(s4);
        t2.addManifest(s5);
        t2.addManifest(s3);
        t2.addManifest(s2);
        
        t2.action();
        t2.logStatus();
        Simulation.closeBuffers();
        
        Scanner scFile = new Scanner(new File("TrucksCSV.txt"));
        
        // Assertion statements - make sure the data written is correct and in a correct format
        line = scFile.nextLine().split(",");
        assertEquals(Map.getCurrHour(), Integer.parseInt(line[0]));
        assertEquals(t1.getId(), Integer.parseInt(line[1]));
        assertEquals(t1.getX(), Double.parseDouble(line[2]));
        assertEquals(t1.getY(), Double.parseDouble(line[3]));
        assertEquals(t1.getLoadSize(), Integer.parseInt(line[4]));
        assertEquals(6 - t1.getLoadSize(), Integer.parseInt(line[5]));
        assertEquals("Done", line[6]);
        assertEquals("[ ]", line[7]);
        assertEquals(2, Integer.parseInt(line[8]));
        assertEquals("null", line[9]);
        assertEquals("null", line[10]);
        assertEquals("[ ]", line[11]);
        
        line = scFile.nextLine().split(",");
        assertEquals(Map.getCurrHour(), Integer.parseInt(line[0]));
        assertEquals(t2.getId(), Integer.parseInt(line[1]));
        assertEquals(t2.getX(), Double.parseDouble(line[2]));
        assertEquals(t2.getY(), Double.parseDouble(line[3]));
        assertEquals(t2.getLoadSize(), Integer.parseInt(line[4]));
        assertEquals(6 - t2.getLoadSize(), Integer.parseInt(line[5]));
        assertEquals("Done", line[6]);
        assertEquals("[ ]", line[7]);
        assertEquals(3, Integer.parseInt(line[8]));
        assertEquals("null", line[9]);
        assertEquals("null", line[10]);
        assertEquals("[ ]", line[11]);
        
        line = scFile.nextLine().split(",");
        assertEquals(Map.getCurrHour(), Integer.parseInt(line[0]));
        assertEquals(t2.getId(), Integer.parseInt(line[1]));
        assertEquals(t2.getX(), Double.parseDouble(line[2]));
        assertEquals(t2.getY(), Double.parseDouble(line[3]));
        assertEquals(t2.getLoadSize(), Integer.parseInt(line[4]));
        assertEquals(6 - t2.getLoadSize(), Integer.parseInt(line[5]));
        assertEquals("Waiting for the available loading dock", line[6]);
        assertEquals("[ ]", line[7]);
        assertEquals(3, Integer.parseInt(line[8]));
        assertEquals("1", line[9]);
        assertEquals("2", line[10]);
        assertEquals("[ 5 3 4 2 ]", line[11]);
        

        BufferedWriter buffer = new BufferedWriter(new FileWriter("TrucksCSV.txt", false));
        Map.reset();
        buffer.close();
        scFile.close();
    }
    
    /**
     * Tests the addManifest() (and internal updateDestination(), getDistance(), compareDistances(), & insertionSortOfManifest()) of Truck class for the correct
     *      return of false if the shipment's size is greater than the load size of the Truck,
     *      addition of the shipments to the manifest,
     *      maintenance of the order of the shipments in the manifest according to the distance and their id,
     *      update of currDestination (and other vars like status) based on the distances to and ids of manifests and space in load.
     */
    @Test
    @DisplayName("Test of addManifest()")
    public void addManifestTest() {
        // Declaration and initialization of test variables
        Map map = new Map(10,10);
        Truck t1 = new Truck(0, 0, 2);
        Truck t2 = new Truck(0, 0, 3);
        Warehouse w1 = new Warehouse(0, 0, 3);
        Warehouse w2 = new Warehouse(3, 4, 3);
        Warehouse w3 = new Warehouse(6, 8, 3);
        Shipment s1  = new Shipment(w2, w1, 3);
        Shipment s3  = new Shipment(w1, w3, 2);
        Shipment s5  = new Shipment(w2, w1, 1);
        Shipment s6  = new Shipment(w2, w1, 2);
        Shipment s7  = new Shipment(w3, w1, 1);
        
        // can't add shipment with size 3 to a truck with load size of 2
        assertFalse(t1.addManifest(s1)); 
        assertEquals(2, t1.spaceInLoad());
        
        // check that the currDestination is updated correctly
        // correct priority (distance then ids)
        assertTrue(t2.addManifest(s6));
        assertEquals(s6.getSource(), t2.getCurrDestination());
        assertEquals(s6, t2.getPriorityManifest());    
        assertEquals("Driving to pick up", t2.getStatus());       
        assertEquals(3, t2.spaceInLoad());      
        assertTrue(t2.addManifest(s7));
        assertEquals(s6.getSource(), t2.getCurrDestination());
        assertEquals(s6, t2.getPriorityManifest());    
        assertEquals("Driving to pick up", t2.getStatus());   
        assertEquals(3, t2.spaceInLoad());
        assertTrue(t2.addManifest(s5));
        assertEquals(s6.getSource(), t2.getCurrDestination());
        assertEquals(s6, t2.getPriorityManifest());    
        assertEquals("Driving to pick up", t2.getStatus());   
        assertEquals(3, t2.spaceInLoad());
        assertTrue(t2.addManifest(s3));
        assertEquals(s3.getSource(), t2.getCurrDestination());
        assertEquals(s3, t2.getPriorityManifest());    
        assertEquals("Driving to pick up", t2.getStatus());  
        assertEquals(3, t2.spaceInLoad());
        // load first, when load is full/not enough space in load for the manifest
        t2.pickup();
        assertEquals(s3.getDestination(), t2.getCurrDestination());    
        assertEquals(s6, t2.getPriorityManifest());    
        assertEquals("Driving to deliver", t2.getStatus());  
        assertEquals(1, t2.spaceInLoad());
        
        // additional -> no manifest = deliver load
        t2.unload();
        assertEquals(s6, t2.getPriorityManifest()); 
        assertEquals(s6.getSource(), t2.getCurrDestination());
        assertEquals("Driving to pick up", t2.getStatus()); 
        assertEquals(3, t2.spaceInLoad());
        t2.pickup();
        assertEquals(s5, t2.getPriorityManifest()); 
        assertEquals(s5.getSource(), t2.getCurrDestination());
        assertEquals("Driving to pick up", t2.getStatus()); 
        assertEquals(1, t2.spaceInLoad());
        t2.pickup();
        assertEquals(s7, t2.getPriorityManifest()); 
        assertEquals(s5.getDestination(), t2.getCurrDestination());
        assertEquals("Driving to deliver", t2.getStatus()); 
        assertEquals(0, t2.spaceInLoad());
        t2.unload();
        assertEquals(s7, t2.getPriorityManifest()); 
        assertEquals(s7.getSource(), t2.getCurrDestination());
        assertEquals("Driving to pick up", t2.getStatus()); 
        assertEquals(1, t2.spaceInLoad());
        t2.pickup();
        assertEquals(null, t2.getPriorityManifest()); 
        assertEquals(s7.getDestination(), t2.getCurrDestination());
        assertEquals("Driving to deliver", t2.getStatus()); 
        assertEquals(0, t2.spaceInLoad());
        t2.unload();
        assertEquals(null, t2.getPriorityManifest()); 
        assertEquals(s6.getDestination(), t2.getCurrDestination());
        assertEquals("Driving to deliver", t2.getStatus()); 
        assertEquals(1, t2.spaceInLoad());
        t2.unload();
        assertEquals("Done", t2.getStatus()); 
        assertEquals(null, t2.getPriorityManifest()); 
        assertEquals(null, t2.getCurrDestination());
        assertTrue(t1.finished());
        assertEquals(3, t2.spaceInLoad());
        
        Map.reset();
    }
    
    /**
     * Tests the unload(), pickup(), peekLoad() of Truck class for the correct
     *      return of load.peek(),
     *      update of load and inLoad vars,
     *      and etc.
     */
    @Test
    @DisplayName("Test of unload(), pickup(), and others")
    public void otherMethodsTest() {
        // Declaration and initialization of test variables
        Map map = new Map(10,10);
        Truck t1 = new Truck(0, 0, 2);
        Truck t2 = new Truck(0, 0, 3);
        Warehouse w1 = new Warehouse(0, 0, 3);
        Warehouse w2 = new Warehouse(3, 4, 3);
        Warehouse w3 = new Warehouse(6, 8, 3);
        Shipment s1  = new Shipment(w2, w1, 3);
        Shipment s3  = new Shipment(w1, w3, 2);
        Shipment s5  = new Shipment(w2, w1, 1);
        Shipment s6  = new Shipment(w2, w1, 2);
        Shipment s7  = new Shipment(w3, w1, 1);
        
        // can't add shipment with size 3 to a truck with load size of 2
        assertFalse(t1.addManifest(s1)); 
        assertEquals(2, t1.spaceInLoad());
        
        // make sure the inLoad var is updated
        assertTrue(t2.addManifest(s6));
        assertEquals(s6.getSource(), t2.getCurrDestination());
        assertEquals(s6, t2.getPriorityManifest());    
        assertEquals("Driving to pick up", t2.getStatus());       
        assertEquals(null, t2.peekLoad());    
        assertEquals(3, t2.spaceInLoad());      
        assertTrue(t2.addManifest(s7));
        assertEquals(s6.getSource(), t2.getCurrDestination());
        assertEquals(s6, t2.getPriorityManifest());    
        assertEquals("Driving to pick up", t2.getStatus());      
        assertEquals(null, t2.peekLoad());     
        assertEquals(3, t2.spaceInLoad());
        assertTrue(t2.addManifest(s5));
        assertEquals(s6.getSource(), t2.getCurrDestination());
        assertEquals(s6, t2.getPriorityManifest());    
        assertEquals("Driving to pick up", t2.getStatus());       
        assertEquals(null, t2.peekLoad());    
        assertEquals(3, t2.spaceInLoad());
        assertTrue(t2.addManifest(s3));
        assertEquals(s3.getSource(), t2.getCurrDestination());
        assertEquals(s3, t2.getPriorityManifest());    
        assertEquals("Driving to pick up", t2.getStatus());      
        assertEquals(null, t2.peekLoad());    
        assertEquals(3, t2.spaceInLoad());
        // load first, when load is full/not enough space in load for the manifest
        t2.pickup();
        assertEquals(s3.getDestination(), t2.getCurrDestination());    
        assertEquals(s6, t2.getPriorityManifest());    
        assertEquals("Driving to deliver", t2.getStatus());      
        assertEquals(s3, t2.peekLoad());    
        assertEquals(1, t2.spaceInLoad());
        
        // additional -> no manifest = deliver load
        t2.unload();
        assertEquals(s6, t2.getPriorityManifest()); 
        assertEquals(s6.getSource(), t2.getCurrDestination());
        assertEquals("Driving to pick up", t2.getStatus());     
        assertEquals(null, t2.peekLoad());    
        assertEquals(3, t2.spaceInLoad());
        t2.pickup();
        assertEquals(s5, t2.getPriorityManifest()); 
        assertEquals(s5.getSource(), t2.getCurrDestination());
        assertEquals("Driving to pick up", t2.getStatus());     
        assertEquals(s6, t2.peekLoad());    
        assertEquals(1, t2.spaceInLoad());
        t2.pickup();
        assertEquals(s7, t2.getPriorityManifest()); 
        assertEquals(s5.getDestination(), t2.getCurrDestination());
        assertEquals("Driving to deliver", t2.getStatus());     
        assertEquals(s5, t2.peekLoad());    
        assertEquals(0, t2.spaceInLoad());
        t2.unload();
        assertEquals(s7, t2.getPriorityManifest()); 
        assertEquals(s7.getSource(), t2.getCurrDestination());
        assertEquals("Driving to pick up", t2.getStatus());     
        assertEquals(s6, t2.peekLoad());    
        assertEquals(1, t2.spaceInLoad());
        t2.pickup();
        assertEquals(null, t2.getPriorityManifest()); 
        assertEquals(s7.getDestination(), t2.getCurrDestination());
        assertEquals("Driving to deliver", t2.getStatus());     
        assertEquals(s7, t2.peekLoad());    
        assertEquals(0, t2.spaceInLoad());
        t2.unload();
        assertEquals(null, t2.getPriorityManifest()); 
        assertEquals(s6.getDestination(), t2.getCurrDestination());
        assertEquals("Driving to deliver", t2.getStatus());     
        assertEquals(s6, t2.peekLoad());    
        assertEquals(1, t2.spaceInLoad());
        t2.unload();
        assertEquals("Done", t2.getStatus()); 
        assertEquals(null, t2.getPriorityManifest()); 
        assertEquals(null, t2.getCurrDestination());
        assertTrue(t1.finished());    
        assertEquals(null, t2.peekLoad());    
        assertEquals(3, t2.spaceInLoad());
        
        Map.reset();
    }
    
    /**
     * Tests the equals() method (and hashCode()) of Truck class for the correct
     *      comparison of the Truck objects.
     */
    @Test
    @DisplayName("Test of equals() method")
    public void equalsTest() {
        // Declaration and initialization of test variables
        Map map = new Map(10,10);
        Truck t1 = new Truck(0, 0, 5);
        Truck t2 = new Truck(0, 0, 5);
        Truck t3 = t1;
        Warehouse w1  = new Warehouse(0, 0, 1);
        
        // Assertion statements - test various cases: comparison with same/different Trucks, other objects, nulls etc.
        assertEquals(t1.hashCode(), t3.hashCode());
        assertNotEquals(t1.hashCode(), t2.hashCode());
        assertTrue(t1.equals(t3));
        assertTrue(t3.equals(t1));
        assertFalse(t1.equals(t2));
        assertFalse(t2.equals(t1));
        assertFalse(t1.equals(w1));
        assertFalse(t2.equals(w1));
        assertFalse(t1.equals(null));
        assertFalse(t2.equals(null));
        assertFalse(t3.equals(null));
        
        Map.reset();
    }
}