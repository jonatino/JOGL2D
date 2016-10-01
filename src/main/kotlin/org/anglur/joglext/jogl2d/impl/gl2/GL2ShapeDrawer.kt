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
import org.anglur.joglext.jogl2d.GLGraphics2D
import org.anglur.joglext.jogl2d.impl.AbstractShapeHelper
import org.anglur.joglext.jogl2d.impl.SimpleOrTesselatingVisitor
import java.awt.BasicStroke
import java.awt.RenderingHints
import java.awt.RenderingHints.Key
import java.awt.Shape

class GL2ShapeDrawer : AbstractShapeHelper() {
	
	private lateinit var gl: GL2
	
	private var simpleFillVisitor: FillSimpleConvexPolygonVisitor
	private var complexFillVisitor: SimpleOrTesselatingVisitor
	private var simpleStrokeVisitor: LineDrawingVisitor
	private var fastLineVisitor: FastLineVisitor
	
	init {
		simpleFillVisitor = FillSimpleConvexPolygonVisitor()
		complexFillVisitor = SimpleOrTesselatingVisitor(simpleFillVisitor, GL2TesselatorVisitor())
		simpleStrokeVisitor = LineDrawingVisitor()
		fastLineVisitor = FastLineVisitor()
	}
	
	override fun setG2D(g2d: GLGraphics2D) {
		super.setG2D(g2d)
		val gl = g2d.glContext.gl
		simpleFillVisitor.setGLContext(gl)
		complexFillVisitor.setGLContext(gl)
		simpleStrokeVisitor.setGLContext(gl)
		fastLineVisitor.setGLContext(gl)
		
		this.gl = gl.gL2
	}
	
	override fun setHint(key: Key, value: Any?) {
		super.setHint(key, value)
		
		if (key === RenderingHints.KEY_ANTIALIASING) {
			if (value === RenderingHints.VALUE_ANTIALIAS_ON) {
				gl.glEnable(GL.GL_MULTISAMPLE)
			} else {
				gl.glDisable(GL.GL_MULTISAMPLE)
			}
		}
	}
	
	override fun draw(shape: Shape) {
		val stroke = stroke
		if (stroke is BasicStroke) {
			if (fastLineVisitor.isValid(stroke)) {
				fastLineVisitor.setStroke(stroke)
				traceShape(shape, fastLineVisitor)
				return
			} else if (stroke.dashArray == null) {
				simpleStrokeVisitor.setStroke(stroke)
				traceShape(shape, simpleStrokeVisitor)
				return
			}
		}
		
		// can fall through for various reasons
		fill(stroke.createStrokedShape(shape))
	}
	
	override fun fill(shape: Shape, forceSimple: Boolean)
			= traceShape(shape, if (forceSimple) simpleFillVisitor else complexFillVisitor)
}
