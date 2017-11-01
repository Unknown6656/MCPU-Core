package epsilonpotato.mcpu.util;

import java.io.Serializable;

public interface BiAction<T, U> extends Serializable
{
    static final long serialVersionUID = 0x172453d0b3e7fde3L;
    
    
    public void eval(T t, U u);
}
