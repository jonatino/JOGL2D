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

import org.anglur.joglext.cacheable.CachedFloatArray
import org.anglur.joglext.jogl2d.PathVisitor

/**
 * This is a fast Bzier curve implementation. I can't use OpenGL's
 * built-in evaluators because subclasses need to do something with the points,
 * not just pass them directly to glVertex2f. This algorithm uses forward
 * differencing. Most of this is taken from [http://www.niksula.hut.fi/~hkankaan/Homepages/bezierfast.html](http://www.niksula.hut.fi/~hkankaan/Homepages/bezierfast.html). I derived
 * the implementation for the quadratic on my own, but it's simple.
 */
abstract class SimplePathVisitor : PathVisitor {
	
	/**
	 * Gets the number of steps to take in a quadratic or cubic curve spline.
	 */
	/**
	 * Sets the number of steps to take in a quadratic or cubic curve spline.
	 */
	var numCurveSteps = CURVE_STEPS
	
	override fun quadTo(previousVertex: FloatArray, control: FloatArray) {
		val p = CachedFloatArray(2)
		
		var xd: Float
		val xdd: Float
		val xdd_per_2: Float
		var yd: Float
		val ydd: Float
		val ydd_per_2: Float
		val t = 1f / numCurveSteps
		val tt = t * t
		
		// x
		p[0] = previousVertex[0]
		xd = 2f * (control[0] - previousVertex[0]) * t
		xdd_per_2 = 1f * (previousVertex[0] - 2 * control[0] + control[2]) * tt
		xdd = xdd_per_2 + xdd_per_2
		
		// y
		p[1] = previousVertex[1]
		yd = 2f * (control[1] - previousVertex[1]) * t
		ydd_per_2 = 1f * (previousVertex[1] - 2 * control[1] + control[3]) * tt
		ydd = ydd_per_2 + ydd_per_2
		
		for (loop in 0..numCurveSteps - 1) {
			lineTo(p)
			
			p[0] = p[0] + xd + xdd_per_2
			xd += xdd
			
			p[1] = p[1] + yd + ydd_per_2
			yd += ydd
		}
		
		// use exactly the last point
		p[0] = control[2]
		p[1] = control[3]
		lineTo(p)
	}
	
	override fun cubicTo(previousVertex: FloatArray, control: FloatArray) {
		val p = CachedFloatArray(2)
		
		var xd: Float
		var xdd: Float
		val xddd: Float
		var xdd_per_2: Float
		val xddd_per_2: Float
		val xddd_per_6: Float
		var yd: Float
		var ydd: Float
		val yddd: Float
		var ydd_per_2: Float
		val yddd_per_2: Float
		val yddd_per_6: Float
		val t = 1f / numCurveSteps
		val tt = t * t
		
		// x
		p[0] = previousVertex[0]
		xd = 3f * (control[0] - previousVertex[0]) * t
		xdd_per_2 = 3f * (previousVertex[0] - 2 * control[0] + control[2]) * tt
		xddd_per_2 = 3f * (3 * (control[0] - control[2]) + control[4] - previousVertex[0]) * tt * t
		
		xddd = xddd_per_2 + xddd_per_2
		xdd = xdd_per_2 + xdd_per_2
		xddd_per_6 = xddd_per_2 / 3
		
		// y
		p[1] = previousVertex[1]
		yd = 3f * (control[1] - previousVertex[1]) * t
		ydd_per_2 = 3f * (previousVertex[1] - 2 * control[1] + control[3]) * tt
		yddd_per_2 = 3f * (3 * (control[1] - control[3]) + control[5] - previousVertex[1]) * tt * t
		
		yddd = yddd_per_2 + yddd_per_2
		ydd = ydd_per_2 + ydd_per_2
		yddd_per_6 = yddd_per_2 / 3
		
		for (loop in 0..numCurveSteps - 1) {
			lineTo(p)
			
			p[0] = p[0] + xd + xdd_per_2 + xddd_per_6
			xd += xdd + xddd_per_2
			xdd += xddd
			xdd_per_2 += xddd_per_2
			
			p[1] = p[1] + yd + ydd_per_2 + yddd_per_6
			yd += ydd + yddd_per_2
			ydd += yddd
			ydd_per_2 += yddd_per_2
		}
		
		// use exactly the last point
		p[0] = control[4]
		p[1] = control[5]
		lineTo(p)
	}
	
	companion object {
		/**
		 * Just a guess. Would be nice to make a better guess based on what's visually
		 * acceptable.
		 */
		val CURVE_STEPS = 30
	}
}
