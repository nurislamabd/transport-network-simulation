package datastructures;
import java.util.NoSuchElementException;

/**
 * Behaves like a Queue (simplified version) with methods like
 * add(), poll(), remove(), size(), peek(), isEmpty(), etc.
 *
 * @author Nuris Abdyldaev
 * @version 04/26/2026
 */
public class MyQueue<E> implements BasicQueue<E>
{
    private QueueNode<E> head;
    private QueueNode<E> tail;
    private int size;
    
    /**
     * Constructs an empty queue.
     */
    public MyQueue() {
        head = null;
        tail = null;
        size = 0;
    }
    
    /**
     * Getter for head (for log purposes)
     * 
     * @return  head
     */
    public QueueNode<E> getHead() 
    {
        return head;
    }
    
    @Override
    public boolean isEmpty(){
        return head == null;
    }

    @Override
    public E peek(){
        if (head == null) return null;
        return (E) head.data;
    }

    @Override
    public E poll(){
        // if empty, throw an exception, otherwise remove head and return it
        if (head == null) return null;
        E ret = (E) head.data;
        head = head.next;
        if (head == null) tail = null;
        size--;
        
        return ret;
    } 
    
    @Override
    public E remove(){
        // if empty, throw an exception, otherwise return poll()
        if (head == null) throw new NoSuchElementException();
        return poll();
    } 

    @Override
    public boolean add(E item){
        // nulls not allowed in queue
        if (item == null) throw new NoSuchElementException();
        
        // add and increment size
        QueueNode<E> newNode = new QueueNode<E>(item);
        if (head != null) {
            tail.next = newNode;
            tail = newNode;
        } else {
            head = newNode;
            tail = newNode;
        }
        
        size++;
        return true;
    }

    @Override
    public int size(){
        return size;
    }
}

