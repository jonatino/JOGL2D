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

import com.jogamp.opengl.GL
import com.jogamp.opengl.GLContext
import com.jogamp.opengl.GLDrawable
import org.anglur.joglext.jogl2d.impl.GLGraphicsConfiguration
import org.anglur.joglext.jogl2d.impl.gl2.*
import java.awt.*
import java.awt.RenderingHints.Key
import java.awt.font.FontRenderContext
import java.awt.font.GlyphVector
import java.awt.geom.AffineTransform
import java.awt.geom.NoninvertibleTransformException
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage
import java.awt.image.BufferedImageOp
import java.awt.image.ImageObserver
import java.awt.image.RenderedImage
import java.awt.image.renderable.RenderableImage
import java.text.AttributedCharacterIterator
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Implements the standard `Graphics2D` functionality, but instead draws
 * to an OpenGL canvas.
 */
class GLGraphics2D : Graphics2D(), Cloneable {
	/**
	 * The parent graphics object, if we have one. This reference is used to pass
	 * control back to the parent.
	 */
	protected var parent: GLGraphics2D? = null
	
	/**
	 * When we are painting, this is the drawable/context we're painting into.
	 */
	protected lateinit var glDrawable: GLDrawable
	lateinit var glContext: GLContext
	
	/**
	 * Ensures we only dispose() once.
	 */
	private var isDisposed: Boolean = false
	
	/**
	 * Keeps the current viewport height for things like painting text.
	 */
	var canvasHeight: Int = 0
		private set
	
	/**
	 * All the drawing helpers or listeners to drawing events.
	 */
	protected var helpers = ArrayList<G2DDrawingHelper>()
	
	/*
   * The following are specific drawing helpers used explicitly.
   */
	
	val shapeHelper = addG2DDrawingHelper(GL2ShapeDrawer()) as GLG2DShapeHelper
	
	protected val imageHelper = addG2DDrawingHelper(GL2ImageDrawer()) as GLG2DImageHelper
	
	val stringHelper = addG2DDrawingHelper(GL2StringDrawer()) as GLG2DTextHelper
	
	val matrixHelper = addG2DDrawingHelper(GL2Transformhelper()) as GLG2DTransformHelper
	
	val colorHelper = addG2DDrawingHelper(GL2ColorHelper()) as GL2ColorHelper
	
	/**
	 * The current clip rectangle. This implementation supports only rectangular
	 * clip areas. This clip must be treated as immutable and replaced but never
	 * changed.
	 */
	protected var clip: Rectangle? = null
	
	protected lateinit var graphicsConfig: GraphicsConfiguration
	
	/**
	 * The set of cached hints for this graphics object.
	 */
	protected var hints = RenderingHints(emptyMap<Key, Any>())
	
	fun addG2DDrawingHelper(helper: G2DDrawingHelper): G2DDrawingHelper {
		helpers.add(helper)
		return helper
	}
	
	fun removeG2DDrawingHelper(helper: G2DDrawingHelper) {
		helpers.remove(helper)
	}
	
	protected fun setCanvas(context: GLContext) {
		glDrawable = context.glDrawable
		glContext = context
		
		for (helper in helpers) {
			helper.setG2D(this)
		}
	}
	
	/**
	 * Sets up the graphics object in preparation for drawing. Initialization such
	 * as getting the viewport
	 */
	fun prePaint(context: GLContext) {
		canvasHeight = GLG2DUtils.getViewportHeight(context.gl)
		setCanvas(context)
		setDefaultState()
	}
	
	protected fun setDefaultState() {
		background = Color.black
		color = Color.white
		font = Font("Arial", Font.PLAIN, 12)
		stroke = BasicStroke()
		composite = AlphaComposite.SrcOver
		setClip(null)
		setRenderingHints(null)
		graphicsConfig = GLGraphicsConfiguration(glDrawable)
	}
	
	fun postPaint() {
		// could glFlush here, but not necessary
	}
	
	fun glDispose() {
		for (helper in helpers) {
			helper.dispose()
		}
	}
	
	override fun draw(s: Shape) {
		shapeHelper.draw(s)
	}
	
	override fun drawString(str: String, x: Int, y: Int) {
		stringHelper.drawString(str, x, y)
	}
	
	override fun drawString(str: String, x: Float, y: Float) {
		stringHelper.drawString(str, x, y)
	}
	
	override fun drawString(iterator: AttributedCharacterIterator, x: Int, y: Int) {
		stringHelper.drawString(iterator, x, y)
	}
	
	override fun drawString(iterator: AttributedCharacterIterator, x: Float, y: Float) {
		stringHelper.drawString(iterator, x, y)
	}
	
	override fun drawGlyphVector(g: GlyphVector, x: Float, y: Float) {
		shapeHelper.fill(g.getOutline(x, y))
	}
	
	override fun fill(s: Shape) {
		shapeHelper.fill(s)
	}
	
	override fun hit(rect: Rectangle, s: Shape, onStroke: Boolean): Boolean {
		var rect = rect
		var s = s
		if (clip != null) {
			rect = clip!!.intersection(rect)
		}
		
		if (rect.isEmpty) {
			return false
		}
		
		if (onStroke) {
			s = shapeHelper.stroke.createStrokedShape(s)
		}
		
		s = transform.createTransformedShape(s)
		return s.intersects(rect)
	}
	
	override fun getDeviceConfiguration(): GraphicsConfiguration {
		return graphicsConfig
	}
	
	override fun getComposite(): Composite {
		return colorHelper.composite
	}
	
	override fun setComposite(comp: Composite) {
		colorHelper.composite = comp
	}
	
	override fun setPaint(paint: Paint) {
		colorHelper.paint = paint
	}
	
	override fun setRenderingHint(hintKey: Key, hintValue: Any) {
		if (!hintKey.isCompatibleValue(hintValue)) {
			throw IllegalArgumentException("$hintValue is not compatible with $hintKey")
		} else {
			for (helper in helpers) {
				helper.setHint(hintKey, hintValue)
			}
		}
	}
	
	override fun getRenderingHint(hintKey: Key): Any? {
		return hints.get(hintKey)
	}
	
	override fun setRenderingHints(hints: Map<*, *>?) {
		resetRenderingHints()
		if (hints != null) {
			addRenderingHints(hints)
		}
	}
	
	protected fun resetRenderingHints() {
		hints = RenderingHints(emptyMap<Key, Any>())
		
		for (helper in helpers) {
			helper.resetHints()
		}
	}
	
	override fun addRenderingHints(hints: Map<*, *>) {
		for ((key, value) in hints) {
			if (key is Key) {
				setRenderingHint(key, value!!)
			}
		}
	}
	
	override fun getRenderingHints(): RenderingHints {
		return hints.clone() as RenderingHints
	}
	
	override fun translate(x: Int, y: Int) {
		matrixHelper.translate(x, y)
	}
	
	override fun translate(x: Double, y: Double) {
		matrixHelper.translate(x, y)
	}
	
	override fun rotate(theta: Double) {
		matrixHelper.rotate(theta)
	}
	
	override fun rotate(theta: Double, x: Double, y: Double) {
		matrixHelper.rotate(theta, x, y)
	}
	
	override fun scale(sx: Double, sy: Double) {
		matrixHelper.scale(sx, sy)
	}
	
	override fun shear(shx: Double, shy: Double) {
		matrixHelper.shear(shx, shy)
	}
	
	override fun transform(Tx: AffineTransform) {
		matrixHelper.transform(Tx)
	}
	
	override fun setTransform(transform: AffineTransform) {
		matrixHelper.transform = transform
	}
	
	override fun getTransform(): AffineTransform {
		return matrixHelper.transform
	}
	
	override fun getPaint(): Paint {
		return colorHelper.paint
	}
	
	override fun getColor(): Color {
		return colorHelper.color
	}
	
	override fun setColor(c: Color) {
		colorHelper.color = c
	}
	
	override fun setBackground(color: Color) {
		colorHelper.background = color
	}
	
	override fun getBackground(): Color {
		return colorHelper.background
	}
	
	override fun getStroke(): Stroke {
		return shapeHelper.stroke
	}
	
	override fun setStroke(s: Stroke) {
		shapeHelper.stroke = s
	}
	
	override fun setPaintMode() {
		colorHelper.setPaintMode()
	}
	
	override fun setXORMode(c: Color) {
		colorHelper.setXORMode(c)
	}
	
	override fun getFont(): Font {
		return stringHelper.font
	}
	
	override fun setFont(font: Font) {
		stringHelper.font = font
	}
	
	override fun getFontMetrics(f: Font): FontMetrics {
		return stringHelper.getFontMetrics(f)
	}
	
	override fun getFontRenderContext(): FontRenderContext {
		return stringHelper.fontRenderContext
	}
	
	override fun getClipBounds(): Rectangle? {
		if (clip == null) {
			return null
		} else {
			try {
				val pts = DoubleArray(8)
				pts[0] = clip!!.minX
				pts[1] = clip!!.minY
				pts[2] = clip!!.maxX
				pts[3] = clip!!.minY
				pts[4] = clip!!.maxX
				pts[5] = clip!!.maxY
				pts[6] = clip!!.minX
				pts[7] = clip!!.maxY
				transform.inverseTransform(pts, 0, pts, 0, 4)
				val minX = Math.min(pts[0], Math.min(pts[2], Math.min(pts[4], pts[6]))).toInt()
				val maxX = Math.max(pts[0], Math.max(pts[2], Math.max(pts[4], pts[6]))).toInt()
				val minY = Math.min(pts[1], Math.min(pts[3], Math.min(pts[5], pts[7]))).toInt()
				val maxY = Math.max(pts[1], Math.max(pts[3], Math.max(pts[5], pts[7]))).toInt()
				return Rectangle(minX, minY, maxX - minX, maxY - minY)
			} catch (e: NoninvertibleTransformException) {
				// Not sure why this would happen
				Logger.getLogger(GLGraphics2D::class.java.name).log(Level.WARNING, "User transform is non-invertible", e)
				
				return clip!!.bounds
			}
			
		}
	}
	
	override fun clip(s: Shape) {
		setClip(s.bounds, true)
	}
	
	override fun clipRect(x: Int, y: Int, width: Int, height: Int) {
		setClip(Rectangle(x, y, width, height), true)
	}
	
	override fun setClip(x: Int, y: Int, width: Int, height: Int) {
		setClip(Rectangle(x, y, width, height), false)
	}
	
	override fun getClip(): Shape? {
		return clipBounds
	}
	
	override fun setClip(clipShape: Shape?) {
		if (clipShape is Rectangle2D) {
			setClip(clipShape as Rectangle2D?, false)
		} else if (clipShape == null) {
			setClip(null, false)
		} else {
			setClip(clipShape.bounds2D)
		}
	}
	
	protected fun setClip(clipShape: Rectangle2D?, intersect: Boolean) {
		if (clipShape == null) {
			clip = null
			scissor(false)
		} else if (intersect && clip != null) {
			val rect = transform.createTransformedShape(clipShape).bounds
			clip = rect.intersection(clip!!)
			scissor(true)
		} else {
			clip = transform.createTransformedShape(clipShape).bounds
			scissor(true)
		}
	}
	
	protected fun scissor(enable: Boolean) {
		val gl = glContext.gl
		if (enable) {
			gl.glScissor(clip!!.x, canvasHeight - clip!!.y - clip!!.height, Math.max(clip!!.width, 0), Math.max(clip!!.height, 0))
			gl.glEnable(GL.GL_SCISSOR_TEST)
		} else {
			clip = null
			gl.glDisable(GL.GL_SCISSOR_TEST)
		}
	}
	
	override fun copyArea(x: Int, y: Int, width: Int, height: Int, dx: Int, dy: Int) {
		colorHelper.copyArea(x, y, width, height, dx, dy)
	}
	
	override fun drawLine(x1: Int, y1: Int, x2: Int, y2: Int) {
		shapeHelper.drawLine(x1, y1, x2, y2)
	}
	
	override fun fillRect(x: Int, y: Int, width: Int, height: Int) {
		shapeHelper.drawRect(x, y, width, height, true)
	}
	
	override fun clearRect(x: Int, y: Int, width: Int, height: Int) {
		val c = color
		colorHelper.setColorNoRespectComposite(background)
		fillRect(x, y, width, height)
		colorHelper.setColorRespectComposite(c)
	}
	
	override fun drawRect(x: Int, y: Int, width: Int, height: Int) {
		shapeHelper.drawRect(x, y, width, height, false)
	}
	
	override fun drawRoundRect(x: Int, y: Int, width: Int, height: Int, arcWidth: Int, arcHeight: Int) {
		shapeHelper.drawRoundRect(x, y, width, height, arcWidth, arcHeight, false)
	}
	
	override fun fillRoundRect(x: Int, y: Int, width: Int, height: Int, arcWidth: Int, arcHeight: Int) {
		shapeHelper.drawRoundRect(x, y, width, height, arcWidth, arcHeight, true)
	}
	
	override fun drawOval(x: Int, y: Int, width: Int, height: Int) {
		shapeHelper.drawOval(x, y, width, height, false)
	}
	
	override fun fillOval(x: Int, y: Int, width: Int, height: Int) {
		shapeHelper.drawOval(x, y, width, height, true)
	}
	
	override fun drawArc(x: Int, y: Int, width: Int, height: Int, startAngle: Int, arcAngle: Int) {
		shapeHelper.drawArc(x, y, width, height, startAngle, arcAngle, false)
	}
	
	override fun fillArc(x: Int, y: Int, width: Int, height: Int, startAngle: Int, arcAngle: Int) {
		shapeHelper.drawArc(x, y, width, height, startAngle, arcAngle, true)
	}
	
	override fun drawPolyline(xPoints: IntArray, yPoints: IntArray, nPoints: Int) {
		shapeHelper.drawPolyline(xPoints, yPoints, nPoints)
	}
	
	override fun drawPolygon(xPoints: IntArray, yPoints: IntArray, nPoints: Int) {
		shapeHelper.drawPolygon(xPoints, yPoints, nPoints, false)
	}
	
	override fun fillPolygon(xPoints: IntArray, yPoints: IntArray, nPoints: Int) {
		shapeHelper.drawPolygon(xPoints, yPoints, nPoints, true)
	}
	
	override fun drawImage(img: Image, xform: AffineTransform, obs: ImageObserver): Boolean {
		return imageHelper.drawImage(img, xform, obs)
	}
	
	override fun drawImage(img: BufferedImage, op: BufferedImageOp, x: Int, y: Int) {
		imageHelper.drawImage(img, op, x, y)
	}
	
	override fun drawRenderedImage(img: RenderedImage, xform: AffineTransform) {
		imageHelper.drawImage(img, xform)
	}
	
	override fun drawRenderableImage(img: RenderableImage, xform: AffineTransform) {
		imageHelper.drawImage(img, xform)
	}
	
	override fun drawImage(img: Image, x: Int, y: Int, observer: ImageObserver): Boolean {
		return imageHelper.drawImage(img, x, y, Color.WHITE, observer)
	}
	
	override fun drawImage(img: Image, x: Int, y: Int, bgcolor: Color, observer: ImageObserver): Boolean {
		return imageHelper.drawImage(img, x, y, bgcolor, observer)
	}
	
	override fun drawImage(img: Image, x: Int, y: Int, width: Int, height: Int, observer: ImageObserver): Boolean {
		return imageHelper.drawImage(img, x, y, width, height, Color.WHITE, observer)
	}
	
	override fun drawImage(img: Image, x: Int, y: Int, width: Int, height: Int, bgcolor: Color, observer: ImageObserver): Boolean {
		return imageHelper.drawImage(img, x, y, width, height, bgcolor, observer)
	}
	
	override fun drawImage(img: Image, dx1: Int, dy1: Int, dx2: Int, dy2: Int, sx1: Int, sy1: Int, sx2: Int, sy2: Int, observer: ImageObserver): Boolean {
		return imageHelper.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, Color.WHITE, observer)
	}
	
	override fun drawImage(img: Image, dx1: Int, dy1: Int, dx2: Int, dy2: Int, sx1: Int, sy1: Int, sx2: Int, sy2: Int, bgcolor: Color,
	                       observer: ImageObserver): Boolean {
		return imageHelper.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer)
	}
	
	override fun create(): Graphics {
		val newG2d = clone()
		
		for (helper in helpers) {
			helper.push(newG2d)
		}
		
		return newG2d
	}
	
	override fun dispose() {
		/*
     * This is also called on the finalizer thread, which should not make OpenGL
     * calls. We also want to make sure that this only executes once.
     */
		if (!isDisposed) {
			isDisposed = true
			
			if (parent != null) {
				// pop in reverse order
				for (i in helpers.indices.reversed()) {
					helpers[i].pop(parent!!)
				}
				
				// the parent needs to set its clip
				parent!!.scissor(parent!!.clip != null)
			}
		}
	}
	
	override fun clone(): GLGraphics2D {
		try {
			val clone = super.clone() as GLGraphics2D
			clone.parent = this
			clone.hints = hints.clone() as RenderingHints
			return clone
		} catch (exception: CloneNotSupportedException) {
			throw AssertionError(exception)
		}
		
	}
}
