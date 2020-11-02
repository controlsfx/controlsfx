/*
 * Copyright (c) 2018, ControlsFX
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of ControlsFX, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL CONTROLSFX BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package impl.org.controlsfx.collections;

import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableListBase;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Minimum implementation of com.sun.javafx.scene.control.ReadOnlyUnbackedObservableList
 */
public abstract class ReadOnlyUnbackedObservableList<E> extends ObservableListBase<E> {

    public void callObservers(Change<E> c) {
        fireChange(c);
    }
    
    @Override public abstract E get(int i);

    @Override public abstract int size();

    @Override public int indexOf(Object o) {
        if (o == null) return -1;

        for (int i = 0; i < size(); i++) {
            Object obj = get(i);
            if (o.equals(obj)) return i;
        }

        return -1;
    }

    @Override public int lastIndexOf(Object o) {
        if (o == null) return -1;

        for (int i = size() - 1; i >= 0; i--) {
            Object obj = get(i);
            if (o.equals(obj)) return i;
        }

        return -1;
    }

    @Override public boolean contains(Object o) {
        return indexOf(o) != -1;
    }

    @Override public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (! contains(o)) {
                return false;
            }
        }
        return true;
    }

    @Override public boolean isEmpty() {
        return size() == 0;
    }

    @Override public ListIterator<E> listIterator() {
        return new ReadOnlyUnbackedObservableList.SelectionListIterator<>(this);
    }

    @Override public ListIterator<E> listIterator(int index) {
        return new ReadOnlyUnbackedObservableList.SelectionListIterator<>(this, index);
    }

    @Override
    public Iterator<E> iterator() {
        return new ReadOnlyUnbackedObservableList.SelectionListIterator<>(this);
    }

    /**
     * NOTE: This method does not fulfill the subList contract from Collections,
     * it simply returns a list containing the values in the given range.
     */
    @Override public List<E> subList(final int fromIndex, final int toIndex) {

        if (fromIndex < 0 || toIndex > size() || fromIndex > toIndex) {
            throw new IndexOutOfBoundsException("[ fromIndex: " + fromIndex + ", toIndex: " + toIndex + ", size: " + size() + " ]");
        }

        final List<E> outer = this;
        return new ReadOnlyUnbackedObservableList<>() {

            @Override
            public E get(int i) {
                return outer.get(i + fromIndex);
            }

            @Override
            public int size() {
                return toIndex - fromIndex;
            }
        };
    }

    @Override
    public Object[] toArray() {
        Object[] arr = new Object[size()];
        for (int i = 0; i < size(); i++) {
            arr[i] = get(i);
        }
        return arr;
    }

    @SuppressWarnings({"unchecked", "SuspiciousSystemArraycopy"})
    @Override
    public <T> T[] toArray(T[] a) {
        Object[] elementData = toArray();
        int size = elementData.length;

        if (a.length < size) {
            return (T[]) Arrays.copyOf(elementData, size, a.getClass());
        }

        System.arraycopy(elementData, 0, a, 0, size);

        if (a.length > size) {
            a[size] = null;
        }
        return a;
    }

    @Override
    public String toString() {
        Iterator<E> i = iterator();
        if (! i.hasNext())
            return "[]";

        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (;;) {
            E e = i.next();
            sb.append(e == this ? "(this Collection)" : e);
            if (! i.hasNext())
                return sb.append(']').toString();
            sb.append(", ");
        }
    }

    @Override
    public boolean add(E e) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void add(int index, E element) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean addAll(E... elements) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public E set(int index, E element) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean setAll(Collection<? extends E> col) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean setAll(E... elements) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public E remove(int index) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void remove(int from, int to) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean removeAll(E... elements) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean retainAll(E... elements) {
        throw new UnsupportedOperationException("Not supported.");
    }

    private static class SelectionListIterator<E> implements ListIterator<E> {
        
        private int index;
        private final ReadOnlyUnbackedObservableList<E> list;

        public SelectionListIterator(ReadOnlyUnbackedObservableList<E> list) {
            this(list, 0);
        }

        public SelectionListIterator(ReadOnlyUnbackedObservableList<E> list, int index) {
            this.list = list;
            this.index = index;
        }

        @Override
        public boolean hasNext() {
            return index < list.size();
        }

        @Override
        public E next() {
            return list.get(index++);
        }

        @Override
        public boolean hasPrevious() {
            return index > 0;
        }

        @Override
        public E previous() {
            return list.get(index--);
        }

        @Override
        public int nextIndex() {
            return index + 1;
        }

        @Override
        public int previousIndex() {
            return index - 1;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void set(E e) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void add(E e) {
            throw new UnsupportedOperationException("Not supported.");
        }
    }
}

