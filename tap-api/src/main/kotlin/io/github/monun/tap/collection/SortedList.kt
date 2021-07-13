/*
 * Tap
 * Copyright (C) 2021 Monun
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.monun.tap.collection

import java.util.*

@Suppress("unused")
class SortedList<E> : AbstractList<E>, RandomAccess, Cloneable {
    private val list: ArrayList<E>
    private val comparator: Comparator<E>?

    constructor() {
        list = ArrayList()
        comparator = null
    }

    constructor(comparator: Comparator<E>? = null) {
        list = ArrayList()
        this.comparator = comparator
    }

    constructor(initialCapacity: Int, comparator: Comparator<E>? = null) {
        list = ArrayList(initialCapacity)
        this.comparator = comparator
    }

    constructor(elements: Collection<E>, comparator: Comparator<E>? = null) {
        list = ArrayList(elements)
        this.comparator = comparator
        sort()
    }

    override fun add(element: E): Boolean {
        sort()
        binaryAdd(element)
        return true
    }

    fun sort() {
        Collections.sort(list, comparator)
    }

    fun binaryAdd(element: E): Int {
        modCount++
        val size = list.size
        if (size == 0) {
            list.add(element)
            return 0
        }
        var index = binarySearch(element)
        if (index < 0) index = -(index + 1)
        list.add(index, element)
        return index
    }

    fun binaryRemove(element: E): Boolean {
        val index = binarySearch(element)
        if (index >= 0) {
            list.removeAt(index)
            return true
        }
        return false
    }

    override fun addAll(elements: Collection<E>): Boolean {
        list.ensureCapacity(elements.size)
        for (element in elements) add(element)
        return true
    }

    override fun clear() {
        list.clear()
    }

    override operator fun contains(element: E): Boolean {
        return list.contains(element)
    }

    override fun containsAll(elements: Collection<E>): Boolean {
        return list.containsAll(elements)
    }

    override fun equals(other: Any?): Boolean {
        return other is SortedList<*> && list == other.list
    }

    private fun binarySearch(element: E): Int {
        return Collections.binarySearch(list, element, comparator)
    }

    override fun get(index: Int): E {
        return list[index]
    }

    override fun hashCode(): Int {
        return list.hashCode()
    }

    override fun indexOf(element: E): Int {
        return list.indexOf(element)
    }

    override fun isEmpty(): Boolean {
        return list.isEmpty()
    }

    override fun lastIndexOf(element: E): Int {
        return list.lastIndexOf(element)
    }

    override fun listIterator(index: Int): MutableListIterator<E> {
        return object : MutableListIterator<E> {
            private val iterator = list.listIterator(index)
            override fun add(element: E) {
                throw UnsupportedOperationException()
            }

            override fun hasNext(): Boolean {
                return iterator.hasNext()
            }

            override fun hasPrevious(): Boolean {
                return iterator.hasPrevious()
            }

            override fun next(): E {
                return iterator.next()
            }

            override fun nextIndex(): Int {
                return iterator.nextIndex()
            }

            override fun previous(): E {
                return iterator.previous()
            }

            override fun previousIndex(): Int {
                return iterator.previousIndex()
            }

            override fun remove() {
                iterator.remove()
            }

            override fun set(element: E) {
                throw UnsupportedOperationException()
            }
        }
    }

    override fun removeAt(index: Int): E {
        return list.removeAt(index)
    }

    override fun remove(element: E): Boolean {
        return list.remove(element)
    }

    override fun removeAll(elements: Collection<E>): Boolean {
        return list.removeAll(elements)
    }

    override fun retainAll(elements: Collection<E>): Boolean {
        return list.retainAll(elements)
    }

    override val size: Int
        get() {
            return list.size
        }

    override fun toArray(): Array<Any?> {
        return list.toTypedArray()
    }

    override fun <T> toArray(a: Array<T>): Array<T> {
        return list.toArray(a)
    }

    override fun toString(): String {
        return list.toString()
    }
}