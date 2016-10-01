/*
 *    Copyright 2016 Jonathan Beaudoin <https://github.com/Jonatino>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.anglur.joglext.cacheable

@Suppress("UNCHECKED_CAST")
open class CachedList<out E>(val minIndex: Int, val capacity: Int) : Iterable<E> {
	
	private var arr = arrayOfNulls<Any>(capacity)
	
	private var size = 0
	private var highest: Int = 0
	private var dirty = false
	
	constructor(capacity: Int) : this(0, capacity)
	
	operator fun get(index: Int) = arr[index] as E
	
	operator fun set(index: Int, element: @UnsafeVariance E?): E {
		val previous = arr[index]
		if (previous == element) return previous as E
		
		arr[index] = element
		if (previous == null && element != null) {
			size++
			if (highest < index) {
				highest = index
			}
		} else if (previous != null && element == null) {
			size--
			if (highest == index) {
				highest--
			}
		}
		dirty = true
		return previous as E
	}
	
	fun add(element: @UnsafeVariance E): Int {
		val index = nextIndex()
		set(index, element)
		return index
	}
	
	fun remove(element: @UnsafeVariance E) {
		for (i in minIndex..highest) {
			if (element!!.equals(arr[i])) {
				set(i, null)
				return
			}
		}
	}
	
	operator fun contains(element: @UnsafeVariance E): Boolean {
		for (e in iterator()) {
			if (element!!.equals(e)) {
				return true
			}
		}
		
		return false
	}
	
	inline fun forEach(action: (E) -> Unit): Unit {
		for (e in iterator()) {
			if (e != null)
				action(e)
		}
	}
	
	open fun clear() {
		for (i in minIndex..arr.size - 1)
			arr[i] = null
		size = 0
		dirty = true
	}
	
	fun size() = size
	
	fun isDirty() = dirty
	
	fun clean() = apply { dirty = false }
	
	fun nextIndex(): Int {
		for (i in minIndex..arr.size - 1) {
			if (null == arr[i]) {
				return i
			}
		}
		throw IllegalStateException("Out of indices!")
	}
	
	override operator fun iterator(): Iterator<E> {
		iterator.pointer = minIndex
		return iterator
	}
	
	private val iterator = IndexerIterator()
	
	private inner class IndexerIterator : Iterator<E> {
		
		var pointer: Int = 0
		
		override fun hasNext() = size > 0 && pointer <= highest
		
		override fun next(): E {
			val o = arr[pointer++]
			if (o == null && hasNext()) {
				return next()
			}
			return o as E
		}
		
		fun remove() = set(pointer, null)
		
	}
	
}