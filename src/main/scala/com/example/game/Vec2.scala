package com.example.game

case class Vec2(var x: Float, var y: Float) {
  def +(that: Vec2): Vec2 = Vec2(this.x + that.x, this.y + that.y)
  def -(that: Vec2): Vec2 = Vec2(this.x - that.x, this.y - that.y)
  def *(scalar: Float): Vec2 = Vec2(this.x * scalar, this.y * scalar)
  def length: Float = math.sqrt(x * x + y * y).toFloat
  def normalized: Vec2 = {
    val len = length
    if (len == 0f) Vec2(0, 0) else Vec2(x / len, y / len)
  }
  def angle: Float = math.atan2(y, x).toFloat

  def dot(v: Vec2): Float = x * v.x + y * v.y
}

object Vec2 {
  def fromAngle(angle: Float): Vec2 = Vec2(math.cos(angle).toFloat, math.sin(angle).toFloat)
}