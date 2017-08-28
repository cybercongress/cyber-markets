package fund.cyber.markets.common;

public class CircularQueue<T> {

    private final int maxSize;

    private T[] elements;
    private int start = 0;
    private int end = 0;

    private boolean isFull = false;

    public CircularQueue(int maxSize) {
        this.maxSize = maxSize;
        elements = (T[]) new Object[maxSize];

    }


    public void addNext(T element) {

        elements[end++] = element;

        if (isFull) start++;
        if (start >= maxSize) start = 0;
        if (end >= maxSize) end = 0;
        if (end == start) isFull = true;
    }

    public T[] getElements() {
        return elements;
    }

    public T getElement(int indexInQueue) {
        if (start + indexInQueue < 10) {
            return elements[start+indexInQueue];
        } else {
            return elements[start+indexInQueue-10];
        }
    }

}
