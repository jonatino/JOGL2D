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
import org.anglur.joglext.jogl2d.impl.BasicStrokeLineVisitor
import java.awt.BasicStroke


/**
 * Draws a line, as outlined by a [BasicStroke]. The current
 * implementation supports everything except dashes. This class draws a series
 * of quads for each line segment, joins corners and endpoints as appropriate.
 */
class LineDrawingVisitor : BasicStrokeLineVisitor() {
	
	private lateinit var gl: GL2
	
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
	
	override fun drawBuffer() =
			vBuffer.drawBuffer(gl, GL.GL_TRIANGLE_STRIP)
	
}
