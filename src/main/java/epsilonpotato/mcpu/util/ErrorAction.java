package epsilonpotato.mcpu.util;


/**
 * Represents a function <pre>{@link T} -> ()</pre> which can possibly throw an exception of the type {@link E} 
 * @param <T> Generic parameter type {@link T}
 * @param <E> Generic exception type {@link E}
 * @author Unknown6656
 */
public interface ErrorAction<T, E extends Exception>
{
    /**
     * The evaluates the action with the given parameter
     * @param t Generic parameter {@link T}
     * @throws E Possibly thrown exception {@link E}
     */
    void eval(T t) throws E;
}
