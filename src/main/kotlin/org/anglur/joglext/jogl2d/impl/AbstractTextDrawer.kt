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
	
	private var stack: Deque<FontState> = ArrayDeque()
	
	private val EMPTY = FontState()
	
	override fun setG2D(g2d: GLGraphics2D) {
		this.g2d = g2d
		
		stack.clear()
		stack.push(EMPTY.reset())
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
	
	private class FontState : Cloneable {
		var font: Font? = null
		var antiAlias: Boolean = false
		
		public override fun clone(): FontState {
			try {
				return super.clone() as FontState
			} catch (e: CloneNotSupportedException) {
				throw AssertionError(e)
			}
			
		}
		
		fun reset() = apply {
			font = null
			antiAlias = false
		}
		
	}
}
