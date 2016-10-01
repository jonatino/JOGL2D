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
