package com.example.game

sealed trait BulletOwner
case object Player extends BulletOwner
case object Bot extends BulletOwner

case class Bullet(var position: Vec2, var direction: Vec2, val speed: Float = 1.5f, var alive: Boolean = true, owner: BulletOwner = Player) {
  def update(dt: Float): Unit = {
    position = position + (direction * speed * dt)
    if (math.abs(position.x) > 1.1f || math.abs(position.y) > 1.1f) alive = false
  }
}