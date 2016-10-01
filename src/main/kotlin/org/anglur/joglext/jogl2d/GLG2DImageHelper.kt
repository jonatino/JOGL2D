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
import java.awt.Image
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.awt.image.BufferedImageOp
import java.awt.image.ImageObserver
import java.awt.image.RenderedImage
import java.awt.image.renderable.RenderableImage

interface GLG2DImageHelper : G2DDrawingHelper {
	fun drawImage(img: Image, x: Int, y: Int, bgcolor: Color, observer: ImageObserver): Boolean
	
	fun drawImage(img: Image, xform: AffineTransform, observer: ImageObserver): Boolean
	
	fun drawImage(img: Image, x: Int, y: Int, width: Int, height: Int, bgcolor: Color, observer: ImageObserver): Boolean
	
	fun drawImage(img: Image, dx1: Int, dy1: Int, dx2: Int, dy2: Int, sx1: Int, sy1: Int, sx2: Int, sy2: Int, bgcolor: Color, observer: ImageObserver): Boolean
	
	fun drawImage(img: BufferedImage, op: BufferedImageOp, x: Int, y: Int)
	
	fun drawImage(img: RenderedImage, xform: AffineTransform)
	
	fun drawImage(img: RenderableImage, xform: AffineTransform)
}