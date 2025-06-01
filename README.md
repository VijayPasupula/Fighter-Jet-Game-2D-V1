# SpaceShooterWindow

A simple 2D Jet Fighter game built with [Scala Native](https://www.scala-native.org/) and OpenGL/SDL2. This project demonstrates how to create a windowed game with native graphics, keyboard and mouse controls, and basic game loop logic, all in pure Scala!

## Features

- **1v1 Jet Dogfight:** You (green jet) vs. Bot (red jet)
- **Player Controls:** Move with W/A/S/D, shoot with SPACE
- **Bot AI:** The bot moves randomly, tries to shoot at you, and bounces off screen edges
- **Bullets and Collisions:** Both jets can fire, and bullets collide with jets
- **Dialog UI:** Start and End dialogs with clickable buttons (mouse support)
- **OpenGL Rendering:** All graphics are rendered using raw OpenGL (no third-party graphics libraries)
- **Cross-platform:** Runs on Linux, macOS, and Windows (with dependencies installed)

## Screenshots

*(Add screenshots or gifs of your game window here)*

## Getting Started

### Prerequisites

- **Scala Native:** This project uses Scala Native (tested with Scala 2.13.x)
- **sbt:** To build and run the project
- **SDL2:** You must have SDL2 installed on your system
- **OpenGL:** A working OpenGL environment (most systems already have this)

#### Install SDL2

- **macOS:** `brew install sdl2`
- **Ubuntu:** `sudo apt-get install libsdl2-dev`
- **Windows:** [Download SDL2 Development Libraries](https://www.libsdl.org/download-2.0.php) and add to your path (see SDL2 docs)

### Building and Running

1. **Clone the repository:**
   ```bash
   git clone https://github.com/yourusername/SpaceShooterWindow.git
   cd SpaceShooterWindow
   ```

2. **Build and run with sbt:**
   ```bash
   sbt run
   ```

   On first run, Scala Native will download dependencies and compile your code. The game window should appear on your desktop.

## Controls

- **Move:** W (up), A (left), S (down), D (right)
- **Shoot:** SPACE
- **Start New Game / Dialogs:** Use the mouse to click the button in dialogs
- **Exit:** Close the window

## Project Structure

```
.
├── build.sbt
├── src/main/scala/com/example/game/
│   ├── GameWindow.scala      # Main game loop & SDL/OpenGL setup
│   ├── GL.scala              # OpenGL bindings (Scala Native externs)
│   ├── GLConsts.scala        # OpenGL constants
│   ├── JetRenderer.scala     # Rendering jets & bullets
│   ├── BotAI.scala           # Simple bot AI logic
│   ├── Bullet.scala          # Bullet logic and owners
│   ├── DialogUI.scala        # Dialog rendering and mouse interaction
│   ├── Vec2.scala            # 2D vector math
│   ├── FighterJet.scala      # (Optional) 3D jet physics stub
│   ├── TextRenderer.scala    # Stub for text rendering (customize if needed)
```

## How It Works

- **GameWindow.scala** is the entry point. It initializes SDL2 and OpenGL, manages the game loop, handles input, and coordinates game state.
- **JetRenderer**, **Bullet**, and **BotJet** manage drawing and updating objects.
- **DialogUI** draws overlays and clickable dialog buttons.
- **Vec2** and **FighterJet** provide vector math and (stub) 3D physics.

## Customization

- **Add More Bots:** Extend the logic in `GameWindow.scala` to add more bots or teams.
- **Improve Text Rendering:** Implement your own font rendering in `TextRenderer.scala`.
- **Add Sound:** Use SDL_mixer or another audio solution for effects.

## Troubleshooting

- **SDL2 Not Found:** Ensure SDL2 is installed and available in your system path.
- **OpenGL Issues:** Make sure your graphics drivers are up to date.
- **Compilation Errors:** Double-check your Scala Native and dependency versions.

## License

MIT License

## Credits

- Inspired by classic jet fighter arcade games.
- Developed by [Your Name] ([yourusername](https://github.com/yourusername)), 2025.

---

*Star this repo if you found it useful or fun! PRs and issues welcome.*
