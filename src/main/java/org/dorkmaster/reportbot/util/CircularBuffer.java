package org.dorkmaster.reportbot.util;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CircularBuffer<T> {

    // ok so not exactly
    protected ConcurrentLinkedQueue<T> buffer = new ConcurrentLinkedQueue<>();

    protected int maxSize;

    public CircularBuffer(int size) {
        maxSize = size;
    }

    public boolean push(T record) {
        if (maxSize == buffer.size()) {
            buffer.remove(0);
        }
        return buffer.add(record);
    }

    public T peek() {
        return buffer.peek();
    }

    public T pop() {
        return buffer.poll();
    }

    public int size() {
        return buffer.size();
    }

    public Collection<T> asList() {
        return Collections.unmodifiableCollection(buffer);
    }
}
