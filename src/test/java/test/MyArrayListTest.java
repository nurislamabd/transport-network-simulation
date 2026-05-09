package test;


import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import datastructures.MyArrayList;

/**
 * This test class tests the MyArrayList class 
 * and all of its methods using various unit tests. 
 *
 * @author  Nuris Abdyldaev
 * @version 2/7/2026
 */
public class MyArrayListTest
{
    /**
     * Default constructor for test class MyArrayListTest
     */
    public MyArrayListTest()
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
     * Tests the constructor of MyArrayList class for the correct
     *      initialization of the internal variables.
     */
    @Test
    public void testOfMyArrayList() {
        // Declaration and initialization of test variables
        MyArrayList intList = new MyArrayList();
        
        // Assertion statements 
        assertEquals(intList.size(), 0);       // Number of elements must be 0
        assertEquals(intList.arraySize(), 10); // Size of the internal array must be 10
    }
    
    /**
     * Tests the add() method with single parameter of MyArrayList class with various cases for the correct:
     *      appending of elements to the end of the list, 
     *      use of resize() (adding 10 empty elements) when the list becomes full.
     */
    @Test
    @DisplayName("Test of add() method with single param")
    public void testOfAdd1() {
        // Declaration and initialization of test variables
        int[] ints  = { 0, 1, 0, 1, 1, 44, -55, 44, -33 };
        String str1 = "[ 0 1 0 1 1 44 -55 44 -33 ]";
        String str2 = "[ 0 1 0 1 1 44 -55 44 -33 50000 ]";
        MyArrayList intList = new MyArrayList();
        for (int i = 0; i < ints.length; ++i) {
            intList.add(ints[i]);
        }
        
        // Assertion statements 
        assertEquals(intList.toString(), str1); // Check for the added elements
        assertEquals(intList.size(), 9);        // Check if the numInts (stores number of elements) increments properly
        assertEquals(intList.arraySize(), 10);  // Check if the internal array's size is still 10
        
        // Add 1 more element to test the resize()
        intList.add(50000);
        
        // Assertion statements 
        assertEquals(intList.toString(), str2); // Check for the added elements
        assertEquals(intList.size(), 10);       // Check if the numInts (stores number of elements) increments properly
        assertEquals(intList.arraySize(), 20);  // Check if the internal array's size is resized to 20 when the array became full
    }
    
    /**
     * Tests the add() with two parameters of MyArrayList class with various cases for the correct: 
     *      prepending of elements (border cases), 
     *      insertion of the elements to the middle of the arrray (basic cases),
     *      appending of elements (border cases),
     *      throw of IndexOutOfBoundsException exception with its message (border cases),
     *      incrementation of internal variable (numInts), 
     *      use of resize() (adding 10 empty elements) when the list becomes full.
     */
    @Test
    @DisplayName("Test of add() method with two params")
    public void testOfAdd2() {
        // Declaration and initialization of test variables
        int[] ints  = { 838, 22, 77, -31, 0, 67, -78 };
        String str1 = "[ -78 67 0 -31 -202 77 22 990 838 ]";
        String str2 = "[ -78 67 0 -31 -202 77 22 990 838 -59880 ]";
        MyArrayList intList = new MyArrayList();
        
        // Prepend elements
        for (int i = 0; i < ints.length; ++i) {
            intList.add(0, ints[i]);
        }
        
        // Insert some elements into the array
        intList.add(4, -202);
        intList.add(7, 990);
        
        // Assertion statements 
        assertEquals(intList.get(4), -202);     // Check for the inserted element
        assertEquals(intList.get(7), 990);      // Check for the inserted element        
        assertEquals(intList.toString(), str1); // Check for all of the added elements
        assertEquals(intList.size(), 9);        // Check if the numInts (stores number of elements) increments properly
        assertEquals(intList.arraySize(), 10);  // Check if the internal array's size is still 10
        
        // Check for the IndexOutOfBoundsException if index is positive (border cases)
        assertEquals("Index is out of range", assertThrows(IndexOutOfBoundsException.class, () -> intList.add(10, 60)).getMessage());
        assertEquals("Index is out of range", assertThrows(IndexOutOfBoundsException.class, () -> intList.add(60, 60)).getMessage());
        
        // Check for the IndexOutOfBoundsException if index is negative (border cases)
        assertEquals("Index is out of range", assertThrows(IndexOutOfBoundsException.class, () -> intList.add(-60, 60)).getMessage());
        assertEquals("Index is out of range", assertThrows(IndexOutOfBoundsException.class, () -> intList.add(-1, 60)).getMessage());
        
        // Append 1 more element to test the resize() (+ border cases)
        intList.add(9, -59880);
        
        // Assertion statements 
        assertEquals(intList.toString(), str2); // Check for the added elements
        assertEquals(intList.size(), 10);       // Check if the numInts (stores number of elements) increments properly
        assertEquals(intList.arraySize(), 20);  // Check if the internal array's size is resized to 20 when the array became full
    }
    
    /**
     * Tests the set() & get() method of MyArrayList class with various cases for the correct: 
     *      return of the first and last elements (border cases), 
     *      return of the middle elements (basic cases), 
     *      throw  of the IndexOutOfBoundsException exception with its message (border cases).
     */
    @Test
    @DisplayName("Test of set() & get() method")
    public void testOfSetAndGet() {
        // Declaration and initialization of test variables
        int[] ints  = { 838, 22, 77, -31, 0, 67, -78 };
        MyArrayList intList = new MyArrayList();
        for (int i = 0; i < ints.length; ++i) {
            intList.add(ints[i]);
        }
        
        // Assertion statements (checking edges and etc.)
        assertEquals(intList.get(4), 0);   // Basic case
        assertEquals(intList.get(2), 77);  // Basic case        
        assertEquals(intList.get(0), 838); // Border case (beginning)
        assertEquals(intList.get(6), -78); // Border case (end)    
        
        intList.set(4, 5);
        intList.set(2, 5);
        intList.set(0, 5);
        intList.set(6, 5);
        assertEquals(intList.get(4), 5); // Basic case
        assertEquals(intList.get(2), 5); // Basic case        
        assertEquals(intList.get(0), 5); // Border case (beginning)
        assertEquals(intList.get(6), 5); // Border case (end)    
        
        // IndexOutOfBoundsException if index out of range (positive) (border cases)
        assertEquals("Index is out of range", assertThrows(IndexOutOfBoundsException.class, () -> intList.get(89)).getMessage());
        assertEquals("Index is out of range", assertThrows(IndexOutOfBoundsException.class, () -> intList.get(7)).getMessage());
        // IndexOutOfBoundsException if index out of range (negative) (border cases)
        assertEquals("Index is out of range", assertThrows(IndexOutOfBoundsException.class, () -> intList.get(-13410)).getMessage());
        assertEquals("Index is out of range", assertThrows(IndexOutOfBoundsException.class, () -> intList.get(-1)).getMessage());
        // IndexOutOfBoundsException if index out of range (positive) (border cases)
        assertEquals("Index is out of range", assertThrows(IndexOutOfBoundsException.class, () -> intList.set(89, 4)).getMessage());
        assertEquals("Index is out of range", assertThrows(IndexOutOfBoundsException.class, () -> intList.set(7, 5)).getMessage());
        // IndexOutOfBoundsException if index out of range (negative) (border cases)
        assertEquals("Index is out of range", assertThrows(IndexOutOfBoundsException.class, () -> intList.set(-13410, 9)).getMessage());
        assertEquals("Index is out of range", assertThrows(IndexOutOfBoundsException.class, () -> intList.set(-1, 1)).getMessage());
    }
    
    /**
     * Tests the clear() method of MyArrayList class with various cases for the correct:
     *      assignment of the internal variables with the initial values.
     */
    @Test
    @DisplayName("Test of clear() method")
    public void testOfClear() {
        // Declaration and initialization of test variables
        int[] ints  = { 838, 22, 77, -31, 0, 67, -78 };
        MyArrayList intList = new MyArrayList();
        for (int i = 0; i < ints.length; ++i) {
            intList.add(ints[i]);
        }
        
        // List contains elements
        assertFalse(intList.isEmpty());
        
        // Clear the list
        intList.clear();
        
        // Assertion statements
        assertEquals(intList.toString(), "[ ]");  // Basic case 
        assertEquals(intList.size(), 0);       // Internal numInts set to 0
        assertEquals(intList.arraySize(), 10); // Internal array's size set to 10
    }
    
    /**
     * Tests the isEmpty() method of MyArrayList class with various cases f0r the correct:
     *      return of false when the list contains elements (intNums > 0),
     *      return of true  when the list is empty (intNums == 0).
     */
    @Test
    @DisplayName("Test of isEmpty() method")
    public void testOfIsEmpty() {
        // Declaration and initialization of test variables
        int[] ints  = { 838, 22, 77, -31, 0, 67, -78 };
        MyArrayList intList = new MyArrayList();
        for (int i = 0; i < ints.length; ++i) {
            intList.add(ints[i]);
        }
        
        // Returns false when the list contains elements
        assertFalse(intList.isEmpty());
        
        // Clear the list
        intList.clear();
        
        // Returns false when the list is empty
        assertTrue(intList.isEmpty()); 
    }
    
    /**
     * Tests the remove() method of MyArrayList class with various cases for the correct:
     *      removal of the elements from the location specified by the indices, 
     *      return of the removed elements (basic and border cases), 
     *      adjustment of the surrounding elements, 
     *      adjustment of the size (use of resize()) of the internal array if emptyCount() > 10, 
     *      decrementation of the internal variable (numInts), 
     *      throw  of the IndexOutOfBoundsException exception with its message (border cases).
     */
    @Test
    @DisplayName("Test of remove() method")
    public void testOfRemove() {
        // Declaration and initialization of test variables
        int[] ints  = { 838, 22, 77, -31, 0, 67, -78, -22, 3, 5, 10 };
        MyArrayList intList = new MyArrayList();
        String str1 = "[ 22 77 -31 0 67 -78 -22 3 5 10 ]";
        String str2 = "[ 22 77 0 67 -78 -22 3 5 10 ]";
        String str3 = "[ 22 77 0 67 -78 -22 3 5 ]";
        for (int i = 0; i < ints.length; ++i) {
            intList.add(ints[i]);
        }
        
        // Assertion statements
        assertEquals(intList.size(), 11);       // numInts should be 11
        assertEquals(intList.arraySize(), 20);  // Internal array's size must be 20
        
        assertEquals(intList.remove(0), 838);   // Returns the removed number (first)
        assertEquals(intList.toString(), str1); // Removed the first int (border case)
        assertEquals(intList.remove(2), -31);   // Returns the removed number (middle)
        assertEquals(intList.toString(), str2); // Removed the middle int (basic case) 
        
        assertEquals(intList.arraySize(), 19);  // Internal array's size must change
        assertEquals(intList.size(), 9);        // numInts should be 9
        
        assertEquals(intList.remove(8), 10);    // Returns the removed number (last)
        assertEquals(intList.toString(), str3); // Removed the last int (border case)
        assertEquals(intList.arraySize(), 18);  // Internal array's size must change to numInts + 10
        assertEquals(intList.size(), 8);        // numInts should be 8

        
        // IndexOutOfBoundsException if index out of range (positive)
        assertEquals("Index is out of range", assertThrows(IndexOutOfBoundsException.class, () -> intList.remove(8)).getMessage());
        assertEquals("Index is out of range", assertThrows(IndexOutOfBoundsException.class, () -> intList.remove(1000)).getMessage());
        // IndexOutOfBoundsException if index out of range (negative)
        assertEquals("Index is out of range", assertThrows(IndexOutOfBoundsException.class, () -> intList.remove(-1)).getMessage());
        assertEquals("Index is out of range", assertThrows(IndexOutOfBoundsException.class, () -> intList.remove(-6000)).getMessage());
    }
    
    /**
     * Tests the size() method of MyArrayList class with various cases for the correct
     *      return of the number of elements.
     */
    @Test
    @DisplayName("Test of size() method")
    public void testOfSize() {
        // Declaration and initialization of test variables
        int[] ints  = { 838, 22, 77, -31 };
        int[] ints2 = { 0, 67, -78, -22, 3, 5, 10 };
        MyArrayList intList = new MyArrayList();
        
        // numInts should be 0 when no elements
        assertEquals(intList.size(), 0); 
        
        // Adding elements to the array
        for (int i = 0; i < ints.length; ++i) {
            intList.add(ints[i]);
        }
        
        // numInts should be 4
        assertEquals(intList.size(), 4); 
        
        // Adding elements to the array        
        for (int i = 0; i < ints2.length; ++i) {
            intList.add(ints2[i]);
        }
        
        // numInts should be 11
        assertEquals(intList.size(), 11);       
    }
    
    /**
     * Tests the arraySize() + private resize() methods of MyArrayList class with various cases for the correct:
     *      return of the internal array's size, 
     *      adjustment of the size (+10 empty elements) of the internal array when it gets full,
     *      adjustment of the size of the internal array if emptyCount() > 10.
     */
    @Test
    @DisplayName("Test of arraySize() + private resize() methods")
    public void testOfArraySizeAndResize() {
        // Declaration and initialization of test variables
        int[] ints  = { 838, 22, 77, -31, 0, 67, -78, -22, 3 };
        MyArrayList intList = new MyArrayList();
        
        // Internal array's size should be 10 in the beginning
        assertEquals(intList.arraySize(), 10); 
        
        // Adding elements to the array
        for (int i = 0; i < ints.length; ++i) {
            intList.add(ints[i]);
        }
        
        // Internal array's size should still be 10 as there is still place for 1 element
        assertEquals(intList.arraySize(), 10); 
        
        // Adding elements to the array        
        intList.add(58);
        
        // Internal array's size should still be 20 as the array got full
        assertEquals(intList.arraySize(), 20); 
        
        // Removing one element
        intList.remove(0);
        
        // Internal array's size should shrink so that there are 10 empty elements
        assertEquals(intList.arraySize(), 19);
    }
    
    /**
     * Tests the emptyCount() method of MyArrayList class with various cases for the correct
     *      return of the number of empty elements in the internal array.
     */
    @Test
    @DisplayName("Test of emptyCount() method")
    public void testOfEmptyCount() {
        // Declaration and initialization of test variables
        int[] ints  = { 838, 22, 77, -31, 0, 67, -78, -22, 3 };
        MyArrayList intList = new MyArrayList();
        
        // Internal array should have 10 empty elements
        assertEquals(intList.emptyCount(), 10); 
        
        // Adding elements to the array
        for (int i = 0; i < ints.length; ++i) {
            intList.add(ints[i]);
        }
        
        // Internal array should have 1 empty element
        assertEquals(intList.emptyCount(), 1); 
        
        // Adding elements to the array        
        intList.add(58);
        
        // Internal array should have 10 empty elements
        assertEquals(intList.emptyCount(), 10); 
        
        // Removing one element
        intList.remove(0);
        
        // Internal array should have 10 empty elements
        assertEquals(intList.emptyCount(), 10); 
    }
    
    /**
     * Tests the overriden toString() method of MyArrayList class with various cases for the correct: 
     *      return of the list of int as a string with values separated by a single space, 
     *      return an empty string when the list is empty.
     */
    @Test
    @DisplayName("Test of toString() method")
    public void testOfToString() {
        // Declaration and initialization of test variables
        int[] ints  = { 838, 22, 77, -31, 0, 67, -78, -22, 3 };
        String str1 = "[ 838 22 77 -31 0 67 -78 -22 3 ]";
        String str2 = "[ 838 22 77 -31 0 67 -78 -22 3 58 ]";
        String str3 = "[ 22 77 -31 0 67 -78 -22 3 58 ]";
        MyArrayList intList = new MyArrayList();
        
        // Should return an empty string when no elements
        assertEquals(intList.toString(), "[ ]"); 
        
        // Adding elements to the array
        for (int i = 0; i < ints.length; ++i) {
            intList.add(ints[i]);
        }
        
        // Should return a string of current elements separated by a single space
        assertEquals(intList.toString(), str1);  
        
        // Adding elements to the array        
        intList.add(58);
        
        // Should return a string of current elements separated by a single space
        assertEquals(intList.toString(), str2); 
        
        // Removing one element
        intList.remove(0);
        
        // Should return a string of current elements separated by a single space
        assertEquals(intList.toString(), str3);  
    }
    
    /**
     * Tests the overriden next() + reset() methods of MyArrayList class with various cases for the correct:
     *      reset of the internal private counter value to 0, 
     *      return of the list element identified by the internal private counter, 
     *      incrementation of the internal private counter after the return value is determined, 
     *      throw  of the IndexOutOfBoundsException exception with its message when the end of the list is reached.
     */
    @Test
    @DisplayName("Test of next() + reset() methods")
    public void testOfNextAndReset() {
        // Declaration and initialization of test variables
        int[] ints  = { 838, 22, 77 };
        MyArrayList intList = new MyArrayList();
        
        // Throws IndexOutOfBoundsException when no elements
        assertEquals("End of stored data is reached.", assertThrows(IndexOutOfBoundsException.class, () -> intList.next()).getMessage());
        
        // Add elements
        for (int i = 0; i < ints.length; ++i) {
            intList.add(ints[i]);
        }
        
        // Assertion statements 
        assertEquals(intList.next(), 838); // counter = 0 -> first element
        assertEquals(intList.next(), 22);  // counter = 1 -> second element
        assertEquals(intList.next(), 77);  // counter = 2 -> third element
        
        // Throws IndexOutOfBoundsException when end of the list is reached
        assertEquals("End of stored data is reached.", assertThrows(IndexOutOfBoundsException.class, () -> intList.next()).getMessage());

        // Append 1 more element and test if the counter was not incremented
        intList.add(4);
        assertEquals(intList.next(), 4);   // counter = 3 -> fourth element
        
        // Throws IndexOutOfBoundsException when end of the list is reached
        assertEquals("End of stored data is reached.", assertThrows(IndexOutOfBoundsException.class, () -> intList.next()).getMessage());
    
        // Reset counter to 0
        intList.reset();
        
        // Assertion statements - after counter was reset to 0
        assertEquals(intList.next(), 838); // counter = 0 -> first element
    }
}