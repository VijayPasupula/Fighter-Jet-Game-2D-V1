package com.example.game

import scala.scalanative.unsafe._
import scala.scalanative.unsigned._
import scala.scalanative.libc.stdlib

object SDLConsts {
  val SDL_GL_CONTEXT_MAJOR_VERSION: CInt = 17
  val SDL_GL_CONTEXT_MINOR_VERSION: CInt = 18
  val SDL_WINDOW_OPENGL: CUnsignedInt = 0x00000002.toUInt

  val SDL_INIT_VIDEO: CUnsignedInt = 0x00000020.toUInt
  val SDL_QUIT: CUnsignedInt = 0x100.toUInt
  val SDL_KEYDOWN: CUnsignedInt = 0x300.toUInt
  val SDL_KEYUP: CUnsignedInt = 0x301.toUInt

  val SDL_WINDOWPOS_CENTERED: CInt = 0x2FFF0000
  val SDL_WINDOW_RESIZABLE: CUnsignedInt = 0x00000020.toUInt
}

@extern
object SDL {
  type SDL_Window = CStruct0
  type SDL_Event = CStruct0

  def SDL_Init(flags: CUnsignedInt): CInt = extern
  def SDL_Quit(): Unit = extern
  def SDL_CreateWindow(title: CString, x: CInt, y: CInt, w: CInt, h: CInt, flags: CUnsignedInt): Ptr[SDL_Window] = extern
  def SDL_DestroyWindow(window: Ptr[SDL_Window]): Unit = extern

  def SDL_PollEvent(event: Ptr[SDL_Event]): CInt = extern

  def SDL_GL_SetAttribute(attr: CInt, value: CInt): CInt = extern
  def SDL_GL_CreateContext(window: Ptr[SDL_Window]): Ptr[Byte] = extern
  def SDL_GL_SwapWindow(window: Ptr[SDL_Window]): Unit = extern
}

@extern
object SDLError {
  @name("SDL_GetError")
  def SDL_GetError(): CString = extern
}

object GameWindow {
  // Set window to 800x800 for a 1:1 aspect ratio
  val WINDOW_WIDTH = 800
  val WINDOW_HEIGHT = 800

  val SC_W = 26
  val SC_A = 4
  val SC_S = 22
  val SC_D = 7
  val SC_SPACE = 44

  def main(args: Array[String]): Unit = {
    println(s"[DEBUG] Starting Jet Fighter Game with window ${WINDOW_WIDTH}x${WINDOW_HEIGHT} (centered, resizable, OpenGL)...")
    Zone { implicit z =>
      if (SDL.SDL_Init(SDLConsts.SDL_INIT_VIDEO) != 0) {
        println("[ERROR] SDL_Init failed: " + fromCString(SDLError.SDL_GetError()))
        stdlib.exit(1)
      }
      println("[DEBUG] SDL Initialized successfully.")

      SDL.SDL_GL_SetAttribute(SDLConsts.SDL_GL_CONTEXT_MAJOR_VERSION, 2)
      SDL.SDL_GL_SetAttribute(SDLConsts.SDL_GL_CONTEXT_MINOR_VERSION, 1)

      val window = SDL.SDL_CreateWindow(
        toCString("Jet Fighter Game 2D"),
        SDLConsts.SDL_WINDOWPOS_CENTERED, SDLConsts.SDL_WINDOWPOS_CENTERED,
        WINDOW_WIDTH, WINDOW_HEIGHT,
        SDLConsts.SDL_WINDOW_RESIZABLE | SDLConsts.SDL_WINDOW_OPENGL
      )
      if (window == null) {
        println("[ERROR] SDL_CreateWindow failed: " + fromCString(SDLError.SDL_GetError()))
        SDL.SDL_Quit()
        stdlib.exit(1)
      }
      println("[DEBUG] SDL_Window created successfully.")

      val glContext = SDL.SDL_GL_CreateContext(window)
      if (glContext == null) {
        println("[ERROR] SDL_GL_CreateContext failed: " + fromCString(SDLError.SDL_GetError()))
        SDL.SDL_DestroyWindow(window)
        SDL.SDL_Quit()
        stdlib.exit(1)
      }
      println("[DEBUG] OpenGL context created successfully.")

      val eventRaw = stackalloc[Byte](56)

      var running = true

      // Controls
      var wPressed = false
      var aPressed = false
      var sPressed = false
      var dPressed = false
      var spacePressed = false

      // Player jet
      var jetPos = Vec2(0f, 0f)
      var jetAngle = 0f
      val jetSpeed = 0.6f
      val jetColor = (0.2f, 0.8f, 0.2f)
      var playerAlive = true

      // Bullets
      var bullets = List.empty[Bullet]
      var canFire = true
      var fireCooldown = 0f

      // Bot jet
      val botColor = (0.85f, 0.2f, 0.2f)
      val bot = new BotJet(Vec2(0.6f, 0.6f), math.Pi.toFloat / 2)
      var botAlive = true

      // Bot firing AI
      var botFireCooldown = 0f
      val botFireInterval = 0.4f

      val frameDelayMs = 16
      var lastTime = System.currentTimeMillis()
      var frame = 0

      // Dialog state
      import DialogUI._
      var dialogState: Option[DialogState] =
        Some(DialogState(StartDialog, visible = true, message = "Welcome to Jet Fighter 2D!", buttonText = "Start New Game"))
      var endDialogQueued = false
      var endDialogMessage = ""
      var endDialogShowTime = 0L

      // Mouse tracking for dialog button clicks
      var mouseX = 0
      var mouseY = 0
      var mouseDown = false
      var mouseClickedNDC: Option[(Float, Float)] = None

      def resetGame(): Unit = {
        wPressed = false
        aPressed = false
        sPressed = false
        dPressed = false
        spacePressed = false

        jetPos = Vec2(0f, 0f)
        jetAngle = 0f
        playerAlive = true
        bullets = List.empty[Bullet]
        canFire = true
        fireCooldown = 0f

        bot.position = Vec2(0.6f, 0.6f)
        bot.angle = math.Pi.toFloat / 2
        botAlive = true
        botFireCooldown = 0f
      }

      println("[DEBUG] Entering main game loop... (close the window to exit)")

      while (running) {
        val currentTime = System.currentTimeMillis()
        val dt = ((currentTime - lastTime).toFloat / 1000f).max(0.016f).min(0.1f)
        lastTime = currentTime

        frame += 1

        // --- Event Polling ---
        while (SDL.SDL_PollEvent(eventRaw.asInstanceOf[Ptr[SDL.SDL_Event]]) != 0) {
          val eventType = !(eventRaw.asInstanceOf[Ptr[CUnsignedInt]])
          if (eventType == SDLConsts.SDL_QUIT) {
            running = false
          }
          else if (eventType == 0x401 /* SDL_MOUSEMOTION */ ) {
            val x = (!(eventRaw + 20).asInstanceOf[Ptr[CInt]]).toInt
            val y = (!(eventRaw + 24).asInstanceOf[Ptr[CInt]]).toInt
            mouseX = x
            mouseY = y
          }
          else if (eventType == 0x402 /* SDL_MOUSEBUTTONDOWN */) {
            mouseDown = true
            val x = (!(eventRaw + 20).asInstanceOf[Ptr[CInt]]).toInt
            val y = (!(eventRaw + 24).asInstanceOf[Ptr[CInt]]).toInt
            mouseClickedNDC = Some(toNDC(x, y))
          }
          else if (eventType == 0x403 /* SDL_MOUSEBUTTONUP */) {
            mouseDown = false
          }
          else if (eventType == SDLConsts.SDL_KEYDOWN || eventType == SDLConsts.SDL_KEYUP) {
            val keysymBase = eventRaw + 16
            val scancode = (!(keysymBase.asInstanceOf[Ptr[CUnsignedInt]])).toInt
            val pressed = eventType == SDLConsts.SDL_KEYDOWN
            scancode match {
              case SC_W     => wPressed = pressed
              case SC_A     => aPressed = pressed
              case SC_S     => sPressed = pressed
              case SC_D     => dPressed = pressed
              case SC_SPACE => spacePressed = pressed
              case _        =>
            }
          }
        }

        // --- Dialog Handling ---
        if (dialogState.isDefined) {
          GL.glViewport(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT)
          GL.glMatrixMode(GLConsts.GL_PROJECTION)
          GL.glLoadIdentity()
          GL.glOrtho(-1, 1, -1, 1, -1, 1) // always 1:1 aspect
          GL.glMatrixMode(GLConsts.GL_MODELVIEW)
          GL.glLoadIdentity()
          GL.glClearColor(0f, 0f, 0.15f, 1f)
          GL.glClear(GLConsts.GL_COLOR_BUFFER_BIT | GLConsts.GL_DEPTH_BUFFER_BIT)

          GL.glDisable(GLConsts.GL_DEPTH_TEST)

          DialogUI.drawDialog(dialogState.get)

          SDL.SDL_GL_SwapWindow(window)
          Thread.sleep(frameDelayMs)

          mouseClickedNDC.foreach { case (mx, my) =>
            if (DialogUI.isButtonClicked(mx, my)) {
              dialogState match {
                case Some(DialogState(StartDialog, _, _, _)) =>
                  resetGame()
                  dialogState = None
                case Some(DialogState(EndDialog, _, _, _)) =>
                  dialogState = Some(DialogState(StartDialog, visible = true, message = "Welcome to Jet Fighter Game 2D!", buttonText = "Start New Game"))
                case _ =>
              }
            }
          }
          mouseClickedNDC = None
        } else {
          // --- Player movement direction and angle ---
          if (playerAlive) {
            var moveDir = Vec2(0f, 0f)
            if (wPressed) moveDir.y += 1f
            if (sPressed) moveDir.y -= 1f
            if (aPressed) moveDir.x -= 1f
            if (dPressed) moveDir.x += 1f

            if (moveDir.x != 0f || moveDir.y != 0f) {
              val norm = moveDir.normalized
              jetAngle = (math.atan2(norm.y, norm.x) - math.Pi/2).toFloat
              jetPos = jetPos + norm * jetSpeed * dt
              jetPos = Vec2(
                math.max(-0.95f, math.min(jetPos.x, 0.95f)),
                math.max(-0.95f, math.min(jetPos.y, 0.95f))
              )
            }
          }

          // --- Fire bullets with cooldown ---
          if (playerAlive && spacePressed && canFire) {
            val launcherTipLocal = Vec2(0f, 0.13f)
            val cosA = math.cos(jetAngle).toFloat
            val sinA = math.sin(jetAngle).toFloat
            val tipWorld = Vec2(
              jetPos.x + launcherTipLocal.x * cosA - launcherTipLocal.y * sinA,
              jetPos.y + launcherTipLocal.x * sinA + launcherTipLocal.y * cosA
            )
            val dir = Vec2.fromAngle(jetAngle + math.Pi.toFloat / 2)
            bullets ::= Bullet(tipWorld, dir, 1.5f, alive = true, owner = Player)
            canFire = false
            fireCooldown = 0.20f
          }
          if (!canFire) {
            fireCooldown -= dt
            if (fireCooldown <= 0f) canFire = true
          }

          // --- Update bullets ---
          bullets.foreach(_.update(dt))
          bullets = bullets.filter(_.alive)

          // --- Update bot ---
          if (botAlive) bot.update(dt)

          // --- Bot AI: fire at player ---
          if (botAlive && playerAlive) {
            botFireCooldown -= dt
            if (botFireCooldown <= 0f) {
              val toPlayer = (jetPos - bot.position).normalized
              val botFacing = Vec2.fromAngle(bot.angle + math.Pi.toFloat / 2)
              val angleDiff = math.acos(botFacing.dot(toPlayer)).abs
              val fireThreshold = math.Pi / 8
              if (angleDiff < fireThreshold) {
                val launcherTipLocal = Vec2(0f, 0.13f)
                val cosA = math.cos(bot.angle).toFloat
                val sinA = math.sin(bot.angle).toFloat
                val tipWorld = Vec2(
                  bot.position.x + launcherTipLocal.x * cosA - launcherTipLocal.y * sinA,
                  bot.position.y + launcherTipLocal.x * sinA + launcherTipLocal.y * cosA
                )
                val dir = Vec2.fromAngle(bot.angle + math.Pi.toFloat / 2)
                bullets ::= Bullet(tipWorld, dir, 1.5f, alive = true, owner = Bot)
                botFireCooldown = botFireInterval
              }
            }
          }

          // --- Bullet-bot collision ---
          if (botAlive) {
            bullets.find { b =>
              b.owner == Player && {
                val distSq = (b.position.x - bot.position.x) * (b.position.x - bot.position.x) +
                             (b.position.y - bot.position.y) * (b.position.y - bot.position.y)
                distSq < 0.05f * 0.05f
              }
            }.foreach { hit =>
              botAlive = false
              hit.alive = false
              println("[DEBUG] Bot hit! You win!")
            }
          }

          // --- Bullet-player collision ---
          if (playerAlive) {
            bullets.find { b =>
              b.owner == Bot && {
                val distSq = (b.position.x - jetPos.x) * (b.position.x - jetPos.x) +
                             (b.position.y - jetPos.y) * (b.position.y - jetPos.y)
                distSq < 0.05f * 0.05f
              }
            }.foreach { hit =>
              playerAlive = false
              hit.alive = false
              println("[DEBUG] Player hit by bot bullet! Game Over!")
            }
          }

          // --- Bot Jet - Player Jet collision ---
          if (playerAlive && botAlive) {
            val dx = jetPos.x - bot.position.x
            val dy = jetPos.y - bot.position.y
            val distSq = dx * dx + dy * dy
            val jetCollisionRadius = 0.08f
            if (distSq < jetCollisionRadius * jetCollisionRadius) {
              playerAlive = false
              botAlive = false
              println("[DEBUG] Player and Bot jets collided! Both eliminated!")
            }
          }

          // --- End-of-game dialog scheduling ---
          if (!playerAlive && botAlive && !endDialogQueued) {
            endDialogQueued = true
            endDialogMessage = "Bot Won! Better luck next time."
            endDialogShowTime = currentTime + 1000
          } else if (!botAlive && playerAlive && !endDialogQueued) {
            endDialogQueued = true
            endDialogMessage = "You Won! Congratulations."
            endDialogShowTime = currentTime + 1000
          } else if (!playerAlive && !botAlive && !endDialogQueued) {
            endDialogQueued = true
            endDialogMessage = "Jets Crashed! Both eliminated."
            endDialogShowTime = currentTime + 1000
          }

          if (endDialogQueued && currentTime >= endDialogShowTime) {
            dialogState = Some(DialogState(EndDialog, visible = true, message = endDialogMessage, buttonText = "OK"))
            endDialogQueued = false
          }

          // --- OpenGL Rendering ---
          GL.glViewport(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT)
          GL.glMatrixMode(GLConsts.GL_PROJECTION)
          GL.glLoadIdentity()
          GL.glOrtho(-1, 1, -1, 1, -1, 1) // always 1:1 aspect
          GL.glMatrixMode(GLConsts.GL_MODELVIEW)
          GL.glLoadIdentity()
          GL.glClearColor(0f, 0f, 0.15f, 1f)
          GL.glClear(GLConsts.GL_COLOR_BUFFER_BIT | GLConsts.GL_DEPTH_BUFFER_BIT)
          GL.glEnable(GLConsts.GL_DEPTH_TEST)

          if (botAlive)
            JetRenderer.drawJet(bot.position, bot.angle, botColor)
          if (playerAlive)
            JetRenderer.drawJet(jetPos, jetAngle, jetColor)
          bullets.foreach(bul => JetRenderer.drawBullet(bul.position))

          SDL.SDL_GL_SwapWindow(window)
          Thread.sleep(frameDelayMs)
        }
      }

      println("[DEBUG] Cleaning up and exiting...")
      SDL.SDL_DestroyWindow(window)
      SDL.SDL_Quit()
    }
  }

  def toNDC(x: Int, y: Int): (Float, Float) = {
    val fx = (x.toFloat / WINDOW_WIDTH) * 2f - 1f
    val fy = 1f - (y.toFloat / WINDOW_HEIGHT) * 2f
    (fx, fy)
  }
}
