
package epsilonpotato.mcpu.core;


import java.util.HashMap;


/**
 * Represents an enumeration of possible component orientations
 * 
 * @author Unknown6656
 */
public enum ComponentOrientation
{
    /**
     * The component's corner location is orientated towards NORTH, meaning that the player is facing NORTH while
     * placing the component behind his back
     */
    NORTH((byte)1),
    /**
     * The component's corner location is orientated towards WEST, meaning that the player is facing WEST while placing
     * the component behind his back
     */
    WEST((byte)2),
    /**
     * The component's corner location is orientated towards SOUTH, meaning that the player is facing SOUTH while
     * placing the component behind his back
     */
    SOUTH((byte)3),
    /**
     * The component's corner location is orientated towards EAST, meaning that the player is facing EAST while placing
     * the component behind his back
     */
    EAST((byte)4),
    /**
     * The component is orientated in an vertical (upright) position facing the north-south axis
     */
    UPRIGHT_NORTH_SOUTH((byte)5),
    /**
     * The component is orientated in an vertical (upright) position facing the east-west axis
     */
    UPRIGHT_EAST_WEST((byte)6);
    
    private static HashMap<Byte, ComponentOrientation> map = new HashMap<>();
    private byte value;
    
    static
    {
        for (ComponentOrientation or : ComponentOrientation.values())
            map.put(or.value, or);
    }
    
    
    /**
     * Returns whether the component is orientated along one of the following orientations:
     * {@link ComponentOrientation#NORTH}, {@link ComponentOrientation#SOUTH} or
     * {@link ComponentOrientation#UPRIGHT_NORTH_SOUTH}
     * @return
     */
    public boolean isNorthSouth()
    {
        return (this == ComponentOrientation.NORTH) || (this == ComponentOrientation.SOUTH) || (this == ComponentOrientation.UPRIGHT_NORTH_SOUTH);
    }
    
    /**
     * Returns whether the component is in an upright position
     * ({@link ComponentOrientation#UPRIGHT_NORTH_SOUTH} or {@link ComponentOrientation#UPRIGHT_EAST_WEST})
     * @return
     */
    public boolean isUpright()
    {
        return (this == UPRIGHT_EAST_WEST) || (this == ComponentOrientation.UPRIGHT_EAST_WEST);
    }
    
    private ComponentOrientation(byte value)
    {
        this.value = value;
    }
    
    /**
     * Returns the component orientation associated with the given byte value
     * @param o Byte value
     * @return Component orientation
     */
    public static ComponentOrientation fromValue(byte o)
    {
        return map.get(o);
    }
    
    /**
     * Returns the byte value associated with the current component orientation
     * @return Byte value
     */
    public byte getValue()
    {
        return value;
    }
}
