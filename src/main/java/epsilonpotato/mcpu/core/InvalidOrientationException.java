package epsilonpotato.mcpu.core;

/**
 * Represents an exception thrown if a component has been placed along an invalid orientation
 * @author Unknown6656
 */
public final class InvalidOrientationException extends Exception
{
    private static final long serialVersionUID = 7623949597745140278L; // the fuck? why does java need this? w/ever....

 
    InvalidOrientationException(ComponentOrientation given, ComponentOrientation... accepted)
    {
        super(String.format("The component cannot be placed in the orientation '%s'; accepted orientations are: '%s'", given.toString(), join(accepted)));
    }
    
    @SafeVarargs
    private static <T> String join(T... arr)
    {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        
        sb.append('\'');
        
        for (T elem : arr)
        {
            sb.append(first ? "" : "', '").append(elem.toString());
            
            first = false;
        }
      
        return sb.append('\'').toString();
    }
}
