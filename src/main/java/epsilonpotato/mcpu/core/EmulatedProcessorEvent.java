package epsilonpotato.mcpu.core;

public interface EmulatedProcessorEvent<T>
{
    public void Raise(EmulatedProcessor proc, T data);
}
