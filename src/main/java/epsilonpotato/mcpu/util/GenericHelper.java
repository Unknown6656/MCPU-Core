package epsilonpotato.mcpu.util;

import java.lang.reflect.TypeVariable;

/**
 * A class containing generic helper methods
 * @author Unknown6656
 */
public final class GenericHelper
{
    private static final GenericHelper instance = new GenericHelper();
    
    private GenericHelper()
    {
    }
    
    
    /**
     * Returns the type information associated with the given generic type parameter {@link T}<br/>
     * <i>Equivalent to .NET's <pre>... = typeof(T);</pre></i>
     * @param <T> Generic type parameter
     * @return Generic type information
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> getGenericClass()
    {
        __<T> ins = instance.new __<T>();
        TypeVariable<?>[] cls = ins.getClass().getTypeParameters(); 

        return (Class<T>)cls[0].getClass();
    }
    
    private final class __<T>
    {
        private __()
        {
        }
    }
}
