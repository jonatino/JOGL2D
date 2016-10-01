package org.anglur.joglext.cacheable

import java.util.*

object CachedFloatArray {
	
	/**
	 * The resource map cache, mapping size in bytes to memory.
	 */
	private val map = ThreadLocal.withInitial { HashMap<Int, FloatArray?>(32) }
	
	/**
	 * Returns a zeroed-out FloatArray of the specified size in bytes.
	 *
	 * @param size The desired amount of bytes of the FloatArray.
	 */
	fun cached(size: Int) = map.get().getOrPut(size, { FloatArray(size) })!!
	
	operator fun invoke(size: Int) = cached(size)
	
}