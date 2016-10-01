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
