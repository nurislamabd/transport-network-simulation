package test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import datastructures.ArrayBasedStack;

/**
 * The test class ArrayBasedStackTest tests the ArrayBasedStack class 
 * and all of its methods using various unit tests. 
 *
 * @author  Nuris Abdyldaev
 * @version 05/01/2026
 */
public class ArrayBasedStackTest
{
    /**
     * Default constructor for test class ArrayBasedStackTest
     */
    public ArrayBasedStackTest()
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
     * Tests the constructor of ArrayBasedStack class for the correct
     *      initialization of the internal variables.
     */
    @Test
    @DisplayName("Test constructor")
    public void testOfArrayBasedStack() {
        // Declaration and initialization of test variables
        ArrayBasedStack<Integer> stack = new ArrayBasedStack<Integer>();
        
        // Assertion statements 
        assertEquals(0, stack.size()); 
        assertTrue(stack.isEmpty());   
    }
    
    /**
     * Tests all methods of ArrayBasedStack with various cases
     */
    @Test
    @DisplayName("Test of all ArrayBasedStack methods")
    public void stackTests() {
        // Declaration and initialization of test variables
        ArrayBasedStack<Integer> stack = new ArrayBasedStack<Integer>();
        
        // test isEmpty(); pop(); 
        assertTrue(stack.isEmpty());
        assertEquals(null, stack.pop());
        
        // test push & peek & size
        for (int i = 0; i < 10; ++i) {
            stack.push(new Integer(i));
            assertEquals(i, stack.peek());
            assertEquals(i + 1, stack.size());
        }
        assertFalse(stack.isEmpty());
        assertEquals(10, stack.size()); 
        assertEquals(9, stack.peek());
        stack.push(50000);
        assertFalse(stack.isEmpty());
        assertEquals(11, stack.size());
        
        // toString();
        assertEquals("[ 0 1 2 3 4 5 6 7 8 9 50000 ]", stack.toString());
        
        // test isEmpty(); pop(); size(); peek()
        assertFalse(stack.isEmpty());
        assertEquals(50000, stack.peek());
        assertEquals(50000, stack.pop());
        assertEquals(10, stack.size());
        assertEquals(9, stack.peek());
        assertEquals(9, stack.pop());
        assertEquals(9, stack.size());
        assertEquals(8, stack.peek());
        assertEquals(8, stack.pop());
        assertEquals(8, stack.size());
        assertEquals(7, stack.peek());
        assertEquals(7, stack.pop());
        assertEquals(7, stack.size());
        assertEquals(6, stack.peek());
        assertEquals(6, stack.pop());
        assertEquals(6, stack.size());
        assertEquals(5, stack.peek());
        assertEquals(5, stack.pop());
        assertEquals(5, stack.size());
        assertEquals(4, stack.peek());
        assertEquals(4, stack.pop());
        assertEquals(4, stack.size());
        assertEquals(3, stack.peek());
        assertEquals(3, stack.pop());
        assertEquals(3, stack.size());
        assertEquals(2, stack.peek());
        assertEquals(2, stack.pop());
        assertEquals(2, stack.size());
        assertEquals(1, stack.peek());
        assertEquals(1, stack.pop());
        assertEquals(1, stack.size());
        assertEquals(0, stack.peek());
        assertEquals(0, stack.pop());
        assertEquals(0, stack.size());
        assertEquals(null, stack.pop());
        assertTrue(stack.isEmpty());
    }
}