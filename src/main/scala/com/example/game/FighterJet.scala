package com.example.game

case class Vec3(var x: Float, var y: Float, var z: Float)

class FighterJet {
  var position = Vec3(0f, 0f, 0f)
  var velocity = Vec3(0f, 0f, -1f)
  var pitch = 0f
  var yaw = 0f
  var roll = 0f
  var speed = 0.1f

  def update(input: JetInput, dt: Float): Unit = {
    // Simple physics: update orientation and move forward
    yaw += input.yaw * dt
    pitch += input.pitch * dt
    roll += input.roll * dt
    speed += input.thrust * dt
    speed = math.max(0.01f, math.min(speed, 2.0f)).toFloat

    // Calculate forward vector (simplified)
    val forwardX = math.sin(yaw).toFloat * math.cos(pitch).toFloat
    val forwardY = -math.sin(pitch).toFloat
    val forwardZ = -math.cos(yaw).toFloat * math.cos(pitch).toFloat
    velocity = Vec3(forwardX * speed, forwardY * speed, forwardZ * speed)
    position = Vec3(position.x + velocity.x * dt, position.y + velocity.y * dt, position.z + velocity.z * dt)
  }
}

case class JetInput(pitch: Float, yaw: Float, roll: Float, thrust: Float)