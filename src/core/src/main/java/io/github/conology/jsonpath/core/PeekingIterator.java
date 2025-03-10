package io.github.conology.jsonpath.core;

import java.util.Iterator;
import java.util.function.Consumer;

public class PeekingIterator<T> implements Iterator<T> {

    private final Iterator<T> wrapped;
    private T peeked = null;

    private PeekingIterator(Iterator<T> wrapped) {
        this.wrapped = wrapped;
    }

    public static <T> PeekingIterator<T> of(Iterator<T> wrapped) {
        return new PeekingIterator<>(wrapped);
    }

    public T peek() {
        if (peeked == null) {
            peeked = wrapped.next();
        }
        return peeked;
    }


    @Override
    public T next() {
        peek();
        var next = peeked;
        peeked = null;
        return next;
    }

    @Override
    public boolean hasNext() {
        return peeked != null || wrapped.hasNext();
    }

    @Override
    public void remove() {
        wrapped.remove();
    }

    @Override
    public void forEachRemaining(Consumer<? super T> action) {
        wrapped.forEachRemaining(action);
    }
}
