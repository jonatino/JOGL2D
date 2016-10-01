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
import com.jogamp.opengl.GL2ES1
import com.jogamp.opengl.util.texture.Texture
import org.anglur.joglext.jogl2d.GLGraphics2D
import org.anglur.joglext.jogl2d.impl.AbstractImageHelper
import java.awt.Color
import java.awt.geom.AffineTransform

class GL2ImageDrawer : AbstractImageHelper() {
	
	private lateinit var gl: GL2
	
	private var savedTransform: AffineTransform? = null
	
	override fun setG2D(g2d: GLGraphics2D) {
		super.setG2D(g2d)
		gl = g2d.glContext.gl.gL2
	}
	
	override fun begin(texture: Texture, xform: AffineTransform?, bgcolor: Color) {
		gl.glTexEnvi(GL2ES1.GL_TEXTURE_ENV, GL2ES1.GL_TEXTURE_ENV_MODE, GL2ES1.GL_MODULATE)
		gl.glTexParameterf(GL2ES1.GL_TEXTURE_ENV, GL2ES1.GL_TEXTURE_ENV_MODE, GL.GL_BLEND.toFloat())
		
		g2d.composite = g2d.composite
		
		texture.enable(gl)
		texture.bind(gl)
		
		savedTransform = null
		if (xform != null && !xform.isIdentity) {
			savedTransform = g2d.transform
			g2d.transform(xform)
		}
		
		g2d.colorHelper.setColorRespectComposite(bgcolor)
	}
	
	override fun end(texture: Texture) {
		if (savedTransform != null) {
			g2d.transform = savedTransform!!
		}
		
		texture.disable(gl)
		g2d.colorHelper.setColorRespectComposite(g2d.color)
	}
	
	override fun applyTexture(texture: Texture, dx1: Int, dy1: Int, dx2: Int, dy2: Int, sx1: Float, sy1: Float, sx2: Float, sy2: Float) {
		gl.glBegin(GL2.GL_QUADS)
		
		// SW
		gl.glTexCoord2f(sx1, sy2)
		gl.glVertex2i(dx1, dy2)
		// SE
		gl.glTexCoord2f(sx2, sy2)
		gl.glVertex2i(dx2, dy2)
		// NE
		gl.glTexCoord2f(sx2, sy1)
		gl.glVertex2i(dx2, dy1)
		// NW
		gl.glTexCoord2f(sx1, sy1)
		gl.glVertex2i(dx1, dy1)
		
		gl.glEnd()
	}
	
}
