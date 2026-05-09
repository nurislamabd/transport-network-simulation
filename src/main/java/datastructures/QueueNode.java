package datastructures;

/**
 * Node class for MyQueue class
 */
public class QueueNode<E> {
    // instance vars
    public E data;
    public QueueNode next;
    
    /**
     * Constuctor with next pointing to null
     * 
     * @param  dataValue  data value to be stored
     */
    public QueueNode(E dataValue) {
      this(dataValue, null);
    }
    
    /**
     * Constuctor with next pointing to a node
     * 
     * @param  dataValue  data value to be stored
     * @param  next       next node
     */
    public QueueNode(E dataValue, QueueNode nextNode) {
      data = dataValue;
      next = nextNode;
    }
}