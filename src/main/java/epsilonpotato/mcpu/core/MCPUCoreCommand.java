package epsilonpotato.mcpu.core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;


/**
 * Represents an enumeration of possible MCPU core plugin commands
 * @author Unknown6656
 */
public enum MCPUCoreCommand implements Serializable
{
    HELP("?", "help"),
    ADD("add"),
    ADD_PROCESSOR("addp", "addproc", "addprocessor"),
    DELETE("delete", "remove"),
    LOAD_BOOK("loadb", "loadbook"),
    LOAD_URI("loadu", "loaduri"),
    START("start"),
    STOP("stop", "halt"),
    RESET("reset"),
    NEXT("next"),
    STATE("state"),
    LIST("list"),
    ARCH("arch", "architectures", "components"),
    UNREGISTER("ureg", "unreg", "unregister"),
    REGISTER("reg", "register"),
    REGISTER_PROCESSOR("regp", "regproc", "regprocessor"),
    UNKNOWN();
    
    
    private static final HashMap<String, MCPUCoreCommand> map = new HashMap<>();
    private final String[] val;
    

    static
    {
        for (MCPUCoreCommand cmd : MCPUCoreCommand.values())
            for (String v : cmd.val)
                map.put(v.toLowerCase().trim(), cmd);
    }
    
    private MCPUCoreCommand(String... cmds)
    {
        val = cmds;
    }
    
    /**
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString()
    {
        return String.join(", ", getValues());
    }
    
    /**
     * Returns a list of string commands which are associated with the current instance
     * @return List of string commands
     */
    public LinkedList<String> getValues()
    {
        LinkedList<String> cmds = new LinkedList<>();
        
        for (String cmd : map.keySet())
            if (map.get(cmd).equals(this))
                cmds.add(cmd);
        
        return cmds;
    }

    /**
     * Returns the command associated with the given string command
     * @param val String command
     * @return Command
     */
    public static MCPUCoreCommand getByValue(String val)
    {
        val = val.toLowerCase().trim();
        
        if (map.containsKey(val))
            return map.get(val);
        else
            return MCPUCoreCommand.UNKNOWN;
    }
    
    /**
     * Returns the set of all registered string commands
     * @return String command set
     */
    public static Set<String> getAllCommands()
    {
        return map.keySet();
    }
}
