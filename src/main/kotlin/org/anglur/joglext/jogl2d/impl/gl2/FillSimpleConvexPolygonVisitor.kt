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
import org.anglur.joglext.jogl2d.VertexBuffer
import org.anglur.joglext.jogl2d.impl.SimplePathVisitor
import java.awt.BasicStroke

/**
 * Fills a simple convex polygon. This class does not test to determine if the
 * polygon is actually simple and convex.
 */
class FillSimpleConvexPolygonVisitor : SimplePathVisitor() {
	protected lateinit var gl: GL2
	
	protected var vBuffer = VertexBuffer.sharedBuffer
	
	override fun setGLContext(context: GL) {
		gl = context.gL2
	}
	
	override fun setStroke(stroke: BasicStroke) {
		// nop
	}
	
	override fun beginPoly(windingRule: Int) {
		vBuffer.clear()
		
		/*
     * We don't care what the winding rule is, we disable face culling.
     */
		gl.glDisable(GL.GL_CULL_FACE)
	}
	
	override fun closeLine() {
		vBuffer.drawBuffer(gl, GL2.GL_POLYGON)
	}
	
	override fun endPoly() {
	}
	
	override fun lineTo(vertex: FloatArray) {
		vBuffer.addVertex(vertex, 0, 1)
	}
	
	override fun moveTo(vertex: FloatArray) {
		vBuffer.clear()
		vBuffer.addVertex(vertex, 0, 1)
	}
}
