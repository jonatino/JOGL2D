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
package org.anglur.joglext.jogl2d.impl.gl2

import com.jogamp.opengl.GL
import com.jogamp.opengl.GL2
import com.jogamp.opengl.fixedfunc.GLMatrixFunc
import org.anglur.joglext.cacheable.CachedFloatArray
import org.anglur.joglext.jogl2d.GLGraphics2D
import org.anglur.joglext.jogl2d.impl.AbstractMatrixHelper

import java.awt.geom.AffineTransform

class GL2Transformhelper : AbstractMatrixHelper() {
	protected lateinit var gl: GL2
	
	private val matrixBuf = CachedFloatArray(16)
	
	override fun setG2D(g2d: GLGraphics2D) {
		super.setG2D(g2d)
		gl = g2d.glContext.getGL().getGL2()
		
		setupGLView()
		flushTransformToOpenGL()
	}
	
	protected fun setupGLView() {
		val viewportDimensions = IntArray(4)
		gl.glGetIntegerv(GL.GL_VIEWPORT, viewportDimensions, 0)
		val width = viewportDimensions[2]
		val height = viewportDimensions[3]
		
		// setup projection
		gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION)
		gl.glLoadIdentity()
		gl.glOrtho(0.0, width.toDouble(), 0.0, height.toDouble(), -1.0, 1.0)
		
		// the MODELVIEW matrix will get adjusted later
		
		gl.glMatrixMode(GL.GL_TEXTURE)
		gl.glLoadIdentity()
	}
	
	/**
	 * Sends the `AffineTransform` that's on top of the stack to the video
	 * card.
	 */
	override fun flushTransformToOpenGL() {
		val matrix = getGLMatrix(stack.peek())
		
		gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW)
		gl.glLoadMatrixf(matrix, 0)
	}
	
	/**
	 * Gets the GL matrix for the `AffineTransform` with the change of
	 * coordinates inlined. Since Java2D uses the upper-left as 0,0 and OpenGL
	 * uses the lower-left as 0,0, we have to pre-multiply the matrix before
	 * loading it onto the video card.
	 */
	protected fun getGLMatrix(transform: AffineTransform): FloatArray {
		matrixBuf[0] = transform.scaleX.toFloat()
		matrixBuf[1] = -transform.shearY.toFloat()
		matrixBuf[4] = transform.shearX.toFloat()
		matrixBuf[5] = -transform.scaleY.toFloat()
		matrixBuf[10] = 1f
		matrixBuf[12] = transform.translateX.toFloat()
		matrixBuf[13] = g2d.canvasHeight - transform.translateY.toFloat()
		matrixBuf[15] = 1f
		
		return matrixBuf
	}
}
