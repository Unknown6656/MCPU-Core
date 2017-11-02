# MCPU Core Framework
_A framework for instruction set emulators and Bukkit integration_

---

The MCPU Core Framework (`MCPUCore`) is a framework which provides the basic structure to emulate electronic components and integrate them into the sandbox-game [Minecraft](http://minecraft.net) as a [Bukkit](http://bukkit.org) server-plugin.

It is composed of the following parts:
 - **Component interface definitions** to define a consistent API for (virtual) electronic components
 - **Bukkit plugin implementations** to easily build a plugin on top of the API
 - **Utility classes** to provide some interopt functionality
 - **Pre-defined components** like 7-segment-displays, 4Bit-color displays, basic ICs and a base for emulated processors.

## Component interface definitions:

The API structure can be found [here](https://github.com/Unknown6656/MCPU-Core/blob/master/docs/readme.md).

## Components

To see the list of implemented components, go to the actual plugin implementation, which can be found here:
[https://github.com/Unknown6656/MCPU-Spigot](https://github.com/Unknown6656/MCPU-Spigot)

## Download section

 - You can find all releases here: [https://github.com/Unknown6656/MCPU-Core/releases](https://github.com/Unknown6656/MCPU-Core/releases)
 - The newest (beta) build can be found here:
 	- [`master` branch](https://github.com/Unknown6656/MCPU-Core/blob/master/target/MCPU-Core-1.0-SNAPSHOT.jar?raw=true)
 	- [`dev` branch](https://github.com/Unknown6656/MCPU-Core/blob/dev/target/MCPU-Core-1.0-SNAPSHOT.jar?raw=true)
  
## (More or less) useful links

 - [https://github.com/Zedly/MCPU](https://github.com/Zedly/MCPU)<br/>
   Zedly's implementation containing mainly the AVR instruction set
 - [https://github.com/Unknown6656/MCPU-Spigot](https://github.com/Unknown6656/MCPU-Spigot)<br/>
   My bukkit implementation containing the plugin and a stack-based instruction set + compiler
 - [https://github.com/Unknown6656/MCPU](https://github.com/Unknown6656/MCPU)<br/>
   An older variation of the project which drifted apart from the 'Minecraft-concept' to become an emulator of its own