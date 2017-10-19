package epsilonpotato.mcpu.core;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.entity.Player;


public abstract class EmulatedProcessorFactory<T extends EmulatedProcessor>
{
    private static final HashMap<String, EmulatedProcessorFactory<EmulatedProcessor>> register = new HashMap<>();
    
    
    public abstract T createProcessor(Player p, Location l, Triplet<Integer, Integer, Integer> size, int iocount);

    public static final void registerFactory(String archname, EmulatedProcessorFactory<EmulatedProcessor> fac)
    {
        register.put(archname, fac);
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
}
