/*
 * Charlatano is a premium CS:GO cheat ran on the JVM.
 * Copyright (C) 2016 Thomas Nappo, Jonathan Beaudoin
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
