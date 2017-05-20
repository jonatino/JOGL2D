# JOGL2D
_Zero-overhead 2D rendering library for JOGL_

[![Build Status](https://travis-ci.org/Jonatino/JOGL2D.svg?branch=master)](https://travis-ci.org/Jonatino/JOGL2D)
[![license](https://img.shields.io/github/license/Jonatino/JOGL2D.svg)](Apache License 2.0)

This library is licensed under Apache License 2.0.


JOGL2D is an open source Kotlin library that provides easy 2D graphics rendering capabilities to JOGL without adding any overhead whatsoever (memory/CPU).
JOGL2D is a lightweight, resource friendly, stripped down version of brandonborkholder's glg2d.

# How Can I Use JOGL2D?
Simply add JOGL2D to your JOGL application using your favourite dependancy management systems.

### Gradle
```groovy
compile 'org.anglur:joglext:1.0.1'
```

### Maven
```xml
<dependency>
  <groupId>org.anglur</groupId>
  <artifactId>joglext</artifactId>
  <version>1.0.1</version>
</dependency>
```

---


Once added, it is very easy to implement. Start off by making a `GLEventListener`
```kotlin
object CharlatanoOverlay : GLEventListener {
    
    private val WINDOW_WIDTH = 500
    private val WINDOW_HEIGHT = 500
    private val FPS = 60
    
    val window = GLWindow.create(GLCapabilities(null))
    
    init {
        GLProfile.initSingleton()
    }
    
    fun open(width: Int = WINDOW_WIDTH, height: Int = WINDOW_HEIGHT, x: Int = 100, y: Int = 1000) {
        val animator = FPSAnimator(window, FPS, true)
        
        window.addWindowListener(object : WindowAdapter() {
            override fun windowDestroyNotify(e: WindowEvent) {
                thread {
                    if (animator.isStarted)
                        animator.stop()
                    System.exit(0)
                }.start()
            }
        })
        
        window.addGLEventListener(this)
        window.setSize(width, height)
        window.setPosition(x, y)
        window.title = "Hello world"
        window.isVisible = true
        animator.start()
    }
    
    val g = GLGraphics2D() //Create GL2D wrapper
    
    override fun display(gLDrawable: GLAutoDrawable) {
        val gl2 = gLDrawable.gl.gL2
        
        gl2.glClear(GL.GL_COLOR_BUFFER_BIT)
        
        g.prePaint(gLDrawable.context) //Updated wrapper to latest glContext
        
        g.color = Color.RED
        g.drawRect(0, 0, 200, 200)
        g.color = Color.YELLOW
        g.drawLine(0, 0, 100, 100)
        g.color = Color.CYAN
        g.drawString("OpenGL 2D Made Easy! :D", 100, 100)
    }
    
    override fun init(glDrawable: GLAutoDrawable) {
    }
    
    override fun reshape(gLDrawable: GLAutoDrawable, x: Int, y: Int, width: Int, height: Int) {
        val gl = gLDrawable.gl.gL2
        gl.glViewport(0, 0, width, height)
    }
    
    override fun dispose(gLDrawable: GLAutoDrawable) {
        g.glDispose()
    }
```

# Screenshots

![Alt text](https://dl.dropboxusercontent.com/u/91292881/ShareX/2016/10/java_2016-10-01_17-45-28.png "Gui Demo")

Huge credits again to @brandonborkholder for open sourcing his G2D library here: https://github.com/brandonborkholder/glg2d
