package datastructures;

/**
 * Behaves like ArrayList class (simplified version) with methods like
 * add(), remove(), size(), toString(), clear(), isEmpty(), etc.
 * 
 * 
 * @author Nuris Abdyldaev
 * @version 04/29/2026
 */
public class MyArrayList<E>
{
    // Instance variables 
    private E[] arrayData;
    private int size;
    private int counter;

    /**
     * Constructor for objects of class MyArrayList
     */
    public MyArrayList()
    {
        // Initialise variables
        // The size of the stored array always starts with 10
        arrayData  = (E[]) new Object[10];
        size       = 0;
        counter    = 0;
    }
    
    /**
     * Speacial constructor for objects of class MyArrayList.
     * Because I will be using known number of objects when creating them
     * I can save time by defining the start size for MyArrayList.
     * 
     * @param  startSize  start size of the internal array
     */
    public MyArrayList(int startSize)
    {
        // Initialise variables
        // The size of the stored array always starts with startSize
        arrayData  = (E[]) new Object[startSize + 1];
        size       = 0;
        counter    = 0;
    }
    
    /**
     * Resizes the array size so that it has exactly 10 empty
     * elements.
     */
    private void resize() {
       // Create an array with new size 
       E[] newArray = (E[]) new Object[size + 10];
    
       // Copy items to the new array
       for (int i = 0; i < size; ++i) {
           newArray[i] = arrayData[i];
       }
    
       // Assign the newArray to arrayData
       arrayData = newArray;
    }
    
    /**
     * Appends the input object (e) to the end of the list and 
     * resizes the internal array if needed.
     * 
     * @param  e an object to be appended
     */
    public void add(E e) {
        // Append e to the array
        arrayData[size] = e;
        
        // Increment the size
        ++size;
        
        // Add 10 empty elements if the array is full
        if (arrayData.length == size) {
            resize();
        }
    }

    /**
     * Sets item at index to e
     * 
     * @param   index  index to be set to e
     * @param   e      value to replace at i
     * @throws  java.lang.IndexOutOfBoundsException if the index is out of range
     */
    public void set(int index, E e) {
        // index out of range
        if (index > size - 1 || index < 0) {
            throw new IndexOutOfBoundsException("Index is out of range");
        }
        
        arrayData[index] = e;
    }
    
    /**
     * Inserts the input object (e) to the location specified 
     * by the index, moving the surrounding objects, and 
     * resizing the internal array if needed.
     * 
     * @param   e      an object to be inserted
     * @param   index  an index where e is to be inserted
     * @throws  java.lang.IndexOutOfBoundsException if the index is out of range
     */
    public void add(int index, E e) throws IndexOutOfBoundsException {
        // Check if index is within the range
        if (index > size || index < 0) {
            throw new IndexOutOfBoundsException("Index is out of range");
        }
        
        // Move the objects to the right from the given index
        for (int i = size; i > index; --i) {
            arrayData[i] = arrayData[i - 1];
        }
        
        // Insert e to the array
        arrayData[index] = e;
        
        // Increment the size
        ++size;
        
        // Add 10 empty elements if the array is full
        if (arrayData.length == size) {
            resize();
        }
    }
    
    /**
     * Returns the object identified by the location specified 
     * by the index. 
     * 
     * @param  index an index where return object is found
     * @return       an int value in the array on the 
     *               specified index
     * @throws java.lang.IndexOutOfBoundsException if the index is out of range             
     */
    public E get(int index) throws IndexOutOfBoundsException {
        // Check if index is within the range
        if (index > size - 1 || index < 0) {
            throw new IndexOutOfBoundsException("Index is out of range");
        }
        
        // Return value at a given index
        return arrayData[index];
    }
    
    /**
     * Returns the list to its initial state, with no data.
     */
    public void clear() {
        // Reinitializes values of the variables to 0 (10 empty elements for the array)
        arrayData = (E[]) new Object[10];
        size      = 0;
        counter   = 0;
    }
    
    /**
     * Indicates if the list is empty or contains objects.
     * 
     * @return   true if the list is empty
     */
    public boolean isEmpty() {
        // Return true if the size is 0
        if (size == 0) {
            return true;
        }
        
        // Return false if the size is not 0
        return false;
    }
    
    /**
     * Removes the object from the location specified by 
     * the index, returns the removed object, and adjusts the 
     * surrounding objects and size of the internal array.
     * 
     * @param   index  an index of the object to be removed and 
     *                 returned
     * @return         an object removed from the array
     * @throws  java.lang.IndexOutOfBoundsException  if the index is out of range
     */
    public E remove(int index) throws IndexOutOfBoundsException {
        // Check if index is within the range
        if (index > size - 1 || index < 0) {
            throw new IndexOutOfBoundsException("Index is out of range");
        }
        
        // Return variable 
        E removedVal = arrayData[index];
        arrayData[index] = null;
        
        // Move elements after the removedVal to the left
        for (int i = index; i < size - 1; ++i) {
            arrayData[i] = arrayData[i + 1];
        }
        
        // Decrement the size value
        --size;
        
        // Resize the array if there are more than 10 empty elements
        if (emptyCount() > 10) {
            resize();
        }
        
        // Return the removed value
        return removedVal;
    }
    
    /**
     * Returns the number of objects in the list.
     * 
     * @return   an int number of objects in the list
     */
    public int size() {
        return size;
    }
    
    /**
     * Returns the size of an internal array.
     * 
     * @return   the size of an internal array
     */ 
    public int arraySize() {
        return arrayData.length;
    }
    
    /**
     * Returns the number of empty elements in the internally 
     * stored array.
     * 
     * @return   an int number of empty elements in the 
     *           internally stored array
     */
    public int emptyCount() {
        return arrayData.length - size;
    }
    
    /**
     * Returns the list of E objects as a string with values 
     * separated by a single space.
     * 
     * @return   a String containing all the objects of the 
     *           array list separated by a single space
     */
    @Override
    public String toString() {
        // Initilize the string for the output
        String out = "[ ";
        
        // Check if the array is empty
        if (isEmpty()) {
            return out + "]";
        }
        
        // Loop through the array to insert elements separated by a space
        for (int i = 0; i < size; ++i) {
            out = out + arrayData[i].toString() + " ";
        }
        
        // Return the string with all the array values
        return out + "]";
    }
    
    /**
     * Resets an internal private counter to zero.
     */
    public void reset() {
        counter = 0;
    }
    
    /**
     * Returns the list object identified by the internal 
     * private counter and increments that counter after object 
     * is determined.
     * 
     * @return   the list object identified by the internal private counter
     * @throws   java.lang.IndexOutOfBoundsException if the 
     *           end of the list is reached
     */
    public E next() throws IndexOutOfBoundsException {
        // Check if the end of the list is not reached
        if (counter == size) {
            throw new IndexOutOfBoundsException("End of stored data is reached.");
        }
        
        // Get the value specified by the counter
        E temp = arrayData[counter];
        
        // Increment the counter
        ++counter;
        
        // Return the "next" value
        return temp;
    }
}