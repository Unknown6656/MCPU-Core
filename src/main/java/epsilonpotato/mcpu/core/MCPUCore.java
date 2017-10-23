
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
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
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

import epsilonpotato.mcpu.core.components.SevenSegmentDisplayFactory;

import static java.lang.Math.*;


public abstract class MCPUCore extends JavaPlugin implements Listener
{
    public static final LinkedList<Triplet<Integer, Integer, Integer>> pendingexplosions = new LinkedList<>();
    public static final HashMap<Integer, IntegratedCircuit> circuits = new HashMap<>();
    public static String usagetext;
    public static Logger log;
    
    
    @Override
    public void onEnable()
    {
        usagetext = "/mcpu command usage:\n" +
                    " list              - Lists all MCPU components in the current world\n" +
                    " add <a> [io]      - Adds a new component with the architecture 'a' and io\n" +
                    "                     pin count 'io' to the world at the callers position\n" +
                    " add <x> <y> <z> <w> <a> [io]\n" +
                    "                   - Adds a new component to the world 'w' at the given coordinates 'x|y|z'\n" +
                    "                     with the architecture 'a' and io pin count 'io' (default = 3x3 pins)" +
                    " remove <n>        - Removes the component No. n\n" +
                    " loadb[ook] <n>    - Loads the book hold in the hand into the processor No. n\n" +
                    " loadu[ri] <n> <u> - Loads the string acessible via the given URI u into the processor No. n\n" +
                    " start <n>         - Starts the processor core No. n\n" +
                    " stop <n>          - Halts the processor core No. n\n" +
                    " next <n>          - Forces the execution of the next instruction of processor core No. n" +
                    " reset <n>         - Halts and resets the processor core No. n\n" +
                    " state <n>         - Displays the state of component No. n\n" +
                    " arch[itectures]   - Lists all available processor architectures and components";
        
        log = getLogger();
        
        try
        {
            ComponentFactory.registerFactory("7seg", new SevenSegmentDisplayFactory());
        }
        catch (Exception e)
        {
        }
        
        registerIntegratedCircuits();
        
        Print(ChatColor.WHITE, "Registered architectures/components (" + ComponentFactory.getRegisteredFactories().size() + "):\n" +
                               String.join("\n", ComponentFactory.getRegisteredFactories()));
        
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, this::onTick, 1, 1);
    }
    
    @Override
    public void onDisable()
    {
        for (IntegratedCircuit ic : circuits.values())
            if (ic instanceof EmulatedProcessor)
                ((EmulatedProcessor)ic).stop();
    }
    
    public void onTick()
    {
        circuits.values().forEach(c ->
        {
            // UPDATE INPUT IO
            for (int io = 0, cnt = c.getIOCount(); io < cnt; ++io)
                if (!c.getIODirection(io))
                {
                    int power = c.getIOLocation(io).getBlock().getBlockPower();
                    
                    c.setIOValue(io, (byte)(power & 0xff));
                }
            
            c.onTick();
            
            // UPDATE OUTPUT IO
            for (int io = 0, cnt = c.getIOCount(); io < cnt; ++io)
                if (c.getIODirection(io))
                {
                    boolean on = c.getIOValue(io) != 0;
                    
                    c.getIOLocation(io).getBlock().setType(on ? Material.REDSTONE_BLOCK : Material.IRON_BLOCK);
                }
        });
    }
    
    @EventHandler
    public void onBlockPlaceEvent(BlockPlaceEvent event)
    {
        Location l = event.getBlock().getLocation();
        
        for (IntegratedCircuit ic : circuits.values())
            if (ic.testCollision(l))
            {
                event.setCancelled(true);
                
                if (event.getPlayer() != null)
                    event.getPlayer().sendMessage(ChatColor.RED + "You cannot place a block on a registered integrated circuit.");
            }
    }
    
    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent event)
    {
        Location l = event.getBlock().getLocation();
        
        for (IntegratedCircuit ic : circuits.values())
            if (ic.testCollision(l))
            {
                event.setCancelled(true);
                
                if (event.getPlayer() != null)
                    event.getPlayer().sendMessage(ChatColor.RED + "You cannot destroy a block from a registered integrated circuit.");
            }
    }
    
    public abstract void registerIntegratedCircuits();
    
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
                    String[] tmp = new String[args.length]; // the fuck? is the created array's size smaller than in C++ or .NET ?
                    
                    // skip first element
                    for (int i = 1; i < args.length; ++i)
                        tmp[i] = args[i - 1];
                    
                    addComponent(sender, tmp);
                    
                    break;
                case "delete":
                case "remove":
                    getInt(args, 1, sender, i ->
                    {
                        if (circuits.containsKey(i))
                        {
                            IntegratedCircuit c = circuits.remove(i);
                            
                            deleteRegion(c.world, c.x, c.y, c.z, c.xsize, c.ysize, c.zsize);
                        }
                        else
                            Print(sender, ChatColor.RED, "The component No. " + i + " could not be found.");
                    });
                    break;
                case "reset":
                    getProcessor(args, 1, sender, proc -> proc.reset());
                    break;
                case "stop":
                    getProcessor(args, 1, sender, proc -> proc.stop());
                    break;
                case "start":
                    getProcessor(args, 1, sender, proc -> proc.start());
                    break;
                case "next":
                    getProcessor(args, 1, sender, proc -> proc.onTick());
                    break;
                case "loadb":
                case "loadbook":
                    getProcessor(args, 1, sender, proc ->
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
                                
                                CompileLoad(sender, proc, new URI("raw:" + b64));
                            }
                            catch (URISyntaxException e)
                            {
                            }
                        else
                            Error(sender, "You must be a player to run this command.");
                    });
                    break;
                case "loadu":
                case "loaduri":
                    final String[] argv = args;
                    
                    getProcessor(args, 1, sender, proc ->
                    {
                        String uri = getArg(argv, 2, sender);
                        
                        if (uri != null)
                            try
                            {
                                if (!CompileLoad(sender, proc, new URI(uri)))
                                    Error(sender, "The instructions could not be fetched from '" + uri + "'.");
                            }
                            catch (URISyntaxException ex)
                            {
                                Error(sender, "The instructions could not be fetched from '" + uri + "'.");
                            }
                    });
                    break;
                case "arch":
                case "architectures":
                    Print(sender, ChatColor.WHITE, String.join(", ", ComponentFactory.getRegisteredFactories()));
                    
                    break;
                case "state":
                    getIC(args, 1, sender, c -> sender.sendMessage("(" + c.getClass().getTypeName() + ") " + c.getState()));
                    break;
                case "list":
                    circuits.keySet().forEach((i) ->
                    {
                        IntegratedCircuit ic = circuits.get(i);
                        
                        sender.sendMessage("[" + i + "]: (" + ic.getClass().getTypeName() + ")" + ic.getState());
                    });
                    break;
                default:
                    if (!onUnprocessedCommand(sender, command, label, args))
                        Error(sender, "The command '" + args[0].trim() + "' is unknown.");
            }
            
            return true;
        }
        else
            return false;
    }
    
    private void addComponent(CommandSender sender, String[] args)
    {
        boolean canbuild = true;
        Player player = null;
        String icname;
        int cpusize = 0;
        Location loc;
        int x, y, z;
        
        if (sender instanceof Player)
            try
            {
                player = (Player)sender;
                loc = player.getLocation();
                icname = args[0];
                
                if (args.length > 1)
                    cpusize = Integer.parseInt(args[1]);
            }
            catch (Exception e)
            {
                Error(sender, "You must provide valid architecture name (and io pin size) for the creation of a new component/processor.");
                
                return;
            }
        else
            try
            {
                x = Integer.parseInt(args[1]);
                y = Integer.parseInt(args[2]);
                z = Integer.parseInt(args[3]);
                loc = new Location(getServer().getWorld(args[4]), x, y, z);
                icname = args[5];
                
                if (args.length > 6)
                    cpusize = Integer.parseInt(args[6]);
            }
            catch (Exception e)
            {
                Error(sender, "You must provide valid x-, y-, z-corrdinates, a valid world name, architecture name (and io pin size) for the creation of a new component/processor.");
                
                return;
            }
        
        x = loc.getBlockX();
        y = loc.getBlockY();
        z = loc.getBlockZ();
        cpusize = max(3, min(cpusize, 16));
        
        for (int i : circuits.keySet())
        {
            IntegratedCircuit ic = circuits.get(i);
            double dist = Math.sqrt(ic.xsize * ic.xsize + ic.ysize * ic.ysize + ic.zsize * ic.zsize) + 2;
            
            if (Math.abs(ic.y - y) < ic.ysize)
                if (Math.sqrt(Math.pow(ic.x - x, 2) + Math.pow(ic.y - y, 2) + Math.pow(ic.z - z, 2)) < dist)
                {
                    Error(sender, "The new processor/circuit cannot be placed here. It would be to close to the existing component no. " + i + ".");
                    
                    canbuild = false;
                    
                    break;
                }
        }
        
        if (canbuild)
            try
            {
                ComponentFactory<IntegratedCircuit> fac = ComponentFactory.getFactoryByName(icname);
                IntegratedCircuit ic = fac.spawnComponent(this, player, loc.getWorld(), x, y, z, ComponentOrientation.NORTH, cpusize);
                int num = circuits.size();
                
                if (ic instanceof EmulatedProcessor)
                    ((EmulatedProcessor)ic).onError = (p, s) -> Print(sender, ChatColor.YELLOW, "Processor " + num + " failed with the folling message:\n" + s);
                
                circuits.put(num, ic);
                
                Error(sender, "The component No. " + num + " has been created.");
            }
            catch (InvalidOrientationException o)
            {
                Error(sender, o.getMessage());
            }
            catch (Exception e)
            {
                Error(sender, "The new component/circuit could not be created. The architecture or type '" + icname + "' is unknown.");
            }
    }
    
    protected boolean CompileLoad(CommandSender sender, EmulatedProcessor core, URI source)
    {
        boolean result = core.load(source);
        
        if (result)
            Print(sender, ChatColor.GREEN, "The code was successfully loaded into the core.");
        else
            Error(sender, "The code could not be loaded into the core.");
        
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
    
    private void getIC(final String[] argv, final int ndx, final CommandSender sender, Consumer<IntegratedCircuit> action)
    {
        getInt(argv, ndx, sender, i ->
        {
            if (circuits.containsKey(i))
                action.accept(circuits.get(i));
            else
                sender.sendMessage(ChatColor.RED + "The core No. " + i + " could not be found.");
        });
    }
    
    private void getProcessor(final String[] argv, final int ndx, final CommandSender sender, Consumer<EmulatedProcessor> action)
    {
        getIC(argv, ndx, sender, ic ->
        {
            if (ic instanceof EmulatedProcessor)
                action.accept((EmulatedProcessor)ic);
            else
                Error(sender, "The component in question must be an emulated processor to perform the current action.");
        });
    }
    
    private static void getInt(final String[] argv, final int ndx, final CommandSender sender, Consumer<Integer> action)
    {
        String arg = getArg(argv, ndx, sender);
        
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
    
    private static String getArg(final String[] argv, final int ndx, final CommandSender sender)
    {
        if (ndx < argv.length)
            return argv[ndx];
        else
            sender.sendMessage(ChatColor.RED + "At least " + (argv.length - ndx + 1) + " more argument(s) are required for this command.");
        
        return null;
    }
    
    protected static void deleteRegion(World w, int x, int y, int z, int xs, int ys, int zs)
    {
        for (int i = 0; i < xs; ++i)
            for (int j = 0; j < zs; ++j)
                for (int k = ys - 1; k >= 0; --k)
                    SetBlock(w, x + i, y + k, z + j, Material.AIR);
    }
    
    public static Block SetBlock(World w, int x, int y, int z, Material m)
    {
        Block b = new Location(w, x, y, z).getBlock();
        
        b.setType(m);
        
        return b;
    }
    
    public static void SetBlock(World w, int x, int y, int z, Material m, Consumer<Block> f)
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
    
    public static void Error(CommandSender s, String m)
    {
        Print(s, ChatColor.RED, m);
    }
}
