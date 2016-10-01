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

package org.anglur.joglext.jogl2d.impl.gl2

import com.jogamp.opengl.GL2
import com.jogamp.opengl.GL2GL3
import org.anglur.joglext.jogl2d.GLGraphics2D
import org.anglur.joglext.jogl2d.impl.AbstractColorHelper
import java.awt.*

class GL2ColorHelper : AbstractColorHelper() {
	
	protected lateinit var gl: GL2
	
	override fun setG2D(g2d: GLGraphics2D) {
		super.setG2D(g2d)
		gl = g2d.glContext.gl.gL2
	}
	
	// This will probably be easier to handle with a fragment shader
	// in the shader pipeline, not sure how to handle it in the fixed-
	// function pipeline.
	override var paint: Paint
		get() = color
		set(paint) = when (paint) {
			is Color -> color = paint
			is GradientPaint -> {
				color = paint.color1
				TODO("setPaint(Paint) with GradientPaint")
			}
			is MultipleGradientPaint -> {
				color = paint.colors[0]
				TODO("setPaint(Paint) with MultipleGradientPaint")
			}
			else -> TODO("setPaint(Paint) with " + paint.javaClass.simpleName)
		}
	
	override fun setColorNoRespectComposite(c: Color) = setColor(gl, c, 1f)
	
	/**
	 * Sets the current color with a call to glColor4*. But it respects the
	 * AlphaComposite if any. If the AlphaComposite wants to pre-multiply an
	 * alpha, pre-multiply it.
	 */
	override fun setColorRespectComposite(c: Color) {
		var alpha = 1f
		val composite = composite
		if (composite is AlphaComposite) {
			alpha = composite.alpha
		}
		
		setColor(gl, c, alpha)
	}
	
	private fun setColor(gl: GL2, c: Color, preMultiplyAlpha: Float) {
		val rgb = c.rgb
		gl.glColor4ub((rgb shr 16 and 0xFF).toByte(), (rgb shr 8 and 0xFF).toByte(), (rgb and 0xFF).toByte(), ((rgb shr 24 and 0xFF) * preMultiplyAlpha).toByte())
	}
	
	override fun setPaintMode() = TODO("setPaintMode()")
	
	override fun setXORMode(c: Color) = TODO("setXORMode(Color)")
	
	override fun copyArea(x: Int, y: Int, width: Int, height: Int, dx: Int, dy: Int) {
		// glRasterPos* is transformed, but CopyPixels is not
		val x2 = x + dx
		val y2 = y + dy + height
		gl.glRasterPos2i(x2, y2)
		
		val x1 = x
		val y1 = g2d.canvasHeight - (y + height)
		gl.glCopyPixels(x1, y1, width, height, GL2GL3.GL_COLOR)
	}
	
}
