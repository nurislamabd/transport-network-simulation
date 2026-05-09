package datastructures;

/**
 * A minimal stack interface.
 *
 * @author Jon Dahl
 * @version 1
 */
public interface BasicStack<E>
{
    /**
     * Pushes an item onto the top of the stack.
     * 
     * @param item the item to be pushed
     * @return the item pushed onto the stack
     */
    E push(E item);
    
    /**
     * Pops an item from the stack and returns it.  The item popped is
     * removed from the stack.
     * 
     * @return the Object popped from the stack
     * @throws EmptyStackException if the stack is empty
     */
    E pop();

    /**
     * Retrieves, but does not remove, the top of this stack.
     *
     * @return the top of this stack
     * @throws EmptyStackException if the stack is empty
     */
    E peek();
    
    /**
     * Returns the size of the stack.
     * 
     * @return the size of the stack
     */
    int size();
    
    /**
     * Returns whether the stack is empty.
     * 
     * @return true if the stack is empty
     */
    boolean isEmpty();
}