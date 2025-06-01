package com.example.game

object JetRenderer {
  // Draws a jet with launcher, at position (centerX, centerY), facing angle (radians), with the given color
  def drawJet(center: Vec2, angle: Float, color: (Float, Float, Float)): Unit = {
    // Jet body vertices in local space (relative to center) -- looks like a jet with a nose launcher
    val body = Array(
      Vec2( 0.0f,  0.11f), // Nose
      Vec2(-0.06f, -0.07f), // Left wing
      Vec2(-0.025f, -0.045f), // Left rear
      Vec2( 0.0f, -0.09f), // Tail
      Vec2( 0.025f, -0.045f), // Right rear
      Vec2( 0.06f, -0.07f)  // Right wing
    )
    // Launcher tip (where bullets fire)
    val launcherTip = Vec2(0.0f, 0.13f)

    // Apply rotation and translation
    def transform(v: Vec2): Vec2 = {
      val cosA = math.cos(angle).toFloat
      val sinA = math.sin(angle).toFloat
      val x = v.x * cosA - v.y * sinA + center.x
      val y = v.x * sinA + v.y * cosA + center.y
      Vec2(x, y)
    }
    // Draw jet body (as two triangles)
    GL.glBegin(GLConsts.GL_TRIANGLES)
    GL.glColor3f(color._1, color._2, color._3)
    // Triangle 1: nose, left wing, right wing
    val v0 = transform(body(0))
    val v1 = transform(body(1))
    val v5 = transform(body(5))
    GL.glVertex3f(v0.x, v0.y, -0.5f)
    GL.glVertex3f(v1.x, v1.y, -0.5f)
    GL.glVertex3f(v5.x, v5.y, -0.5f)
    // Triangle 2: left wing, left rear, right rear
    val v2 = transform(body(2))
    val v3 = transform(body(3))
    val v4 = transform(body(4))
    GL.glVertex3f(v1.x, v1.y, -0.5f)
    GL.glVertex3f(v2.x, v2.y, -0.5f)
    GL.glVertex3f(v4.x, v4.y, -0.5f)
    // Triangle 3: left rear, tail, right rear
    GL.glVertex3f(v2.x, v2.y, -0.5f)
    GL.glVertex3f(v3.x, v3.y, -0.5f)
    GL.glVertex3f(v4.x, v4.y, -0.5f)
    GL.glEnd()

    // Draw launcher (as a short line)
    val launcherStart = transform(body(0))
    val launcherEnd = transform(launcherTip)
    GL.glBegin(GLConsts.GL_LINES)
    GL.glColor3f(1f, 0.8f, 0.1f)
    GL.glVertex3f(launcherStart.x, launcherStart.y, -0.45f)
    GL.glVertex3f(launcherEnd.x, launcherEnd.y, -0.45f)
    GL.glEnd()
  }

  // Draw a bullet at position, with aspect correction
  def drawBullet(pos: Vec2): Unit = {
    GL.glBegin(GLConsts.GL_TRIANGLES)
    GL.glColor3f(1f, 1f, 0.2f)
    val r = 0.015f
    val x = pos.x
    val y = pos.y
    GL.glVertex3f(x, y + r, -0.4f)
    GL.glVertex3f(x - r * 0.5f, y - r, -0.4f)
    GL.glVertex3f(x + r * 0.5f, y - r, -0.4f)
    GL.glEnd()
  }

}