
package epsilonpotato.mcpu.core;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;

import epsilonpotato.mcpu.core.components.BinaryLogicGateFactory;
import epsilonpotato.mcpu.core.components.SevenSegmentDisplayFactory;
import epsilonpotato.mcpu.util.Triplet;
import epsilonpotato.mcpu.util.Tuple;

import static java.lang.Math.*;


public abstract class MCPUCore extends JavaPlugin implements Listener
{
    public static final LinkedList<Triplet<Integer, Integer, Integer>> pendingexplosions = new LinkedList<>();
    public static final HashMap<Integer, IntegratedCircuit> circuits = new HashMap<>();
    public static String usagetext;
    public static Logger log;
    public static Server srv;
    
    
    public MCPUCore(HashMap<String, Tuple<String, String>> usageoptions)
    {
        if (usageoptions == null)
            usageoptions = new HashMap<>();

        usageoptions.put("add", new Tuple<>("<x> <y> <z> <w> <a> [io]", "Adds a new component to the world 'w' at the given coordinates 'x|y|z' with the type 'a' and io pin count 'io' (default = 3x3 pins)"));
        usageoptions.put("addp", new Tuple<>("<x> <y> <z> <w> <a> [io]", "Adds a new processor to the world 'w' at the given coordinates 'x|y|z' with the architecture 'a' and io pin count 'io' (default = 3x3 pins)"));
        usageoptions.put("add", new Tuple<>("<a> [io]", "Adds a new component with the type 'a' and io pin count 'io' to the world at the callers position"));
        usageoptions.put("addp", new Tuple<>("<a> [io]", "Adds a new processor with the architecture 'a' and io pin count 'io' to the world at the callers position"));
        usageoptions.put("remove", new Tuple<>("<n>", "Removes the component No. n"));
        usageoptions.put("loadb", new Tuple<>("<n>", "Loads the book hold in the hand into the processor No. n"));
        usageoptions.put("loadu", new Tuple<>("<n> <u>", "Loads the string acessible via the given URI u into the processor No. n"));
        usageoptions.put("start", new Tuple<>("<n>", "Starts the processor core No. n"));
        usageoptions.put("stop", new Tuple<>("<n>", "Halts the processor core No. n"));
        usageoptions.put("next", new Tuple<>("<n>", "Forces the execution of the next instruction of processor core No. n"));
        usageoptions.put("reset", new Tuple<>("<n>", "Halts and resets the processor core No. n"));
        usageoptions.put("state", new Tuple<>("<n>", "Displays the state of component No. n"));
        usageoptions.put("list", new Tuple<>("", "Lists all MCPU components in the current world"));
        usageoptions.put("arch[itectures]", new Tuple<>("", "Lists all available processor architectures and component types"));
        
        List<String> cmds = new ArrayList<String>(usageoptions.keySet());
        StringBuilder sb = new StringBuilder();
        Collections.sort(cmds);
        
        sb.append(ChatColor.GOLD).append(ChatColor.UNDERLINE).append("/mcpu").append(ChatColor.WHITE).append("command usage:\n");
        
        for (String cmd : cmds)
        {
            Tuple<String, String> nfo = usageoptions.get(cmd);
            
            sb.append(ChatColor.GOLD).append(cmd.trim()).append(ChatColor.GRAY).append(' ').append(nfo.x).append(ChatColor.WHITE).append(" - ").append(nfo.y).append('\n');
        }
        
        usagetext = sb.toString();
    }
    
    @Override
    public final void onEnable()
    {
        srv = getServer();
        log = getLogger();
        
        try
        {
            ComponentFactory.registerFactory("7seg", new SevenSegmentDisplayFactory());
            ComponentFactory.registerFactory("and", new BinaryLogicGateFactory((x, y) -> x & y, "and"));
            ComponentFactory.registerFactory("nand", new BinaryLogicGateFactory((x, y) -> ~(x & y), "nand"));
            ComponentFactory.registerFactory("or", new BinaryLogicGateFactory((x, y) -> x | y, "or"));
            ComponentFactory.registerFactory("nor", new BinaryLogicGateFactory((x, y) -> ~(x | y), "nor"));
            ComponentFactory.registerFactory("xor", new BinaryLogicGateFactory((x, y) -> x ^ y, "xor"));
            ComponentFactory.registerFactory("nxor", new BinaryLogicGateFactory((x, y) -> ~(x ^ y), "nxor"));
        }
        catch (Exception e)
        {
        }
        
        registerIntegratedCircuits();
        
        Print(ChatColor.WHITE, "Registered architectures/components (" + ComponentFactory.getRegisteredFactories().size() + "):\n\t " +
                               String.join(", ", ComponentFactory.getRegisteredFactories()));
        
        srv.getPluginManager().registerEvents(this, this);
        srv.getScheduler().scheduleSyncRepeatingTask(this, this::onTick, 1, 1);
    }
    
    @Override
    public void onDisable()
    {
        for (IntegratedCircuit ic : circuits.values())
            if (ic instanceof EmulatedProcessor)
                ((EmulatedProcessor)ic).stop();
    }
    
    public final void onTick()
    {
        circuits.values().forEach(c ->
        {
            if (c == null)
                return;
            
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
        
        // REMOVE ZOMBIES
        for (int i : circuits.keySet().toArray(new Integer[0]))
            if (circuits.get(i) == null)
                circuits.remove(i);
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
    
    public List<String> onTabComplete(CommandSender sender, Command cmd, String[] args)
    {
        if (cmd.getName().equalsIgnoreCase("mcpu"))
        {
            LinkedList<String> list = new LinkedList<>();
            
            if (args.length == 0)
                list.addAll(MCPUCoreCommand.getAllCommands());
            else if (args.length == 1)
                switch (MCPUCoreCommand.getByValue(args[0]))
                {
                    case ADD:
                    case ADD_PROCESSOR:
                    {
                        list.addAll(ComponentFactory.getRegisteredFactories());
                        
                        break;
                    }
                    case NEXT:
                    case RESET:
                    case START:
                    case STOP:
                    case LOAD_URI:
                    case LOAD_BOOK:
                    {
                        for (int id : circuits.keySet())
                            if (circuits.get(id).isEmulatedProcessor())
                                list.add("" + id);
                        
                        break;
                    }
                    case STATE:
                    case DELETE:
                    {
                        for (int id : circuits.keySet())
                            list.add("" + id);
                        
                        break;
                    }
                    default:
                        return null;
                }

            return list;
        }
        else
            return null;
    }
    
    public abstract void registerIntegratedCircuits();
    
    public abstract boolean onUnprocessedCommand(CommandSender sender, Command command, String label, String[] args);
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (command.getName().equalsIgnoreCase("mcpu"))
        {
            if (args.length == 0)
                args = new String[] { "?" };
            
            switch (MCPUCoreCommand.getByValue(args[0]))
            {
                case HELP:
                    Print(sender, ChatColor.YELLOW, usagetext);
                    break;
                case ADD:
                case ADD_PROCESSOR:
                    boolean isproc = args[0].toLowerCase().contains("p");
                    String[] tmp = new String[args.length - 1];
                    
                    // skip first element
                    for (int i = 1; i < args.length; ++i)
                        tmp[i - 1] = args[i];
                    
                    addComponent(sender, tmp, isproc);
                    
                    break;
                case DELETE:
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
                case RESET:
                    getProcessor(args, 1, sender, proc -> proc.reset());
                    break;
                case STOP:
                    getProcessor(args, 1, sender, proc -> proc.stop());
                    break;
                case START:
                    getProcessor(args, 1, sender, proc -> proc.start());
                    break;
                case NEXT:
                    getProcessor(args, 1, sender, proc -> proc.onTick());
                    break;
                case LOAD_BOOK:
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
                case LOAD_URI:
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
                case ARCH:
                    Print(sender, ChatColor.WHITE, String.join(", ", ComponentFactory.getRegisteredFactories()));
                    
                    break;
                case STATE:
                    getIC(args, 1, sender, c -> sender.sendMessage("(" + c.getClass().getSimpleName() + ") " + c.getState()));
                    break;
                case LIST:
                    circuits.keySet().forEach((i) ->
                    {
                        IntegratedCircuit ic = circuits.get(i);
                        
                        sender.sendMessage("[" + i + "]: (" + ic.getClass().getSimpleName() + ") " + ic.getState());
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
    
    private void addComponent(CommandSender sender, String[] args, boolean isproc)
    {
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
        
        BlockPlacingContext context = new BlockPlacingContext(loc.getWorld());
        
        try
        {
            ComponentFactory<IntegratedCircuit> fac = ComponentFactory.getFactoryByName((isproc ? "processor.emulated." : "") + icname);
            Triplet<Integer, Integer, Integer> size = fac.getEstimatedSize();
            
            for (int i : circuits.keySet())
            {
                IntegratedCircuit ic = circuits.get(i);
                
                if (size != null)
                {
                    if ((abs(ic.x - x) * 2 < (ic.xsize + size.x)) &&
                        (abs(ic.y - y) * 2 < (ic.ysize + size.y)) &&
                        (abs(ic.z - z) * 2 < (ic.zsize + size.z)))
                    {
                        Error(sender, "The new processor/circuit cannot be placed here. It would intersect the existing component no. " + i + ".");
                        
                        return;
                    }
                }
                else if ((x < ic.x + ic.xsize) && (y < ic.y + ic.ysize))
                {
                    double dist = Math.sqrt(ic.xsize * ic.xsize + ic.ysize * ic.ysize + ic.zsize * ic.zsize) + 2;
                    
                    if (Math.abs(ic.y - y) < ic.ysize)
                        if (Math.sqrt(Math.pow(ic.x - x, 2) + Math.pow(ic.y - y, 2) + Math.pow(ic.z - z, 2)) < dist)
                        {
                            Error(sender, "The new processor/circuit cannot be placed here. It would be to close to the existing component no. " + i + ".");
                            
                            return;
                        }
                }
            }
            
            IntegratedCircuit ic = fac.spawnComponent(context, this, player, x, y, z, ComponentOrientation.NORTH, cpusize);
            int num = circuits.size();
            
            while (circuits.containsKey(num))
                ++num;
            
            final int cnum = num; // java 8 being bitchy
            
            if (ic == null)
            {
                context.rollback();
                
                Error(sender, "A fucking critical error occured. This should not even be happening. Save your lives while you still can.....");
            }
            else if (ic instanceof EmulatedProcessor)
                ((EmulatedProcessor)ic).onError = (p, s) -> Print(sender, ChatColor.YELLOW, "Processor " + cnum + " failed with the folling message:\n" + s);
            
            circuits.put(cnum, ic);
            
            Print(sender, ChatColor.GREEN, "The component No. " + cnum + " has been created.");
        }
        catch (InvalidOrientationException o)
        {
            context.rollback();
            
            Error(sender, o.getMessage());
        }
        catch (Exception e)
        {
            context.rollback();
            
            Error(sender, "The new component/circuit could not be created. The architecture or type '" + icname + "' is unknown.");
            
            if (!isproc)
                Error(sender, "If the component is an emulated processor, try adding the processor with the command '/mcpu addp " + icname + "'.");
        }
    }
    
    protected boolean CompileLoad(CommandSender sender, EmulatedProcessor core, URI source)
    {
        try
        {
            boolean result = core.load(source);
            
            if (result)
                Print(sender, ChatColor.GREEN, "The code was successfully loaded into the core.");
            else
                Error(sender, "The code could not be loaded into the core.");
            
            return result;
        }
        catch (Exception e)
        {
            Error(sender, "A critical error occured.");
            
            e.printStackTrace();
            
            return false;
        }
    }
    
    private static String[] GetBook(ItemStack stack)
    {
        if (stack != null)
            if (stack.getAmount() > 0)
                if (((stack.getType() == Material.BOOK_AND_QUILL) || (stack.getType() == Material.WRITTEN_BOOK)) && stack.hasItemMeta())
                {
                    BookMeta bm = (BookMeta)stack.getItemMeta();
                    String[] lines = new String[0];
                    
                    lines = bm.getPages().toArray(lines);

                    for (int i = 0; i < lines.length; ++i)
                        lines[i] = ChatColor.stripColor(lines[i]);
                    
                    System.out.println(String.join(" / ", lines));
                    
                    return lines;
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
            if (ic.isEmulatedProcessor())
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
            catch (NumberFormatException e)
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
        BlockPlacingContext context = new BlockPlacingContext(w);
        
        for (int i = 0; i < xs; ++i)
            for (int j = 0; j < zs; ++j)
                for (int k = ys - 1; k >= 0; --k)
                    context.addBlock(x + i, y + k, z + j, Material.AIR);
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
