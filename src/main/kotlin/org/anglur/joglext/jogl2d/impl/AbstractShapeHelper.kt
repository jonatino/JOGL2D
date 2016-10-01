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


import org.anglur.joglext.cacheable.CachedFloatArray
import org.anglur.joglext.jogl2d.GLG2DShapeHelper
import org.anglur.joglext.jogl2d.GLGraphics2D
import org.anglur.joglext.jogl2d.PathVisitor
import org.anglur.joglext.jogl2d.shape.ShapeIterator
import java.awt.BasicStroke
import java.awt.RenderingHints
import java.awt.RenderingHints.Key
import java.awt.Shape
import java.awt.Stroke
import java.awt.geom.*
import java.util.*

abstract class AbstractShapeHelper : GLG2DShapeHelper {
	
	protected var strokeStack: Deque<Stroke> = ArrayDeque()
	
	init {
		strokeStack.push(BasicStroke())
	}
	
	override fun setG2D(g2d: GLGraphics2D) {
		strokeStack.clear()
		strokeStack.push(BasicStroke())
	}
	
	override fun push(newG2d: GLGraphics2D) {
		strokeStack.push(newG2d.getStroke())
	}
	
	override fun pop(parentG2d: GLGraphics2D) {
		strokeStack.pop()
	}
	
	override fun setHint(key: Key, value: Any?) {
		// nop
	}
	
	override fun resetHints() {
		setHint(RenderingHints.KEY_ANTIALIASING, null)
	}
	
	override fun dispose() {
		// nop
	}
	
	override var stroke: Stroke
		get() = strokeStack.peek()
		set(stroke) {
			strokeStack.pop()
			strokeStack.push(stroke)
		}
	
	override fun drawRoundRect(x: Int, y: Int, width: Int, height: Int, arcWidth: Int, arcHeight: Int, fill: Boolean) {
		ROUND_RECT.setRoundRect(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), arcWidth.toFloat(), arcHeight.toFloat())
		if (fill) {
			fill(ROUND_RECT, true)
		} else {
			draw(ROUND_RECT)
		}
	}
	
	override fun drawRect(x: Int, y: Int, width: Int, height: Int, fill: Boolean) {
		RECT.setRect(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat())
		if (fill) {
			fill(RECT, true)
		} else {
			draw(RECT)
		}
	}
	
	override fun drawLine(x1: Int, y1: Int, x2: Int, y2: Int) {
		LINE.setLine(x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat())
		draw(LINE)
	}
	
	override fun drawOval(x: Int, y: Int, width: Int, height: Int, fill: Boolean) {
		ELLIPSE.setFrame(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat())
		if (fill) {
			fill(ELLIPSE, true)
		} else {
			draw(ELLIPSE)
		}
	}
	
	override fun drawArc(x: Int, y: Int, width: Int, height: Int, startAngle: Int, arcAngle: Int, fill: Boolean) {
		ARC.setArc(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble(), startAngle.toDouble(), arcAngle.toDouble(), if (fill) Arc2D.PIE else Arc2D.OPEN)
		if (fill) {
			fill(ARC, true)
		} else {
			draw(ARC)
		}
	}
	
	override fun drawPolyline(xPoints: IntArray, yPoints: IntArray, nPoints: Int) {
		drawPoly(xPoints, yPoints, nPoints, false, false)
	}
	
	override fun drawPolygon(xPoints: IntArray, yPoints: IntArray, nPoints: Int, fill: Boolean) {
		drawPoly(xPoints, yPoints, nPoints, fill, true)
	}
	
	protected fun drawPoly(xPoints: IntArray, yPoints: IntArray, nPoints: Int, fill: Boolean, close: Boolean) {
		val path = Path2D.Float(PathIterator.WIND_NON_ZERO, nPoints)
		path.moveTo(xPoints[0].toFloat(), yPoints[0].toFloat())
		for (i in 1..nPoints - 1) {
			path.lineTo(xPoints[i].toFloat(), yPoints[i].toFloat())
		}
		
		if (close) {
			path.closePath()
		}
		
		if (fill) {
			fill(path)
		} else {
			draw(path)
		}
	}
	
	override fun fill(shape: Shape) {
		if (shape is Rectangle2D ||
				shape is Ellipse2D ||
				shape is Arc2D ||
				shape is RoundRectangle2D) {
			fill(shape, true)
		} else {
			fill(shape, false)
		}
	}
	
	protected abstract fun fill(shape: Shape, isDefinitelySimpleConvex: Boolean)
	
	protected fun traceShape(shape: Shape, visitor: PathVisitor) {
		visitShape(shape, visitor)
	}
	
	companion object {
		/**
		 * We know this is single-threaded, so we can use these as archetypes.
		 */
		protected val ELLIPSE = Ellipse2D.Float()
		protected val ROUND_RECT = RoundRectangle2D.Float()
		protected val ARC = Arc2D.Float()
		protected val RECT = Rectangle2D.Float()
		protected val LINE = Line2D.Float()
		
		fun visitShape(shape: Shape, visitor: PathVisitor) {
			val iterator = ShapeIterator.get(shape)
			visitor.beginPoly(iterator.windingRule)
			
			val coords = CachedFloatArray(10)
			val previousVertex = CachedFloatArray(2)
			while (!iterator.isDone) {
				val type = iterator.currentSegment(coords)
				when (type) {
					PathIterator.SEG_MOVETO -> visitor.moveTo(coords)
					
					PathIterator.SEG_LINETO -> visitor.lineTo(coords)
					
					PathIterator.SEG_QUADTO -> visitor.quadTo(previousVertex, coords)
					
					PathIterator.SEG_CUBICTO -> visitor.cubicTo(previousVertex, coords)
					
					PathIterator.SEG_CLOSE -> visitor.closeLine()
				}
				
				when (type) {
					PathIterator.SEG_LINETO, PathIterator.SEG_MOVETO -> {
						previousVertex[0] = coords[0]
						previousVertex[1] = coords[1]
					}
					
					PathIterator.SEG_QUADTO -> {
						previousVertex[0] = coords[2]
						previousVertex[1] = coords[3]
					}
					
					PathIterator.SEG_CUBICTO -> {
						previousVertex[0] = coords[4]
						previousVertex[1] = coords[5]
					}
				}
				iterator.next()
			}
			
			visitor.endPoly()
		}
	}
}