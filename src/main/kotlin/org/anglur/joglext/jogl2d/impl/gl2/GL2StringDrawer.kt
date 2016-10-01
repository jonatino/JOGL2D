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


import com.jogamp.opengl.*
import com.jogamp.opengl.glu.gl2.GLUgl2
import com.jogamp.opengl.util.gl2.GLUT.*
import org.anglur.joglext.jogl2d.font.*
import org.anglur.joglext.jogl2d.impl.AbstractTextDrawer
import java.text.AttributedCharacterIterator

/**
 * Draws text for the `GLGraphics2D` class.
 */
class GL2StringDrawer : AbstractTextDrawer() {
	
	override fun dispose() {
	}
	
	override fun drawString(iterator: AttributedCharacterIterator, x: Float, y: Float) {
		drawString(iterator, x.toInt(), y.toInt())
	}
	
	override fun drawString(iterator: AttributedCharacterIterator, x: Int, y: Int) {
		val builder = StringBuilder(iterator.endIndex - iterator.beginIndex)
		while (iterator.next() != AttributedCharacterIterator.DONE) {
			builder.append(iterator.current())
		}
		
		drawString(builder.toString(), x, y)
	}
	
	override fun drawString(string: String, x: Float, y: Float) {
		drawString(string, x.toInt(), y.toInt())
	}
	
	override fun drawString(string: String, x: Int, y: Int) {
		g2d.glContext.gl.gL2.glRasterPos2d(x.toDouble(), y.toDouble())
		glutBitmapString(BITMAP_TIMES_ROMAN_10, string)
	}
	
	val swapbytes = IntArray(1)
	val lsbfirst = IntArray(1)
	val rowlength = IntArray(1)
	val skiprows = IntArray(1)
	val skippixels = IntArray(1)
	val alignment = IntArray(1)
	
	fun glutBitmapString(font: Int, string: String) {
		val gl = GLUgl2.getCurrentGL2()
		
		beginBitmap(gl)
		val len = string.length
		for (i in 0..len - 1) {
			bitmapCharacterImpl(gl, font, string[i])
		}
		endBitmap(gl)
	}
	
	private fun beginBitmap(gl: GL2) {
		gl.glGetIntegerv(GL2GL3.GL_UNPACK_SWAP_BYTES, swapbytes, 0)
		gl.glGetIntegerv(GL2GL3.GL_UNPACK_LSB_FIRST, lsbfirst, 0)
		gl.glGetIntegerv(GL2ES2.GL_UNPACK_ROW_LENGTH, rowlength, 0)
		gl.glGetIntegerv(GL2ES2.GL_UNPACK_SKIP_ROWS, skiprows, 0)
		gl.glGetIntegerv(GL2ES2.GL_UNPACK_SKIP_PIXELS, skippixels, 0)
		gl.glGetIntegerv(GL.GL_UNPACK_ALIGNMENT, alignment, 0)
		gl.glPixelStorei(GL2GL3.GL_UNPACK_SWAP_BYTES, GL.GL_FALSE)
		gl.glPixelStorei(GL2GL3.GL_UNPACK_LSB_FIRST, GL.GL_FALSE)
		gl.glPixelStorei(GL2ES2.GL_UNPACK_ROW_LENGTH, 0)
		gl.glPixelStorei(GL2ES2.GL_UNPACK_SKIP_ROWS, 0)
		gl.glPixelStorei(GL2ES2.GL_UNPACK_SKIP_PIXELS, 0)
		gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1)
	}
	
	private fun endBitmap(gl: GL2) {
		/* Restore saved modes. */
		gl.glPixelStorei(GL2GL3.GL_UNPACK_SWAP_BYTES, swapbytes[0])
		gl.glPixelStorei(GL2GL3.GL_UNPACK_LSB_FIRST, lsbfirst[0])
		gl.glPixelStorei(GL2ES2.GL_UNPACK_ROW_LENGTH, rowlength[0])
		gl.glPixelStorei(GL2ES2.GL_UNPACK_SKIP_ROWS, skiprows[0])
		gl.glPixelStorei(GL2ES2.GL_UNPACK_SKIP_PIXELS, skippixels[0])
		gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, alignment[0])
	}
	
	private fun bitmapCharacterImpl(gl: GL2, font: Int, cin: Char) {
		val fontinfo = getBitmapFont(font)
		val c = cin.toInt() and 0xFFFF
		if (c < fontinfo.first || c >= fontinfo.first + fontinfo.num_chars)
			return
		val ch = fontinfo.ch[c - fontinfo.first]
		if (ch != null) {
			gl.glBitmap(ch.width, ch.height, ch.xorig, ch.yorig,
					ch.advance, 0f, ch.bitmap, 0)
		}
	}
	
	private val bitmapFonts = arrayOfNulls<BitmapFontRec>(9)
	
	private fun getBitmapFont(font: Int): BitmapFontRec {
		var rec: BitmapFontRec? = bitmapFonts[font]
		if (rec == null) {
			when (font) {
				BITMAP_9_BY_15 -> rec = GLUTBitmap9x15.glutBitmap9By15
				BITMAP_8_BY_13 -> rec = GLUTBitmap8x13.glutBitmap8By13
				BITMAP_TIMES_ROMAN_10 -> rec = GLUTBitmapTimesRoman10.glutBitmapTimesRoman10
				BITMAP_TIMES_ROMAN_24 -> rec = GLUTBitmapTimesRoman24.glutBitmapTimesRoman24
				BITMAP_HELVETICA_10 -> rec = GLUTBitmapHelvetica10.glutBitmapHelvetica10
				BITMAP_HELVETICA_12 -> rec = GLUTBitmapHelvetica12.glutBitmapHelvetica12
				BITMAP_HELVETICA_18 -> rec = GLUTBitmapHelvetica18.glutBitmapHelvetica18
				else -> throw GLException("Unknown bitmap font number " + font)
			}
			bitmapFonts[font] = rec
		}
		return rec
	}
	
}
