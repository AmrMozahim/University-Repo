public class Stack<T> {
    private Queue<T> queue;

    public Stack(int capacity) {
        queue = new Queue<>(capacity);
    }

    public void push(T item) {
        queue.enqueue(item);
        int size = queue.size();
        for (int i = 0; i < size - 1; i++) {
            queue.enqueue(queue.dequeue());
        }
    }

    public T pop() {
        if (isEmpty()) throw new RuntimeException("Stack is empty");
        return queue.dequeue();
    }

    public T peek() {
        if (isEmpty()) throw new RuntimeException("Stack is empty");
        return queue.peek();
    }

    public boolean isEmpty() { return queue.isEmpty(); }
    public int size() { return queue.size(); }
}