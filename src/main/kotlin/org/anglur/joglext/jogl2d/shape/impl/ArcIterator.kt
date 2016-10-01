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
import java.awt.geom.Arc2D
import java.awt.geom.PathIterator
import java.util.*

object ArcIterator : PathIterator {
	
	private lateinit var a: Arc2D
	private var affine: AffineTransform? = null
	
	private var x: Double = 0.toDouble()
	private var y: Double = 0.toDouble()
	private var w: Double = 0.toDouble()
	private var h: Double = 0.toDouble()
	private var angStRad: Double = 0.toDouble()
	private var increment: Double = 0.toDouble()
	private var cv: Double = 0.toDouble()
	private var arcSegs: Int = 0
	private var lineSegs: Int = 0
	private var index: Int = 0
	
	fun set(a: Arc2D, affine: AffineTransform?) = apply {
		this.index = 0
		this.a = a
		this.w = a.getWidth() / 2
		this.h = a.getHeight() / 2
		this.x = a.getX() + w
		this.y = a.getY() + h
		this.angStRad = -Math.toRadians(a.getAngleStart())
		this.affine = affine
		val ext = -a.getAngleExtent()
		if (ext >= 360.0 || ext <= -360) {
			arcSegs = 4
			this.increment = Math.PI / 2
			// btan(Math.PI / 2);
			this.cv = 0.5522847498307933
			if (ext < 0) {
				increment = -increment
				cv = -cv
			}
		} else {
			arcSegs = Math.ceil(Math.abs(ext) / 90.0).toInt()
			this.increment = Math.toRadians(ext / arcSegs)
			this.cv = btan(increment)
			if (cv == 0.0) {
				arcSegs = 0
			}
		}
		when (a.getArcType()) {
			Arc2D.OPEN -> lineSegs = 0
			Arc2D.CHORD -> lineSegs = 1
			Arc2D.PIE -> lineSegs = 2
		}
		if (w < 0 || h < 0) {
			arcSegs = -1
			lineSegs = -1
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
	override fun isDone() = index > arcSegs + lineSegs
	
	/**
	 * Moves the iterator to the next segment of the path forwards
	 * along the primary direction of traversal as long as there are
	 * more points in that direction.
	 */
	override fun next() {
		index++
	}
	
	private fun btan(increment: Double): Double {
		var increment = increment
		increment /= 2.0
		return 4.0 / 3.0 * Math.sin(increment) / (1.0 + Math.cos(increment))
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
		if (isDone()) {
			throw NoSuchElementException("arc iterator out of bounds")
		}
		var angle = angStRad
		if (index == 0) {
			coords[0] = (x + Math.cos(angle) * w).toFloat()
			coords[1] = (y + Math.sin(angle) * h).toFloat()
			affine?.transform(coords, 0, coords, 0, 1)
			
			return PathIterator.SEG_MOVETO
		}
		if (index > arcSegs) {
			if (index == arcSegs + lineSegs) {
				return PathIterator.SEG_CLOSE
			}
			coords[0] = x.toFloat()
			coords[1] = y.toFloat()
			affine?.transform(coords, 0, coords, 0, 1)
			
			return PathIterator.SEG_LINETO
		}
		angle += increment * (index - 1)
		var relx = Math.cos(angle)
		var rely = Math.sin(angle)
		coords[0] = (x + (relx - cv * rely) * w).toFloat()
		coords[1] = (y + (rely + cv * relx) * h).toFloat()
		angle += increment
		relx = Math.cos(angle)
		rely = Math.sin(angle)
		coords[2] = (x + (relx + cv * rely) * w).toFloat()
		coords[3] = (y + (rely - cv * relx) * h).toFloat()
		coords[4] = (x + relx * w).toFloat()
		coords[5] = (y + rely * h).toFloat()
		affine?.transform(coords, 0, coords, 0, 3)
		
		return PathIterator.SEG_CUBICTO
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
			throw NoSuchElementException("arc iterator out of bounds")
		}
		var angle = angStRad
		if (index == 0) {
			coords[0] = x + Math.cos(angle) * w
			coords[1] = y + Math.sin(angle) * h
			affine?.transform(coords, 0, coords, 0, 1)
			
			return PathIterator.SEG_MOVETO
		}
		if (index > arcSegs) {
			if (index == arcSegs + lineSegs) {
				return PathIterator.SEG_CLOSE
			}
			coords[0] = x
			coords[1] = y
			affine?.transform(coords, 0, coords, 0, 1)
			
			return PathIterator.SEG_LINETO
		}
		angle += increment * (index - 1)
		var relx = Math.cos(angle)
		var rely = Math.sin(angle)
		coords[0] = x + (relx - cv * rely) * w
		coords[1] = y + (rely + cv * relx) * h
		angle += increment
		relx = Math.cos(angle)
		rely = Math.sin(angle)
		coords[2] = x + (relx + cv * rely) * w
		coords[3] = y + (rely - cv * relx) * h
		coords[4] = x + relx * w
		coords[5] = y + rely * h
		affine?.transform(coords, 0, coords, 0, 3)
		
		return PathIterator.SEG_CUBICTO
	}
	
	operator fun invoke(rect: Arc2D, affine: AffineTransform? = null) = set(rect, affine)
	
	// ArcIterator.btan(Math.PI/2)
	val CtrlVal = 0.5522847498307933
	/*
 * ctrlpts contains the control points for a set of 4 cubic
 * bezier curves that approximate a circle of radius 0.5
 * centered at 0.5, 0.5
 */
	private val pcv = 0.5 + CtrlVal * 0.5
	private val ncv = 0.5 - CtrlVal * 0.5
	private val ctrlpts = arrayOf(doubleArrayOf(1.0, pcv, pcv, 1.0, 0.5, 1.0), doubleArrayOf(ncv, 1.0, 0.0, pcv, 0.0, 0.5), doubleArrayOf(0.0, ncv, ncv, 0.0, 0.5, 0.0), doubleArrayOf(pcv, 0.0, 1.0, ncv, 1.0, 0.5))
	
}
	
