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


import com.jogamp.opengl.GLException
import com.jogamp.opengl.glu.GLU
import com.jogamp.opengl.glu.GLUtessellatorCallback
import com.jogamp.opengl.glu.GLUtessellatorCallbackAdapter
import org.anglur.joglext.cacheable.CachedFloatArray
import org.anglur.joglext.jogl2d.VertexBuffer
import java.awt.BasicStroke
import java.awt.geom.PathIterator

/**
 * Fills a shape by tesselating it with the GLU library. This is a slower
 * implementation and `FillNonintersectingPolygonVisitor` should be used
 * when possible.
 */
abstract class AbstractTesselatorVisitor : SimplePathVisitor() {
	
	protected val tesselator by lazy { GLU.gluNewTess() }
	
	protected var callback: GLUtessellatorCallback
	
	/**
	 * Last command was a move to. This is where drawing starts.
	 */
	protected var drawStart = CachedFloatArray(2)
	protected var drawing = false
	
	protected var drawMode: Int = 0
	protected var vBuffer = VertexBuffer(1024)
	
	init {
		callback = TessellatorCallback()
	}
	
	override fun setStroke(stroke: BasicStroke) {
		// nop
	}
	
	override fun beginPoly(windingRule: Int) {
		configureTesselator(windingRule)
		
		GLU.gluTessBeginPolygon(tesselator, null)
	}
	
	protected fun configureTesselator(windingRule: Int) {
		when (windingRule) {
			PathIterator.WIND_EVEN_ODD -> GLU.gluTessProperty(tesselator, GLU.GLU_TESS_WINDING_RULE, GLU.GLU_TESS_WINDING_ODD.toDouble())
			
			PathIterator.WIND_NON_ZERO -> GLU.gluTessProperty(tesselator, GLU.GLU_TESS_WINDING_RULE, GLU.GLU_TESS_WINDING_NONZERO.toDouble())
		}
		
		GLU.gluTessCallback(tesselator, GLU.GLU_TESS_VERTEX, callback)
		GLU.gluTessCallback(tesselator, GLU.GLU_TESS_BEGIN, callback)
		GLU.gluTessCallback(tesselator, GLU.GLU_TESS_END, callback)
		GLU.gluTessCallback(tesselator, GLU.GLU_TESS_ERROR, callback)
		GLU.gluTessCallback(tesselator, GLU.GLU_TESS_COMBINE, callback)
		GLU.gluTessNormal(tesselator, 0.0, 0.0, -1.0)
		
	}
	
	override fun moveTo(vertex: FloatArray) {
		endIfRequired()
		drawStart[0] = vertex[0]
		drawStart[1] = vertex[1]
	}
	
	override fun lineTo(vertex: FloatArray) {
		startIfRequired()
		addVertex(vertex)
	}
	
	private fun addVertex(vertex: FloatArray) {
		val v = DoubleArray(3)
		v[0] = vertex[0].toDouble()
		v[1] = vertex[1].toDouble()
		GLU.gluTessVertex(tesselator, v, 0, v)
	}
	
	override fun closeLine() {
		endIfRequired()
	}
	
	override fun endPoly() {
		// shape may just end on the starting point without calling closeLine
		endIfRequired()
		
		GLU.gluTessEndPolygon(tesselator)
		GLU.gluDeleteTess(tesselator)
	}
	
	private fun startIfRequired() {
		if (!drawing) {
			GLU.gluTessBeginContour(tesselator)
			addVertex(drawStart)
			drawing = true
		}
	}
	
	private fun endIfRequired() {
		if (drawing) {
			GLU.gluTessEndContour(tesselator)
			drawing = false
		}
	}
	
	protected fun beginTess(type: Int) {
		drawMode = type
		vBuffer.clear()
	}
	
	protected fun addTessVertex(vertex: DoubleArray) {
		vBuffer.addVertex(vertex[0].toFloat(), vertex[1].toFloat())
	}
	
	protected abstract fun endTess()
	
	protected inner class TessellatorCallback : GLUtessellatorCallbackAdapter() {
		override fun begin(type: Int) {
			beginTess(type)
		}
		
		override fun end() {
			endTess()
		}
		
		override fun vertex(vertexData: Any?) {
			assert(vertexData is DoubleArray) { "Invalid assumption" }
			addTessVertex(vertexData as DoubleArray)
		}
		
		override fun combine(coords: DoubleArray, data: Array<Any>?, weight: FloatArray?, outData: Array<Any>) {
			outData[0] = coords
		}
		
		override fun error(errnum: Int) {
			throw GLException("Tesselation Error: " + GLU().gluErrorString(errnum))
		}
	}
}
