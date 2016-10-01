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
