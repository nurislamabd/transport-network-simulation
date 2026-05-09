package test;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import datastructures.MyArrayList;
import datastructures.QueueNode;
import models.Map;
import models.Shipment;
import models.Truck;
import models.Warehouse;
import simulation.Simulation;

/**
 * The test class WarehouseTest tests the Warehouse class 
 * and all of its methods using various unit tests.  
 *
 * @author  Nuris Abdyldaev
 * @version 04/28/2026
 */
public class WarehouseTest
{
    /**
     * Default constructor for test class WarehouseTest
     */
    public WarehouseTest()
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
     * Tests the constructor (and getters) of Warehouse class for the correct
     *      initialization of the internal variables.
     */
    @Test
    @DisplayName("Test of constructor")
    public void constructorTest() {
        // Declaration and initialization of test variables
        Warehouse w1 = new Warehouse(0, 0, 1);
        Warehouse w2 = new Warehouse(0, 0, 1);
        
        // Assertion statements - check for correct initialization of vars
        assertEquals(1, w1.getId()); 
        assertTrue(0 == w1.getX());
        assertTrue(0 == w1.getY());
        assertTrue(1 == w1.getLoadingDocks());
        assertEquals(2, w2.getId()); 
        assertTrue(0 == w2.getX());
        assertTrue(0 == w2.getY());
        assertTrue(1 == w2.getLoadingDocks());
        
        assertThrows(IllegalArgumentException.class, () -> new Warehouse(-1, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> new Warehouse(100, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> new Warehouse(0, -1, 1));
        assertThrows(IllegalArgumentException.class, () -> new Warehouse(0, 100, 1));
        assertThrows(IllegalArgumentException.class, () -> new Warehouse(0, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new Warehouse(0, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> new Warehouse(0, 0, -4));
        
        Map.reset();
    }
    
    /**
     * Tests the action() of Warehouse class for the correct
     *      actions to be done on the trucks queue.
     */
    @Test
    @DisplayName("Test of action()")
    public void actionTest() {
        // Declaration and initialization of test variables
        Warehouse w1 = new Warehouse(0, 0, 3);
        Warehouse w2 = new Warehouse(0, 0, 1);
        Shipment fromW2ToW1    = new Shipment(w2, w1, 1);   
        Shipment fromW1ToW2    = new Shipment(w1, w2, 1);   
        Shipment fromW2ToW1v2  = new Shipment(w2, w1, 1);
        Shipment fromW1ToW2v2  = new Shipment(w1, w2, 1);        
        
        w1.addToInventory(fromW1ToW2);
        w1.addToInventory(fromW1ToW2v2);
        
        
        // truck1 - only unload
        Truck t1 = new Truck(0, 0, 5); 
        t1.addManifest(fromW2ToW1);
        t1.pickup();
        
        // truck2 - unload and load
        Truck t2 = new Truck(0, 0, 5);
        t2.addManifest(fromW2ToW1v2);
        t2.addManifest(fromW1ToW2);
        t2.pickup();
        assertEquals(fromW2ToW1v2, t2.peekLoad());
        assertEquals(fromW1ToW2, t2.getPriorityManifest());
        
        // truck3 - only load
        Truck t3 = new Truck(0, 0, 5);
        t3.addManifest(fromW1ToW2v2);
        
        // truck 4 - a truck that should def remain in the queue unprocessed
        Truck t4 = new Truck(0, 0, 5);
         
        w1.addTruckToQueue(t1);
        w1.addTruckToQueue(t2);
        w1.addTruckToQueue(t3);
        w1.addTruckToQueue(t4);
        
        // Assertion statements
        QueueNode<Truck> node = w1.getTrucksHead();
        assertEquals(t1, node.data);
        node = node.next;
        assertEquals(t2, node.data);
        node = node.next;
        assertEquals(t3, node.data);
        node = node.next; 
        assertEquals(t4, node.data);
        node = node.next; 
        assertNull(node);
        
        w1.action(); 
        MyArrayList<Shipment> inventory = w1.getInventory();
        node = w1.getTrucksHead();
        assertEquals(2, inventory.size());
        assertEquals(fromW2ToW1, inventory.get(0));
        assertEquals(fromW2ToW1v2, inventory.get(1));
        assertEquals(t4, node.data); // 3 loadingDocks, truck4 should be in the queue
        
        w1.action(); 
        assertNull(w1.getTrucksHead()); // empty queue
        
        Map.reset();
    }
    
    /**
     * Tests the logStatus() of Warehouse class for the correct
     *      log of object's info.
     */
    @Test
    @DisplayName("Test of logStatus()")
    public void logStatusTest() throws java.io.IOException {
        // Declaration and initialization of test variables\
        Simulation.initializeBuffers();
        
        Warehouse w1 = new Warehouse(0, 0, 1);
        Warehouse w2 = new Warehouse(0, 0, 1);
        Warehouse w3 = new Warehouse(0, 0, 1);
        Shipment shipment1 = new Shipment(w1, w2, 1);
        Shipment shipment2 = new Shipment(w2, w3, 1);
        Shipment shipment3 = new Shipment(w3, w3, 1);
        Truck t1 = new Truck(0, 0, 5);
        Truck t2 = new Truck(0, 0, 5);
        Truck t3 = new Truck(0, 0, 5);
        String[] line = {};
        shipment2.setLocation(null);
        
        w2.addTruckToQueue(t1);
        w2.addToInventory(shipment1);
        w2.addToInventory(shipment2);
        w2.removeFromInventory(shipment2);
        
        w3.addTruckToQueue(t2);
        w3.addTruckToQueue(t1);
        w3.addTruckToQueue(t3);
        w3.addToInventory(shipment2);
        w3.addToInventory(shipment1);
        w3.addToInventory(shipment3);
        
        w1.logStatus();
        w2.logStatus();
        w3.logStatus();
        Simulation.closeBuffers();
        
        Scanner scFile = new Scanner(new File("WarehousesCSV.txt"));
        
        // Assertion statements - make sure the data written is correct and in a correct format
        line = scFile.nextLine().split(",");
        assertEquals(Map.getCurrHour(), Integer.parseInt(line[0]));
        assertEquals(w1.getId(), Integer.parseInt(line[1]));
        assertEquals(w1.getX(), Double.parseDouble(line[2]));
        assertEquals(w1.getY(), Double.parseDouble(line[3]));
        assertEquals(w1.getLoadingDocks(), Integer.parseInt(line[4]));
        assertEquals(0, Integer.parseInt(line[5]));
        assertEquals("[ ]", line[6]);
        assertEquals(0, Integer.parseInt(line[7]));
        assertEquals("[ ]", line[8]);
        
        line = scFile.nextLine().split(",");
        assertEquals(Map.getCurrHour(), Integer.parseInt(line[0]));
        assertEquals(w2.getId(), Integer.parseInt(line[1]));
        assertEquals(w2.getX(), Double.parseDouble(line[2]));
        assertEquals(w2.getY(), Double.parseDouble(line[3]));
        assertEquals(w2.getLoadingDocks(), Integer.parseInt(line[4]));
        assertEquals(1, Integer.parseInt(line[5]));
        assertEquals("[ 1 ]", line[6]);
        assertEquals(1, Integer.parseInt(line[7]));
        assertEquals("[ 1 ]", line[8]);
        
        line = scFile.nextLine().split(",");
        assertEquals(Map.getCurrHour(), Integer.parseInt(line[0]));
        assertEquals(w3.getId(), Integer.parseInt(line[1]));
        assertEquals(w3.getX(), Double.parseDouble(line[2]));
        assertEquals(w3.getY(), Double.parseDouble(line[3]));
        assertEquals(w3.getLoadingDocks(), Integer.parseInt(line[4]));
        assertEquals(3, Integer.parseInt(line[5]));
        assertEquals("[ 2 1 3 ]", line[6]);
        assertEquals(3, Integer.parseInt(line[7]));
        assertEquals("[ 1 2 3 ]", line[8]);
        
        
        
        BufferedWriter buffer = new BufferedWriter(new FileWriter("WarehousesCSV.txt", false));
        Map.reset();
        buffer.close();
        scFile.close();
    }
    
    /**
     * Tests the equals() method (and hashCode()) of Warehouse class for the correct
     *      comparison of the Warehouse objects.
     */
    @Test
    @DisplayName("Test of equals() method")
    public void equalsTest() {
        // Declaration and initialization of test variables
        Warehouse w1 = new Warehouse(0, 0, 1);
        Warehouse w2 = new Warehouse(0, 0, 1);
        Warehouse w3 = w1;
        Shipment s1  = new Shipment(w1, w2, 1);
        
        // Assertion statements - test various cases: comparison with same/different Warehouses, other objects, nulls etc.
        assertEquals(w1.hashCode(), w3.hashCode());
        assertNotEquals(w1.hashCode(), w2.hashCode());
        assertTrue(w1.equals(w3));
        assertTrue(w3.equals(w1));
        assertFalse(w1.equals(w2));
        assertFalse(w2.equals(w1));
        assertFalse(w1.equals(s1));
        assertFalse(w2.equals(s1));
        assertFalse(w1.equals(null));
        assertFalse(w2.equals(null));
        assertFalse(w3.equals(null));
        
        Map.reset();
    }
    
    /**
     * Tests the addTruckToQueue() (and getters) of Warehouse class for the correct
     *      addition of the shipments to the inventory,
     *      maintenance of the order of the shipments in the inventory,
     *      set of shipments location to this warehouse.
     */
    @Test
    @DisplayName("Test of addToInventory()")
    public void addToInventoryTest() {
        // Declaration and initialization of test variables
        Warehouse w1 = new Warehouse(0, 0, 1);
        Warehouse w2 = new Warehouse(0, 0, 1);
        Shipment s1  = new Shipment(w2, w1, 1);
        Shipment s2  = new Shipment(w2, w1, 1);
        Shipment s3  = new Shipment(w2, w1, 1);
        Shipment s4  = new Shipment(w2, w1, 1);
        Shipment s5  = new Shipment(w2, w1, 1);
        Shipment s6  = new Shipment(w2, w1, 1);
        Shipment s7  = new Shipment(w2, w1, 1);
        
        w1.addToInventory(s7);
        w1.addToInventory(s4);
        w1.addToInventory(s3);
        w1.addToInventory(s5);
        w1.addToInventory(s1);
        w1.addToInventory(s6);
        w1.addToInventory(s2);
        
        // Assertion statements - check for sorting, and set of location of shipments
        MyArrayList<Shipment> inventory = w1.getInventory();
        assertTrue(inventory.get(0).equals(s1));
        assertTrue(inventory.get(1).equals(s2));
        assertTrue(inventory.get(2).equals(s3));
        assertTrue(inventory.get(3).equals(s4));
        assertTrue(inventory.get(4).equals(s5));
        assertTrue(inventory.get(5).equals(s6));
        assertTrue(inventory.get(6).equals(s7));
        
        assertTrue(s1.getLocation().equals(w1));
        assertTrue(s2.getLocation().equals(w1));
        assertTrue(s3.getLocation().equals(w1));
        assertTrue(s4.getLocation().equals(w1));
        assertTrue(s5.getLocation().equals(w1));
        assertTrue(s6.getLocation().equals(w1));
        assertTrue(s7.getLocation().equals(w1));
        
        w1.addToInventory(s7);
        w1.addToInventory(s4);
        w1.addToInventory(s3);
        w1.addToInventory(s5);
        w1.addToInventory(s1);
        w1.addToInventory(s6);
        w1.addToInventory(s2);
        
        // Assertion statements - check for sorting
        MyArrayList<Shipment> inventory1 = w1.getInventory();
        assertTrue(inventory1.get(0).equals(s1));
        assertTrue(inventory1.get(1).equals(s1));
        assertTrue(inventory1.get(2).equals(s2));
        assertTrue(inventory1.get(3).equals(s2));
        assertTrue(inventory1.get(4).equals(s3));
        assertTrue(inventory1.get(5).equals(s3));
        assertTrue(inventory1.get(6).equals(s4));
        assertTrue(inventory1.get(7).equals(s4));
        assertTrue(inventory1.get(8).equals(s5));
        assertTrue(inventory1.get(9).equals(s5));
        assertTrue(inventory1.get(10).equals(s6));
        assertTrue(inventory1.get(11).equals(s6));
        assertTrue(inventory1.get(12).equals(s7));
        assertTrue(inventory1.get(13).equals(s7));
        
        Map.reset();
    }
    
    /**
     * Tests the removeFromInventory() of Warehouse class for the correct
     *      return of true when shipment is removed and decremention of the inventorySortedUntil,
     *      set of shipments location to null,
     *      return of false if the shipment is not found.
     */
    @Test
    @DisplayName("Test of removeFromInventory()")
    public void removeFromInventoryTest() {
        // Declaration and initialization of test variables
        Warehouse w1 = new Warehouse(0, 0, 1);
        Warehouse w2 = new Warehouse(0, 0, 1);
        Shipment s1  = new Shipment(w2, w1, 1);
        Shipment s2  = new Shipment(w2, w1, 1);
        Shipment s3  = new Shipment(w2, w1, 1);
        Shipment s4  = new Shipment(w2, w1, 1);
        Shipment s5  = new Shipment(w2, w1, 1);
        Shipment s6  = new Shipment(w2, w1, 1);
        Shipment s7  = new Shipment(w2, w1, 1);
        
        // can't remove when not in the inventory
        assertFalse(w1.removeFromInventory(s1));
        assertFalse(w1.removeFromInventory(s2));
        assertFalse(w1.removeFromInventory(s3));
        assertFalse(w1.removeFromInventory(s4));
        assertFalse(w1.removeFromInventory(s5));
        
        w1.addToInventory(s7);
        
        // can't remove when not in the inventory
        assertFalse(w1.removeFromInventory(s1));
        assertFalse(w1.removeFromInventory(s2));
        assertFalse(w1.removeFromInventory(s3));
        assertFalse(w1.removeFromInventory(s4));
        assertFalse(w1.removeFromInventory(s5));
        
        
        w1.addToInventory(s4);
        w1.addToInventory(s3);
        w1.addToInventory(s5);
        w1.addToInventory(s1);
        w1.addToInventory(s6);
        w1.addToInventory(s2);
        
        // check that shipments are correctly added
        MyArrayList<Shipment> inventory = w1.getInventory();
        assertTrue(inventory.get(0).equals(s1));
        assertTrue(inventory.get(1).equals(s2));
        assertTrue(inventory.get(2).equals(s3));
        assertTrue(inventory.get(3).equals(s4));
        assertTrue(inventory.get(4).equals(s5));
        assertTrue(inventory.get(5).equals(s6));
        assertTrue(inventory.get(6).equals(s7));
        assertTrue(s1.getLocation().equals(w1));
        assertTrue(s2.getLocation().equals(w1));
        assertTrue(s3.getLocation().equals(w1));
        assertTrue(s4.getLocation().equals(w1));
        assertTrue(s5.getLocation().equals(w1));
        assertTrue(s6.getLocation().equals(w1));
        assertTrue(s7.getLocation().equals(w1));
        
        // check for crrect removal
        assertTrue(w1.removeFromInventory(s4));
        assertTrue(s4.getLocation() == (null));
        inventory = w1.getInventory(); 
        assertTrue(inventory.get(0).equals(s1));
        assertTrue(inventory.get(1).equals(s2));
        assertTrue(inventory.get(2).equals(s3));
        assertTrue(inventory.get(3).equals(s5));
        assertTrue(inventory.get(4).equals(s6));
        assertTrue(inventory.get(5).equals(s7));
        
        assertTrue(w1.removeFromInventory(s2));
        assertTrue(s2.getLocation() == (null));
        inventory = w1.getInventory(); 
        assertTrue(inventory.get(0).equals(s1));
        assertTrue(inventory.get(1).equals(s3));
        assertTrue(inventory.get(2).equals(s5));
        assertTrue(inventory.get(3).equals(s6));
        assertTrue(inventory.get(4).equals(s7));
        
        assertTrue(w1.removeFromInventory(s7));
        assertTrue(s7.getLocation() == (null));
        inventory = w1.getInventory(); 
        assertTrue(inventory.get(0).equals(s1));
        assertTrue(inventory.get(1).equals(s3));
        assertTrue(inventory.get(2).equals(s5));
        assertTrue(inventory.get(3).equals(s6));
        
        // check that shipments are correctly added after removal
        w1.addToInventory(s4);
        w1.addToInventory(s7);
        w1.addToInventory(s2);
        
        inventory = w1.getInventory(); 
        assertTrue(inventory.get(0).equals(s1));
        assertTrue(inventory.get(1).equals(s2));
        assertTrue(inventory.get(2).equals(s3));
        assertTrue(inventory.get(3).equals(s4));
        assertTrue(inventory.get(4).equals(s5));
        assertTrue(inventory.get(5).equals(s6));
        assertTrue(inventory.get(6).equals(s7));
        
        w1.addToInventory(s7);
        w1.addToInventory(s4);
        w1.addToInventory(s3);
        w1.addToInventory(s5);
        w1.addToInventory(s1);
        w1.addToInventory(s6);
        w1.addToInventory(s2);
        
        // check that shipments are sorted after removal
        MyArrayList<Shipment> inventory1 = w1.getInventory();
        assertTrue(inventory1.get(0).equals(s1));
        assertTrue(inventory1.get(1).equals(s1));
        assertTrue(inventory1.get(2).equals(s2));
        assertTrue(inventory1.get(3).equals(s2));
        assertTrue(inventory1.get(4).equals(s3));
        assertTrue(inventory1.get(5).equals(s3));
        assertTrue(inventory1.get(6).equals(s4));
        assertTrue(inventory1.get(7).equals(s4));
        assertTrue(inventory1.get(8).equals(s5));
        assertTrue(inventory1.get(9).equals(s5));
        assertTrue(inventory1.get(10).equals(s6));
        assertTrue(inventory1.get(11).equals(s6));
        assertTrue(inventory1.get(12).equals(s7));
        assertTrue(inventory1.get(13).equals(s7));
        
        Map.reset();
    }
    
    /**
     * Tests the addTruckToQueue() of Warehouse class for the correct
     *      addition of the trucks to the trucks queue.
     */
    @Test
    @DisplayName("Test of addTruckToQueue()")
    public void addTruckToQueueTest() {
        // Declaration and initialization of test variables
        Warehouse w1 = new Warehouse(0, 0, 1);
        Warehouse w2 = new Warehouse(0, 0, 1);
        Truck t1 = new Truck(0, 0, 5);
        Truck t2 = new Truck(0, 0, 5);
        Truck t3 = new Truck(0, 0, 5);
        Truck t4 = new Truck(0, 0, 5);
        Truck t5 = new Truck(0, 0, 5);
        Truck t6 = new Truck(0, 0, 5);
        Truck t7 = new Truck(0, 0, 5);
        
        w1.addTruckToQueue(t7);
        w1.addTruckToQueue(t4);
        w1.addTruckToQueue(t3);
        w1.addTruckToQueue(t5);
        w1.addTruckToQueue(t1);
        w1.addTruckToQueue(t6);
        w1.addTruckToQueue(t2);
        
        // Assertion statements - check for the correct addition to the queue
        QueueNode<Truck> node = w1.getTrucksHead();
        assertEquals(t7, node.data);
        node = node.next;
        assertEquals(t4, node.data);
        node = node.next;
        assertEquals(t3, node.data);
        node = node.next;
        assertEquals(t5, node.data);
        node = node.next;
        assertEquals(t1, node.data);
        node = node.next;
        assertEquals(t6, node.data);
        node = node.next;
        assertEquals(t2, node.data);
        node = node.next;
        assertNull(node);
        
        Map.reset();
    }
}