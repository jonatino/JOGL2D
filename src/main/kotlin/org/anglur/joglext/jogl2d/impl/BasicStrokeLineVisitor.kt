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

package org.anglur.joglext.jogl2d.impl

import com.jogamp.common.nio.Buffers
import org.anglur.joglext.cacheable.CachedFloatArray
import org.anglur.joglext.jogl2d.VertexBuffer
import java.awt.BasicStroke
import java.lang.Math.*

/**
 * Draws a line, as outlined by a [BasicStroke]. The current
 * implementation supports everything except dashes. This class draws a series
 * of quads for each line segment, joins corners and endpoints as appropriate.
 */
abstract class BasicStrokeLineVisitor : SimplePathVisitor() {
	
	protected var lineJoin: Int = 0
	protected var endCap: Int = 0
	protected var lineOffset: Float = 0.toFloat()
	protected var miterLimit: Float = 0.toFloat()
	
	protected var lastPoint: FloatArray? = null
	protected var secondLastPoint: FloatArray? = null
	protected var firstPoint: FloatArray? = null
	protected var secondPoint: FloatArray? = null
	
	protected var vBuffer = VertexBuffer(1024)
	protected var tmpBuffer = Buffers.newDirectFloatBuffer(1024)
	
	override fun setStroke(stroke: BasicStroke) {
		lineJoin = stroke.lineJoin
		lineOffset = stroke.lineWidth / 2
		endCap = stroke.endCap
		miterLimit = stroke.miterLimit
		
		// TODO
		if (stroke.dashArray != null) {
			TODO("BasicStroke with dash array")
		}
	}
	
	override fun beginPoly(windingRule: Int) {
		clear()
	}
	
	override fun endPoly() {
		finishAndDrawLine()
	}
	
	override fun moveTo(vertex: FloatArray) {
		finishAndDrawLine()
		
		lastPoint = floatArrayOf(vertex[0], vertex[1])
		firstPoint = lastPoint
	}
	
	override fun lineTo(vertex: FloatArray) {
		// ignore 0-length lines
		if (lastPoint!![0] == vertex[0] && lastPoint!![1] == vertex[1]) {
			return
		}
		
		val vClone = floatArrayOf(vertex[0], vertex[1])
		if (secondPoint == null) {
			secondPoint = vClone
		}
		
		if (secondLastPoint != null) {
			applyCorner(vertex)
		}
		
		secondLastPoint = lastPoint
		lastPoint = vClone
	}
	
	override fun closeLine() {
		/*
	 * Our first point we stroked is around the second point we hit. So we add
     * the first 2 points so we do all the corners. Then we end on the last two
     * points to finish the last two triangles.
     */
		if (firstPoint != null && secondPoint != null) {
			lineTo(firstPoint!!)
			lineTo(secondPoint!!)
			
			val buf = vBuffer.buffer
			addVertex(buf.get(0), buf.get(1))
			addVertex(buf.get(2), buf.get(3))
			
			drawBuffer()
		}
		
		clear()
	}
	
	protected fun clear() {
		vBuffer.clear()
		firstPoint = null
		lastPoint = null
		
		secondPoint = null
		secondLastPoint = null
	}
	
	protected fun finishAndDrawLine() {
		if (firstPoint != null && secondPoint != null) {
			applyEndCap(secondLastPoint!!, lastPoint!!, false)
			
			val buf = vBuffer.buffer
			if (tmpBuffer.capacity() < buf.position()) {
				tmpBuffer = Buffers.newDirectFloatBuffer(buf.position())
			}
			
			tmpBuffer.clear()
			
			buf.flip()
			tmpBuffer.put(buf)
			tmpBuffer.flip()
			
			buf.clear()
			applyEndCap(firstPoint!!, secondPoint!!, true)
			buf.put(tmpBuffer)
			
			drawBuffer()
		}
		
		clear()
	}
	
	override fun quadTo(previousVertex: FloatArray, control: FloatArray) {
		val originalJoin = lineJoin
		
		// go around the corners quickly
		lineJoin = BasicStroke.JOIN_BEVEL
		super.quadTo(previousVertex, control)
		lineJoin = originalJoin
	}
	
	override fun cubicTo(previousVertex: FloatArray, control: FloatArray) {
		val originalJoin = lineJoin
		
		// go around the corners quickly
		lineJoin = BasicStroke.JOIN_BEVEL
		super.cubicTo(previousVertex, control)
		lineJoin = originalJoin
	}
	
	protected fun applyCorner(vertex: FloatArray) {
		when (lineJoin) {
			BasicStroke.JOIN_BEVEL -> drawCornerBevel(secondLastPoint!!, lastPoint!!, vertex)
			
			BasicStroke.JOIN_ROUND -> drawCornerRound(secondLastPoint!!, lastPoint!!, vertex)
			
			BasicStroke.JOIN_MITER -> drawCornerMiter(secondLastPoint!!, lastPoint!!, vertex)
			
			else -> TODO("BasicStroke with unknown line join: " + lineJoin)
		}
	}
	
	protected fun drawCornerRound(secondLastPoint: FloatArray, lastPoint: FloatArray, point: FloatArray) {
		val offset1 = lineOffset(secondLastPoint, lastPoint)
		val offset2 = lineOffset(lastPoint, point)
		
		val v1 = subtract(lastPoint, secondLastPoint)
		normalize(v1)
		val v2 = subtract(lastPoint, point)
		normalize(v2)
		
		val rightPt1 = add(lastPoint, offset1)
		val rightPt2 = add(lastPoint, offset2)
		val leftPt1 = subtract(lastPoint, offset1)
		val leftPt2 = subtract(lastPoint, offset2)
		
		var alpha = getIntersectionAlpha(rightPt1, v1, rightPt2, v2)
		
		// get the outside angle (our vectors v1, v2 are unit vectors)
		val theta = (Math.PI - acos((v1[0] * v2[0] + v1[1] * v2[1]).toDouble())).toFloat()
		
		// if inside corner is right side
		if (alpha <= 0) {
			val rightInside = addScaled(rightPt1, v1, alpha)
			
			addVertex(rightInside[0], rightInside[1])
			addVertex(leftPt1[0], leftPt1[1])
			
			val max = ceil((theta / THETA_STEP).toDouble()).toInt()
			// rotate the other way
			for (i in 0..max - 1) {
				val newX = COS_STEP * offset1[0] + SIN_STEP * offset1[1]
				offset1[1] = -SIN_STEP * offset1[0] + COS_STEP * offset1[1]
				offset1[0] = newX
				
				addVertex(rightInside[0], rightInside[1])
				addVertex(lastPoint[0] - offset1[0], lastPoint[1] - offset1[1])
			}
			
			addVertex(rightInside[0], rightInside[1])
			addVertex(leftPt2[0], leftPt2[1])
		} else {
			alpha = -alpha
			val leftInside = addScaled(leftPt1, v1, alpha)
			
			addVertex(rightPt1[0], rightPt1[1])
			addVertex(leftInside[0], leftInside[1])
			
			val max = ceil((theta / THETA_STEP).toDouble()).toInt()
			for (i in 0..max - 1) {
				val newX = COS_STEP * offset1[0] - SIN_STEP * offset1[1]
				offset1[1] = SIN_STEP * offset1[0] + COS_STEP * offset1[1]
				offset1[0] = newX
				
				addVertex(lastPoint[0] + offset1[0], lastPoint[1] + offset1[1])
				addVertex(leftInside[0], leftInside[1])
			}
			
			addVertex(rightPt2[0], rightPt2[1])
			addVertex(leftInside[0], leftInside[1])
		}
	}
	
	protected fun drawCornerBevel(secondLastPoint: FloatArray, lastPoint: FloatArray, point: FloatArray) {
		val offset1 = lineOffset(secondLastPoint, lastPoint)
		val offset2 = lineOffset(lastPoint, point)
		
		val v1 = subtract(lastPoint, secondLastPoint)
		normalize(v1)
		val v2 = subtract(lastPoint, point)
		normalize(v2)
		
		val rightPt1 = add(lastPoint, offset1)
		val rightPt2 = add(lastPoint, offset2)
		val leftPt1 = subtract(lastPoint, offset1)
		val leftPt2 = subtract(lastPoint, offset2)
		
		var alpha = getIntersectionAlpha(rightPt1, v1, rightPt2, v2)
		
		// if inside corner is right side
		if (alpha <= 0) {
			val rightInside = addScaled(rightPt1, v1, alpha)
			
			addVertex(rightInside[0], rightInside[1])
			addVertex(leftPt1[0], leftPt1[1])
			addVertex(rightInside[0], rightInside[1])
			addVertex(leftPt2[0], leftPt2[1])
		} else {
			// carry the math through and this turns out
			alpha = -alpha
			val leftInside = addScaled(leftPt1, v1, alpha)
			
			addVertex(rightPt1[0], rightPt1[1])
			addVertex(leftInside[0], leftInside[1])
			addVertex(rightPt2[0], rightPt2[1])
			addVertex(leftInside[0], leftInside[1])
		}
	}
	
	protected fun drawCornerMiter(secondLastPoint: FloatArray, lastPoint: FloatArray, point: FloatArray) {
		val offset1 = lineOffset(secondLastPoint, lastPoint)
		val offset2 = lineOffset(lastPoint, point)
		
		val v1 = subtract(lastPoint, secondLastPoint)
		normalize(v1)
		val v2 = subtract(lastPoint, point)
		normalize(v2)
		
		val rightPt1 = add(lastPoint, offset1)
		val rightPt2 = add(lastPoint, offset2)
		val leftPt1 = subtract(lastPoint, offset1)
		
		var alpha = getIntersectionAlpha(rightPt1, v1, rightPt2, v2)
		val rightCorner = addScaled(rightPt1, v1, alpha)
		
		// other side is just the negative alpha
		alpha = -alpha
		val leftCorner = addScaled(leftPt1, v1, alpha)
		
		// If we exceed the miter limit, draw beveled corner
		val dist = distance(rightCorner, leftCorner)
		
		if (dist > miterLimit * lineOffset * 2f) {
			drawCornerBevel(secondLastPoint, lastPoint, point)
		} else {
			addVertex(rightCorner[0], rightCorner[1])
			addVertex(leftCorner[0], leftCorner[1])
		}
	}
	
	protected fun distance(pt1: FloatArray, pt2: FloatArray): Float {
		val diffX = (pt1[0] - pt2[0]).toDouble()
		val diffY = (pt1[1] - pt2[1]).toDouble()
		val distSq = diffX * diffX + diffY * diffY
		return sqrt(distSq).toFloat()
	}
	
	protected fun addScaled(pt: FloatArray, v: FloatArray, alpha: Float): FloatArray {
		return floatArrayOf(pt[0] + v[0] * alpha, pt[1] + v[1] * alpha)
	}
	
	protected fun normalize(v: FloatArray) {
		val norm = sqrt((v[0] * v[0] + v[1] * v[1]).toDouble()).toFloat()
		v[0] /= norm
		v[1] /= norm
	}
	
	protected fun subtract(pt1: FloatArray, pt2: FloatArray): FloatArray {
		return floatArrayOf(pt1[0] - pt2[0], pt1[1] - pt2[1])
	}
	
	protected fun add(pt1: FloatArray, pt2: FloatArray): FloatArray {
		return floatArrayOf(pt2[0] + pt1[0], pt2[1] + pt1[1])
	}
	
	protected fun getIntersectionAlpha(pt1: FloatArray, v1: FloatArray, pt2: FloatArray, v2: FloatArray): Float {
		var t = (pt2[0] - pt1[0]) * v2[1] - (pt2[1] - pt1[1]) * v2[0]
		t /= v1[0] * v2[1] - v1[1] * v2[0]
		return t
	}
	
	protected fun lineOffset(linePoint1: FloatArray, linePoint2: FloatArray): FloatArray {
		val vec = CachedFloatArray(2)
		vec[0] = linePoint2[0] - linePoint1[0]
		vec[1] = linePoint2[1] - linePoint1[1]
		
		var norm = vec[0] * vec[0] + vec[1] * vec[1]
		norm = sqrt(norm.toDouble()).toFloat()
		
		val scale = lineOffset / norm
		val offset = CachedFloatArray(2)
		offset[0] = vec[1] * scale
		offset[1] = -vec[0] * scale
		return offset
	}
	
	protected fun lineCorners(linePoint1: FloatArray, linePoint2: FloatArray, vertex: FloatArray, offset: Float): FloatArray {
		val translated = CachedFloatArray(2)
		translated[0] = linePoint2[0] - linePoint1[0]
		translated[1] = linePoint2[1] - linePoint1[1]
		
		var norm = translated[0] * translated[0] + translated[1] * translated[1]
		norm = sqrt(norm.toDouble()).toFloat()
		
		val scale = offset / norm
		val corners = CachedFloatArray(4)
		corners[0] = translated[1] * scale + vertex[0]
		corners[1] = -translated[0] * scale + vertex[1]
		corners[2] = -translated[1] * scale + vertex[0]
		corners[3] = translated[0] * scale + vertex[1]
		return corners
	}
	
	/**
	 * Finds the intersection of two lines. This method was written to reduce the
	 * number of array creations and so is quite dense. However, it is easy to
	 * understand the theory behind the computation. I found this at [http://mathforum.org/library/drmath/view/62814.html](http://mathforum.org/library/drmath/view/62814.html).
	 *
	 *
	 *
	 *
	 * We have two lines, specified by three points (P1, P2, P3). They share the
	 * second point. This gives us an easy way to represent the line in parametric
	 * form. For example the first line has the form
	 *
	 *
	 *
	 * &lt;x, y&gt; = &lt;P1x, P1y&gt; + t * &lt;P2x-P1x, P2y-P1y&gt;
	 *
	 *
	 *
	 *
	 *
	 *
	 * `&lt;P1x, P1y&gt;` is a point on the line,
	 * while
	 * `&lt;P2x-P1x, P2y-P1y&gt;`
	 * is the direction of the line. The method for solving for the intersection
	 * of these two parametric lines is straightforward. Let `o1` and
	 * `o2` be the points on the lines and `v1` and
	 * `v2` be the two direction vectors. Now we have
	 *
	 *
	 *
	 * p1 = o1 + t * v1
	 * p2 = o2 + s * v2
	 *
	 *
	 *
	 * We can solve to find the intersection by
	 *
	 *
	 *
	 * o1 + t * v1 = o2 + s * v2
	 * t * v1 = o2 - o1 + s * v2
	 * (t * v1) x v2 = (o2 - o1 + s * v2) x v2    ; cross product by v2
	 * t * (v1 x v2) = (o2 - o1) x v2             ; to get rid of s term
	 *
	 *
	 *
	 * Solving for `t` is easy since we only have the z component. Put
	 * `t` back into the first equation gives us our point of
	 * intersection.
	 *
	 *
	 *
	 * This method solves for `t`, but not directly for lines
	 * intersecting the point parameters. Since we're trying to use this for the
	 * miter corners, we want to solve for the intersections of the two outside
	 * edges of the lines that go from `secondLastPoint` to
	 * `lastPoint` and from `lastPoint` to
	 * `point`.
	 *
	 */
	protected fun getMiterIntersections(secondLastPoint: FloatArray, lastPoint: FloatArray, point: FloatArray): FloatArray {
		val o1 = lineCorners(secondLastPoint, lastPoint, lastPoint, lineOffset)
		val o2 = lineCorners(lastPoint, point, lastPoint, lineOffset)
		
		val v1 = CachedFloatArray(2)
		v1[0] = lastPoint[0] - secondLastPoint[0]
		v1[1] = lastPoint[1] - secondLastPoint[1]
		val v2 = CachedFloatArray(2)
		v2[0] = lastPoint[0] - point[0]
		v2[1] = lastPoint[1] - point[1]
		
		var norm = sqrt((v1[0] * v1[0] + v1[1] * v1[1]).toDouble()).toFloat()
		v1[0] /= norm
		v1[1] /= norm
		norm = sqrt((v2[0] * v2[0] + v2[1] * v2[1]).toDouble()).toFloat()
		v2[0] /= norm
		v2[1] /= norm
		
		val intersections = CachedFloatArray(4)
		
		var t = (o2[0] - o1[0]) * v2[1] - (o2[1] - o1[1]) * v2[0]
		t /= v1[0] * v2[1] - v1[1] * v2[0]
		intersections[0] = o1[0] + t * v1[0]
		intersections[1] = o1[1] + t * v1[1]
		
		t = (o2[2] - o1[2]) * v2[1] - (o2[3] - o1[3]) * v2[0]
		t /= v1[0] * v2[1] - v1[1] * v2[0]
		intersections[2] = o1[2] + t * v1[0]
		intersections[3] = o1[3] + t * v1[1]
		
		return intersections
	}
	
	protected fun applyEndCap(point1: FloatArray, point2: FloatArray, first: Boolean) {
		when (endCap) {
			BasicStroke.CAP_BUTT -> drawCapButt(point1, point2, first)
			
			BasicStroke.CAP_SQUARE -> drawCapSquare(point1, point2, first)
			
			BasicStroke.CAP_ROUND -> drawCapRound(point1, point2, first)
		}
	}
	
	protected fun drawCapButt(point1: FloatArray, point2: FloatArray, first: Boolean) {
		val offset = lineOffset(point1, point2)
		
		val pt = if (first) point1 else point2
		var cornerPt = add(pt, offset)
		addVertex(cornerPt[0], cornerPt[1])
		cornerPt = subtract(pt, offset)
		addVertex(cornerPt[0], cornerPt[1])
	}
	
	protected fun drawCapSquare(point1: FloatArray, point2: FloatArray, first: Boolean) {
		val offset = lineOffset(point1, point2)
		
		val offsetRotated: FloatArray
		val pt: FloatArray
		if (first) {
			offsetRotated = floatArrayOf(offset[1], -offset[0])
			pt = point1
		} else {
			offsetRotated = floatArrayOf(-offset[1], offset[0])
			pt = point2
		}
		
		var cornerPt = add(add(pt, offset), offsetRotated)
		addVertex(cornerPt[0], cornerPt[1])
		cornerPt = add(subtract(pt, offset), offsetRotated)
		addVertex(cornerPt[0], cornerPt[1])
	}
	
	protected fun drawCapRound(point1: FloatArray, point2: FloatArray, first: Boolean) {
		/*
	 * Instead of doing a triangle-fan around the cap, we're going to jump back
     * and forth from the tip toward the body of the line.
     */
		
		var offsetRight: FloatArray
		val offsetLeft: FloatArray
		val pt: FloatArray
		if (first) {
			val v = subtract(point1, point2)
			normalize(v)
			v[0] *= lineOffset
			v[1] *= lineOffset
			
			offsetRight = v
			offsetLeft = floatArrayOf(v[0], v[1])
			pt = point1
		} else {
			offsetRight = lineOffset(point1, point2)
			offsetLeft = floatArrayOf(-offsetRight[0], -offsetRight[1])
			pt = point2
		}
		
		val max = ceil(Math.PI / 2.0 / THETA_STEP.toDouble()).toInt()
		for (i in 0..max - 1) {
			addVertex(pt[0] + offsetRight[0], pt[1] + offsetRight[1])
			addVertex(pt[0] + offsetLeft[0], pt[1] + offsetLeft[1])
			
			var newX = COS_STEP * offsetRight[0] + -SIN_STEP * offsetRight[1]
			offsetRight[1] = SIN_STEP * offsetRight[0] + COS_STEP * offsetRight[1]
			offsetRight[0] = newX
			
			newX = COS_STEP * offsetLeft[0] + SIN_STEP * offsetLeft[1]
			offsetLeft[1] = -SIN_STEP * offsetLeft[0] + COS_STEP * offsetLeft[1]
			offsetLeft[0] = newX
		}
		
		if (first) {
			offsetRight = lineOffset(point1, point2)
			
			addVertex(pt[0] + offsetRight[0], point1[1] + offsetRight[1])
			addVertex(pt[0] - offsetRight[0], point1[1] - offsetRight[1])
		} else {
			val v = subtract(point2, point1)
			normalize(v)
			v[0] *= lineOffset
			v[1] *= lineOffset
			
			addVertex(pt[0] + v[0], pt[1] + v[1])
		}
	}
	
	protected fun addVertex(x: Float, y: Float) {
		vBuffer.addVertex(x, y)
	}
	
	protected abstract fun drawBuffer()
	
	companion object {
		protected var THETA_STEP = 0.5f
		protected var COS_STEP = cos(THETA_STEP.toDouble()).toFloat()
		protected var SIN_STEP = sin(THETA_STEP.toDouble()).toFloat()
	}
}