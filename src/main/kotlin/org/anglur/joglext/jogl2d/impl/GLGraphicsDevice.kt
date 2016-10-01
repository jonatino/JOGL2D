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

import java.awt.GraphicsConfiguration
import java.awt.GraphicsDevice

/**
 * Fulfills the contract of a `GraphicsDevice`.
 */
class GLGraphicsDevice(private val config: GLGraphicsConfiguration) : GraphicsDevice() {
	
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
