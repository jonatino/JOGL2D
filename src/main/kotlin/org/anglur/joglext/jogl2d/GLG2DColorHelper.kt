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

import java.awt.Color
import java.awt.Composite
import java.awt.Paint

interface GLG2DColorHelper : G2DDrawingHelper {
	
	var composite: Composite
	
	var paint: Paint
	
	var color: Color
	
	var background: Color
	
	fun setColorNoRespectComposite(c: Color)
	
	fun setColorRespectComposite(c: Color)
	
	fun setPaintMode()
	
	fun setXORMode(c: Color)
	
	fun copyArea(x: Int, y: Int, width: Int, height: Int, dx: Int, dy: Int)
	
}