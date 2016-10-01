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
import org.anglur.joglext.cacheable.CachedFloatArray
import org.anglur.joglext.jogl2d.VertexBuffer
import org.anglur.joglext.jogl2d.impl.SimplePathVisitor
import java.awt.BasicStroke

/**
 * Draws a line using the native GL implementation of a line. This is only
 * appropriate if the width of the line is less than a certain number of pixels
 * (not coordinate units) so that the user cannot see that the join and
 * endpoints are different. See [.isValid] for a set of
 * useful criteria.
 */
class FastLineVisitor : SimplePathVisitor() {
	
	private var testMatrix = CachedFloatArray(16)
	
	private var buffer = VertexBuffer.sharedBuffer
	
	private lateinit var gl: GL2
	
	private lateinit var stroke2: BasicStroke
	
	private var glLineWidth: Float = 0.toFloat()
	
	override fun setGLContext(context: GL) {
		gl = context.gL2
	}
	
	override fun setStroke(stroke: BasicStroke) {
		gl.glLineWidth(glLineWidth)
		gl.glPointSize(glLineWidth)
		
		/*
     * Not perfect copy of the BasicStroke implementation, but it does get
     * decently close. The pattern is pretty much the same. I think it's pretty
     * much impossible to do with out a fragment shader and only the fixed
     * function pipeline.
     */
		val dash = stroke.dashArray
		if (dash != null) {
			var totalLength = 0f
			for (f in dash) {
				totalLength += f
			}
			
			var lengthSoFar = 0f
			var prevIndex = 0
			var mask = 0
			for (i in dash.indices) {
				lengthSoFar += dash[i]
				
				val nextIndex = (lengthSoFar / totalLength * 16).toInt()
				for (j in prevIndex..nextIndex - 1) {
					mask = mask or (i.inv() and 1 shl j)
				}
				
				prevIndex = nextIndex
			}
			
			/*
       * XXX Should actually use the stroke phase, but not sure how yet.
       */
			
			gl.glEnable(GL2.GL_LINE_STIPPLE)
			val factor = totalLength.toInt()
			gl.glLineStipple(factor shr 4, mask.toShort())
		} else {
			gl.glDisable(GL2.GL_LINE_STIPPLE)
		}
		
		this.stroke2 = stroke
	}
	
	/**
	 * Returns `true` if this class can reasonably render the line. This
	 * takes into account whether or not the transform will blow the line width
	 * out of scale and it obvious that we aren't drawing correct corners and line
	 * endings.
	 *
	 *
	 *
	 *
	 * Note: This must be called before [.setStroke]. If this
	 * returns `false` then this renderer should not be used.
	 *
	 */
	fun isValid(stroke: BasicStroke): Boolean {
		// if the dash length is odd, I don't know how to handle that yet
		val dash = stroke.dashArray
		if (dash != null && dash.size and 1 == 1) {
			return false
		}
		
		gl.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, testMatrix, 0)
		
		val scaleX = Math.abs(testMatrix[0])
		val scaleY = Math.abs(testMatrix[5])
		
		// scales are different, we can't get a good line width
		if (Math.abs(scaleX - scaleY) > 1e-6) {
			return false
		}
		
		val strokeWidth = stroke.lineWidth
		
		// gl line width is in pixels, convert to pixel width
		glLineWidth = strokeWidth * scaleX
		
		// we'll only try if it's a thin line
		return glLineWidth <= 2
	}
	
	override fun moveTo(vertex: FloatArray) {
		drawLine(false)
		drawLine(false)
		buffer.addVertex(vertex, 0, 1)
	}
	
	override fun lineTo(vertex: FloatArray) {
		buffer.addVertex(vertex, 0, 1)
	}
	
	override fun closeLine() {
		drawLine(true)
	}
	
	private fun drawLine(close: Boolean) {
		val buf = buffer.buffer
		val p = buf.position()
		buffer.drawBuffer(gl, if (close) GL2.GL_LINE_LOOP else GL2.GL_LINE_STRIP)
		
		/*
     * We'll ignore butt endcaps, but we'll pretend like we're drawing round,
     * bevel or miter corners as well as round or square corners by just putting
     * a point there. Since our line should be very thin, pixel-wise, it
     * shouldn't be noticeable.
     */
		if (stroke2.dashArray == null) {
			buf.position(p)
			buffer.drawBuffer(gl, GL2.GL_POINTS)
		}
		
		buffer.clear()
	}
	
	override fun beginPoly(windingRule: Int) {
		buffer.clear()
		
		/*
     * pen hangs down and to the right. See java.awt.Graphics
     */
		gl.glMatrixMode(GL2.GL_MODELVIEW)
		gl.glPushMatrix()
		gl.glTranslatef(0.5f, 0.5f, 0f)
		
		gl.glPushAttrib(GL2.GL_LINE_BIT or GL2.GL_POINT_BIT)
	}
	
	override fun endPoly() {
		drawLine(false)
		gl.glDisable(GL2.GL_LINE_STIPPLE)
		gl.glPopMatrix()
		
		gl.glPopAttrib()
	}
	
}
