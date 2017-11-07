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
    /**
     * Command to display the help/usage page
     */
    HELP("?", "help"),
    /**
     * Command to display the about/version page
     */
    ABOUT("about", "version"),
    /**
     * Command to add a new component
     */
    ADD("add"),
    /**
     * Command to add a new processor
     */
    ADD_PROCESSOR("addp", "addproc", "addprocessor"),
    /**
     * Command to delete a component
     */
    DELETE("delete", "remove"),
    /**
     * Command to load a book as code into a processor
     */
    LOAD_BOOK("loadb", "loadbook"),
    /**
     * Command to load an URI into a processor
     */
    LOAD_URI("loadu", "loaduri"),
    /**
     * Command to start a processor
     */
    START("start", "run"),
    /**
     * Command to stop a processor
     */
    STOP("stop", "halt"),
    /**
     * Command to reset a processor
     */
    RESET("reset"),
    /**
     * Command to force the execution of the next processor instruction
     */
    NEXT("next"),
    /**
     * Command to show the state of a component
     */
    STATE("state"),
    /**
     * Command to list all component's states
     */
    LIST("list"),
    /**
     * Command to list all architectures/component types
     */
    ARCH("arch", "architectures", "components"),
    /**
     * Command to unregister a component
     */
    UNREGISTER("ureg", "unreg", "unregister"),
    /**
     * Command to give a component register wand to the player
     */
    REGISTER("reg", "register"),
    /**
     * Command to give a processor register wand to the player
     */
    REGISTER_PROCESSOR("regp", "regproc", "regprocessor"),
    /**
     * Unknown command
     */
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
