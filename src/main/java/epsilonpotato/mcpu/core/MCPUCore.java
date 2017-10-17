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
                    " add               - Adds a new core to the world at the callers position\n" +
                    " add <x> <y> <z>   - Adds a new core to the world at the given coordinates\n" +
                    " remove <n>        - Removes the core No. n\n" +
                    " loadb[ook] <n>    - Loads the book hold in the hand into the core No. n\n" +
                    " loadu[ri] <n> <u> - Loads the string acessible via the given URI u into the core No. n\n" +
                    " start <n>         - Starts the processor core No. n\n" +
                    " stop <n>          - Halts the core No. n\n" +
                    " next <n>          - Forces the execution of the next instruction of core No. n" +
                    " reset <n>         - Halts and resets the core No. n\n" +
                    " state <n>         - Displays the state of core No. n";

        log = getLogger();

        getServer().getPluginManager().registerEvents(this, this);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> cores.values().forEach(c ->
        {
            c.nextInstruction();
            
            // TODO : update IOPorts here ?
            
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

    public abstract void onWorldSaveEvent(WorldSaveEvent event);

    public abstract void onWorldLoadEvent(WorldLoadEvent event);

    public abstract void onWorldInitEvent(WorldInitEvent event);

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (command.getName().equalsIgnoreCase("mcpu"))
        {
            if (args.length == 0)
                args = new String[]
                {
                    "?"
                };

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
                    Location loc;
                    int x, y, z;

                    if (sender instanceof Player)
                    {
                        player = (Player)sender;
                        loc = player.getLocation();
                        architecture = args[0];
                    }
                    else
                        try
                        {
                            x = Integer.parseInt(args[1]);
                            y = Integer.parseInt(args[2]);
                            z = Integer.parseInt(args[3]);
                            loc = new Location(getServer().getWorld(args[4]), x, y, z);
                            architecture = args[5];
                        }
                        catch (Exception e)
                        {
                            Print(sender, ChatColor.RED, "You must provide valid x-, y-, z-corrdinates, a valid world and architecture name for the creation of a new core.");

                            break;
                        }

                    x = loc.getBlockX();
                    y = loc.getBlockY();
                    z = loc.getBlockZ();

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
                case "delete":
                case "remove":
                    GetInt(args, 1, sender, i ->
                   {
                       if (cores.containsKey(i))
                       {
                           EmulatedProcessor c = cores.remove(i);

                           DeleteCPU(c.world, c.x, c.y, c.z, CPUSIZE);
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
                        String url = GetArg(argv, 2, sender);
                        StringBuilder code = new StringBuilder();

                        if (url != null)
                            try
                            {
                                if (!CompileLoad(sender, c, new URI(url)))
                                    Print(sender, ChatColor.RED, "The instructions could not be fetched from '" + url + "'.");
                            }
                            catch (URISyntaxException ex)
                            {
                                Print(sender, ChatColor.RED, "The instructions could not be fetched from '" + url + "'.");
                            }
                    });
                    break;
                case "state":
                    GetCore(args, 1, sender, c -> sender.sendMessage(c.getState()));
                    break;
                case "list":
                    cores.keySet().forEach((i) ->
                    {
                        EmulatedProcessor c = cores.get(i);

                        sender.sendMessage("[" + i + "]: " + c.getState());
                    });
                    break;
                default:
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

    private void DeleteCPU(World w, int x, int y, int z, int size)
    {
        int sdhl = (size / 2 /* Integer division */) * 2;

        for (int i = -sdhl - 2; i <= sdhl + 2; ++i)
            for (int j = -sdhl - 2; j <= sdhl + 2; ++j)
                for (int k = 3; k >= -1; --k)
                    SetBlock(w, x + i, y + k, z + j, Material.AIR);
    }

    @SuppressWarnings("deprecation")
    private <T extends EmulatedProcessor> Tuple<Integer, T> SpawnCPU(Player p, int x, int y, int z, World w, int size, EmulatedProcessorFactory<T> fac)
    {
        int num = cores.size();
        int sdhl = (size / 2 /* Integer division */) * 2;

        DeleteCPU(w, x, y, z, size);

        for (int i = -sdhl - 2; i <= sdhl + 2; ++i)
            for (int j = -sdhl - 2; j <= sdhl + 2; ++j)
                SetBlock(w, x + i, y - 1, z + j, Material.STONE);

        for (int i = -sdhl; i <= sdhl; ++i)
            for (int j = -sdhl; j <= sdhl; ++j)
                SetBlock(w, x + i, y, z + j, Material.WOOL, b -> b.setData(DyeColor.BLACK.getWoolData())); // TODO: fix deprecated calls

        SetBlock(w, x - sdhl, y, z - sdhl, Material.GOLD_BLOCK);
        SetBlock(w, x - sdhl + 1, y, z - sdhl, Material.GOLD_BLOCK);

        for (int i = -sdhl; i <= sdhl; i += 2)
        {
            SetBlock(w, x + i, y, z - sdhl - 1, Material.IRON_BLOCK);
            SetBlock(w, x + i, y, z + sdhl + 1, Material.IRON_BLOCK);
            // SetBlock(w, x + i, y - 1, z - sdhl - 2, Material.IRON_BLOCK);
            // SetBlock(w, x + i, y - 1, z + sdhl + 2, Material.IRON_BLOCK);
            SetBlock(w, x + i, y, z - sdhl - 2, Material.REDSTONE_WIRE);
            SetBlock(w, x + i, y, z + sdhl + 2, Material.REDSTONE_WIRE);
        }

        for (int j = -sdhl; j <= sdhl; j += 2)
        {
            SetBlock(w, x - sdhl - 1, y, z + j, Material.IRON_BLOCK);
            SetBlock(w, x + sdhl + 1, y, z + j, Material.IRON_BLOCK);
            // SetBlock(w, x - sdhl - 2, y - 1, z + j, Material.IRON_BLOCK);
            // SetBlock(w, x + sdhl + 2, y - 1, z + j, Material.IRON_BLOCK);
            SetBlock(w, x - sdhl - 2, y, z + j, Material.REDSTONE_WIRE);
            SetBlock(w, x + sdhl + 2, y, z + j, Material.REDSTONE_WIRE);
        }

        SetBlock(w, x - sdhl + 1, y + 1, z - sdhl, Material.LEVER, b ->
         {
             b.setData((byte)6);
             b.getState().update();
         });
        SetBlock(w, x - sdhl, y + 1, z - sdhl, Material.SIGN_POST, b ->
         {
             Sign sign = (Sign)b.getState();

             sign.setLine(0, "CPU No. " + num);
             sign.setLine(1, p.getDisplayName());
             sign.update();
         });

        int blsize = size * 2 + 1;
        T proc = fac.createProcessor(p, new Location(w, x - size - 1, y, z - size - 1), new Triplet<>(blsize, 2, blsize), size * 4);

        cores.put(num, proc);

        return new Tuple<>(num, proc);
    }

    private static Block SetBlock(World w, int x, int y, int z, Material m)
    {
        Block b = new Location(w, x, y, z).getBlock();

        b.setType(m);

        return b;
    }

    private static void SetBlock(World w, int x, int y, int z, Material m, Consumer<Block> f)
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
