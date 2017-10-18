package epsilonpotato.mcpu.core;

import org.bukkit.Location;
import org.bukkit.entity.Player;


public abstract class EmulatedProcessorFactory<T extends EmulatedProcessor>
{
    public abstract String getArchitectureName();

    public abstract T createProcessor(Player p, Location l, Triplet<Integer, Integer, Integer> size, int iocount);

    @SuppressWarnings("unchecked")
    public static EmulatedProcessorFactory<EmulatedProcessor> getFactoryByArchitectureName(String nameString)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException
    {
        return (EmulatedProcessorFactory<EmulatedProcessor>)Class.forName("epsilonpotato.mcpu." + nameString + ".Factory").newInstance();
    }
}
