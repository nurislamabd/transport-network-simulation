package test;


import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.Scanner;
import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import simulation.Main;


/**
 * The test class MainTest tests Main's randomConfiguration() and configurate() methods.
 *
 * @author  Nuris Abdyldaev
 * @version 05/02/2026
 */
public class MainTest
{
    /**
     * Default constructor for test class MainTest
     */
    public MainTest()
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
     * Tests the configure() of Main class for the correct
     *      throw of exception if invalid params,
     *      creation of configuration.
     */
    @Test
    @DisplayName("Test of configure()")
    public void configureTest() throws java.io.IOException {
        // declaration and initialization of test variables
        Main.configure(new File("config.txt"), 1, 1, 1, 2, 1);
        Scanner scFile = new Scanner(new File("config.txt"));
        String[] line = {};
        
        // assertion statements - make sure the data written is correct and in a correct format
        line = scFile.nextLine().split(",");
        assertEquals(1, Integer.parseInt(line[0]));
        assertEquals(1, Integer.parseInt(line[1]));
        
        line = scFile.nextLine().split(",");
        assertEquals(2, Integer.parseInt(line[0]));
        assertEquals(1, Integer.parseInt(line[1]));
        assertEquals(1, Integer.parseInt(line[2]));
        
        line = scFile.nextLine().split(",");
        assertEquals("Warehouse", line[0]);
        assertTrue(Double.parseDouble(line[1]) <= 1 && Double.parseDouble(line[1]) >= 0 );
        assertTrue(Double.parseDouble(line[2]) <= 1 && Double.parseDouble(line[2]) >= 0 );        
        assertTrue(Integer.parseInt(line[3]) <= 3 && Integer.parseInt(line[3]) >= 1 );
        
        line = scFile.nextLine().split(",");
        assertEquals("Warehouse", line[0]);
        assertTrue(Double.parseDouble(line[1]) <= 1 && Double.parseDouble(line[1]) >= 0 );
        assertTrue(Double.parseDouble(line[2]) <= 1 && Double.parseDouble(line[2]) >= 0 );        
        assertTrue(Integer.parseInt(line[3]) <= 3 && Integer.parseInt(line[3]) >= 1 );
        
        line = scFile.nextLine().split(",");
        assertEquals("Shipment", line[0]);
        assertTrue(Integer.parseInt(line[1]) <= 2 && Integer.parseInt(line[1]) >= 1 );
        assertTrue(Integer.parseInt(line[2]) <= 2 && Integer.parseInt(line[2]) >= 1 );        
        assertTrue(Integer.parseInt(line[3]) <= 3 && Integer.parseInt(line[3]) >= 1 );  
        
        line = scFile.nextLine().split(",");
        assertEquals("Truck", line[0]);
        assertTrue(Double.parseDouble(line[1]) <= 1 && Double.parseDouble(line[1]) >= 0 );
        assertTrue(Double.parseDouble(line[2]) <= 1 && Double.parseDouble(line[2]) >= 0 );        
        assertTrue(Integer.parseInt(line[3]) <= 5 && Integer.parseInt(line[3]) >= 2 );     
        assertTrue(Integer.parseInt(line[4]) == 1 );
        
        scFile.close();
        
        // assert throws 
        assertThrows(IllegalArgumentException.class, () -> Main.configure(new File("config.txt"), 0, 1, 1, 2, 1));
        assertThrows(IllegalArgumentException.class, () -> Main.configure(new File("config.txt"), 1, 0, 1, 2, 1));
        assertThrows(IllegalArgumentException.class, () -> Main.configure(new File("config.txt"), 1001, 1, 1, 2, 1));
        assertThrows(IllegalArgumentException.class, () -> Main.configure(new File("config.txt"), 1, 1001, 1, 2, 1));
        assertThrows(IllegalArgumentException.class, () -> Main.configure(new File("config.txt"), 1, 1, 0, 2, 1));
        assertThrows(IllegalArgumentException.class, () -> Main.configure(new File("config.txt"), 1, 1, 5001, 2, 1));
        assertThrows(IllegalArgumentException.class, () -> Main.configure(new File("config.txt"), 1, 1, 1, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> Main.configure(new File("config.txt"), 1, 1, 1, 5001, 1));
        assertThrows(IllegalArgumentException.class, () -> Main.configure(new File("config.txt"), 1, 1, 1, 2, 0));
        assertThrows(IllegalArgumentException.class, () -> Main.configure(new File("config.txt"), 1, 1, 1, 2, 10001));
        
        
        BufferedWriter buffer = new BufferedWriter(new FileWriter("config.txt", false));
        buffer.close();
    }
    
    /**
     * Tests the randomConfiguration() of Main class for the correct
     *      throw of exception if invalid params,
     *      creation of configuration.
     */
    @Test
    @DisplayName("Test of randomConfiguration()")
    public void randomConfigurationTest() throws java.io.IOException {
        // declaration and initialization of test variables
        Main.randomConfiguration(new File("config.txt"));
        Scanner scFile = new Scanner(new File("config.txt"));
        String[] line = {};
        
        // assertion statements - make sure the data written is correct and in a correct format
        line = scFile.nextLine().split(",");
        assertTrue(Integer.parseInt(line[0]) <= 1000 && Integer.parseInt(line[0]) >= 10);
        assertTrue(Integer.parseInt(line[1]) <= 1000 && Integer.parseInt(line[1]) >= 10);
        int x = Integer.parseInt(line[0]);
        int y = Integer.parseInt(line[1]);
        
        line = scFile.nextLine().split(",");
        assertTrue(Integer.parseInt(line[0]) <= 5000 && Integer.parseInt(line[0]) >= 2);
        assertTrue(Integer.parseInt(line[1]) <= 10000 && Integer.parseInt(line[1]) >= 1);
        assertTrue(Integer.parseInt(line[2]) <= 5000 && Integer.parseInt(line[2]) >= 1);
        int warehouses = Integer.parseInt(line[0]);
        int shipments = Integer.parseInt(line[1]);
        int trucks = Integer.parseInt(line[2]);
        
        for (int i = 0; i < warehouses; i++) {
            line = scFile.nextLine().split(",");
            assertEquals("Warehouse", line[0]);
            assertTrue(Double.parseDouble(line[1]) <= x && Double.parseDouble(line[1]) >= 0 );
            assertTrue(Double.parseDouble(line[2]) <= y && Double.parseDouble(line[2]) >= 0 );        
            assertTrue(Integer.parseInt(line[3]) <= 3 && Integer.parseInt(line[3]) >= 1 );
        }
        
        for (int i = 0; i < shipments; i++) {
            line = scFile.nextLine().split(",");
            assertEquals("Shipment", line[0]);
            assertTrue(Integer.parseInt(line[1]) <= warehouses && Integer.parseInt(line[1]) >= 1 );
            assertTrue(Integer.parseInt(line[2]) <= warehouses && Integer.parseInt(line[2]) >= 1 );        
            assertTrue(Integer.parseInt(line[3]) <= 3 && Integer.parseInt(line[3]) >= 1 );  
        }
        
        for (int i = 0; i < trucks; i++) {
            line = scFile.nextLine().split(",");
            assertEquals("Truck", line[0]);
            assertTrue(Double.parseDouble(line[1]) <= x && Double.parseDouble(line[1]) >= 0 );
            assertTrue(Double.parseDouble(line[2]) <= y && Double.parseDouble(line[2]) >= 0 );        
            assertTrue(Integer.parseInt(line[3]) <= 5 && Integer.parseInt(line[3]) >= 2 );   
            for (int j = 4; j < line.length; j++) {
                assertTrue(Integer.parseInt(line[j]) <= shipments && Integer.parseInt(line[4]) >= 1 );  
            }
        }
        
        scFile.close();
        BufferedWriter buffer = new BufferedWriter(new FileWriter("config.txt", false));
        buffer.close();
    }
}