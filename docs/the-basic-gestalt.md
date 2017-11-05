# The basic Gestalt:

This is the optimal layout looks as follows:
```plain
a <┄┄┄┄ b    "b inherits/extends a"


    IC <┄┄┄┄┄┄┄┄┄ EmulatedProcessor <┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄ SquareProcessor
    ↑↑                    ↑                                ↑ ↑ ↑ ↑ ↑ ↑ ↑
 ╭┄┄╯┆                    ┆                           x86 ┄╯ ┆ ┆ ┆ ┆ ┆ ╰┄ avr
 ┆   ┆                  Sensor                        x64 ┄┄┄╯ ┆ ┆ ┆ ╰┄ z80
 ┆ Gates                 ↑  ↑                             amd ┄╯ ┆ ╰┄ stack-
 ┆ ↑↑↑↑↑                 ┆  ╰┄ proximity sensor                  ┆    based
 ┆ ┆┆┆┆╰┄ not         .......                                 .......
 ┆ ┆┆┆╰┄ xor, nxor
 ┆ ┆┆╰┄ and, nand
 ┆ ┆╰┄ or, nor
 ┆ ╰┄ .....
 ┆
 ╰┄ Displays
    ↑  ↑  ↑
    ┆  ┆  ╰┄ 7segment
    ┆  ╰┄┄┄┄ 4bit wool display 
 .......
```

The current implementation resembles more like this:
```plain
a <┄┄┄┄ b    "b inherits/extends a"


    IntegratedCircuit <┄┄┄┄ EmulatedProcessor <┄┄┄┄ LeverAwareEmulatedProcessor <┄┄┄┄ SquareEmulatedProcessor
    ↑ ↑ ↑ ↑ ↑ ↑ ↑ ↑ ↑                                                                           ↑
    ┆ ┆ ┆ ┆ ┆ ┆ ┆ ┆ ╰┄ BinaryLogicGate                                                          ┆
    ┆ ┆ ┆ ┆ ┆ ┆ ┆ ┆    ↑  ↑  ↑  ↑  ↑                                                 implementations from other
    ┆ ┆ ┆ ┆ ┆ ┆ ┆ ┆    ┆  ┆  ┆  ┆  ╰┄ not                                           repositories, e.g. Zedly/MCPU
    ┆ ┆ ┆ ┆ ┆ ┆ ┆ ┆    ┆  ┆  ┆  ╰┄ xor, nxor                                         and Unknown6656/MCPU-Spigot
    ┆ ┆ ┆ ┆ ┆ ┆ ┆ ┆    ┆  ┆  ╰┄ and, nand
    ┆ ┆ ┆ ┆ ┆ ┆ ┆ ┆    ┆  ╰┄ or, nor
    ┆ ┆ ┆ ┆ ┆ ┆ ┆ ┆    ╰┄ .....
    ┆ ┆ ┆ ┆ ┆ ┆ ┆ ┆ 
    ┆ ┆ ┆ ┆ ┆ ┆ ┆ ╰┄ LogicGate2x2
    ┆ ┆ ┆ ┆ ┆ ┆ ┆     ↑   ↑   ↑
    ┆ ┆ ┆ ┆ ┆ ┆ ┆     ┆   ┆   ╰┄ hadd (Half Adder)
    ┆ ┆ ┆ ┆ ┆ ┆ ┆     ┆   ╰┄ T Flip Flop
    ┆ ┆ ┆ ┆ ┆ ┆ ┆     ╰┄ .....
    ┆ ┆ ┆ ┆ ┆ ┆ ┆ 
    ┆ ┆ ┆ ┆ ┆ ┆ ╰┄ SevenSegmentDisplay
    ┆ ┆ ┆ ┆ ┆ ╰┄ WoolDisplay16x16, WoolDisplay32x32
    ┆ ┆ ┆ ┆ ╰┄ Multiplexer1to8, Demultiplexer1to8
    ┆ ┆ ┆ ╰┄ SmokeGenerator
    ┆ ┆ ┆
  .........
```

All components define I/O-Ports for interaction with redstone circuits and are being created by component factories, which in turn are instanciated by the plugin's core class `MCPUCore`.<br/>
See the file [api.md](api.md) for more information.
