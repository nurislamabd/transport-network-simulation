package test;



import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import datastructures.MyQueue;

import org.junit.jupiter.api.DisplayName;
import java.util.NoSuchElementException;

/**
 * The test class MyQueueTest tests the MyQueue class 
 * and all of its methods using various unit tests. 
 *
 * @author  Nuris Abdyldaev
 * @version 04/26/2026
 */
public class MyQueueTest
{
    /**
     * Default constructor for test class MyQueueTest
     */
    public MyQueueTest()
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
     * Tests the constructor of MyQueue class for the correct
     *      initialization of the internal variables.
     */
    @Test
    @DisplayName("Test constructor")
    public void testOfMyQueue() {
        // Declaration and initialization of test variables
        MyQueue<Integer> queue = new MyQueue<Integer>();
        
        // Assertion statements 
        assertEquals(0, queue.size()); 
        assertTrue(queue.isEmpty());   
    }
    
    /**
     * Tests all methods of MyQueue with various cases
     */
    @Test
    @DisplayName("Test of all MyQueue methods")
    public void queueTests() {
        // Declaration and initialization of test variables
        MyQueue<Integer> queue = new MyQueue<Integer>();
        
        // test isEmpty(); remove(); poll();
        assertTrue(queue.isEmpty());
        assertThrows(NoSuchElementException.class, () -> queue.remove());
        assertEquals(null, queue.poll());
        
        // test add & peek & size
        for (int i = 0; i < 10; ++i) {
            assertTrue(queue.add(new Integer(i)));
            assertEquals(0, queue.peek());
            assertEquals(i + 1, queue.size());
        }
        assertFalse(queue.isEmpty());
        assertEquals(10, queue.size()); 
        assertEquals(0, queue.peek());
        assertTrue(queue.add(50000));
        assertFalse(queue.isEmpty());
        assertEquals(11, queue.size());
        
        // test isEmpty(); remove(); poll(); size(); peek()
        assertFalse(queue.isEmpty());
        assertEquals(0, queue.peek());
        assertEquals(0, queue.remove());
        assertEquals(10, queue.size());
        assertEquals(1, queue.peek());
        assertEquals(1, queue.poll());
        assertEquals(9, queue.size());
        assertEquals(2, queue.peek());
        assertEquals(2, queue.remove());
        assertEquals(8, queue.size());
        assertEquals(3, queue.peek());
        assertEquals(3, queue.poll());
        assertEquals(7, queue.size());
        assertEquals(4, queue.peek());
        assertEquals(4, queue.remove());
        assertEquals(6, queue.size());
        assertEquals(5, queue.peek());
        assertEquals(5, queue.poll());
        assertEquals(5, queue.size());
        assertEquals(6, queue.peek());
        assertEquals(6, queue.remove());
        assertEquals(4, queue.size());
        assertEquals(7, queue.peek());
        assertEquals(7, queue.poll());
        assertEquals(3, queue.size());
        assertEquals(8, queue.peek());
        assertEquals(8, queue.remove());
        assertEquals(2, queue.size());
        assertEquals(9, queue.peek());
        assertEquals(9, queue.poll());
        assertEquals(1, queue.size());
        assertEquals(50000, queue.peek());
        assertEquals(50000, queue.remove());
        assertEquals(0, queue.size());
        assertThrows(NoSuchElementException.class, () -> queue.remove());
        assertEquals(null, queue.poll());
        assertTrue(queue.isEmpty());
    }
}