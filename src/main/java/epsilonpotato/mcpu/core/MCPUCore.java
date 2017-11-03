
package epsilonpotato.mcpu.core;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;

import epsilonpotato.mcpu.core.components.factories.*;
import epsilonpotato.mcpu.util.*;
import net.minecraft.server.v1_12_R1.Items;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static java.lang.Math.*;


/**
 * The plugin's core class. All actual bukkit plugins using the MCPU framework must derive from this class
 * @author Unknown6656
 */
public abstract class MCPUCore extends JavaPlugin implements Listener, TabCompleter
{
    private static final String registerWandTag = "______mcpu_integrated_component_register_tool";
    private static final String circuitFileName = "circuits.yml";
    private static String usagetext;
    /**
     * A hash map of all registered ICs. The key is a unique integer number (optimally starting at zero and incremented by one every time a component is added). 
     */
    public static final HashMap<Integer, IntegratedCircuit> circuits = new HashMap<>();
    /**
     * The console logger instance
     */
    public static Logger log;
    /**
     * The current server instance
     */
    public static Server srv;
    
    
    /**
     * Abstract method which registers more integrated circuits using the method {@see epsilonpotato.mcpu.core.ComponentFactory#registerFactory(String, epsilonpotato.mcpu.core.ComponentFactory)}.
     */
    public abstract void registerIntegratedCircuits();
    
    /**
     * Abstract event handler which is raised if a given command is unhandled
     * @param sender The command sender
     * @param command The command
     * @param label Command label (?)
     * @param args Command arguments
     * @return Boolean value which indicates whether the command could be handled or not
     */
    public abstract boolean onUnprocessedCommand(CommandSender sender, Command command, String label, String[] args);
    
    
    /**
     * Creates a new instance and passes a hash map of additional usage options and commands (can be null)
     * @param usageoptions Additional usage options which will be displayed when typing a help-command. The key is the first command argument. The tuple is composed of the argument syntax and a short description
     */
    public MCPUCore(HashMap<String, Tuple<String, String>> usageoptions)
    {
        if (usageoptions == null)
            usageoptions = new HashMap<>();

        usageoptions.put("add", new Tuple<>("<x> <y> <z> <w> <a> [io]", "Adds a new component to the world 'w' at the given coordinates 'x|y|z' with the type 'a' and io pin count 'io' (default = 3x3 pins)"));
        usageoptions.put("addp", new Tuple<>("<x> <y> <z> <w> <a> [io]", "Adds a new processor to the world 'w' at the given coordinates 'x|y|z' with the architecture 'a' and io pin count 'io' (default = 3x3 pins)"));
        usageoptions.put("add", new Tuple<>("<a> [io]", "Adds a new component with the type 'a' and io pin count 'io' to the world at the callers position"));
        usageoptions.put("addp", new Tuple<>("<a> [io]", "Adds a new processor with the architecture 'a' and io pin count 'io' to the world at the callers position"));
        usageoptions.put("remove", new Tuple<>("<n>", "Removes the component No. n"));
        usageoptions.put("unregister", new Tuple<>("<n>", "Unregisters the component No. n without removing it from the world."));
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

        try
        {
            ComponentFactory.registerFactory("smoke", new SmokeGeneratorFactory());
            ComponentFactory.registerFactory("7seg", new SevenSegmentDisplayFactory());
            ComponentFactory.registerFactory("mux8", new Multiplexer1to8Factory());
            ComponentFactory.registerFactory("demux8", new Demultiplexer1to8Factory());
            ComponentFactory.registerFactory("disp16", new WoolDisplay16x16Factory());
            ComponentFactory.registerFactory("disp32", new WoolDisplay32x32Factory());
            ComponentFactory.registerFactory("and", new BinaryLogicGateFactory((x, y) -> x & y, "and"));
            ComponentFactory.registerFactory("nand", new BinaryLogicGateFactory((x, y) -> ~(x & y), "nand"));
            ComponentFactory.registerFactory("or", new BinaryLogicGateFactory((x, y) -> x | y, "or"));
            ComponentFactory.registerFactory("nor", new BinaryLogicGateFactory((x, y) -> ~(x | y), "nor"));
            ComponentFactory.registerFactory("xor", new BinaryLogicGateFactory((x, y) -> x ^ y, "xor"));
            ComponentFactory.registerFactory("nxor", new BinaryLogicGateFactory((x, y) -> ~(x ^ y), "nxor"));
            ComponentFactory.registerFactory("hadd", new LogicGate2x2Factory((g, s) ->
            {
                g[2] = g[0] ^ g[1];
                g[3] = g[0] & g[1];
            }, "hadd"));
            ComponentFactory.registerFactory("tflipflop", new LogicGate2x2Factory((g, s) ->
            {
                if (g[0] != 0)
                    s[0] = (byte)(s[0] == 0 ? 1 : 2); // set only on rising flank
                else
                    s[0] = 0;
                
                if (s[0] == 1) // if rising flank
                    s[1] = (byte)(s[1] != 0 ? 0 : 1);
                
                if (g[1] != 0)
                    s[1] = 0;
                
                g[2] = s[1];
                g[3] = g[2] != 0 ? 0 : 1;
            }, "tflipflop"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        registerIntegratedCircuits();
    }
    
    /**
     * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
     */
    @Override
    public final void onEnable()
    {
        srv = getServer();
        log = getLogger();
        
        print(ChatColor.WHITE, "Registered architectures/components (" + ComponentFactory.getRegisteredFactories().size() + "):\n\t " +
                               String.join(", ", ComponentFactory.getRegisteredFactories()));
        
        srv.getPluginManager().registerEvents(this, this);
        srv.getScheduler().scheduleSyncRepeatingTask(this, this::onTick, 1, 1);

        getCommand("mcpu").setTabCompleter(this);
        getDataFolder().mkdirs();
        
        onWorldLoadEvent(null);
    }
    
    /**
     * @see org.bukkit.plugin.java.JavaPlugin#onDisable()
     */
    @Override
    public void onDisable()
    {
        for (IntegratedCircuit ic : circuits.values())
            if (ic instanceof EmulatedProcessor)
                ((EmulatedProcessor)ic).stop();
        
        onWorldSaveEvent(null);
    }
    
    /**
     * A function executed on each tick designed to update all component's I/O ports and perform each component's '{@link IntegratedCircuit#onTick()}' method.
     */
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
    
    /**
     * Event handler which is being called if a block has been placed in the world. The handler verifies that the block placed does not interfere with integrated circuits.
     * @param event Event data
     */
    @EventHandler
    public final void onBlockPlaceEvent(BlockPlaceEvent event)
    {
        Location l = event.getBlock().getLocation();
        
        for (int i : circuits.keySet())
        {
            IntegratedCircuit ic = circuits.get(i);

            if (ic.testCollision(l))
            {
                event.setCancelled(true);
                
                if (event.getPlayer() != null)
                    event.getPlayer().sendMessage(ChatColor.RED + "You cannot place a block on the integrated circuit no. " + i + ".");
            }
        }
    }
    
    /**
     * Event handler which is being called if a block has been destroyed. The handler verifies that the block is not a part of any registered IC.
     * @param event Event data
     */
    @EventHandler
    public final void onBlockBreakEvent(BlockBreakEvent event)
    {
        Location l = event.getBlock().getLocation();

        for (int i : circuits.keySet())
        {
            IntegratedCircuit ic = circuits.get(i);

            if (ic.testCollision(l))
            {
                event.setCancelled(true);
                
                if (event.getPlayer() != null)
                    event.getPlayer().sendMessage(ChatColor.RED + "You cannot destroy a block from the integrated circuit no. " + i + ".");
            }
        }
    }

    /**
     * Event handler which is being called if the world is saved. 
     * @param event Event data
     */
    @EventHandler
    public final void onWorldSaveEvent(WorldSaveEvent event)
    {
        try
        {
            byte[] data = serialize();
            File target = getCircuitStorageFile();
            FileOutputStream fos = new FileOutputStream(target);
            
            fos.write(data);
            fos.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Event handler which is being called if the world is loaded
     * @param event Event data
     */
    @EventHandler
    public final void onWorldLoadEvent(WorldLoadEvent event)
    {
        try
        {
            File target = getCircuitStorageFile();
            
            if (target.exists())
            {
                FileInputStream fis = new FileInputStream(target);
                byte[] data = new byte[(int)target.length()];
                
                fis.read(data);
                fis.close();

                deserialize(data);
            }   
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Event handler which is being called if the world is initialised.
     * @param event Event data
     */
    @EventHandler
    public final void onWorldInitEvent(WorldInitEvent event)
    {
        onWorldLoadEvent(event == null ? null : new WorldLoadEvent(event.getWorld()));
    }

    private final File getCircuitStorageFile() throws IOException
    {
        File dat = new File(getDataFolder().getPath() + '/' + circuitFileName);
        
        if (!dat.exists())
        {
            dat.getParentFile().mkdirs();
            dat.createNewFile();
        }
        
        return dat;
    }
    
    /**
     * Event handler which handles when a player presses the `TAB`-key in the console halfway through typing a command
     * @param sender The sender (player or console user)
     * @param cmd The command
     * @param args Command arguments
     * @return Command completition list
     */
    @EventHandler
    public final List<String> onTabComplete(CommandSender sender, Command cmd, String[] args)
    {
        print(ChatColor.AQUA, cmd.getName());
        
        for (String s : args)
            print(ChatColor.AQUA, s);
        
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
                    case REGISTER:
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
                    case UNREGISTER:
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
    
    /**
     * Event handler which is being called when a player interacts with a block. The handler processes the usage of the 'register magic wand'
     * @param event Event data
     */
    @EventHandler
    public final void onPlayerInteract(PlayerInteractEvent event)
    {        
        if ((event != null) && (event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK))
        {
            ItemStack is = event.getItem();
            
            if ((is != null) && (is.getType() == Material.STICK))
            {
                HashMap<String, String> tags = ItemNBTHelper.getTags(is);
                
                if (tags.containsKey(registerWandTag) && registerWandTag.equals(tags.get(registerWandTag)))
                {
                    String name = tags.get("name");
                    String[] args = tags.get("args").split("§§");
                    int size = Integer.parseInt(tags.get("size"));
                    UUID pid = UUID.fromString(tags.get("uuid"));
                    
                    registerComponent(getServer().getPlayer(pid), name, args, size);
                }
            }
        }
    }
    
    /**
     * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    @Override
    public final boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (command.getName().equalsIgnoreCase("mcpu"))
        {
            if (args.length == 0)
                args = new String[] { "?" };
            
            MCPUCoreCommand cmd = MCPUCoreCommand.getByValue(args[0]);

            switch (cmd)
            {
                case HELP:
                    print(sender, ChatColor.YELLOW, usagetext);
                    break;
                case ADD:
                case ADD_PROCESSOR:
                case REGISTER:
                case REGISTER_PROCESSOR:
                    String[] tmp = new String[args.length - 1];
                    
                    // skip first element
                    for (int i = 1; i < args.length; ++i)
                        tmp[i - 1] = args[i];
                    
                    if ((cmd == MCPUCoreCommand.REGISTER) || (cmd == MCPUCoreCommand.REGISTER_PROCESSOR))
                        if (sender instanceof Player)
                            giveRegisterWand((Player)sender, tmp, cmd == MCPUCoreCommand.REGISTER_PROCESSOR);
                        else
                            error(sender, "You must be a player to execute this command.");
                    else
                        addComponent(sender, tmp, cmd == MCPUCoreCommand.ADD_PROCESSOR);
                    
                    break;
                case UNREGISTER:
                    getInt(args, 1, sender, i ->
                    {
                        if (circuits.containsKey(i))
                        {
                            circuits.remove(i);
                            
                            print(sender, ChatColor.GREEN, "The component No. " + i + " has been unregistered from the world.");
                        }
                        else
                            error(sender, "The component No. " + i + " could not be found.");
                    });
                    break;
                case DELETE:
                    getInt(args, 1, sender, i ->
                    {
                        if (circuits.containsKey(i))
                        {
                            final IntegratedCircuit c = circuits.remove(i);
                            
                            if (c.assocblocks == null)
                                deleteRegion(c.world, c.x, c.y, c.z, c.xsize, c.ysize, c.zsize);
                            else
                                Parallel.For(0, c.assocblocks.size(), ndx ->
                                {
                                    Triplet<Integer, Integer, Integer> loc = c.assocblocks.get(ndx);
                                    
                                    c.world.getBlockAt(loc.x, loc.y, loc.z).setType(Material.AIR);
                                });
                            
                            print(sender, ChatColor.GREEN, "The component No. " + i + " has been removed from the world.");
                        }
                        else
                            error(sender, "The component No. " + i + " could not be found.");
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
                                String[] book = getBook(stack);
                                
                                if (book == null)
                                    book = getBook(player.getInventory().getItemInOffHand());
                                
                                if (book == null)
                                    error(sender, "You must be holding a writable/readable book in your main hand to load data into the processor.");
                                else
                                {
                                    String b64 = new String(Base64.getEncoder().encode(String.join("\n", book).getBytes()));
                                    
                                    compileLoad(sender, proc, new URI("data:text/plain;base64," + b64));
                                }
                            }
                            catch (URISyntaxException e)
                            {
                            }
                        else
                            error(sender, "You must be a player to run this command.");
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
                                if (!compileLoad(sender, proc, new URI(uri)))
                                    error(sender, "The instructions could not be fetched from '" + uri + "'.");
                            }
                            catch (URISyntaxException ex)
                            {
                                error(sender, "The instructions could not be fetched from '" + uri + "'.");
                            }
                    });
                    break;
                case ARCH:
                    print(sender, ChatColor.WHITE, String.join(", ", ComponentFactory.getRegisteredFactories()));
                    
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
                        error(sender, "The command '" + args[0].trim() + "' is unknown.");
            }
            
            return true;
        }
        else
            return false;
    }
    
    private void giveRegisterWand(Player sender, String[] args, boolean isproc)
    {
        int cpusize = 0;
        String icname;
        
        try
        {
            icname = args[0];
            
            if (args.length > 1)
                cpusize = Integer.parseInt(args[1]);
        }
        catch (Exception e)
        {
            error(sender, "You must provide valid architecture name (and io pin size) for the creation of a new component/processor.");
            
            return;
        }

        icname = (isproc ? "processor.emulated." : "") + icname;
        
        try
        {
            ComponentFactory.getFactoryByName(icname);
            HashMap<String, String> tags = new HashMap<>();

            tags.put("name", icname);
            tags.put("size", "" + cpusize);
            tags.put("args", String.join("§§", args));
            tags.put("uuid", ((Player)sender).getUniqueId().toString());
            tags.put(registerWandTag, registerWandTag);
            
            String[] lore = new String[] {
                ChatColor.LIGHT_PURPLE + "Right-click with this magic wand on the",
                ChatColor.LIGHT_PURPLE + "north-west corner of a structure to",
                ChatColor.LIGHT_PURPLE + "register it as a '" + ChatColor.AQUA + icname + ChatColor.LIGHT_PURPLE + "'-component.",
                ChatColor.LIGHT_PURPLE + "The wand will (more or less) behave as",
                ChatColor.LIGHT_PURPLE + "if you would have stood in the clicked",
                ChatColor.LIGHT_PURPLE + "block and used the '" + ChatColor.GOLD + "/mcpu add " + ChatColor.ITALIC + icname + ChatColor.RESET + ChatColor.LIGHT_PURPLE + "'",
                ChatColor.LIGHT_PURPLE + "command."
            };

            ItemNBTHelper.addItem(Items.STICK, 1, tags, lore, "Magic wand to register a " + icname + "-circuit", sender);

            print(sender, ChatColor.GREEN, "Use the given wand to left-click on the corner or spawn block of an existing circuit structure in order to register it.\n" +
                                           ChatColor.YELLOW + "Be VERY careful with it! It is extremly buggy at the moment.\n" +
                                           ChatColor.RED + "I said: be CAREFUL !!");
        }
        catch (ClassNotFoundException e)
        {
            error(sender, "The component or architecture '" + icname + "' could not be found.");
            
            return;
        }
    }

    private void registerComponent(Player p, String name, String[] args, int cpusize)
    {
        try
        {
            Block corner = p.getTargetBlock(null, 15);
            Location loc = corner.getLocation();
            int x = loc.getBlockX(), y = loc.getBlockY(), z = loc.getBlockZ();
            ComponentOrientation or = ComponentOrientation.NORTH; // <---- TODO : change orientation depending on the player's orientation

            ComponentFactory<IntegratedCircuit> fac;
            fac = ComponentFactory.getFactoryByName(name);

            int i = checkForCollisions(fac.getEstimatedSize(or), x, y, z);
            
            if (i != -1)
                error(p, "The new processor/circuit cannot be placed here. It would intersect the existing component no. " + i + ".");
            else
            {
                BlockPlacingContext context = new BlockPlacingContext(loc.getWorld());
                
                try
                {
                    spawnComponent(context, fac, p, x, y, z, or, cpusize);
                }
                catch (InvalidOrientationException e)
                {
                    context.rollback();
                }
            }
        }
        catch (ClassNotFoundException e)
        {
            // TODO do smth.
            e.printStackTrace();
        }
    }
    
    private void addComponent(CommandSender sender, String[] args, boolean isproc)
    {
        Player player = null;
        int cpusize = 0;
        String icname;
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
                error(sender, "You must provide valid architecture name (and io pin size) for the creation of a new component/processor.");
                
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
                error(sender, "You must provide valid x-, y-, z-corrdinates, a valid world name, architecture name (and io pin size) for the creation of a new component/processor.");
                
                return;
            }
        
        x = loc.getBlockX();
        y = loc.getBlockY();
        z = loc.getBlockZ();
        cpusize = max(3, min(cpusize, 16));
        
        BlockPlacingContext context = new BlockPlacingContext(loc.getWorld());
        
        try
        {
            ComponentOrientation orient = ComponentOrientation.NORTH; // <---- TODO : change orientation depending on the player's orientation
            
            ComponentFactory<IntegratedCircuit> fac = ComponentFactory.getFactoryByName((isproc ? "processor.emulated." : "") + icname);
            Triplet<Integer, Integer, Integer> size = fac.getEstimatedSize(orient);
            int i = checkForCollisions(size, x, y, z);
            
            if (i != -1)
                error(sender, "The new processor/circuit cannot be placed here. It would intersect the existing component no. " + i + ".");
            else
                spawnComponent(context, fac, player, x, y, z, orient, cpusize);
        }
        catch (InvalidOrientationException o)
        {
            context.rollback();
            
            error(sender, o.getMessage());
        }
        catch (Exception e)
        {
            context.rollback();
            
            error(sender, "The new component/circuit could not be created. The architecture or type '" + icname + "' is unknown.");
            
            if (!isproc)
                error(sender, "If the component is an emulated processor, try adding the processor with the command '/mcpu addp " + icname + "'.");
        }
    }
    
    private int spawnComponent(BlockPlacingContext context, ComponentFactory<IntegratedCircuit> fac, Player player, int x, int y, int z, ComponentOrientation orient, int cpusize)
            throws InvalidOrientationException
    {
        IntegratedCircuit ic = fac.spawnComponent(context, this, player, x, y, z, orient, cpusize);
        int num = circuits.size();
        
        while (circuits.containsKey(num))
            ++num;
        
        final int fnum = num;
        
        if (ic == null)
        {
            context.rollback();
            
            // some serious shit should be fixed if we ever arrive in this line ..... 
            error(player, "A fucking critical error occured. This should not even be happening. Save your lives while you still can.....");
        }
        else if (ic instanceof EmulatedProcessor)
            ((EmulatedProcessor)ic).onError = (p, s) -> print(player, ChatColor.YELLOW, "Processor " + fnum + " failed with the folling message:\n" + s);
        
        circuits.put(fnum, ic);

        ic.setAssociatedBlocks(context.getBlocks());
        
        print(player, ChatColor.GREEN, "The component No. " + fnum + " has been created.");
        
        return fnum;
    }
    
    private int checkForCollisions(Triplet<Integer, Integer, Integer> size, int x, int y, int z)
    {
        for (int i : circuits.keySet())
        {
            IntegratedCircuit ic = circuits.get(i);
            
            if (size != null)
            {
                if ((abs(ic.x - x) * 2 < (ic.xsize + size.x)) &&
                    (abs(ic.y - y) * 2 < (ic.ysize + size.y)) &&
                    (abs(ic.z - z) * 2 < (ic.zsize + size.z)))
                    return i;
            }
            else if ((x < ic.x + ic.xsize) && (y < ic.y + ic.ysize))
            {
                double dist = Math.sqrt(ic.xsize * ic.xsize + ic.ysize * ic.ysize + ic.zsize * ic.zsize) + 2;
                
                if (Math.abs(ic.y - y) < ic.ysize)
                    if (Math.sqrt(Math.pow(ic.x - x, 2) + Math.pow(ic.y - y, 2) + Math.pow(ic.z - z, 2)) < dist)
                        return i;
            }
        }

        return -1;
    }
    
    /**
     * @param sender
     * @param core
     * @param source
     * @return
     */
    protected final boolean compileLoad(CommandSender sender, EmulatedProcessor core, URI source)
    {
        try
        {
            boolean result = core.load(source);
            
            if (result)
                print(sender, ChatColor.GREEN, "The code was successfully loaded into the core.");
            else
                error(sender, "The code could not be loaded into the core.");
            
            return result;
        }
        catch (Exception e)
        {
            error(sender, "A critical error occured.");
            
            e.printStackTrace();
            
            return false;
        }
    }

    /**
     * @return
     * @throws IOException
     */
    public final void serialize(YamlConfiguration conf)
    {
        conf.clear();
        
        YamlConfiguration ics = conf.getOrCreateSection("circuits");
        int num = 0;
        
        for (int index : circuits.keySet())
        {
            IntegratedCircuit ic = circuits.get(index); 
            
            if (ic != null)
            {                
                ic.serialize(ics.getOrCreateSection("ic_" + num));

                ++num;
            }
        }
            
        conf.set("circuit_count", num);
    }

    /**
     * @param data
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public final void deserialize(YamlConfiguration conf)
    {
        int cnt = conf.getInt("circuit_count", 0);
        YamlConfiguration ics = conf.getOrCreateSection("circuits");
        
        circuits.clear();
        
        for (int index = 0; index < cnt; ++index);
            if (ics.containsKey("ic_" + index))
            {
                YamlConfiguration icmap = ics.getOrCreateSection("ic_" + index);
                IntegratedCircuit ic = null; // black magic again
                
                ic.deserialize(icmap);
                
                circuits.put(index, ic);
            }
    }

    private void getIC(final String[] argv, final int ndx, final CommandSender sender, Action<IntegratedCircuit> action)
    {
        getInt(argv, ndx, sender, i ->
        {
            if (circuits.containsKey(i))
                action.eval(circuits.get(i));
            else
                sender.sendMessage(ChatColor.RED + "The component No. " + i + " could not be found.");
        });
    }
    
    private void getProcessor(final String[] argv, final int ndx, final CommandSender sender, Action<EmulatedProcessor> action)
    {
        getIC(argv, ndx, sender, ic ->
        {
            if (ic.isEmulatedProcessor())
                action.eval((EmulatedProcessor)ic);
            else
                error(sender, "The component in question must be an emulated processor to perform the current action.");
        });
    }
    
    private static String[] getBook(ItemStack stack)
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
                    
                    return lines;
                }
            
        return null;
    }
    
    private static void getInt(final String[] argv, final int ndx, final CommandSender sender, Action<Integer> action)
    {
        String arg = getArg(argv, ndx, sender);
        
        if (arg != null)
            try
            {
                action.eval(Integer.parseInt(arg));
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

    public static void print(ChatColor c, String m)
    {
        print(null, c, m);
    }
    
    public static void print(CommandSender s, ChatColor c, String m)
    {
        if (s != null)
            s.sendMessage(c + m);
        
        if ((s == null) || (s instanceof Player))
            log.log(Level.INFO, ChatColor.stripColor(m));
    }
    
    public static void error(CommandSender s, String m)
    {
        print(s, ChatColor.RED, m);
    }
}
