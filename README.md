# WI Freecam (Wurst-Imperium Freecam) ![](https://img.wimods.net/github.com/Wurst-Imperium/WI-Freecam)

WI Freecam allows you to detach your camera and move it around freely while your character stays in place. It's the photo mode that Mojang never added. Spectator mode except it doesn't move your character and works on any server. The literal perspective shift you need to become a better builder.

Have you ever built a giant dirt tower just to get a better view of your village? Ever wasted half a stack of rockets trying to take a screenshot of your skyscraper from the perfect angle? Ever dug up all your hidden redstone because you forgot how it works? These are common signs that you need a Freecam.

## Why choose WI Freecam

### Movement controls that don't suck

You shouldn't have to fight your tools to position a camera.

- WASD is for horizontal movement only. Pressing W won't send you diagonally downward just because your crosshair isn't perfectly level. You have space and shift for that.

- Your camera stops moving the moment you release the button. No sliding past your target as if you're on ice.

- Scroll to change speed, but with precision. You can quickly adjust your speed, but you can also scroll it to an exact value. The on-screen indicator displays your current speed so that there's no guessing (unlike vanilla spectator mode).

- Vertical speed that actually makes sense. WI Freecam stores your preferred *ratio* between horizontal and vertical speed, not an absolute value that you then have to re-adjust every time. Set it to 50% once and it will always be half as fast as your horizontal speed.

- Precise input mode. CTRL-click on any slider and type in the exact number you want, instead of fiddling with the mouse for 20 minutes because you're one pixel off.

### Always up to date

I'm tired of mods that take forever to update when a new Minecraft version comes out. You should be too. "Long term modding version" is a made-up concept by modders who either can't escape dependency hell or stopped playing Minecraft years ago.

WI Freecam provides day one support for every new Minecraft version, usually within **30 minutes** of release. That's right, minutes, not weeks! Snapshots are supported too, though those can take a bit longer if they break something.

How is this possible?

First, through lots of automation. As soon as a new Minecraft version is released, a robot compiles the mod against that version, runs it, and goes through a big testing routine where it does everything you could possibly do in the mod at a ridiculously fast speed ([it's quite fun to watch](https://www.youtube.com/watch?v=fKRFiPcDbfw)). If all tests pass, it then also releases the mod automatically. But if even one pixel looks different, it instead tells me what went wrong so I can get straight to fixing it.

Second, because I built WI Freecam using my own custom settings engine instead of relying on an off-the-shelf config library. You can have the best snapshot automation in the world, but that won't help you at all if you're stuck waiting for Cloth Config to update (as I found out the hard way in my last mod).

### A proper modern Freecam

Most Freecam mods are built on decade-old workarounds. Some freeze your movement packets, others spawn a fake camera entity. Both methods mess up the game's physics, which then requires dozens of messy patches to deal with the side effects.

WI Freecam does something radically simpler: **it just moves the camera**. No fake entities. No frozen packets. No physics workarounds. The camera is just a coordinate: no hitbox, no collision, no name tag, no problems. This wasn't possible in older Minecraft versions, but clinging to outdated fakery today is unnecessary. WI Freecam is what Freecam should have always been.

### Safe to use on servers

I can't believe this is a standout feature, but WI Freecam seems to be the only Freecam mod that isn't vulnerable to translation exploits like [MC-265322](https://bugs.mojang.com/browse/MC/issues/MC-265322) ([link with more info](https://wurst.wiki/sign_translation_vulnerability)).

It sucks that Mojang still hasn't patched this bug (it's been around since 1.20), but at the same time you should be able to just install a mod and expect it to be safe. There's a certain other Freecam mod developer out there who tells you that this exploit exists but then just goes "good luck, I'm not gonna do anything about it", which I find extremely irresponsible.

WI Freecam still has translations and keybinds by the way. Don't believe anyone who tells you that's not possible.

## Feature List

- Free camera movement
- Full player physics while in Freecam
- No fake entities or movement packet shenanigans
- On-the-fly switching between camera control and character control (keybindable too)
- Separate horizontal and vertical speed sliders (ratio-based)
- Scroll to change speed (optional)
- On-screen speed indicator (optional)
- Adjustable initial camera position (inside, in front, on top)
- Optional tracer line to help you find your character
- Hide hand for clean screenshots (optional)
- Disable on damage for safety (optional)
- Works on snapshots, updates instantly to new releases
- No third-party dependencies other than Fabric API
- Automated testing to ensure it works reliably

Disclosure: This mod collects anonymous version statistics, which help me decide which old versions are still worth supporting. An opt out is available in the settings menu, but no personal data is collected anyways.

## Downloads

[![Download WI Freecam](https://i.imgur.com/7agiImL.png)](https://go.wimods.net/from/github.com/Wurst-Imperium/WI-Freecam?to=https://www.wimods.net/wi-freecam/download/?utm_source=GitHub&utm_medium=WI+Freecam&utm_campaign=README.md&utm_content=WI+Freecam+GitHub+repo+download+button)

## Installation

> [!IMPORTANT]
> Always make sure that your modloader and all of your mods are made for the same Minecraft version. Your game will crash if you mix different versions.

### Installation using Fabric

1. Install [Fabric Loader](https://go.wimods.net/from/github.com/Wurst-Imperium/WI-Freecam?to=https://fabricmc.net/use/installer/).
2. Add [Fabric API](https://go.wimods.net/from/github.com/Wurst-Imperium/WI-Freecam?to=https://modrinth.com/mod/fabric-api) to your mods folder.
3. Add WI Freecam to your mods folder.

(NeoForge is not yet supported.)

## Chat Commands

- `/freecam`: Opens the settings menu

## Keybinds

- Toggle Freecam (default: <kbd>U</kbd>)
- Switch Camera/Player Control (default: not bound)
- Open Settings (default: <kbd>Right CTRL</kbd>)

## Supported languages

- English (US)
- German (Germany)

Pull requests welcome - [add your native language](https://github.com/Wurst-Imperium/WI-Freecam/tree/master/src/main/resources/assets/wi_freecam/translations)!

## Development Setup

> [!IMPORTANT]
> Make sure you have [Java Development Kit 21](https://adoptium.net/temurin/releases?version=21&os=any&arch=any) installed. It won't work with other versions.

### Development using Eclipse

1. Clone the repository:

   ```pwsh
   git clone https://github.com/Wurst-Imperium/WI-Freecam.git
   cd WI-Freecam
   ```

2. Generate the sources:

   In Fabric versions:
   ```pwsh
   ./gradlew genSources eclipse
   ```

3. In Eclipse, go to `Import...` > `Existing Projects into Workspace` and select this project.

4. **Optional:** Right-click on the project and select `Properties` > `Java Code Style`. Then under `Clean Up`, `Code Templates`, `Formatter`, import the respective files in the `codestyle` folder.

### Development using VSCode / Cursor

> [!TIP]
> You'll probably want to install the [Extension Pack for Java](https://go.wimods.net/from/github.com/Wurst-Imperium/WI-Freecam?to=https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack) to make development easier.

1. Clone the repository:

   ```pwsh
   git clone https://github.com/Wurst-Imperium/WI-Freecam.git
   cd WI-Freecam
   ```

2. Generate the sources:

   In Fabric versions:
   ```pwsh
   ./gradlew genSources vscode
   ```

3. Open the `WI-Freecam` folder in VSCode / Cursor.

4. **Optional:** In the VSCode settings, set `java.format.settings.url` to `https://raw.githubusercontent.com/Wurst-Imperium/WI-Freecam/master/codestyle/formatter.xml` and `java.format.settings.profile` to `Wurst-Imperium`.

### Development using IntelliJ IDEA

I don't use or recommend IntelliJ, but the commands to run would be:

```pwsh
git clone https://github.com/Wurst-Imperium/WI-Freecam.git
cd WI-Freecam
./gradlew genSources idea --no-configuration-cache
```

**Note:** IntelliJ IDEA is [not yet compatible](https://github.com/FabricMC/fabric-loom/issues/1349) with Gradle's configuration cache. You will run into issues.

Possible workarounds:
- Turn off args files ([this setting](https://i.imgur.com/zHqIOYg.png)). Won't work for some users because of a command length limit.
- Add `--no-configuration-cache` to all of your Gradle commands.
