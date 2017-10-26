package epsilonpotato.mcpu.util;

public interface ErrorConsumer<T, E extends Exception>
{
    void accept(T obj) throws E;
}
