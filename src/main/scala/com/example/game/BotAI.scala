package com.example.game

import scala.util.Random

class BotJet(var position: Vec2, var angle: Float) {
  var speed: Float = 0.5f + Random.nextFloat() * 0.3f
  var moveTimer: Float = 0f
  var targetDir: Float = 0f

  def update(dt: Float): Unit = {
    // Move in current direction
    val dirVec = Vec2.fromAngle(angle)
    position = position + dirVec * speed * dt

    // Change direction randomly every 1.2 seconds
    moveTimer -= dt
    if (moveTimer <= 0f) {
      targetDir = Random.between(-math.Pi/1.8, math.Pi/1.8).toFloat
      moveTimer = 1.2f + Random.nextFloat() * 0.7f
    }
    angle += targetDir * dt

    // Bounce off screen edges
    if (position.x < -0.95f || position.x > 0.95f) {
      angle = math.Pi.toFloat - angle
      position.x = math.max(math.min(position.x, 0.95f), -0.95f)
    }
    if (position.y < -0.95f || position.y > 0.95f) {
      angle = -angle
      position.y = math.max(math.min(position.y, 0.95f), -0.95f)
    }
  }
}