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

package org.anglur.joglext.jogl2d

import java.awt.RenderingHints.Key

/**
 * Rendering hints for the GLG2D library that customize the behavior.
 */
object GLG2DRenderingHints {
	
	private var keyId = 384739478
	
	/**
	 * Never clear the texture cache.
	 */
	val VALUE_CLEAR_TEXTURES_CACHE_NEVER = Any()
	
	/**
	 * Clear the texture cache before each paint. This allows images to be cached
	 * within a paint cycle.
	 */
	val VALUE_CLEAR_TEXTURES_CACHE_EACH_PAINT = Any()
	
	/**
	 * Use the default texture cache policy.
	 */
	val VALUE_CLEAR_TEXTURES_CACHE_DEFAULT = VALUE_CLEAR_TEXTURES_CACHE_NEVER
	
	/**
	 * Specifies when to clear the texture cache. Each image to be painted must be
	 * turned into a texture and then the texture is re-used whenever that image
	 * is seen. Values can be one of
	 *
	 *
	 *
	 *  * [.VALUE_CLEAR_TEXTURES_CACHE_DEFAULT]
	 *  * [.VALUE_CLEAR_TEXTURES_CACHE_NEVER]
	 *  * [.VALUE_CLEAR_TEXTURES_CACHE_EACH_PAINT]
	 *  * any integer for the maximum size of the cache
	 *
	 */
	val KEY_CLEAR_TEXTURES_CACHE: Key = object : Key(keyId++) {
		override fun isCompatibleValue(`val`: Any): Boolean {
			return `val` === VALUE_CLEAR_TEXTURES_CACHE_DEFAULT ||
					`val` === VALUE_CLEAR_TEXTURES_CACHE_EACH_PAINT ||
					`val` === VALUE_CLEAR_TEXTURES_CACHE_NEVER ||
					`val` is Int
		}
	}
	
}
