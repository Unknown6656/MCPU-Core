package epsilonpotato.mcpu.util;

import java.io.Serializable;

public final class Triplet<X, Y, Z> implements Serializable
{
    private static final long serialVersionUID = -8100911397280809156L;
    public X x;
    public Y y;
    public Z z;
    
    
    public Triplet(X x, Y y, Z z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
