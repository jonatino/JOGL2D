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
package org.anglur.joglext.jogl2d.impl

import com.jogamp.opengl.GL
import org.anglur.joglext.cacheable.CachedFloatArray
import org.anglur.joglext.jogl2d.PathVisitor
import org.anglur.joglext.jogl2d.VertexBuffer
import java.awt.BasicStroke
import java.lang.Math.acos
import java.lang.Math.sqrt

/**
 * Tesselating is expensive. This is a simple workaround to check if we can just
 * draw the simple, convex polygon without tesselating. At each corner, we have
 * to check the sign of the z-component of the cross-product. If it's the same
 * all the way around, we know that every turn went the same direction. That
 * ensures it's convex. That's necessary, but not sufficient since we might
 * still have self-intersections. For that, we check that the total curvature
 * along the path is 2π. That ensures it's simple.
 *
 *
 *
 *
 * This checks every corner and if it has the same sign and total curvature is
 * 2π, we know the polygon is convex. Once we get to the end, we draw it. If
 * it's not convex, then we fall back to tesselating it.
 *
 *
 *
 * There are many places where we could fail being a simple convex polygon and
 * then have to fail over to the tesselator. As soon as we fail over we need to
 * catch the tesselator up to the current position and then use the tesselator
 * from then on. For that reason, this class is a little messy.
 *
 */
class SimpleOrTesselatingVisitor(protected var simpleFallback: PathVisitor, protected var tesselatorFallback: PathVisitor) : SimplePathVisitor() {
	/**
	 * This buffer is used to store points for the simple polygon, until we find
	 * out it's not simple. Then we push all this data to the tesselator and
	 * ignore the buffer.
	 */
	protected var vBuffer = VertexBuffer(1024)
	
	/**
	 * This is the buffer of vertices we'll use to test the corner.
	 */
	protected var previousVertices = CachedFloatArray(4)
	protected var numberOfPreviousVertices: Int = 0
	
	/**
	 * The total curvature along the path. Since we know we close the path, if
	 * it's a simple, convex polygon, we'll have a total curvature of 2π.
	 */
	protected var totalCurvature: Double = 0.toDouble()
	
	/**
	 * All corners must have the same sign.
	 */
	protected var sign: Int = 0
	
	/**
	 * The flag to indicate if we currently believe this polygon to be simple and
	 * convex.
	 */
	protected var isConvexSoFar: Boolean = false
	
	/**
	 * The flag to indicate if we are on our first segment (move-to). If we have
	 * multiple move-to's, then we need to tesselate.
	 */
	protected var firstContour: Boolean = false
	
	/**
	 * Keep the winding rule for when we pass the information off to the
	 * tesselator.
	 */
	protected var windingRule: Int = 0
	
	override fun setGLContext(context: GL) {
		simpleFallback.setGLContext(context)
		tesselatorFallback.setGLContext(context)
	}
	
	override fun setStroke(stroke: BasicStroke) {
		// this is only used to fill, no need to consider stroke
	}
	
	override fun beginPoly(windingRule: Int) {
		isConvexSoFar = true
		firstContour = true
		sign = 0
		totalCurvature = 0.0
		
		this.windingRule = windingRule
	}
	
	override fun moveTo(vertex: FloatArray) {
		if (firstContour) {
			firstContour = false
		} else if (isConvexSoFar) {
			setUseTesselator(false)
		}
		
		if (isConvexSoFar) {
			numberOfPreviousVertices = 1
			previousVertices = floatArrayOf(vertex[0], vertex[1], 0f, 0f)
			
			vBuffer.clear()
			vBuffer.addVertex(vertex[0], vertex[1])
		} else {
			tesselatorFallback.closeLine()
			tesselatorFallback.moveTo(vertex)
		}
	}
	
	override fun lineTo(vertex: FloatArray) {
		if (isConvexSoFar) {
			vBuffer.addVertex(vertex[0], vertex[1])
			
			if (!isValidCorner(vertex)) {
				setUseTesselator(false)
			}
		} else {
			tesselatorFallback.lineTo(vertex)
		}
	}
	
	/**
	 * Returns true if the corner is correct, using the new vertex and the buffer
	 * of previous vertices. This always updates the buffer of previous vertices.
	 */
	protected fun isValidCorner(vertex: FloatArray): Boolean {
		if (numberOfPreviousVertices >= 2) {
			val diff1 = (previousVertices[2] - previousVertices[0]).toDouble()
			val diff2 = (previousVertices[3] - previousVertices[1]).toDouble()
			val diff3 = (vertex[0] - previousVertices[0]).toDouble()
			val diff4 = (vertex[1] - previousVertices[1]).toDouble()
			
			val cross2 = diff1 * diff4 - diff2 * diff3
			
			/*
       * Check that the current sign of the cross-product is the same as the
       * others.
       */
			val currentSign = sign(cross2)
			if (sign == 0) {
				sign = currentSign
				
				// allow for currentSign = 0, in which case we don't care
			} else if (currentSign * sign == -1) {
				return false
			}
			
			/*
       * Check that the total curvature along the path is less than 2π.
       */
			val norm1sq = diff1 * diff1 + diff2 * diff2
			val norm2sq = diff3 * diff3 + diff4 * diff4
			val dot = diff1 * diff3 + diff2 * diff4
			val cosThetasq = dot * dot / (norm1sq * norm2sq)
			val theta = acos(sqrt(cosThetasq))
			
			totalCurvature += theta
			if (totalCurvature > 2 * Math.PI + 1e-3) {
				return false
			}
		}
		
		numberOfPreviousVertices++
		previousVertices[2] = previousVertices[0]
		previousVertices[3] = previousVertices[1]
		previousVertices[0] = vertex[0]
		previousVertices[1] = vertex[1]
		
		return true
	}
	
	protected fun sign(value: Double): Int {
		if (value > 1e-8) {
			return 1
		} else if (value < -1e-8) {
			return -1
		} else {
			return 0
		}
	}
	
	override fun closeLine() {
		if (isConvexSoFar) {
			/*
	   * If we're convex so far, we need to finish out all the corners to make
       * sure everything is kosher.
       */
			val buf = vBuffer.buffer
			val vertex = CachedFloatArray(2)
			val position = buf.position()
			
			buf.rewind()
			buf.get(vertex)
			
			var good = false
			if (isValidCorner(vertex)) {
				buf.get(vertex)
				if (isValidCorner(vertex)) {
					good = true
				}
			}
			
			buf.position(position)
			
			if (!good) {
				setUseTesselator(true)
			}
		} else {
			tesselatorFallback.closeLine()
		}
	}
	
	override fun endPoly() {
		if (isConvexSoFar) {
			simpleFallback.beginPoly(windingRule)
			drawToVisitor(simpleFallback, true)
			simpleFallback.endPoly()
		} else {
			tesselatorFallback.endPoly()
		}
	}
	
	/**
	 * Sets the state to start using the tesselator. This will catch the
	 * tesselator up to the current position and then set `isConvexSoFar` to
	 * false so we can start using the tesselator exclusively.
	 *
	 *
	 * If `doClose` is true, then we will also close the line when we update
	 * the tesselator. This is for when we realized it's not a simple poly after
	 * we already finished the first path.
	 */
	protected fun setUseTesselator(doClose: Boolean) {
		isConvexSoFar = false
		
		tesselatorFallback.beginPoly(windingRule)
		drawToVisitor(tesselatorFallback, doClose)
	}
	
	protected fun drawToVisitor(visitor: PathVisitor, doClose: Boolean) {
		val buf = vBuffer.buffer
		buf.flip()
		
		val vertex = CachedFloatArray(2)
		
		if (buf.hasRemaining()) {
			buf.get(vertex)
			visitor.moveTo(vertex)
		}
		
		while (buf.hasRemaining()) {
			buf.get(vertex)
			visitor.lineTo(vertex)
		}
		
		if (doClose) {
			visitor.closeLine()
		}
		
		// put everything back the way it was
		vBuffer.clear()
	}
}
