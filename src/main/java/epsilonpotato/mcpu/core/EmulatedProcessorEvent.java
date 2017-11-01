package epsilonpotato.mcpu.core;

import java.io.Serializable;

public interface EmulatedProcessorEvent<T> extends Serializable
{
    static final long serialVersionUID = 0xe6de605c35e43246L;
    
    
    public void Raise(EmulatedProcessor proc, T data);
}
