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