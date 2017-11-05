package epsilonpotato.mcpu.util;

import java.io.Serializable;

public interface ErrorConsumer<T, E extends Exception>
{
    void accept(T obj) throws E;
}
