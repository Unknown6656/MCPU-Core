package epsilonpotato.mcpu.core;

import java.util.HashMap;
import java.util.Set;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import epsilonpotato.mcpu.util.BlockHelper;
import epsilonpotato.mcpu.util.GenericHelper;
import epsilonpotato.mcpu.util.Triplet;


/**
 * Represents an abstract factory for creating integrated circuits
 * @param <T> The component type (extending {@link epsilonpotato.mcpu.core.IntegratedCircuit})
 * @author Unknown6656
 */
public abstract class ComponentFactory<T extends IntegratedCircuit>
{
    private static final HashMap<String, ComponentFactory<IntegratedCircuit>> register = new HashMap<>();
    /**
     * The component type informations, of which the current factory instance creates components
     */
    protected final Class<T> componentType = GenericHelper.getGenericClass();
    
    
    /**
     * Spawns a new component (use {@link ComponentFactory#spawnComponent(BlockPlacingContext, MCPUCore, Player, int, int, int, ComponentOrientation, int)} instead)
     * @param context The block placing context, into which the component will be placed
     * @param caller The caller plugin
     * @param p The player which invoked the component placing
     * @param x The component's desired lowest north-western corner's X-coordinates
     * @param y The component's desired lowest north-western corner's Y-coordinates
     * @param z The component's desired lowest north-western corner's Z-coordinates
     * @param or The component's desired orientation
     * @param iocount The component's desired I/O port count (normally only used by emulated processor instances)
     * @return The newly added component
     * @throws InvalidOrientationException Thrown, if the component was placed along an invalid orientation
     */
    protected abstract T spawnComponent(BlockPlacingContext context, MCPUCore caller, Player p, int x, int y, int z, ComponentOrientation or, int iocount) throws InvalidOrientationException;
    


    /**
     * Returns the component's estimated size in the given orientation (or 'null' if unknown)
     * @param or Component orientation
     * @return Estimated component size
     */
    public Triplet<Integer, Integer, Integer> getEstimatedSize(ComponentOrientation or)
    {
        return null;
    }
    
    /**
     * Registers the given factory using the given name
     * @param <U> Generic factory type
     * @param name Factory name
     * @param fac Factory instance
     * @throws Exception Thrown if a factory with the same name already exists
     */
    @SuppressWarnings("unchecked")
    public static final <U extends IntegratedCircuit> void registerFactory(String name, ComponentFactory<U> fac) throws Exception
    {
        if (register.containsKey(name))
            throw new Exception("A component with the name '" + name + "' has already been registered.");
        else
            register.put(name, (ComponentFactory<IntegratedCircuit>)fac);
    }
    
    /**
     * Creates a new component and spawns it into the given block placing context
     * @param context The block placing context, into which the component will be placed
     * @param caller The caller plugin
     * @param name The component factory's name
     * @param p The player which invoked the component placing
     * @param l The component's desired lowest north-western corner's location
     * @param or The component's desired orientation
     * @param iocount The component's desired I/O port count (normally only used by emulated processor instances)
     * @return The newly added component
     * @throws ClassNotFoundException Thrown, if the component factory could not be found
     * @throws InvalidOrientationException Thrown, if the component was placed along an invalid orientation
     */
    public static final IntegratedCircuit createComponent(BlockPlacingContext context, MCPUCore caller, String name, Player p, Location l, ComponentOrientation or, int iocount)
            throws ClassNotFoundException, InvalidOrientationException
    {
        return getFactoryByName(name).spawnComponent(context, caller, p, l.getBlockX(), l.getBlockY(), l.getBlockZ(), or, iocount);
    }
    
    /**
     * Creates a stone base inside the given block placing context if no solid support material could be found at the given coordinates
     * @param context Block placing context
     * @param x The lowest north-western corner's X-coordinates
     * @param y The lowest north-western corner's Y-coordinates
     * @param z The lowest north-western corner's Z-coordinates
     * @param xs The base's size in X-direction
     * @param zs The base's size in Z-direction
     */
    protected static final void createBase(BlockPlacingContext context, int x, int y, int z, int xs, int zs)
    {
        xs = Math.max(0, xs);
        zs = Math.max(0, zs);
        
        for (int i = 0; i < xs; ++i)
            for (int j = 0; j < zs; ++j)
            {
                Location loc = new Location(context.getWorld(), x + i, y, z + j);
                
                if (!BlockHelper.isOpaque(loc.getBlock()))
                    context.addBlock(x + i, y, z + j, Material.STONE);
            }
    }
    
    /**
     * Creates a black woollen frame inside the given block placing context at the given coordinates
     * @param context Block placing context
     * @param x The lowest north-western corner's X-coordinates
     * @param y The lowest north-western corner's Y-coordinates
     * @param z The lowest north-western corner's Z-coordinates
     * @param xs The frame's size in X-direction
     * @param ys The frame's size in Y-direction
     * @param zs The frame's size in Z-direction
     */
    @SuppressWarnings("deprecation")
    protected static final void createWoolFrame(BlockPlacingContext context, int x, int y, int z, int xs, int ys, int zs)
    {
        xs = Math.max(0, xs);
        ys = Math.max(0, ys);
        zs = Math.max(0, zs);
        
        for (int i = 0; i < xs; ++i)
            for (int j = 0; j < zs; ++j)
                for (int k = 0; k < ys; ++k)
                    context.addBlock(x + i, y + k, z + j, Material.WOOL, b -> b.setData(DyeColor.BLACK.getWoolData())); // TODO: fix deprecated calls
    }
    
    /**
     * Creates all pins for the given integrated circuit
     * @param <T> Integrated circuit type
     * @param context Block placing context
     * @param t Integrated circuit instance 
     */
    protected static final <T extends IntegratedCircuit> void createPins(BlockPlacingContext context, T t)
    {
        for (int i = 0, c = t.getIOCount(); i < c; ++i)
            context.addBlock(t.getIOLocation(i), Material.IRON_BLOCK);
    }
    
    /**
     * Returns the factory registered with the given name
     * @param name Factory name
     * @return Component factory
     * @throws ClassNotFoundException Thrown if the factory could not be found
     */
    public static final ComponentFactory<IntegratedCircuit> getFactoryByName(String name)
            throws ClassNotFoundException
    {
        if (register.containsKey(name))
            return register.get(name);
        else
            throw new ClassNotFoundException("The component factory '" + name + "' could not be found.");
    }

    /**
     * Returns the component factory associated with the given generic type {@link U}
     * @param <U> Generic type parameter (must inherit {@link IntegratedCircuit}
     * @return Component factory
     */
    public static final <U extends IntegratedCircuit> ComponentFactory<U> getFactory()
    {
        Class<U> typeU = GenericHelper.getGenericClass();
        
        return getFactory(typeU);
    }

    /**
     * Returns the component factory associated with the given generic type {@link U}
     * @param <U> Generic type parameter (must inherit {@link IntegratedCircuit}
     * @param type Generic type information
     * @return Component factory
     */
    @SuppressWarnings("unchecked")
    public static final <U extends IntegratedCircuit> ComponentFactory<U> getFactory(Class<U> type)
    {
        for (ComponentFactory<IntegratedCircuit> fac : register.values())
            if (fac != null)
                if (fac.componentType.isAssignableFrom(type))
                    return (ComponentFactory<U>)fac;
        
        return null;
    }
    
    /**
     * Returns a list of all registered factory names
     * @return List of factory names
     */
    public static Set<String> getRegisteredFactories()
    {
        return register.keySet();
    }
}
