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

import java.awt.Font
import java.awt.FontMetrics
import java.awt.font.FontRenderContext
import java.text.AttributedCharacterIterator

interface GLG2DTextHelper : G2DDrawingHelper {
	
	var font: Font
	
	fun getFontMetrics(font: Font): FontMetrics
	
	val fontRenderContext: FontRenderContext
	
	fun drawString(iterator: AttributedCharacterIterator, x: Int, y: Int)
	
	fun drawString(iterator: AttributedCharacterIterator, x: Float, y: Float)
	
	fun drawString(string: String, x: Float, y: Float)
	
	fun drawString(string: String, x: Int, y: Int)
	
}