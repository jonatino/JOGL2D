package org.anglur.joglext.jogl2d.shape.impl

import java.awt.geom.AffineTransform
import java.awt.geom.Ellipse2D
import java.awt.geom.PathIterator
import java.util.*

object EllipseIterator : PathIterator {
	
	private lateinit var e: Ellipse2D
	private var affine: AffineTransform? = null
	
	private var x: Double = 0.toDouble()
	private var y: Double = 0.toDouble()
	private var w: Double = 0.toDouble()
	private var h: Double = 0.toDouble()
	private var index: Int = 0
	
	fun set(r: Ellipse2D, affine: AffineTransform?) = apply {
		this.index = 0
		this.e = r
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
	 * store the coordinates of the point(s).N
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
			throw NoSuchElementException("ellipse iterator out of bounds")
		}
		if (index == 5) {
			return PathIterator.SEG_CLOSE
		}
		if (index == 0) {
			val ctrls = ctrlpts[3]
			coords[0] = (x + ctrls[4] * w).toFloat()
			coords[1] = (y + ctrls[5] * h).toFloat()
			affine?.transform(coords, 0, coords, 0, 1)
			
			return PathIterator.SEG_MOVETO
		}
		val ctrls = ctrlpts[index - 1]
		coords[0] = (x + ctrls[0] * w).toFloat()
		coords[1] = (y + ctrls[1] * h).toFloat()
		coords[2] = (x + ctrls[2] * w).toFloat()
		coords[3] = (y + ctrls[3] * h).toFloat()
		coords[4] = (x + ctrls[4] * w).toFloat()
		coords[5] = (y + ctrls[5] * h).toFloat()
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
			throw NoSuchElementException("ellipse iterator out of bounds")
		}
		if (index == 5) {
			return PathIterator.SEG_CLOSE
		}
		if (index == 0) {
			val ctrls = ctrlpts[3]
			coords[0] = x + ctrls[4] * w
			coords[1] = y + ctrls[5] * h
			affine?.transform(coords, 0, coords, 0, 1)
			
			return PathIterator.SEG_MOVETO
		}
		val ctrls = ctrlpts[index - 1]
		coords[0] = x + ctrls[0] * w
		coords[1] = y + ctrls[1] * h
		coords[2] = x + ctrls[2] * w
		coords[3] = y + ctrls[3] * h
		coords[4] = x + ctrls[4] * w
		coords[5] = y + ctrls[5] * h
		affine?.transform(coords, 0, coords, 0, 3)
		
		return PathIterator.SEG_CUBICTO
	}
	
	operator fun invoke(rect: Ellipse2D, affine: AffineTransform? = null) = set(rect, affine)
	
	// ArcIterator.btan(Math.PI/2)
	val CtrlVal = 0.5522847498307933
	/*
 * ctrlpts contains the control points for a set of 4 cubic
 * bezier curves that approximate a circle of radius 0.5
 * centered at 0.5, 0.5
 */
	private val pcv = 0.5 + CtrlVal * 0.5
	private val ncv = 0.5 - CtrlVal * 0.5
	private val ctrlpts = arrayOf<DoubleArray>(doubleArrayOf(1.0, pcv, pcv, 1.0, 0.5, 1.0), doubleArrayOf(ncv, 1.0, 0.0, pcv, 0.0, 0.5), doubleArrayOf(0.0, ncv, ncv, 0.0, 0.5, 0.0), doubleArrayOf(pcv, 0.0, 1.0, ncv, 1.0, 0.5))
	
}
	
