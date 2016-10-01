package org.anglur.joglext.jogl2d.shape

import org.anglur.joglext.jogl2d.shape.impl.*
import java.awt.Shape
import java.awt.geom.*

/**
 * Created by Jonathan on 9/30/2016.
 */
object ShapeIterator {
	
	fun get(shape: Shape): PathIterator {
		when (shape) {
			is Ellipse2D -> return EllipseIterator(shape)
			is RoundRectangle2D -> return RoundRectIterator(shape)
			is Arc2D -> return ArcIterator(shape)
			is Rectangle2D -> return RectIterator(shape)
			is Line2D -> return LineIterator(shape)
		}
		throw RuntimeException("Unknown shape! ${shape.javaClass.simpleName}")
	}
	
}
