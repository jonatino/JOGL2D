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
package org.anglur.joglext.jogl2d.impl


import org.anglur.joglext.jogl2d.GLG2DTextHelper
import org.anglur.joglext.jogl2d.GLGraphics2D
import java.awt.Font
import java.awt.FontMetrics
import java.awt.RenderingHints
import java.awt.RenderingHints.Key
import java.awt.font.FontRenderContext
import java.lang.Math.ceil
import java.util.*

abstract class AbstractTextDrawer : GLG2DTextHelper {
	
	protected lateinit var g2d: GLGraphics2D
	
	protected var stack: Deque<FontState> = ArrayDeque()
	
	override fun setG2D(g2d: GLGraphics2D) {
		this.g2d = g2d
		
		stack.clear()
		stack.push(FontState())
	}
	
	override fun push(newG2d: GLGraphics2D) {
		stack.push(stack.peek().clone())
	}
	
	override fun pop(parentG2d: GLGraphics2D) {
		stack.pop()
	}
	
	override fun setHint(key: Key, value: Any?) {
		if (key === RenderingHints.KEY_TEXT_ANTIALIASING) {
			stack.peek().antiAlias = value === RenderingHints.VALUE_TEXT_ANTIALIAS_ON
		}
	}
	
	override fun resetHints() {
		setHint(RenderingHints.KEY_TEXT_ANTIALIASING, null)
	}
	
	override var font: Font
		get() = stack.peek().font!!
		set(font) {
			stack.peek().font = font
		}
	
	override fun getFontMetrics(font: Font): FontMetrics {
		return GLFontMetrics(font, fontRenderContext)
	}
	
	override val fontRenderContext: FontRenderContext
		get() = FontRenderContext(g2d.transform, stack.peek().antiAlias, false)
	
	/**
	 * The default implementation is good enough for now.
	 */
	class GLFontMetrics(font: Font, private var frc: FontRenderContext) : FontMetrics(font) {
		
		override fun getFontRenderContext(): FontRenderContext {
			return frc
		}
		
		override fun charsWidth(data: CharArray, off: Int, len: Int): Int {
			if (len <= 0) {
				return 0
			}
			
			val bounds = font.getStringBounds(data, off, len, frc)
			return ceil(bounds.width).toInt()
		}
		
		companion object {
			private val serialVersionUID = 3676850359220061793L
		}
	}
	
	protected class FontState : Cloneable {
		var font: Font? = null
		var antiAlias: Boolean = false
		
		public override fun clone(): FontState {
			try {
				return super.clone() as FontState
			} catch (e: CloneNotSupportedException) {
				throw AssertionError(e)
			}
			
		}
	}
}
