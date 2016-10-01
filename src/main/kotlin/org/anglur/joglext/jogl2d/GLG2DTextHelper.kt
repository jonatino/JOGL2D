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

import java.awt.Font
import java.awt.FontMetrics
import java.awt.font.FontRenderContext
import java.text.AttributedCharacterIterator

interface GLG2DTextHelper : G2DDrawingHelper {
	
	var font: Font
	
	fun getFontMetrics(font: Font): FontMetrics
	
	val fontRenderContext: FontRenderContext
	
	fun drawString(iterator: AttributedCharacterIterator, x: Int, y: Int)
	
	fun drawString(iterator: AttributedCharacterIterator, x: Float, y: Float)
	
	fun drawString(string: String, x: Float, y: Float)
	
	fun drawString(string: String, x: Int, y: Int)
	
}