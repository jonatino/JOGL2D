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

import java.awt.GraphicsConfiguration
import java.awt.GraphicsDevice

/**
 * Fulfills the contract of a `GraphicsDevice`.
 */
class GLGraphicsDevice(protected val config: GLGraphicsConfiguration) : GraphicsDevice() {
	
	override fun getType(): Int {
		if (config.target.chosenGLCapabilities.isOnscreen) {
			return GraphicsDevice.TYPE_RASTER_SCREEN
		} else {
			return GraphicsDevice.TYPE_IMAGE_BUFFER
		}
	}
	
	override fun getIDstring(): String {
		return "glg2d"
	}
	
	override fun getConfigurations(): Array<GraphicsConfiguration> {
		return arrayOf(defaultConfiguration)
	}
	
	override fun getDefaultConfiguration(): GraphicsConfiguration {
		return config
	}
}
