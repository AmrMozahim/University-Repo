public class Queue<T> {
    private T[] items;
    private int front;
    private int back;
    private int count;
    private int capacity;

    @SuppressWarnings("unchecked")
    public Queue(int capacity) {
        this.capacity = capacity;
        items = (T[]) new Object[capacity];
        front = 0;
        back = -1;
        count = 0;
    }

    public void enqueue(T item) {
        if (count == capacity) resize(capacity * 2);
        back = (back + 1) % capacity;
        items[back] = item;
        count++;
    }

    public T dequeue() {
        if (isEmpty()) throw new RuntimeException("Queue is empty");
        T item = items[front];
        items[front] = null;
        front = (front + 1) % capacity;
        count--;
        if (count > 0 && count == capacity / 4) resize(capacity / 2);
        return item;
    }

    public T peek() {
        if (isEmpty()) throw new RuntimeException("Queue is empty");
        return items[front];
    }

    public boolean isEmpty() { return count == 0; }
    public int size() { return count; }

    @SuppressWarnings("unchecked")
    private void resize(int newCapacity) {
        T[] newItems = (T[]) new Object[newCapacity];
        for (int i = 0; i < count; i++) {
            int index = (front + i) % capacity;
            newItems[i] = items[index];
        }
        items = newItems;
        front = 0;
        back = count - 1;
        capacity = newCapacity;
    }
}