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

import com.jogamp.opengl.util.texture.Texture
import com.jogamp.opengl.util.texture.awt.AWTTextureIO
import org.anglur.joglext.jogl2d.GLG2DImageHelper
import org.anglur.joglext.jogl2d.GLG2DRenderingHints
import org.anglur.joglext.jogl2d.GLGraphics2D
import java.awt.Color
import java.awt.Image
import java.awt.RenderingHints.Key
import java.awt.geom.AffineTransform
import java.awt.image.*
import java.awt.image.renderable.RenderableImage
import java.lang.ref.Reference
import java.lang.ref.ReferenceQueue
import java.lang.ref.WeakReference
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger


abstract class AbstractImageHelper : GLG2DImageHelper {
	
	/**
	 * See [GLG2DRenderingHints.KEY_CLEAR_TEXTURES_CACHE]
	 */
	protected var imageCache = TextureCache()
	protected var clearCachePolicy: Any? = null
	
	protected lateinit var g2d: GLGraphics2D
	
	protected abstract fun begin(texture: Texture, xform: AffineTransform?, bgcolor: Color)
	
	protected abstract fun applyTexture(texture: Texture, dx1: Int, dy1: Int, dx2: Int, dy2: Int,
	                                    sx1: Float, sy1: Float, sx2: Float, sy2: Float)
	
	protected abstract fun end(texture: Texture)
	
	override fun setG2D(g2d: GLGraphics2D) {
		this.g2d = g2d
		
		if (clearCachePolicy === GLG2DRenderingHints.VALUE_CLEAR_TEXTURES_CACHE_EACH_PAINT) {
			imageCache.clear()
		}
	}
	
	override fun push(newG2d: GLGraphics2D) {
		// nop
	}
	
	override fun pop(parentG2d: GLGraphics2D) {
		// nop
	}
	
	override fun setHint(key: Key, value: Any?) {
		if (key === GLG2DRenderingHints.KEY_CLEAR_TEXTURES_CACHE) {
			clearCachePolicy = value
		}
	}
	
	override fun resetHints() {
		clearCachePolicy = GLG2DRenderingHints.VALUE_CLEAR_TEXTURES_CACHE_DEFAULT
	}
	
	override fun dispose() {
		imageCache.clear()
	}
	
	override fun drawImage(img: Image, x: Int, y: Int, bgcolor: Color, observer: ImageObserver): Boolean {
		return drawImage(img, AffineTransform.getTranslateInstance(x.toDouble(), y.toDouble()), bgcolor, observer)
	}
	
	override fun drawImage(img: Image, xform: AffineTransform, observer: ImageObserver): Boolean {
		return drawImage(img, xform, Color.WHITE, observer)
	}
	
	override fun drawImage(img: Image, x: Int, y: Int, width: Int, height: Int, bgcolor: Color, observer: ImageObserver): Boolean {
		val imgHeight = img.getHeight(null).toDouble()
		val imgWidth = img.getWidth(null).toDouble()
		
		if (imgHeight < 0 || imgWidth < 0) {
			return false
		}
		
		val transform = AffineTransform.getTranslateInstance(x.toDouble(), y.toDouble())
		transform.scale(width / imgWidth, height / imgHeight)
		return drawImage(img, transform, bgcolor, observer)
	}
	
	override fun drawImage(img: Image, dx1: Int, dy1: Int, dx2: Int, dy2: Int, sx1: Int, sy1: Int, sx2: Int,
	                       sy2: Int, bgcolor: Color, observer: ImageObserver): Boolean {
		val texture = getTexture(img, observer) ?: return false
		
		val height = texture.height.toFloat()
		val width = texture.width.toFloat()
		begin(texture, null, bgcolor)
		applyTexture(texture, dx1, dy1, dx2, dy2, sx1 / width, sy1 / height, sx2 / width, sy2 / height)
		end(texture)
		
		return true
	}
	
	protected fun drawImage(img: Image, xform: AffineTransform, color: Color, observer: ImageObserver): Boolean {
		val texture = getTexture(img, observer) ?: return false
		
		begin(texture, xform, color)
		applyTexture(texture)
		end(texture)
		
		return true
	}
	
	protected fun applyTexture(texture: Texture) {
		val width = texture.width
		val height = texture.height
		val coords = texture.imageTexCoords
		
		applyTexture(texture, 0, 0, width, height, coords.left(), coords.top(), coords.right(), coords.bottom())
	}
	
	/**
	 * Cache the texture if possible. I have a feeling this will run into issues
	 * later as images change. Just not sure how to handle it if they do. I
	 * suspect I should be using the ImageConsumer class and dumping pixels to the
	 * screen as I receive them.
	 *
	 *
	 *
	 *
	 * If an image is a BufferedImage, turn it into a texture and cache it. If
	 * it's not, draw it to a BufferedImage and see if all the image data is
	 * available. If it is, cache it. If it's not, don't cache it. But if not all
	 * the image data is available, we will draw it what we have, since we draw
	 * anything in the image to a BufferedImage.
	 *
	 */
	protected fun getTexture(image: Image, observer: ImageObserver): Texture? {
		var texture: Texture? = imageCache[image]
		if (texture == null) {
			val bufferedImage: BufferedImage?
			if (image is BufferedImage && image.type != BufferedImage.TYPE_CUSTOM) {
				bufferedImage = image
			} else {
				bufferedImage = toBufferedImage(image)
			}
			
			if (bufferedImage != null) {
				texture = create(bufferedImage)
				addToCache(image, texture)
			}
		}
		
		return texture
	}
	
	protected fun create(image: BufferedImage): Texture {
		// we'll assume the image is complete and can be rendered
		return AWTTextureIO.newTexture(g2d.glContext.getGL().getGLProfile(), image, false)
	}
	
	protected fun destroy(texture: Texture) {
		texture.destroy(g2d.glContext.getGL())
	}
	
	protected fun addToCache(image: Image, texture: Texture) {
		if (clearCachePolicy is Number) {
			val maxSize = (clearCachePolicy as Number).toInt()
			if (imageCache.size > maxSize) {
				if (LOGGER.isLoggable(Level.FINE)) {
					LOGGER.fine("Clearing texture cache with size " + imageCache.size)
				}
				
				imageCache.clear()
			}
		}
		
		imageCache.put(image, texture)
	}
	
	protected fun toBufferedImage(image: Image): BufferedImage? {
		if (image is VolatileImage) {
			return image.snapshot
		}
		
		val width = image.getWidth(null)
		val height = image.getHeight(null)
		if (width < 0 || height < 0) {
			return null
		}
		
		val bufferedImage = BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR)
		bufferedImage.createGraphics().drawImage(image, null, null)
		return bufferedImage
	}
	
	override fun drawImage(img: BufferedImage, op: BufferedImageOp, x: Int, y: Int) {
		TODO("drawImage(BufferedImage, BufferedImageOp, int, int)")
	}
	
	override fun drawImage(img: RenderedImage, xform: AffineTransform) {
		TODO("drawImage(RenderedImage, AffineTransform)")
	}
	
	override fun drawImage(img: RenderableImage, xform: AffineTransform) {
		TODO("drawImage(RenderableImage, AffineTransform)")
	}
	
	/**
	 * We could use a WeakHashMap here, but we want access to the ReferenceQueue
	 * so we can dispose the Textures when the Image is no longer referenced.
	 */
	@SuppressWarnings("serial")
	protected inner class TextureCache : HashMap<WeakKey<Image>, Texture>() {
		private val queue = ReferenceQueue<Image>()
		
		fun expungeStaleEntries() {
			var ref: Reference<out Image>? = queue.poll()
			while (ref != null) {
				val texture = remove(ref)
				if (texture != null) {
					destroy(texture)
				}
				
				ref = queue.poll()
			}
		}
		
		operator fun get(image: Image): Texture? {
			expungeStaleEntries()
			val key = WeakKey(image, null)
			return get(key)
		}
		
		fun put(image: Image, texture: Texture): Texture? {
			expungeStaleEntries()
			val key = WeakKey(image, queue)
			return put(key, texture)
		}
	}
	
	protected class WeakKey<T>(value: T, queue: ReferenceQueue<T>?) : WeakReference<T>(value, queue) {
		private val hash: Int
		
		init {
			hash = value!!.hashCode()
		}
		
		override fun hashCode(): Int {
			return hash
		}
		
		override fun equals(other: Any?): Boolean {
			if (this === other) {
				return true
			} else if (other is WeakKey<*>) {
				return other.hash == hash && get() === other.get()
			} else {
				return false
			}
		}
	}
	
	companion object {
		private val LOGGER = Logger.getLogger(AbstractImageHelper::class.java.name)
	}
}