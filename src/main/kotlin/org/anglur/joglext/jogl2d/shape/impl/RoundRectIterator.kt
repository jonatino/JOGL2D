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
import java.awt.geom.RoundRectangle2D
import java.util.*

/**
 * Created by Jonathan on 9/30/2016.
 */
object RoundRectIterator : PathIterator {
	
	private lateinit var r: Rectangle2D
	private var affine: AffineTransform? = null
	
	private var x: Double = 0.toDouble()
	private var y: Double = 0.toDouble()
	private var w: Double = 0.toDouble()
	private var h: Double = 0.toDouble()
	private var aw: Double = 0.toDouble()
	private var ah: Double = 0.toDouble()
	private var index: Int = 0
	
	fun set(r: RoundRectangle2D, affine: AffineTransform?) = apply {
		this.index = 0
		this.x = r.x
		this.y = r.y
		this.w = r.width
		this.h = r.height
		this.aw = Math.min(w, Math.abs(r.arcWidth))
		this.ah = Math.min(h, Math.abs(r.arcHeight))
		this.affine = affine
		if (aw < 0 || ah < 0) {
			// Don't draw anything...
			index = ctrlpts.size
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
	override fun isDone() = index >= ctrlpts.size
	
	/**
	 * Moves the iterator to the next segment of the path forwards
	 * along the primary direction of traversal as long as there are
	 * more points in that direction.
	 */
	override fun next() {
		index++
	}
	
	private val angle = Math.PI / 4.0
	private val a = 1.0 - Math.cos(angle)
	private val b = Math.tan(angle)
	private val c = Math.sqrt(1.0 + b * b) - 1 + a
	private val cv = 4.0 / 3.0 * a * b / c
	private val acv = (1.0 - cv) / 2.0
	
	// For each array:
	//     4 values for each point {v0, v1, v2, v3}:
	//         point = (x + v0 * w + v1 * arcWidth,
	//                  y + v2 * h + v3 * arcHeight);
	private val ctrlpts = arrayOf(doubleArrayOf(0.0, 0.0, 0.0, 0.5), doubleArrayOf(0.0, 0.0, 1.0, -0.5), doubleArrayOf(0.0, 0.0, 1.0, -acv, 0.0, acv, 1.0, 0.0, 0.0, 0.5, 1.0, 0.0), doubleArrayOf(1.0, -0.5, 1.0, 0.0), doubleArrayOf(1.0, -acv, 1.0, 0.0, 1.0, 0.0, 1.0, -acv, 1.0, 0.0, 1.0, -0.5), doubleArrayOf(1.0, 0.0, 0.0, 0.5), doubleArrayOf(1.0, 0.0, 0.0, acv, 1.0, -acv, 0.0, 0.0, 1.0, -0.5, 0.0, 0.0), doubleArrayOf(0.0, 0.5, 0.0, 0.0), doubleArrayOf(0.0, acv, 0.0, 0.0, 0.0, 0.0, 0.0, acv, 0.0, 0.0, 0.0, 0.5), doubleArrayOf())
	private val types = intArrayOf(PathIterator.SEG_MOVETO, PathIterator.SEG_LINETO, PathIterator.SEG_CUBICTO, PathIterator.SEG_LINETO, PathIterator.SEG_CUBICTO, PathIterator.SEG_LINETO, PathIterator.SEG_CUBICTO, PathIterator.SEG_LINETO, PathIterator.SEG_CUBICTO, PathIterator.SEG_CLOSE)
	
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
	 * @see .SEG_MOVETO
	 
	 * @see .SEG_LINETO
	 
	 * @see .SEG_QUADTO
	 
	 * @see .SEG_CUBICTO
	 
	 * @see .SEG_CLOSE
	 */
	override fun currentSegment(coords: FloatArray): Int {
		if (isDone) {
			throw NoSuchElementException("roundrect iterator out of bounds")
		}
		val ctrls = ctrlpts[index]
		var nc = 0
		var i = 0
		while (i < ctrls.size) {
			coords[nc++] = (x + ctrls[i + 0] * w + ctrls[i + 1] * aw).toFloat()
			coords[nc++] = (y + ctrls[i + 2] * h + ctrls[i + 3] * ah).toFloat()
			i += 4
		}
		affine?.transform(coords, 0, coords, 0, nc / 2)
		
		return types[index]
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
	 * @see .SEG_MOVETO
	 
	 * @see .SEG_LINETO
	 
	 * @see .SEG_QUADTO
	 
	 * @see .SEG_CUBICTO
	 
	 * @see .SEG_CLOSE
	 */
	override fun currentSegment(coords: DoubleArray): Int {
		if (isDone) {
			throw NoSuchElementException("roundrect iterator out of bounds")
		}
		val ctrls = ctrlpts[index]
		var nc = 0
		var i = 0
		while (i < ctrls.size) {
			coords[nc++] = x + ctrls[i + 0] * w + ctrls[i + 1] * aw
			coords[nc++] = y + ctrls[i + 2] * h + ctrls[i + 3] * ah
			i += 4
		}
		affine?.transform(coords, 0, coords, 0, nc / 2)
		
		return types[index]
	}
	
	
	operator fun invoke(rect: RoundRectangle2D, affine: AffineTransform? = null) = set(rect, affine)
	
}