package epsilonpotato.mcpu.core;

import java.util.HashMap;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;


public abstract class ComponentFactory<T extends IntegratedCircuit>
{
    private static final HashMap<String, ComponentFactory<IntegratedCircuit>> register = new HashMap<>();
    
    

    public abstract T spawnComponent(MCPUCore caller, Player p, World w, int x, int y, int z, ComponentOrientation or, int iocount) throws InvalidOrientationException;

    
    @SuppressWarnings("unchecked")
    public static final <U extends IntegratedCircuit> void registerFactory(String name, ComponentFactory<U> fac) throws Exception
    {
        if (register.containsKey(name))
            throw new Exception("A component with the name '" + name + "' has already been registered.");
        else
            register.put(name, (ComponentFactory<IntegratedCircuit>)fac);
    }
    
    public static final IntegratedCircuit createComponent(MCPUCore caller, String name, Player p, Location l, ComponentOrientation or, int iocount)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvalidOrientationException
    {
        return getFactoryByName(name).spawnComponent(caller, p, l.getWorld(), l.getBlockX(), l.getBlockY(), l.getBlockZ(), or, iocount);
    }
    
    public static final ComponentFactory<IntegratedCircuit> getFactoryByName(String name)
            throws ClassNotFoundException
    {
        if (register.containsKey(name))
            return register.get(name);
        else
            throw new ClassNotFoundException("The component factory '" + name + "' could not be found.");
    }

    public static Set<String> getRegisteredFactories()
    {
        return register.keySet();
    }
}