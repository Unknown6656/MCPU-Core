package epsilonpotato.mcpu.core;

import java.util.HashMap;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Player;


public abstract class EmulatedProcessorFactory<T extends EmulatedProcessor>
{
    private static final HashMap<String, EmulatedProcessorFactory<EmulatedProcessor>> register = new HashMap<>();
    
    
    public abstract T createProcessor(Player p, Location l, Triplet<Integer, Integer, Integer> size, int iocount);

    @SuppressWarnings("unchecked")
    public static final <U extends EmulatedProcessor> void registerFactory(String archname, EmulatedProcessorFactory<U> fac)
    {
        register.put(archname, (EmulatedProcessorFactory<EmulatedProcessor>)fac);
    }
    
    public static final EmulatedProcessor createProcessor(String archname, Player p, Location l, Triplet<Integer, Integer, Integer> size, int iocount)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException
    {
        return getFactoryByArchitectureName(archname).createProcessor(p, l, size, iocount);
    }
    
    @SuppressWarnings("unchecked")
    public static final EmulatedProcessorFactory<EmulatedProcessor> getFactoryByArchitectureName(String archname)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException
    {
        try
        {
            return register.get(archname);
        }
        catch (Exception e)
        {
            return (EmulatedProcessorFactory<EmulatedProcessor>)Class.forName("epsilonpotato.mcpu." + archname + ".Factory").newInstance();
        }
    }

    public static Set<String> getRegisteredArchitectures()
    {
        return register.keySet();
    }
}
