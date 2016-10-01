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