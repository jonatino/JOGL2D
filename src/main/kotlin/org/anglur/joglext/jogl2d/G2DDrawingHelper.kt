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

import java.awt.RenderingHints

/**
 * Assists in the drawing of a particular aspect of the Graphics2D object. This
 * allows the drawing to be segregated into certain aspects, such as image, text
 * or shape drawing.
 */
interface G2DDrawingHelper {
	/**
	 * Sets the current `GLGraphics2D` parent. The current `GL` and
	 * `GLContext` objects can be accessed from this. This should clear all
	 * internal stacks in the helper object because previous painting iterations
	 * may not have called dispose() for each time they called create().
	 
	 * @param g2d The parent context for subsequent drawing operations.
	 */
	fun setG2D(g2d: GLGraphics2D)
	
	/**
	 * Sets the new `GLGraphics2D` context in a stack. This is called when
	 * `Graphics2D.create()` is called and each helper is given notice to
	 * push any necessary information onto the stack. This is used in conjunction
	 * with [.pop].
	 
	 * @param newG2d The new context, top of the stack.
	 */
	fun push(newG2d: GLGraphics2D)
	
	/**
	 * Sets the new `GLGraphics2D` context in a stack after a pop. This is
	 * called when `Graphics2D.dispose()` is called and each helper is given
	 * notice to pop any necessary information off the stack. This is used in
	 * conjunction with [.push].
	 
	 * @param parentG2d The new context, top of the stack - which is actually the parent
	 * *                  of what was popped.
	 */
	fun pop(parentG2d: GLGraphics2D)
	
	/**
	 * Sets a new rendering hint. The state of all rendering hints is kept by the
	 * `GLGraphics2D` object, but all new state changes are propagated to
	 * all listeners.
	 
	 * @param key   The rendering hint key.
	 * *
	 * @param value The new hint value.
	 */
	fun setHint(key: RenderingHints.Key, value: Any?)
	
	/**
	 * Clears all hints back to their default states.
	 */
	fun resetHints()
	
	/**
	 * Disposes the helper object. This is not called during the dispose operation
	 * of the `Graphics2D` object. This should dispose all GL resources when
	 * all drawing is finished and no more calls will be executing on this OpenGL
	 * context and these resources.
	 */
	fun dispose()
}
