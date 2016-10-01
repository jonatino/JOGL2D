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


import java.awt.Shape
import java.awt.Stroke

interface GLG2DShapeHelper : G2DDrawingHelper {
	
	var stroke: Stroke
	
	fun drawRoundRect(x: Int, y: Int, width: Int, height: Int, arcWidth: Int, arcHeight: Int, fill: Boolean)
	
	fun drawRect(x: Int, y: Int, width: Int, height: Int, fill: Boolean)
	
	fun drawLine(x1: Int, y1: Int, x2: Int, y2: Int)
	
	fun drawOval(x: Int, y: Int, width: Int, height: Int, fill: Boolean)
	
	fun drawArc(x: Int, y: Int, width: Int, height: Int, startAngle: Int, arcAngle: Int, fill: Boolean)
	
	fun drawPolyline(xPoints: IntArray, yPoints: IntArray, nPoints: Int)
	
	fun drawPolygon(xPoints: IntArray, yPoints: IntArray, nPoints: Int, fill: Boolean)
	
	fun draw(shape: Shape)
	
	fun fill(shape: Shape)
}