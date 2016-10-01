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

package org.anglur.joglext.jogl2d.impl.gl2


import com.jogamp.opengl.GL
import com.jogamp.opengl.GL2
import org.anglur.joglext.jogl2d.impl.AbstractTesselatorVisitor

class GL2TesselatorVisitor : AbstractTesselatorVisitor() {
	protected lateinit var gl: GL2
	
	override fun setGLContext(context: GL) {
		gl = context.gL2
	}
	
	override fun endTess() {
		vBuffer.drawBuffer(gl, drawMode)
	}
}
