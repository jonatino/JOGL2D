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

import java.awt.Color
import java.awt.Composite
import java.awt.Paint

interface GLG2DColorHelper : G2DDrawingHelper {
	
	var composite: Composite
	
	var paint: Paint
	
	var color: Color
	
	fun setColorNoRespectComposite(c: Color)
	
	fun setColorRespectComposite(c: Color)
	
	var background: Color
	
	fun setPaintMode()
	
	fun setXORMode(c: Color)
	
	fun copyArea(x: Int, y: Int, width: Int, height: Int, dx: Int, dy: Int)
}