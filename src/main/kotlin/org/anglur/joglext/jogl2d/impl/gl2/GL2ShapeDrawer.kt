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
import org.anglur.joglext.jogl2d.GLGraphics2D
import org.anglur.joglext.jogl2d.impl.AbstractShapeHelper
import org.anglur.joglext.jogl2d.impl.SimpleOrTesselatingVisitor
import java.awt.BasicStroke
import java.awt.RenderingHints
import java.awt.RenderingHints.Key
import java.awt.Shape

class GL2ShapeDrawer : AbstractShapeHelper() {
	protected lateinit var gl: GL2
	
	protected var simpleFillVisitor: FillSimpleConvexPolygonVisitor
	protected var complexFillVisitor: SimpleOrTesselatingVisitor
	protected var simpleStrokeVisitor: LineDrawingVisitor
	protected var fastLineVisitor: FastLineVisitor
	
	init {
		simpleFillVisitor = FillSimpleConvexPolygonVisitor()
		complexFillVisitor = SimpleOrTesselatingVisitor(simpleFillVisitor, GL2TesselatorVisitor())
		simpleStrokeVisitor = LineDrawingVisitor()
		fastLineVisitor = FastLineVisitor()
	}
	
	override fun setG2D(g2d: GLGraphics2D) {
		super.setG2D(g2d)
		val gl = g2d.glContext.getGL()
		simpleFillVisitor.setGLContext(gl)
		complexFillVisitor.setGLContext(gl)
		simpleStrokeVisitor.setGLContext(gl)
		fastLineVisitor.setGLContext(gl)
		
		this.gl = gl.getGL2()
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
	
	override fun fill(shape: Shape, forceSimple: Boolean) {
		if (forceSimple) {
			traceShape(shape, simpleFillVisitor)
		} else {
			traceShape(shape, complexFillVisitor)
		}
	}
}
