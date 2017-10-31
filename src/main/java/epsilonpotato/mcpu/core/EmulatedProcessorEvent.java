package epsilonpotato.mcpu.core;

import java.io.Serializable;

public interface EmulatedProcessorEvent<T> extends Serializable
{
    public void Raise(EmulatedProcessor proc, T data);
}
