package test;


import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import models.Map;

/**
 * The test class MapTest tests the Map class 
 * and all of its methods using various unit tests. 
 *
 * @author  Nuris Abdyldaev
 * @version 04/26/2026
 */
public class MapTest
{
    /**
     * Default constructor for test class MapTest
     */
    public MapTest()
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
     * Tests all methods of Map class with various test cases.
     */
    @Test
    public void mapTest() {
        // Declaration and initialization of test variables
        Map map0 = new Map(50, 10);
        Map map1 = new Map();
        Map map2 = new Map();
        
        // Assertion statements 
        // test contructors & getters
        assertEquals(0, map0.getCurrHour());
        assertEquals(0, map1.getCurrHour());
        assertEquals(0, map2.getCurrHour());
        assertEquals(50.0, map0.getMapX());
        assertEquals(50.0, map1.getMapX());
        assertEquals(50.0, map2.getMapX());
        assertEquals(10.0, map0.getMapY());
        assertEquals(10.0, map1.getMapY());
        assertEquals(10.0, map2.getMapY());
        
        // test clockIncrement()
        map0.clockIncrement();
        assertEquals(1, map0.getCurrHour());
        assertEquals(1, map1.getCurrHour());
        assertEquals(1, map2.getCurrHour());
        map1.clockIncrement();
        assertEquals(2, map0.getCurrHour());
        assertEquals(2, map1.getCurrHour());
        assertEquals(2, map2.getCurrHour());
        map2.clockIncrement();
        assertEquals(3, map0.getCurrHour());
        assertEquals(3, map1.getCurrHour());
        assertEquals(3, map2.getCurrHour());
        
        // test reset()
        map0.reset();
        assertEquals(0, map0.getCurrHour());
        assertEquals(0, map1.getCurrHour());
        assertEquals(0, map2.getCurrHour());
        assertEquals(0.0, map0.getMapX());
        assertEquals(0.0, map1.getMapX());
        assertEquals(0.0, map2.getMapX());
        assertEquals(0.0, map0.getMapY());
        assertEquals(0.0, map1.getMapY());
        assertEquals(0.0, map2.getMapY());
    }
}