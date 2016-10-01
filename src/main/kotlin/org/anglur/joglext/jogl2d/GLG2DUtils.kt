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

import com.jogamp.opengl.GL
import com.jogamp.opengl.GL2ES1
import org.anglur.joglext.cacheable.CachedIntArray
import java.awt.Color

object GLG2DUtils {
	
	fun setColor(gl: GL2ES1, c: Color, preMultiplyAlpha: Float) {
		val rgb = c.rgb
		gl.glColor4ub((rgb shr 16 and 0xFF).toByte(), (rgb shr 8 and 0xFF).toByte(), (rgb and 0xFF).toByte(), ((rgb shr 24 and 0xFF) * preMultiplyAlpha).toByte())
	}
	
	fun getGLColor(c: Color) = c.getComponents(null)
	
	private val viewportDimensions = CachedIntArray(4)
	
	fun getViewportHeight(gl: GL): Int {
		gl.glGetIntegerv(GL.GL_VIEWPORT, viewportDimensions, 0)
		val canvasHeight = viewportDimensions[3]
		return canvasHeight
	}
	
	fun ensureIsGLBuffer(gl: GL, bufferId: Int): Int {
		if (gl.glIsBuffer(bufferId)) {
			return bufferId
		} else {
			return genBufferId(gl)
		}
	}
	
	private val ids = CachedIntArray(1)
	
	fun genBufferId(gl: GL): Int {
		gl.glGenBuffers(1, ids, 0)
		return ids[0]
	}
}
