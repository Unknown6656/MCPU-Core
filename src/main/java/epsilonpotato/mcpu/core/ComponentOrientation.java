package epsilonpotato.mcpu.core;

import java.util.HashMap;


public enum ComponentOrientation
{
    NORTH((byte)1),
    WEST((byte)2),
    SOUTH((byte)3),
    EAST((byte)4),
    UPRIGHT_NORTH_SOUTH((byte)5),
    UPRIGHT_EAST_WEST((byte)6);
    
    private static HashMap<Byte, ComponentOrientation> map = new HashMap<>();
    private byte value;
    
    static
    {
        for (ComponentOrientation or : ComponentOrientation.values())
            map.put(or.value, or);
    }
    
    
    private ComponentOrientation(byte value)
    {
        this.value = value;
    }
    
    public static ComponentOrientation fromValue(byte o)
    {
        return map.get(o);
    }
    
    public byte getValue()
    {
        return value;
    }
}
