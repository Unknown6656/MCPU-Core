
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
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;

import epsilonpotato.mcpu.core.components.BinaryLogicGateType;
import epsilonpotato.mcpu.core.components.LogicGate2x2Type;
import epsilonpotato.mcpu.core.components.factories.*;
import epsilonpotato.mcpu.util.*;
import net.minecraft.server.v1_12_R1.Items;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

import static java.lang.Math.*;


/**
 * The plugin's core class. All actual bukkit plugins using the MCPU framework
 * must derive from this class
 * 
 * @author Unknown6656
 */
public abstract class MCPUCore extends JavaPlugin implements Listener, TabCompleter
{
    private static final String registerWandTag = "______mcpu_integrated_component_register_tool";
    private static final String circuitFileName = "circuits.yml";
    private final HashMap<String, Tuple<String, String>> usageoptions;
    /**
     * A hash map of all registered ICs. The key is a unique integer number
     * (optimally starting at zero and incremented by one every time a component
     * is added).
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
     * Returns the 'about'- or version-information of the plugin
     * @return About/version text
     */
    public abstract String getAboutText();
    
    /**
     * Abstract method which registers more integrated circuits using the method
     * {@link epsilonpotato.mcpu.core.ComponentFactory#registerFactory(String, epsilonpotato.mcpu.core.ComponentFactory)}.
     */
    public abstract void registerIntegratedCircuits();
    
    /**
     * Abstract event handler which is raised if a given command is unhandled
     * 
     * @param sender The command sender
     * @param command The command
     * @param label Command label (?)
     * @param args Command arguments
     * @return Boolean value which indicates whether the command could be
     * handled or not
     */
    public abstract boolean onUnprocessedCommand(CommandSender sender, Command command, String label, String[] args);
    
    
    /**
     * Creates a new instance and passes a hash map of additional usage options
     * and commands (can be null)
     * 
     * @param usageoptions Additional usage options which will be displayed when
     * typing a help-command. The key is the first command argument. The tuple
     * is composed of the argument syntax and a short description
     */
    public MCPUCore(HashMap<String, Tuple<String, String>> usageoptions)
    {
        if (usageoptions == null)
            usageoptions = new HashMap<>();
        
        Reader rd = getTextResource("usageoptions.yml");
        YamlConfiguration conf = YamlConfiguration.read(rd).getOrCreateSection("options");
        int num = 0;
        
        while (conf.containsKey("option_" + num))
        {
            YamlConfiguration confOption = conf.getOrCreateSection("option_" + num);
            String key = confOption.getString("command", "").toLowerCase().trim();

            if (key.length() > 0)
            {
                String args = confOption.getString("arguments", "");
                String desc = confOption.getString("longdescr", "");    
                
                usageoptions.put(key, new Tuple<>(args, desc));
                
                ++num;
            }
            else
                break;
        }
        
        try
        {
            rd.close();
        }
        catch (IOException e)
        {
        }
        
        this.usageoptions = usageoptions;
        
        try
        {
            ComponentFactory.registerFactory("smoke", new SmokeGeneratorFactory());
            ComponentFactory.registerFactory("7seg", new SevenSegmentDisplayFactory());
            ComponentFactory.registerFactory("mux8", new Multiplexer1to8Factory());
            ComponentFactory.registerFactory("demux8", new Demultiplexer1to8Factory());
            ComponentFactory.registerFactory("disp16", new WoolDisplay16x16Factory());
            ComponentFactory.registerFactory("disp32", new WoolDisplay32x32Factory());
            ComponentFactory.registerFactory("adc", new AnalogDigitalConverterFactory());
            ComponentFactory.registerFactory("and", new BinaryLogicGateFactory(BinaryLogicGateType.AND));
            ComponentFactory.registerFactory("nand", new BinaryLogicGateFactory(BinaryLogicGateType.NAND));
            ComponentFactory.registerFactory("or", new BinaryLogicGateFactory(BinaryLogicGateType.OR));
            ComponentFactory.registerFactory("nor", new BinaryLogicGateFactory(BinaryLogicGateType.NOR));
            ComponentFactory.registerFactory("xor", new BinaryLogicGateFactory(BinaryLogicGateType.XOR));
            ComponentFactory.registerFactory("nxor", new BinaryLogicGateFactory(BinaryLogicGateType.NXOR));
            ComponentFactory.registerFactory("hadd", new LogicGate2x2Factory(LogicGate2x2Type.HALF_ADDER));
            ComponentFactory.registerFactory("tflipflop", new LogicGate2x2Factory(LogicGate2x2Type.T_FLIP_FLOP));
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
     * A function executed on each tick designed to update all component's I/O
     * ports and perform each component's '{@link IntegratedCircuit#onTick()}'
     * method.
     */
    public final void onTick()
    {
        circuits.values().forEach(c ->
        {
            if (c == null)
                return;
            else if (!c.isCompletelyLoaded())
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
     * Event handler which is being called if a block has been placed in the
     * world. The handler verifies that the block placed does not interfere with
     * integrated circuits.
     * 
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
     * Event handler which is being called if a block has been destroyed. The
     * handler verifies that the block is not a part of any registered IC.
     * 
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
     * 
     * @param event Event data
     */
    @EventHandler
    public final void onWorldSaveEvent(WorldSaveEvent event)
    {
        try
        {
            YamlConfiguration conf = YamlConfiguration.emptyConfiguration();
            
            serialize(conf);
            
            conf.save(getCircuitStorageFile());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * Event handler which is being called if the world is loaded
     * 
     * @param event Event data
     */
    @EventHandler
    public final void onWorldLoadEvent(WorldLoadEvent event)
    {
        try
        {
            File target = getCircuitStorageFile();
            
            if (target.exists())
                deserialize(YamlConfiguration.read(target));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * Event handler which is being called if the world is initialised.
     * 
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
     * Event handler which handles when a player presses the `TAB`-key in the
     * console halfway through typing a command
     * 
     * @param event Event data
     */
    @EventHandler
    public final void onTabCompleteEvent(TabCompleteEvent event)
    {
        String[] tokens = event.getBuffer().split("\\s+");
        
        List<String> compl = onTabComplete(event.getSender(), tokens);
        
        compl = compl == null ? new LinkedList<>() : compl;
        compl.addAll(event.getCompletions());
        
        event.setCompletions(compl);
    }
    
    /**
     * Event handler which handles when a player presses the `TAB`-key in the
     * console halfway through typing a command
     * 
     * @param sender The sender (player or console user)
     * @param cmd The command
     * @param args Command arguments
     * @return Command completition list
     */
    public final List<String> onTabComplete(CommandSender sender, String[] args)
    {
        if (args[0].equalsIgnoreCase("/mcpu"))
        {
            LinkedList<String> list = new LinkedList<>();
            
            if (args.length == 1)
                list.addAll(MCPUCoreCommand.getAllCommands());
            else if (args.length == 2)
                switch (MCPUCoreCommand.getByValue(args[1]))
                {
                    case ADD:
                    case ADD_PROCESSOR:
                    case REGISTER:
                    case REGISTER_PROCESSOR:
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
                    case ABOUT:
                    case ARCH:
                    case HELP:
                    case LIST:
                        return null;
                    case UNKNOWN:
                    default:
                        return null; // onTabComplete(sender, new String[] { args[0] }); // (?)
                }
            
            return list;
        }
        else
            return null;
    }
    
    /**
     * Event handler which is being called when a player interacts with a block.
     * The handler processes the usage of the 'register magic wand'
     * 
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
     * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender,
     * org.bukkit.command.Command, java.lang.String, java.lang.String[])
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
                {
                    List<String> cmds = new ArrayList<String>(usageoptions.keySet());
                    StringBuilder sb = new StringBuilder();
                    
                    if (args.length > 1)
                    {
                        String cmnd = args[1].toLowerCase().trim();
                        
                        if (cmds.contains(cmnd))
                        {
                            Tuple<String, String> nfo = usageoptions.get(cmnd);
                            
                            sb.append(ChatColor.GOLD).append(cmnd).append(ChatColor.GRAY).append(' ').append(nfo.x).append(ChatColor.WHITE).append(" - ").append(nfo.y).append('\n');
                        }
                        else
                            sb.append(ChatColor.RED).append("The mcpu subcommand '").append(cmnd).append("' does not exist.");    
                    }
                    else
                    {
                        sb.append(ChatColor.WHITE).append("These ").append(ChatColor.GOLD).append("/mcpu").append(ChatColor.WHITE).append(" commands are available:\n");

                        Collections.sort(cmds);
                        boolean first = true;
                        
                        for (String key : cmds)
                            if (!first)
                                sb.append(ChatColor.WHITE).append(',');
                            else
                            {
                                first = false;
                            
                                sb.append(ChatColor.GOLD).append(key);
                            }

                        sb.append(ChatColor.WHITE).append("\ntype '").append(ChatColor.GOLD).append("/mcpu ? <command>").append(ChatColor.WHITE).append("' for more information about the specific command.");
                    }

                    print(sender, ChatColor.WHITE, sb.toString());
                    
                    break;
                }
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
                            {
                                Parallel.For(0, c.assocblocks.size(), ndx ->
                                {
                                    Triplet<Integer, Integer, Integer> loc = c.assocblocks.get(ndx);
                                    
                                    c.world.getBlockAt(loc.x, loc.y, loc.z).setType(Material.AIR);
                                });
                                Parallel.For(0, c.assocblocks.size(), ndx ->
                                {
                                    Triplet<Integer, Integer, Integer> loc = c.assocblocks.get(ndx);
                                    
                                    // have to do this separately because of missing block updates.
                                    c.world.getBlockAt(loc.x, loc.y, loc.z).getState().update();
                                });
                            }
                            
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
                case ABOUT:
                    StringBuilder sb = new StringBuilder();

                    sb.append(ChatColor.GOLD + "--------------------- ABOUT ---------------------\n");
                    sb.append(ChatColor.AQUA + "  MCPU-Core:\n");
                    sb.append(ChatColor.WHITE + "        Copyright (c) 2017 Unknown6656 and Zedly\n");
                    sb.append(ChatColor.AQUA + "  " + getName() + ":\n");
                    sb.append(ChatColor.WHITE + getAboutText());
                    
                    print(sender, ChatColor.WHITE, sb.toString());
                    
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
        ComponentOrientation or;
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
                or = getDirection(loc);
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
            or = ComponentOrientation.NORTH; // <---- TODO : change orientation depending on the player's orientation
            
            ComponentFactory<IntegratedCircuit> fac = ComponentFactory.getFactoryByName((isproc ? "processor.emulated." : "") + icname);
            Triplet<Integer, Integer, Integer> size = fac.getEstimatedSize(or);
            int i = checkForCollisions(size, x, y, z);
            
            if (i != -1)
                error(sender, "The new processor/circuit cannot be placed here. It would intersect the existing component no. " + i + ".");
            else
                spawnComponent(context, fac, player, x, y, z, or, cpusize);
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
    
    private int spawnComponent(BlockPlacingContext context, ComponentFactory<IntegratedCircuit> fac, Player player, int x, int y, int z, ComponentOrientation orient, int cpusize) throws InvalidOrientationException
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
    
    /**
     * Checks whether an object with the given coordinates and size collides
     * with any registered components and returns the collided component's ID
     * 
     * @param size The object's size (x|y|z)
     * @param x The object's X-coordinate
     * @param y The object's Y-coordinate
     * @param z The object's Z-coordinate
     * @return ID number of the component in question (-1 if no collision
     * occures)
     */
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
     * Compiles the code given by the URI into the processor 'core'
     * 
     * @param sender
     * @param core Emulated target processor
     * @param source Code to be compiled/loaded
     * @return 'true', if the operation was successful - otherwise false
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
     * Serialises all components into the given YAML configuration
     * @param conf YAML configuration
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
                YamlConfiguration confIC = ics.getOrCreateSection("ic_" + num);
                
                confIC.set("type", ic.getClass().getTypeName());
                
                ic.serialize(confIC.getOrCreateSection("yaml"));
                
                ++num;
            }
        }
        
        conf.set("circuit_count", num);
    }

    /**
     * Deserialises the given YAML configuration and places all stored components into the world
     * @param conf YAML configuration
     */
    public final void deserialize(YamlConfiguration conf)
    {
        YamlConfiguration ics = conf.getOrCreateSection("circuits");
        int cnt = conf.getInt("circuit_count", 0);
        
        circuits.clear();
        
        for (int index = 0; index < cnt; ++index)
            if (ics.containsKey("ic_" + index))
                try
                {
                    YamlConfiguration icmap = ics.getOrCreateSection("ic_" + index + ".yaml");
                    String icname = ics.getString("ic_" + index + ".type", null);
                    IntegratedCircuit ic = (IntegratedCircuit)Class.forName(icname).newInstance();
                    
                    ic.deserialize(icmap);
                    
                    circuits.put(index, ic);
                }
                catch (InstantiationException | IllegalAccessException | ClassNotFoundException e)
                {
                    e.printStackTrace();
                }
        
        System.out.println("loaded " + circuits.size() + " components.");
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
    
    /**
     * Deletes the given region from the given world
     * @param w World
     * @param x The region's lowest X-coordinate
     * @param y The region's lowest Y-coordinate
     * @param z The region's lowest Z-coordinate
     * @param xs The region's size in X-direction
     * @param ys The region's size in Y-direction
     * @param zs The region's size in Z-direction
     */
    protected static void deleteRegion(World w, int x, int y, int z, int xs, int ys, int zs)
    {
        BlockPlacingContext context = new BlockPlacingContext(w);
        
        for (int i = 0; i < xs; ++i)
            for (int j = 0; j < zs; ++j)
                for (int k = ys - 1; k >= 0; --k)
                    context.addBlock(x + i, y + k, z + j, Material.AIR);
    }
    
    /**
     * Prints the given message m with the given chat colour c to the server's command line
     * @param c Chat colour
     * @param m Message
     */
    public static void print(ChatColor c, String m)
    {
        print(null, c, m);
    }
    
    /**
     * Prints the given message m with the given chat colour c to the given target
     * @param s Target (command line, player, etc.)
     * @param c Chat colour
     * @param m Message
     */
    public static void print(CommandSender s, ChatColor c, String m)
    {
        if (s != null)
            s.sendMessage(c + m);
        
        if ((s == null) || (s instanceof Player))
            log.log(Level.INFO, ChatColor.stripColor(m));
    }
    
    /**
     * Prints the given error message m to the given target
     * @param s Target (command line, player, etc.)
     * @param m Error message (will be displayed in red)
     */
    public static void error(CommandSender s, String m)
    {
        print(s, ChatColor.RED, m);
    }

    private static ComponentOrientation getDirection(Location loc)
    {
        double yaw = (loc.getYaw() - 90) % 360;
        double pitch = -loc.getPitch();
        
        if (yaw < 0)
            yaw += 360.0;
        
        return getDirection(yaw, pitch < 0 ? 0 : pitch);
    }
    
    private static ComponentOrientation getDirection(double yaw, double pitch)
    {
        boolean upright = pitch > 45;
        
        yaw += 45;
        yaw %= 360;
        
        if (yaw < 90)
            return upright ? ComponentOrientation.UPRIGHT_NORTH_SOUTH : ComponentOrientation.NORTH;
        else if (yaw < 180)
            return upright ? ComponentOrientation.UPRIGHT_EAST_WEST : ComponentOrientation.EAST;
        else if (yaw < 270)
            return upright ? ComponentOrientation.UPRIGHT_NORTH_SOUTH : ComponentOrientation.SOUTH;
        else
            return upright ? ComponentOrientation.UPRIGHT_EAST_WEST : ComponentOrientation.WEST;
    }
}
