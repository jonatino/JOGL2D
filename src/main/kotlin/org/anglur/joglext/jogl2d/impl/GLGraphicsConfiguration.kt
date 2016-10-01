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

import com.jogamp.opengl.GLDrawable
import java.awt.GraphicsConfiguration
import java.awt.GraphicsDevice
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
class GLGraphicsConfiguration(val target: GLDrawable) : GraphicsConfiguration() {
	
	private val device: GLGraphicsDevice
	
	init {
		device = GLGraphicsDevice(this)
	}
	
	override fun getDevice(): GraphicsDevice {
		return device
	}
	
	override fun createCompatibleImage(width: Int, height: Int): BufferedImage {
		return BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
	}
	
	/*
	 * Any reasonable {@code ColorModel} can be transformed into a texture we can
	 * render in OpenGL. I'm not worried about creating an exactly correct one
	 * right now.
	 */
	override fun getColorModel(): ColorModel {
		return ColorModel.getRGBdefault()
	}
	
	override fun getColorModel(transparency: Int): ColorModel? {
		when (transparency) {
			Transparency.OPAQUE, Transparency.TRANSLUCENT -> return colorModel
			Transparency.BITMASK -> return DirectColorModel(25, 0xff0000, 0xff00, 0xff, 0x1000000)
			else -> return null
		}
	}
	
	override fun getDefaultTransform(): AffineTransform {
		return AffineTransform()
	}
	
	override fun getNormalizingTransform(): AffineTransform {
		return AffineTransform()
	}
	
	override fun getBounds(): Rectangle {
		return Rectangle(target.surfaceWidth, target.surfaceHeight)
	}
}
