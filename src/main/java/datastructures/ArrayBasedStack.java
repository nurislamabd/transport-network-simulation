package datastructures;
import java.util.Arrays;

/**
 * An array based stack. The array doubles in length when needed,
 * but never shrinks.
 *
 * @author Jon Dahl
 * @version 1
 */
public class ArrayBasedStack<E> implements BasicStack<E>
{
    private static final int DEFAULT_SIZE = 10;
    private int top; // index of next available cell in array
    private E[] data; // the array storing the stack

    /**
     * Constructs an empty stack.
     */
    public ArrayBasedStack()
    {
        // Unchecked warning is unavoidable
        data = (E[]) new Object[DEFAULT_SIZE];
    }
    
    @Override
    public String toString(){
        String str = "[ ";
        for (int i = 0; i < top; i++) str += data[i].toString() + " ";
        return str + "]";
    }

    public boolean isEmpty(){
        return top == 0;
    }

    public E peek(){
        if (top == 0) return null;
        return data[top - 1];
    }

    public E pop(){
        if (top == 0) return null;
        E ret = data[--top];
        data[top] = null;
        return ret;
    } 

    public E push(E item){
        checkCapacity();
        data[top++] = item;
        return item;
    }

    public int size(){
        return top;
    }

    private void checkCapacity(){
        if (top == data.length){
            // create a copy of the data array with double the capacity
            data = Arrays.copyOf(data, data.length * 2);
        }
    }
}
