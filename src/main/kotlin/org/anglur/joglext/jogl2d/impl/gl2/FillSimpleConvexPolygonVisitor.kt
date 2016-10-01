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
