package epsilonpotato.mcpu.core;

import java.io.Serializable;

/**
 * Represents a processor event handler method
 * @param <T> The event data type
 * @author Unknown6656
 */
public interface EmulatedProcessorEvent<T> extends Serializable
{
    /**
     * The serialisation unique ID
     */
    static final long serialVersionUID = 0xe6de605c35e43246L;
    
    
    /**
     * Executed if the current event was raised by the given processor
     * @param proc Emulated processor
     * @param data Event data
     */
    public void Raise(EmulatedProcessor proc, T data);
}
