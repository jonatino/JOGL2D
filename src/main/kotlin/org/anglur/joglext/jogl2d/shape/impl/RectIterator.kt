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

package org.anglur.joglext.jogl2d.shape.impl

import java.awt.geom.AffineTransform
import java.awt.geom.PathIterator
import java.awt.geom.Rectangle2D
import java.util.*

/**
 * Created by Jonathan on 9/30/2016.
 */
object RectIterator : PathIterator {
	
	private lateinit var r: Rectangle2D
	private var affine: AffineTransform? = null
	
	private var x: Double = 0.toDouble()
	private var y: Double = 0.toDouble()
	private var w: Double = 0.toDouble()
	private var h: Double = 0.toDouble()
	private var index: Int = 0
	
	fun set(r: Rectangle2D, affine: AffineTransform?) = apply {
		this.index = 0
		this.r = r
		this.x = r.x
		this.y = r.y
		this.w = r.width
		this.h = r.height
		this.affine = affine
		if (w < 0 || h < 0) {
			index = 6
		}
	}
	
	/**
	 * Return the winding rule for determining the insideness of the
	 * path.
	 * @see #WIND_EVEN_ODD
	 * @see #WIND_NON_ZERO
	 */
	override fun getWindingRule() = PathIterator.WIND_NON_ZERO
	
	/**
	 * Tests if there are more points to read.
	 * @return true if there are more points to read
	 */
	override fun isDone() = index > 5
	
	/**
	 * Moves the iterator to the next segment of the path forwards
	 * along the primary direction of traversal as long as there are
	 * more points in that direction.
	 */
	override fun next() {
		index++
	}
	
	/**
	 * Returns the coordinates and type of the current path segment in
	 * the iteration.
	 * The return value is the path segment type:
	 * SEG_MOVETO, SEG_LINETO, SEG_QUADTO, SEG_CUBICTO, or SEG_CLOSE.
	 * A float array of length 6 must be passed in and may be used to
	 * store the coordinates of the point(s).
	 * Each point is stored as a pair of float x,y coordinates.
	 * SEG_MOVETO and SEG_LINETO types will return one point,
	 * SEG_QUADTO will return two points,
	 * SEG_CUBICTO will return 3 points
	 * and SEG_CLOSE will not return any points.
	 * @see #SEG_MOVETO
	 * @see #SEG_LINETO
	 * @see #SEG_QUADTO
	 * @see #SEG_CUBICTO
	 * @see #SEG_CLOSE
	 */
	override fun currentSegment(coords: FloatArray): Int {
		if (isDone) {
			throw NoSuchElementException("rect iterator out of bounds")
		}
		if (index == 5) {
			return PathIterator.SEG_CLOSE
		}
		coords[0] = x.toFloat()
		coords[1] = y.toFloat()
		if (index == 1 || index == 2) {
			coords[0] += w.toFloat()
		}
		if (index == 2 || index == 3) {
			coords[1] += h.toFloat()
		}
		affine?.transform(coords, 0, coords, 0, 1)
		
		return (if (index == 0) PathIterator.SEG_MOVETO else PathIterator.SEG_LINETO)
	}
	
	/**
	 * Returns the coordinates and type of the current path segment in
	 * the iteration.
	 * The return value is the path segment type:
	 * SEG_MOVETO, SEG_LINETO, SEG_QUADTO, SEG_CUBICTO, or SEG_CLOSE.
	 * A double array of length 6 must be passed in and may be used to
	 * store the coordinates of the point(s).
	 * Each point is stored as a pair of double x,y coordinates.
	 * SEG_MOVETO and SEG_LINETO types will return one point,
	 * SEG_QUADTO will return two points,
	 * SEG_CUBICTO will return 3 points
	 * and SEG_CLOSE will not return any points.
	 * @see #SEG_MOVETO
	 * @see #SEG_LINETO
	 * @see #SEG_QUADTO
	 * @see #SEG_CUBICTO
	 * @see #SEG_CLOSE
	 */
	override fun currentSegment(coords: DoubleArray): Int {
		if (isDone) {
			throw NoSuchElementException("rect iterator out of bounds")
		}
		if (index == 5) {
			return PathIterator.SEG_CLOSE
		}
		coords[0] = x
		coords[1] = y
		if (index == 1 || index == 2) {
			coords[0] += w
		}
		if (index == 2 || index == 3) {
			coords[1] += h
		}
		affine?.transform(coords, 0, coords, 0, 1)
		
		return (if (index == 0) PathIterator.SEG_MOVETO else PathIterator.SEG_LINETO)
	}
	
	
	operator fun invoke(rect: Rectangle2D, affine: AffineTransform? = null) = set(rect, affine)
	
}