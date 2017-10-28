package epsilonpotato.mcpu.core;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;


public enum MCPUCoreCommand
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
    
    @Override
    public String toString()
    {
        return String.join(", ", getValues());
    }
    
    public LinkedList<String> getValues()
    {
        LinkedList<String> cmds = new LinkedList<>();
        
        for (String cmd : map.keySet())
            if (map.get(cmd).equals(this))
                cmds.add(cmd);
        
        return cmds;
    }

    public static MCPUCoreCommand getByValue(String val)
    {
        val = val.toLowerCase().trim();
        
        if (map.containsKey(val))
            return map.get(val);
        else
            return MCPUCoreCommand.UNKNOWN;
    }
    
    public static Set<String> getAllCommands()
    {
        return map.keySet();
    }
}
