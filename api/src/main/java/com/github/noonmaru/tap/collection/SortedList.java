/*
 * Copyright (c) 2020 Noonmaru
 *
 *  Licensed under the General Public License, Version 3.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/gpl-3.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.noonmaru.tap.collection;

import java.util.*;

public class SortedList<E> extends AbstractList<E> implements RandomAccess, Cloneable {

    final ArrayList<E> list;

    final Comparator<E> comparator;

    public SortedList() {
        this.list = new ArrayList<>();
        this.comparator = null;
    }

    public SortedList(Comparator<E> comparator) {
        this.list = new ArrayList<>();
        this.comparator = comparator;
    }

    public SortedList(int initialCapacity) {
        this.list = new ArrayList<>(initialCapacity);
        this.comparator = null;
    }

    public SortedList(int initialCapacity, Comparator<E> comparator) {
        this.list = new ArrayList<>(initialCapacity);
        this.comparator = comparator;
    }

    public SortedList(Collection<E> c) {
        this.list = new ArrayList<>(c);
        this.comparator = null;
        sort();
    }

    public SortedList(Collection<E> c, Comparator<E> comparator) {
        this.list = new ArrayList<>(c);
        this.comparator = comparator;
        sort();
    }

    @Override
    public boolean add(E element) {
        sort();
        binaryAdd(element);

        return true;
    }

    public void sort() {
        this.list.sort(this.comparator);
    }

    public int binaryAdd(E element) {
        if (element == null)
            throw new NullPointerException();

        this.modCount++;

        int size = list.size();

        if (size == 0) {
            list.add(element);
            return 0;
        }

        int index = binarySearch(element);

        if (index < 0)
            index = -(index + 1);

        list.add(index, element);

        return index;
    }

    public boolean binaryRemove(E o) {
        int index = binarySearch(o);

        if (index >= 0) {
            this.list.remove(index);

            return true;
        }

        return false;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        this.list.ensureCapacity(c.size());

        for (E e : c)
            add(e);

        return true;
    }

    @Override
    public void clear() {
        this.list.clear();
    }

    @Override
    public boolean contains(Object o) {
        return this.list.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return this.list.containsAll(c);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object o) {
        return o instanceof SortedList && this.list.equals(((SortedList<E>) o).list);
    }

    public int binarySearch(E element) {
        return Collections.binarySearch(this.list, element, this.comparator);
    }

    @Override
    public E get(int index) {
        return this.list.get(index);
    }

    @Override
    public int hashCode() {
        return this.list.hashCode();
    }

    @Override
    public int indexOf(Object o) {
        return this.list.indexOf(o);
    }

    @Override
    public boolean isEmpty() {
        return this.list.isEmpty();
    }

    @Override
    public int lastIndexOf(Object o) {
        return this.list.lastIndexOf(o);
    }

    @Override
    public ListIterator<E> listIterator(final int index) {
        return new ListIterator<E>() {
            private final ListIterator<E> iterator = SortedList.this.list.listIterator(index);

            @Override
            public void add(E e) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean hasNext() {
                return this.iterator.hasNext();
            }

            @Override
            public boolean hasPrevious() {
                return this.iterator.hasPrevious();
            }

            @Override
            public E next() {
                return this.iterator.next();
            }

            @Override
            public int nextIndex() {
                return this.iterator.nextIndex();
            }

            @Override
            public E previous() {
                return this.iterator.previous();
            }

            @Override
            public int previousIndex() {
                return this.iterator.previousIndex();
            }

            @Override
            public void remove() {
                this.iterator.remove();
            }

            @Override
            public void set(E e) {
                throw new UnsupportedOperationException();
            }

        };
    }

    @Override
    public E remove(int index) {
        return this.list.remove(index);
    }

    @Override
    public boolean remove(Object o) {
        return this.list.remove(o);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return this.list.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return this.list.retainAll(c);
    }

    @Override
    public int size() {
        return this.list.size();
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return Collections.unmodifiableList(super.subList(fromIndex, toIndex));
    }

    @Override
    public Object[] toArray() {
        return this.list.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return this.list.toArray(a);
    }

    @Override
    public String toString() {
        return this.list.toString();
    }

}
