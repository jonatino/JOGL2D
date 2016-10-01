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

package org.anglur.joglext.jogl2d

import com.jogamp.opengl.GLAutoDrawable
import com.jogamp.opengl.GLEventListener
import javax.swing.JComponent

/**
 * Wraps a `JComponent` and paints it using a `GLGraphics2D`. This
 * object will paint the entire component fully for each frame.
 *
 *
 *
 *
 * update the size and layout of the painted Swing component.
 *
 */
class GLG2DSimpleEventListener(
		/**
		 * The component to paint.
		 */
		protected var comp: JComponent) : GLEventListener {
	
	/**
	 * The cached object.
	 */
	protected var g2d: GLGraphics2D? = null
	
	override fun display(drawable: GLAutoDrawable) {
		prePaint(drawable)
		paintGL(g2d!!)
		postPaint(drawable)
	}
	
	/**
	 * Called before any painting is done. This should setup the matrices and ask
	 * the `GLGraphics2D` object to setup any client state.
	 */
	protected fun prePaint(drawable: GLAutoDrawable) {
		setupViewport(drawable)
		g2d!!.prePaint(drawable.context)
		
		// clip to only the component we're painting
		g2d!!.translate(comp.getX(), comp.getY())
		g2d!!.clipRect(0, 0, comp.getWidth(), comp.getHeight())
	}
	
	/**
	 * Defines the viewport to paint into.
	 */
	protected fun setupViewport(drawable: GLAutoDrawable) {
		drawable.gl.glViewport(0, 0, drawable.surfaceWidth, drawable.surfaceHeight)
	}
	
	/**
	 * Called after all Java2D painting is complete.
	 */
	protected fun postPaint(drawable: GLAutoDrawable) {
		g2d!!.postPaint()
	}
	
	/**
	 * Paints using the `GLGraphics2D` object. This could be forwarded to
	 * any code that expects to draw using the Java2D framework.
	 *
	 *
	 * Currently is paints the component provided, turning off double-buffering in
	 * the `RepaintManager` to force drawing directly to the
	 * `Graphics2D` object.
	 *
	 */
	protected fun paintGL(g2d: GLGraphics2D) {
		val wasDoubleBuffered = comp.isDoubleBuffered()
		comp.setDoubleBuffered(false)
		
		comp.paint(g2d)
		
		comp.setDoubleBuffered(wasDoubleBuffered)
	}
	
	override fun init(drawable: GLAutoDrawable) {
		g2d = createGraphics2D(drawable)
	}
	
	override fun reshape(drawable: GLAutoDrawable, x: Int, y: Int, width: Int, height: Int) {
	}
	
	/**
	 * Creates the `Graphics2D` object that forwards Java2D calls to OpenGL
	 * calls.
	 */
	protected fun createGraphics2D(drawable: GLAutoDrawable): GLGraphics2D {
		return GLGraphics2D()
	}
	
	override fun dispose(arg0: GLAutoDrawable) {
		if (g2d != null) {
			g2d!!.glDispose()
			g2d = null
		}
	}
}
