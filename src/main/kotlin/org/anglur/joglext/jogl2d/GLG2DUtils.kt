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

import com.jogamp.opengl.GL
import com.jogamp.opengl.GL2ES1
import java.awt.Color
import java.util.logging.Level
import java.util.logging.Logger

object GLG2DUtils {
	private val LOGGER = Logger.getLogger(GLG2DUtils::class.java.name)
	
	fun setColor(gl: GL2ES1, c: Color, preMultiplyAlpha: Float) {
		val rgb = c.rgb
		gl.glColor4ub((rgb shr 16 and 0xFF).toByte(), (rgb shr 8 and 0xFF).toByte(), (rgb and 0xFF).toByte(), ((rgb shr 24 and 0xFF) * preMultiplyAlpha).toByte())
	}
	
	fun getGLColor(c: Color): FloatArray {
		return c.getComponents(null)
	}
	
	fun getViewportHeight(gl: GL): Int {
		val viewportDimensions = IntArray(4)
		gl.glGetIntegerv(GL.GL_VIEWPORT, viewportDimensions, 0)
		val canvasHeight = viewportDimensions[3]
		return canvasHeight
	}
	
	fun logGLError(gl: GL) {
		val error = gl.glGetError()
		if (error != GL.GL_NO_ERROR) {
			LOGGER.log(Level.SEVERE, "GL Error: code " + error)
		}
	}
	
	fun ensureIsGLBuffer(gl: GL, bufferId: Int): Int {
		if (gl.glIsBuffer(bufferId)) {
			return bufferId
		} else {
			return genBufferId(gl)
		}
	}
	
	fun genBufferId(gl: GL): Int {
		val ids = IntArray(1)
		gl.glGenBuffers(1, ids, 0)
		return ids[0]
	}
}
