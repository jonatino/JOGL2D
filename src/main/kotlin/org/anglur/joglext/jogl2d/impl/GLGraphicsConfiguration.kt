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

import com.jogamp.opengl.GLDrawable
import java.awt.GraphicsConfiguration
import java.awt.Rectangle
import java.awt.Transparency
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.awt.image.ColorModel
import java.awt.image.DirectColorModel

/**
 * Fulfills the contract of a `GraphicsConfiguration`.
 *
 *
 *
 *
 * Implementation note: this object is intended primarily to allow callers to
 * create compatible images. The transforms and bounds should be thought out
 * before being used.
 *
 */
object GLGraphicsConfiguration : GraphicsConfiguration() {
	
	lateinit var target: GLDrawable
	
	fun setDrawable(target: GLDrawable) = apply { this.target = target }
	
	private val device: GLGraphicsDevice
	
	init {
		device = GLGraphicsDevice(this)
	}
	
	override fun getDevice() = device
	
	override fun createCompatibleImage(width: Int, height: Int) =
			BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
	
	/*
	 * Any reasonable {@code ColorModel} can be transformed into a texture we can
	 * render in OpenGL. I'm not worried about creating an exactly correct one
	 * right now.
	 */
	override fun getColorModel() = ColorModel.getRGBdefault()
	
	override fun getColorModel(transparency: Int): ColorModel? {
		when (transparency) {
			Transparency.OPAQUE, Transparency.TRANSLUCENT -> return colorModel
			Transparency.BITMASK -> return DirectColorModel(25, 0xff0000, 0xff00, 0xff, 0x1000000)
			else -> return null
		}
	}
	
	override fun getDefaultTransform() = AffineTransform()
	
	override fun getNormalizingTransform() = AffineTransform()
	
	override fun getBounds() = Rectangle(target.surfaceWidth, target.surfaceHeight)
	
}
