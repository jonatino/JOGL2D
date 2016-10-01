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

import org.anglur.joglext.jogl2d.GLG2DTransformHelper
import org.anglur.joglext.jogl2d.GLGraphics2D
import java.awt.RenderingHints.Key
import java.awt.geom.AffineTransform
import java.util.*

abstract class AbstractMatrixHelper : GLG2DTransformHelper {
	protected lateinit var g2d: GLGraphics2D
	
	protected var stack: Deque<AffineTransform> = ArrayDeque()
	
	override fun setG2D(g2d: GLGraphics2D) {
		this.g2d = g2d
		
		stack.clear()
		stack.push(AffineTransform())
	}
	
	override fun push(newG2d: GLGraphics2D) {
		stack.push(transform)
	}
	
	override fun pop(parentG2d: GLGraphics2D) {
		stack.pop()
		flushTransformToOpenGL()
	}
	
	override fun setHint(key: Key, value: Any?) {
		// nop
	}
	
	override fun resetHints() {
		// nop
	}
	
	override fun dispose() {
		// nop
	}
	
	override fun translate(x: Int, y: Int) {
		translate(x.toDouble(), y.toDouble())
		flushTransformToOpenGL()
	}
	
	override fun translate(tx: Double, ty: Double) {
		transform0.translate(tx, ty)
		flushTransformToOpenGL()
	}
	
	override fun rotate(theta: Double) {
		transform0.rotate(theta)
		flushTransformToOpenGL()
	}
	
	override fun rotate(theta: Double, x: Double, y: Double) {
		transform0.rotate(theta, x, y)
		flushTransformToOpenGL()
	}
	
	override fun scale(sx: Double, sy: Double) {
		transform0.scale(sx, sy)
		flushTransformToOpenGL()
	}
	
	override fun shear(shx: Double, shy: Double) {
		transform0.shear(shx, shy)
		flushTransformToOpenGL()
	}
	
	override fun transform(Tx: AffineTransform) {
		transform0.concatenate(Tx)
		flushTransformToOpenGL()
	}
	
	override var transform: AffineTransform
		get() = transform0.clone() as AffineTransform
		set(transform) {
			transform0.setTransform(transform)
			flushTransformToOpenGL()
		}
	
	/**
	 * Returns the `AffineTransform` at the top of the stack, *not* a
	 * copy.
	 */
	protected val transform0: AffineTransform
		get() = stack.peek()
	
	/**
	 * Sends the `AffineTransform` that's on top of the stack to the video
	 * card.
	 */
	protected abstract fun flushTransformToOpenGL()
}
