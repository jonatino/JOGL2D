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
import org.anglur.joglext.jogl2d.impl.BasicStrokeLineVisitor
import java.awt.BasicStroke


/**
 * Draws a line, as outlined by a [BasicStroke]. The current
 * implementation supports everything except dashes. This class draws a series
 * of quads for each line segment, joins corners and endpoints as appropriate.
 */
class LineDrawingVisitor : BasicStrokeLineVisitor() {
	protected lateinit var gl: GL2
	
	override fun setGLContext(context: GL) {
		gl = context.gL2
	}
	
	override fun beginPoly(windingRule: Int) {
		/*
	 * pen hangs down and to the right. See java.awt.Graphics
     */
		gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW)
		gl.glPushMatrix()
		gl.glTranslatef(0.5f, 0.5f, 0f)
		
		super.beginPoly(windingRule)
	}
	
	override fun endPoly() {
		super.endPoly()
		
		gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW)
		gl.glPopMatrix()
	}
	
	override fun drawBuffer() {
		vBuffer.drawBuffer(gl, GL.GL_TRIANGLE_STRIP)
	}
}
