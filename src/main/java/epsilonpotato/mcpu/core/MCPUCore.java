
package epsilonpotato.mcpu.core;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;

import static java.lang.Math.*;


public abstract class MCPUCore extends JavaPlugin implements Listener
{
    public static final LinkedList<Triplet<Integer, Integer, Integer>> pendingexplosions = new LinkedList<>();
    public static final HashMap<Integer, EmulatedProcessor> cores = new HashMap<>();
    private static final int CPUSIZE = 3; // TODO fix
    public static String usagetext;
    public static Logger log;
    
    
    @Override
    public void onEnable()
    {
        usagetext = "/mcpu command usage:\n" +
                    " list              - Lists all available MCPU cores\n" +
                    " add7seg           - Adds a new 7-segment-display to the world at the callers position\n" +
                    " add <a> [io]      - Adds a new core with the architecture 'a' and io\n" +
                    "                     pin count 'io' to the world at the callers position\n" +
                    " add <x> <y> <z> <w> <a> [io]\n" +
                    "                   - Adds a new core to the world 'w' at the given coordinates 'x|y|z'\n" +
                    "                     with the architecture 'a' and io pin count 'io' (default = 3x3 pins)" +
                    " remove <n>        - Removes the core No. n\n" +
                    " loadb[ook] <n>    - Loads the book hold in the hand into the core No. n\n" +
                    " loadu[ri] <n> <u> - Loads the string acessible via the given URI u into the core No. n\n" +
                    " start <n>         - Starts the processor core No. n\n" +
                    " stop <n>          - Halts the core No. n\n" +
                    " next <n>          - Forces the execution of the next instruction of core No. n" +
                    " reset <n>         - Halts and resets the core No. n\n" +
                    " state <n>         - Displays the state of core No. n\n" +
                    " arch[itectures]   - Lists all available processor architectures";
        
        log = getLogger();
        
        registerProcessorArchitectures();
        
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> cores.values().forEach(c ->
        {
            c.nextInstruction();
            
            for (int io = 0, cnt = c.getIOCount(); io < cnt; ++io)
                if (c.getIODirection(io))
                {
                    boolean on = c.getIO(io) != 0;
                    
                    c.getIOLocation(io).getBlock().setType(on ? Material.REDSTONE_BLOCK : Material.IRON_BLOCK);
                }
                else
                {
                    int power = c.getIOLocation(io).getBlock().getBlockPower();
                    
                    c.setIO(io, (byte)(power & 0xff));
                }
        }), 1, 1);
    }
    
    @Override
    public void onDisable()
    {
        for (EmulatedProcessor core : cores.values())
            core.stop();
    }
    
    @EventHandler
    public void onBlockPlaceEvent(BlockPlaceEvent event)
    {
        Location l = event.getBlock().getLocation();
        
        for (EmulatedProcessor core : cores.values())
            if (core.testCollision(l))
            {
                event.setCancelled(true);
                
                if (event.getPlayer() != null)
                    event.getPlayer().sendMessage(ChatColor.RED + "You cannot place a block on a registered CPU core.");
            }
    }
    
    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent event)
    {
        Location l = event.getBlock().getLocation();
        
        for (EmulatedProcessor core : cores.values())
            if (core.testCollision(l))
            {
                event.setCancelled(true);
                
                if (event.getPlayer() != null)
                    event.getPlayer().sendMessage(ChatColor.RED + "You cannot destroy a block from a registered CPU core.");
            }
    }
    
    public abstract void registerProcessorArchitectures();
    
    public abstract void onWorldSaveEvent(WorldSaveEvent event);
    
    public abstract void onWorldLoadEvent(WorldLoadEvent event);
    
    public abstract void onWorldInitEvent(WorldInitEvent event);
    
    public abstract boolean onUnprocessedCommand(CommandSender sender, Command command, String label, String[] args);
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (command.getName().equalsIgnoreCase("mcpu"))
        {
            if (args.length == 0)
                args = new String[] { "?" };
            
            switch (args[0].toLowerCase().trim())
            {
                case "?":
                case "help":
                    Print(sender, ChatColor.YELLOW, usagetext);
                    break;
                case "add":
                {
                    Player player = null;
                    boolean canbuild = true;
                    String architecture;
                    int cpusize = 0;
                    Location loc;
                    int x, y, z;
                    
                    if (sender instanceof Player)
                        try
                        {
                            player = (Player)sender;
                            loc = player.getLocation();
                            architecture = args[0];

                            if (args.length > 1)
                                cpusize = Integer.parseInt(args[1]);
                        }
                        catch (Exception e)
                        {
                            Print(sender, ChatColor.RED, "You must provide valid architecture name and cpu size for the creation of a new core.");
                            
                            break;
                        }
                    else
                        try
                        {
                            x = Integer.parseInt(args[1]);
                            y = Integer.parseInt(args[2]);
                            z = Integer.parseInt(args[3]);
                            loc = new Location(getServer().getWorld(args[4]), x, y, z);
                            architecture = args[5];
                            
                            if (args.length > 6)
                                cpusize = Integer.parseInt(args[6]);
                        }
                        catch (Exception e)
                        {
                            Print(sender, ChatColor.RED, "You must provide valid x-, y-, z-corrdinates, a valid world name, architecture name and a cpu size for the creation of a new core.");
                            
                            break;
                        }
                    
                    x = loc.getBlockX();
                    y = loc.getBlockY();
                    z = loc.getBlockZ();
                    cpusize = max(3, min(cpusize, 16));
                    
                    for (int i : cores.keySet())
                    {
                        EmulatedProcessor core = cores.get(i);
                        double dist = Math.sqrt(core.xsize * core.xsize + core.ysize * core.ysize + core.zsize * core.zsize) + 2;
                        
                        if (Math.abs(core.y - y) < core.ysize)
                            if (Math.sqrt(Math.pow(core.x - x, 2) + Math.pow(core.y - y, 2) + Math.pow(core.z - z, 2)) < dist)
                            {
                                Print(sender, ChatColor.RED, "The new processor core cannot be placed here. It would be to close to existing core no. " + i + ".");
                                
                                canbuild = false;
                                
                                break;
                            }
                    }
                    
                    if (canbuild)
                        try
                        {
                            EmulatedProcessorFactory<EmulatedProcessor> fac = EmulatedProcessorFactory.getFactoryByArchitectureName(architecture);
                            
                            Tuple<Integer, EmulatedProcessor> t = SpawnCPU(player, x, y, z, loc.getWorld(), CPUSIZE, fac);
                            
                            t.y.onError = (p, s) -> Print(sender, ChatColor.YELLOW, "Processor " + t.x + " failed with the folling message:\n" + s);
                            
                            Print(sender, ChatColor.GREEN, "The core No. " + t.x + " has been created.");
                        }
                        catch (Exception e)
                        {
                            Print(sender, ChatColor.RED, "The new processor core could not be created. The architecture '" + architecture + "' is unknown.");
                        }
                }
                    break;
                case "add7seg":
                    if (sender instanceof Player)
                    {
                        Player player = (Player)sender;
                        Location loc = player.getLocation();
                        Tuple<Integer, SevenSegmentDisplay> t = spawnDisplay(player, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld());
                        
                        Print(sender, ChatColor.GREEN, "The display No. " + t.x + " has been created.");
                    }
                    else
                        Print(sender, ChatColor.RED, "You must be a player inside the world to place a 7-segment-display.");
                    
                    break;
                case "delete":
                case "remove":
                    GetInt(args, 1, sender, i ->
                    {
                        if (cores.containsKey(i))
                        {
                            EmulatedProcessor c = cores.remove(i);
                            
                            deleteRegion(c.world, c.x, c.y, c.z, c.xsize, c.ysize, c.zsize);
                        }
                        else
                            Print(sender, ChatColor.RED, "The core No. " + i + " could not be found.");
                    });
                    break;
                case "reset":
                    GetCore(args, 1, sender, c -> c.reset());
                    break;
                case "stop":
                    GetCore(args, 1, sender, c -> c.stop());
                    break;
                case "start":
                    GetCore(args, 1, sender, c -> c.start());
                    break;
                case "next":
                    GetCore(args, 1, sender, c -> c.nextInstruction());
                    break;
                case "loadb":
                case "loadbook":
                    GetCore(args, 1, sender, c ->
                    {
                        if (sender instanceof Player)
                            try
                            {
                                Player player = (Player)sender;
                                ItemStack stack = player.getInventory().getItemInMainHand();
                                String[] book = GetBook(stack);
                                
                                if (book == null)
                                    book = GetBook(player.getInventory().getItemInOffHand());
                                
                                String b64 = new String(Base64.getEncoder().encode(String.join("\n", book).getBytes()));
                                
                                CompileLoad(sender, c, new URI("raw:" + b64));
                            }
                            catch (URISyntaxException e)
                            {
                            }
                        else
                            Print(sender, ChatColor.RED, "You must be a player to run this command.");
                    });
                    break;
                case "loadu":
                case "loaduri":
                    final String[] argv = args; // IDE throws some obscure error
                    // if I replace 'argv' with
                    // 'args' in this block
                    
                    GetCore(args, 1, sender, c ->
                    {
                        String uri = GetArg(argv, 2, sender);
                        
                        if (uri != null)
                            try
                            {
                                if (!CompileLoad(sender, c, new URI(uri)))
                                    Print(sender, ChatColor.RED, "The instructions could not be fetched from '" + uri + "'.");
                            }
                            catch (URISyntaxException ex)
                            {
                                Print(sender, ChatColor.RED, "The instructions could not be fetched from '" + uri + "'.");
                            }
                    });
                    break;
                case "arch":
                case "architectures":
                    Print(sender, ChatColor.WHITE, String.join(", ", EmulatedProcessorFactory.getRegisteredArchitectures()));
                    
                    break;
                case "state":
                    GetCore(args, 1, sender, c -> sender.sendMessage("(" + c.getClass().getTypeName() + ") " + c.getState()));
                    break;
                case "list":
                    cores.keySet().forEach((i) ->
                    {
                        EmulatedProcessor c = cores.get(i);
                        
                        sender.sendMessage("[" + i + "]: (" + c.getClass().getTypeName() + ")" + c.getState());
                    });
                    break;
                default:
                    if (!onUnprocessedCommand(sender, command, label, args))
                        Print(sender, ChatColor.RED, "The command '" + args[0].trim() + "' is unknown.");
            }
            
            return true;
        }
        else
            return false;
    }
    
    protected boolean CompileLoad(CommandSender sender, EmulatedProcessor core, URI source)
    {
        boolean result = core.load(source);
        
        if (result)
            Print(sender, ChatColor.GREEN, "The code was successfully loaded into the core.");
        else
            Print(sender, ChatColor.RED, "The code could not be loaded into the core.");
        
        return result;
    }
    
    private static String[] GetBook(ItemStack stack)
    {
        if (stack != null)
            if (stack.getAmount() > 0)
                if (((stack.getType() == Material.BOOK_AND_QUILL) || (stack.getType() == Material.WRITTEN_BOOK)) && stack.hasItemMeta())
                {
                    BookMeta bm = (BookMeta)stack.getItemMeta();
                    String[] lines = new String[0];
                    
                    return bm.getPages().toArray(lines);
                }
            
        return null;
    }
    
    private void GetCore(final String[] argv, final int ndx, final CommandSender sender, Consumer<EmulatedProcessor> action)
    {
        GetInt(argv, ndx, sender, i ->
        {
            if (cores.containsKey(i))
                action.accept(cores.get(i));
            else
                sender.sendMessage(ChatColor.RED + "The core No. " + i + " could not be found.");
        });
    }
    
    private static void GetInt(final String[] argv, final int ndx, final CommandSender sender, Consumer<Integer> action)
    {
        String arg = GetArg(argv, ndx, sender);
        
        if (arg != null)
            try
            {
                action.accept(Integer.parseInt(arg));
            }
            catch (Exception e)
            {
                sender.sendMessage(ChatColor.RED + "The argument " + (ndx + 1) + " could not be interpreted as a numeric value.");
            }
    }
    
    private static String GetArg(final String[] argv, final int ndx, final CommandSender sender)
    {
        if (ndx < argv.length)
            return argv[ndx];
        else
            sender.sendMessage(ChatColor.RED + "At least " + (argv.length - ndx + 1) + " more argument(s) are required for this command.");
        
        return null;
    }
    
    protected void deleteRegion(World w, int x, int y, int z, int xs, int ys, int zs)
    {
        for (int i = 0; i < xs; ++i)
            for (int j = 0; j < zs; ++j)
                for (int k = ys - 1; k >= 0; --k)
                    SetBlock(w, x + i, y + k, z + j, Material.AIR);
    }
    
    @SuppressWarnings("deprecation")
    protected <T extends EmulatedProcessor> Tuple<Integer, T> SpawnCPU(Player p, int x, int y, int z, World w, int iosidecount, EmulatedProcessorFactory<T> fac)
    {
        int sidelength = iosidecount * 2 - 1;
        
        deleteRegion(w, x - 2, y - 1, z - 2, sidelength + 4, 3, sidelength + 4);
        
        // CREATE STONE BASE
        for (int i = -2; i < sidelength + 2; ++i)
            for (int j = -2; j < sidelength + 2; ++j)
                SetBlock(w, x + i, y - 1, z + j, Material.STONE);
        
        // CREATE WOOL BODY
        for (int i = 0; i < sidelength; ++i)
            for (int j = 0; j < sidelength; ++j)
                SetBlock(w, x + i, y, z + j, Material.WOOL, b -> b.setData(DyeColor.BLACK.getWoolData())); // TODO: fix deprecated calls
        
        // CREATE GOLD CORNER + SIGN + LEVER
        int num = cores.size();
        
        SetBlock(w, x, y, z, Material.GOLD_BLOCK);
        SetBlock(w, x + 1, y, z, Material.GOLD_BLOCK);
        SetBlock(w, x - sidelength + 1, y + 1, z - sidelength, Material.LEVER, b ->
        {
            b.setData((byte)6);
            b.getState().update();
        });
        SetBlock(w, x - sidelength, y + 1, z - sidelength, Material.SIGN_POST, b ->
        {
            Sign sign = (Sign)b.getState();
            
            sign.setLine(0, "CPU No. " + num);
            sign.setLine(1, p.getDisplayName());
            sign.update();
        });
   
        // CREATE CONNECTOR PINS AND WIRE
        for (int i = 0; i <= sidelength; i += 2)
        {
            SetBlock(w, x + i, y, z - 1, Material.IRON_BLOCK); // NORTH SIDE
            SetBlock(w, x + i, y, z + sidelength + 1, Material.IRON_BLOCK); // SOUTH SIDE
            SetBlock(w, x - 1, y, z + i, Material.IRON_BLOCK); // WEST SIDE
            SetBlock(w, x + sidelength + 1, y, z + i, Material.IRON_BLOCK); // EAST SIDE
            
            SetBlock(w, x + i, y, z - 2, Material.REDSTONE_WIRE); // NORTH SIDE
            SetBlock(w, x + i, y, z + sidelength + 2, Material.REDSTONE_WIRE); // SOUTH SIDE
            SetBlock(w, x - 2, y, z + i, Material.REDSTONE_WIRE); // WEST SIDE
            SetBlock(w, x + sidelength + 2, y, z + i, Material.REDSTONE_WIRE); // EAST SIDE
        }

        T proc = fac.createProcessor(p, new Location(w, x - 1, y, z - 1), new Triplet<>(sidelength + 2, 2, sidelength + 2), iosidecount * 4);
        
        cores.put(num, proc);
        
        return new Tuple<>(num, proc);
    }

    @SuppressWarnings("deprecation")
    protected Tuple<Integer, SevenSegmentDisplay> spawnDisplay(Player p, int x, int y, int z, World w)
    {
        // CREATE STONE BASE
        for (int i = 0; i < 9; ++i)
            for (int j = 0; j < 14; ++j)
                SetBlock(w, x + i, y - 1, z + j, Material.STONE);

        // CREATE WOOL FRAME
        for (int i = 0; i < 9; ++i)
            for (int j = 0; j < 12; ++j)
                SetBlock(w, x + i, y - 1, z + j, Material.STONE);
        
        // CREATE GOLD BLOCK + LEVER
        SetBlock(w, x, y, z, Material.GOLD_BLOCK);
        SetBlock(w, x, y + 1, z, Material.LEVER, b ->
        {
            b.setData((byte)6);
            b.getState().update();
        });
        
        // CREATE PINS
        for (int i = 0; i < 9; i += 2)
        {
            SetBlock(w, x + i, y, z + 12, Material.IRON_BLOCK);
            SetBlock(w, x + i, y, z + 13, Material.REDSTONE_WIRE);
        }
        
        return add(new SevenSegmentDisplay(p, x, y, z));
    }
    
    private <T extends EmulatedProcessor> Tuple<Integer, T> add(T proc) 
    {
        int num = cores.size();

        cores.put(num, proc);
        
        return new Tuple<Integer, T>(num, proc);
    }
    
    protected static Block SetBlock(World w, int x, int y, int z, Material m)
    {
        Block b = new Location(w, x, y, z).getBlock();
        
        b.setType(m);
        
        return b;
    }
    
    protected static void SetBlock(World w, int x, int y, int z, Material m, Consumer<Block> f)
    {
        f.accept(SetBlock(w, x, y, z, m));
    }
    
    public static void Print(ChatColor c, String m)
    {
        Print(null, c, m);
    }
    
    public static void Print(CommandSender s, ChatColor c, String m)
    {
        if (s != null)
            s.sendMessage(c + m);
        
        log.log(Level.INFO, c + m);
    }
}
