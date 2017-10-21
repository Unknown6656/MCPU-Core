# The basic Gestalt:

## Inheritance tree:
```
a <──── b    "b inherits/extends a"


    IC <───────── EmulatedProcessor <────── LeverAwareEmulatedProcessor <────── SquareProcessor
    ↑↑                    ↑                                                      ↑ ↑ ↑ ↑ ↑ ↑ ↑
 ╭┄┄╯┆                    ┆                                                 x86 ┄╯ ┆ ┆ ┆ ┆ ┆ ╰┄ avr
 ┆   ┆                  Sensor                                              x64 ┄┄┄╯ ┆ ┆ ┆ ╰┄ z80
 ┆ Gates                 ↑  ↑                                                   amd ┄╯ ┆ ╰┄ stack-
 ┆ ↑↑↑↑↑                 ┆  ╰┄ proximity sensor                                        ┆    based
 ┆ ┆┆┆┆╰┄ not        .........                                                      .......
 ┆ ┆┆┆╰┄ xor, nxor
 ┆ ┆┆╰┄ and, nand
 ┆ ┆╰┄ or, nor
 ┆ ╰┄ .......
 ┆
 ╰┄ Displays
    ↑  ↑  ↑
    ┆  ┆  ╰┄ 7segment
    ┆  ╰┄┄┄┄ 4bit wool display 
 .......
```

### Orientation enum:
```java
public enum Orientation
{
    // possible IC orientations
    NORTH, WEST, SOUTH, EAST, VERTICAL_NS, VERTICAL_EW
}
```
### Abstract IC class definition:
```java
public abstract class IC
{
----------------------------- FIELDS -----------------------------

    // (  x  |  y  |  z  ) --> the IC's north-west corner
    // (xsize|ysize|zsize) --> the IC's dimensions (must be >= 0)
    protected final int x, y, z, xsize, ysize, zsize;

    // The IC's surrounding world
    protected final World world;

    // The current instance's creator
    protected final Player creator;

    // The IC's orientation in the world
    protected final Orientation orientation;

    // An array of io ports (initialized at .ctor-call) containing the current value & direction
    // the value is in the range of [0..15] and the direction is defined as:
    //  true  --> the IO pin is set to output (writing signals to the surrounding world)
    //  false --> the IO pin is set to input (reading signals from the surrounding world)
    protected abstract (byte, boolean)[] io;

---------------------------- ABSTRACTS ----------------------------

    // Fired by the plugin on each tick. This should only do 'internal' logic
    protected abstract void onTick();

    // Returns a string representing the current state
    public abstract string getState();

    // Returns the IO port's location
    public abstract Location getIOLocation(int port);

----------------------------- METHODS -----------------------------

    // Check whether the orientation is valid and assign local fields
    // No need to build blocks - the plugin or factory will do it for you
    public .ctor(Player cr, Location loc, (int, int, int) size, int iocount, Orientation orient)
    {
        if (invalid(orient))
            throw .....

        orientation = orient;
        (xsize|ysize|zsize) = size;
        (x|y|z) = loc.xyz;
        world = loc.world;
        creator = cr;
        io = new (byte, boolean)[iocount];
    }

    // Returns the number of io ports
    public final int getIOCount() -> return io != null ? io.length : 0;

    // Checks whether the port is inside the range and executes the callback if so
    private final void checkPort(int port, f() callback)
    {
        if ((port >= 0) && (port < getIOCount()))
            callback();
    }

    // Returns the IO port's value
    public final byte getIOValue(int port)
    {
        checkPort(port, () -> {
            if (io[port].direction)
                return io[port].value;
        });
    }

    // Sets the IO port's value
    public final void setIOValue(int port, byte value)
    {
        checkPort(port, () -> {
            if (!io[port].direction)
                io[port].value = value;
        });
    }

    // Returns the IO port's direction
    public final boolean getIODirection(int port)
    {
        checkPort(port, () -> {
            return io[port].direction;
        });
    }

    // Returns the creating player
    public final Player getCreator() -> creator;

    // Returns the surrounding world
    public final World getWorld() -> world;

    // Returns the IC's north-west corner
    public Location getLocation() -> new Location(world, x, y, z);

    // Returns true if location is inside the IC
    // Used by the plugin to check whether a block can be broken or not
    public final boolean testCollision (Location loc)
    {
        return loc != [ (x|y|z) ... (x+xsize-1|y+ysize-1|z+zsize-1) ];
    }
}   
```
## Emulated Processor abstract class definition:
```java
public abstract class EmulatedProcessor : IC
{
----------------------------- FIELDS ------------------------------

    // determines whether the processor can run or not
    private final boolean canrun;

    // the number of elapsed ticks since the last processor reset
    protected long ticks;

    // a λ-function, which will be called by 'executeNextInstruction()' in case of a fatal error.
    // The first parameter represents the error message, the second one the current instance
    // (simply pass 'this'). The plugin class automatically subsribes to such error handlers
    public (string, EmulatedProcessor -> void) onError;

---------------------------- ABSTRACTS ----------------------------

    // internal methods to extend start/stop/reset logic
    protected abstract void innerStop();
    protected abstract void innerStart();
    protected abstract void innerReset();

    // loads instruction from the given string (either as byte array, or as interpretable or compilable string)
    // w/ever...
    // This should call 'reset()' before the actual load.
    public abstract boolean load(string code);

    // the method which handles the instruction execution.
    // Calls 'stop()' if the Processor has no more instructions to execute
    public abstract void executeNextInstruction();

----------------------------- METHODS -----------------------------

    // <inherits parent constructor>

    // starts the processor
    public final void start()
    {
        innerStart();
        canrun = true;
    }

    // stops the processor
    public final void stop()
    {
        canrun = false;
        innerStop();
    }

    // resets the processor
    public final void reset()
    {
        stop();
    
        ticks = 0;
    
        innerReset();
    }

    // loads instruction from the given uri
    public final boolean load(URI uri)
    {
        String fetched_code = ...........

        return load(fetched_code);
    }

    // implementation from 'IC'
    @Override public void onTick()
    {
        if (canrun)
        {
            executeNextInstruction();

            ++ticks;
        }
    }

    // returns the number of elapsed ticks since the last reset
    public final long getElapsedTicks() -> ticks;
}
```
### Lever-aware emulated processor abstract class definition:
```java
public abstract class LeverAwareEmulatedProcessor : EmulatedProcessor
{
----------------------------- FIELDS -------------------------------

---------------------------- ABSTRACTS ----------------------------

    // returns the location of the on/off-lever for the current instance
    public abstract Location getLeverLocation();

----------------------------- METHODS -----------------------------

    // <inherits parent constructor>

    // implementation from 'IC'
    @Override protected final void executeNextInstruction()
    {
        if (canrun)
        {
            Block lever = getLeverLocation().getBlock();

            if (lever == Material.LEVER)
                if (lever.isPowered())
                {
                    executeNextInstruction();

                    ++ticks;
                }
        }
    }
}
```
### Suqare emulated processor abstract class definition:
```java
public abstract class LeverAwareEmulatedProcessor : LeverAwareEmulatedProcessor
{
----------------------------- FIELDS -------------------------------

---------------------------- ABSTRACTS ----------------------------

----------------------------- METHODS -----------------------------

    // creates square dimensions
    public .ctor(Player cr, Location loc, int iosidecount, Orientation orient)
    {
        super..ctor(cr, loc, (iosidecount * 2 + 1, 2, iosidecount * 2 + 1), iosidecount * 4, orient);
    }

    // implementation from 'LeverAwareEmulatedProcessor'
    @Override public final Location getLeverLocation() -> (x + 1|y|z + 1);
}
```
