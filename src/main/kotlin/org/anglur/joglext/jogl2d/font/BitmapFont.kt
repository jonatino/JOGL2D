package org.anglur.joglext.jogl2d.font

class BitmapFontRec(var name: String,
                    var num_chars: Int,
                    var first: Int,
                    var ch: Array<BitmapCharRec?>)


class BitmapCharRec(var width: Int,
                    var height: Int,
                    var xorig: Float,
                    var yorig: Float,
                    var advance: Float,
                    var bitmap: ByteArray?)