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
import java.awt.geom.Line2D
import java.awt.geom.PathIterator
import java.util.*


/**
 * Created by Jonathan on 9/30/2016.
 */
object LineIterator : PathIterator {
	
	private lateinit var line: Line2D
	private var affine: AffineTransform? = null
	
	private var index: Int = 0
	
	fun set(line: Line2D, affine: AffineTransform?) = apply {
		this.index = 0
		this.line = line
		this.affine = affine
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
	override fun isDone() = index > 1
	
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
			throw NoSuchElementException("line iterator out of bounds")
		}
		val type: Int
		if (index === 0) {
			coords[0] = line.getX1().toFloat()
			coords[1] = line.getY1().toFloat()
			type = PathIterator.SEG_MOVETO
		} else {
			coords[0] = line.getX2().toFloat()
			coords[1] = line.getY2().toFloat()
			type = PathIterator.SEG_LINETO
		}
		affine?.transform(coords, 0, coords, 0, 1)
		
		return type
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
			throw NoSuchElementException("line iterator out of bounds")
		}
		val type: Int
		if (index === 0) {
			coords[0] = line.getX1()
			coords[1] = line.getY1()
			type = PathIterator.SEG_MOVETO
		} else {
			coords[0] = line.getX2()
			coords[1] = line.getY2()
			type = PathIterator.SEG_LINETO
		}
		affine?.transform(coords, 0, coords, 0, 1)
		
		return type
	}
	
	
	operator fun invoke(line: Line2D, affine: AffineTransform? = null) = set(line, affine)
	
}